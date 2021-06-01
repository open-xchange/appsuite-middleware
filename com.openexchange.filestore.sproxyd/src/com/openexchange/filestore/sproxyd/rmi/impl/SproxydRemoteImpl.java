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

package com.openexchange.filestore.sproxyd.rmi.impl;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import com.openexchange.auth.Authenticator;
import com.openexchange.auth.Credentials;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.database.Databases;
import com.openexchange.exception.OXException;
import com.openexchange.filestore.sproxyd.rmi.SproxydRemoteManagement;
import com.openexchange.server.ServiceLookup;


/**
 * {@link SproxydRemoteImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class SproxydRemoteImpl implements SproxydRemoteManagement {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(SproxydRemoteImpl.class);

    private final ServiceLookup services;

    /**
     * Initializes a new {@link SproxydRemoteImpl}.
     */
    public SproxydRemoteImpl(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public List<String> listAllObjectsURLs(String adminUser, String adminPassword) throws RemoteException {
        authenticate(adminUser, adminPassword);

        DatabaseService dbService = services.getOptionalService(DatabaseService.class);
        if (null == dbService) {
            throw new RemoteException("Required service is missing");
        }

        ContextService contextService = services.getOptionalService(ContextService.class);
        if (null == contextService) {
            throw new RemoteException("Required service is missing");
        }

        try {
            List<Integer> contextIds = contextService.getDistinctContextsPerSchema();
            Set<String> scalityIds = new HashSet<String>(contextIds.size(), 0.9F);
            for (Integer contextId : contextIds) {
                // Add the Scality identifiers
                addAllObjectsURLsInSchema(contextId.intValue(), scalityIds, dbService);
            }

            List<String> sortedIds = new ArrayList<String>(scalityIds);
            scalityIds = null;
            Collections.sort(sortedIds);
            return sortedIds;
        } catch (OXException e) {
            LOG.error("", e);
            throw new RemoteException(e.getMessage());
        } catch (RuntimeException e) {
            LOG.error("", e);
            throw new RemoteException("A runtime error occurred: " + e.getMessage());
        }
    }

    private void addAllObjectsURLsInSchema(int idOfContextInSchema, Set<String> set, DatabaseService dbService) throws RemoteException {
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet result = null;
        try {
            con = dbService.getReadOnly(idOfContextInSchema);
            stmt = con.prepareStatement("SELECT DISTINCT scality_id FROM scality_filestore");
            result = stmt.executeQuery();
            while (result.next()) {
                set.add(result.getString(1));
            }
        } catch (OXException e) {
            LOG.error("", e);
            throw new RemoteException(e.getMessage());
        } catch (SQLException e) {
            LOG.error("", e);
            throw new RemoteException("An SQL error occurred: " + e.getMessage());
        } catch (RuntimeException e) {
            LOG.error("", e);
            throw new RemoteException("A runtime error occurred: " + e.getMessage());
        } finally {
            Databases.closeSQLStuff(result, stmt);
            if (null != con) {
                dbService.backReadOnly(idOfContextInSchema, con);
            }
        }
    }

    private void authenticate(String adminUser, String adminPassword) throws RemoteException {
        Authenticator authenticator = services.getOptionalService(Authenticator.class);
        if (null == authenticator) {
            throw new RemoteException("Required service is missing");
        }

        try {
            authenticator.doAuthentication(new Credentials(adminUser, adminPassword));
        } catch (OXException e) {
            LOG.debug("", e);
            throw new RemoteException("The credentials are invalid");
        }
    }

}
