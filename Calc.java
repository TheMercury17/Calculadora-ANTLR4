import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
import java.io.InputStream;
import java.io.FileInputStream;

/**
 * Punto de entrada principal para la ejecucion de la Calculadora con ANTLR 4.
 * Procesa entradas desde un archivo o desde la entrada estandar (consola).
 */
public class Calc {
    public static void main(String[] args) throws Exception {
        InputStream is = System.in;
        if (args.length > 0) {
            is = new FileInputStream(args[0]);
        }

        CharStream input = CharStreams.fromStream(is);
        LabeledExprLexer lexer = new LabeledExprLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        LabeledExprParser parser = new LabeledExprParser(tokens);

        ParseTree tree = parser.prog();

        if (parser.getNumberOfSyntaxErrors() == 0) {
            EvalVisitor eval = new EvalVisitor();
            eval.visit(tree);
        } else {
            System.err.println("Se detectaron errores sintacticos en el flujo de entrada.");
        }
    }
}
