package gsmith.chaos;

import gsmith.chaos.color.ColorPalette;
import gsmith.chaos.color.DefaultColorPalette;
import gsmith.chaos.color.GradientColorPalette;
import gsmith.chaos.color.MultiGradientColorPalette;
import gsmith.chaos.color.RandomColorPalette;
import gsmith.math.Complex;
import gsmith.math.ComplexEquation;
import gsmith.math.ComplexPolynomial;
import gsmith.math.ComplexRootFinder;
import gsmith.math.MuellersMethod;
import gsmith.math.NewtonsMethod;

import java.awt.Color;
import java.util.Properties;

public class DrawMapContext {
    public static final double DEFAULT_ZOOM = 10.0;
    public static final double DEFAULT_TOLERANCE = 0.001;
    public static final int DEFAULT_MAX_ITERATIONS = 50;
    public static final int DEFAULT_HEIGHT = 400;
    public static final int DEFAULT_WIDTH = 400;
    public static final double DEFAULT_START_COORD = -2.0;
    public static final double DEFAULT_END_COORD = 2.0;

    private ComplexEquation equation;
    private ComplexRootFinder rootFinder;

    private ColorPalette palette;
    private ImageRunner imageRunner;

    private double zoom = DEFAULT_ZOOM;
    private double tolerance = DEFAULT_TOLERANCE;
    private int maxIterations = DEFAULT_MAX_ITERATIONS;

    private int height = DEFAULT_HEIGHT;
    private int width = DEFAULT_WIDTH;

    private double startX = DEFAULT_START_COORD;
    private double endX = DEFAULT_END_COORD;
    private double startY = DEFAULT_START_COORD;
    private double endY = DEFAULT_END_COORD;

    // computed values
    private Double stepX = null;
    private Double stepY = null;

    public DrawMapContext(ComplexEquation equation, ComplexRootFinder rootFinder) {
        this.equation = equation;
        this.rootFinder = rootFinder;
    }

    @Override
    public DrawMapContext clone() {
        try {
            DrawMapContext ctx = (DrawMapContext)super.clone();
            // clear these out
            ctx.stepX = null;
            ctx.stepY = null;
            return ctx;
        }
        catch (CloneNotSupportedException ex) {
            throw new InternalError(ex);
        }
    }

    /** Get the equation to draw.
     */
    public ComplexEquation getEquation() {
        return this.equation;
    }

    /** Set the equation to draw.
     */
    public void setEquation(ComplexEquation equation) {
        this.equation = equation;
    }

    /** Get the root finder to use.
     */
    public ComplexRootFinder getRootFinder() {
        return this.rootFinder;
    }

    /** Set the root finder to use.
     */
    public void setRootFinder(ComplexRootFinder rootFinder) {
        this.rootFinder = rootFinder;
    }

    public ColorPalette getColorPalette() {
        return this.palette;
    }

    public void setColorPalette(ColorPalette palette) {
        this.palette = palette;
    }

    public ImageRunner getImageRunner() {
        return imageRunner;
    }

    public void setImageRunner(ImageRunner imageRunner) {
        this.imageRunner = imageRunner;
    }

    /** Get the zoom factor.
     */
    public double getZoom() {
        return this.zoom;
    }

    /** Set the zoom factor (greater than 0).
     */
    public void setZoom(double zoom) {
        if (zoom <= 0.0) {
            throw new IllegalArgumentException("illegal zoom less than 0");
        }
        this.zoom = zoom;
    }

    /** Get the tolerance for 'closeness' to a root.
     */
    public double getTolerance() {
        return this.tolerance;
    }

    /** Set the tolerance for 'closeness' to a root (greater than 0).
     */
    public void setTolerance(double tolerance) {
        if (tolerance <= 0.0) {
            throw new IllegalArgumentException("illegal tolerance less than 0");
        }
        this.tolerance = tolerance;
    }

    /** Get the maximum number of iterations to try.
     */
    public int getMaxIterations() {
        return this.maxIterations;
    }

    /** Set the maximum number of iterations to try (greater than 0).
     */
    public void setMaxIterations(int maxIterations) {
        if (maxIterations <= 0.0) {
            throw new IllegalArgumentException("illegal maxIterations less than 0");
        }
        this.maxIterations = maxIterations;
    }

