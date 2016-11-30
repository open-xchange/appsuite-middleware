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

package com.openexchange.ajax.framework;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.mime.FormBodyPart;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.InputStreamBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie2;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.junit.Assert;
import org.xml.sax.SAXException;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.httpunit.cookies.CookieJar;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.AJAXRequest.FieldParameter;
import com.openexchange.ajax.framework.AJAXRequest.FileParameter;
import com.openexchange.ajax.framework.AJAXRequest.Method;
import com.openexchange.ajax.framework.AJAXRequest.Parameter;
import com.openexchange.configuration.AJAXConfig;
import com.openexchange.configuration.AJAXConfig.Property;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.tools.URLParameter;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * Executes the {@link AJAXRequest}s, processes the response as defined through {@link AbstractAJAXParser} and returns an
 * {@link AbstractAJAXResponse}.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class Executor extends Assert {

    private Executor() {
        super();
    }

    public static <T extends AbstractAJAXResponse> T execute(final AJAXClient client,
        final AJAXRequest<T> request) throws OXException, IOException,
        JSONException {
        return execute(client.getSession(), request);
    }

    public static <T extends AbstractAJAXResponse> T execute(final AJAXClient client, final AJAXRequest<T> request, final String protocol, final String hostname) throws OXException, IOException, JSONException {
        return execute(client.getSession(), request, protocol, hostname, getSleep());
    }

    public static <T extends AbstractAJAXResponse> T execute(final AJAXSession session,
        final AJAXRequest<T> request) throws OXException, IOException,
        JSONException {
        return execute(session, request,
            AJAXConfig.getProperty(Property.PROTOCOL),
            AJAXConfig.getProperty(Property.HOSTNAME), getSleep());
    }

    public static <T extends AbstractAJAXResponse> T execute(final AJAXSession session,
        final AJAXRequest<T> request, final String hostname) throws OXException,
        IOException, JSONException {
        return execute(session, request, AJAXConfig
            .getProperty(Property.PROTOCOL), hostname, getSleep());
    }

    public static <T extends AbstractAJAXResponse> T execute(final AJAXSession session, final AJAXRequest<T> request,
        final String protocol, final String hostname) throws OXException, IOException,
        JSONException {
        return execute(session, request, protocol, hostname, getSleep());
    }

    private static final AtomicLong COUNTER = new AtomicLong(1);

    public static <T extends AbstractAJAXResponse> T execute(final AJAXSession session, final AJAXRequest<T> request,
        final String protocol, final String hostname, final int sleep) throws OXException, IOException,
        JSONException {
        final String urlString;
        if (request instanceof PortAwareAjaxRequest) {
            urlString = protocol + "://" + hostname + ":" + ((PortAwareAjaxRequest<T>) request).getPort() + request.getServletPath();
        } else {
            urlString = protocol + "://" + hostname + request.getServletPath();
        }
        final HttpUriRequest httpRequest;
        final Method method = request.getMethod();
        switch (method) {
            case GET:
                httpRequest = new HttpGet(addQueryParamsToUri(urlString, getGETParameter(session, request)));
                break;
            case DELETE:
                httpRequest = new HttpDelete(addQueryParamsToUri(urlString, getGETParameter(session, request)));
                break;
            case POST:
                HttpEntity postEntity;
                String contentType = detectContentTypeHeader(request);
                if ("multipart/form-data".equals(contentType)) {
                    postEntity = buildMultipartEntity(request);
                } else {
                    postEntity = getBodyParameters(request);
                }

                HttpPost httpPost = new HttpPost(urlString + getURLParameter(session, request, true));
                httpPost.setEntity(postEntity);
                httpRequest = httpPost;
                break;
            case UPLOAD:
                final HttpPost httpUpload = new HttpPost(urlString + getURLParameter(session, request, false)); //TODO old request used to set "mimeEncoded" = true here
                addUPLOADParameter(httpUpload, request);
                httpRequest = httpUpload;
                break;
            case PUT:
                final HttpPut httpPut = new HttpPut(urlString + getURLParameter(session, request, false));
                Object body = request.getBody();
                if (null != body) {
                    final ByteArrayEntity entity = new ByteArrayEntity(createBodyBytes(body));
                    entity.setContentType("text/javascript; charset=UTF-8");
                    httpPut.setEntity(entity);
                }
                httpRequest = httpPut;
                break;
            default:
                throw AjaxExceptionCodes.IMVALID_PARAMETER.create(request.getMethod().name());
        }
        for (final Header header : request.getHeaders()) {
            if (method == Method.POST ) {
                if (!"Content-Type".equalsIgnoreCase(header.getName())) {
                    httpRequest.addHeader(header.getName(), header.getValue());
                }
            } else {
                httpRequest.addHeader(header.getName(), header.getValue());
            }
        }
        // Test echo header
        final String echoHeaderName = AJAXConfig.getProperty(AJAXConfig.Property.ECHO_HEADER, "");
        String echoValue = null;
        if (!isEmpty(echoHeaderName)) {
            echoValue = "pingMeBack-"+COUNTER.getAndIncrement();
            httpRequest.addHeader(echoHeaderName, echoValue);
        }

        final DefaultHttpClient httpClient = session.getHttpClient();

        final long startRequest = System.currentTimeMillis();
        final HttpResponse response = httpClient.execute(httpRequest);
        final long requestDuration = System.currentTimeMillis() - startRequest;
        if (null != echoValue) {
            final org.apache.http.Header header = response.getFirstHeader(echoHeaderName);
            if (null == header) {
                fail("Missing echo header: " + echoHeaderName);
            } else {
                assertEquals("Wrong echo header", echoValue, header.getValue());
            }
        }
        syncCookies(httpClient, session.getConversation());

        try {
            Thread.sleep(sleep);
        } catch (final InterruptedException e) {
            // Restore the interrupted status; see http://www.ibm.com/developerworks/java/library/j-jtp05236/index.html
            Thread.currentThread().interrupt();
            System.out.println("InterruptedException while sleeping between test requests. Does that help?");
            e.printStackTrace();
        } //emulating HttpUnit to avoid the Apache bug that mixes package up

        final AbstractAJAXParser<? extends T> parser = request.getParser();
        final String responseBody = parser.checkResponse(response, httpRequest);

        final long startParse = System.currentTimeMillis();
        final T retval = parser.parse(responseBody);
        final long parseDuration = System.currentTimeMillis() - startParse;

        retval.setRequestDuration(requestDuration);
        retval.setParseDuration(parseDuration);

        return retval;
    }

    private static String detectContentTypeHeader(AJAXRequest<?> request) {
        for (final Header header : request.getHeaders()) {
            if ("Content-Type".equalsIgnoreCase(header.getName())) {
                return header.getValue();
            }
        }

        return null;
    }

    private static boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = Character.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

    public static void syncCookies(final WebConversation conversation, final DefaultHttpClient httpClient, final String hostname) {
        // workaround for tests that prepend the protocol
        final String domain;
        if (hostname.startsWith("http://")) {
            domain = hostname.substring(7);
        } else if (hostname.startsWith("https://")) {
            domain = hostname.substring(8);
        } else {
            domain = hostname;
        }
        final String[] cookies = conversation.getCookieNames();
        final CookieStore cookieStore = httpClient.getCookieStore();
        final Set<String> storedNames = new HashSet<>();
        for (final Cookie cookie : cookieStore.getCookies()) {
            storedNames.add(cookie.getName());
        }
        for (final String name: cookies) {
            if (!storedNames.contains(name)) {
                final com.meterware.httpunit.cookies.Cookie cookie = conversation.getCookieDetails(name);
                final BasicClientCookie2 newCookie = new BasicClientCookie2(name, cookie.getValue());
                newCookie.setDomain(domain);
                cookieStore.addCookie(newCookie);
            }
        }
    }

    public static void syncCookies(final DefaultHttpClient httpClient, final WebConversation conversation) {
        final Set<String> storedNames = new HashSet<>();
        for (final String name : conversation.getCookieNames()) {
            storedNames.add(name);
        }
        final CookieStore cookieStore = httpClient.getCookieStore();
        final CookieJar cookieJar = conversation.getCookieJar();
        for (final Cookie cookie : cookieStore.getCookies()) {
            final String name = cookie.getName();
            if (!storedNames.contains(name)) {
                cookieJar.putCookie(name, cookie.getValue());
            }
        }
    }

    public static WebResponse execute4Download(final AJAXSession session, final AJAXRequest<?> request,
        final String protocol, final String hostname) throws OXException, IOException, JSONException, SAXException {
        final String urlString = protocol + "://" + hostname + request.getServletPath();
        final WebRequest req;
        switch (request.getMethod()) {
        case GET:
            final GetMethodWebRequest get = new GetMethodWebRequest(urlString);
            req = get;
            addURLParameter(get, session, request);
            break;
        case PUT:
            final PutMethodWebRequest put = new PutMethodWebRequest(addURLParameter(urlString, session, request), new ByteArrayInputStream(request.getBody().toString().getBytes("US-ASCII")), "text/javascript; charset=us-ascii");
            req = put;
            break;
        default:
            throw AjaxExceptionCodes.IMVALID_PARAMETER.create(request.getMethod().name());
        }
        final WebConversation conv = session.getConversation();
        final WebResponse resp;
        // The upload returns a web page that should not be interpreted.
        // final long startRequest = System.currentTimeMillis();
        resp = Method.GET == request.getMethod() ? conv.getResource(req) : conv.getResponse(req);
        //final long requestDuration = System.currentTimeMillis() - startRequest;
        return resp;
    }

    private static String addURLParameter(final String urlString, final AJAXSession session, final AJAXRequest<?> request) throws IOException, JSONException {
        final StringBuilder sb = new StringBuilder(urlString);
        boolean first = true;
        if (null != session.getId()) {
            sb.append('?').append(AJAXServlet.PARAMETER_SESSION).append('=').append(session.getId());
            first = false;
        }
        for (final Parameter param : request.getParameters()) {
            if (!(param instanceof FileParameter)) {
                if (first) {
                    sb.append('?');
                    first = false;
                } else {
                    sb.append('&');
                }
                sb.append(encode(param.getName())).append('=').append(encode(param.getValue()));
            }
        }
        return sb.toString();
    }

    private static String encode(final String s) throws UnsupportedEncodingException {
        return URLEncoder.encode(s, "ISO-8859-1");
    }

    private static void addURLParameter(final WebRequest req, final AJAXSession session, final AJAXRequest<?> request) throws IOException, JSONException {
        if (null != session.getId()) {
            req.setParameter(AJAXServlet.PARAMETER_SESSION, session.getId());
        }
        for (final Parameter param : request.getParameters()) {
            if (!(param instanceof FileParameter)) {
                req.setParameter(param.getName(), param.getValue());
            }
        }
    }

    /*************************************
     *** Rewrite for HttpClient: Start ***
     *************************************/

    private static String addQueryParamsToUri(String uri, final List<NameValuePair> queryParams){

        java.util.Collections.sort(queryParams, new Comparator<NameValuePair>(){
            @Override
            public int compare(final NameValuePair o1, final NameValuePair o2) {
                return (o1.getName().compareTo(o2.getName()));
            }}); //sorting the query params alphabetically

        if(uri.contains("?")) {
            uri += "&";
        } else {
            uri += "?";
        }
        return uri + URLEncodedUtils.format(queryParams, "UTF-8");
    }

    private static List<NameValuePair> getGETParameter(final AJAXSession session, final AJAXRequest<?> ajaxRequest) throws IOException, JSONException{ //new
        final List<NameValuePair> pairs = new LinkedList<>();

        if (session.getId() != null) {
            pairs.add( new BasicNameValuePair(AJAXServlet.PARAMETER_SESSION, session.getId()));
        }

        for (final Parameter param : ajaxRequest.getParameters()) {
            if (!(param instanceof FileParameter)) {
                pairs.add( new BasicNameValuePair(param.getName(), param.getValue()));
            }
        }

        return pairs;
    }

    private static void addUPLOADParameter(final HttpPost postMethod, final AJAXRequest<?> request) throws IOException, JSONException {
        final MultipartEntity parts = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);

        for (final Parameter param : request.getParameters()) {
            if (param instanceof FieldParameter) {

                final FieldParameter fparam = (FieldParameter) param;
                final StringBody body = new StringBody(fparam.getFieldContent(), Charset.forName("UTF-8"));
                parts.addPart(new FormBodyPart(fparam.getFieldName(), body));
            }
            if (param instanceof FileParameter) {
                final FileParameter fparam = (FileParameter) param;
                final InputStream is = fparam.getInputStream();
                InputStreamBody body;
                if(null != fparam.getMimeType() && !"".equals(fparam.getMimeType())) {
                    body = new InputStreamBody(is, fparam.getMimeType(), fparam.getValue());
                } else {
                    body = new InputStreamBody(is, fparam.getValue());
                }
                parts.addPart(new FormBodyPart(fparam.getName(), body));
            }
        }
        postMethod.setEntity(parts);

    }

    private static HttpEntity getBodyParameters(final AJAXRequest<?> request) throws IOException, JSONException {
        final List<NameValuePair> pairs = new LinkedList<>();

        for (final Parameter param : request.getParameters()) {
            if (param instanceof FieldParameter) {
                final FieldParameter fparam = (FieldParameter) param;
                pairs.add( new BasicNameValuePair(fparam.getFieldName(), fparam.getFieldContent()));
            }
        }

        return new UrlEncodedFormEntity(pairs);
    }

    private static HttpEntity buildMultipartEntity(AJAXRequest<?> request) throws IOException, JSONException {
        MultipartEntity entity = new MultipartEntity();
        for (final Parameter param : request.getParameters()) {
            if (param instanceof FileParameter) {
                entity.addPart(param.getName(), new InputStreamBody(
                      ((FileParameter) param).getInputStream(),
                      ((FileParameter) param).getMimeType(),
                      ((FileParameter) param).getFileName()));
            } else if (param instanceof FieldParameter) {
                entity.addPart(((FieldParameter)param).getFieldName(), new StringBody(((FieldParameter)param).getFieldContent(), Charset.forName("UTF-8")));
            }
        }

        return entity;
    }

    /*************************************
     *** Rewrite for HttpClient: End   ***
     *************************************/

    /**
     * @param strict <code>true</code> to only add URLParameters to the URL. This is needed for the POST request of the login method.
     * Unfortunately breaks this a lot of other tests.
     */
    private static String getURLParameter(final AJAXSession session, final AJAXRequest<?> request, final boolean strict) throws IOException, JSONException {
        final URLParameter parameter = new URLParameter();
        if (null != session.getId()) {
            parameter.setParameter(AJAXServlet.PARAMETER_SESSION, session.getId());
        }
        for (final Parameter param : request.getParameters()) {
            if (!strict && !(param instanceof FileParameter) && !(param instanceof FieldParameter)) {
                parameter.setParameter(param.getName(), param.getValue());
            }
            if (strict && param instanceof com.openexchange.ajax.framework.AJAXRequest.URLParameter) {
                parameter.setParameter(param.getName(), param.getValue());
            }
            // Don't throw error here because field and file parameters are added on POST with method addBodyParameter().
        }
        return parameter.getURLParameters();
    }

    private static InputStream createBody(final Object body) throws UnsupportedCharsetException {
        return new ByteArrayInputStream(body.toString().getBytes(Charsets.UTF_8));
    }

    private static byte[] createBodyBytes(final Object body) throws UnsupportedCharsetException {
        return body.toString().getBytes(Charsets.UTF_8);
    }

    private static int getSleep() {
        String sleepS;
        try {
            sleepS = AJAXConfig.getProperty(Property.SLEEP);
        } catch (final NullPointerException e) {
            sleepS = null;
        }
        int sleep;
        try {
            sleep = Integer.parseInt(sleepS);
        } catch (final NumberFormatException e) {
            sleep = 500;
        }
        return sleep;
    }
}
