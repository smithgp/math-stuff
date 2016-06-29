package gsmith.chaos.color;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

/** A color palette that caches the computed colors.
 */
public abstract class CachingColorPalette implements ColorPalette {
    private Map<Integer, Color> cache = new HashMap<>();

    @Override
    public Color getColor(int index) {
        synchronized (cache) {
            Color c = cache.get(index);
            if (c == null) {
                c = _getColor(index);
                cache.put(index, c);
            }
            return c;
        }
    }

    /** The delegate method.
     */
    protected abstract Color _getColor(int index);

    @Override
    public void reset() {
        synchronized (cache) {
            cache.clear();
        }
    }
}
