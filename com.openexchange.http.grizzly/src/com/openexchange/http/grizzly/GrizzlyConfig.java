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

package com.openexchange.http.grizzly;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import com.openexchange.config.ConfigTools;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.DefaultInterests;
import com.openexchange.config.Interests;
import com.openexchange.config.Reloadable;
import com.openexchange.exception.OXException;
import com.openexchange.http.grizzly.osgi.Services;
import com.openexchange.http.grizzly.util.IPTools;
import com.openexchange.java.Strings;
import com.openexchange.server.Initialization;

/**
 * {@link GrizzlyConfig} Collects and exposes configuration parameters needed by GrizzlOX
 *
 * @author <a href="mailto:marc	.arens@open-xchange.com">Marc Arens</a>
 */
public class GrizzlyConfig implements Initialization, Reloadable {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(GrizzlyConfig.class);

    private static final GrizzlyConfig instance = new GrizzlyConfig();

    private final PropertyChangeSupport changes = new PropertyChangeSupport(this);

    public static GrizzlyConfig getInstance() {
        return instance;
    }

    private final AtomicBoolean started = new AtomicBoolean();

    // grizzly properties

    /** The host for the http network listener. Default value: 0.0.0.0, bind to every nic of your host. */
    private String httpHost = "0.0.0.0";

    /** The default port for the http network listener. */
    private int httpPort = 8009;

    /** The default port for the https network listener. */
    private int httpsPort = 8010;

    /** Enable grizzly monitoring via JMX? */
    private boolean isJMXEnabled = false;

    /** Enable Bi-directional, full-duplex communications channels over a single TCP connection. */
    private boolean isWebsocketsEnabled = false;

    /** Enable Technologies for pseudo realtime communication with the server */
    private boolean isCometEnabled = false;

    /** The max. number of allowed request parameters */
    private int maxRequestParameters = 1000;

    /** Unique backend route for every single backend behind the load balancer */
    private String backendRoute = "OX0";

    /** Do we want to send absolute or relative redirects */
    private boolean isAbsoluteRedirect = false;

    /** Do we want a fast or a clean shut-down */
    private boolean shutdownFast = false;

    /** The number of seconds to await the shut-down */
    private int awaitShutDownSeconds = 90;

    /** The maximum header size for an HTTP request in bytes. */
    private int maxHttpHeaderSize = 8192;

    /** Enable SSL */
    private boolean isSslEnabled = false;

    /** Path to keystore with X.509 certificates */
    private String keystorePath = "";

    /** Keystore password */
    private String keystorePassword = "";

    // server properties

    /** Maximal age of a cookie in seconds. A negative value destroys the cookie when the browser exits. A value of 0 deletes the cookie. */
    private int cookieMaxAge = 604800;

    /** Interval between two client requests in seconds until the JSession is declared invalid */
    private int cookieMaxInactivityInterval = 1800;

    /** Marks cookies as secure although the request is insecure e.g. when the backend is behind a ssl terminating proxy */
    private boolean isForceHttps = false;

    /** Make the cookie accessible only via http methods. This prevents Javascript access to the cookie / cross site scripting */
    private boolean isCookieHttpOnly = true;

    /** The the value for the <code>Content-Security-Policy</code> header<br>Please refer to <a href="http://www.html5rocks.com/en/tutorials/security/content-security-policy/">An Introduction to Content Security Policy</a>*/
    private String contentSecurityPolicy = null;

    /** Default encoding for incoming Http Requests, this value must be equal to the web server's default encoding */
    private String defaultEncoding = "UTF-8";

    /** Do we want to consider X-Forward-* Headers */
    private boolean isConsiderXForwards = false;

    /** A comma separated list of known proxies */
    private List<String> knownProxies = Collections.emptyList();
    /**
     * The name of the protocolHeader used to identify the originating IP address of a client connecting to a web server through an HTTP
     * proxy or load balancer
     */
    private String forHeader = "X-Forwarded-For";

    /** The name of the protocolHeader used to decide if we are dealing with a in-/secure Request */
    private String protocolHeader = "X-Forwarded-Proto";

    /** The value indicating secure http communication */
    private String httpsProtoValue = "https";

    /** The port used for http communication */
    private int httpProtoPort = 80;

    /** The port used for https communication */
    private int httpsProtoPort = 443;

    /** The name of the echo header whose value is echoed for each request providing that header, see mod_id for apache */
    private String echoHeader = "X-Echo-Header";