    /** Get the picture height.
     */
    public int getHeight() {
        return this.height;
    }

    /** Set the picture height (greater than 0).
     */
    public void setHeight(int height) {
        if (height < 0) {
            throw new IllegalArgumentException("illegal height less than 0");
        }
        this.height = height;
        this.stepY = null;
    }

    /** Get the picture width.
     */
    public int getWidth() {
        return this.width;
    }

    /** Set the picture width (greater than 0).
     */
    public void setWidth(int width) {
        if (width < 0) {
            throw new IllegalArgumentException("illegal width less than 0");
        }
        this.width = width;
        this.stepX = null;
    }

    /** Get the starting x coordinate (left-hand-side).
     */
    public double getStartXCoord() {
        return this.startX;
    }

    /** Set the starting x coordinate (left-hand-side).
     */
    public void setStartXCoord(double startX) {
        this.startX = startX;
        this.stepX = null;
    }

    /** Get the ending x coordinate (right-hand-side).
     */
    public double getEndXCoord() {
        return this.endX;
    }

    /** Set the ending x coordinate (right-hand-side).
     */
    public void setEndXCoord(double endX) {
        this.endX = endX;
        this.stepX = null;
    }

    /** Get the starting y coordinate (top).
     */
    public double getStartYCoord() {
        return this.startY;
    }

    /** Set the starting y coordinate (top).
     */
    public void setStartYCoord(double startY) {
        this.startY = startY;
        this.stepY = null;
    }

    /** Get the ending y coordinate (bottom).
     */
    public double getEndYCoord() {
        return this.endY;
    }

    /** Set the ending y coordinate (bottom).
     */
    public void setEndYCoord(double endY) {
        this.endY = endY;
        this.stepY = null;
    }

    /** Get the x-coordinate-system width for each pixel width.
     */
    public double getStepX() {
        if (stepX == null) {
            stepX = (getEndXCoord() - getStartXCoord()) / getWidth();
        }
        return stepX;
    }

    /** Get the y-coordinate-system width for each pixel width.
     */
    public double getStepY() {
        if (stepY == null) {
            stepY = (getEndYCoord() - getStartYCoord()) / getHeight();
        }
        return stepY;
    }

    /** Get the color to use for a root-finding process that took the specified
     * number of iterations.
     */
    public synchronized Color getColorForIteration(int iteration) {
        return palette.getColor(iteration);
    }

    /** Parse a properties object to a drawing context.
     *
     * @param p the properties.
     * @return the context.
     * @throws IllegalArgumentException thrown if the properties are invalid.
     */
    public static DrawMapContext create(Properties p) throws IllegalArgumentException {
        int order = getInteger(p, "order", true, 1);

        // read the equation coefficients
        Complex[] a = new Complex[order + 1];
        for (int i = 0; i < a.length; i++) {
            Double re = getDouble(p, "coeff." + i + ".real", false, null);
            Double im = getDouble(p, "coeff." + i + ".imag", false, null);

            a[i] = new Complex(re != null ? re : 0.0, im != null ? im : 0.0);
        }

        String str = p.getProperty("rootFinder");
        ComplexRootFinder rootFinder;
        if ("newton".equalsIgnoreCase(str)) {
            rootFinder = new NewtonsMethod();
        }
        else if ("mueller".equalsIgnoreCase(str)) {
            rootFinder = new MuellersMethod();
        }
        else if (str != null && str.length() > 0) {
            try {
                rootFinder = Class.forName(str).asSubclass(ComplexRootFinder.class).newInstance();
            }
            catch (InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
                throw new IllegalArgumentException("unable to instantiate rootFinder '" + str + "'", ex);
            }
        }
        else {
            throw new IllegalArgumentException("missing 'rootFinder'");
        }

        DrawMapContext ctx = new DrawMapContext(new ComplexPolynomial(a), rootFinder);

        // read the other properties
        Double d = getDouble(p, "zoom", false, 0.0);
        if (d != null) {
            ctx.setZoom(d);
        }

        d = getDouble(p, "tolerance", false, 0.0);
        if (d != null) {
            ctx.setTolerance(d);
        }

        str = p.getProperty("maxIterations");
        if (str != null) {
            try {
                int i = Integer.parseInt(str.trim());
                if (i <= 0L) {
                    throw new IllegalArgumentException("invalid 'maxIterations' value " + i +
                            ", must be greater than 0");
                }
                ctx.setMaxIterations(i);
            }
            catch (NumberFormatException ex) {
                throw new IllegalArgumentException("invalid 'maxIterations'", ex);
            }
        }

        Integer i = getInteger(p, "height", false, 0);
        if (i != null) {
            ctx.setHeight(i);
        }

        i = getInteger(p, "width", false, 0);
        if (i != null) {
            ctx.setWidth(i);
        }

        d = getDouble(p, "start_x", false, null);
        if (d != null) {
            ctx.setStartXCoord(d);
        }
        d = getDouble(p, "end_x", false, null);
        if (d != null) {
            ctx.setEndXCoord(d);
        }

        d = getDouble(p, "start_y", false, null);
        if (d != null) {
            ctx.setStartYCoord(d);
        }
        d = getDouble(p, "end_y", false, null);
        if (d != null) {
            ctx.setEndYCoord(d);
        }

        // load the color palette
        ctx.setColorPalette(createColorPalette(p, ctx.getMaxIterations()));

        str = p.getProperty("runner");
        if (str == null || str.length() <= 0 || "default".equals(str)) {
            ctx.setImageRunner(new ImageRunner.Default());
        }
        else {
            try {
                ctx.setImageRunner(Class.forName(str).asSubclass(ImageRunner.class).newInstance());
            }
            catch (InstantiationException | IllegalAccessException | ClassNotFoundException ex) {
                throw new IllegalArgumentException("unable to instantiate image runner '" + str + "'", ex);
            }
        }

        return ctx;
    }

