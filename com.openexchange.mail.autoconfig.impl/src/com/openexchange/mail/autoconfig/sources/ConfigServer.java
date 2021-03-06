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

import static com.openexchange.mail.autoconfig.tools.Utils.OX_TARGET_ID;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HttpContext;
import com.openexchange.exception.OXException;
import com.openexchange.java.Autoboxing;
import com.openexchange.java.InetAddresses;
import com.openexchange.mail.autoconfig.Autoconfig;
import com.openexchange.mail.autoconfig.DefaultAutoconfig;
import com.openexchange.mail.autoconfig.xmlparser.AutoconfigParser;
import com.openexchange.mail.autoconfig.xmlparser.ClientConfig;
import com.openexchange.rest.client.httpclient.HttpClientService;
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
     * 
     * @param services The service lookup
     */
    public ConfigServer(ServiceLookup services) {
        super(services);
    }

    @Override
    protected String getAccountId() {
        return "configServer";
    }

    @Override
    public Autoconfig getAutoconfig(final String emailLocalPart, final String emailDomain, final String password, int userId, int contextId) throws OXException {
        return getAutoconfig(emailLocalPart, emailDomain, password, userId, contextId, true);
    }

    @Override
    public DefaultAutoconfig getAutoconfig(final String emailLocalPart, final String emailDomain, final String password, int userId, int contextId, boolean forceSecure) throws OXException {
        DefaultAutoconfig autoconfig = getAutoconfig(emailLocalPart, emailDomain, userId, contextId, forceSecure, true);
        if (null == autoconfig) {
            /*
             * Fallback to HTTP
             */
            LOG.debug("Trying to get configuration via HTTP because establishing a HTTPS connection failed.");
            return getAutoconfig(emailLocalPart, emailDomain, userId, contextId, forceSecure, false);
        }
        return autoconfig;
    }

    private DefaultAutoconfig getAutoconfig(final String emailLocalPart, final String emailDomain, int userId, int contextId, boolean forceSecure, boolean useTLS) throws OXException {
        URL url = getAutoConfigURL(emailDomain, useTLS);
        if (null == url) {
            return null;
        }
        HttpContext httpContext = httpContextFor(contextId, userId);
        httpContext.setAttribute(OX_TARGET_ID, url);
        HttpClient httpclient = services.getServiceSafe(HttpClientService.class).getHttpClient("autoconfig-server");
        HttpGet req = null;
        HttpResponse rsp = null;
        try {

            HttpHost target = new HttpHost(url.getHost(), -1, url.getProtocol());
            req = new HttpGet(url.getPath() + "?" + URLEncodedUtils.format(Arrays.<NameValuePair> asList(new BasicNameValuePair("emailaddress", new StringBuilder(emailLocalPart).append('@').append(emailDomain).toString())), "UTF-8"));

            rsp = httpclient.execute(target, req, httpContext);
            int statusCode = rsp.getStatusLine().getStatusCode();
            if (statusCode != 200) {
                HttpClients.close(req, rsp);
                req = null;
                rsp = null;
                LOG.info("Could not retrieve config XML from autoconfig server. Return code was: {}", Autoboxing.I(statusCode));

                // Try 2nd URL
                url = getWellKownConfigURL(emailDomain, useTLS);
                if (null == url) {
                    return null;
                }
                req = new HttpGet(url.getPath() + "?" + URLEncodedUtils.format(Arrays.<NameValuePair> asList(new BasicNameValuePair("emailaddress", new StringBuilder(emailLocalPart).append('@').append(emailDomain).toString())), "UTF-8"));
                rsp = httpclient.execute(target, req, httpContext);
                statusCode = rsp.getStatusLine().getStatusCode();
                if (statusCode != 200) {
                    LOG.info("Could not retrieve config XML from main domain. Return code was: {}", Autoboxing.I(statusCode));
                    return null;
                }
            }

            Header contentType = rsp.getFirstHeader("Content-Type");
            if (!contentType.getValue().contains("text/xml")) {
                LOG.info("Could not retrieve config XML from autoconfig server. The response's content type is not of type 'text/xml'.");
                return null;
            }

            ClientConfig clientConfig = new AutoconfigParser().getConfig(rsp.getEntity().getContent());

            DefaultAutoconfig autoconfig = getBestConfiguration(clientConfig, emailDomain);
            if (null == autoconfig) {
                return null;
            }

            // If 'forceSecure' is true, ensure that both - mail and transport settings - either support SSL or STARTTLS
            if (skipDueToForcedSecure(forceSecure, autoconfig)) {
                // Either mail or transport do not support a secure connection (or neither of them)
                return null;
            }

            replaceUsername(autoconfig, emailLocalPart, emailDomain);
            return autoconfig;
        } catch (ClientProtocolException e) {
            LOG.warn("Could not retrieve config XML.", e);
            return null;
        } catch (java.net.UnknownHostException e) {
            // Apparently an invalid host-name was deduced from given auto-config data
            LOG.debug("Could not retrieve config XML.", e);
            return null;
        } catch (IOException e) {
            // Apparently an I/O communication problem occurred while trying to connect to/read from deduced end-point from auto-config data
            LOG.debug("Could not retrieve config XML.", e);
            return null;
        } finally {
            HttpClients.close(req, rsp);
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Checks whether the given URL is invalid or not
     *
     * @param url The URL to check
     * @return <code>true</code> if the URL is invalid, <code>false</code> otherwise
     */
    private static boolean isInvalid(URL url) {
        return isValid(url) == false;
    }

    /**
     * Checks whether the given URL is valid or not
     *
     * @param url The URL to check
     * @return <code>true</code> if the URL is valid, <code>false</code> otherwise
     */
    private static boolean isValid(URL url) {
        try {
            InetAddress inetAddress = InetAddress.getByName(url.getHost());
            if (InetAddresses.isInternalAddress(inetAddress)) {
                return false;
            }
        } catch (UnknownHostException e) {
            return false;
        }
        return true;
    }

    private URL getAutoConfigURL(String emailDomain, boolean useTLS) {
        String sUrl = new StringBuilder(64) // @formatter:off
            .append(useTLS ? "https" : "http")
            .append("://autoconfig.")
            .append(emailDomain)
            .append("/mail/config-v1.1.xml").toString(); // @formatter:on
        return buildAndCheckUrl(sUrl);
    }

    private URL getWellKownConfigURL(String emailDomain, boolean useTLS) {
        String sUrl = new StringBuilder(64) // @formatter:off 
            .append(useTLS ? "https" : "http")
            .append(emailDomain)
            .append("/.well-known/autoconfig/mail/config-v1.1.xml").toString();  // @formatter:on
        return buildAndCheckUrl(sUrl);
    }

    private URL buildAndCheckUrl(String sUrl) {
        try {
            URL url = new URL(sUrl);
            if (isInvalid(url)) {
                LOG.debug("Invalid URL: {}. Skipping config server source for mail auto-config", sUrl);
                return null;
            }
            if("http".equalsIgnoreCase(url.getProtocol())) {
                LOG.warn("Using insecure connection for communication with {}. Please note that this is deprecated and might not work in the future. For details see \"https://developer.mozilla.org/en-US/docs/Mozilla/Thunderbird/Autoconfiguration#ssl\"", url);
            }
            return url;
        } catch (MalformedURLException e) {
            LOG.debug("Unable to parse URL: {}. Skipping config server source for mail auto-config", sUrl, e);
        }
        return null;
    }
}
