import javax.swing.*;
import java.awt.*;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Componente visual para la representacion grafica de funciones matematicas.
 * Permite graficar una o varias curvas en un plano cartesiano interactivo con
 * transformacion de coordenadas, deteccion de limites y manejo de asintotas/discontinuidades.
 */
public class PlotWindow extends JPanel {

    public static class Series {
        public String title;
        public List<Double> xs;
        public List<Double> ys;
        public Color color;

        public Series(String title, List<Double> xs, List<Double> ys, Color color) {
            this.title = title;
            this.xs = xs;
            this.ys = ys;
            this.color = color;
        }
    }

    private final List<Series> seriesList = new ArrayList<>();
    private final Double customYmin;
    private final Double customYmax;
    private final String chartTitle;

    public PlotWindow(String title, List<Double> xs, List<Double> ys) {
        this(title, xs, ys, null, null);
    }

    public PlotWindow(String title, List<Double> xs, List<Double> ys, Double ymin, Double ymax) {
        this.chartTitle = title;
        this.customYmin = ymin;
        this.customYmax = ymax;
        this.seriesList.add(new Series(title, xs, ys, new Color(41, 128, 185)));
        initFrame();
    }

    public PlotWindow(String title, List<Series> series, Double ymin, Double ymax) {
        this.chartTitle = title;
        this.customYmin = ymin;
        this.customYmax = ymax;
        this.seriesList.addAll(series);
        initFrame();
    }

    private void initFrame() {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame(chartTitle != null ? chartTitle : "Calculadora Cientifica - Grafica");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(850, 650);
            frame.setLocationRelativeTo(null);
            frame.add(this);
            frame.setVisible(true);
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();
        int padding = 50;

        // Fondo
        g2.setColor(new Color(250, 250, 252));
        g2.fillRect(0, 0, width, height);

        if (seriesList.isEmpty()) {
            return;
        }

        // Determinar limites globales
        double globalXmin = Double.MAX_VALUE;
        double globalXmax = -Double.MAX_VALUE;
        double globalYmin = (customYmin != null) ? customYmin : Double.MAX_VALUE;
        double globalYmax = (customYmax != null) ? customYmax : -Double.MAX_VALUE;

        for (Series s : seriesList) {
            for (double x : s.xs) {
                if (Double.isFinite(x)) {
                    globalXmin = Math.min(globalXmin, x);
                    globalXmax = Math.max(globalXmax, x);
                }
            }
            if (customYmin == null || customYmax == null) {
                for (double y : s.ys) {
                    if (Double.isFinite(y)) {
                        globalYmin = Math.min(globalYmin, y);
                        globalYmax = Math.max(globalYmax, y);
                    }
                }
            }
        }

        if (globalXmin >= globalXmax) {
            globalXmin = -10.0;
            globalXmax = 10.0;
        }
        if (globalYmin >= globalYmax) {
            globalYmin = -1.0;
            globalYmax = 1.0;
        }

        // Margen visual vertical
        if (customYmin == null && customYmax == null) {
            double yRange = globalYmax - globalYmin;
            if (yRange == 0) yRange = 1.0;
            globalYmin -= yRange * 0.05;
            globalYmax += yRange * 0.05;
        }

        int plotWidth = width - 2 * padding;
        int plotHeight = height - 2 * padding;

        // Dibujar cuadricula y ejes
        g2.setColor(new Color(225, 228, 232));
        g2.setStroke(new BasicStroke(1.0f));

        // Divisiones de cuadricula
        int gridSteps = 10;
        for (int i = 0; i <= gridSteps; i++) {
            int gx = padding + i * plotWidth / gridSteps;
            int gy = padding + i * plotHeight / gridSteps;
            g2.drawLine(gx, padding, gx, height - padding);
            g2.drawLine(padding, gy, width - padding, gy);
        }

        // Posicion de ejes cartesianos (X=0 y Y=0)
        int originX = padding + (int) ((0.0 - globalXmin) / (globalXmax - globalXmin) * plotWidth);
        int originY = (height - padding) - (int) ((0.0 - globalYmin) / (globalYmax - globalYmin) * plotHeight);

        g2.setColor(new Color(100, 110, 125));
        g2.setStroke(new BasicStroke(1.5f));

        // Eje X si es visible
        if (originY >= padding && originY <= height - padding) {
            g2.drawLine(padding, originY, width - padding, originY);
        } else {
            g2.drawLine(padding, height - padding, width - padding, height - padding);
        }

        // Eje Y si es visible
        if (originX >= padding && originX <= width - padding) {
            g2.drawLine(originX, padding, originX, height - padding);
        } else {
            g2.drawLine(padding, padding, padding, height - padding);
        }

        // Etiquetas de limites
        g2.setColor(new Color(70, 80, 95));
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        g2.drawString(String.format("x_min: %.2f", globalXmin), padding, height - padding + 20);
        g2.drawString(String.format("x_max: %.2f", globalXmax), width - padding - 75, height - padding + 20);
        g2.drawString(String.format("y_max: %.2f", globalYmax), padding - 40, padding - 10);
        g2.drawString(String.format("y_min: %.2f", globalYmin), padding - 40, height - padding + 5);

        // Dibujar curvas
        for (Series series : seriesList) {
            if (series.xs.size() < 2) continue;

            g2.setColor(series.color != null ? series.color : Color.BLUE);
            g2.setStroke(new BasicStroke(2.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

            Path2D.Double path = new Path2D.Double();
            boolean inSegment = false;

            for (int i = 0; i < series.xs.size(); i++) {
                double x = series.xs.get(i);
                double y = series.ys.get(i);

                if (!Double.isFinite(x) || !Double.isFinite(y) || y < globalYmin - 100 || y > globalYmax + 100) {
                    inSegment = false;
                    continue;
                }

                double px = padding + ((x - globalXmin) / (globalXmax - globalXmin)) * plotWidth;
                double py = (height - padding) - ((y - globalYmin) / (globalYmax - globalYmin)) * plotHeight;

                if (!inSegment) {
                    path.moveTo(px, py);
                    inSegment = true;
                } else {
                    path.lineTo(px, py);
                }
            }

            g2.draw(path);
        }

        // Leyenda
        int legendX = padding + 15;
        int legendY = padding + 25;
        g2.setFont(new Font("SansSerif", Font.BOLD, 13));
        for (Series s : seriesList) {
            g2.setColor(s.color != null ? s.color : Color.BLUE);
            g2.fillRect(legendX, legendY - 10, 14, 14);
            g2.setColor(Color.DARK_GRAY);
            g2.drawString(s.title != null ? s.title : "Curva", legendX + 20, legendY + 2);
            legendY += 20;
        }
    }
}
