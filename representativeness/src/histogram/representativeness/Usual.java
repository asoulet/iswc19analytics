package histogram.representativeness;

public class Usual {
	
	private static double [] alphas = {-1, -0.5, 0.001, 0.5, 1};

	public static double best(DigitDistribution fsd) {
		int bestIndex = 0;
		double bestWLS = Double.MAX_VALUE;
		for (int i = 0; i < alphas.length; i++) {
			fsd.setAlpha(alphas[i]);
			double wls = fsd.getWLS();
			if (Math.abs(wls) < bestWLS && wls > 0) {
				bestIndex = i;
				bestWLS = Math.abs(wls);
			}
		}
		return alphas[bestIndex];
	}

}
