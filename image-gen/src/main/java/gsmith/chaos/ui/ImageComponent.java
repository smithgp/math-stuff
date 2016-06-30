package gsmith.chaos.ui;

import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JComponent;

/** A component that paints an image.
 */
public class ImageComponent<I extends Image> extends JComponent implements Imagable<I> {
    private I image;
    public ImageComponent() {
        super();
        image = null;
    }

    public I getImage() {
        return image;
    }

    @Override
    public void setImage(I image) {
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
