package com.openexchange.groupware.tx;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.tx.DBProvider;

public class ConfigurableDBProvider implements DBProvider {

	private String url;
	private String driver;
	private String login;
	private String password;
	
	public Connection getReadConnection(Context ctx) {
		try {
			return DriverManager.getConnection(url,login,password);
		} catch (SQLException e) {
		}
		return null;
	}

	public void releaseReadConnection(Context ctx, Connection con) {
		if(con == null)
			return;
		try {
			if(!con.isClosed())
				con.close();
		} catch (SQLException e) {
		}
	}

	public Connection getWriteConnection(Context ctx) {
		return getReadConnection(ctx);
	}

	public void releaseWriteConnection(Context ctx, Connection con) {
		releaseReadConnection(ctx,con);
	}

	public String getDriver() {
		return driver;
	}

	public void setDriver(String driver) throws ClassNotFoundException {
		Class.forName(driver);
		this.driver = driver;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
