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

package com.openexchange.chronos.provider.internal.config;

import static com.openexchange.chronos.provider.internal.Constants.ACCOUNT_ID;
import static com.openexchange.chronos.provider.internal.Constants.PROVIDER_ID;
import static com.openexchange.java.Autoboxing.I;
import static com.openexchange.osgi.Tools.requireService;
import java.util.Map;
import org.json.JSONObject;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.account.CalendarAccountService;
import com.openexchange.exception.OXException;
import com.openexchange.jslob.JSlobEntry;
import com.openexchange.jslob.JSlobKeys;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link ChronosJSlobEntry}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class ChronosJSlobEntry implements JSlobEntry {

    protected final ServiceLookup services;

    /**
     * Initializes a new {@link ChronosJSlobEntry}.
     *
     * @param services A service lookup reference
     */
    protected ChronosJSlobEntry(ServiceLookup services) {
        super();
        this.services = services;
    }

    protected abstract Object getValue(ServerSession session, JSONObject userConfig) throws OXException;

    protected abstract void setValue(ServerSession session, JSONObject userConfig, Object value) throws OXException;

    @Override
    public String getKey() {
        return JSlobKeys.CALENDAR;
    }

    @Override
    public Object getValue(Session session) throws OXException {
        CalendarAccount account = requireService(CalendarAccountService.class, services).getAccount(session, ACCOUNT_ID, null);
        if (null == account) {
            throw CalendarExceptionCodes.ACCOUNT_NOT_FOUND.create(I(ACCOUNT_ID));
        }
        ServerSession serverSession = ServerSessionAdapter.valueOf(session);
        if (false == isAvailable(serverSession)) {
            return JSONObject.NULL;
        }
        return getValue(serverSession, account.getUserConfiguration());
    }

    @Override
    public void setValue(Object value, Session session) throws OXException {
        CalendarAccountService accountService = requireService(CalendarAccountService.class, services);
        CalendarAccount account = accountService.getAccount(session, ACCOUNT_ID, null);
        if (null == account) {
            throw CalendarExceptionCodes.ACCOUNT_NOT_FOUND.create(I(ACCOUNT_ID));
        }
        ServerSession serverSession = ServerSessionAdapter.valueOf(session);
        if (false == isAvailable(serverSession)) {
            throw CalendarExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(PROVIDER_ID);
        }
        JSONObject userConfig = account.getUserConfiguration();
        setValue(serverSession, userConfig, value);
        accountService.updateAccount(session, ACCOUNT_ID, userConfig, account.getLastModified().getTime(), null);
    }

    @Override
    public Map<String, Object> metadata(Session session) throws OXException {
        return null;
    }

    private static boolean isAvailable(ServerSession session) throws OXException {
        if (session.isAnonymous() || session.getUser().isGuest() || false == session.getUserPermissionBits().hasCalendar()) {
            return false;
        }
        return true;
    }

}
