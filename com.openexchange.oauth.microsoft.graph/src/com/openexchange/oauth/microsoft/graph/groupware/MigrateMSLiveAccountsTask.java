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

package com.openexchange.oauth.microsoft.graph.groupware;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.Attributes;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.groupware.update.TaskAttributes;
import com.openexchange.groupware.update.UpdateConcurrency;
import com.openexchange.groupware.update.UpdateExceptionCodes;
import com.openexchange.groupware.update.UpdateTaskV2;
import com.openexchange.oauth.KnownApi;

/**
 * {@link MigrateMSLiveAccountsTask} - Migrates i.e. renames the serviceId and displayName of
 * the old MSLive accounts to the new Microsoft Graph serviceId and displayName.
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public class MigrateMSLiveAccountsTask implements UpdateTaskV2 {

    private static final String RENAME_SOURCE_ID = "UPDATE oauthAccounts SET serviceId=? WHERE serviceId=?";
    private static final String DEPRECATED_SERVICE_ID = "com.openexchange.oauth.msliveconnect";

    private static final String RENAME_DISPLAY_NAME = "UPDATE oauthAccounts SET displayName=? WHERE displayName=?";
    private static final String DEPRECATED_DISPLAY_NAME = "My MS Live account";

    /**
     * Initialises a new {@link MigrateMSLiveAccountsTask}.
     */
    public MigrateMSLiveAccountsTask() {
        super();
    }

    @Override
    public void perform(PerformParameters params) throws OXException {
        Connection connection = params.getConnection();
        try (PreparedStatement stmt = connection.prepareStatement(RENAME_SOURCE_ID)) {
            stmt.setString(1, KnownApi.MICROSOFT_GRAPH.getServiceId());
            stmt.setString(2, DEPRECATED_SERVICE_ID);
            stmt.executeUpdate();
        } catch (SQLException x) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(x, x.getMessage());
        }
        try (PreparedStatement stmt = connection.prepareStatement(RENAME_DISPLAY_NAME)) {
            stmt.setString(1, "My " + KnownApi.MICROSOFT_GRAPH.getDisplayName() + " account");
            stmt.setString(2, DEPRECATED_DISPLAY_NAME);
            stmt.executeUpdate();
        } catch (SQLException x) {
            throw UpdateExceptionCodes.SQL_PROBLEM.create(x, x.getMessage());
        }
        // Also rename accounts with numerical index, e.g. 'My MS Live account (1)' ?
    }

    @Override
    public String[] getDependencies() {
        return new String[0];
    }

    @Override
    public TaskAttributes getAttributes() {
        return new Attributes(UpdateConcurrency.BLOCKING);
    }

}
