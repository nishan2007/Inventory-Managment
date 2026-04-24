import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DocScreenshotGenerator {
    private static final int WIDTH = 1280;
    private static final int PADDING = 36;
    private static final int TITLE_HEIGHT = 44;
    private static final Font TITLE_FONT = new Font("SansSerif", Font.BOLD, 28);
    private static final Font BODY_FONT = new Font("Monospaced", Font.PLAIN, 20);
    private static final Color BG = new Color(245, 247, 250);
    private static final Color PANEL = new Color(255, 255, 255);
    private static final Color BORDER = new Color(214, 220, 228);
    private static final Color TITLE = new Color(31, 41, 55);
    private static final Color TEXT = new Color(55, 65, 81);

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            throw new IllegalArgumentException("Usage: DocScreenshotGenerator <title> <output-file>");
        }

        String title = args[0];
        Path output = Path.of(args[1]);
        Path input = Path.of(output.toString().replaceAll("\\.png$", ".txt"));
        String body = Files.readString(input);

        BufferedImage probe = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        Graphics2D probeGraphics = probe.createGraphics();
        probeGraphics.setFont(BODY_FONT);
        FontMetrics bodyMetrics = probeGraphics.getFontMetrics();
        int lineHeight = bodyMetrics.getHeight() + 6;
        int lines = Math.max(1, body.split("\\R", -1).length);
        probeGraphics.dispose();

        int height = PADDING * 2 + TITLE_HEIGHT + (lines * lineHeight) + 40;
        BufferedImage image = new BufferedImage(WIDTH, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(BG);
        g.fillRect(0, 0, WIDTH, height);

        int panelX = PADDING;
        int panelY = PADDING;
        int panelW = WIDTH - (PADDING * 2);
        int panelH = height - (PADDING * 2);

        g.setColor(PANEL);
        g.fillRoundRect(panelX, panelY, panelW, panelH, 28, 28);
        g.setColor(BORDER);
        g.setStroke(new BasicStroke(2f));
        g.drawRoundRect(panelX, panelY, panelW, panelH, 28, 28);

        g.setFont(TITLE_FONT);
        g.setColor(TITLE);
        g.drawString(title, panelX + 24, panelY + 38);

        g.setFont(BODY_FONT);
        g.setColor(TEXT);
        FontMetrics metrics = g.getFontMetrics();
        int textY = panelY + TITLE_HEIGHT + 34;
        for (String line : body.split("\\R", -1)) {
            g.drawString(line, panelX + 24, textY);
            textY += metrics.getHeight() + 6;
        }

        g.dispose();
        ImageIO.write(image, "png", output.toFile());
    }
}
