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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.update.PerformParameters;
import com.openexchange.oauth.KnownApi;
import com.openexchange.oauth.scope.OXScope;

/**
 * {@link RenameMigrateLinkedInServiceIdUpdateTask}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 */
public class RenameMigrateLinkedInServiceIdUpdateTask extends AbstractOAuthUpdateTask {

    /**
     * Initialises a new {@link RenameMigrateLinkedInServiceIdUpdateTask}.
     */
    public RenameMigrateLinkedInServiceIdUpdateTask() {
        super();
    }

    @Override
    void innerPerform(Connection connection, PerformParameters performParameters) throws OXException, SQLException {
        for (int contextId : performParameters.getContextsInSameSchema()) {
            try (PreparedStatement stmt = connection.prepareStatement("UPDATE oauthAccounts SET serviceId=? WHERE cid=? AND serviceId=?")) {
                stmt.setString(1, KnownApi.LINKEDIN.getServiceId());
                stmt.setInt(2, contextId);
                stmt.setString(3, "com.openexchange.socialplugin.linkedin");
                stmt.execute();
            }

            try (PreparedStatement stmt = connection.prepareStatement("UPDATE oauthAccounts SET scope=? WHERE cid=? AND serviceId=?")) {
                stmt.setString(1, OXScope.contacts_ro.name());
                stmt.setInt(2, contextId);
                stmt.setString(3, KnownApi.LINKEDIN.getServiceId());
                stmt.execute();
            }
        }
    }

    @Override
    public String[] getDependencies() {
        return new String[] { OAuthCreateTableTask2.class.getName(), OAuthAddScopeColumnTask.class.getName() };
    }
}
