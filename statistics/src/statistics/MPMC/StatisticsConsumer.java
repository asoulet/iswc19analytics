package statistics.MPMC;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;

import org.apache.log4j.Logger;

import robusta.consumer.Pattern;
import robusta.consumer.PatternConsumer;

public class StatisticsConsumer extends PatternConsumer {

	private static Logger logger = Logger.getLogger(StatisticsConsumer.class);

	private Connection connection;
	private PreparedStatement insertProp;
	private PreparedStatement updateProp;
	private PreparedStatement insertType;
	private PreparedStatement updateType;
	private String driver;
	private String host;
	private String user;
	private String pwd;
	private String dbname;
	
	public StatisticsConsumer(BlockingQueue<Pattern> queue, String filename) {
		super(queue);
		Properties dbProperties = new Properties();
		try {
			dbProperties.load(new FileInputStream(filename));
		} catch (FileNotFoundException e) {
			logger.error(e);
		} catch (IOException e) {
			logger.error(e);
		}
		driver = dbProperties.getProperty("db_driver");
		host = dbProperties.getProperty("db_host");
		user = dbProperties.getProperty("db_user");
		pwd = dbProperties.getProperty("db_password");
		dbname = dbProperties.getProperty("db_dbname");
	}
	
	public void open() {
		try {
			connection = robusta.tools.Database.getConnection(driver, host, user, pwd, dbname);
			insertProp = connection.prepareStatement("INSERT INTO " + dbname + ".property (label, weight) VALUES (?, ?)");
			updateProp = connection.prepareStatement("UPDATE " + dbname + ".property SET weight = weight + ? WHERE label = ?");
			insertType = connection.prepareStatement("INSERT INTO " + dbname + ".type (label, weight) VALUES (?, ?)");
			updateType = connection.prepareStatement("UPDATE " + dbname + ".type SET weight = weight + ? WHERE label = ?");
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			logger.error(e);
		}
	}

	@Override
	public void begin() {
		open();
	}

	@Override
	public void consume(Pattern pattern) throws Exception {
		StatisticsPattern sp = (StatisticsPattern) pattern;
		logger.debug(sp);
		updateProperty(sp.getProperty(), sp.getWeight(), 0);
		if (sp.getProperty().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type"))
			updateType(sp.getObject(), sp.getWeight(), 0);
	}
	
	@Override
	public void end() {
		close();
	}
	
	public void insertProperty(String property, double weight, int attempts) throws Exception {
		try {
			insertProp.setString(1, property);
			insertProp.setDouble(2, weight);
			insertProp.execute();
		} catch (Exception e) {
			if (e instanceof SQLException)
				updateProperty(property, weight, attempts + 1);
			else
				throw e;
		}
	}

	public void updateProperty(String property, double weight, int attempts) throws Exception {
		try {
			updateProp.setDouble(1, weight);
			updateProp.setString(2, property);
			if (updateProp.executeUpdate() == 0) {
				if (attempts < 5)
					insertProperty(property, weight, attempts);
				else
					logger.debug("error update " + property + " " + weight + " " + attempts);
			}
		} catch (Exception e) {
			if (e instanceof SQLException && attempts < 5)
				insertProperty(property, weight, attempts);
			else
				throw e;
		}
	}

	public void insertType(String type, double weight, int attempts) throws Exception {
		try {
			insertType.setString(1, type);
			insertType.setDouble(2, weight);
			insertType.execute();
		} catch (Exception e) {
			if (e instanceof SQLException)
				updateType(type, weight, attempts + 1);
			else
				throw e;
		}
	}

	public void updateType(String type, double weight, int attempts) throws Exception {
		try {
			updateType.setDouble(1, weight);
			updateType.setString(2, type);
			if (updateType.executeUpdate() == 0) {
				if (attempts < 5)
					insertType(type, weight, attempts);
				else
					logger.debug("error update " + type + " " + weight + " " + attempts);
			}
		} catch (Exception e) {
			if (e instanceof SQLException && attempts < 5)
				insertType(type, weight, attempts);
			else
				throw e;
		}
	}

	public void close() {
		try {
			insertProp.close();
			insertType.close();
			updateProp.close();
			updateType.close();
			connection.close();
		} catch (Exception e) {
			logger.error(e, e);
		}
	}

}
