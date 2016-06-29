package gsmith.math;

import java.util.Arrays;
import java.util.Collection;

/** A complex-number polynomial equation.
 */
public final class ComplexPolynomial implements ComplexEquation, Cloneable {
    /**
     * The coefficients.
     */
    private final Complex[] a;

    // cached hashCode() and toString()
    private Integer hashCode = null;
    private String toString = null;

    /** Constructor from the coefficients.
     * Coefficients can be null to represent 0.
     */
    public ComplexPolynomial(double... a) {
        this.a = new Complex[a.length];
        for (int i = 0; i < a.length; i++) {
            this.a[i] = new Complex(a[i], 0.0);
        }
    }

    /** Constructor from the coefficients.
     * Coefficients can be null to represent 0.
     */
    public ComplexPolynomial(Complex... a) {
        this.a = new Complex[a.length];
        System.arraycopy(a, 0, this.a, 0, a.length);
    }

    /** Copy constructor.
     */
    public ComplexPolynomial(ComplexPolynomial eq) {
        this(eq.a);
    }

    /** Get the order of this polynomial. This is the highest power of x in the
     * equation. There will this + 1 coefficients.
     */
    public int getOrder() {
        return this.a.length - 1;
    }

    /** Return the ith coefficient of the equation.
     */
    public Complex a(int i) {
        return this.a[i];
    }

    /** Get the coefficients.
     */
    public Complex[] a() {
        Complex[] a = new Complex[this.a.length];
        System.arraycopy(this.a, 0, a, 0, this.a.length);
        return a;
    }

    /** Compute f(x) and f'(x). This will use Horner's method.
     */
    @Override
    public Complex f(Complex x, Complex[] deriv) {
        int n = getOrder();
        // computing f'(x) also
        if (deriv != null && deriv.length >= 1) {
            Complex y = a(n);
            Complex z = a(n);
            for (int j = n - 1; j >= 1; j--) {
                // y = x * y + a[j]
                y = x.mul(y).add(a(j));
                // z = x * z + y
                z = x.mul(z).add(y);
            }
            deriv[0] = z;
            // y = x * y + a[0]
            return x.mul(y).add(a(0));
        }
        // not computing f'(x), this is a bit faster
        else {
            Complex y = a(n);
            for (int j = n - 1; j >= 1; j--) {
                // y = x * y + a[j]
                y = x.mul(y).add(a(j));
            }
            // x * y + a[0]
            return x.mul(y).add(a(0));
        }
    }

    /** Find the roots of this equation.
    *
    * @param x0 the first approximate root.
    * @param x1 the second approximate root.
    * @param x2 the third approximate root.
    * @param tolerance the tolerance for when a root estimation is close
    *            enough.
    * @param maxIteration the maximum number of iterations.
    * @param roots the array to hold the computed roots
    * @param finder the root finder to use.
    * @return the number of roots actually found, 0 or less for error of
    *         invalid equation.
    */
    public int deflate(Complex x0, Complex x1, Complex x2, double tolerance,
            int maxIterations, Collection<Complex> roots,
            ComplexRootFinder finder) {
        // start root finding

        // second order -- use quadratic formula
        int deg = getOrder();
        if (deg == 2) {
            if (!a[2].equals(0.0)) {
                Complex[] holder = new Complex[] { null, null };
                quadraticFormula(a[2], a[1], a[0], holder);
                roots.add(holder[0]);
                roots.add(holder[1]);
                return 2;
            }
            // it's actually a first order
            else {
                deg--;
            }
        }

        // first order
        if (deg == 1) {
            // not an equation
            if (a[1].equals(0.0)) {
                return 0;
            }

            roots.add(a[0].mul(-1.0).div(a[1]));
            return 1;
        }

        // if we're here, it's at least 3rd order
        Complex[] rootHolder = new Complex[1];
        // start off with this equation; after each iteration, we will evaluate
        // Q(x) for the found root, and assign that to eq for the next
        // iteration.
        ComplexPolynomial eq = this;
        for (int j = deg - 1; j >= 2; j--) {
            rootHolder[0] = null;
            if (finder.find(x0, x1, x2, tolerance, maxIterations, eq, rootHolder) <= 0) {
                // Instead of failing, this could change the approximate
                // roots and try again (sometimes mueller's fails on some roots)
                return 0;
            }
            roots.add(rootHolder[0]);
            eq = getQ(eq, rootHolder[0]);
        }
        rootHolder = new Complex[] { null, null };
        quadraticFormula(eq.a[2], eq.a[1], eq.a[0], rootHolder);
        roots.add(rootHolder[1]);
        roots.add(rootHolder[0]);
        return deg - 1;
    }

