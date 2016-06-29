package gsmith.math;

/** A complex-number equation. It is expected that toString() will return an
 * appropriate display representation of this equation.
 */
@FunctionalInterface
public interface ComplexEquation {
    /** Evaluate f(x).
     */
    default Complex f(Complex x) {
        return f(x, null);
    }

    /** Evaluate f(x) and f'(x).
     *
     * @param x x.
     * @param deriv a Complex[1] to hold the value of f'(x); if null or length <
     *            1, f'(x) does not need to be computed.
     * @return f(x).
     */
    Complex f(Complex x, Complex[] deriv);
}
