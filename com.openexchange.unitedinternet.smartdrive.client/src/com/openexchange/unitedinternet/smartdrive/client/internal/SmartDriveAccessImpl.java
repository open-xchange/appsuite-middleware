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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.unitedinternet.smartdrive.client.internal;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpConnectionManager;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import com.openexchange.unitedinternet.smartdrive.client.SmartDriveAccess;
import com.openexchange.unitedinternet.smartdrive.client.SmartDriveConstants;
import com.openexchange.unitedinternet.smartdrive.client.SmartDriveException;
import com.openexchange.unitedinternet.smartdrive.client.SmartDriveExceptionCodes;
import com.openexchange.unitedinternet.smartdrive.client.SmartDriveStatefulAccess;
import com.openexchange.unitedinternet.smartdrive.client.SmartDriveStatelessAccess;

/**
 * {@link SmartDriveAccessImpl}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SmartDriveAccessImpl implements SmartDriveAccess {

    /**
     * The default HTTP time out.
     */
    private static final int TIMEOUT = 60000;

    /**
     * The maximum number of connections to be used for a host configuration.
     */
    private static final int MAX_HOST_CONNECTIONS = 20;

    /**
     * The HTTPS identifier constant.
     */
    private static final String HTTPS = "https";

    /**
     * The HTTP identifier constant.
     */
    private static final String HTTP = "http";

    /**
     * The HTTP protocol constant.
     */
    private static final Protocol PROTOCOL_HTTP = Protocol.getProtocol("http");

    /*
     * Member stuff
     */

    private final String userName;

    private final URL url;

    private final HttpClient client;

    private volatile SmartDriveStatefulAccess statefulAccess;

    private volatile SmartDriveStatelessAccess statelessAccess;

    private String password;

    private boolean authenticated;

    /**
     * Initializes a new {@link SmartDriveAccessImpl}.
     * 
     * @param userName The name of the SmartDrive user
     * @param url The URL to SmartDrive server; e.g. <code>"http://www.smart-drive-server.com"</code>
     * @param configuration The HTTP client configuration; e.g. {@link SmartDriveConstants#CONFIG_TIMEOUT}
     * @throws SmartDriveException If initialization fails
     */
    public SmartDriveAccessImpl(final String userName, final String url, final Map<String, Object> configuration) throws SmartDriveException {
        super();
        this.userName = userName;
        this.password = (String) configuration.get(SmartDriveConstants.CONFIG_PASSWORD);
        /*
         * The URL to SmartDrive server
         */
        try {
            this.url = new URL(url);
        } catch (final MalformedURLException e) {
            throw SmartDriveExceptionCodes.INVALID_URL.create(e, url);
        }
        client = createNewHttpClient(this.url, configuration);
    }

    private void authenticate() throws SmartDriveException {
        if (authenticated) {
            return;
        }

        GetMethod getMethod = new GetMethod("");
        getMethod.setFollowRedirects(true);

        try {
            client.executeMethod(getMethod);
            String site = getMethod.getResponseBodyAsString();

            String action = extractAction(site);
            Map<String, String> hidden = extractHidden(site);

            hidden.put("username", userName);
            hidden.put("password", password);

            PostMethod postMethod = new PostMethod(action);

            for (Map.Entry<String, String> entry : hidden.entrySet()) {
                postMethod.setParameter(entry.getKey(), entry.getValue());
            }

            client.executeMethod(postMethod);

            Header locationHeader = postMethod.getResponseHeader("Location");
            if (locationHeader != null) {
                String redirectURLString = locationHeader.getValue();
                URL redirectURL = new URL(redirectURLString);

                client.getHostConfiguration().setHost(redirectURL.getHost());

                getMethod = new GetMethod(redirectURL.getPath() + "?" + redirectURL.getQuery());

                client.executeMethod(getMethod);
            } else {
                getMethod = new GetMethod("/");

                client.executeMethod(getMethod);
            }

            getMethod = new GetMethod("/userdata.js?username=" + userName);

            client.executeMethod(getMethod);

            String sessionId = extractSessionId(getMethod.getResponseBodyAsString());

            client.getParams().setParameter(HTTP_CLIENT_PARAM_SESSION_ID, sessionId);
            authenticated = true;
        } catch (HttpException e) {
            throw SmartDriveExceptionCodes.HTTP_ERROR.create(e, e.getMessage());
        } catch (IOException e) {
            throw SmartDriveExceptionCodes.IO_ERROR.create(e, e.getMessage());
        }

    }

    private static Pattern SESSIONID = Pattern.compile("webde.products.os.userdata.sessionid\\s+=\\s+(.+)");

    private static String extractSessionId(String site) {
        Matcher matcher = SESSIONID.matcher(site);
        if (matcher.find()) {
            String group = matcher.group(1);
            return group.substring(1, group.length() - 2);
        }
        return "";
    }

    private static Pattern INPUT = Pattern.compile("<input[^>]*?>");

    private static Pattern NAME = Pattern.compile("name=\"(.*?)\"");

    private static Pattern VALUE = Pattern.compile("value=\"(.*?)\"");

    private static Map<String, String> extractHidden(String site) {
        Matcher matcher = INPUT.matcher(site);
        Map<String, String> hidden = new HashMap<String, String>();

        while (matcher.find()) {
            String inputField = matcher.group(0);
            String name = "", value = "";

            Matcher nameMatcher = NAME.matcher(inputField);
            if (nameMatcher.find()) {
                name = nameMatcher.group(1);
            }

            Matcher valueMatcher = VALUE.matcher(inputField);
            if (valueMatcher.find()) {
                value = valueMatcher.group(1);
            }

            hidden.put(name, value);
        }

        return hidden;
    }

    private static Pattern ACTION = Pattern.compile("action=\"(.*?)\"");

    private static String extractAction(String site) {
        Matcher matcher = ACTION.matcher(site);
        if (matcher.find()) {
            return matcher.group(1);
        }

        return "GNITZ";
    }

    public SmartDriveStatefulAccess getStatefulAccess() throws SmartDriveException {
        SmartDriveStatefulAccess tmp = statefulAccess;
        if (null == tmp) {
            synchronized (this) {
                tmp = statefulAccess;
                if (null == tmp) {
                    authenticate();
                    statefulAccess = tmp = new SmartDriveStatefulAccessImpl(userName, url, client);
                }
            }
        }
        return tmp;
    }

    public SmartDriveStatelessAccess getStatelessAccess() throws SmartDriveException {
        SmartDriveStatelessAccess tmp = statelessAccess;
        if (null == tmp) {
            synchronized (this) {
                tmp = statelessAccess;
                if (null == tmp) {
                    statelessAccess = tmp = new SmartDriveStatelessAccessImpl(userName, url, client, this);
                }
            }
        }
        return tmp;
    }

    /**
     * Creates a new {@link HttpClient}.
     * 
     * @return The newly created {@link HttpClient}
     * @throws FileStorageException If creation fails
     */
    private static HttpClient createNewHttpClient(final URL url, final Map<String, Object> configuration) throws SmartDriveException {
        /*
         * Create host configuration or URI
         */
        final HostConfiguration hostConfiguration;
        {
            final String protocol = url.getProtocol();
            if (HTTPS.equalsIgnoreCase(protocol)) {
                int port = url.getPort();
                if (port == -1) {
                    port = 443;
                }
                hostConfiguration = new HostConfiguration();
                hostConfiguration.setHost(url.getHost(), port, new Protocol(HTTPS, ((ProtocolSocketFactory) new TrustAllAdapter()), port));
            } else if (HTTP.equalsIgnoreCase(protocol)) {
                int port = url.getPort();
                if (port == -1) {
                    port = 80;
                }
                hostConfiguration = new HostConfiguration();
                hostConfiguration.setHost(url.getHost(), port, PROTOCOL_HTTP);
            } else {
                throw SmartDriveExceptionCodes.UNSUPPORTED_PROTOCOL.create(protocol);
            }
        }
        /*
         * Define a HttpConnectionManager, which is also responsible for possible multi-threading support
         */
        final HttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
        final HttpConnectionManagerParams params = new HttpConnectionManagerParams();
        params.setMaxConnectionsPerHost(hostConfiguration, MAX_HOST_CONNECTIONS);
        connectionManager.setParams(params);
        /*
         * Create the HttpClient object and eventually pass the Credentials:
         */
        final HttpClient newClient = new HttpClient(connectionManager);
        int timeout = TIMEOUT;
        {
            final String sTimeout = (String) configuration.get(CONFIG_TIMEOUT);
            if (null != sTimeout) {
                try {
                    timeout = Integer.parseInt(sTimeout);
                } catch (final NumberFormatException e) {
                    org.apache.commons.logging.LogFactory.getLog(SmartDriveAccessImpl.class).warn(
                        "Configuration property \"" + CONFIG_TIMEOUT + "\" is not a number: " + sTimeout);
                    timeout = TIMEOUT;
                }
            }
        }
        newClient.getParams().setSoTimeout(timeout);
        newClient.getParams().setIntParameter("http.connection.timeout", timeout);
        newClient.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(0, false));
        newClient.setHostConfiguration(hostConfiguration);
        /*
         * Apply credentials
         */
        final String login = (String) configuration.get(CONFIG_LOGIN);
        final String password = (String) configuration.get(CONFIG_PASSWORD);
        if (null != login && null != password) {
            final Credentials creds = new UsernamePasswordCredentials(login, password);
            newClient.getParams().setAuthenticationPreemptive(true);
            newClient.getState().setCredentials(AuthScope.ANY, creds);
        }
        /*
         * Return
         */
        return newClient;
    }

}
