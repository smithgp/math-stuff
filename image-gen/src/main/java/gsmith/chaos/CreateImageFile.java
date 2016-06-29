package gsmith.chaos;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

import javax.imageio.ImageIO;

/** Main class for writing a complex-number, root-finding image.
 */
public class CreateImageFile {
    public static void main(String... args) throws Exception {
        if (args.length < 2) {
            usage(1);
        }

        Properties p = new Properties();
        try (InputStream in = new FileInputStream(args[0])) {
            p.load(in);
        }
        DrawMapContext ctx = DrawMapContext.create(p);

        File out = new File(args[1]);
        System.out.println("Writing " + ctx.getEquation());
        System.out.println("to " + out.getAbsolutePath());
        writeImage(out, ctx);
    }

    private static void usage(Integer exit) {
        System.err.println("Usage: java " + CreateImageFile.class.getName() + " .properties out.png");
        if (exit != null) {
            System.exit(exit);
        }
    }

    private static void writeImage(File out, final DrawMapContext ctx) throws IOException, InterruptedException, ExecutionException {
        BufferedImage im = new BufferedImage(ctx.getWidth(), ctx.getHeight(), BufferedImage.TYPE_INT_RGB);
        final Graphics2D g = im.createGraphics();
        g.setColor(Color.BLACK);
        g.fillRect(0, 0, im.getWidth(), im.getHeight());
        try {
            final long hashStep = ((long)ctx.getWidth() * (long)ctx.getHeight()) / 100L;
            final AtomicLong counter = new AtomicLong(0L);
            System.out.println("#!#! x=" + ctx.getStartXCoord() + " to " + ctx.getEndXCoord() + " by " +
                    ctx.getStepX());
            System.out.println("#!#! y=" + ctx.getStartYCoord() + " to " + ctx.getEndYCoord() + " by " +
                    ctx.getStepY());

            ctx.getImageRunner().run(ctx, (x, y, i, j, numIterations) -> {
                Color c = ctx.getColorForIteration(numIterations);
                g.setColor(c);
                g.fillRect(i, j, 1, 1);
                if (counter.incrementAndGet() % hashStep == 0) {
                    System.out.print('.');
                }
            }).get();
        }
        finally {
            g.dispose();
            System.out.println();
        }
        System.out.println("Done");
        ImageIO.write(im, "PNG", out);
    }
}