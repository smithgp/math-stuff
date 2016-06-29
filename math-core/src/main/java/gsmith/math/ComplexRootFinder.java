package gsmith.math;

@FunctionalInterface
public interface ComplexRootFinder
{
    /** Find the nearest root of the equation based on the estimated roots.
     * @param x0 the first approximate root.
     * @param x1 the second approximate root.
     * @param x2 the third approximate root.
     * @param tolerance the tolerance for when a root estimation is close enough.
     * @param maxIterations the maximum number of iterations.
     * @param eq the equation
     * @param rootHolder a Complex[1] to hold the discovered root.
     * @return the number of iterations required to calculate the root, less
     *         than 0 for cannot be found, 0 for exceeded maxIterations.
     */
    int find(Complex x0, Complex x1, Complex x2,
             double tolerance, int maxIterations, ComplexEquation eq,
             Complex[] rootHolder);
}
