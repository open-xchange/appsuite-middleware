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

package com.openexchange.mail.autoconfig.http;

import org.apache.http.impl.client.HttpClientBuilder;
import com.openexchange.rest.client.httpclient.DefaultHttpClientConfigProvider;
import com.openexchange.rest.client.httpclient.HttpBasicConfig;

/**
 * {@link AutoConfigHttpConfiguration}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.4
 */
public class AutoConfigHttpConfiguration extends DefaultHttpClientConfigProvider {

    /**
     * Initializes a new {@link AutoConfigHttpConfiguration}.
     *
     */
    public AutoConfigHttpConfiguration() {
        super("autoconfig-server", "Open-Xchange Auto-Config Client");
    }

    @Override
    public HttpBasicConfig configureHttpBasicConfig(HttpBasicConfig config) {
        return config
            .setConnectTimeout(3000)
            .setSocketReadTimeout(10000)
            .setMaxTotalConnections(100)
            .setMaxConnectionsPerRoute(100);
    }

    @Override
    public void modify(HttpClientBuilder builder) {
        super.modify(builder);
        builder.setRoutePlanner(UserAwareRoutePlanner.USER_PLANNER_INSTANCE).setRedirectStrategy(TargetAwareRedirectStrategy.TARGET_STRATEGY_INSTANCE);
    }

}
