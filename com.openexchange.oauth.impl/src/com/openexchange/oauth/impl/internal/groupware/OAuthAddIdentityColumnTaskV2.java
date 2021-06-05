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
 * {@link OAuthAddIdentityColumnTaskV2}
 * Adds the 'identity' column and an index for the identity (cid,identity(191))
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since 7.10.0
 */
public class OAuthAddIdentityColumnTaskV2 extends AbstractOAuthUpdateTask {

    private static final String IDENTITY_NAME = "identity";

    /**
     * Initialises a new {@link OAuthAddIdentityColumnTaskV2}.
     */
    public OAuthAddIdentityColumnTaskV2() {
        super();
    }

    @Override
    void innerPerform(Connection connection, PerformParameters performParameters) throws OXException, SQLException {
        if (!Tools.columnExists(connection, CreateOAuthAccountTable.TABLE_NAME, IDENTITY_NAME)) {
            Tools.addColumns(connection, CreateOAuthAccountTable.TABLE_NAME, new Column(IDENTITY_NAME, "varchar(767)"));
        }
        String indexName = Tools.existsIndex(connection, CreateOAuthAccountTable.TABLE_NAME, new String[] { IDENTITY_NAME });
        if (indexName != null) {
            Tools.dropIndex(connection, CreateOAuthAccountTable.TABLE_NAME, indexName);
        }
        Tools.createIndex(connection, CreateOAuthAccountTable.TABLE_NAME, IDENTITY_NAME, new String[] { "cid", "`identity`(191)" }, false);
    }

    @Override
    public String[] getDependencies() {
        return new String[] { DropForeignKeyFromOAuthAccountTask.class.getName() };
    }

}
