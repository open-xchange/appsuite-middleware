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
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLHandshakeException;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.mail.autoconfig.DefaultAutoconfig;
import com.openexchange.mail.autoconfig.Autoconfig;
import com.openexchange.mail.autoconfig.IndividualAutoconfig;
import com.openexchange.mail.autoconfig.xmlparser.AutoconfigParser;
import com.openexchange.mail.autoconfig.xmlparser.ClientConfig;
import com.openexchange.rest.client.httpclient.HttpClients;
import com.openexchange.server.ServiceLookup;

/**
 * Connects to the Mozilla ISPDB. For more information see <a
 * href="https://developer.mozilla.org/en/Thunderbird/Autoconfiguration">https://developer.mozilla.org/en/Thunderbird/Autoconfiguration</a>
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> Added google-common cache
 */
public class ISPDB extends AbstractProxyAwareConfigSource {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(ISPDB.class);

    private static final String PROPERTY_ISPDB_URL = "com.openexchange.mail.autoconfig.ispdb";

    // -------------------------------------------------------------------------------------------------- //

    private final Cache<String, DefaultAutoconfig> autoConfigCache;

    /**
     * Initializes a new {@link ISPDB}.
     *
     * @param services The service look-up
     */
    public ISPDB(ServiceLookup services) {
        super(services);
        autoConfigCache = CacheBuilder.newBuilder().initialCapacity(16).expireAfterAccess(1, TimeUnit.DAYS).maximumSize(500).build();
    }

    @Override
    public Autoconfig getAutoconfig(String emailLocalPart, String emailDomain, String password, User user, Context context) throws OXException {
        return getAutoconfig(emailLocalPart, emailDomain, password, user, context, true);
    }

    @Override
    public DefaultAutoconfig getAutoconfig(String emailLocalPart, String emailDomain, String password, User user, Context context, boolean forceSecure) throws OXException {
        ConfigViewFactory configViewFactory = services.getService(ConfigViewFactory.class);
        ConfigView view = configViewFactory.getView(user.getId(), context.getContextId());

        String sUrl = view.get(PROPERTY_ISPDB_URL, String.class);
        if (sUrl == null) {
            return null;
        }
        if (!sUrl.endsWith("/")) {
            sUrl += "/";
        }
        sUrl += emailDomain;

        // Check cache
        {
            DefaultAutoconfig autoconfig = autoConfigCache.getIfPresent(sUrl);
            if (null != autoconfig) {
                return generateIndividualAutoconfig(emailLocalPart, emailDomain, autoconfig, forceSecure);
            }
        }

        URL url;
        try {
            url = new URL(sUrl);
        } catch (MalformedURLException e) {
            LOG.warn("Unable to parse URL: {}", sUrl, e);
            return null;
        }

        DefaultHttpClient httpclient = HttpClients.getHttpClient("Open-Xchange ISPDB Client");
        try {
            HttpHost proxy = getHttpProxyIfEnabled(httpclient, view);
            if (null != proxy) {
                httpclient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
            }

            int port = url.getPort();
            if (port < 0) {
                port = url.getProtocol().equalsIgnoreCase("https") ? 443 : 80;
            }
            if (forceSecure) {
                port = 443;
            }

            HttpHost target = new HttpHost(url.getHost(), port, url.getProtocol());
            HttpGet req = new HttpGet(url.getPath());

            LOG.info("Executing request retrieve config XML via {} using {}", target, null == proxy ? "no proxy" : proxy);
            HttpResponse rsp = httpclient.execute(target, req);

            int httpCode = rsp.getStatusLine().getStatusCode();
            if (httpCode != 200) {
                LOG.info("Could not retrieve config XML. Return code was: {}", rsp.getStatusLine());
                return null;
            }

            // Read & parse response
            ClientConfig clientConfig = new AutoconfigParser().getConfig(rsp.getEntity().getContent());
            DefaultAutoconfig autoconfig = getBestConfiguration(clientConfig, emailDomain);
            autoConfigCache.put(sUrl, autoconfig);
            return generateIndividualAutoconfig(emailLocalPart, emailDomain, autoconfig, forceSecure);
        } catch (SSLHandshakeException e) {
            LOG.info("Could not retrieve config XML.", e);
            return null;
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
            httpclient.getConnectionManager().shutdown();
        }
    }

    private DefaultAutoconfig generateIndividualAutoconfig(String emailLocalPart, String emailDomain, DefaultAutoconfig autoconfig, boolean forceSecure) {
        IndividualAutoconfig retval = new IndividualAutoconfig(autoconfig);
        retval.setUsername(autoconfig.getUsername());
        replaceUsername(retval, emailLocalPart, emailDomain);
        retval.setMailStartTls(forceSecure);
        retval.setTransportStartTls(forceSecure);
        return retval;
    }

}
