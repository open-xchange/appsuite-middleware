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

import java.util.Collections;
import java.util.List;
import com.openexchange.config.ConfigTools;
import com.openexchange.config.ConfigurationService;
import com.openexchange.http.grizzly.util.IPTools;
import com.openexchange.java.Strings;

/**
 * {@link GrizzlyConfig} Collects and exposes configuration parameters needed by GrizzlOX
 *
 * @author <a href="mailto:marc	.arens@open-xchange.com">Marc Arens</a>
 */
public class GrizzlyConfig {

    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(GrizzlyConfig.class);

    /**
     * Creates a new builder instance.
     *
     * @return The builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /** Create an appropriate instance of <code>GrizzlyConfig</code> */
    public static final class Builder {

        private String httpHost = "0.0.0.0";
        private int httpPort = 8009;
        private int httpsPort = 8010;
        private boolean isJMXEnabled = false;
        private boolean isWebsocketsEnabled = false;
        private boolean isCometEnabled = false;
        private int maxRequestParameters = 1000;
        private String backendRoute = "OX0";
        private boolean isAbsoluteRedirect = false;
        private boolean shutdownFast = false;
        private int awaitShutDownSeconds = 90;
        private int maxHttpHeaderSize = 8192;
        private boolean isSslEnabled = false;
        private String keystorePath = "";
        private String keystorePassword = "";
        private int cookieMaxAge = 604800;
        private int cookieMaxInactivityInterval = 1800;
        private boolean isForceHttps = false;
        private boolean isCookieHttpOnly = true;
        private String contentSecurityPolicy = null;
        private String defaultEncoding = "UTF-8";
        private boolean isConsiderXForwards = false;
        private List<String> knownProxies = Collections.emptyList();
        private String forHeader = "X-Forwarded-For";
        private String protocolHeader = "X-Forwarded-Proto";
        private String httpsProtoValue = "https";
        private int httpProtoPort = 80;
        private int httpsProtoPort = 443;
        private String echoHeader = "X-Echo-Header";
        private int maxBodySize = 104857600;
        private int maxNumberOfHttpSessions = 250000;
        private boolean isSessionAutologin = false;
        private List<String> enabledCiphers = null;
        private long wsTimeoutMillis;

        /**
         * Initializes a new {@link GrizzlyConfig.Builder}.
         */
        Builder() {
            super();
        }

