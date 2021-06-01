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

package com.openexchange.chronos.schedjoules.http;

import static com.openexchange.java.Autoboxing.i;
import com.openexchange.chronos.schedjoules.impl.SchedJoulesProperty;
import com.openexchange.config.DefaultInterests;
import com.openexchange.config.Interests;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.rest.client.httpclient.DefaultHttpClientConfigProvider;
import com.openexchange.rest.client.httpclient.HttpBasicConfig;
import com.openexchange.server.ServiceLookup;

/**
 * {@link SchedJoulesHttpConfiguration}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.4
 */
public class SchedJoulesHttpConfiguration extends DefaultHttpClientConfigProvider {

    private ServiceLookup services;

    /**
     * Initializes a new {@link SchedJoulesHttpConfiguration}.
     *
     * @param services The service lookup
     */
    public SchedJoulesHttpConfiguration(ServiceLookup services) {
        super("schedjoules", "Open-Xchange SchedJoules Client");
        this.services = services;
    }

    @Override
    public Interests getAdditionalInterests() {
        return DefaultInterests.builder().propertiesOfInterest(SchedJoulesProperty.connectionTimeout.getFQPropertyName()).build();
    }

    @Override
    public HttpBasicConfig configureHttpBasicConfig(HttpBasicConfig config) {
        LeanConfigurationService configurationService = services.getService(LeanConfigurationService.class);
        int timeout = null == configurationService
            ? i(SchedJoulesProperty.connectionTimeout.getDefaultValue(Integer.class))
            : configurationService.getIntProperty(SchedJoulesProperty.connectionTimeout);
        return config.setConnectTimeout(timeout).setSocketReadTimeout(timeout);
    }

}
