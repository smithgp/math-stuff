package gsmith.math;

/** Use Newton's method for finding roots of an equation.
 */
public class NewtonsMethod implements ComplexRootFinder {
    @Override
    public int find(Complex x0, Complex x1, Complex x2, double tolerance,
            int maxIterations, ComplexEquation eq, Complex[] rootHolder) {
        return find(x0, tolerance, maxIterations, eq, rootHolder);
    }

    public int find(Complex p0, double tolerance, int maxIterations,
            ComplexEquation eq, Complex[] rootHolder) {
        Complex p;
        Complex f;
        Complex[] fPrime = new Complex[1];
        for (int i = 1; i <= maxIterations; i++) {
            f = eq.f(p0, fPrime);
            // failed -- this would cause division by 0
            if (fPrime[0] == null || fPrime[0].equals(0.0)) {
                rootHolder[0] = null;
                return -1;
            }
            p = p0.sub(f.div(fPrime[0]));
            if (p.sub(p0).abs() < tolerance) {
                rootHolder[0] = p;
                return i;
            }
            p0 = p;
        }
        // this means we didn't find it under the max # of iterations
        rootHolder[0] = null;
        return 0;
    }
}