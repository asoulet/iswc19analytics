package histogram.MPMC;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;
import org.apache.log4j.Logger;

import robusta.MPMCMonitor;

public class HistogramMonitor implements MPMCMonitor {
	
	private static Logger logger = Logger.getLogger(HistogramMonitor.class);

	private Connection connection = null;
	private PreparedStatement insert = null;
	private String driver;
	private String host;
	private String user;
	private String pwd;
	private String dbname;

	private int histogramDelay = 15 * 60 * 1000;
	
	public HistogramMonitor(String filename) {
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
		histogramDelay = Integer.parseInt(properties.getProperty("histogram_delay")) * 1000;
	}

	public void close() {
		try {
			insert.close();
			insert = null;
			connection.close();
		} catch (Exception e) {
			logger.error(e);
		}
	}

	@Override
	public void monitor() {
			if (insert != null)
				try {
					insert.execute();
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
			insert = connection.prepareStatement(
					"insert into " + dbname + ".histogram_monitoring (row_number, weight_sum, relation_number) "
							+ "select count(*), sum(weight), count(distinct relation) from " + dbname + ".histogram;");
			connection.setAutoCommit(true);
		} catch (SQLException e) {
			logger.error(e);
		}
	}

	@Override
	public int getDelay() {
		return histogramDelay ;
	}

}
