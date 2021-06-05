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
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
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

    public final static String CLIENT_ID = "davsub";

    /** The service look-up */
    protected final ServiceLookup services;

    /** The subscription source */
    protected final SubscriptionSource subscriptionSource;

    /**
     * Initializes a new {@link AbstractDAVSubscribeService}.
     *
     * @throws OXException
     */
    protected AbstractDAVSubscribeService(ServiceLookup services) throws OXException {
        super(services.getServiceSafe(com.openexchange.folderstorage.FolderService.class));
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
     * Gets the response body as a DOM document.
     *
     * @param httpResponse The HTTP response providing the response body
     * @return The parsed DOM document
     * @throws IOException If DOM document cannot be parsed
     */
    protected Document getResponseBodyAsDocument(HttpResponse httpResponse) throws IOException {
        InputStream in = httpResponse.getEntity().getContent();
        if (in == null) {
            return null;
        }
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

    /**
     * Resets given HTTP request
     *
     * @param request The HTTP request
     */
    protected static void reset(HttpRequestBase request) {
        if (null == request) {
            return;
        }
        try {
            request.reset();
        } catch (Exception e) {
            // Ignore
        }
    }

    /**
     * Ensures that the entity content is fully consumed and the content stream, if exists, is closed silently.
     *
     * @param response The HTTP response to consume and close
     */
    protected static void consume(HttpResponse response) {
        if (null == response) {
            return;
        }
        HttpEntity entity = response.getEntity();
        if (null == entity) {
            return;
        }
        try {
            EntityUtils.consume(entity);
        } catch (Exception e) {
            // Ignore
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
            URIBuilder builder = new URIBuilder()
                .setScheme(baseUri.getScheme())
                .setHost(baseUri.getHost())
                .setPort(baseUri.getPort())
                .setPath(null == optPath ? baseUri.getPath() : optPath)
                .setQuery(null == queryString ? null : URLEncodedUtils.format(queryString, "UTF-8"));
            return builder.build();
        } catch (URISyntaxException x) {
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
