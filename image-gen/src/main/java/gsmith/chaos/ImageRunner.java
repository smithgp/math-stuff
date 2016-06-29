package gsmith.chaos;

import gsmith.math.Complex;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;

@FunctionalInterface
public interface ImageRunner {
    @FunctionalInterface
    public static interface Callback {
        /** Callback from finding roots of any equation.
         * @param x the real x coordinate
         * @param y the real y coordinate
         * @param i the integer x position of the real coordinate system
         * @param y the integer x position of the real coordinate system
         * @param numIterations the number of iterations needed to find the root of the equation at (x, y);
         *        0 or less not found.
         */
        public void callback(double x, double y, int i, int j, int numIterations);

        /** Tell that the running process should cancel.
         */
        public default boolean isCancelled() {
            return false;
        }
    }

    /** Run a drawing context against the equation and root finder.
     * @param ctx the context.
     * @param callback the callback to invoke for each image grid point; not guaranteed to be called in any particular
     *        order.
     * @return a future of when the run will be finished
     */
    CompletableFuture<Void> run(DrawMapContext ctx, Callback callback);

    /** Default implementation.
     * This does it in the current thread.
     */
    // Note: threading this out by rows or columns appears to be generally slower than doing it directly in a
    // double-loop, probably just due to the overhead of thread creation and future combining. It's pretty fast on
    // my i7 laptop (approx. 1-1.5s) so I'm not too worried about it for now.
    public final class Default implements ImageRunner {
        @Override
        public CompletableFuture<Void> run(DrawMapContext ctx, Callback callback) {
            Complex EPSILON = new Complex(0.1, 0.0);
            Complex EPSILON2 = new Complex(0.2, 0.0);
            double x = ctx.getStartXCoord();
            try {
                // for now we're doing this in the current thread, but we might run this against a thread pool at some point
                for (int i = 0; i < ctx.getWidth(); x += ctx.getStepX(), i++) {
                    double y = ctx.getStartYCoord();
                    for (int j = 0; j < ctx.getHeight(); y += ctx.getStepY(), j++) {
                        if (callback.isCancelled()) {
                            throw new CancellationException();
                        }
                        Complex approx = new Complex(x, y);
                        Complex[] rootHolder = new Complex[] {
                            null
                        };
                        int numIter = ctx.getRootFinder().find(approx,
                                approx.sub(EPSILON), approx.sub(EPSILON2),
                                ctx.getTolerance(), ctx.getMaxIterations(),
                                ctx.getEquation(), rootHolder);
                        callback.callback(x, y, i, j, numIter);
                    }
                    if (callback.isCancelled()) {
                        throw new CancellationException();
                    }
                }
            }
            catch (CancellationException ex) {
                CompletableFuture<Void> f = new CompletableFuture<>();
                f.completeExceptionally(ex);
                return f;
            }
            return CompletableFuture.completedFuture(null);
        }
    }
}