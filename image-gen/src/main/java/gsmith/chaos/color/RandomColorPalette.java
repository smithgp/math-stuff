package gsmith.chaos.color;

import java.awt.Color;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class RandomColorPalette extends CachingColorPalette {
    private Random colorRandom = null;
    private Set<Integer> usedColors = new HashSet<>();

    @Override
    protected Color _getColor(int index) {
        if (index <= 0) {
            return Color.BLACK;

        }
        synchronized (usedColors) {
            if (colorRandom == null) {
                colorRandom = new Random();
            }
            int rgb = 0;
            for (int i = 0; i < 100; i++) {
                rgb = colorRandom.nextInt();
                if (!usedColors.contains(rgb)) {
                    break;
                }
            }
            usedColors.add(rgb);
            return new Color(rgb);
        }
    }

    @Override
    public void reset() {
        super.reset();
        usedColors.clear();
    }
}