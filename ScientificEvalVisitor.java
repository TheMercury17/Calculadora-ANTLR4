import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Evaluador semantico para la Calculadora Cientifica Graficadora.
 * Extiende ScientificCalcBaseVisitor con tipo de retorno Double para soporte
 * de numeros reales, funciones trigonometricas, trascendentes, constantes,
 * variables dinámicas y comandos de graficacion.
 */
public class ScientificEvalVisitor extends ScientificCalcBaseVisitor<Double> {

    // Tabla de simbolos / Memoria de variables
    private final Map<String, Double> memory = new HashMap<>();

    /**
     * Muestra el resultado de evaluar una expresion.
     */
    @Override
    public Double visitPrintExpr(ScientificCalcParser.PrintExprContext ctx) {
        Double value = visit(ctx.expr());
        if (value != null) {
            System.out.println(value);
        }
        return value;
    }

    /**
     * Asignacion de variables en la tabla de simbolos: ID '=' expr NEWLINE
     */
    @Override
    public Double visitAssign(ScientificCalcParser.AssignContext ctx) {
        String id = ctx.ID().getText();
        Double value = visit(ctx.expr());
        if (value != null) {
            memory.put(id, value);
        }
        return value;
    }

    /**
     * Comando para limpiar la memoria de variables.
     */
    @Override
    public Double visitClear(ScientificCalcParser.ClearContext ctx) {
        memory.clear();
        System.out.println("Memoria eliminada.");
        return 0.0;
    }

    /**
     * Comando 'vars' para listar todas las variables registradas.
     */
    @Override
    public Double visitShowVars(ScientificCalcParser.ShowVarsContext ctx) {
        if (memory.isEmpty()) {
            System.out.println("No hay variables definidas.");
            return 0.0;
        }
        for (Map.Entry<String, Double> entry : memory.entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }
        return 0.0;
    }

    /**
     * Linea en blanco.
     */
    @Override
    public Double visitBlank(ScientificCalcParser.BlankContext ctx) {
        return 0.0;
    }

    /**
     * Evaluacion de literales numericos enteros y reales.
     */
    @Override
    public Double visitNumber(ScientificCalcParser.NumberContext ctx) {
        return Double.parseDouble(ctx.NUMBER().getText());
    }

    /**
     * Recuperacion de identificador/variable desde la memoria.
     */
    @Override
    public Double visitId(ScientificCalcParser.IdContext ctx) {
        String id = ctx.ID().getText();
        if (memory.containsKey(id)) {
            return memory.get(id);
        }
        System.err.println("Variable no definida: " + id);
        return 0.0;
    }

    /**
     * Operacion de potencia: base ^ exponente (asociativa por derecha)
     */
    @Override
    public Double visitPower(ScientificCalcParser.PowerContext ctx) {
        Double base = visit(ctx.expr(0));
        Double exponent = visit(ctx.expr(1));
        if (base == null || exponent == null) return 0.0;
        return Math.pow(base, exponent);
    }

    /**
     * Operadores unarios (+ y -).
     */
    @Override
    public Double visitUnary(ScientificCalcParser.UnaryContext ctx) {
        Double value = visit(ctx.expr());
        if (value == null) return 0.0;
        if (ctx.op.getText().equals("-")) {
            return -value;
        }
        return value;
    }

    /**
     * Multiplicacion y division.
     */
    @Override
    public Double visitMulDiv(ScientificCalcParser.MulDivContext ctx) {
        Double left = visit(ctx.expr(0));
        Double right = visit(ctx.expr(1));
        if (left == null || right == null) return 0.0;

        if (ctx.op.getType() == ScientificCalcParser.MUL) {
            return left * right;
        } else {
            if (right == 0.0) {
                System.err.println("Error semantico: Division por cero en '" + ctx.getText() + "'.");
            }
            return left / right;
        }
    }

    /**
     * Suma y resta.
     */
    @Override
    public Double visitAddSub(ScientificCalcParser.AddSubContext ctx) {
        Double left = visit(ctx.expr(0));
        Double right = visit(ctx.expr(1));
        if (left == null || right == null) return 0.0;

        if (ctx.op.getType() == ScientificCalcParser.ADD) {
            return left + right;
        }
        return left - right;
    }

    /**
     * Evaluacion de funciones cientificas de 1 argumento.
     */
    @Override
    public Double visitFunctionCall(ScientificCalcParser.FunctionCallContext ctx) {
        String function = ctx.function().getText();
        Double value = visit(ctx.expr());
        if (value == null) return 0.0;

        switch (function) {
            case "sin":   return Math.sin(value);
            case "cos":   return Math.cos(value);
            case "tan":   return Math.tan(value);
            case "asin":  return Math.asin(value);
            case "acos":  return Math.acos(value);
            case "atan":  return Math.atan(value);
            case "sqrt":  return Math.sqrt(value);
            case "log":   return Math.log10(value);
            case "ln":    return Math.log(value);
            case "abs":   return Math.abs(value);
            case "exp":   return Math.exp(value);
            case "floor": return Math.floor(value);
            case "ceil":  return Math.ceil(value);
            default:
                throw new RuntimeException("Funcion desconocida: " + function);
        }
    }