    /** The maximum allowed size for PUT and POST bodies */
    private int maxBodySize = 104857600;

    /** The max. number of HTTP sessions */
    private int maxNumberOfHttpSessions = 250000;

    // sessiond properties

    /** Is autologin enabled in the session.d properties? */
    private boolean isSessionAutologin = false;

    private List<String> enabledCiphers = null;

    /** The Web Socket timeout in milliseconds */
    private long wsTimeoutMillis;


    @Override
    public void start() throws OXException {
        if (!started.compareAndSet(false, true)) {
            LOG.error("{} already started", this.getClass().getName());
            return;
        }
        init();
    }

    @Override
    public void stop() {
        if (!started.compareAndSet(true, false)) {
            LOG.error("{} cannot be stopped since it has no been started before", this.getClass().getName());
            return;
        }
    }

    private void init() throws OXException {
        ConfigurationService configService = Services.getService(ConfigurationService.class);
        if (configService == null) {
            throw GrizzlyExceptionCode.NEEDED_SERVICE_MISSING.create(ConfigurationService.class.getSimpleName());
        }

        // grizzly properties
        this.isJMXEnabled = configService.getBoolProperty("com.openexchange.http.grizzly.hasJMXEnabled", false);
        this.isWebsocketsEnabled = configService.getBoolProperty("com.openexchange.http.grizzly.hasWebSocketsEnabled", false);
        this.isCometEnabled = configService.getBoolProperty("com.openexchange.http.grizzly.hasCometEnabled", false);
        this.isAbsoluteRedirect = configService.getBoolProperty("com.openexchange.http.grizzly.doAbsoluteRedirect", false);
        this.maxHttpHeaderSize = configService.getIntProperty("com.openexchange.http.grizzly.maxHttpHeaderSize", 8192);
        this.wsTimeoutMillis = configService.getIntProperty("com.openexchange.http.grizzly.wsTimeoutMillis", 900000);
        this.isSslEnabled = configService.getBoolProperty("com.openexchange.http.grizzly.hasSSLEnabled", false);
        this.keystorePath = configService.getProperty("com.openexchange.http.grizzly.keystorePath", "");
        this.keystorePassword = configService.getProperty("com.openexchange.http.grizzly.keystorePassword", "");


        // server properties
        this.cookieMaxAge = Integer.valueOf(ConfigTools.parseTimespanSecs(configService.getProperty("com.openexchange.cookie.ttl", "1W"))).intValue();
        this.cookieMaxInactivityInterval = configService.getIntProperty("com.openexchange.servlet.maxInactiveInterval", 1800);
        this.isForceHttps = configService.getBoolProperty("com.openexchange.forceHTTPS", false);
        this.isCookieHttpOnly = configService.getBoolProperty("com.openexchange.cookie.httpOnly", true);
        {
            String csp = configService.getProperty("com.openexchange.servlet.contentSecurityPolicy", "").trim();
            csp = Strings.unquote(csp);
            this.contentSecurityPolicy = csp.trim();
        }
        this.defaultEncoding = configService.getProperty("DefaultEncoding", "UTF-8");
        this.isConsiderXForwards = configService.getBoolProperty("com.openexchange.server.considerXForwards", false);
        String proxyCandidates = configService.getProperty("com.openexchange.server.knownProxies", "");
        setKnownProxies(proxyCandidates);
        this.forHeader = configService.getProperty("com.openexchange.server.forHeader", "X-Forwarded-For");
        this.protocolHeader = configService.getProperty("com.openexchange.server.protocolHeader", "X-Forwarded-Proto");
        this.httpsProtoValue = configService.getProperty("com.openexchange.server.httpsProtoValue", "https");
        this.httpProtoPort = configService.getIntProperty("com.openexchange.server.httpProtoPort", 80);
        this.httpsProtoPort = configService.getIntProperty("com.openexchange.server.httpsProtoPort", 443);
        final int configuredMaxBodySize = configService.getIntProperty("com.openexchange.servlet.maxBodySize", 104857600);
        this.maxBodySize = configuredMaxBodySize <= 0 ? Integer.MAX_VALUE : configuredMaxBodySize;
        final int configuredMaxNumberOfHttpSessions = configService.getIntProperty("com.openexchange.servlet.maxActiveSessions", 250000);
        this.maxNumberOfHttpSessions = configuredMaxNumberOfHttpSessions <= 0 ? 0 : configuredMaxNumberOfHttpSessions;
        this.shutdownFast = configService.getBoolProperty("com.openexchange.connector.shutdownFast", false);
        this.awaitShutDownSeconds = configService.getIntProperty("com.openexchange.connector.awaitShutDownSeconds", 90);

        this.httpHost = configService.getProperty("com.openexchange.connector.networkListenerHost", "127.0.0.1");
        // keep backwards compatibility with ajp config
        if(httpHost.equals("*")) {
            this.httpHost="0.0.0.0";
        }
        this.httpPort = configService.getIntProperty("com.openexchange.connector.networkListenerPort", 8009);
        this.httpsPort = configService.getIntProperty("com.openexchange.connector.networkSslListenerPort", 8010);
        this.maxRequestParameters = configService.getIntProperty("com.openexchange.connector.maxRequestParameters", 1000);
        this.backendRoute = configService.getProperty("com.openexchange.server.backendRoute", "OX0");
        this.echoHeader = configService.getProperty("com.openexchange.servlet.echoHeaderName","X-Echo-Header");

        // sessiond properties
        this.isSessionAutologin = configService.getBoolProperty("com.openexchange.sessiond.autologin", false);

        this.enabledCiphers = configService.getProperty("com.openexchange.http.grizzly.enabledCipherSuites", "", ",");

    }

