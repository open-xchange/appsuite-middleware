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

package com.openexchange.drive.json.action;

import static com.openexchange.osgi.Tools.requireService;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Locale;
import javax.servlet.http.HttpServletRequest;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.capabilities.CapabilityService;
import com.openexchange.config.ConfigurationService;
import com.openexchange.dispatcher.DispatcherPrefixService;
import com.openexchange.drive.DriveClientVersion;
import com.openexchange.drive.DriveService;
import com.openexchange.drive.DriveSession;
import com.openexchange.drive.events.subscribe.DriveSubscriptionStore;
import com.openexchange.drive.json.DriveShareJSONParser;
import com.openexchange.drive.json.internal.DefaultDriveSession;
import com.openexchange.drive.json.internal.Services;
import com.openexchange.drive.json.json.DriveFieldMapper;
import com.openexchange.exception.OXException;
import com.openexchange.framework.request.RequestContextHolder;
import com.openexchange.groupware.notify.hostname.HostData;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.i18n.LocaleTools;
import com.openexchange.java.Strings;
import com.openexchange.share.ShareService;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.CountingHttpServletRequest;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractDriveAction}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public abstract class AbstractDriveAction implements AJAXActionService {

    private final DriveShareJSONParser parser;

    /**
     * Initializes a new {@link AbstractDriveShareAction}.
     */
    protected AbstractDriveAction() {
        super();
        this.parser = new DriveShareJSONParser();
    }

    /**
     * Gets the drive share parser.
     *
     * @return The parser
     */
    protected DriveShareJSONParser getShareParser() {
        return parser;
    }

    /**
     * Gets the drive service.
     *
     * @return The drive service
     * @throws OXException if the service is unavailable
     */
    protected DriveService getDriveService() throws OXException {
        return requireService(DriveService.class, Services.get());
    }

    /**
     * Gets the default share service.
     *
     * @return The share service
     * @throws OXException if the service is unavailable
     */
    protected ShareService getShareService() throws OXException {
        return requireService(ShareService.class, Services.get());
    }

    protected DriveSubscriptionStore getSubscriptionStore() throws OXException {
        return Services.getService(DriveSubscriptionStore.class, true);
    }

    protected ConfigurationService getConfigService() throws OXException {
        return Services.getService(ConfigurationService.class, true);
    }

    protected abstract AJAXRequestResult doPerform(AJAXRequestData requestData, DefaultDriveSession session) throws OXException;

    protected boolean requiresRootFolderID() {
        return true;
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        /*
         * check module permissions
         */
        {
            CapabilityService capabilityService = Services.getService(CapabilityService.class, true);
            if (false == capabilityService.getCapabilities(session).contains(DriveService.CAPABILITY_DRIVE)) {
                throw AjaxExceptionCodes.NO_PERMISSION_FOR_MODULE.create("drive");
            }
        }
        /*
         * Create drive session
         */
        String rootFolderID = requestData.getParameter("root");
        if (requiresRootFolderID() && Strings.isEmpty(rootFolderID)) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create("root");
        }
        int apiVersion = requestData.containsParameter("apiVersion") ? requestData.getParameter("apiVersion", Integer.class).intValue() : 0;
        DefaultDriveSession driveSession = new DefaultDriveSession(
            session, rootFolderID, extractHostData(requestData, session), apiVersion, extractClientVersion(requestData), extractLocale(requestData));
        /*
         * extract device name information if present
         */
        String device = requestData.getParameter("device");
        if (false == Strings.isEmpty(device)) {
            driveSession.setDeviceName(device);
        }
        /*
         * extract meta override parameter if present
         */
        if (requestData.containsParameter("driveMeta")) {
            driveSession.setDriveMeta(requestData.getParameter("driveMeta"));
        }
        /*
         * extract push token if present
         */
        String pushToken = requestData.getParameter("pushToken");
        if (false == Strings.isEmpty(pushToken)) {
            session.setParameter(DriveSession.PARAMETER_PUSH_TOKEN, pushToken);
        }
        /*
         * extract diagnostics parameter if present
         */
        String diagnostics = requestData.getParameter("diagnostics");
        if (false == Strings.isEmpty(diagnostics)) {
            driveSession.setDiagnostics(Boolean.valueOf(diagnostics));
        }
        /*
         * extract columns parameter to fields if present
         */
        String columnsValue = requestData.getParameter("columns");
        if (false == Strings.isEmpty(columnsValue)) {
            String[] splitted = Strings.splitByComma(columnsValue);
            int[] columnIDs = new int[splitted.length];
            for (int i = 0; i < splitted.length; i++) {
                try {
                    columnIDs[i] = Integer.parseInt(splitted[i]);
                } catch (NumberFormatException e) {
                    throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create("columns");
                }
            }
            driveSession.setFields(Arrays.asList(DriveFieldMapper.getInstance().getFields(columnIDs)));
        }
        /*
         * perform
         */
        return doPerform(requestData, driveSession);
    }

    private static DriveClientVersion extractClientVersion(AJAXRequestData requestData) throws OXException {
        String version = requestData.containsParameter("version") ? requestData.getParameter("version") : null;
        if (Strings.isEmpty(version)) {
            return DriveClientVersion.VERSION_0;
        }
        try {
            return new DriveClientVersion(version);
        } catch (IllegalArgumentException e) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(e, "version", version);
        }
    }

    /**
     * Extracts a possible set locale override from the supplied request data.
     *
     * @param requestData The request data
     * @return The locale, or <code>null</code> if not set
     */
    private static Locale extractLocale(AJAXRequestData requestData) {
        Locale localeOverride = null;
        if (requestData.containsParameter("locale")) {
            localeOverride = LocaleTools.getLocale(requestData.getParameter("locale"));
        }
        if (null == localeOverride && requestData.containsParameter("language")) {
            localeOverride = LocaleTools.getLocale(requestData.getParameter("language"));
        }
        return localeOverride;
    }

    /**
     * Extracts host data from the supplied request and session.
     *
     * @param requestData The AJAX request data
     * @param session The session
     * @return The extracted host data
     */
    private static HostData extractHostData(AJAXRequestData requestData, ServerSession session) {
        /*
         * get host data from reuest context or session parameter
         */
        com.openexchange.framework.request.RequestContext requestContext = RequestContextHolder.get();
        if (null != requestContext) {
            return requestContext.getHostData();
        }
        HostData hostData = (HostData) session.getParameter(HostnameService.PARAM_HOST_DATA);
        if (null != hostData) {
            return hostData;
        }
        /*
         * build up hostdata from request as fallback
         */
        final boolean secure = requestData.isSecure();
        final String httpSessionID = requestData.getRoute();
        final String route = Tools.extractRoute(httpSessionID);
        final int port = null != requestData.optHttpServletRequest() ? requestData.optHttpServletRequest().getServerPort() : -1;
        final String host = determineHost(requestData, session);
        final String prefix = Services.getService(DispatcherPrefixService.class).getPrefix();
        return new HostData() {

            @Override
            public String getHTTPSession() {
                return httpSessionID;
            }

            @Override
            public boolean isSecure() {
                return secure;
            }

            @Override
            public String getRoute() {
                return route;
            }

            @Override
            public int getPort() {
                return port;
            }

            @Override
            public String getHost() {
                return host;
            }

            @Override
            public String getDispatcherPrefix() {
                return prefix;
            }
        };
    }

    private static String determineHost(AJAXRequestData requestData, ServerSession session) {
        String hostName = null;
        /*
         * Ask hostname service if available
         */
        HostnameService hostnameService = Services.getOptionalService(HostnameService.class);
        if (null != hostnameService) {
            if (session.getUser().isGuest()) {
                hostName = hostnameService.getGuestHostname(session.getUserId(), session.getContextId());
            } else {
                hostName = hostnameService.getHostname(session.getUserId(), session.getContextId());
            }
        }
        /*
         * Get hostname from request
         */
        if (Strings.isEmpty(hostName)) {
            hostName = requestData.getHostname();
        }
        /*
         * Get hostname from java
         */
        if (Strings.isEmpty(hostName)) {
            try {
                hostName = InetAddress.getLocalHost().getCanonicalHostName();
            } catch (UnknownHostException e) {
                // ignore
            }
        }
        /*
         * Fall back to localhost as last resort
         */
        if (Strings.isEmpty(hostName)) {
            hostName = "localhost";
        }
        return hostName;
    }

    /**
     * Enables an unlimited body size by setting the maximum body size in the underlying {@link CountingHttpServletRequest} to
     * <code>-1</code>.
     *
     * @param requestData The AJAX request data
     */
    protected void enableUnlimitedBodySize(AJAXRequestData requestData) {
        HttpServletRequest servletRequest = requestData.optHttpServletRequest();
        if (servletRequest instanceof CountingHttpServletRequest) {
            ((CountingHttpServletRequest)servletRequest).setMax(-1);
        }
    }

    /**
     * Actively prevents additional image transformations by setting the <code>transformationNeeded</code> parameter to
     * <code>false</code> in the supplied request data reference.
     *
     * @param requestData The request data
     */
    protected void preventTransformations(AJAXRequestData requestData) {
        requestData.putParameter("transformationNeeded", String.valueOf(false));
    }

}
