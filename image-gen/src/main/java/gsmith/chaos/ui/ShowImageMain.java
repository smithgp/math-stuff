package gsmith.chaos.ui;

import static javax.swing.SwingUtilities.invokeLater;
import gsmith.chaos.DrawMapContext;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.KeyStroke;

/** Main class for showing a complex-number, root-finding image in a window.
 */
public class ShowImageMain {
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
        ImageComponent panel = new ImageComponent();
        panel.setDoubleBuffered(false);
        window.getContentPane().add(panel, BorderLayout.CENTER);

        // hook up Cmd-W to close the window
        KeyStroke closeKey = KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.META_MASK);
        panel.getInputMap().put(closeKey, "closeWindow");
        panel.getActionMap().put("closeWindow", new AbstractAction("Close Window") {
            @Override
            public void actionPerformed(ActionEvent e) {
                window.setVisible(false);
                window.dispose();
                System.exit(0);
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

    private static void calcuateAndShowImage(final DrawMapContext ctx, Imagable component) {
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
                //System.out.println("(" + i + "," + j + ") thread=" + Thread.currentThread().getName());
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
}