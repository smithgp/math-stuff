package gsmith.chaos.color;

import java.awt.Color;
import java.util.Properties;

/** Implements an indexed palette of colors.
 */
@FunctionalInterface
public interface ColorPalette {
    /** Get the color to use the specified index.
     * This should return the same Color for an index value.
     */
    Color getColor(int index);

    /** Initialize the color palette from the specified properties.
     */
    default void init(Properties p, int maxSteps) {
    }

    /** Reset the palette.
     */
    default void reset() {
    }
}
