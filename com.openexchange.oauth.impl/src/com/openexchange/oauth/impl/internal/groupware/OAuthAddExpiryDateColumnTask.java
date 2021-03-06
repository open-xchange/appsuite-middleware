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

package com.openexchange.oauth.impl.internal.groupware;

import java.sql.Connection;
import java.sql.SQLException;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.tools.update.Column;
import com.openexchange.tools.update.Tools;

/**
 * {@link OAuthAddExpiryDateColumnTask} - Adds the <code>expiryDate</code> column to the <code>oauthAccounts</code> table.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public class OAuthAddExpiryDateColumnTask extends AbstractOAuthUpdateTask {

    /**
     * Initializes a new {@link OAuthAddExpiryDateColumnTask}.
     */
    public OAuthAddExpiryDateColumnTask() {
        super();
    }

    @Override
    void innerPerform(Connection connection, PerformParameters performParameters) throws OXException, SQLException {
        if (Tools.columnNotExists(connection, CreateOAuthAccountTable.TABLE_NAME, "expiryDate")) {
            Tools.addColumns(connection, CreateOAuthAccountTable.TABLE_NAME, new Column("expiryDate", "BIGINT(64) DEFAULT NULL"));
        }
    }

    @Override
    public String[] getDependencies() {
        return new String[] { OAuthAddIdentityColumnTaskV2.class.getName() };
    }
}
