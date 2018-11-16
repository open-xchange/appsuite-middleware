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

package com.openexchange.dav;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.openexchange.ajax.Client;
import com.openexchange.clientinfo.ClientInfo;
import com.openexchange.clientinfo.ClientInfoProvider;
import com.openexchange.clientinfo.ClientInfoType;
import com.openexchange.java.Strings;
import com.openexchange.session.Session;
import com.openexchange.uadetector.UserAgentParser;
import net.sf.uadetector.ReadableUserAgent;

/**
 * {@link DAVClientInfoProvider}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class DAVClientInfoProvider implements ClientInfoProvider {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(DAVClientInfoProvider.class);

    private final LoadingCache<String, DAVClientInfo> clientInfoCache;

    private static final String MAC_CALENDAR = "macos_calendar";
    private static final String MAC_ADDRESSBOOK = "macos_addressbook";
    private static final String IOS_DAV = "ios_calendar/addressbook";
    private static final String THUNDERBIRD_LIGHTNING = "thunderbird_lightning";
    private static final String EMCLIENT = "emclient";
    private static final String EMCLIENT_APPSUITE = "emclient_appsuite";
    private static final String OX_SYNC = "oxsyncapp";
    private static final String CALDAV_SYNC = "caldav_sync";
    private static final String CARDDAV_SYNC = "carddav_sync";
    private static final String DAVDROID = "davdroid";
    private static final String WINDOWS_PHONE = "windows_phone";
    private static final String WINDOWS = "windows";
    private static final String GENERIC_CALDAV = "generic_caldav";
    private static final String GENERIC_CARDDAV = "generic_carddav";
    private static final String UNKNOWN = "unknown";

    /**
     * Initializes a new {@link DAVClientInfoProvider}.
     */
    public DAVClientInfoProvider(final UserAgentParser userAgentParser) {
        super();
        CacheLoader<String, DAVClientInfo> loader = new CacheLoader<String, DAVClientInfo>() {

            @Override
            public DAVClientInfo load(String sUserAgent) throws Exception {
                DAVUserAgent userAgent = DAVUserAgent.parse(sUserAgent);
                String clientFamily = getClientFamily(userAgent);
                ReadableUserAgent readableUserAgent = userAgentParser.parse(sUserAgent);
                if (UNKNOWN.equals(clientFamily) && (null == readableUserAgent || UNKNOWN.equalsIgnoreCase(readableUserAgent.getName()))) {
                    // Maybe iOS accountsd?
                    if (Strings.isNotEmpty(sUserAgent) && sUserAgent.contains("iOS") && sUserAgent.contains("accountsd")) {
                        return new DAVClientInfo(DAVUserAgent.IOS.getReadableName(), "ios", null, IOS_DAV, null, IOS_DAV, ClientInfoType.DAV);
                    }

                    // Unknown User-Agent
                    return new DAVClientInfo(userAgent.getReadableName(), clientFamily);
                }

                String osVersion = null;
                String osFamily = null;
                if (null != readableUserAgent.getOperatingSystem()) {
                    osFamily = readableUserAgent.getOperatingSystem().getFamilyName();
                    String osVersionMajor = readableUserAgent.getOperatingSystem().getVersionNumber().getMajor();
                    String osVersionMinor = readableUserAgent.getOperatingSystem().getVersionNumber().getMinor();
                    if (Strings.isNotEmpty(osVersionMajor)) {
                        if (Strings.isNotEmpty(osVersionMinor)) {
                            osVersion = new StringBuilder(osVersionMajor).append(".").append(osVersionMinor).toString();
                        } else {
                            osVersion = osVersionMajor;
                        }
                    }
                }
                if (Strings.isEmpty(osFamily) || UNKNOWN.equals(osFamily)) {
                    osFamily = getOSFamily(userAgent);
                }

                String clientVersion = null;
                String client = readableUserAgent.getName();
                if (EMCLIENT.equals(clientFamily)) {
                    client = "eM Client";
                }
                if (EMCLIENT_APPSUITE.equals(clientFamily)) {
                    client = "eM Client for OX App Suite";
                }
                String clientVersionMajor = readableUserAgent.getVersionNumber().getMajor();
                String clientVersionMinor = readableUserAgent.getVersionNumber().getMinor();
                if (Strings.isNotEmpty(clientVersionMajor)) {
                    if (Strings.isNotEmpty(clientVersionMinor)) {
                        clientVersion = new StringBuilder(clientVersionMajor).append('.').append(clientVersionMinor).toString();
                    } else {
                        clientVersion = clientVersionMajor;
                    }
                }
                if (userAgent.equals(DAVUserAgent.OX_SYNC) || userAgent.equals(DAVUserAgent.SMOOTH_SYNC)) {
                    return new DAVClientInfo(userAgent.getReadableName(), osFamily, osVersion, client, clientVersion, clientFamily, ClientInfoType.OXAPP);
                }
                if (UNKNOWN.equals(clientFamily)) {
                    //Maybe akonadi
                    if (Strings.isNotEmpty(sUserAgent) && sUserAgent.contains("akonadi")) {
                        return new DAVClientInfo("KDE/Plasma DAV Client", "linux", null, "akonadi", null, "akonadi", ClientInfoType.DAV);
                    }
                }
                return new DAVClientInfo(userAgent.getReadableName(), osFamily, osVersion, client, clientVersion, clientFamily);
            }
        };
        clientInfoCache = CacheBuilder.newBuilder().initialCapacity(128).maximumSize(65536).expireAfterAccess(2, TimeUnit.HOURS).build(loader);
    }

    @Override
    public ClientInfo getClientInfo(Session session) {
        if (null == session) {
            return null;
        }

        String sUserAgent = (String) session.getParameter(Session.PARAM_USER_AGENT);
        if (Strings.isEmpty(sUserAgent)) {
            // Unknown User-Agent
            return new DAVClientInfo(DAVUserAgent.UNKNOWN.getReadableName(), getClientFamily(DAVUserAgent.UNKNOWN));
        }

        try {
            DAVClientInfo davClientInfo = clientInfoCache.get(sUserAgent);
            return UNKNOWN.equals(davClientInfo.getClientFamily()) ? null : davClientInfo;
        } catch (ExecutionException e) {
            LOG.error("Failed to determine client info for User-Agent {}", sUserAgent, e.getCause());
            return new DAVClientInfo(DAVUserAgent.UNKNOWN.getReadableName(), getClientFamily(DAVUserAgent.UNKNOWN));
        }
    }

    @Override
    public ClientInfo getClientInfo(String clientId) {
        if (Strings.isEmpty(clientId)) {
            return null;
        }
        Client client = Client.getClientByID(clientId);
        if (Client.CALDAV.equals(client)) {
            return new DAVClientInfo(DAVUserAgent.GENERIC_CALDAV.getReadableName(), getClientFamily(DAVUserAgent.GENERIC_CALDAV));
        }
        if (Client.CARDDAV.equals(client)) {
            return new DAVClientInfo(DAVUserAgent.GENERIC_CARDDAV.getReadableName(), getClientFamily(DAVUserAgent.GENERIC_CARDDAV));
        }
        return null;
    }

    private String getClientFamily(DAVUserAgent userAgent) {
        switch (userAgent) {
            case MAC_CALENDAR:
                return MAC_CALENDAR;
            case MAC_CONTACTS:
                return MAC_ADDRESSBOOK;
            case IOS:
                return IOS_DAV;
            case THUNDERBIRD_LIGHTNING:
                return THUNDERBIRD_LIGHTNING;
            case EM_CLIENT:
                return EMCLIENT;
            case EM_CLIENT_FOR_APPSUITE:
                return EMCLIENT_APPSUITE;
            case OX_SYNC:
            case SMOOTH_SYNC:
                return OX_SYNC;
            case CALDAV_SYNC:
                return CALDAV_SYNC;
            case CARDDAV_SYNC:
                return CARDDAV_SYNC;
            case DAVDROID:
                return DAVDROID;
            case WINDOWS_PHONE:
                return WINDOWS_PHONE;
            case WINDOWS:
                return WINDOWS;
            case GENERIC_CALDAV:
                return GENERIC_CALDAV;
            case GENERIC_CARDDAV:
                return GENERIC_CARDDAV;
            default:
                return UNKNOWN;
        }
    }

    private String getOSFamily(DAVUserAgent userAgent) {
        switch (userAgent) {
            case MAC_CALENDAR:
            case MAC_CONTACTS:
                return "macos";
            case IOS:
                return "ios";
            case EM_CLIENT:
            case EM_CLIENT_FOR_APPSUITE:
            case WINDOWS:
                return "windows";
            case DAVDROID:
            case OX_SYNC:
            case CALDAV_SYNC:
            case CARDDAV_SYNC:
            case SMOOTH_SYNC:
                return "android";
            default:
                return "unknown";
        }
    }

}
