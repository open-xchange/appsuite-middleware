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

package com.openexchange.file.storage.cifs.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.apache.commons.logging.Log;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageAccountManagerLookupService;
import com.openexchange.file.storage.FileStorageAccountManagerProvider;
import com.openexchange.file.storage.FileStorageExceptionCodes;
import com.openexchange.file.storage.FileStorageService;
import com.openexchange.file.storage.cifs.CIFSService;
import com.openexchange.file.storage.cifs.CIFSServices;
import com.openexchange.log.LogFactory;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.osgi.SimpleRegistryListener;
import com.openexchange.sessiond.SessiondService;

/**
 * {@link CIFSActivator} - Activator for CIFS bundle.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CIFSActivator extends HousekeepingActivator {

    volatile CIFSService cifsFileStorageService;

    private volatile CIFSServiceRegisterer registerer;

    /**
     * Initializes a new {@link CIFSActivator}.
     */
    public CIFSActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { FileStorageAccountManagerLookupService.class, SessiondService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            CIFSServices.setServices(this);
            /*
             * Some initialization stuff
             */
            final BundleContext context = this.context;
            final CIFSActivator activator = this;
            track(FileStorageAccountManagerProvider.class, new SimpleRegistryListener<FileStorageAccountManagerProvider>() {

                @Override
                public void added(final ServiceReference<FileStorageAccountManagerProvider> ref, final FileStorageAccountManagerProvider service) {
                    CIFSService cifsFileStorageService = activator.cifsFileStorageService;
                    if (null != cifsFileStorageService) {
                        return;
                    }
                    try {
                        cifsFileStorageService = CIFSService.newInstance(context.getService(ref));
                        activator.registerService(FileStorageService.class, cifsFileStorageService);
                        activator.cifsFileStorageService = cifsFileStorageService;
                    } catch (final OXException e) {
                        if (!FileStorageExceptionCodes.NO_ACCOUNT_MANAGER_FOR_SERVICE.equals(e)) {
                            final Log log = com.openexchange.log.Log.valueOf(LogFactory.getLog(CIFSActivator.class));
                            log.error(e.getMessage(), e);
                        }
                    }
                }

                @Override
                public void removed(final ServiceReference<FileStorageAccountManagerProvider> ref, final FileStorageAccountManagerProvider service) {
                    // Nope
                }
            });
            openTrackers();
            /*
             * Register event handler
             */
            if (false) {
                final Dictionary<String, Object> dict = new Hashtable<String, Object>(1);
                dict.put(EventConstants.EVENT_TOPIC, FileStorageAccountManagerProvider.TOPIC);
                final CIFSServiceRegisterer registerer = new CIFSServiceRegisterer(context);
                registerService(EventHandler.class, registerer, dict);
                this.registerer = registerer;
            }
        } catch (final Exception e) {
            com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(CIFSActivator.class)).error(e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public <S> void registerService(final Class<S> clazz, final S service) {
        super.registerService(clazz, service);
    }

    @Override
    protected void stopBundle() throws Exception {
        try {
            final CIFSServiceRegisterer registerer = this.registerer;
            if (null != registerer) {
                registerer.close();
                this.registerer = null;
            }
            // Clean-up
            cleanUp();
            // Clear service registry
            CIFSServices.setServices(null);
        } catch (final Exception e) {
            com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(CIFSActivator.class)).error(e.getMessage(), e);
            throw e;
        }
    }

}
