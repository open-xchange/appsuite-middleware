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

package com.openexchange.chronos.provider.xctx;

import java.util.EnumSet;
import java.util.Locale;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.CalendarCapability;
import com.openexchange.chronos.provider.folder.FolderCalendarAccess;
import com.openexchange.chronos.provider.folder.FolderCalendarProvider;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.CalendarService;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.share.core.tools.ShareLinks;
import com.openexchange.share.subscription.XctxSessionManager;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link XctxCalendarProvider}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.5
 */
public class XctxCalendarProvider implements FolderCalendarProvider {

    private static final Logger LOG = LoggerFactory.getLogger(XctxCalendarProvider.class);

    private final ServiceLookup services;

    /**
     * Initializes a new {@link XctxCalendarProvider}.
     *
     * @param services A service lookup reference
     */
    public XctxCalendarProvider(ServiceLookup services) {
        super();
        this.services = services;
    }

    @Override
    public String getId() {
        return Constants.PROVIDER_ID;
    }

    @Override
    public int getDefaultMaxAccounts() {
        return 20; // will use com.openexchange.calendar.xctx2.maxAccounts automatically during checks if set 
    }

    @Override
    public boolean isAvailable(Session session) {
        try {
            ServerSession serverSession = ServerSessionAdapter.valueOf(session);
            return false == serverSession.getUser().isGuest() && serverSession.getUserPermissionBits().hasGroupware();
        } catch (OXException e) {
            LOG.warn("Unexpected error while checking xctx2 calendar availability", e);
            return false;
        }
    }

    @Override
    public String getDisplayName(Locale locale) {
        return StringHelper.valueOf(locale).getString(XctxCalendarStrings.PROVIDER_NAME);
    }

    @Override
    public EnumSet<CalendarCapability> getCapabilities() {
        return CalendarCapability.getCapabilities(XctxCalendarAccess.class);
    }

    @Override
    public FolderCalendarAccess connect(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        Session guestSession = doGuestLogin(session, account.getUserConfiguration());
        CalendarSession calendarSession = services.getServiceSafe(CalendarService.class).init(guestSession, parameters);
        return new XctxCalendarAccess(services, account, session, calendarSession);
    }

    @Override
    public JSONObject configureAccount(Session session, JSONObject userConfig, CalendarParameters parameters) throws OXException {
        return reconfigureAccount(session, null, userConfig, parameters);
    }

    @Override
    public JSONObject reconfigureAccount(Session session, CalendarAccount calendarAccount, JSONObject userConfig, CalendarParameters parameters) throws OXException {
        /*
         * get & check necessary configuration properties
         */
        if (null == userConfig || false == userConfig.hasAndNotNull("url")) {
            throw CalendarExceptionCodes.INVALID_CONFIGURATION.create("url");
        }
        String url = userConfig.optString("url", null);
        if (null != calendarAccount && null != calendarAccount.getUserConfiguration() && false == url.equals(calendarAccount.getUserConfiguration().optString("url"))) {
            throw CalendarExceptionCodes.INVALID_CONFIGURATION.create("url");
        }
        String password = userConfig.optString("password", null);
        if (Strings.isNotEmpty(password)) {
            //TODO: implement secret handling
            throw CalendarExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(getId());
        }
        /*
         * implicitly check access by logging in as guest user
         */
        doGuestLogin(session, url, password);
        /*
         * extract & return slipstreamed internal config if set, otherwise use empty default
         */
        Object internalConfig = userConfig.remove("internalConfig");
        if (null != internalConfig && JSONObject.class.isInstance(internalConfig)) {
            return (JSONObject) internalConfig;
        }
        return new JSONObject();
    }

    @Override
    public void onAccountCreated(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        // no
    }

    @Override
    public void onAccountUpdated(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        // no
    }

    @Override
    public void onAccountDeleted(Session session, CalendarAccount account, CalendarParameters parameters) throws OXException {
        // no
    }

    @Override
    public void onAccountDeleted(Context context, CalendarAccount account, CalendarParameters parameters) throws OXException {
        // no
    }

    private Session doGuestLogin(Session localSession, JSONObject userConfig) throws OXException {
        if (null == userConfig || false == userConfig.hasAndNotNull("url")) {
            throw CalendarExceptionCodes.INVALID_CONFIGURATION.create("url");
        }
        String shareUrl = userConfig.optString("url", null);
        String password = userConfig.optString("password", null);
        return doGuestLogin(localSession, shareUrl, password);
    }

    private Session doGuestLogin(Session localSession, String shareUrl, String password) throws OXException {
        String baseToken = ShareLinks.extractBaseToken(shareUrl);
        return services.getServiceSafe(XctxSessionManager.class).getGuestSession(localSession, baseToken, password);
    }

}
