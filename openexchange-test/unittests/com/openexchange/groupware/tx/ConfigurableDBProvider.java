package com.openexchange.groupware.tx;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.openexchange.groupware.contexts.Context;

public class ConfigurableDBProvider implements DBProvider {

	private String url;
	private String driver;
	private String login;
	private String password;
	
	public Connection getReadConnection(final Context ctx) {
		try {
			return DriverManager.getConnection(url,login,password);
		} catch (final SQLException e) {
		}
		return null;
	}

	public void releaseReadConnection(final Context ctx, final Connection con) {
		if(con == null) {
			return;
		}
		try {
			if(!con.isClosed()) {
				con.close();
			}
		} catch (final SQLException e) {
		}
	}

	public Connection getWriteConnection(final Context ctx) {
		return getReadConnection(ctx);
	}

	public void releaseWriteConnection(final Context ctx, final Connection con) {
		releaseReadConnection(ctx,con);
	}

	public String getDriver() {
		return driver;
	}

	public void setDriver(final String driver) throws ClassNotFoundException {
		Class.forName(driver);
		this.driver = driver;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(final String login) {
		this.login = login;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(final String password) {
		this.password = password;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(final String url) {
		this.url = url;
	}

}
