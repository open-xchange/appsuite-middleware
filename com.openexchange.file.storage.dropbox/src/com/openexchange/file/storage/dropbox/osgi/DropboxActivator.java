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

package com.openexchange.file.storage.dropbox.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import com.openexchange.config.ConfigurationService;
import com.openexchange.file.storage.FileStorageAccountManagerLookupService;
import com.openexchange.file.storage.dropbox.DropboxConfiguration;
import com.openexchange.file.storage.dropbox.DropboxServices;
import com.openexchange.file.storage.oauth.OAuthFileStorageAccountEventHandler;
import com.openexchange.mime.MimeTypeMap;
import com.openexchange.net.ssl.config.SSLConfigurationService;
import com.openexchange.oauth.API;
import com.openexchange.oauth.OAuthService;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.oauth.access.OAuthAccessRegistryService;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.sessiond.SessiondEventConstants;
import com.openexchange.sessiond.SessiondService;
import com.openexchange.timer.TimerService;

/**
 * {@link DropboxActivator} - Activator for Dropbox bundle.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DropboxActivator extends HousekeepingActivator {

    /**
     * Initializes a new {@link DropboxActivator}.
     */
    public DropboxActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return new Class<?>[] { FileStorageAccountManagerLookupService.class, ConfigurationService.class, SessiondService.class, MimeTypeMap.class, TimerService.class, OAuthService.class,
            OAuthAccessRegistryService.class, SSLConfigurationService.class };
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            DropboxServices.setServices(this);
            /*
             * Some initialization stuff
             */
            final BundleContext context = this.context;
            DropboxConfiguration.getInstance().configure(getService(ConfigurationService.class));
            /*
             * Register tracker
             */
            track(OAuthServiceMetaData.class, new OAuthServiceMetaDataRegisterer(context));
            openTrackers();
            /*
             * Register event handler
             */
            final Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
            serviceProperties.put(EventConstants.EVENT_TOPIC, SessiondEventConstants.TOPIC_LAST_SESSION);
            registerService(EventHandler.class, new OAuthFileStorageAccountEventHandler(this, API.DROPBOX), serviceProperties);
        } catch (final Exception e) {
            org.slf4j.LoggerFactory.getLogger(DropboxActivator.class).error("", e);
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
            // Clean-up
            cleanUp();
            // Clear service registry
            DropboxServices.setServices(null);
        } catch (final Exception e) {
            org.slf4j.LoggerFactory.getLogger(DropboxActivator.class).error("", e);
            throw e;
        }
    }

}
