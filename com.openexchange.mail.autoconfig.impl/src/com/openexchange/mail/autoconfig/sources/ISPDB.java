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

package com.openexchange.mail.autoconfig.sources;

import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.mail.autoconfig.tools.Utils.getHttpProxyIfEnabled;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.openexchange.config.cascade.ConfigView;
import com.openexchange.config.cascade.ConfigViewFactory;
import com.openexchange.exception.OXException;
import com.openexchange.mail.autoconfig.Autoconfig;
import com.openexchange.mail.autoconfig.DefaultAutoconfig;
import com.openexchange.mail.autoconfig.IndividualAutoconfig;
import com.openexchange.mail.autoconfig.tools.ProxyInfo;
import com.openexchange.mail.autoconfig.xmlparser.AutoconfigParser;
import com.openexchange.mail.autoconfig.xmlparser.ClientConfig;
import com.openexchange.rest.client.httpclient.HttpClientService;
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
    protected String getAccountId() {
        return "ispdb";
    }

    @Override
    public Autoconfig getAutoconfig(String emailLocalPart, String emailDomain, String password, int userId, int contextId) throws OXException {
        return getAutoconfig(emailLocalPart, emailDomain, password, userId, contextId, true);
    }

    @Override
    public DefaultAutoconfig getAutoconfig(String emailLocalPart, String emailDomain, String password, int userId, int contextId, boolean forceSecure) throws OXException {
        ConfigViewFactory configViewFactory = services.getServiceSafe(ConfigViewFactory.class);
        ConfigView view = configViewFactory.getView(userId, contextId);

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
                if (skipDueToForcedSecure(forceSecure, autoconfig)) {
                    // Either mail or transport do not support a secure connection (or neither of them)
                    return null;
                }

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

        ProxyInfo proxy = getHttpProxyIfEnabled(view);
        HttpClient httpclient = services.getServiceSafe(HttpClientService.class).getHttpClient("autoconfig-ispdb");
        HttpGet req = null;
        HttpResponse rsp = null;
        try {
            int port = getPort(forceSecure, url);
            HttpHost target = new HttpHost(url.getHost(), port, url.getProtocol());
            req = new HttpGet(url.getPath());

            LOG.info("Executing request to retrieve config XML via {} using {}", target, null == proxy ? "no proxy" : proxy);
            rsp = httpclient.execute(target, req, httpContextFor(contextId, userId));

            int httpCode = rsp.getStatusLine().getStatusCode();
            if (httpCode != 200) {
                LOG.info("Could not retrieve config XML. Return code was: {}", rsp.getStatusLine());
                return null;
            }

            // Read & parse response
            ClientConfig clientConfig = new AutoconfigParser().getConfig(rsp.getEntity().getContent());
            DefaultAutoconfig autoconfig = getBestConfiguration(clientConfig, emailDomain);
            if (null == autoconfig) {
                return null;
            }
            autoConfigCache.put(sUrl, autoconfig);

            // If 'forceSecure' is true, ensure that both - mail and transport settings - either support SSL or STARTTLS
            if (skipDueToForcedSecure(forceSecure, autoconfig)) {
                // Either mail or transport do not support a secure connection (or neither of them)
                return null;
            }

            return generateIndividualAutoconfig(emailLocalPart, emailDomain, autoconfig, forceSecure);
        } catch (Exception e) {
            LOG.warn("Could not retrieve config XML.", e);
            return null;
        } finally {
            HttpClients.close(req, rsp);
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

    private int getPort(boolean forceSecure, URL url) {
        int port = url.getPort();
        if (forceSecure) {
            port = 443;
            LOG.debug("Using port 443 because the \"forceSecure\" flag is set");
        } else if (port < 0) {
            /*
             * No port set, e.g. "https://autoconfig.thunderbird.net/v1.1/"
             */
            if (url.getProtocol().equalsIgnoreCase("https")) {
                port = 443;
            } else {
                port = 80;
                LOG.warn("Using port 80 for communication as per configured URL {}. Please note that this is deprecated and might not work in the future. For details see \"https://developer.mozilla.org/en-US/docs/Mozilla/Thunderbird/Autoconfiguration#ssl\"", url);
            }
        } else {
            /* Port set per configuration, e.g. http://autoconfig.thunderbird.net:80/v1.1/ */
            LOG.debug("Using port {} as per configured URL {}", I(port), url.toString());
        }
        return port;
    }

}
