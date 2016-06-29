package gsmith.chaos.ui;

import java.awt.Image;

/** Something can hold and operate on an image.
 */
@FunctionalInterface
public interface Imagable {
    void setImage(Image i);
}
