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

package com.openexchange.database.migration.mbean;

import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.LoggerFactory;
import com.openexchange.database.migration.DBMigration;
import com.openexchange.database.migration.internal.DBMigrationExecutorServiceImpl;
import com.openexchange.management.ManagementService;

/**
 * {@link MBeanRegisterer}
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 */
public class MBeanRegisterer implements ServiceTrackerCustomizer<ManagementService, ManagementService> {

    private static final ObjectName PLACEHOLDER = getObjectnameSilent(new DBMigration(null, "placeholder", null, "placeholder"));

    private final BundleContext context;
    private final DBMigrationExecutorServiceImpl dbMigrationExecutorService;
    private final ConcurrentMap<DBMigration, ObjectName> migrations;

    private volatile ManagementService managementService;

    /**
     * Initializes a new {@link MBeanRegisterer}.
     *
     * @param context The bundle context
     * @param dbMigrationExecutorService A reference to the DB migration service
     */
    public MBeanRegisterer(BundleContext context, DBMigrationExecutorServiceImpl dbMigrationExecutorService) {
        super();
        this.context = context;
        this.dbMigrationExecutorService = dbMigrationExecutorService;
        this.migrations = new ConcurrentHashMap<DBMigration, ObjectName>();
    }

    /**
     * Adds a management MBean for the supplied migration.
     *
     * @param migration The migration to add the MBean for
     * @return <code>true</code> if the migration was added, <code>false</code>, otherwise
     */
    public boolean register(DBMigration migration) {
        if (null != migrations.putIfAbsent(migration, PLACEHOLDER)) {
            LoggerFactory.getLogger(MBeanRegisterer.class).error("DBMigration {} already added.", migration);
            return false;
        }
        registerMBeans();
        return true;
    }

    @Override
    public ManagementService addingService(ServiceReference<ManagementService> reference) {
        ManagementService managementService = context.getService(reference);
        this.managementService = managementService;
        registerMBeans();
        return managementService;
    }

    @Override
    public void modifiedService(ServiceReference<ManagementService> reference, ManagementService service) {
        // Nothing to do.
    }

    @Override
    public void removedService(ServiceReference<ManagementService> reference, ManagementService service) {
        unregisterMBeans();
        this.managementService = null;
        context.ungetService(reference);
    }

    private void registerMBeans() {
        ManagementService managementService = this.managementService;
        if (null != managementService) {
            for (Entry<DBMigration, ObjectName> entry : migrations.entrySet()) {
                ObjectName objectName = entry.getValue();
                if (PLACEHOLDER == objectName) {
                    ObjectName name = registerMBean(entry.getKey());
                    if(null != name){
                        entry.setValue(name);
                    }
                }
            }
        }
    }

    private ObjectName registerMBean(DBMigration migration) {
        try {
            ObjectName objectName = getObjectname(migration);
            managementService.registerMBean(objectName, new DefaultDBMigrationMBean(dbMigrationExecutorService, migration));
            return objectName;
        } catch (Exception e) {
            LoggerFactory.getLogger(MBeanRegisterer.class).error("Error registering migration MBean", e);
        }
        return null;
    }

    private void unregisterMBeans() {
        ManagementService managementService = this.managementService;
        if (null != managementService) {
            for (Iterator<Entry<DBMigration, ObjectName>> it = migrations.entrySet().iterator(); it.hasNext();) {
                ObjectName objectName = it.next().getValue();
                if (PLACEHOLDER != objectName) {
                    unregisterMBean(objectName);
                    it.remove();
                }
            }
        }
    }

    private void unregisterMBean(ObjectName objectName) {
        try {
            managementService.unregisterMBean(objectName);
        } catch (Exception e) {
            LoggerFactory.getLogger(MBeanRegisterer.class).error("Error unregistering migration MBean", e);
        }
    }

    private static ObjectName getObjectname(DBMigration migration) throws MalformedObjectNameException {
        Hashtable<String, String> table = new Hashtable<String, String>(2);
        table.put("name", migration.getSchemaName());
        table.put("source", migration.getFileLocation());
        return new ObjectName(DBMigrationMBean.DOMAIN, table);
    }

    private static ObjectName getObjectnameSilent(DBMigration migration) {
        try {
            return getObjectname(migration);
        } catch (MalformedObjectNameException e) {
            LoggerFactory.getLogger(MBeanRegisterer.class).warn("", e);
            return null;
        }
    }

}
