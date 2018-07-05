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

package com.openexchange.clientinfo.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.ImmutableMap;
import com.openexchange.ajax.Client;
import com.openexchange.clientinfo.ClientInfo;
import com.openexchange.clientinfo.ClientInfoProvider;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.serverconfig.ServerConfig;
import com.openexchange.serverconfig.ServerConfigService;
import com.openexchange.session.Session;
import com.openexchange.uadetector.UserAgentParser;
import net.sf.uadetector.OperatingSystem;
import net.sf.uadetector.ReadableUserAgent;
import net.sf.uadetector.UserAgentFamily;

/**
 * {@link WebClientInfoProvider}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.10.0
 */
public class WebClientInfoProvider implements ClientInfoProvider {

    private static final String CLIENT_APPSUITE = "OX App Suite";
    private static final String CLIENT_OX6 = "OX6 UI";

    private static final String OS_FAMILY_WINDOWS  = "windows";
    private static final String OS_FAMILY_MACOS = "macos";
    private static final String OS_FAMILY_LINUX = "linux";
    private static final String OS_FAMILY_ANDROID = "android";
    private static final String OS_FAMILY_IOS = "ios";

    private static final String BROWSER_CHROME = "chrome";
    private static final String BROWSER_SAFARI = "safari";
    private static final String BROWSER_FIREFOX = "firefox";
    private static final String BROWSER_EDGE = "egde";
    private static final String BROWSER_MSIE = "msie";
    private static final String BROWSER_OPERA = "opera";
    private static final String BROWSER_CHROMIUM = "chromium";
    private static final String BROWSER_UNKNOWN = "unknown";

    private final ServiceLookup services;
    private final Map<String, String> osMapping;
    private final Cache<Key, WebClientInfo> clientInfoCache;

    /**
     * Initializes a new {@link WebClientInfoProvider}.
     *
     * @param services The service look-up
     */
    public WebClientInfoProvider(ServiceLookup services) {
        super();
        this.services = services;
        Map<String, String> map = new HashMap<>();
        map.put("6.0", "Windows Vista");
        map.put("6.1", "Windows 7");
        map.put("6.2", "Windows 8");
        map.put("6.3", "Windows 8.1");
        map.put("10.0", "Windows 10");
        this.osMapping = ImmutableMap.copyOf(map);

        clientInfoCache = CacheBuilder.newBuilder().initialCapacity(128).maximumSize(65536).expireAfterAccess(2, TimeUnit.HOURS).build();
    }