        /**
         * (Re-)Initializes this builder using specified service.
         *
         * @param configService The service
         */
        public Builder initializeFrom(ConfigurationService configService) {
            // Grizzly properties
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
            // keep backwards compatibility with AJP configuration
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

            return this;
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

        public Builder setHttpHost(String httpHost) {
            this.httpHost = httpHost;
            return this;
        }

        public Builder setHttpPort(int httpPort) {
            this.httpPort = httpPort;
            return this;
        }

        public Builder setHttpsPort(int httpsPort) {
            this.httpsPort = httpsPort;
            return this;
        }

        public Builder setJMXEnabled(boolean isJMXEnabled) {
            this.isJMXEnabled = isJMXEnabled;
            return this;
        }

        public Builder setWebsocketsEnabled(boolean isWebsocketsEnabled) {
            this.isWebsocketsEnabled = isWebsocketsEnabled;
            return this;
        }

        public Builder setCometEnabled(boolean isCometEnabled) {
            this.isCometEnabled = isCometEnabled;
            return this;
        }

        public Builder setMaxRequestParameters(int maxRequestParameters) {
            this.maxRequestParameters = maxRequestParameters;
            return this;
        }

        public Builder setBackendRoute(String backendRoute) {
            this.backendRoute = backendRoute;
            return this;
        }

        public Builder setAbsoluteRedirect(boolean isAbsoluteRedirect) {
            this.isAbsoluteRedirect = isAbsoluteRedirect;
            return this;
        }

        public Builder setShutdownFast(boolean shutdownFast) {
            this.shutdownFast = shutdownFast;
            return this;
        }

        public Builder setAwaitShutDownSeconds(int awaitShutDownSeconds) {
            this.awaitShutDownSeconds = awaitShutDownSeconds;
            return this;
        }

        public Builder setMaxHttpHeaderSize(int maxHttpHeaderSize) {
            this.maxHttpHeaderSize = maxHttpHeaderSize;
            return this;
        }

        public Builder setSslEnabled(boolean isSslEnabled) {
            this.isSslEnabled = isSslEnabled;
            return this;
        }

        public Builder setKeystorePath(String keystorePath) {
            this.keystorePath = keystorePath;
            return this;
        }

        public Builder setKeystorePassword(String keystorePassword) {
            this.keystorePassword = keystorePassword;
            return this;
        }

        public Builder setCookieMaxAge(int cookieMaxAge) {
            this.cookieMaxAge = cookieMaxAge;
            return this;
        }

        public Builder setCookieMaxInactivityInterval(int cookieMaxInactivityInterval) {
            this.cookieMaxInactivityInterval = cookieMaxInactivityInterval;
            return this;
        }

        public Builder setForceHttps(boolean isForceHttps) {
            this.isForceHttps = isForceHttps;
            return this;
        }

        public Builder setCookieHttpOnly(boolean isCookieHttpOnly) {
            this.isCookieHttpOnly = isCookieHttpOnly;
            return this;
        }

        public Builder setContentSecurityPolicy(String contentSecurityPolicy) {
            this.contentSecurityPolicy = contentSecurityPolicy;
            return this;
        }

        public Builder setDefaultEncoding(String defaultEncoding) {
            this.defaultEncoding = defaultEncoding;
            return this;
        }

        public Builder setConsiderXForwards(boolean isConsiderXForwards) {
            this.isConsiderXForwards = isConsiderXForwards;
            return this;
        }

        public Builder setKnownProxies(List<String> knownProxies) {
            this.knownProxies = knownProxies;
            return this;
        }

        public Builder setForHeader(String forHeader) {
            this.forHeader = forHeader;
            return this;
        }

        public Builder setProtocolHeader(String protocolHeader) {
            this.protocolHeader = protocolHeader;
            return this;
        }

        public Builder setHttpsProtoValue(String httpsProtoValue) {
            this.httpsProtoValue = httpsProtoValue;
            return this;
        }

        public Builder setHttpProtoPort(int httpProtoPort) {
            this.httpProtoPort = httpProtoPort;
            return this;
        }

        public Builder setHttpsProtoPort(int httpsProtoPort) {
            this.httpsProtoPort = httpsProtoPort;
            return this;
        }

        public Builder setEchoHeader(String echoHeader) {
            this.echoHeader = echoHeader;
            return this;
        }

        public Builder setMaxBodySize(int maxBodySize) {
            this.maxBodySize = maxBodySize;
            return this;
        }

        public Builder setMaxNumberOfHttpSessions(int maxNumberOfHttpSessions) {
            this.maxNumberOfHttpSessions = maxNumberOfHttpSessions;
            return this;
        }

        public Builder setSessionAutologin(boolean isSessionAutologin) {
            this.isSessionAutologin = isSessionAutologin;
            return this;
        }

        public Builder setEnabledCiphers(List<String> enabledCiphers) {
            this.enabledCiphers = enabledCiphers;
            return this;
        }

        public Builder setWsTimeoutMillis(long wsTimeoutMillis) {
            this.wsTimeoutMillis = wsTimeoutMillis;
            return this;
        }

        public GrizzlyConfig build() {
            return new GrizzlyConfig(httpHost, httpPort, httpsPort, isJMXEnabled, isWebsocketsEnabled, isCometEnabled, maxRequestParameters, backendRoute, isAbsoluteRedirect, shutdownFast, awaitShutDownSeconds, maxHttpHeaderSize, isSslEnabled, keystorePath, keystorePassword, cookieMaxAge, cookieMaxInactivityInterval, isForceHttps, isCookieHttpOnly, contentSecurityPolicy, defaultEncoding, isConsiderXForwards, knownProxies, forHeader, protocolHeader, httpsProtoValue, httpProtoPort, httpsProtoPort, echoHeader, maxBodySize, maxNumberOfHttpSessions, isSessionAutologin, enabledCiphers, wsTimeoutMillis);
        }
    }

    // ----------------------------------------------------------------------------------------------------

    // Grizzly properties

    /** The host for the http network listener. Default value: 0.0.0.0, bind to every nic of your host. */
    private final String httpHost;

    /** The default port for the http network listener. */
    private final int httpPort;

    /** The default port for the https network listener. */
    private final int httpsPort;

