/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.chronos.provider.xctx;

import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.Attendee;
import com.openexchange.chronos.common.CalendarUtils;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.FreeBusyProvider;
import com.openexchange.chronos.provider.account.CalendarAccountService;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.chronos.service.FreeBusyResult;
import com.openexchange.config.lean.DefaultProperty;
import com.openexchange.config.lean.LeanConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.java.CallerRunsCompletionService;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.share.core.tools.ShareToken;
import com.openexchange.share.core.tools.ShareTool;
import com.openexchange.threadpool.ThreadPoolCompletionService;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.userconf.UserPermissionService;

/**
 * {@link XctxFreeBusyProvider}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.5
 */
public class XctxFreeBusyProvider implements FreeBusyProvider {

    private static final Logger LOG = LoggerFactory.getLogger(XctxFreeBusyProvider.class);

    private final ServiceLookup services;
    private final XctxCalendarProvider provider;

    /**
     * Initializes a new {@link XctxFreeBusyProvider}.
     *
     * @param services A service lookup reference
     * @param provider The calendar provider
     */
    public XctxFreeBusyProvider(ServiceLookup services, XctxCalendarProvider provider) {
        super();
        this.services = services;
        this.provider = provider;
    }

    @Override
    public Map<Attendee, Map<Integer, FreeBusyResult>> query(Session session, List<Attendee> attendees, Date from, Date until, boolean merge, CalendarParameters parameters) throws OXException {
        /*
         * get free/busy-enabled cross-context calendar accounts of user
         */
        List<CalendarAccount> accounts = getFreeBusyEnabledAccounts(session);
        if (accounts.isEmpty() || null == attendees || attendees.isEmpty()) {
            return Collections.emptyMap();
        }
        /*
         * query free/busy data for single account
         */
        if (1 == accounts.size()) {
            return getFreeBusy(session, accounts.get(0), parameters, attendees, from, until, merge);
        }
        /*
         * query free/busy data for each account and collect combined results
         */
        CompletionService<Map<Attendee, Map<Integer, FreeBusyResult>>> completionService = getCompletionService();
        for (CalendarAccount account : accounts) {
            completionService.submit(() -> getFreeBusy(session, account, parameters, attendees, from, until, merge));
        }
        Map<Attendee, Map<Integer, FreeBusyResult>> resultsPerAccountId = new HashMap<Attendee, Map<Integer, FreeBusyResult>>(attendees.size());
        for (int i = 0; i < accounts.size(); i++) {
            try {
                for (Entry<Attendee, Map<Integer, FreeBusyResult>> resultsForAttendee : completionService.take().get().entrySet()) {
                    Attendee attendee = CalendarUtils.find(attendees, resultsForAttendee.getKey());
                    if (null == attendee) {
                        LOG.debug("Skipping unexpected attendee {} in free/busy results", attendee);
                        continue;
                    }
                    Map<Integer, FreeBusyResult> results = resultsPerAccountId.get(attendee);
                    if (null == results) {
                        results = new HashMap<Integer, FreeBusyResult>();
                        resultsPerAccountId.put(attendee, results);
                    }
                    results.putAll(resultsForAttendee.getValue());
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw CalendarExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            } catch (ExecutionException e) {
                Throwable cause = e.getCause();
                if (null != cause && OXException.class.isInstance(e.getCause())) {
                    throw (OXException) cause;
                }
                throw CalendarExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
        return resultsPerAccountId;
    }

    private Map<Attendee, Map<Integer, FreeBusyResult>> getFreeBusy(Session session, CalendarAccount account, CalendarParameters parameters, List<Attendee> attendees, Date from, Date until, boolean merge) {
        Map<Attendee, Map<Integer, FreeBusyResult>> resultsPerAccountId = new HashMap<Attendee, Map<Integer, FreeBusyResult>>(attendees.size());
        XctxCalendarAccess calendarAccess = null;
        try {
            calendarAccess = provider.connect(session, account, parameters);
            for (Entry<Attendee, FreeBusyResult> entry : calendarAccess.getFreeBusy(attendees, from, until, merge).entrySet()) {
                Attendee attendee = CalendarUtils.find(attendees, entry.getKey());
                if (null == attendee) {
                    LOG.debug("Skipping unexpected attendee {} in free/busy results from account {}", attendee, I(account.getAccountId()));
                    continue;
                }
                resultsPerAccountId.put(attendee, Collections.singletonMap(I(account.getAccountId()), entry.getValue()));
            }
        } catch (OXException e) {
            return getErrorResults(account, attendees, e);
        } finally {
            if (null != calendarAccess) {
                calendarAccess.close();
            }
        }
        return resultsPerAccountId;
    }

    private static Map<Attendee, Map<Integer, FreeBusyResult>> getErrorResults(CalendarAccount account, List<Attendee> attendees, OXException error) {
        FreeBusyResult errorResult = new FreeBusyResult(Collections.emptyList(), Collections.singletonList(error));
        Map<Attendee, Map<Integer, FreeBusyResult>> resultsPerAccount = new HashMap<Attendee, Map<Integer, FreeBusyResult>>(attendees.size());
        for (Attendee attendee : attendees) {
            resultsPerAccount.put(attendee, Collections.singletonMap(I(account.getAccountId()), errorResult));
        }
        return resultsPerAccount;
    }

    private List<CalendarAccount> getFreeBusyEnabledAccounts(Session session) throws OXException {
        DefaultProperty property = DefaultProperty.valueOf("com.openexchange.calendar.xctx2.enableFreeBusy", Boolean.TRUE);
        if (false == services.getServiceSafe(LeanConfigurationService.class).getBooleanProperty(session.getUserId(), session.getContextId(), property)) {
            return Collections.emptyList();
        }
        List<CalendarAccount> enabledAccounts = new ArrayList<CalendarAccount>();
        CalendarAccountService accountService = services.getServiceSafe(CalendarAccountService.class);
        for (CalendarAccount account : accountService.getAccounts(session, Constants.PROVIDER_ID, null)) {
            try {
                if (isFreeBusyEnabled(account)) {
                    enabledAccounts.add(account);
                }
            } catch (OXException e) {
                LOG.warn("Error checking free/busy permissions for calendar account {}", I(account.getAccountId()), e);
            }
        }
        return enabledAccounts;
    }

    private boolean isFreeBusyEnabled(CalendarAccount account) throws OXException {
        if (null == account.getUserConfiguration()) {
            return false;
        }
        String baseToken = ShareTool.extractBaseToken(account.getUserConfiguration().optString("url", null));
        if (null == baseToken) {
            return false;
        }
        ShareToken shareToken = new ShareToken(baseToken);
        return services.getServiceSafe(UserPermissionService.class)
            .getUserPermissionBits(shareToken.getUserID(), shareToken.getContextID()).hasFreeBusy();
    }

    private static <V> CompletionService<V> getCompletionService() {
        ThreadPoolService threadPool = ThreadPools.getThreadPool();
        if (null == threadPool) {
            return new CallerRunsCompletionService<V>();
        }
        return new ThreadPoolCompletionService<V>(threadPool);
    }

}
