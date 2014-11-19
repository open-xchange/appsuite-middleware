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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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
import com.openexchange.admin.daemons.ClientAdminThreadExtended;
import com.openexchange.admin.exceptions.TargetDatabaseException;
import com.openexchange.admin.rmi.dataobjects.Database;
import com.openexchange.admin.rmi.dataobjects.MaintenanceReason;
import com.openexchange.admin.rmi.exceptions.NoSuchObjectException;
import com.openexchange.admin.rmi.exceptions.StorageException;
import com.openexchange.admin.schemamove.SchemaMoveService;
import com.openexchange.admin.services.AdminServiceRegistry;
import com.openexchange.admin.storage.interfaces.OXContextStorageInterface;
import com.openexchange.admin.storage.interfaces.OXToolStorageInterface;
import com.openexchange.context.ContextService;
import com.openexchange.exception.OXException;
import com.openexchange.java.Autoboxing;
import com.openexchange.sessiond.SessiondService;


/**
 * {@link SchemaMoveImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class SchemaMoveImpl implements SchemaMoveService {

    private static final int DEFAULT_REASON = 1431655765;

    /**
     * Initializes a new {@link SchemaMoveImpl}.
     */
    public SchemaMoveImpl() {
        super();
        // TODO Auto-generated constructor stub

    }

    @Override
    public void disableSchema(String schemaName) throws OXException, TargetDatabaseException {
        try {
            /*
             * Precondition: a distinct write pool must be set for all contexts of the given schema
             */
            OXToolStorageInterface tool = OXToolStorageInterface.getInstance();
            if (!tool.isDistinctWritePoolIDForSchema(schemaName)) {
                throw new TargetDatabaseException("Cannot proceed with schema move: Multiple write pool IDs are in use for schema " + schemaName);
            }

            /*
             * Disable all enabled contexts with configured maintenance reason
             */
            Integer reasonId = Integer.parseInt(ClientAdminThreadExtended.cache.getProperties().getProp("SCHEMA_MOVE_MAINTENANCE_REASON", Integer.toString(DEFAULT_REASON)));
            OXContextStorageInterface contextStorage = OXContextStorageInterface.getInstance();
            List<Integer> disabledContexts = contextStorage.disable(schemaName, new MaintenanceReason(reasonId));

            /*
             * Invalidate disabled contexts
             */
            ContextService contextService = AdminServiceRegistry.getInstance().getService(ContextService.class);
            contextService.invalidateContexts(Autoboxing.I2i(disabledContexts));

            /*
             * Kill sessions for the disabled contexts globally
             */
            SessiondService sessiondService = AdminServiceRegistry.getInstance().getService(SessiondService.class, true);
            sessiondService.removeContextSessionsGlobal(new HashSet<Integer>(disabledContexts));
        } catch (StorageException e) {
            throw new OXException(e);
        } catch (NoSuchObjectException e) {
            throw new OXException(e);
        }
    }

    @Override
    public Map<String, String> getDbAccessInfoForSchema(String schemaName) throws OXException {
        try {
            if (null == schemaName) {
                return null;
            }
            int writePoolId = OXToolStorageInterface.getInstance().getDatabaseIDByDatabaseSchema(schemaName);
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
            safePut.put("schema", schemaName);
            safePut.put("driver", database.getDriver());
            safePut.put("login", database.getLogin());
            safePut.put("name", database.getName());
            safePut.put("password", database.getPassword());

            return props;
        } catch (StorageException e) {
            throw new OXException(e);
        } catch (NoSuchObjectException e) {
            throw new OXException(e);
        }
    }

    @Override
    public void enableSchema(String schemaName, String sourceSchema, boolean deleteSource) throws OXException {
        // TODO Auto-generated method stub

    }

}