    @Override
    public ClientInfo getClientInfo(Session session) {
        if (null != session) {
            // Get User-Agent from session
            String userAgent = (String) session.getParameter(Session.PARAM_USER_AGENT);
            if (Strings.isEmpty(userAgent)) {
                return getClientInfo(session.getClient());
            }

            // Acquire needed service
            UserAgentParser parser = services.getService(UserAgentParser.class);
            if (null == parser) {
                return getClientInfo(session.getClient());
            }

            // Determine client
            String client = "";
            if (Client.APPSUITE_UI.getClientId().equals(session.getClient())) {
                ServerConfigService serverConfigService = services.getService(ServerConfigService.class);
                if (null != serverConfigService) {
                    try {
                        String hostname = (String) session.getParameter(Session.PARAM_HOST_NAME);
                        ServerConfig config = serverConfigService.getServerConfig(hostname, session);
                        client = config.getProductName();
                    } catch (OXException e) {
                        client = CLIENT_APPSUITE;
                    }
                }
            } else if (Client.OX6_UI.getClientId().equals(session.getClient())) {
                client = CLIENT_OX6;
            }
            if (Strings.isEmpty(client)) {
                return null;
            }

            // Check if still/already present in cache
            Key key = new Key(userAgent, client);
            WebClientInfo webClientInfo = clientInfoCache.getIfPresent(key);
            if (null != webClientInfo) {
                return webClientInfo;
            }

            // ... otherwise determine client info
            ReadableUserAgent info = parser.parse(userAgent);
            OperatingSystem operatingSystem = info.getOperatingSystem();
            String browserFamily = getBrowserFamily(info.getFamily());
            String os = null;
            String osVersion = null;
            StringBuilder osReadableName = new StringBuilder();
            if (null != operatingSystem) {
                os = operatingSystem.getFamilyName();
                if (Strings.isNotEmpty(os)) {
                    os = os.toLowerCase();
                }
                if (Strings.isNotEmpty(os) && "os x".equals(os)) {
                    os = OS_FAMILY_MACOS;
                }
                String osVersionMajor = operatingSystem.getVersionNumber().getMajor();
                String osVersionMinor = operatingSystem.getVersionNumber().getMinor();
                switch (os) {
                    case OS_FAMILY_WINDOWS:
                        String mappedOs = osMapping.get(getVersionNumber(operatingSystem));
                        if (Strings.isNotEmpty(mappedOs)) {
                            osReadableName.append(mappedOs);
                        } else {
                            osReadableName.append("Windows").append(" ").append(osVersionMajor).append(".").append(osVersionMinor);
                        }
                        break;
                    case OS_FAMILY_MACOS:
                        try {
                            int major = Integer.parseInt(osVersionMajor);
                            int minor = Integer.parseInt(osVersionMinor);
                            if (major >= 10 && minor >= 12) {
                                osReadableName.append("macOS ").append(osVersionMajor).append(".").append(osVersionMinor);
                            }
                        } catch (NumberFormatException e) {
                            osReadableName.append("MacOS X");
                        }
                        break;
                    case OS_FAMILY_ANDROID:
                        osReadableName.append("Android ").append(osVersionMajor);
                        if (Strings.isNotEmpty(osVersionMinor)) {
                            osReadableName.append(".").append(osVersionMinor);
                        }
                        break;
                    case OS_FAMILY_LINUX:
                        osReadableName.append("Linux");
                        break;
                    case OS_FAMILY_IOS:
                        osReadableName.append("iOS ").append(osVersionMajor);
                        if (Strings.isNotEmpty(osVersionMinor)) {
                            osReadableName.append(".").append(osVersionMinor);
                        }
                        break;
                }
                if (Strings.isNotEmpty(osVersionMajor)) {
                    if (Strings.isNotEmpty(osVersionMinor)) {
                        osVersion = new StringBuilder(osVersionMajor).append(".").append(osVersionMinor).toString();
                    } else {
                        osVersion = osVersionMajor;
                    }
                }
            }

            String browser = info.getName();
            String browserVersion = info.getVersionNumber().getMajor();
            if ("Chrome".equals(browser)) {
                if (userAgent.contains("Edge")) { // MS Edge
                    browser = "Edge";
                    browserVersion = null;
                    browserFamily = BROWSER_EDGE;
                }
            }
            if ("Mozilla".equals(browser) || "IE".equals(browser)) {
                if (userAgent.contains("Trident/7.0; rv:11.0")) { //MSIE 11
                    browser = "Internet Explorer";
                    browserVersion = "11";
                    browserFamily = BROWSER_MSIE;
                }
            }

            webClientInfo = new WebClientInfo(client, osReadableName.toString(), os, osVersion, browser, browserVersion, browserFamily);
            clientInfoCache.put(key, webClientInfo);
            return webClientInfo;
        }

        return null;
    }

    @Override
    public ClientInfo getClientInfo(String clientId) {
        String client = "";
        if (Client.APPSUITE_UI.getClientId().equals(clientId)) {
            client = CLIENT_APPSUITE;
        } else if (Client.OX6_UI.getClientId().equals(clientId)) {
            client = CLIENT_OX6;
        } else {
            return null;
        }
        return new WebClientInfo(client, null, null, null, null, null, BROWSER_UNKNOWN);
    }

    private String getVersionNumber(OperatingSystem operatingSystem) {
        StringBuilder sb = new StringBuilder();
        sb.append(operatingSystem.getVersionNumber().getMajor()).append(".").append(operatingSystem.getVersionNumber().getMinor());
        return sb.toString();
    }

    private String getBrowserFamily(UserAgentFamily family) {
        switch (family) {
            case CHROME:
            case CHROME_MOBILE:
                return BROWSER_CHROME;
            case CHROMIUM:
                return BROWSER_CHROMIUM;
            case FIREFOX:
                return BROWSER_FIREFOX;
            case OPERA:
            case OPERA_MINI:
            case OPERA_MOBILE:
                return BROWSER_OPERA;
            case SAFARI:
            case SAFARI_RSS_READER:
                return BROWSER_SAFARI;
            default:
                return BROWSER_UNKNOWN;
        }
    }

    private static class Key {

        private final String client;
        private final String userAgent;

        Key(String userAgent, String client) {
            super();
            this.userAgent = userAgent;
            this.client = client;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((client == null) ? 0 : client.hashCode());
            result = prime * result + ((userAgent == null) ? 0 : userAgent.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Key other = (Key) obj;
            if (client == null) {
                if (other.client != null) {
                    return false;
                }
            } else if (!client.equals(other.client)) {
                return false;
            }
            if (userAgent == null) {
                if (other.userAgent != null) {
                    return false;
                }
            } else if (!userAgent.equals(other.userAgent)) {
                return false;
            }
            return true;
        }
    }

}
