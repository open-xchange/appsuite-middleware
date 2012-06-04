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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.groupware.infostore.osgi;

import java.util.Arrays;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Stack;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.provider.DBPoolProvider;
import com.openexchange.file.storage.FileStorageEventConstants;
import com.openexchange.groupware.impl.FolderLockManagerImpl;
import com.openexchange.groupware.infostore.database.impl.InfostoreFilenameReservationsCreateTableTask;
import com.openexchange.groupware.infostore.webdav.EntityLockManagerImpl;
import com.openexchange.groupware.infostore.webdav.LockCleaner;
import com.openexchange.groupware.infostore.webdav.PropertyCleaner;
import com.openexchange.groupware.infostore.webdav.PropertyStoreImpl;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskV2;

/**
 * {@link InfostoreActivator}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class InfostoreActivator implements BundleActivator {

    private final Stack<ServiceRegistration<?>> registrations = new Stack<ServiceRegistration<?>>();

    @Override
    public void start(final BundleContext context) throws Exception {
        final InfostoreFilenameReservationsCreateTableTask task = new InfostoreFilenameReservationsCreateTableTask();
        LockCleaner lockCleaner = new LockCleaner(new FolderLockManagerImpl(new DBPoolProvider()), new EntityLockManagerImpl(new DBPoolProvider(), "infostore_lock"));
        PropertyCleaner propertyCleaner = new PropertyCleaner(new PropertyStoreImpl(new DBPoolProvider(), "oxfolder_property"), new PropertyStoreImpl(new DBPoolProvider(), "infostore_property"));
        Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
        serviceProperties.put(EventConstants.EVENT_TOPIC, FileStorageEventConstants.ALL_TOPICS);
        registrations.push(context.registerService(CreateTableService.class.getName(), task, null));
        registrations.push(context.registerService(UpdateTaskProviderService.class.getName(), new UpdateTaskProviderService() {
            @Override
            public Collection<UpdateTaskV2> getUpdateTasks() {
                return Arrays.asList(((UpdateTaskV2) task));
            }
        }, null));
        registrations.push(context.registerService(EventHandler.class, lockCleaner, serviceProperties));
        registrations.push(context.registerService(EventHandler.class, propertyCleaner, serviceProperties));
    }

    @Override
    public void stop(final BundleContext context) throws Exception {
        while (!registrations.empty()) {
            registrations.pop().unregister();
        }
    }
}