    private static ColorPalette createColorPalette(Properties baseProps, int maxIterations) {
        String str = baseProps.getProperty("palette");
        ColorPalette palette = null;
        if (str == null || str.length() <= 0 || "default".equals(str)) {
            palette = new DefaultColorPalette();
        }
        else if ("random".equals(str)) {
            palette = new RandomColorPalette();
        }
        else if ("gradient".equals(str)) {
            palette = new GradientColorPalette();
        }
        else if ("multi-gradient".equals(str)) {
            palette = new MultiGradientColorPalette();
        }
        else if (str.length() > 0) {
            try {
                Class<?> cl = Class.forName(str);
                palette = cl.asSubclass(ColorPalette.class).newInstance();
            }
            catch (Exception ex) {
                throw new IllegalArgumentException("unable to instantiate palette '" + str + "'", ex);
            }
        }

        // pull out all properties starting with "palette."
        Properties paletteProps = new Properties();
        baseProps.forEach((key, value) -> {
            String name = key.toString();
            if (name.startsWith("palette.")) {
                paletteProps.setProperty(name.substring("palette.".length()), value.toString());
            }
        });
        palette.init(paletteProps, maxIterations);
        return palette;
    }

    private static Double getDouble(Properties p, String name, boolean required, Double min) {
        String str = p.getProperty(name);
        if (str != null) {
            try {
                Double d = Double.valueOf(str.trim());
                if (min != null && d <= min) {
                    throw new IllegalArgumentException("invalid '" + name + "' value " + d + ", must be greater than " +
                            min);
                }
                return d;
            }
            catch (NumberFormatException ex) {
                throw new IllegalArgumentException("invalid '" + name + "'", ex);
            }
        }
        else if (required) {
            throw new IllegalArgumentException("missing '" + name + "'");
        }

        // !required
        return null;
    }

    private static Integer getInteger(Properties p, String name, boolean required, Integer min) {
        String str = p.getProperty(name);
        if (str != null) {
            try {
                Integer i = Integer.valueOf(str.trim());
                if (min != null && i <= min) {
                    throw new IllegalArgumentException("invalid '" + name + "' value " + i + ", must be greater than " +
                            min);
                }
                return i;
            }
            catch (NumberFormatException ex) {
                throw new IllegalArgumentException("invalid '" + name + "'", ex);
            }
        }
        else if (required) {
            throw new IllegalArgumentException("missing '" + name + "'");
        }

        // !required
        return null;
    }
}