    /** Enable grizzly monitoring via JMX? */
    private final boolean isJMXEnabled;

    /** Enable Bi-directional, full-duplex communications channels over a single TCP connection. */
    private final boolean isWebsocketsEnabled;

    /** Enable Technologies for pseudo realtime communication with the server */
    private final boolean isCometEnabled;

    /** The max. number of allowed request parameters */
    private final int maxRequestParameters;

    /** Unique backend route for every single backend behind the load balancer */
    private final String backendRoute;

    /** Do we want to send absolute or relative redirects */
    private final boolean isAbsoluteRedirect;

    /** Do we want a fast or a clean shut-down */
    private final boolean shutdownFast;

    /** The number of seconds to await the shut-down */
    private final int awaitShutDownSeconds;

    /** The maximum header size for an HTTP request in bytes. */
    private final int maxHttpHeaderSize;

    /** Enable SSL */
    private final boolean isSslEnabled;

    /** Path to keystore with X.509 certificates */
    private final String keystorePath;

    /** Keystore password */
    private final String keystorePassword;

    // server properties

    /** Maximal age of a cookie in seconds. A negative value destroys the cookie when the browser exits. A value of 0 deletes the cookie. */
    private final int cookieMaxAge;

    /** Interval between two client requests in seconds until the JSession is declared invalid */
    private final int cookieMaxInactivityInterval;

    /** Marks cookies as secure although the request is insecure e.g. when the backend is behind a ssl terminating proxy */
    private final boolean isForceHttps;

    /** Make the cookie accessible only via http methods. This prevents Javascript access to the cookie / cross site scripting */
    private final boolean isCookieHttpOnly;

    /** The the value for the <code>Content-Security-Policy</code> header<br>Please refer to <a href="http://www.html5rocks.com/en/tutorials/security/content-security-policy/">An Introduction to Content Security Policy</a>*/
    private final String contentSecurityPolicy;

    /** Default encoding for incoming Http Requests, this value must be equal to the web server's default encoding */
    private final String defaultEncoding;

    /** Do we want to consider X-Forward-* Headers */
    private final boolean isConsiderXForwards;

    /** A comma separated list of known proxies */
    private final List<String> knownProxies;
    /**
     * The name of the protocolHeader used to identify the originating IP address of a client connecting to a web server through an HTTP
     * proxy or load balancer
     */
    private final String forHeader;

    /** The name of the protocolHeader used to decide if we are dealing with a in-/secure Request */
    private final String protocolHeader;

    /** The value indicating secure http communication */
    private final String httpsProtoValue;

    /** The port used for http communication */
    private final int httpProtoPort;

    /** The port used for https communication */
    private final int httpsProtoPort;

    /** The name of the echo header whose value is echoed for each request providing that header, see mod_id for apache */
    private final String echoHeader;

    /** The maximum allowed size for PUT and POST bodies */
    private final int maxBodySize;

    /** The max. number of HTTP sessions */
    private final int maxNumberOfHttpSessions;

    // sessiond properties

    /** Is autologin enabled in the session.d properties? */
    private final boolean isSessionAutologin;

    private final List<String> enabledCiphers;

    /** The Web Socket timeout in milliseconds */
    private final long wsTimeoutMillis;

