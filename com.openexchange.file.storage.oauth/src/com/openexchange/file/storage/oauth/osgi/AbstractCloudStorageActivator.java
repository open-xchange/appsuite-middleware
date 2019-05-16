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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.file.storage.oauth.osgi;

import java.util.Dictionary;
import java.util.Hashtable;
import org.osgi.framework.BundleContext;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.util.tracker.ServiceTrackerCustomizer;
import org.slf4j.LoggerFactory;
import com.openexchange.file.storage.oauth.OAuthFileStorageAccountEventHandler;
import com.openexchange.oauth.KnownApi;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.osgi.HousekeepingActivator;
import com.openexchange.sessiond.SessiondEventConstants;

/**
 * {@link AbstractCloudStorageActivator}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.1
 */
public abstract class AbstractCloudStorageActivator extends HousekeepingActivator {

    /**
     * Initialises a new {@link AbstractCloudStorageActivator}.
     */
    public AbstractCloudStorageActivator() {
        super();
    }

    @Override
    protected void startBundle() throws Exception {
        try {
            BundleContext context = this.context;
            // Register the OAuthServiceMetadata tracker
            track(OAuthServiceMetaData.class, getServiceRegisterer(context));
            openTrackers();
            // Register event handler
            Dictionary<String, Object> serviceProperties = new Hashtable<String, Object>(1);
            serviceProperties.put(EventConstants.EVENT_TOPIC, SessiondEventConstants.TOPIC_LAST_SESSION);
            registerService(EventHandler.class, new OAuthFileStorageAccountEventHandler(this, getAPI()), serviceProperties);
        } catch (final Exception e) {
            LoggerFactory.getLogger(AbstractCloudStorageActivator.class).error("", e);
            throw e;
        }
    }

    /**
     * Returns the {@link ServiceTrackerCustomizer} for the {@link OAuthServiceMetaData} relevant for the
     * specific cloud storage
     *
     * @return the {@link ServiceTrackerCustomizer} for the {@link OAuthServiceMetaData} relevant for the
     */
    protected abstract ServiceTrackerCustomizer<OAuthServiceMetaData, OAuthServiceMetaData> getServiceRegisterer(BundleContext context);

    /**
     * Returns the {@link KnownApi} for the cloud storage
     *
     * @return the {@link KnownApi} for the cloud storage
     */
    protected abstract KnownApi getAPI();
}