    /** Find Q(x) for the polynomial represented by the coefficients, for the
     * specified root. This will return the coefficients of a polynomial of 1
     * smaller degree.
     */
    private static Complex[] getQ(Complex[] b, Complex root) {
        int n = b.length - 1;
        Complex y = b[n];
        Complex[] a = new Complex[n];
        for (int j = n - 1; j >= 1; j--) {
            y = root.mul(y).add(b[j]);
            a[j - 1] = y;
        }
        return a;
    }

    private static ComplexPolynomial getQ(ComplexPolynomial b, Complex root) {
        return new ComplexPolynomial(getQ(b.a, root));
    }

    /** Use the quadratic formula to find the roots of the 2nd order equation
     * specified by the coefficients.
     *
     * @param a2 the x^2 coefficient.
     * @param a1 the x coefficient.
     * @param a0 the x^0 coefficient
     * @param roots the Complex[2] to store the roots.
     */
    private static void quadraticFormula(Complex a2, Complex a1, Complex a0, Complex[] roots) {
        Complex tmp = a1.pow(2.0);
        tmp = tmp.sub(a2.mul(a0).mul(4.0));
        tmp = tmp.pow(0.5);
        Complex bottom = a2.mul(2.0);
        roots[0] = a1.mul(-1.0).add(tmp).div(bottom);
        roots[1] = a1.mul(-1.0).sub(tmp).div(bottom);
    }

    @Override
    public ComplexPolynomial clone() {
        return new ComplexPolynomial(this.a);
    }

    @Override
    public int hashCode() {
        if (hashCode == null) {
            hashCode = Arrays.hashCode(this.a);
        }
        return hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ComplexPolynomial) {
            return Arrays.equals(this.a, ((ComplexPolynomial)o).a);
        }
        return false;
    }

    @Override
    public String toString() {
        if (toString == null) {
            StringBuilder buf = new StringBuilder();
            for (int i = a.length - 1; i >= 0; i--) {
                if (a[i] != null && !a[i].equals(0.0)) {
                    Complex coeff = a[i];
                    // other coefficients already written
                    if (buf.length() > 0) {
                        // check if actually a negative real number
                        if (coeff.isRealNumber() && coeff.re < 0.0) {
                            coeff = new Complex(-coeff.re);
                            buf.append(" - ");
                        }
                        else {
                            buf.append(" + ");
                        }
                    }
                    // first coefficient written, check if actually a negative real number
                    else if (coeff.isRealNumber() && coeff.re < 0.0) {
                        coeff = new Complex(-coeff.re);
                        buf.append("-");
                    }
                    // always write the constant
                    if (i == 0) {
                        buf.append(coeff);
                    }
                    else {
                        // write the coefficient if not exactly 1.0
                        if (!coeff.equals(1.0)) {
                            buf.append('(').append(coeff).append(')');
                        }
                        // write the power
                        buf.append('x');
                        if (i != 1) {
                            buf.append('^').append(i);
                        }
                    }
                }
            }
            toString = buf.toString();
        }
        return toString;
    }

    public static void main(String[] args) {
        ComplexEquation eq = new ComplexPolynomial(new Complex(1.0, 0),
                new Complex(0.0, 1.0), new Complex(0.0, -1.2), new Complex(3.745, -0.9430912));
        System.out.println(eq);

        eq = new ComplexPolynomial(new Complex(-1.0), Complex.ZERO, Complex.ZERO, new Complex(1.0));
        System.out.println(eq);

        eq = new ComplexPolynomial(new Complex(1.0), Complex.ZERO, Complex.ZERO, new Complex(-1.0));
        System.out.println(eq);

        eq = new ComplexPolynomial(new Complex(-1.0), new Complex(3.0, -4.125), new Complex(-1.0), new Complex(1.0));
        System.out.println(eq);

        eq = new ComplexPolynomial(Complex.ZERO, new Complex(3.0, -4.125), new Complex(-1.0), new Complex(1.0));
        System.out.println(eq);

        eq = new ComplexPolynomial(Complex.ZERO, new Complex(3.0, -4.125), new Complex(-1.0), Complex.ZERO);
        System.out.println(eq);
    }
}
