package gsmith.math;

/** Use MuellersMethod for finding roots of an equation.
 */
public class MuellersMethod implements ComplexRootFinder {
    @Override
    public int find(Complex x0, Complex x1, Complex x2, double tolerance,
            int maxIterations, ComplexEquation eq, Complex[] rootHolder) {
        // x1 - x0
        Complex h1 = x1.sub(x0);
        // x2 - x1
        Complex h2 = x2.sub(x1);
        // (f(x1) - f(x0)) / h1
        Complex delta1 = eq.f(x1).sub(eq.f(x0)).div(h1);
        // (f(x2) - f(x1)) / h2
        Complex delta2 = eq.f(x2).sub(eq.f(x1)).div(h2);
        // (delta2 - delta1) / (h2 + h1)
        Complex d = delta2.sub(delta1).div(h2.add(h1));

        Complex D, E, h, p, b;
        for (int i = 2; i <= maxIterations; i++) {
            // b = delta2 + (h2 * d)
            b = delta2.add(h2.mul(d));

            // D = (b^2 - 4*f(x2)*d) ^ .5
            D = b.pow(2.0).sub(eq.f(x2).mul(d).mul(4)).pow(0.5);

            if (b.sub(D).abs() < b.add(D).abs()) {
                E = b.add(D);
            }
            else {
                E = b.sub(D);
            }
            if (E.equals(0.0)) {
                rootHolder[0] = null;
                return -1; // division by 0
            }

            // h = (-2 * f(x2)) / E
            h = eq.f(x2).mul(-2.0).div(E);
            // p = x2 + h
            p = x2.add(h);

            // found it
            if (h.abs() < tolerance) {
                rootHolder[0] = p;
                return i;
            }

            x0 = x1;
            x1 = x2;
            x2 = p;
            h1 = x1.sub(x0);
            h2 = x2.sub(x1);
            if (h1.equals(0.0) || h2.equals(0.0)) {
                rootHolder[0] = null;
                return -1; // division by 0
            }

            // (f(x1) - f(x0)) / h1
            delta1 = eq.f(x1).sub(eq.f(x0)).div(h1);
            // (f(x2) - f(x1)) / h2
            delta2 = eq.f(x2).sub(eq.f(x1)).div(h2);
            // (delta2 - delta1) / (h2 + h1)
            d = delta2.sub(delta1).div(h2.add(h1));
        }

        // we didn't find it under the max # of iterations
        rootHolder[0] = null;
        return 0; // failed
    }
}