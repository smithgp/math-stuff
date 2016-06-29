package gsmith.chaos.color;

import static gsmith.chaos.color.GradientColorPalette.getGradientColor;
import static gsmith.chaos.color.GradientColorPalette.readColor;

import java.awt.Color;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

/** A color palette that uses a linear gradient between its configured colors.
 */
public class MultiGradientColorPalette implements ColorPalette {
    private static final Color[] DEFAULT_COLORS = {
            Color.red,
            Color.cyan,
            Color.blue,
            Color.magenta,
            Color.green,
            Color.yellow
    };

    private Color[] colors = DEFAULT_COLORS;
    private int maxSteps = (DEFAULT_COLORS.length - 1) * 10;

    @Override
    public void init(Properties p, int maxSteps) {
        this.maxSteps = maxSteps;

        String s = p.getProperty("colors");
        if (s != null) {
            StringTokenizer toker = new StringTokenizer(s, ",; ");
            List<Color> colors = new LinkedList<>();
            while (toker.hasMoreTokens()) {
                readColor(toker.nextToken().trim()).ifPresent(colors::add);
            }
            if (!colors.isEmpty()) {
                this.colors = colors.toArray(new Color[colors.size()]);
            }
            else {
                this.colors = DEFAULT_COLORS;
            }
        }
    }

    @Override
    public Color getColor(int step) {
        // for the index, find the correspond start/end colors from our set of colors
        if (step <= 0) {
            return Color.black;
        }

        // max out
        step = Math.min(step, maxSteps);
        // find out how many steps are for each gradient pair
        int bucketSize = maxSteps / (colors.length - 1);
        // the left-overs go in a last bucket
        if (maxSteps % (colors.length - 1) > 0) {
            bucketSize++;
        }
        // start color
        int i = (step - 1) / bucketSize;

        // do a gradient between the start color and the next color
        return getGradientColor(((step - 1) % bucketSize) + 1, bucketSize, colors[i], colors[i + 1]);
    }
}