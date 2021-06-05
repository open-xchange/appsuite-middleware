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

package com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.mailsetting;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Set;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.AbstractMultiAttributeChanger;
import com.openexchange.admin.storage.mysqlStorage.user.attribute.changer.Attribute;

/**
 * {@link AbstractUserSettingMailAttributeChanger}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
abstract class AbstractUserSettingMailAttributeChanger extends AbstractMultiAttributeChanger {

    static final String SQL_STATEMENT_TEMPLATE = "UPDATE " + TABLE_TOKEN + " SET " + COLUMN_TOKEN + " WHERE cid = ? AND user = ?";

    /**
     * Initialises a new {@link AbstractUserSettingMailAttributeChanger}.
     */
    public AbstractUserSettingMailAttributeChanger() {
        super();
    }

    @Override
    protected PreparedStatement prepareStatement(String table, Set<Attribute> attributes, Connection connection) throws SQLException {
        String sqlStatement = SQL_STATEMENT_TEMPLATE.replaceAll(TABLE_TOKEN, table).replaceAll(COLUMN_TOKEN, prepareAttributes(attributes));
        return connection.prepareStatement(sqlStatement);
    }

    @Override
    protected PreparedStatement prepareDefaultStatement(String table, Set<Attribute> attributes, Connection connection) throws SQLException {
        String sqlStatement = SQL_STATEMENT_TEMPLATE.replaceAll(TABLE_TOKEN, table).replaceAll(COLUMN_TOKEN, prepareAttributes(attributes).replaceAll("\\?", "DEFAULT"));
        return connection.prepareStatement(sqlStatement);
    }
}
