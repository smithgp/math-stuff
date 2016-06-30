package gsmith.chaos.ui;

import java.awt.Image;

/** Something can hold and operate on an image.
 */
@FunctionalInterface
public interface Imagable<I extends Image> {
    void setImage(I i);
}
