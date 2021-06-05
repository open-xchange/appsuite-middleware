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

package com.openexchange.oauth.yahoo.internal;

import java.util.concurrent.TimeUnit;
import org.scribe.model.Request;
import org.scribe.model.RequestTuner;
import com.openexchange.net.ssl.SSLSocketFactoryProvider;
import com.openexchange.oauth.yahoo.osgi.Services;

/**
 * {@link YahooRequestTuner}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.6.1
 */
public final class YahooRequestTuner extends RequestTuner {

    private static final YahooRequestTuner INSTANCE = new YahooRequestTuner();

    /**
     * Gets the instance
     *
     * @return The instance
     */
    public static YahooRequestTuner getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes a new {@link YahooRequestTuner}.
     */
    private YahooRequestTuner() {
        super();
    }

    @Override
    public void tune(Request request) {
        request.setConnectTimeout(5, TimeUnit.SECONDS);
        request.setReadTimeout(30, TimeUnit.SECONDS);

        SSLSocketFactoryProvider factoryProvider = Services.getOptionalService(SSLSocketFactoryProvider.class);
        if (null != factoryProvider) {
            request.setSSLSocketFactory(factoryProvider.getDefault());
        }
    }

}
