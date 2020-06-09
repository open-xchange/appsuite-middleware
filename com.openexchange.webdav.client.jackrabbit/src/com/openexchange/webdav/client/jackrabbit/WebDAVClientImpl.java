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

package com.openexchange.webdav.client.jackrabbit;

import static com.openexchange.webdav.client.jackrabbit.Utils.addHeaders;
import static com.openexchange.webdav.client.jackrabbit.Utils.asOXException;
import static com.openexchange.webdav.client.jackrabbit.Utils.getPropertyNameSet;
import static com.openexchange.webdav.client.jackrabbit.Utils.getPropertySet;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.protocol.HttpContext;
import org.apache.jackrabbit.webdav.DavException;
import org.apache.jackrabbit.webdav.DavServletResponse;
import org.apache.jackrabbit.webdav.MultiStatus;
import org.apache.jackrabbit.webdav.MultiStatusResponse;
import org.apache.jackrabbit.webdav.client.methods.BaseDavRequest;
import org.apache.jackrabbit.webdav.client.methods.HttpCopy;
import org.apache.jackrabbit.webdav.client.methods.HttpDelete;
import org.apache.jackrabbit.webdav.client.methods.HttpLock;
import org.apache.jackrabbit.webdav.client.methods.HttpMkcol;
import org.apache.jackrabbit.webdav.client.methods.HttpMove;
import org.apache.jackrabbit.webdav.client.methods.HttpPropfind;
import org.apache.jackrabbit.webdav.client.methods.HttpProppatch;
import org.apache.jackrabbit.webdav.client.methods.HttpReport;
import org.apache.jackrabbit.webdav.client.methods.HttpUnlock;
import org.apache.jackrabbit.webdav.lock.LockInfo;
import org.apache.jackrabbit.webdav.lock.Scope;
import org.apache.jackrabbit.webdav.lock.Type;
import org.apache.jackrabbit.webdav.property.DavPropertySet;
import org.apache.jackrabbit.webdav.version.report.ReportInfo;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.rest.client.httpclient.HttpClientService;
import com.openexchange.rest.client.httpclient.HttpClients;
import com.openexchange.rest.client.httpclient.ManagedHttpClient;
import com.openexchange.server.ServiceLookup;
import com.openexchange.webdav.client.WebDAVClient;
import com.openexchange.webdav.client.WebDAVClientException;
import com.openexchange.webdav.client.WebDAVClientExceptionCodes;
import com.openexchange.webdav.client.WebDAVResource;
import com.openexchange.webdav.client.WebDAVXmlBody;
import com.openexchange.webdav.client.functions.ErrorAwareFunction;

/**
 * {@link WebDAVClientImpl}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.4
 */
public class WebDAVClientImpl implements WebDAVClient {

    /** The identifier prefix for obtaining a WebDAV-associated HTTP client */
    public final static String HTTP_CLIENT_ID = "webdav";

    private final HttpClient httpClient;
    private final HttpContext context;
    private final URI baseUrl;

    /**
     * Initializes a new {@link WebDAVClientImpl}.
     *
     * @param clientProvider The provides the HTTP client to use
     * @param context The context to pass when executing an HTTP request
     * @param baseUrl The URL of the WebDAV host to connect to
     */
    private WebDAVClientImpl(HttpClient httpClient, HttpContext context, URI baseUrl) {
        super();
        this.httpClient = httpClient;
        this.context = context;
        this.baseUrl = baseUrl;
    }

    /**
     * Initializes a new {@link WebDAVClientImpl}.
     *
     * @param client The underlying HTTP client to use
     * @param baseUrl The URL of the WebDAV host to connect to
     * @param services The service look-up providing OSGi services
     */
    public WebDAVClientImpl(HttpClient client, URI baseUrl) {
        this(client, null, baseUrl);
    }

    /**
     * Initializes a new {@link WebDAVClientImpl}.
     *
     * @param baseUrl The URL of the WebDAV host to connect to
     * @param login The user name to use for authentication
     * @param password The password to use for authentication
     * @param services The service look-up providing OSGi services
     * @param optClientId The optional http client id to use
     * @throws OXException If initialization fails
     */
    public WebDAVClientImpl(URI baseUrl, String login, String password, ServiceLookup services, Optional<String> optClientId) throws IllegalStateException, OXException {
        this(initDefaultClient(services, optClientId), initDefaultContext(baseUrl, login, password), baseUrl);
    }

