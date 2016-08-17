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

package com.openexchange.file.storage.composition.osgi;

import org.osgi.service.event.EventAdmin;
import com.openexchange.config.ConfigurationService;
import com.openexchange.file.storage.composition.FileStreamHandlerRegistry;
import com.openexchange.file.storage.composition.IDBasedFileAccessFactory;
import com.openexchange.file.storage.composition.IDBasedFolderAccessFactory;
import com.openexchange.file.storage.composition.internal.AbstractCompositingIDBasedFileAccess;
import com.openexchange.file.storage.composition.internal.CompositingIDBasedFolderAccess;
import com.openexchange.file.storage.composition.internal.FileStreamHandlerRegistryImpl;
import com.openexchange.file.storage.composition.internal.Services;
import com.openexchange.file.storage.registry.FileStorageServiceRegistry;
import com.openexchange.objectusecount.ObjectUseCountService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.share.ShareService;
import com.openexchange.share.notification.ShareNotificationService;
import com.openexchange.threadpool.ThreadPoolService;


/**
 * {@link FileStorageCompositionActivator}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class FileStorageCompositionActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link FileStorageCompositionActivator}.
     */
    public FileStorageCompositionActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[]{ FileStorageServiceRegistry.class, EventAdmin.class, ThreadPoolService.class, ConfigurationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        final ServiceLookup services = this;
        Services.setServiceLookup(services);

        // The tracking factory
        TrackingIDBasedFileAccessFactory fileAccessFactory = new TrackingIDBasedFileAccessFactory(services, context);
        rememberTracker(fileAccessFactory);

        // Start-up & register FileStreamHandlerRegistry
        FileStreamHandlerRegistryImpl registry = new FileStreamHandlerRegistryImpl(context);
        AbstractCompositingIDBasedFileAccess.setHandlerRegistry(registry);
        rememberTracker(registry);
        trackService(ShareService.class);
        trackService(ShareNotificationService.class);
        trackService(ObjectUseCountService.class);
        openTrackers();

        // Register file access factory
        registerService(IDBasedFileAccessFactory.class, fileAccessFactory);

        // Register folder access factory
        registerService(IDBasedFolderAccessFactory.class, new IDBasedFolderAccessFactory() {

            @Override
            public CompositingIDBasedFolderAccess createAccess(Session session) {
                return new CompositingIDBasedFolderAccess(session, services);
            }

        });

        registerService(FileStreamHandlerRegistry.class, registry);
    }

    @Override
    protected void stopBundle() throws Exception {
        super.stopBundle();
        AbstractCompositingIDBasedFileAccess.setHandlerRegistry(null);
        Services.setServiceLookup(null);
    }

}
