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

package com.openexchange.file.storage.osgi;

import org.osgi.service.event.EventAdmin;
import com.openexchange.file.storage.FileStorageAccountManagerLookupService;
import com.openexchange.file.storage.internal.FileStorageServiceLookup;
import com.openexchange.file.storage.registry.FileStorageServiceRegistry;
import com.openexchange.osgi.HousekeepingActivator;

/**
 * {@link FileStorageActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.18.2
 */
public final class FileStorageActivator extends HousekeepingActivator {

    private volatile OSGIFileStorageServiceRegistry registry;
    private volatile OSGIFileStorageAccountManagerLookupService lookupService;

    /**
     * Initializes a new {@link FileStorageActivator}.
     */
    public FileStorageActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class[] { EventAdmin.class };
    }

    @Override
    protected void startBundle() throws Exception {
        final org.apache.commons.logging.Log log = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(FileStorageActivator.class));
        try {
            if (log.isInfoEnabled()) {
                log.info("starting bundle: com.openexchange.file.storage");
            }
            /*
             * Keep lookup reference
             */
            FileStorageServiceLookup.set(this);
            /*
             * Start registry tracking
             */
            final OSGIFileStorageServiceRegistry registry = new OSGIFileStorageServiceRegistry();
            registry.start(context);
            this.registry = registry;
            /*
             * Start provider tracking
             */
            final OSGIFileStorageAccountManagerLookupService lookupService = new OSGIFileStorageAccountManagerLookupService();
            lookupService.start(context);
            this.lookupService = lookupService;
            /*
             * Register services
             */
            registerService(FileStorageServiceRegistry.class, registry);
            registerService(FileStorageAccountManagerLookupService.class, lookupService);
        } catch (final Exception e) {
            log.error("Starting bundle \"com.openexchange.file.storage\" failed.", e);
            throw e;
        }
    }

    @Override
    public void stopBundle() throws Exception {
        final org.apache.commons.logging.Log log = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(FileStorageActivator.class));
        try {
            if (log.isInfoEnabled()) {
                log.info("stopping bundle: com.openexchange.file.storage");
            }
            unregisterServices();
            /*
             * Stop look-up service
             */
            final OSGIFileStorageAccountManagerLookupService lookupService = this.lookupService;
            if (null != lookupService) {
                lookupService.stop();
                this.lookupService = null;
            }
            /*
             * Stop registry
             */
            final OSGIFileStorageServiceRegistry registry = this.registry;
            if (null != registry) {
                registry.stop();
                this.registry = null;
            }
            /*
             * Clear lookup reference
             */
            FileStorageServiceLookup.set(null);
            cleanUp();
        } catch (final Exception e) {
            log.error("Stopping bundle \"com.openexchange.file.storage\" failed.", e);
            throw e;
        }
    }

}
