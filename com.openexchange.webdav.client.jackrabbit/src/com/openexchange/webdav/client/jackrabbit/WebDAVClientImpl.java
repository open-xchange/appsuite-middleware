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
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.FileEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
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
import com.openexchange.rest.client.httpclient.HttpClients;
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

    private final CloseableHttpClient client;
    private final String baseUrl;

    /**
     * Initializes a new {@link WebDAVClientImpl}.
     *
     * @param client The underlying HTTP client to use
     * @param baseUrl The URL of the WebDAV host to connect to
     */
    public WebDAVClientImpl(CloseableHttpClient client, String baseUrl) {
        super();
        this.client = client;
        this.baseUrl = baseUrl;
    }

    /**
     * Initializes a new {@link WebDAVClientImpl}.
     *
     * @param baseUrl The URL of the WebDAV host to connect to
     * @param login The username to use for authentication
     * @param password The password to use for authentication
     */
    public WebDAVClientImpl(String baseUrl, String login, String password) {
        this(initDefaultClient(login, password), baseUrl);
    }

    public void close() {
        Streams.close(client);
    }

    @Override
    public List<WebDAVResource> propFind(String href, int depth, Set<QName> props, Map<String, String> headers) throws WebDAVClientException {
        HttpPropfind request = null;
        HttpResponse response = null;
        try {
            request = addHeaders(new HttpPropfind(getUri(href), getPropertyNameSet(props), depth), headers);
            response = client.execute(request);
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
            response = client.execute(request);
            Document doc = request.getResponseBodyAsDocument(response.getEntity());
            request.checkSuccess(response);
            return handler.apply(doc);
        } catch (Exception e) {
            throw asOXException(e);
        } finally {
            HttpClients.close(request, response);
        }
    }

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
            response = client.execute(request);
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
            response = client.execute(request);
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
            response = client.execute(request);
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
        CloseableHttpResponse response = null;
        try {
            request = addHeaders(new HttpGet(getUri(href)), headers);
            response = client.execute(request);
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
            response = client.execute(request);
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
            response = client.execute(request);
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
            response = client.execute(request);
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
            response = client.execute(request);
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
            response = client.execute(request);
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
            response = client.execute(request);
            request.checkSuccess(response);
        } catch (Exception e) {
            throw asOXException(e);
        } finally {
            HttpClients.close(request, response);
        }
    }

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


    private void put(String href, HttpEntity entity, Map<String, String> headers) throws WebDAVClientException {
        HttpPut request = null;
        HttpResponse response = null;
        try {
            request = addHeaders(new HttpPut(getUri(href)), headers);
            request.setEntity(entity);
            response = client.execute(request);
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

    private URI getUri(String href) {
        return URI.create(baseUrl + href);
    }

    private static CloseableHttpClient initDefaultClient(String login, String password) {
        HttpClients.ClientConfig config = HttpClients.ClientConfig.newInstance();
        config.setUserAgent("Open-Xchange WebDAV client");
        config.setCredentials(login, password);
        return HttpClients.getHttpClient(config);
    }

}
