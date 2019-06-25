package histogram.MPMC;

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

public class RCCDatabaseConsumer extends PatternConsumer {

	private static Logger logger = Logger.getLogger(RCCDatabaseConsumer.class);

	private Connection connection;
	private PreparedStatement insert;
	private PreparedStatement update;
	private String driver;
	private String host;
	private String user;
	private String pwd;
	private String dbname;

	public RCCDatabaseConsumer(BlockingQueue<Pattern> queue, String filename) {
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
			insert = connection.prepareStatement("INSERT INTO " + dbname + ".histogram (relation, cardinality, weight) VALUES (?, ?, ?)");
			update = connection.prepareStatement("UPDATE " + dbname + ".histogram SET weight = weight + ? WHERE relation = ? AND cardinality = ?");
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
		RelationCardinalityCounter cc = (RelationCardinalityCounter) pattern;
		updateResource(cc, true);
	}
	
	@Override
	public void end() {
		close();
	}
	
	public void insertResource(RelationCardinalityCounter pattern) throws Exception {
		try {
			insert.setString(1, pattern.getRelation());
			insert.setInt(2, pattern.getCardinality());
			insert.setDouble(3, pattern.getWeight());
			insert.execute();
		} catch (Exception e) {
			if (e instanceof SQLException)
				updateResource(pattern, false);
			else
				throw e;
		}
	}

	public void updateResource(RelationCardinalityCounter pattern, boolean insertion) throws Exception {
		try {
			update.setDouble(1, pattern.getWeight());
			update.setString(2, pattern.getRelation());
			update.setInt(3, pattern.getCardinality());
			if (update.executeUpdate() == 0 && insertion)
				insertResource(pattern);
			else
				logger.debug("error update " + pattern.getRelation() + " " + pattern.getCardinality());
		} catch (Exception e) {
			if (e instanceof SQLException && insertion)
				insertResource(pattern);
			else
				throw e;
		}
	}

	public void close() {
		try {
			insert.close();
			update.close();
			connection.close();
		} catch (Exception e) {
			logger.error(e);
		}
	}

}
