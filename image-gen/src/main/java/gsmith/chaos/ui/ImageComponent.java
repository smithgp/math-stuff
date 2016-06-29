package gsmith.chaos.ui;

import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JComponent;

/** A component that paints an image.
 */
public class ImageComponent extends JComponent implements Imagable {
    private Image image;
    public ImageComponent() {
        super();
        image = null;
    }

    @Override
    public void setImage(Image image) {
        this.image = image;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.clearRect(0, 0, getWidth(), getHeight());
        if (image != null) {
            // Note: we could be more efficient by only drawing the changed parts,
            // but this works for now
            g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
        }
    }
}
