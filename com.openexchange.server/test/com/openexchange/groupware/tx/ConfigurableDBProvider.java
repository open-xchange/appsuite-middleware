/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.groupware.tx;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import com.openexchange.database.provider.DBProvider;
import com.openexchange.groupware.contexts.Context;

/**
 * Only used for testing!
 */
public class ConfigurableDBProvider implements DBProvider {

    private String url;
    private String driver;
    private String login;
    private String password;

    /**
     * Initializes a new {@link ConfigurableDBProvider}.
     */
    public ConfigurableDBProvider() {
        super();
    }

    @Override
    public Connection getReadConnection(final Context ctx) {
        try {
            java.util.Properties defaults = new java.util.Properties();
            if (login != null) {
                defaults.put("user", login);
            }
            if (password != null) {
                defaults.put("password", password);
            }
            defaults.setProperty("useSSL", "false");
            return DriverManager.getConnection(url, defaults);
        } catch (SQLException e) {
        }
        return null;
    }

    @Override
    public void releaseReadConnection(final Context ctx, final Connection con) {
        if (con == null) {
            return;
        }
        try {
            if (!con.isClosed()) {
                con.close();
            }
        } catch (SQLException e) {
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
