package gsmith.math;

/** Complex number.
 */
public final class Complex implements Cloneable {
    public static final Complex ZERO = new Complex(0.0, 0.0);

    public final double re;
    public final double im;

    // cached hashCode since this is immutable
    private Integer hashCode = null;

    /**
     * Constructor from a real part. The imaginary part will be 0.0.
     */
    public Complex(double re) {
        this(re, 0.0);
    }

    /**
     * Constructor.
     */
    public Complex(double re, double im) {
        this.re = re;
        this.im = im;
    }

    /**
     * Copy constructor.
     */
    public Complex(Complex input) {
        re = input.re;
        im = input.im;
    }

    /** Bean accessor for {@link #re}.
     */
    public double getRe() {
        return re;
    }

    /** Bean accessor for {@link #im}.
     */
    public double getIm() {
        return im;
    }

    /** Add the specified number.
     */
    public Complex add(Complex op) {
        return new Complex(re + op.re, im + op.im);
    }

    /** Add the specified number.
     */
    public Complex add(double op) {
        return new Complex(re + op, im);
    }

    /** Subtract the specified number.
     */
    public Complex sub(Complex op) {
        return new Complex(re - op.re, im - op.im);
    }

    /** Subtract the specified number.
     */
    public Complex sub(double op) {
        return new Complex(re - op, im);
    }

    /** Multiply by the specified number.
     */
    public Complex mul(Complex op) {
        return new Complex(re * op.re - im * op.im, re * op.im + im * op.re);
    }

    /** Multiply by the specified number.
     */
    public Complex mul(double op) {
        return new Complex(re * op, im * op);
    }

    /** Divide by this specified number.
     */
    public Complex div(Complex op) {
        Complex result = mul(op.conjugate());
        return new Complex(result.re / (op.abs() * op.abs()),
                result.im / (op.abs() * op.abs()));
    }

    /** Get the absolute value of this, which is also the "r" of this in polar coordinates.
     */
    public double abs() {
        return Math.sqrt(re * re + im * im);
    }

    /** Raise this to the specified power.
     */
    public Complex pow(double x) {
        double modulus = Math.sqrt(re * re + im * im);
        double arg = Math.atan2(im, re);
        double logRe = Math.log(modulus);
        double logIm = arg;
        double xLogRe = x * logRe;
        double xLogIm = x * logIm;
        double modulusAns = Math.exp(xLogRe);
        return new Complex(modulusAns * Math.cos(xLogIm), modulusAns *
                Math.sin(xLogIm));
    }

    /**
     * Returns a complex k-th root of this complex number. The root that is
     * returned is the one with the smallest positive arg. (If k is 0, the
     * return value is 1. If k is negative, the value is 1/integerRoot(-k).)
     */
    public Complex integerRoot(int k) {
        boolean neg = false;
        if (k < 0) {
            k = -k;
            neg = true;
        }
        double a;
        double b;
        if (k == 0) {
            a = 1;
            b = 0;
        }
        else if (k == 1) {
            a = re;
            b = im;
        }
        else {
            double length = abs();
            double angle = theta();
            if (angle < 0) {
                angle += Math.PI * 2;
            }
            length = Math.pow(length, 1.0 / k);
            angle = angle / k;
            a = length * Math.cos(angle);
            b = length * Math.sin(angle);
        }
        if (neg) {
            double denom = a * a + b * b;
            a = a / denom;
            b = -b / denom;
        }
        return new Complex(a, b);
    }

    /** Get the square root of this.
     */
    public Complex sqrt() {
        return integerRoot(2);
    }

    /**
     * Returns arg(this), the angular polar coordinate of this complex number,
     * in the range -pi to pi. The return value is simply Math.atan2(imaginary
     * part, real part).
     */
    public double theta() {
        return Math.atan2(im, re);
    }

    /** Return a Complex number from the specific polar coordinates.
     */
    public Complex fromPolar(double magnitude, double angle) {
        return new Complex(magnitude * Math.cos(angle), magnitude * Math.sin(angle));
    }

    public Complex conjugate() {
        return new Complex(re, -im);
    }

    @Override
    public String toString() {
        if (re == 0) {
            if (im == 0) {
                return "0";
            }
            else {
                return im + "i";
            }
        }
        else {
            if (im == 0) {
                return Double.toString(re);
            }
            else if (im < 0) {
                return re + " - " + Math.abs(im) + "i";
            }
            else {
                return re + " + " + im + "i";
            }
        }
    }

    public boolean isRealNumber() {
        return Double.doubleToLongBits(re) != Double.doubleToLongBits(0.0);
    }

    @Override
    public int hashCode() {
        if (hashCode == null) {
            int result = 1;
            long temp = Double.doubleToLongBits(im);
            result = 31 * result + (int)(temp ^ (temp >>> 32));
            temp = Double.doubleToLongBits(re);
            result = 31 * result + (int)(temp ^ (temp >>> 32));
            return result;
        }
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (obj instanceof Number) {
            obj = new Complex(((Number)obj).doubleValue());
        }
        else if (!(obj instanceof Complex)) {
            return false;
        }
        Complex other = (Complex)obj;
        if (Double.doubleToLongBits(im) != Double.doubleToLongBits(other.im)) {
            return false;
        }
        if (Double.doubleToLongBits(re) != Double.doubleToLongBits(other.re)) {
            return false;
        }
        return true;
    }

    @Override
    public Complex clone() {
        return new Complex(this);
    }

    public static void main(String argv[]) {
        Complex a = new Complex(3, 4);
        System.out.println("a = " + a);
        // a = Complex.valueOf(a.toString());
        // System.out.println("a=" + a);
        Complex b = new Complex(1.1, -100.998987);
        System.out.println("b = " + b);
        System.out.println("a + b = " + a.add(b));
        System.out.println("a - b = " + a.sub(b));
        System.out.println("a * b = " + a.mul(b));
        System.out.println("a / b = " + a.div(b));
        System.out.println("a ^ 2.5 = " + a.pow(2.5));
        System.out.println("b ^ -0.75 = " + b.pow(-0.75));
        System.out.println("a.abs() = " + a.abs());
        System.out.println("a.theta() = " + a.theta());
        System.out.println("b.abs() = " + b.abs());
        System.out.println("b.theta() = " + b.theta());
    }
}
