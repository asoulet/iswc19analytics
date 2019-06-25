package statistics.MPMC;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;
import org.apache.log4j.Logger;

import robusta.MPMCMonitor;

public class StatisticsMonitor implements MPMCMonitor {
	
	private static Logger logger = Logger.getLogger(StatisticsMonitor.class);

	private Connection connection = null;
	private PreparedStatement insertProp = null;
	private PreparedStatement insertType = null;
	private String driver;
	private String host;
	private String user;
	private String pwd;
	private String dbname;

	private int statisticsDelay = 15 * 60 * 1000;
	
	public StatisticsMonitor(String filename) {
		Properties properties = new Properties();
		try {
			properties.load(new FileInputStream(filename));
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		driver = properties.getProperty("db_driver");
		host = properties.getProperty("db_host");
		user = properties.getProperty("db_user");
		pwd = properties.getProperty("db_password");
		dbname = properties.getProperty("db_dbname");
		statisticsDelay = Integer.parseInt(properties.getProperty("monitor_delay")) * 1000;
	}

	public void close() {
		try {
			insertProp.close();
			insertProp = null;
			insertType.close();
			insertType = null;
			connection.close();
		} catch (Exception e) {
			//e.printStackTrace();
			logger.error(e);
		}
	}

	@Override
	public void monitor() {
		if (insertProp != null)
			try {
				insertProp.execute();
			} catch (Exception e) {
			}
		if (insertType != null)
			try {
				insertType.execute();
			} catch (Exception e) {
			}
	}

	@Override
	public void end() {
		close();
	}

	@Override
	public void begin() {
		open();
	}

	private void open() {
		try {
			connection = robusta.tools.Database.getConnection(driver, host, user, pwd, dbname);
			insertProp = connection.prepareStatement(
					"insert into " + dbname + ".property_monitoring (`row_number`, weight_sum) "
							+ "select count(*), sum(weight) from " + dbname + ".property;");
			insertType = connection.prepareStatement(
					"insert into " + dbname + ".type_monitoring (`row_number`, weight_sum) "
							+ "select count(*), sum(weight) from " + dbname + ".type;");
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			logger.error(e, e);
		}
	}

	@Override
	public int getDelay() {
		return statisticsDelay ;
	}

}
