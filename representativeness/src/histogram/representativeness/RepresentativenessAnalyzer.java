package histogram.representativeness;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import org.apache.log4j.Logger;

import robusta.MPMCMonitor;



public class RepresentativenessAnalyzer implements MPMCMonitor {

	private static Logger logger = Logger.getLogger(RepresentativenessAnalyzer.class);
	
	private int representativenessDelay = 15 * 60 * 1000;

	private Connection connection;
	private String driver;
	private String host;
	private String user;
	private String pwd;
	private String dbname;
	
	protected DigitDistribution fsd = null;
	protected LawStrategy strategy = LawStrategy.GRADIENT;	
	protected double totalFacts = 0;
	protected double totalAnalyzedFacts = 0;
	protected double totalMissingFacts = 0;

	private PreparedStatement insert;
	private PreparedStatement insertRelationRep;
	private PreparedStatement updateRelationRep;
	private PreparedStatement deleteRelationRep;
	
	public RepresentativenessAnalyzer(String filename) {
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(filename));
		} catch (FileNotFoundException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
		driver = properties.getProperty("db_driver");
		host = properties.getProperty("db_host");
		user = properties.getProperty("db_user");
		pwd = properties.getProperty("db_password");
		dbname = properties.getProperty("db_dbname");
		representativenessDelay = Integer.parseInt(properties.getProperty("representativeness_delay")) * 1000;
	}
	
	public void open() {
		try {
			connection = robusta.tools.Database.getConnection(driver, host, user, pwd, dbname);
			insert = connection.prepareStatement(
					"insert into " + dbname + ".representativeness (total, analyzed, missing, relation_number) VALUES (?,?,?,(select count(*) from " + dbname + ".relation_rep));");
			insertRelationRep = connection.prepareStatement("INSERT INTO " + dbname + ".relation_rep (relation, first_time, analyzed, missing) VALUES (?, NOW(), ?, ?)");
			updateRelationRep = connection.prepareStatement("UPDATE " + dbname + ".relation_rep SET analyzed = ?, missing = ?  WHERE relation = ?");
			deleteRelationRep = connection.prepareStatement("DELETE FROM " + dbname + ".relation_rep WHERE relation = ?");
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			logger.error(e);
		}
	}
	
	public void updateRelation(String relation, double analyzed, double missing) throws Exception {
		try {
			updateRelationRep.setDouble(1, analyzed);
			updateRelationRep.setDouble(2, missing);
			updateRelationRep.setString(3, relation);
			if (updateRelationRep.executeUpdate() == 0)
				insertRelation(relation, analyzed, missing);
		} catch (Exception e) {
			if (e instanceof SQLException)
				insertRelation(relation, analyzed, missing);
			else
				throw e;
		}
	}

	public void deleteRelation(String relation) {
		try {
			deleteRelationRep.setString(1, relation);
		} catch (Exception e) {
			logger.debug(e);
		}
	}



	private void insertRelation(String relation, double analyzed, double missing) throws Exception {
		try {
			insertRelationRep.setString(1, relation);
			insertRelationRep.setDouble(2, analyzed);
			insertRelationRep.setDouble(3, missing);
			insertRelationRep.execute();
		} catch (Exception e) {
				throw e;
		}
	}

	public void close() {
		try {
			if (insert != null)
				insert.close();
			if (insertRelationRep != null)
				insertRelationRep.close();
			if (updateRelationRep != null)
				updateRelationRep.close();
			if (deleteRelationRep != null)
				deleteRelationRep.close();
			connection.close();
		} catch (Exception e) {
			logger.error(e);
		}
	}
	
	public void run() {
		try {
			Statement stmt = connection.createStatement();
			ResultSet rs = stmt.executeQuery("select * from " + dbname + ".relation_fsd order by relation, fsd");
			fsd = null;
			totalFacts = 0;
			totalAnalyzedFacts = 0;
			totalMissingFacts = 0;
			String previous = "";
			while (rs.next()) {
				String relation = rs.getString("relation");
				if (!relation.equals(previous)) {
					if (fsd != null)
						analyzeCurrentDistribution(previous);
					fsd = new DigitDistribution();
					previous = relation;
				}
				int digit = rs.getInt("fsd");
				double weight = rs.getDouble("weight");
				fsd.setCount(digit, weight);
			}
			rs.close();
			stmt.close();
			if (fsd != null)
				analyzeCurrentDistribution(previous);
			insert.setDouble(1, totalFacts);
			insert.setDouble(2, totalAnalyzedFacts);
			insert.setDouble(3, totalMissingFacts);
			insert.execute();
		} catch (SQLException e) {
			logger.error(e);
		}
	}
	
	public void analyzeCurrentDistribution(String relation) {
		fsd.normalize();
		totalFacts += fsd.getSum();
		if (fsd.isAnalyzable()) {
			parameterizeAlpha();
			if (fsd.getMAD() < 0.015) {
				double presentFacts = fsd.getSum();
				double missingFacts = 0;
				double [] missings = new double [10];
				double missingNumbers = 0;
				double ben = 0;
				double freq = 0;
				for (int d = 9; d >= 1; d--) {
						ben += fsd.getGeneralizedBenford(d);
						freq += fsd.getCount(d);
					double v = (freq) /  (ben) - fsd.getSum();
					missingNumbers = Math.max(missingNumbers, v);
				}
				
				missingFacts = 0;
				for (int d = 1; d <= 9; d++) {
					missings[d] = (fsd.getGeneralizedBenford(d) * (fsd.getSum() + missingNumbers) - fsd.getCount(d)) * d;
					missingFacts +=  missings[d];
				}
				totalAnalyzedFacts += presentFacts;
				totalMissingFacts += missingFacts;
				try {
					updateRelation(relation, presentFacts, missingFacts);
				} catch (Exception e) {
					logger.warn(e);
				}
			}
			else
				deleteRelation(relation);
		}
		else
			deleteRelation(relation);
	}

	public final void parameterizeAlpha() {
		switch (strategy ) {
		case GRADIENT:
			GradientDescent gd = new GradientDescent(fsd);
			fsd.setAlpha(gd.run());
			break;
		case BEST:
			fsd.setAlpha(Usual.best(fsd));
			break;
		case BENFORD:
			fsd.setAlpha(0.001);
			break;
		default:
			break;
		}
	}

	@Override
	public void monitor() {
		run();
	}

	@Override
	public void end() {
		close();
	}

	@Override
	public void begin() {
		open();
	}

	@Override
	public int getDelay() {
		return representativenessDelay;
	}
	

}