    private HttpResponse execute(HttpUriRequest request) throws IOException, ClientProtocolException {
        return context == null ? httpClient.execute(request) : httpClient.execute(request, context);
    }

    @Override
    public List<WebDAVResource> propFind(String href, int depth, Set<QName> props, Map<String, String> headers) throws WebDAVClientException {
        HttpPropfind request = null;
        HttpResponse response = null;
        try {
            request = addHeaders(new HttpPropfind(getUri(href), getPropertyNameSet(props), depth), headers);
            response = execute(request);
            request.checkSuccess(response);
            MultiStatus multiStatus = request.getResponseBodyAsMultiStatus(response);
            return parseResources(multiStatus, DavServletResponse.SC_OK);
        } catch (Exception e) {
            throw asOXException(e);
        } finally {
            HttpClients.close(request, response);
        }
    }

    @Override
    public <T> T report(String href, WebDAVXmlBody body, ErrorAwareFunction<Document, T> handler, Map<String, String> headers) throws WebDAVClientException {
        HttpReport request = null;
        HttpResponse response = null;
        try {
            ReportInfo reportInfo = new ReportInfo(body.toXML(), 0);
            Element xml = body.toXML();
            reportInfo.setContentElement(xml);

            HttpReport report = new HttpReport(getUri(href), reportInfo);
            request = addHeaders(report, headers);
            response = execute(request);
            Document doc = request.getResponseBodyAsDocument(response.getEntity());
            request.checkSuccess(response);
            return handler.apply(doc);
        } catch (Exception e) {
            throw asOXException(e);
        } finally {
            HttpClients.close(request, response);
        }
    }

    /**
     * Converts the given document into a string
     *
     * @param doc The {@link Document} to convert
     * @return The String representation
     * @throws OXException
     */
    private static String convertDocumentToString(Document doc) throws OXException {
        try {
            final TransformerFactory tf = TransformerFactory.newInstance();
            final Transformer transformer = tf.newTransformer();
            final StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            return writer.getBuffer().toString();
        } catch (TransformerException e) {
            throw WebDAVClientExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    @Override
    public <T> T search(String href, WebDAVXmlBody body,  ErrorAwareFunction<Document, T> handler, Map<String, String> headers) throws WebDAVClientException {

        BaseDavRequest request = null;
        HttpResponse response = null;
        try {
            request = new BaseDavRequest(getUri(href)) {
                @Override
                public String getMethod() {
                    return "SEARCH";
                }
            };

            final StringEntity entity = new StringEntity(convertDocumentToString(body.toXML().getOwnerDocument()));
            entity.setContentType("text/xml");
            request.setEntity(entity);

            request = addHeaders(request, headers);
            response = execute(request);
            HttpEntity responseEntity = response.getEntity();
            Document doc = request.getResponseBodyAsDocument(responseEntity);
            request.checkSuccess(response);
            return handler.apply(doc);
        } catch (Exception e) {
            throw asOXException(e);
        } finally {
            HttpClients.close(request, response);
        }
    }

    @Override
    public void delete(String href, Map<String, String> headers) throws WebDAVClientException {
        HttpDelete request = null;
        HttpResponse response = null;
        try {
            request = addHeaders(new HttpDelete(getUri(href)), headers);
            response = execute(request);
            request.checkSuccess(response);
        } catch (Exception e) {
            throw asOXException(e);
        } finally {
            HttpClients.close(request, response);
        }
    }

    @Override
    public boolean exists(String href, Map<String, String> headers) throws WebDAVClientException {
        HttpHead request = null;
        HttpResponse response = null;
        try {
            request = addHeaders(new HttpHead(getUri(href)), headers);
            request = new HttpHead(getUri(href));
            response = execute(request);
            int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status <= 299) {
                return true;
            }
            if (DavServletResponse.SC_NOT_FOUND == status) {
                return false;
            }
            throw new DavException(status, response.getStatusLine().getReasonPhrase());
        } catch (Exception e) {
            throw asOXException(e);
        } finally {
            HttpClients.close(request, response);
        }
    }

    @Override
    public InputStream get(String href, Map<String, String> headers) throws WebDAVClientException {
        HttpGet request = null;
        HttpResponse response = null;
        try {
            request = addHeaders(new HttpGet(getUri(href)), headers);
            response = execute(request);
            int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status <= 299) {
                HttpResponseStream responseStream = new HttpResponseStream(response);
                response = null;
                request = null;
                return responseStream;
            }
            throw new DavException(status, response.getStatusLine().getReasonPhrase());
        } catch (Exception e) {
            throw asOXException(e);
        } finally {
            HttpClients.close(request, response);
        }
    }

