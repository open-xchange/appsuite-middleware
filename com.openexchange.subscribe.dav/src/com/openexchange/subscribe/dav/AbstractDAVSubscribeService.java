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

package com.openexchange.subscribe.dav;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.util.EntityUtils;
import org.apache.jackrabbit.webdav.xml.DomUtil;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import com.google.common.io.BaseEncoding;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.rest.client.httpclient.HttpClients;
import com.openexchange.rest.client.httpclient.HttpClients.ClientConfig;
import com.openexchange.server.ServiceLookup;
import com.openexchange.subscribe.AbstractSubscribeService;
import com.openexchange.subscribe.SubscriptionSource;

/**
 * {@link AbstractDAVSubscribeService} - The abstract super class for *DAV subscribe services.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public abstract class AbstractDAVSubscribeService extends AbstractSubscribeService {

    /** The service look-up */
    protected final ServiceLookup services;

    /** The subscription source */
    protected final SubscriptionSource subscriptionSource;

    /**
     * Initializes a new {@link AbstractDAVSubscribeService}.
     */
    protected AbstractDAVSubscribeService(ServiceLookup services) {
        super();
        this.services = services;
        this.subscriptionSource = initSS();
    }

    @Override
    public SubscriptionSource getSubscriptionSource() {
        return subscriptionSource;
    }

    @Override
    public boolean handles(int folderModule) {
        return getFolderModule().getModule() == folderModule;
    }

    /**
     * Initializes the appropriate subscription source.
     *
     * @param module The associated module
     * @param appendix The identifier appendix
     * @return The subscription source
     */
    protected final SubscriptionSource initSS() {
        SubscriptionSource source = new SubscriptionSource();
        source.setDisplayName(getDisplayName());
        source.setFolderModule(getFolderModule().getModule());
        source.setId(getId());
        source.setSubscribeService(this);

        DynamicFormDescription form = new DynamicFormDescription();
        form.add(FormElement.input("login", FormStrings.FORM_LABEL_LOGIN));
        form.add(FormElement.password("password", FormStrings.FORM_LABEL_PASSWORD));
        source.setFormDescription(form);

        return source;
    }

    /**
     * Sets the Authorization header.
     *
     * @param request The HTTP request
     * @param login The login
     * @param password The password
     */
    protected void setAuthorizationHeader(HttpRequestBase request, String login, String password) {
        String encodedCredentials = BaseEncoding.base64().encode((login + ":" + password).getBytes(Charsets.UTF_8));
        request.setHeader(HttpHeaders.AUTHORIZATION, "Basic " + encodedCredentials);
    }

    /**
     * Initializes the HTTP client to use
     *
     * @param maxTotalConnections The max. total connection; default is 20
     * @param maxConnectionsPerRoute The max. connections per route; default is 10
     * @param conntectTimeout The connect timeout in milliseconds; default is 30000
     * @param readTimeout The read timeout in milliseconds; default is 30000
     * @return The HTTP client instance
     */
    protected HttpClient initHttpClient(int maxTotalConnections, int maxConnectionsPerRoute, int conntectTimeout, int readTimeout) {
        ClientConfig clientConfig = ClientConfig.newInstance()
            .setUserAgent("Open-Xchange DAV Http Client")
            .setMaxTotalConnections(maxTotalConnections > 0 ? maxTotalConnections : 20)
            .setMaxConnectionsPerRoute(maxConnectionsPerRoute > 0 ? maxConnectionsPerRoute : 10)
            .setConnectionTimeout(conntectTimeout > 0 ? conntectTimeout : 30000)
            .setSocketReadTimeout(readTimeout > 0 ? readTimeout : 30000);

        return HttpClients.getHttpClient(clientConfig);
    }

    /**
     * Gets the response body as a DOM document.
     *
     * @param httpResponse The HTTP response providing the response body
     * @return The parsed DOM document
     * @throws IOException If DOM document cannot be parsed
     */
    protected Document getResponseBodyAsDocument(HttpResponse httpResponse) throws IOException {
        InputStream in = httpResponse.getEntity().getContent();
        if (in != null) {
            try {
                return DomUtil.parseDocument(in);
            } catch (ParserConfigurationException e) {
                throw new IOException("XML parser configuration error", e);
            } catch (SAXException e) {
                throw new IOException("XML parsing error", e);
            } finally {
                Streams.close(in);
            }
        }
        return null;
    }

    /**
     * Resets given HTTP request
     *
     * @param request The HTTP request
     */
    protected static void reset(HttpRequestBase request) {
        if (null != request) {
            try {
                request.reset();
            } catch (final Exception e) {
                // Ignore
            }
        }
    }

    /**
     * Ensures that the entity content is fully consumed and the content stream, if exists, is closed silently.
     *
     * @param response The HTTP response to consume and close
     */
    protected static void consume(HttpResponse response) {
        if (null != response) {
            HttpEntity entity = response.getEntity();
            if (null != entity) {
                try {
                    EntityUtils.consume(entity);
                } catch (Exception e) {
                    // Ignore
                }
            }
        }
    }

    /**
     * Closes the supplied HTTP request & response resources silently.
     *
     * @param request The HTTP request to reset
     * @param response The HTTP response to consume and close
     */
    protected static void close(HttpRequestBase request, HttpResponse response) {
        consume(response);
        reset(request);
    }

    /**
     * Builds the URI from given arguments
     *
     * @param baseUri The base URI
     * @param queryString The query string parameters
     * @return The built URI string
     * @throws IllegalArgumentException If the given string violates RFC 2396
     */
    protected static URI buildUri(URI baseUri, List<NameValuePair> queryString, String optPath) {
        try {
            URIBuilder builder = new URIBuilder();
            builder.setScheme(baseUri.getScheme()).setHost(baseUri.getHost()).setPort(baseUri.getPort()).setPath(null == optPath ? baseUri.getPath() : optPath).setQuery(null == queryString ? null : URLEncodedUtils.format(queryString, "UTF-8"));
            return builder.build();
        } catch (final URISyntaxException x) {
            throw new IllegalArgumentException("Failed to build URI", x);
        }
    }

    /**
     * Gets the display name; e.g. <code>"ProviderX"</code>.
     *
     * @return The display name
     */
    protected abstract String getDisplayName();

    /**
     * Gets the identifier; e.g. <code>"com.openexchange.subscribe.carddav.providerX"</code>.
     *
     * @return The identifier
     */
    protected abstract String getId();

    /**
     * Gets the associated folder module.
     *
     * @return The folder module
     */
    protected abstract DAVFolderModule getFolderModule();

}