    GrizzlyConfig(String httpHost, int httpPort, int httpsPort, boolean isJMXEnabled, boolean isWebsocketsEnabled, boolean isCometEnabled, int maxRequestParameters, String backendRoute, boolean isAbsoluteRedirect, boolean shutdownFast, int awaitShutDownSeconds, int maxHttpHeaderSize, boolean isSslEnabled, String keystorePath, String keystorePassword, int cookieMaxAge, int cookieMaxInactivityInterval, boolean isForceHttps, boolean isCookieHttpOnly, String contentSecurityPolicy, String defaultEncoding, boolean isConsiderXForwards, List<String> knownProxies, String forHeader, String protocolHeader, String httpsProtoValue, int httpProtoPort, int httpsProtoPort, String echoHeader, int maxBodySize, int maxNumberOfHttpSessions, boolean isSessionAutologin, List<String> enabledCiphers, long wsTimeoutMillis) {
        super();
        this.httpHost = httpHost;
        this.httpPort = httpPort;
        this.httpsPort = httpsPort;
        this.isJMXEnabled = isJMXEnabled;
        this.isWebsocketsEnabled = isWebsocketsEnabled;
        this.isCometEnabled = isCometEnabled;
        this.maxRequestParameters = maxRequestParameters;
        this.backendRoute = backendRoute;
        this.isAbsoluteRedirect = isAbsoluteRedirect;
        this.shutdownFast = shutdownFast;
        this.awaitShutDownSeconds = awaitShutDownSeconds;
        this.maxHttpHeaderSize = maxHttpHeaderSize;
        this.isSslEnabled = isSslEnabled;
        this.keystorePath = keystorePath;
        this.keystorePassword = keystorePassword;
        this.cookieMaxAge = cookieMaxAge;
        this.cookieMaxInactivityInterval = cookieMaxInactivityInterval;
        this.isForceHttps = isForceHttps;
        this.isCookieHttpOnly = isCookieHttpOnly;
        this.contentSecurityPolicy = contentSecurityPolicy;
        this.defaultEncoding = defaultEncoding;
        this.isConsiderXForwards = isConsiderXForwards;
        this.knownProxies = knownProxies;
        this.forHeader = forHeader;
        this.protocolHeader = protocolHeader;
        this.httpsProtoValue = httpsProtoValue;
        this.httpProtoPort = httpProtoPort;
        this.httpsProtoPort = httpsProtoPort;
        this.echoHeader = echoHeader;
        this.maxBodySize = maxBodySize;
        this.maxNumberOfHttpSessions = maxNumberOfHttpSessions;
        this.isSessionAutologin = isSessionAutologin;
        this.enabledCiphers = enabledCiphers;
        this.wsTimeoutMillis = wsTimeoutMillis;
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
        return httpHost;
    }

    /**
     * Gets the httpPort
     *
     * @return The httpPort
     */
    public int getHttpPort() {
        return httpPort;
    }

    /**
     * Gets the httpsPort
     *
     * @return The httpsPort
     */
    public int getHttpsPort() {
        return httpsPort;
    }

    /**
     * Gets the hasJMXEnabled
     *
     * @return The hasJMXEnabled
     */
    public boolean isJMXEnabled() {
        return isJMXEnabled;
    }

    /**
     * Gets the hasWebsocketsEnabled
     *
     * @return The hasWebsocketsEnabled
     */
    public boolean isWebsocketsEnabled() {
        return isWebsocketsEnabled;
    }

    /**
     * Gets the hasCometEnabled
     *
     * @return The hasCometEnabled
     */
    public boolean isCometEnabled() {
        return isCometEnabled;
    }

    /**
     * Gets the maxRequestParameters
     *
     * @return The maxRequestParameters
     */
    public int getMaxRequestParameters() {
        return maxRequestParameters;
    }

    /**
     * Gets the backendRoute
     *
     * @return The backendRoute
     */
    public String getBackendRoute() {
        return backendRoute;
    }

    /**
     * Gets the cookieMaxAge
     *
     * @return The cookieMaxAge
     */
    public int getCookieMaxAge() {
        return cookieMaxAge;
    }

    /**
     * Gets the cookieMaxInactivityInterval in seconds
     *
     * @return The cookieMaxInactivityInterval in seconds
     */
    public int getCookieMaxInactivityInterval() {
        return cookieMaxInactivityInterval;
    }

    /**
     * Gets the isForceHttps
     *
     * @return The isForceHttps
     */
    public boolean isForceHttps() {
        return isForceHttps;
    }

    /**
     * Gets the isCookieHttpOnly
     *
     * @return The isCookieHttpOnly
     */
    public boolean isCookieHttpOnly() {
        return isCookieHttpOnly;
    }

    /**
     * Gets the <code>Content-Security-Policy</code> header.
     * <p>
     * Please refer to <a href="http://www.html5rocks.com/en/tutorials/security/content-security-policy/">An Introduction to Content Security Policy</a>
     *
     * @return The <code>Content-Security-Policy</code> header; default value is <code>null</code>/empty string.
     */
    public String getContentSecurityPolicy() {
        return contentSecurityPolicy;
    }

    /**
     * Gets the isSessionAutologin
     *
     * @return The isSessionAutologin
     */
    public boolean isSessionAutologin() {
        return isSessionAutologin;
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

}