    @Override
    public void mkCol(String href, Map<String, String> headers) throws WebDAVClientException {
        HttpMkcol request = null;
        HttpResponse response = null;
        try {
            request = addHeaders(new HttpMkcol(getUri(href)), headers);
            response = execute(request);
            request.checkSuccess(response);
        } catch (Exception e) {
            throw asOXException(e);
        } finally {
            HttpClients.close(request, response);
        }
    }

    @Override
    public void move(String href, String destinationHref, Map<String, String> headers) throws WebDAVClientException {
        HttpMove request = null;
        HttpResponse response = null;
        try {
            request = addHeaders(new HttpMove(getUri(href), getUri(destinationHref), true), headers);
            response = execute(request);
            request.checkSuccess(response);
        } catch (Exception e) {
            throw asOXException(e);
        } finally {
            HttpClients.close(request, response);
        }
    }

    @Override
    public void copy(String href, String destinationHref, Map<String, String> headers) throws WebDAVClientException {
        HttpCopy request = null;
        HttpResponse response = null;
        try {
            request = addHeaders(new HttpCopy(getUri(href), getUri(destinationHref), true, true), headers);
            response = execute(request);
            request.checkSuccess(response);
        } catch (Exception e) {
            throw asOXException(e);
        } finally {
            HttpClients.close(request, response);
        }
    }

    @Override
    public void propPatch(String href, Map<QName, Object> propsToSet, Set<QName> propsToRemove, Map<String, String> headers) throws WebDAVClientException {
        HttpProppatch request = null;
        HttpResponse response = null;
        try {
            request = addHeaders(new HttpProppatch(getUri(href), getPropertySet(propsToSet), getPropertyNameSet(propsToRemove)), headers);
            response = execute(request);
            request.checkSuccess(response);
        } catch (Exception e) {
            throw asOXException(e);
        } finally {
            HttpClients.close(request, response);
        }
    }

    @Override
    public void put(String href, InputStream content, String contentType, long contentLength, Map<String, String> headers) throws WebDAVClientException {
        ThresholdFileHolder fileHolder = null;
        try {
            fileHolder = new ThresholdFileHolder();
            fileHolder.write(content);
            HttpEntity entity;
            if (fileHolder.isInMemory()) {
                entity = new ByteArrayEntity(fileHolder.getBuffer().toByteArray(), ContentType.parse(contentType));
            } else {
                entity = new FileEntity(fileHolder.getTempFile(), ContentType.parse(contentType));
            }
            put(href, entity, headers);
        } catch (OXException e) {
            throw asOXException(e);
        } finally {
            Streams.close(fileHolder);
        }
    }

    @Override
    public String lock(String href, long timeout, Map<String, String> headers) throws WebDAVClientException {
        LockInfo lockInfo = new LockInfo(Scope.EXCLUSIVE, Type.WRITE, null, 0 < timeout ? timeout : LockInfo.INFINITE_TIMEOUT, false);
        HttpLock request = null;
        HttpResponse response = null;
        try {
            request = addHeaders(new HttpLock(getUri(href), lockInfo), headers);
            response = execute(request);
            request.checkSuccess(response);
            return request.getLockToken(response);
        } catch (Exception e) {
            throw asOXException(e);
        } finally {
            HttpClients.close(request, response);
        }
    }

    @Override
    public void unlock(String href, String lockToken, Map<String, String> headers) throws WebDAVClientException {
        HttpUnlock request = null;
        HttpResponse response = null;
        try {
            request = addHeaders(new HttpUnlock(getUri(href), lockToken), headers);
            response = execute(request);
            request.checkSuccess(response);
        } catch (Exception e) {
            throw asOXException(e);
        } finally {
            HttpClients.close(request, response);
        }
    }

