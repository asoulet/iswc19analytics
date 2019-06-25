package histogram.representativeness;

public class DigitDistribution implements Derivative {
	
	private double sum = 0;
	private double [] count = new double [9];
	private double [] distribution = new double [9];
	private double alpha = -0.5;
	
	public void add(DigitDistribution distribution) {
		for (int i = 0; i < 9; i++)
			count[i] += distribution.count[i];
	}
	
	public void divide(double n) {
		for (int i = 0; i < 9; i++)
			count[i] /= n;
	}
	
	public void normalize() {
		sum = 0;
		for (int i = 0; i < 9; i++)
			sum += count[i];
		for (int i = 0; i < 9; i++)
			distribution[i] = ((double)count[i]) / sum;
	}
	
	public double getCount(int d) {
		return count[d - 1];
	}
	
	public double getDistribution(int d) {
		return distribution[d - 1];
	}
	
	public double getGeneralizedBenford(int d) {
		return getGeneralizedBenford(d, alpha);
	}
	
	public static double getGeneralizedBenford(int d, double a) {
		return (Math.pow(d + 1, a) - Math.pow(d, a)) / (Math.pow(10, a) - 1);
	}
	
	public static double getDerivative(int d, double a) {
		return (
					Math.pow(d + 1, a) * (Math.log((1 + d) / 10.) * Math.pow(10, a) - Math.log(1 + d))
					- Math.pow(d, a) * (Math.log(d / 10.) * Math.pow(10, a) - Math.log(d))
				) 
				/ sq(Math.pow(10, a) - 1);
	}
	
	private static double sq(double x) {
		return x * x;
	}

	@Override
	public double df(double x) {
		double a = x;
		double value = 0;
		for (int d = 1; d <= 9; d++) {
			value += getDerivative(d, a) * (sq(getGeneralizedBenford(d, a)) - sq(distribution[d - 1])) / (sq(getGeneralizedBenford(d, a)));
		}
		return value;
	}

	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}
	
	public double getWLS() {
		double value = 0;
		for (int d = 1; d <= 9; d++) {
			value += sq(getGeneralizedBenford(d, alpha) - distribution[d - 1]) / (getGeneralizedBenford(d, alpha));
		}
		return value;
	}
	
	public double getMAD() {
		double value = 0;
		for (int d = 1; d <= 9; d++) {
			value += Math.abs(getGeneralizedBenford(d, alpha) - distribution[d - 1]);
		}
		return value / 9;
	}
	
	public double getMissingN() {
		double missing = 0;
		for (int d = 1; d <= 9; d++) {
			double current = count[d - 1] / getGeneralizedBenford(d) - sum;
			if (current > missing)
				missing = current;
		}
		return missing;
	}

	public void report() {
		for (int d = 1; d <= 9; d++)
			System.out.println(d + " " + getCount(d) + " " + getDistribution(d) + " " + getGeneralizedBenford(d));
		
		System.out.println("N = " + sum);
		System.out.println("alpha = " + alpha);
		System.out.println("WLS = " + getWLS());
		System.out.println("missingN = " + getMissingN());
		System.out.println("BenfordCompleteness = " + (sum / (getMissingN() + sum)));
	}

	public void setCount(int digit, double c) {
		count[digit - 1] = c;
	}

	public double getSum() {
		return sum;
	}

	public double getAlpha() {
		return alpha;
	}

	public double getMissing(int d) {
		return 	(getGeneralizedBenford(d) * (getSum() + getMissingN()) - getCount(d));
	}
	
	public Conformity complyBenfordLaw() {
		DigitDistribution dist = new DigitDistribution();
		for (int d = 1; d <= 9; d++)
			dist.setCount(d, (int) Math.round(getGeneralizedBenford(d) * 1000));
		dist.normalize();
		dist.setAlpha(0.0001);
		double mad = dist.getMAD();
		if (mad < 0.006)
			return Conformity.CLOSE;
		if (mad < 0.012)
			return Conformity.ACCEPTABLE;
		if (mad < 0.015)
			return Conformity.MARGINAL;
		return Conformity.NON;
	}
	
	public enum Conformity {
		CLOSE, ACCEPTABLE, MARGINAL, NON
	}

	public long getIterations() {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public boolean isAnalyzable() {
		for (int d = 0; d < 9; d++) {
			if (count[d] == 0)
				return false;
			if (d >= 1 && ((count[d] < count[d - 1] && count[d - 1] > count[d] * 1000) || (count[d - 1] < count[d] && count[d] > count[d - 1] * 1000))) // modification
				return false;
		}
		return true;
	}

}
