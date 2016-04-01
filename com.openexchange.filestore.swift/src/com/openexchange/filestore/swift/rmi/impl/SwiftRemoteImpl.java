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

package com.openexchange.filestore.swift.rmi.impl;

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
import com.openexchange.filestore.swift.rmi.SwiftRemoteManagement;
import com.openexchange.server.ServiceLookup;


/**
 * {@link SwiftRemoteImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class SwiftRemoteImpl implements SwiftRemoteManagement {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(SwiftRemoteImpl.class);

    private final ServiceLookup services;

    /**
     * Initializes a new {@link SwiftRemoteImpl}.
     */
    public SwiftRemoteImpl(ServiceLookup services) {
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
            List<Integer> contextIds = contextService.getAllContextIds();
            Set<Integer> visited = new HashSet<Integer>(contextIds.size(), 0.9f);
            Set<String> swiftIds = new HashSet<String>(1024, 0.9F);
            for (Integer contextId : contextIds) {
                if (visited.add(contextId)) {
                    // Add the Swift identifiers
                    addAllObjectsURLsInSchema(contextId.intValue(), swiftIds, dbService);

                    // Discard other contexts in that schema
                    int[] contextsInSameSchema = dbService.getContextsInSameSchema(contextId.intValue());
                    if (null != contextsInSameSchema) {
                        for (int i = contextsInSameSchema.length; i-- > 0;) {
                            visited.add(Integer.valueOf(contextsInSameSchema[i]));
                        }
                    }
                }
            }

            List<String> sortedIds = new ArrayList<String>(swiftIds);
            swiftIds = null;
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
            stmt = con.prepareStatement("SELECT DISTINCT swift_id FROM swift_filestore");
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