    /**
     * Parses the given {@link MultiStatus} into a list of {@link WebDAVResource}s
     *
     * @param multiStatus The {@link MultiStatus}
     * @param status The overall status
     * @return A list of {@link WebDAVResource}s
     */
    private static List<WebDAVResource> parseResources(MultiStatus multiStatus, int status) {
        List<WebDAVResource> resources = new ArrayList<WebDAVResource>();
        MultiStatusResponse[] responses = multiStatus.getResponses();
        for (MultiStatusResponse response : responses) {
            String href = response.getHref();
            DavPropertySet propertySet = response.getProperties(status);
            ParsedWebDAVResource resource = new ParsedWebDAVResource(href, propertySet);
            resources.add(resource);
        }
        return resources;
    }

    /**
     * Performs a put operation
     *
     * @param href The target
     * @param entity The {@link HttpEntity}
     * @param headers A map of header values
     * @throws WebDAVClientException
     */
    private void put(String href, HttpEntity entity, Map<String, String> headers) throws WebDAVClientException {
        HttpPut request = null;
        HttpResponse response = null;
        try {
            request = addHeaders(new HttpPut(getUri(href)), headers);
            request.setEntity(entity);
            response = execute(request);
            int status = response.getStatusLine().getStatusCode();
            if (status >= 200 && status <= 299) {
                return;
            }
            throw new DavException(status, response.getStatusLine().getReasonPhrase());
        } catch (Exception e) {
            throw asOXException(e);
        } finally {
            HttpClients.close(request, response);
        }
    }

    /**
     * Converts the href into a {@link URI}
     *
     * @param href The href to convert
     * @return The {@link URI}
     * @throws OXException
     */
    private URI getUri(String href) throws OXException {
        try {
            URIBuilder uriBuilder = new URIBuilder();
            if (null != baseUrl.getScheme()) {
                uriBuilder.setScheme(baseUrl.getScheme());
            }
            if (null != baseUrl.getHost()) {
                uriBuilder.setHost(baseUrl.getHost());
            }
            if (0 < baseUrl.getPort()) {
                uriBuilder.setPort(baseUrl.getPort());
            }
            if (Strings.isNotEmpty(href)) {
                uriBuilder.setPath(URLDecoder.decode(href, "UTF-8"));
            }
            return uriBuilder.build();
        } catch (URISyntaxException | UnsupportedEncodingException e) {
            throw WebDAVClientExceptionCodes.UNABLE_TO_PARSE_URI.create(baseUrl.toString() + href, e);
        }
    }

    // ------------------------------------------------ Static helpers ----------------------------------------------------------------------

    /**
     * Initializes the default client
     *
     * @param services The {@link ServiceLookup}
     * @param optClientId The optional http client id to use
     * @return The {@link ManagedHttpClient}
     * @throws OXException in case of errors
     */
    private static ManagedHttpClient initDefaultClient(ServiceLookup services, Optional<String> optClientId) throws OXException {
        return services.getServiceSafe(HttpClientService.class).getHttpClient(optClientId.orElse(HTTP_CLIENT_ID));
    }

    /**
     * Initializes the default {@link HttpContext}
     *
     * @param baseUrl The base url
     * @param login The login name
     * @param password The login password
     * @return The {@link HttpContext}
     */
    private static HttpContext initDefaultContext(URI baseUrl, String login, String password) {
        HttpHost targetHost = new HttpHost(baseUrl.getHost(), determinePort(baseUrl), baseUrl.getScheme());
        CredentialsProvider credsProvider = new BasicCredentialsProvider();
        credsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(login, password));

        AuthCache authCache = new BasicAuthCache();
        authCache.put(targetHost, new BasicScheme());

        // Add AuthCache to the execution context
        HttpClientContext context = HttpClientContext.create();
        context.setCredentialsProvider(credsProvider);
        context.setAuthCache(authCache);
        return context;
    }

    /**
     * Determines the port
     *
     * @param baseUrl The url
     * @return The port
     */
    private static int determinePort(URI baseUrl) {
        int port = baseUrl.getPort();
        if (port > 0) {
            return port;
        }
        return "https".equals(baseUrl.getScheme()) ? 443 : 80;
    }
}
