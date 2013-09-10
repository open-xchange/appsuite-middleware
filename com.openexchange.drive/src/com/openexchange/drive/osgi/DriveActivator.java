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

package com.openexchange.drive.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.apache.commons.logging.Log;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import com.openexchange.config.ConfigurationService;
import com.openexchange.database.CreateTableService;
import com.openexchange.database.DatabaseService;
import com.openexchange.drive.DriveService;
import com.openexchange.drive.checksum.events.ChecksumEventListener;
import com.openexchange.drive.checksum.rdb.DriveCreateTableService;
import com.openexchange.drive.checksum.rdb.DriveCreateTableTask;
import com.openexchange.drive.checksum.rdb.DriveDeleteListener;
import com.openexchange.drive.internal.DriveServiceImpl;
import com.openexchange.drive.internal.DriveServiceLookup;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.file.storage.composition.IDBasedFolderAccessFactory;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.groupware.delete.DeleteListener;
import com.openexchange.groupware.update.DefaultUpdateTaskProviderService;
import com.openexchange.groupware.update.UpdateTaskProviderService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.threadpool.ThreadPoolService;

/**
 * {@link DriveActivator}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class DriveActivator extends HousekeepingActivator {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(DriveActivator.class);

    /**
     * Initializes a new {@link DriveActivator}.
     */
    public DriveActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { IDBasedFileAccessFactory.class, ManagedFileManagement.class, DatabaseService.class,
            IDBasedFolderAccessFactory.class, EventAdmin.class, ConfigurationService.class, ThreadPoolService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        LOG.info("starting bundle: " + context.getBundle().getSymbolicName());
        DriveServiceLookup.set(this);
        registerService(DriveService.class, new DriveServiceImpl());
        registerService(CreateTableService.class, new DriveCreateTableService());
        registerService(UpdateTaskProviderService.class, new DefaultUpdateTaskProviderService(new DriveCreateTableTask()));
        registerService(DeleteListener.class, new DriveDeleteListener());
        Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
        serviceProperties.put(EventConstants.EVENT_TOPIC, ChecksumEventListener.getHandledTopics());
        registerService(EventHandler.class, new ChecksumEventListener(), serviceProperties);
    }

    @Override
    protected void stopBundle() throws Exception {
        LOG.info("stopping bundle: " + context.getBundle().getSymbolicName());
        DriveServiceLookup.set(null);
        super.stopBundle();
    }

}
