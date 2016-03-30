/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.groupware.tx;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.groupware.contexts.Context;

/**
 * TODO remove this duplicate class due to class in com.openexchange.server.
 */
public class ConfigurableDBProvider implements DBProvider {

	private String url;
	private String driver;
	private String login;
	private String password;

	@Override
    public Connection getReadConnection(final Context ctx) {
		try {
			return DriverManager.getConnection(url,login,password);
		} catch (final SQLException e) {
		    // Ignore
		}
		return null;
	}

	@Override
    public void releaseReadConnection(final Context ctx, final Connection con) {
		if(con == null) {
			return;
		}
		try {
			if(!con.isClosed()) {
				con.close();
			}
		} catch (final SQLException e) {
		    // Ignore
		}
	}

	@Override
    public Connection getWriteConnection(final Context ctx) {
		return getReadConnection(ctx);
	}

	@Override
    public void releaseWriteConnection(final Context ctx, final Connection con) {
		releaseReadConnection(ctx,con);
	}

	@Override
    public void releaseWriteConnectionAfterReading(final Context ctx, final Connection con) {
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
