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

import static com.openexchange.java.Autoboxing.i;
import static com.openexchange.mail.autoconfig.tools.Utils.OX_CONTEXT_ID;
import static com.openexchange.mail.autoconfig.tools.Utils.OX_USER_ID;
import static com.openexchange.mail.autoconfig.tools.Utils.getHttpProxyIfEnabled;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.auth.AuthScope;
import org.apache.http.impl.conn.DefaultRoutePlanner;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.mail.autoconfig.tools.ProxyInfo;
import com.openexchange.mail.autoconfig.tools.Services;
import com.openexchange.rest.client.httpclient.util.HttpContextUtils;

/**
 * {@link UserAwareRoutePlanner}
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.4
 */
public class UserAwareRoutePlanner extends DefaultRoutePlanner {

    public final static UserAwareRoutePlanner USER_PLANNER_INSTANCE = new UserAwareRoutePlanner();

    private final static Logger LOGGER = LoggerFactory.getLogger(UserAwareRoutePlanner.class);

    /**
     * Initializes a new {@link UserAwareRoutePlanner}.
     */
    private UserAwareRoutePlanner() {
        super(null);
    }

    @Override
    protected HttpHost determineProxy(HttpHost target, HttpRequest request, HttpContext context) throws HttpException {
        Object attribute = context.getAttribute(OX_CONTEXT_ID);
        if (null == attribute || false == Integer.class.isAssignableFrom(attribute.getClass())) {
            return super.determineProxy(target, request, context);
        }
        Integer contextId = (Integer) attribute;
        attribute = context.getAttribute(OX_USER_ID);
        if (null == attribute || false == Integer.class.isAssignableFrom(attribute.getClass())) {
            return super.determineProxy(target, request, context);
        }
        Integer userId = (Integer) attribute;

        /*
         * Get the configured proxy
         */
        ConfigViewFactory configViewFactory = Services.getService(ConfigViewFactory.class);
        try {
            ConfigView view = configViewFactory.getView(i(userId), i(contextId));
            ProxyInfo proxy = getHttpProxyIfEnabled(view);
            if (null == proxy) {
                return super.determineProxy(target, request, context);
            }
            boolean isHttps = proxy.getProxyUrl().getScheme().equalsIgnoreCase("https");
            int prxyPort = proxy.getProxyUrl().getPort();
            if (prxyPort == -1) {
                prxyPort = isHttps ? 443 : 80;
            }
            HttpHost httpHost = new HttpHost(proxy.getProxyUrl().getHost(), prxyPort, proxy.getProxyUrl().getScheme());

            /*
             * Set credential provider for authentication against the proxy
             */
            AuthScope authScope = new AuthScope(httpHost.getHostName(), httpHost.getPort());
            HttpContextUtils.addCredentialProvider(context, proxy.getProxyLogin(), proxy.getProxyPassword(), authScope);

            return httpHost;
        } catch (OXException e) {
            LOGGER.debug("Unable to get proxy", e);
        }

        return super.determineProxy(target, request, context);
    }

}
