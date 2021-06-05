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

package com.openexchange.datatypes.genericonf.storage.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import com.openexchange.datatypes.genericonf.WidgetSwitcher;
import com.openexchange.java.Autoboxing;

/**
 * {@link FromSQL}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class FromSQL implements WidgetSwitcher {

    private SQLException exception;

    @Override
    public Object checkbox(Object... args) {
        ResultSet rs = (ResultSet) args[0];
        String columnName = (String) args[1];

        try {
            return Autoboxing.valueOf(rs.getBoolean(columnName));
        } catch (SQLException e) {
            this.exception = e;
            return null;
        }
    }

    @Override
    public Object input(Object... args) {
        return string(args);
    }

    private Object string(Object[] args) {
        ResultSet rs = (ResultSet) args[0];
        String columnName = (String) args[1];

        try {
            return rs.getString(columnName);
        } catch (SQLException e) {
            this.exception = e;
            return null;
        }
    }

    @Override
    public Object password(Object... args) {
        return string(args);
    }

    public void throwException() throws SQLException {
        if (exception != null) {
            throw exception;
        }

    }

    @Override
    public Object link(Object... args) {
        return string(args);
    }

    @Override
    public Object text(Object... args) {
        return string(args);
    }

    @Override
    public Object custom(Object... args) {
        return string(args);
    }

}
