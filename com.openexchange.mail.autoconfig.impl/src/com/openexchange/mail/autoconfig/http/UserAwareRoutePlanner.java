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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.mail.autoconfig.http;

import static com.openexchange.java.Autoboxing.i;
import static com.openexchange.mail.autoconfig.tools.Utils.OX_CONTEXT_ID;
import static com.openexchange.mail.autoconfig.tools.Utils.OX_USER_ID;
import static com.openexchange.mail.autoconfig.tools.Utils.getHttpProxyIfEnabled;
import org.apache.http.HttpException;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.conn.DefaultRoutePlanner;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.mail.autoconfig.tools.ProxyInfo;
import com.openexchange.mail.autoconfig.tools.Services;

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
            BasicCredentialsProvider provider = new BasicCredentialsProvider();
            provider.setCredentials(new AuthScope(httpHost.getHostName(), httpHost.getPort()), new UsernamePasswordCredentials(proxy.getProxyLogin(), proxy.getProxyPassword()));
            context.setAttribute(HttpClientContext.CREDS_PROVIDER, provider);

            return httpHost;
        } catch (OXException e) {
            LOGGER.debug("Unable to get proxy", e);
        }

        return super.determineProxy(target, request, context);
    }

}