    /**
     * Evaluacion de funciones de 2 argumentos (Reto 2: pow, max, min).
     */
    @Override
    public Double visitFunction2Call(ScientificCalcParser.Function2CallContext ctx) {
        String function = ctx.function2().getText();
        Double v1 = visit(ctx.expr(0));
        Double v2 = visit(ctx.expr(1));
        if (v1 == null || v2 == null) return 0.0;

        switch (function) {
            case "pow": return Math.pow(v1, v2);
            case "max": return Math.max(v1, v2);
            case "min": return Math.min(v1, v2);
            default:
                throw new RuntimeException("Funcion binaria desconocida: " + function);
        }
    }

    /**
     * Evaluacion de constantes matematicas predefinidas (pi, e).
     */
    @Override
    public Double visitConstantExpr(ScientificCalcParser.ConstantExprContext ctx) {
        String constant = ctx.constant().getText();
        if (constant.equals("pi")) {
            return Math.PI;
        }
        if (constant.equals("e")) {
            return Math.E;
        }
        return 0.0;
    }

    /**
     * Expresiones entre parentesis.
     */
    @Override
    public Double visitParens(ScientificCalcParser.ParensContext ctx) {
        return visit(ctx.expr());
    }

    /**
     * Comando de graficacion estandar: plot(expr, xmin, xmax)
     */
    @Override
    public Double visitPlotExpr(ScientificCalcParser.PlotExprContext ctx) {
        Double xmin = visit(ctx.expr(1));
        Double xmax = visit(ctx.expr(2));
        if (xmin == null || xmax == null) return 0.0;

        int samples = 800;
        List<Double> xs = new ArrayList<>();
        List<Double> ys = new ArrayList<>();

        Double prevX = memory.get("x");
        for (int i = 0; i < samples; i++) {
            double x = xmin + i * (xmax - xmin) / (samples - 1);
            memory.put("x", x);
            Double y = visit(ctx.expr(0));
            if (y != null && Double.isFinite(y)) {
                xs.add(x);
                ys.add(y);
            }
        }
        if (prevX != null) memory.put("x", prevX); else memory.remove("x");

        String title = "f(x) = " + ctx.expr(0).getText();
        new PlotWindow(title, xs, ys);
        System.out.println("Graficando funcion: " + title + " en [" + xmin + ", " + xmax + "]");
        return 0.0;
    }

    /**
     * Comando de graficacion con limites verticales explicitos (Reto 3): plot(expr, xmin, xmax, ymin, ymax)
     */
    @Override
    public Double visitPlotRangeExpr(ScientificCalcParser.PlotRangeExprContext ctx) {
        Double xmin = visit(ctx.expr(1));
        Double xmax = visit(ctx.expr(2));
        Double ymin = visit(ctx.expr(3));
        Double ymax = visit(ctx.expr(4));
        if (xmin == null || xmax == null || ymin == null || ymax == null) return 0.0;

        int samples = 800;
        List<Double> xs = new ArrayList<>();
        List<Double> ys = new ArrayList<>();

        Double prevX = memory.get("x");
        for (int i = 0; i < samples; i++) {
            double x = xmin + i * (xmax - xmin) / (samples - 1);
            memory.put("x", x);
            Double y = visit(ctx.expr(0));
            if (y != null && Double.isFinite(y)) {
                xs.add(x);
                ys.add(y);
            }
        }
        if (prevX != null) memory.put("x", prevX); else memory.remove("x");

        String title = "f(x) = " + ctx.expr(0).getText();
        new PlotWindow(title, xs, ys, ymin, ymax);
        System.out.println("Graficando funcion con rango: " + title + " en X: [" + xmin + ", " + xmax + "], Y: [" + ymin + ", " + ymax + "]");
        return 0.0;
    }

    /**
     * Comando de graficacion para multiples funciones simultaneas (Reto 4): plot(expr1, expr2, xmin, xmax)
     */
    @Override
    public Double visitPlotMultiExpr(ScientificCalcParser.PlotMultiExprContext ctx) {
        Double xmin = visit(ctx.expr(2));
        Double xmax = visit(ctx.expr(3));
        if (xmin == null || xmax == null) return 0.0;

        int samples = 800;
        List<Double> xs1 = new ArrayList<>();
        List<Double> ys1 = new ArrayList<>();
        List<Double> xs2 = new ArrayList<>();
        List<Double> ys2 = new ArrayList<>();

        Double prevX = memory.get("x");
        for (int i = 0; i < samples; i++) {
            double x = xmin + i * (xmax - xmin) / (samples - 1);
            memory.put("x", x);
            Double y1 = visit(ctx.expr(0));
            Double y2 = visit(ctx.expr(1));

            if (y1 != null && Double.isFinite(y1)) {
                xs1.add(x);
                ys1.add(y1);
            }
            if (y2 != null && Double.isFinite(y2)) {
                xs2.add(x);
                ys2.add(y2);
            }
        }
        if (prevX != null) memory.put("x", prevX); else memory.remove("x");

        List<PlotWindow.Series> series = new ArrayList<>();
        series.add(new PlotWindow.Series("f1(x) = " + ctx.expr(0).getText(), xs1, ys1, new Color(41, 128, 185)));
        series.add(new PlotWindow.Series("f2(x) = " + ctx.expr(1).getText(), xs2, ys2, new Color(231, 76, 60)));

        String title = "Comparativa: " + ctx.expr(0).getText() + " vs " + ctx.expr(1).getText();
        new PlotWindow(title, series, null, null);
        System.out.println("Graficando multiples curvas: " + ctx.expr(0).getText() + " y " + ctx.expr(1).getText());
        return 0.0;
    }
}
