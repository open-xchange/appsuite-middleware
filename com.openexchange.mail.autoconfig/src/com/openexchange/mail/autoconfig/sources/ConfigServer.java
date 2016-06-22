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

package com.openexchange.mail.autoconfig.sources;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.mail.autoconfig.Autoconfig;
import com.openexchange.mail.autoconfig.xmlparser.AutoconfigParser;
import com.openexchange.mail.autoconfig.xmlparser.ClientConfig;
import com.openexchange.rest.client.httpclient.HttpClients;
import com.openexchange.server.ServiceLookup;

/**
 * {@link ConfigServer}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class ConfigServer extends AbstractProxyAwareConfigSource {

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ConfigServer.class);

    /**
     * Initializes a new {@link ConfigServer}.
     */
    public ConfigServer(ServiceLookup services) {
        super(services);
    }

    @Override
    public Autoconfig getAutoconfig(final String emailLocalPart, final String emailDomain, final String password, final User user, final Context context) throws OXException {
        return getAutoconfig(emailLocalPart, emailDomain, password, user, context, true);
    }

    @Override
    public Autoconfig getAutoconfig(final String emailLocalPart, final String emailDomain, final String password, final User user, final Context context, boolean forceSecure) throws OXException {
        URL url;
        {
            String sUrl = new StringBuilder("http://autoconfig.").append(emailDomain).append("/mail/config-v1.1.xml").toString();
            try {
                url = new URL(sUrl);
            } catch (MalformedURLException e) {
                LOG.warn("Unable to parse URL: {}", sUrl, e);
                return null;
            }
        }

        // New HTTP client
        DefaultHttpClient httpclient = null;
        try {

            {
                int timeout = 3000;
                HttpClients.ClientConfig clientConfig = HttpClients.ClientConfig.newInstance().setConnectionTimeout(timeout).setSocketReadTimeout(timeout).setUserAgent("Open-Xchange Auto-Config Client");
                httpclient = HttpClients.getHttpClient(clientConfig);
            }

            {
                ConfigViewFactory configViewFactory = services.getService(ConfigViewFactory.class);
                ConfigView view = configViewFactory.getView(user.getId(), context.getContextId());
                HttpHost proxy = getHttpProxyIfEnabled(httpclient, view);
                if (null != proxy) {
                    httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
                }
            }

            HttpHost target = new HttpHost(url.getHost(), -1, url.getProtocol());
            HttpGet req = new HttpGet(url.getPath() + "?" + URLEncodedUtils.format(Arrays.<NameValuePair> asList(new BasicNameValuePair("emailaddress", new StringBuilder(emailLocalPart).append('@').append(emailDomain).toString())), "UTF-8"));

            HttpResponse rsp = httpclient.execute(target, req);
            int statusCode = rsp.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                LOG.info("Could not retrieve config XML from autoconfig server. Return code was: {}", statusCode);

                // Try 2nd URL
                {
                    String sUrl = new StringBuilder(64).append("http://").append(emailDomain).append("/.well-known/autoconfig/mail/config-v1.1.xml").toString();
                    try {
                        url = new URL(sUrl);
                    } catch (MalformedURLException e) {
                        LOG.warn("Unable to parse URL: {}", sUrl, e);
                        return null;
                    }
                }
                req = new HttpGet(url.getPath() + "?" + URLEncodedUtils.format(Arrays.<NameValuePair> asList(new BasicNameValuePair("emailaddress", new StringBuilder(emailLocalPart).append('@').append(emailDomain).toString())), "UTF-8"));
                rsp = httpclient.execute(target, req);
                statusCode = rsp.getStatusLine().getStatusCode();
                if (statusCode != 200) {
                    LOG.info("Could not retrieve config XML from main domain. Return code was: {}", statusCode);
                    return null;
                }
            }
            
            Header contentType = rsp.getFirstHeader("Content-Type");
            if (!contentType.getValue().equals("text/xml")) {
                LOG.warn("Could not retrieve config XML from autoconfig server. The response's content type is not of 'text/xml'.");
                return null;
            }

            ClientConfig clientConfig = new AutoconfigParser().getConfig(rsp.getEntity().getContent());

            Autoconfig autoconfig = getBestConfiguration(clientConfig, emailDomain);
            replaceUsername(autoconfig, emailLocalPart, emailDomain);
            autoconfig.setMailStartTls(forceSecure);
            autoconfig.setTransportStartTls(forceSecure);
            return autoconfig;
        } catch (ClientProtocolException e) {
            LOG.warn("Could not retrieve config XML.", e);
            return null;
        } catch (IOException e) {
            LOG.warn("Could not retrieve config XML.", e);
            return null;
        } finally {
            // When HttpClient instance is no longer needed,
            // shut down the connection manager to ensure
            // immediate deallocation of all system resources
            if (null != httpclient) {
                httpclient.getConnectionManager().shutdown();
            }
        }
    }
}

