package org.quickcached.cache.impl.h2database;

import java.sql.Connection;
import java.util.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.h2.jdbcx.JdbcConnectionPool;
import org.quickcached.QuickCached;
import org.quickcached.cache.impl.BaseCacheImpl;

/**
 * H2 based implementation
 * @author Ifteqar Ahmed
 * @author Akshathkumar Shetty
 */
public class H2CacheImpl extends BaseCacheImpl {
	private static final Logger logger = Logger.getLogger(H2CacheImpl.class.getName());
	private static JdbcConnectionPool cp = JdbcConnectionPool.create("jdbc:h2:mem:test", "sa", "sa");

	static {
		createTable();
	}


	public static Connection getConnection() throws SQLException {
		return cp.getConnection();
	}
	
	public String getName() {
		return "H2CacheImpl";
	}
	
	public long getSize() {
		return -1;
	}
	
	public void setToCache(String key, Object value, int objectSize, 
			int expInSec) throws Exception {
		Connection con = null;
		PreparedStatement pstmt = null;
		
		java.sql.Timestamp currentTime = new java.sql.Timestamp(System.currentTimeMillis());
		java.sql.Timestamp expTime = null;

		if (expInSec != 0) {
			expTime = new java.sql.Timestamp(System.currentTimeMillis() + expInSec * 1000);
		}
		
		try {
			con = getConnection();

			pstmt = con.prepareStatement(
					"INSERT INTO DATA_CACHE (KEY, DATA, SIZE, CREATION_TIME_STAMP, EXPIRY_TIME_STAMP, LAST_ACCESS_TIME) values(?,?,?,?,?,?)");
			int k = 1;			

			pstmt.setString(k++, key);
			pstmt.setObject(k++, value);
			pstmt.setInt(k++, objectSize);

			pstmt.setTimestamp(k++, currentTime);
			pstmt.setTimestamp(k++, expTime);
			pstmt.setTimestamp(k++, currentTime);

			pstmt.executeUpdate();
		} catch (Exception e) {
			if(e.getMessage().startsWith("Unique index or primary key violation:")) {
				try {
					pstmt = con.prepareStatement(
						"UPDATE DATA_CACHE SET DATA=?, SIZE=?, CREATION_TIME_STAMP=?, EXPIRY_TIME_STAMP=?, LAST_ACCESS_TIME=? WHERE KEY=?");
					int k = 1;

					pstmt.setObject(k++, value);
					pstmt.setInt(k++, objectSize);

					pstmt.setTimestamp(k++, currentTime);
					pstmt.setTimestamp(k++, expTime);
					pstmt.setTimestamp(k++, currentTime);

					pstmt.setString(k++, key);

					pstmt.executeUpdate();
				} catch (Exception er) {
					logger.log(Level.WARNING, "Update Error: " + er, er);
					throw er;
				}
			} else {
				logger.log(Level.WARNING, "Error: " + e, e);
				throw e;
			}
		} finally {
			try {
				pstmt.close();
			} catch (SQLException e1) {
				logger.log(Level.WARNING, "Error: " + e1, e1);
			}
			try {
				con.close();
			} catch (SQLException e1) {
				logger.log(Level.WARNING, "Error: " + e1, e1);
			}
		}
	}
	
	public void updateToCache(String key, Object value, int objectSize) throws Exception {
		Connection con = null;
		PreparedStatement pstmt = null;
				
		try {
			java.sql.Timestamp currentTime = new java.sql.Timestamp(System.currentTimeMillis());
			
			con = getConnection();
			pstmt = con.prepareStatement(
				"UPDATE DATA_CACHE SET DATA=?, SIZE=?, LAST_ACCESS_TIME=? WHERE KEY=?");
			int k = 1;

			pstmt.setObject(k++, value);
			pstmt.setInt(k++, objectSize);

			pstmt.setTimestamp(k++, currentTime);
			
			pstmt.setString(k++, key);

			pstmt.executeUpdate();
		} catch (Exception er) {
			logger.log(Level.WARNING, "Update Error: " + er, er);
			throw er;
		} finally {
			try {
				pstmt.close();
			} catch (SQLException e1) {
				logger.log(Level.WARNING, "Error: " + e1, e1);
			}
			try {
				con.close();
			} catch (SQLException e1) {
				logger.log(Level.WARNING, "Error: " + e1, e1);
			}
		}
	}
	
	public void updateToCache(String key, Object value, int objectSize, int expInSec) throws Exception {
		Connection con = null;
		PreparedStatement pstmt = null;
				
		try {
			java.sql.Timestamp currentTime = new java.sql.Timestamp(System.currentTimeMillis());
			
			java.sql.Timestamp expTime = null;

			if (expInSec != 0) {
				expTime = new java.sql.Timestamp(System.currentTimeMillis() + expInSec * 1000);
			}	
			
			con = getConnection();
			pstmt = con.prepareStatement(
				"UPDATE DATA_CACHE SET DATA=?, SIZE=?, LAST_ACCESS_TIME=?, EXPIRY_TIME_STAMP=? WHERE KEY=?");
			int k = 1;

			pstmt.setObject(k++, value);
			pstmt.setInt(k++, objectSize);

			pstmt.setTimestamp(k++, currentTime);
			pstmt.setTimestamp(k++, expTime);
			
			pstmt.setString(k++, key);

			pstmt.executeUpdate();
		} catch (Exception er) {
			logger.log(Level.WARNING, "Update Error: " + er, er);
			throw er;
		} finally {
			try {
				pstmt.close();
			} catch (SQLException e1) {
				logger.log(Level.WARNING, "Error: " + e1, e1);
			}
			try {
				con.close();
			} catch (SQLException e1) {
				logger.log(Level.WARNING, "Error: " + e1, e1);
			}
		}
	}
	
