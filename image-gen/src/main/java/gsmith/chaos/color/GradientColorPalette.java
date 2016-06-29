package gsmith.chaos.color;

import java.awt.Color;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.Properties;

/** Do a simple linear gradient color palette.
 */
public class GradientColorPalette implements ColorPalette {
    private static final Color DEFAULT_START_COLOR = Color.RED;
    private static final Color DEFAULT_END_COLOR = Color.BLUE;

    private Color start = DEFAULT_START_COLOR;
    private Color end = DEFAULT_END_COLOR;
    private int numSteps = 10;

    @Override
    public void init(Properties p, int maxIterations) {
        this.numSteps = maxIterations;

        String s = p.getProperty("start");
        if (s != null) {
            readColor(s.trim()).ifPresent(this::setStartColor);
        }
        s = p.getProperty("end");
        if (s != null) {
            readColor(s.trim()).ifPresent(this::setEndColor);
        }
    }

    public Color getStartColor() {
        return start;
    }

    public void setStartColor(Color c) {
        start = c != null ? c : DEFAULT_START_COLOR;
    }

    public Color getEndColor() {
        return end;
    }

    public void setEndColor(Color c) {
        end = c != null ? c : DEFAULT_END_COLOR;
    }

    public int getNumSteps() {
        return numSteps;
    }

    public void setNumSteps(int steps) {
        this.numSteps = steps > 0 ? steps : 10;
    }

    @Override
    public Color getColor(int index) {
        if (index <= 0) {
            return Color.BLACK;
        }
        return getGradientColor(index, numSteps, start, end);
    }

    /** Get the color the specified step of numSteps of the gradient.
     * @param step the step number of the gradient.
     * @param numSteps the number of steps in the gradient.
     * @param start the start color.
     * @param end the end color.
     * @return the color.
     */
    public static Color getGradientColor(int step, int numSteps, Color start, Color end) {
        double ratio = (double)Math.min(step, numSteps) / (double)numSteps;
        return new Color((int)(end.getRed() * ratio + start.getRed() * (1 - ratio)),
                         (int)(end.getGreen() * ratio + start.getGreen() * (1 - ratio)),
                         (int)(end.getBlue() * ratio + start.getBlue() * (1 - ratio)),
                         start.getAlpha());
    }

    /** Read a color from the specified string.
     * The passed in string can be a 0xHex, 0Octal, or decimal int of the RGB values,
     * or can be the name of one of the static Color fields in java.awt.Color (e.g. "red", "cyan");
     */
    public static Optional<Color> readColor(String s) {
        try {
            return Optional.of(Color.decode(s));
        }
        catch (NumberFormatException ex) {
            try {
                // see if the name is one of the static Color fields
                Field f = Color.class.getDeclaredField(s);
                if (Modifier.isStatic(f.getModifiers()) && Color.class.equals(f.getType())) {
                    return Optional.ofNullable((Color)f.get(null));
                }
            }
            catch (NoSuchFieldException | IllegalAccessException ignore) {
                // no match or not public so return empty below
            }
        }
        return Optional.empty();
    }
}