    /**
     * Gets the started
     *
     * @return The started
     */
    public AtomicBoolean getStarted() {
        return started;
    }

    /**
     * Gets the defaultEncoding used for incoming http requests
     *
     * @return The defaultEncoding
     */
    public String getDefaultEncoding() {
        return defaultEncoding;
    }

    /**
     * Gets the httpHost
     *
     * @return The httpHost
     */
    public String getHttpHost() {
        return instance.httpHost;
    }

    /**
     * Gets the httpPort
     *
     * @return The httpPort
     */
    public int getHttpPort() {
        return instance.httpPort;
    }

    /**
     * Gets the httpsPort
     *
     * @return The httpsPort
     */
    public int getHttpsPort() {
        return instance.httpsPort;
    }

    /**
     * Gets the hasJMXEnabled
     *
     * @return The hasJMXEnabled
     */
    public boolean isJMXEnabled() {
        return instance.isJMXEnabled;
    }

    /**
     * Gets the hasWebsocketsEnabled
     *
     * @return The hasWebsocketsEnabled
     */
    public boolean isWebsocketsEnabled() {
        return instance.isWebsocketsEnabled;
    }

    /**
     * Gets the hasCometEnabled
     *
     * @return The hasCometEnabled
     */
    public boolean isCometEnabled() {
        return instance.isCometEnabled;
    }

    /**
     * Gets the maxRequestParameters
     *
     * @return The maxRequestParameters
     */
    public int getMaxRequestParameters() {
        return instance.maxRequestParameters;
    }

    /**
     * Gets the backendRoute
     *
     * @return The backendRoute
     */
    public String getBackendRoute() {
        return instance.backendRoute;
    }

    /**
     * Gets the cookieMaxAge
     *
     * @return The cookieMaxAge
     */
    public int getCookieMaxAge() {
        return instance.cookieMaxAge;
    }

    /**
     * Gets the cookieMaxInactivityInterval
     *
     * @return The cookieMaxInactivityInterval
     */
    public int getCookieMaxInactivityInterval() {
        return instance.cookieMaxInactivityInterval;
    }

    /**
     * Gets the isForceHttps
     *
     * @return The isForceHttps
     */
    public boolean isForceHttps() {
        return instance.isForceHttps;
    }

    /**
     * Gets the isCookieHttpOnly
     *
     * @return The isCookieHttpOnly
     */
    public boolean isCookieHttpOnly() {
        return instance.isCookieHttpOnly;
    }

    /**
     * Gets the <code>Content-Security-Policy</code> header.
     * <p>
     * Please refer to <a href="http://www.html5rocks.com/en/tutorials/security/content-security-policy/">An Introduction to Content Security Policy</a>
     *
     * @return The <code>Content-Security-Policy</code> header; default value is <code>null</code>/empty string.
     */
    public String getContentSecurityPolicy() {
        return instance.contentSecurityPolicy;
    }

    /**
     * Gets the isSessionAutologin
     *
     * @return The isSessionAutologin
     */
    public boolean isSessionAutologin() {
        return instance.isSessionAutologin;
    }