	public Object getFromCache(String key) throws Exception {
		Object obj = null;
		Connection con = null;
		PreparedStatement pstmt = null;
		
		try {
			con = getConnection();

			pstmt = con.prepareStatement("SELECT DATA,EXPIRY_TIME_STAMP FROM DATA_CACHE WHERE  KEY = ?");
			int k = 1;
			pstmt.setString(k++, key);
			ResultSet rs = pstmt.executeQuery();

			java.sql.Timestamp expTime = null;
			if(rs.next()) {
				obj = rs.getObject("DATA");
				expTime = rs.getTimestamp("EXPIRY_TIME_STAMP");
			} else {
				if(QuickCached.DEBUG) logger.log(Level.FINE, "no value in db for key: {0}", key);
			}
			
			if (expTime != null && expTime.before(new Date())) {
				logger.log(Level.FINE, "Expired value for key {0}", key);
				obj = null;
			}
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error: " + e, e);
			throw e;
		} finally {
			try {
				pstmt.close();
			} catch (SQLException e1) {
				logger.log(Level.WARNING, "Error: " + e1, e1);
			}
			try {
				con.close();
			} catch (SQLException e1) {
				logger.log(Level.WARNING, "Error: " + e1, e1);
			}
		}
		return obj;
	}
	
	public boolean deleteFromCache(String key) throws Exception {
		Connection con = null;
		int rowsAffected = 0;
		PreparedStatement pstmt = null;
				
		try {
			con = getConnection();
			pstmt = con.prepareStatement("DELETE FROM DATA_CACHE WHERE KEY = ?");
			int k = 1;
			pstmt.setString(k++, key);
			rowsAffected = pstmt.executeUpdate();
			
			if(QuickCached.DEBUG) logger.log(Level.FINE, "rowsAffected: {0}", rowsAffected);
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error: " + e, e);
			throw e;
		} finally {
			try {
				pstmt.close();
			} catch (SQLException e1) {
				logger.log(Level.WARNING, "Error: " + e1, e1);
			}
			try {
				con.close();
			} catch (SQLException e1) {
				logger.log(Level.WARNING, "Error: " + e1, e1);
			}
		}
		
		return rowsAffected > 0;
	}
	
	public void flushCache() throws Exception {
		Connection con = null;
		int rowsAffected = 0;
		PreparedStatement pstmt = null;
		
		if(QuickCached.DEBUG) logger.log(Level.FINE, "flush");
		
		try {
			con = cp.getConnection();
			pstmt = con.prepareStatement("DELETE FROM DATA_CACHE");
			rowsAffected = pstmt.executeUpdate();
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error: " + e, e);
			throw e;
		} finally {
			try {
				pstmt.close();
			} catch (SQLException e1) {
				logger.log(Level.WARNING, "Error: " + e1, e1);
			}
			try {
				con.close();
			} catch (SQLException e1) {
				logger.log(Level.WARNING, "Error: " + e1, e1);
			}
		}
		logger.fine("rows cleared: " + rowsAffected);
	}


	public static void createTable() {
		Connection con = null;
		PreparedStatement pstmt = null;
		try {
			con = cp.getConnection();
			/*
			try {
				pstmt = con.prepareStatement("DROP TABLE DATA_CACHE");
				pstmt.execute();
			} catch (Exception e1) {
				logger.log(Level.WARNING, "Error: " + e1, e1);
			}
			 * 
			 */

			pstmt = con.prepareStatement("CREATE MEMORY TABLE DATA_CACHE(KEY nvarchar (250) PRIMARY KEY HASH,"
					+ "DATA OTHER  NULL,"
					+ " SIZE  numeric (18) NULL  ,"
					+ " CREATION_TIME_STAMP  datetime  NULL  ,"
					+ " EXPIRY_TIME_STAMP  datetime  NULL  ,"
					+ "	LAST_ACCESS_TIME  datetime  NULL);");

			pstmt.execute();

			pstmt = con.prepareStatement("CREATE UNIQUE HASH INDEX INDEX_DATA_CACHE_KEY ON DATA_CACHE (KEY); "
					+ "CREATE INDEX INDEX_DATA_CACHE_EXP ON DATA_CACHE (EXPIRY_TIME_STAMP);"
					+ "CREATE INDEX INDEX_DATA_CACHE_LST ON DATA_CACHE (LAST_ACCESS_TIME);");

			pstmt.execute();

			logger.fine("Table & indexes created");

			PurgeOldData.startCleanUp();
		} catch (Exception e) {
			logger.log(Level.WARNING, "Error: " + e, e);
		} finally {
			try {
				pstmt.close();
			} catch (SQLException e1) {
				logger.log(Level.WARNING, "Error: " + e1, e1);
			}
			try {
				con.close();
			} catch (SQLException e1) {
				logger.log(Level.WARNING, "Error: " + e1, e1);
			}
		}
	}
	
	public boolean saveToDisk() {
		return false;
	}

	public boolean readFromDisk() {
		return false;
	}
}
