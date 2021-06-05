/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
        } catch (Exception e) {
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