    private void setKnownProxies(String ipList) {
        if(ipList.isEmpty()) {
            this.knownProxies = Collections.emptyList();
        } else {
            List<String> proxyCandidates = IPTools.splitAndTrim(ipList, IPTools.COMMA_SEPARATOR);
            List<String> erroneousIPs = IPTools.filterErroneousIPs(proxyCandidates);
            if(!erroneousIPs.isEmpty()) {
                LOG.warn("Falling back to empty list as com.openexchange.server.knownProxies contains malformed IPs: {}", erroneousIPs);
            } else {
                this.knownProxies = proxyCandidates;
            }
        }
    }
    /**
     * Returns the known proxies as comma separated list of IPs
     * @return the known proxies as comma separated list of IPs or an empty String
     */
    public List<String> getKnownProxies() {
        return knownProxies;
    }

    /**
     * Gets the name of forward for header
     * @return the forwardHeader
     */
    public String getForHeader() {
        return forHeader;
    }

    /**
     * Gets the protocolHeader
     *
     * @return The protocolHeader
     */
    public String getProtocolHeader() {
        return protocolHeader;
    }


    /**
     * Gets the httpsProtoValue
     *
     * @return The httpsProtoValue
     */
    public String getHttpsProtoValue() {
        return httpsProtoValue;
    }


    /**
     * Gets the httpProtoPort
     *
     * @return The httpProtoPort
     */
    public int getHttpProtoPort() {
        return httpProtoPort;
    }


    /**
     * Gets the httpsProtoPort
     *
     * @return The httpsProtoPort
     */
    public int getHttpsProtoPort() {
        return httpsProtoPort;
    }


    /**
     * Gets the isAbsoluteRedirect
     *
     * @return The isAbsoluteRedirect
     */
    public boolean isAbsoluteRedirect() {
        return isAbsoluteRedirect;
    }

    /**
     * Gets the shutdown-fast flag
     *
     * @return The shutdown-fast flag
     */
    public boolean isShutdownFast() {
        return shutdownFast;
    }

    /**
     * Gets the awaitShutDownSeconds
     *
     * @return The awaitShutDownSeconds
     */
    public int getAwaitShutDownSeconds() {
        return awaitShutDownSeconds;
    }

    /**
     * Gets if we should consider X-Forward-Headers that reach the backend.
     * Those can be spoofed by clients so we have to make sure to consider the headers only if the proxy/proxies reliably override those
     * headers for incoming requests.
     * Disabled by default as we now use relative redirects for Grizzly.
     * @return
     */
    public boolean isConsiderXForwards() {
        return isConsiderXForwards;
    }

    /**
     * Get the name of the echo header whose value is echoed for each request providing that header when using KippData's mod_id.
     * @return The name of the echo header whose value is echoed for each request providing that header.
     */
    public String getEchoHeader() {
        return this.echoHeader;
    }

    /** Get the maximum allowed size for PUT and POST bodies */
    public int getMaxBodySize() {
        return maxBodySize;
    }

    /**
     * Gets the maximum number of active sessions
     *
     * @return The maximum number of active sessions
     */
    public int getMaxNumberOfHttpSessions() {
        return maxNumberOfHttpSessions;
    }

    /**
     * Get the maximum header size for an HTTP request in bytes.
     *
     * @return the maximum header size for an HTTP request in bytes.
     */
    public int getMaxHttpHeaderSize() {
        return maxHttpHeaderSize;
    }

    /**
     * Gets the Web Socket timeout in milliseconds
     *
     * @return The timeout
     */
    public long getWsTimeoutMillis() {
        return wsTimeoutMillis;
    }

    public boolean isSslEnabled() {
        return isSslEnabled;
    }

    public String getKeystorePath() {
        return keystorePath;
    }

    public String getKeystorePassword() {
        return keystorePassword;
    }

    public List<String> getEnabledCiphers() {
        return enabledCiphers;
    }

    @Override
    public void reloadConfiguration(ConfigurationService configService) {
        String proxies = configService.getProperty("com.openexchange.server.knownProxies");
        List<String> oldProxies = knownProxies;
        setKnownProxies(proxies);
        changes.firePropertyChange("knownProxies", oldProxies, proxies);
    }

    @Override
    public Interests getInterests() {
        return DefaultInterests.builder().propertiesOfInterest("com.openexchange.server.knownProxies").build();
    }

    public void addPropertyListener(PropertyChangeListener listener) {
        changes.addPropertyChangeListener(listener);
    }

    public void removePropertyListener(PropertyChangeListener listener) {
        changes.removePropertyChangeListener(listener);
    }

}
