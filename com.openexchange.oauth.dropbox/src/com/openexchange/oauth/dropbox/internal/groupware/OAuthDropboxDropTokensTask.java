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

package com.openexchange.oauth.dropbox.internal.groupware;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskAdapter;
import com.openexchange.oauth.KnownApi;

/**
 * {@link OAuthDropboxDropTokensTask} - Update task to drop all OAuth tokens in
 * preparation for OAuth 2.0.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class OAuthDropboxDropTokensTask extends UpdateTaskAdapter {

    /**
     * Initialises a new {@link OAuthDropboxDropTokensTask}.
     */
    public OAuthDropboxDropTokensTask() {
        super();
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection writeConnection = params.getConnection();
        PreparedStatement statement = null;
        try {
            statement = writeConnection.prepareStatement("UPDATE oauthAccounts SET accessToken = ?, accessSecret = ? WHERE serviceId = ?");
            int parameterIndex = 1;
            statement.setString(parameterIndex++, "");
            statement.setString(parameterIndex++, "");
            statement.setString(parameterIndex++, KnownApi.DROPBOX.getServiceId());
            statement.execute();
        } catch (SQLException e) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(e, e.getMessage());
        } finally {
            Databases.closeSQLStuff(statement);
        }
    }

    @Override
    public String[] getDependencies() {
        return new String[] { "com.openexchange.oauth.impl.internal.groupware.OAuthCreateTableTask2" };
    }
}
