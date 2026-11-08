package experiments;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

final class Plotter {
    static final class Series {
        final String label;
        final double[] y;
        final Color color;
        final boolean dashed;

        Series(String label, double[] y, Color color, boolean dashed) {
            this.label = label;
            this.y = y;
            this.color = color;
            this.dashed = dashed;
        }
    }

    private static final int W = 900, H = 580;
    private static final int LEFT = 90, RIGHT = 30, TOP = 60, BOTTOM = 80;

    static void lineChart(String title, String xLabel, String yLabel, double[] x,
                          List<Series> series, File out) throws IOException {
        BufferedImage img = new BufferedImage(W, H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, W, H);

        int px0 = LEFT, px1 = W - RIGHT, py0 = H - BOTTOM, py1 = TOP;
        double xMin = x[0], xMax = x[x.length - 1];
        double yMaxData = 0;
        for (Series s : series) for (double v : s.y) yMaxData = Math.max(yMaxData, v);
        double step = niceStep(yMaxData / 6.0);
        double yMax = Math.ceil(yMaxData / step) * step;
        if (yMax <= 0) yMax = 1;

        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.BOLD, 20));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, (W - fm.stringWidth(title)) / 2, 34);

        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        fm = g.getFontMetrics();
        for (double v = 0; v <= yMax + step / 2; v += step) {
            int py = (int) Math.round(py0 - (v / yMax) * (py0 - py1));
            g.setColor(new Color(225, 225, 225));
            g.drawLine(px0, py, px1, py);
            g.setColor(Color.BLACK);
            String t = formatTick(v, step);
            g.drawString(t, px0 - 8 - fm.stringWidth(t), py + 5);
        }
        for (double xv : x) {
            int px = (int) Math.round(px0 + (xv - xMin) / (xMax - xMin) * (px1 - px0));
            g.setColor(new Color(225, 225, 225));
            g.drawLine(px, py0, px, py1);
            g.setColor(Color.BLACK);
            String t = String.valueOf((long) xv);
            g.drawString(t, px - fm.stringWidth(t) / 2, py0 + 20);
        }
        g.setStroke(new BasicStroke(1.5f));
        g.setColor(Color.BLACK);
        g.drawLine(px0, py0, px1, py0);
        g.drawLine(px0, py0, px0, py1);

        g.setFont(new Font("SansSerif", Font.PLAIN, 15));
        fm = g.getFontMetrics();
        g.drawString(xLabel, (px0 + px1) / 2 - fm.stringWidth(xLabel) / 2, py0 + 45);
        AffineTransform old = g.getTransform();
        g.rotate(-Math.PI / 2);
        g.drawString(yLabel, -((py0 + py1) / 2 + fm.stringWidth(yLabel) / 2), 28);
        g.setTransform(old);

        for (Series s : series) {
            float[] dash = s.dashed ? new float[]{8f, 6f} : null;
            g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10f, dash, 0f));
            g.setColor(s.color);
            int prevX = 0, prevY = 0;
            for (int i = 0; i < x.length; i++) {
                int px = (int) Math.round(px0 + (x[i] - xMin) / (xMax - xMin) * (px1 - px0));
                int py = (int) Math.round(py0 - (s.y[i] / yMax) * (py0 - py1));
                if (i > 0) g.drawLine(prevX, prevY, px, py);
                prevX = px;
                prevY = py;
            }
            g.setStroke(new BasicStroke(1.5f));
            for (int i = 0; i < x.length; i++) {
                int px = (int) Math.round(px0 + (x[i] - xMin) / (xMax - xMin) * (px1 - px0));
                int py = (int) Math.round(py0 - (s.y[i] / yMax) * (py0 - py1));
                g.fillOval(px - 4, py - 4, 8, 8);
            }
        }

        g.setFont(new Font("SansSerif", Font.PLAIN, 13));
        fm = g.getFontMetrics();
        int lx = px0 + 15, ly = py1 + 15;
        int boxW = 0;
        for (Series s : series) boxW = Math.max(boxW, fm.stringWidth(s.label));
        boxW += 60;
        int boxH = series.size() * 20 + 10;
        g.setColor(new Color(255, 255, 255, 225));
        g.fillRect(lx - 8, ly - 12, boxW, boxH);
        g.setColor(new Color(180, 180, 180));
        g.setStroke(new BasicStroke(1f));
        g.drawRect(lx - 8, ly - 12, boxW, boxH);
        for (Series s : series) {
            float[] dash = s.dashed ? new float[]{6f, 4f} : null;
            g.setStroke(new BasicStroke(2.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10f, dash, 0f));
            g.setColor(s.color);
            g.drawLine(lx, ly, lx + 30, ly);
            g.setColor(Color.BLACK);
            g.drawString(s.label, lx + 38, ly + 5);
            ly += 20;
        }
        g.dispose();
        ImageIO.write(img, "png", out);
    }

    private static double niceStep(double raw) {
        if (raw <= 0) return 1;
        double exp = Math.pow(10, Math.floor(Math.log10(raw)));
        double f = raw / exp;
        double nice = f <= 1 ? 1 : f <= 2 ? 2 : f <= 5 ? 5 : 10;
        return nice * exp;
    }

    private static String formatTick(double v, double step) {
        if (step >= 1) return String.format("%.0f", v);
        int decimals = (int) Math.max(1, Math.ceil(-Math.log10(step)));
        return String.format("%." + decimals + "f", v);
    }

    static List<Series> newList() { return new ArrayList<>(); }
}
