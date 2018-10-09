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

package com.openexchange.admin.schemamove.internal;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.admin.daemons.ClientAdminThreadExtended;
import com.openexchange.admin.exceptions.TargetDatabaseException;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.MaintenanceReason;
import com.openexchange.admin.rmi.exceptions.MissingServiceException;
import com.openexchange.admin.rmi.exceptions.NoSuchObjectException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.schemamove.SchemaMoveService;
import com.openexchange.admin.services.AdminServiceRegistry;
import com.openexchange.admin.storage.interfaces.OXContextStorageInterface;
import com.openexchange.admin.storage.interfaces.OXToolStorageInterface;
import com.openexchange.admin.storage.mysqlStorage.OXContextMySQLStorage;
import com.openexchange.admin.storage.mysqlStorage.OXUtilMySQLStorage;
import com.openexchange.context.ContextService;
import com.openexchange.database.DatabaseService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Autoboxing;
import com.openexchange.sessiond.SessiondService;

/**
 * {@link SchemaMoveImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SchemaMoveImpl implements SchemaMoveService {

    private static final Logger LOG = LoggerFactory.getLogger(SchemaMoveImpl.class);

    private static final int DEFAULT_REASON = 1431655765;

    /**
     * Initializes a new {@link SchemaMoveImpl}.
     */
    public SchemaMoveImpl() {
        super();
    }

    @Override
    public void disableSchema(String schemaName) throws StorageException, NoSuchObjectException, TargetDatabaseException, MissingServiceException {
        /*
         * Precondition: a distinct write pool must be set for all contexts of the given schema
         */
        OXToolStorageInterface tool = OXToolStorageInterface.getInstance();
        if (!tool.isDistinctWritePoolIDForSchema(schemaName)) {
            throw new TargetDatabaseException(
                "Cannot proceed with schema move: Multiple write pool IDs are in use for schema " + schemaName);
        }

        /*
         * Disable all enabled contexts with configured maintenance reason
         */
        Integer reasonId = Integer.valueOf(ClientAdminThreadExtended.cache.getProperties().getProp("SCHEMA_MOVE_MAINTENANCE_REASON", Integer.toString(DEFAULT_REASON)));
        OXContextStorageInterface contextStorage = OXContextStorageInterface.getInstance();
        contextStorage.disable(schemaName, new MaintenanceReason(reasonId));
    }

    @Override
    public Map<String, String> getDbAccessInfoForSchema(String schemaName) throws StorageException, NoSuchObjectException {
        if (null == schemaName) {
            return null;
        }
        int writePoolId = OXToolStorageInterface.getInstance().getDatabaseIDByDatabaseSchema(schemaName);
        return fetchDbAccessInfo(writePoolId);
    }

    @Override
    public Map<String, String> getDbAccessInfoForCluster(int clusterId) throws StorageException, NoSuchObjectException {
        if (clusterId <= 0) {
            return null;
        }
        int writePoolId = OXUtilMySQLStorage.getInstance().getWritePoolIdForCluster(clusterId);
        return fetchDbAccessInfo(writePoolId);
    }

    /**
     * Fetch db access information
     *
     * @param writePoolId
     * @return
     * @throws StorageException
     */
    private Map<String, String> fetchDbAccessInfo(int writePoolId) throws StorageException {
        Database database = OXToolStorageInterface.getInstance().loadDatabaseById(writePoolId);

        final Map<String, String> props = new HashMap<String, String>(6);
        class SafePut {

            void put(String name, String value) {
                if (null != value) {
                    props.put(name, value);
                }
            }
        }
        SafePut safePut = new SafePut();

        safePut.put("url", database.getUrl());
        safePut.put("driver", database.getDriver());
        safePut.put("login", database.getLogin());
        safePut.put("name", database.getName());
        safePut.put("password", database.getPassword());

        return props;
    }

    @Override
    public void invalidateContexts(String schemaName, boolean invalidateSession) throws StorageException, MissingServiceException {
        /*
         * Invalidate disabled contexts
         */
        OXContextStorageInterface contextStorage = OXContextStorageInterface.getInstance();
        List<Integer> contextIds = contextStorage.getContextIdsBySchema(schemaName);
        ContextService contextService = getContextService();
        DatabaseService dbService = getDatabaseService();
        try {
            int[] contextIdArray = Autoboxing.I2i(contextIds);
            dbService.invalidate(contextIdArray);
            contextService.invalidateContexts(contextIdArray);
            LOG.info("Invalidated {} cached context objects for schema '{}'", Integer.valueOf(contextIdArray.length), schemaName);
        } catch (OXException e) {
            throw StorageException.wrapForRMI(e);
        }

        if(invalidateSession) {
            /*
             * Kill sessions for the disabled contexts globally
             */
            SessiondService sessiondService = getSessiondService();
            try {
                sessiondService.removeContextSessionsGlobal(new HashSet<Integer>(contextIds));
            } catch (OXException e) {
                throw StorageException.wrapForRMI(e);
            }
        }
    }

    @Override
    public void enableSchema(String schemaName) throws StorageException, NoSuchObjectException, MissingServiceException {
        /*
         * Disable all enabled contexts with configured maintenance reason
         */
        Integer reasonId = Integer.valueOf(ClientAdminThreadExtended.cache.getProperties().getProp("SCHEMA_MOVE_MAINTENANCE_REASON", Integer.toString(DEFAULT_REASON)));
        OXContextStorageInterface contextStorage = OXContextStorageInterface.getInstance();
        contextStorage.enable(schemaName, new MaintenanceReason(reasonId));
    }

    private ContextService getContextService() throws MissingServiceException {
        try {
            return AdminServiceRegistry.getInstance().getService(ContextService.class, true);
        } catch (OXException e) {
            throw new MissingServiceException(e.getMessage());
        }
    }

    private SessiondService getSessiondService() throws MissingServiceException {
        try {
            return AdminServiceRegistry.getInstance().getService(SessiondService.class, true);
        } catch (OXException e) {
            throw new MissingServiceException(e.getMessage());
        }
    }

    private DatabaseService getDatabaseService() throws MissingServiceException {
        try {
            return AdminServiceRegistry.getInstance().getService(DatabaseService.class, true);
        } catch (OXException e) {
            throw new MissingServiceException(e.getMessage());
        }
    }

    @Override
    public void restorePoolReferences(String sourceSchema, String targetSchema, int targetClusterId) throws StorageException {
        OXContextStorageInterface contextStorage = OXContextStorageInterface.getInstance();
        contextStorage.updateContextReferences(sourceSchema, targetSchema, targetClusterId);
    }

    @Override
    public String createSchema(int targetClusterId) throws StorageException {
        OXContextStorageInterface contextStorage = OXContextMySQLStorage.getInstance();
        return contextStorage.createSchema(targetClusterId);
    }

}
