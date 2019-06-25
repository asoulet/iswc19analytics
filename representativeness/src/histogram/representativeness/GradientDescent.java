package histogram.representativeness;

public class GradientDescent {

	private double initialX = 0.1;
	private double gamma = 0.01;
	private double precision = 0.000001;
	private Derivative derivative;
	private long iterations = 0;
	
	public GradientDescent(Derivative derivative) {
		this.derivative = derivative;
	}
	
	public double run() {
		iterations = 0;
		double currentX = initialX;
		double previousStepSize = 1;
		while (previousStepSize > precision) {
			double previousX = currentX;
			currentX += -gamma * derivative.df(previousX);
			previousStepSize = Math.abs(currentX - previousX);
			iterations++;
		}
		return currentX;
	}

	public long getIterations() {
		return iterations;
	}
}
