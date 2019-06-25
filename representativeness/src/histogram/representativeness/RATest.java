package histogram.representativeness;

public class RATest {

	public static void main(String[] args) {
		RepresentativenessAnalyzer ra = new RepresentativenessAnalyzer("properties/DBproperties.properties");
		ra.open();
		ra.run();
		ra.close();
	}

}
