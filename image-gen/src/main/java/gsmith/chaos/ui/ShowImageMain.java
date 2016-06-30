package gsmith.chaos.ui;

import static javax.swing.SwingUtilities.invokeLater;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import gsmith.chaos.DrawMapContext;

/** Main class for showing a complex-number, root-finding image in a window.
 */
public class ShowImageMain {
    /** A JFileChooser filter for an ImageIO writer type.
     */
    private static class ImageTypeFileFilter extends FileFilter {
        private final String description;
        private final String imageIOType;
        private final String[] extensions;

        private ImageTypeFileFilter(String description, String imageIOType, String... extensions) {
            this.description = description;
            this.imageIOType = imageIOType;
            this.extensions = extensions;
        }

        @Override
        public boolean accept(File f) {
            if (f != null) {
                if (f.isDirectory()) {
                    return true;
                }
                int i = f.getName().lastIndexOf('.');
                if (i > 0 && i < f.getName().length() - 1) {
                    String fileExt = f.getName().substring(i + 1);
                    return Arrays.stream(extensions).anyMatch(fileExt::equalsIgnoreCase);
                }
            }
            return false;
        }

        @Override
        public String getDescription() {
            return description;
        }

        public String[] getExtensions() {
            return extensions;
        }

        public String getImageIOType() {
            return imageIOType;
        }
    }

    /** The available image type file filters.
     */
    private static final ImageTypeFileFilter[] FILE_TYPE_FILTERS;
    static {
        // create our list of Save image types from what the JRE says it can write out
        final Set<String> availableIOTypes = new HashSet<>(Arrays.asList(ImageIO.getWriterFormatNames()));
        List<ImageTypeFileFilter> l = new LinkedList<>();
        // png is supposed to be in every JRE
        if (availableIOTypes.contains("png")) {
            l.add(new ImageTypeFileFilter("Portable Network Graphics (*.png)", "png", "png"));
        }
        if (availableIOTypes.contains("jpg")) {
            l.add(new ImageTypeFileFilter("Joint Photographic Experts Group (*.jpg)", "jpg", "jpg", ".jpeg"));
        }
        if (availableIOTypes.contains("gif")) {
            l.add(new ImageTypeFileFilter("Graphics Interchange Format (*.gif)", "gif", "gif"));
        }
        if (availableIOTypes.contains("bmp")) {
            l.add(new ImageTypeFileFilter("Bitmap (*.bmp)", "bmp", "bmp"));
        }
        if (availableIOTypes.contains("wbmp")) {
            l.add(new ImageTypeFileFilter("Windows Bitmap (*.wbmp)", "wbmp", "wbmp"));
        }
        // this isn't normally there, but could be
        if (availableIOTypes.contains("tiff")) {
            l.add(new ImageTypeFileFilter("Tagged Image File Format (*.tiff)", "tiff", "tiff"));
        }
        FILE_TYPE_FILTERS = l.toArray(new ImageTypeFileFilter[l.size()]);
    }
    private static Pair<File, ImageTypeFileFilter> lastSave = null;

    public static void main(String... args) throws Exception {
        if (args.length < 1) {
            usage(1);
        }

        Properties properties = new Properties();
        try (InputStream in = new FileInputStream(args[0])) {
            properties.load(in);
        }
        DrawMapContext ctx = DrawMapContext.create(properties);

        final JFrame window = new JFrame("Math: " + ctx.getEquation());
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setSize(Math.min(ctx.getWidth(), 800), Math.min(ctx.getHeight(), 800));
        window.setResizable(true);

        // this will paint the image
        ImageComponent<BufferedImage> panel = new ImageComponent<>();
        panel.setDoubleBuffered(false);
        window.getContentPane().add(panel, BorderLayout.CENTER);

        // hook up Cmd-W to close the window
        panel.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.META_MASK), "closeWindow");
        panel.getActionMap().put("closeWindow", new AbstractAction("Close Window") {
            @Override
            public void actionPerformed(ActionEvent e) {
                window.setVisible(false);
                window.dispose();
                System.exit(0);
            }
        });

        // hook up Cmd-S to save to file
        panel.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_S, KeyEvent.META_MASK), "save");
        panel.getActionMap().put("save", new AbstractAction("Save...") {
            @Override
            public void actionPerformed(ActionEvent e) {
                promptForSave(window, Optional.ofNullable(lastSave)).ifPresent(p -> {
                    lastSave = p;
                    //System.out.println("Saving " + p.right.getImageIOType() + " image to " + p.left);
                    try {
                        ImageIO.write(panel.getImage(), p.right.getImageIOType(), p.left);
                    }
                    catch (IOException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(window, "Unable to write file: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                    }
                });
            }
        });

        // show the window
        window.setVisible(true);

        // start the math
        calcuateAndShowImage(ctx, panel);
    }

    private static void usage(Integer exit) {
        System.err.println("Usage: java " + ShowImageMain.class.getName() + " .properties");
        if (exit != null) {
            System.exit(exit);
        }
    }

    private static void runAndDispose(Graphics g, Consumer<Graphics> c) {
        try {
            c.accept(g);
        }
        finally {
            g.dispose();
        }
    }

    private static void calcuateAndShowImage(final DrawMapContext ctx, Imagable<BufferedImage> component) {
        // create an image buffer that this will paint to
        BufferedImage im = new BufferedImage(ctx.getWidth(), ctx.getHeight(), BufferedImage.TYPE_INT_RGB);
        // initialize it to all black
        runAndDispose(im.createGraphics(), g -> {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, im.getWidth(), im.getHeight());
        });

        final long hashStep = ((long)ctx.getWidth() * (long)ctx.getHeight()) / 100L;
        final AtomicLong counter = new AtomicLong(0L);
        final AtomicInteger highestIteration = new AtomicInteger(0);

        System.out.println("y=" + ctx.getEquation().toString());
        System.out.println("x=" + ctx.getStartXCoord() + " to " + ctx.getEndXCoord() + " by " +
                ctx.getStepX());
        System.out.println("y=" + ctx.getStartYCoord() + " to " + ctx.getEndYCoord() + " by " +
                ctx.getStepY());
        // go
        long startTime = System.nanoTime();
        try {
            System.out.println("Starting...");
            ctx.getImageRunner().run(ctx, (x, y, i, j, numIterations) -> {
                Color c = ctx.getColorForIteration(numIterations);
                // update the image buffer
                runAndDispose(im.createGraphics(), g -> {
                    g.setColor(c);
                    g.fillRect(i, j, 1, 1);
                });
                // have the component repaint the image
                invokeLater(() -> component.setImage(im));
                // progress
                long stepNum = counter.incrementAndGet();
                if (stepNum % hashStep == 0) {
                    System.out.print('.');
                }
                // update this
                highestIteration.getAndUpdate(v -> Math.max(v, numIterations));
            }).get();
        }
        catch (InterruptedException ignore) {
        }
        catch (ExecutionException ex) {
            ex.getCause().printStackTrace();
        }
        long endTime = System.nanoTime();
        System.out.println();
        System.out.println("Time=" + (endTime - startTime) + "ns.");
        System.out.println("Highest # of iterations=" + highestIteration.get());
    }

    /** Prompt the user for a filename and image type.
     * @return the selected file and image type, or none
     */
    private static Optional<Pair<File, ImageTypeFileFilter>> promptForSave(JFrame window, Optional<Pair<File, ImageTypeFileFilter>> lastSave) {
        // configure a file chooser
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setMultiSelectionEnabled(false);
        chooser.setDialogTitle("Save As...");
        // configure the image types available
        chooser.setAcceptAllFileFilterUsed(false);
        Arrays.stream(FILE_TYPE_FILTERS).forEachOrdered(chooser::addChoosableFileFilter);
        // configure from a previous save
        lastSave.ifPresent(p -> {
            chooser.setSelectedFile(p.left);
            chooser.setFileFilter(p.right);
        });

        if (chooser.showSaveDialog(window) == JFileChooser.APPROVE_OPTION) {
            ImageTypeFileFilter filter = (ImageTypeFileFilter)chooser.getFileFilter();
            File file = chooser.getSelectedFile();
            // if it doesn't have an extension, use the filter's default extension
            if (file.getName().lastIndexOf('.') < 0) {
                file = new File(file.getParentFile(), file.getName() + "." + filter.getExtensions()[0]);
            }
            return Optional.of(new Pair<>(file, filter));
        }
        return Optional.empty();
    }

    private static class Pair<L, R> {
        public final L left;
        public final R right;

        public Pair(L left, R right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((left == null) ? 0 : left.hashCode());
            result = prime * result + ((right == null) ? 0 : right.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Pair<?, ?> other = (Pair<?, ?>)obj;
            if (left == null) {
                if (other.left != null) {
                    return false;
                }
            }
            else if (!left.equals(other.left)) {
                return false;
            }
            if (right == null) {
                if (other.right != null) {
                    return false;
                }
            }
            else if (!right.equals(other.right)) {
                return false;
            }
            return true;
        }
    }
}