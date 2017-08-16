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
 *     Copyright (C) 2004-2020 Open-Xchange, Inc.
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

package com.openexchange.chronos.provider.composition.impl;

import static com.openexchange.java.Autoboxing.I;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.chronos.provider.CalendarAccess;
import com.openexchange.chronos.provider.CalendarAccount;
import com.openexchange.chronos.provider.CalendarProvider;
import com.openexchange.chronos.provider.CalendarProviderRegistry;
import com.openexchange.chronos.provider.account.CalendarAccountService;
import com.openexchange.chronos.provider.groupware.GroupwareCalendarAccess;
import com.openexchange.chronos.service.CalendarParameters;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;
import com.openexchange.tx.TransactionAware;

/**
 * {@link AbstractCompositingIDBasedCalendarAccess}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public abstract class AbstractCompositingIDBasedCalendarAccess implements TransactionAware, CalendarParameters {

    protected static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AbstractCompositingIDBasedCalendarAccess.class);

    protected final ServiceLookup services;
    protected final Session session;

    private final CalendarProviderRegistry providerRegistry;
    private final Map<String, Object> parameters;
    private final ConcurrentMap<Integer, CalendarAccess> connectedAccesses;

    /**
     * Initializes a new {@link AbstractCompositingIDBasedCalendarAccess}.
     *
     * @param session The session to create the ID-based access for
     * @param providerRegistry A reference to the calendar provider registry
     * @param services A service lookup reference
     */
    protected AbstractCompositingIDBasedCalendarAccess(Session session, CalendarProviderRegistry providerRegistry, ServiceLookup services) throws OXException {
        super();
        this.services = services;
        this.providerRegistry = providerRegistry;
        this.session = session;
        this.parameters = new HashMap<String, Object>();
        this.connectedAccesses = new ConcurrentHashMap<Integer, CalendarAccess>();
    }

    @Override
    public void startTransaction() throws OXException {
        ConcurrentMap<Integer, CalendarAccess> connectedAccesses = this.connectedAccesses;
        if (false == connectedAccesses.isEmpty()) {
            for (CalendarAccess access : connectedAccesses.values()) {
                LOG.warn("Access already connected: {}", access);
            }
        }
        connectedAccesses.clear();
    }

    @Override
    public void finish() throws OXException {
        /*
         * close any connected calendar accesses
         */
        ConcurrentMap<Integer, CalendarAccess> connectedAccesses = this.connectedAccesses;
        for (Iterator<Entry<Integer, CalendarAccess>> iterator = connectedAccesses.entrySet().iterator(); iterator.hasNext();) {
            Entry<Integer, CalendarAccess> entry = iterator.next();
            CalendarAccess access = entry.getValue();
            LOG.debug("Closing calendar access {} for account {}.", access, entry.getKey());
            access.close();
            iterator.remove();
        }
    }

    @Override
    public void commit() throws OXException {
        //
    }

    @Override
    public void rollback() throws OXException {
        //
    }

    @Override
    public void setTransactional(boolean transactional) {
        //
    }

    @Override
    public void setRequestTransactional(boolean transactional) {
        //
    }

    @Override
    public void setCommitsTransaction(boolean commits) {
        //
    }

    @Override
    public <T> CalendarParameters set(String parameter, T value) {
        parameters.put(parameter, value);
        return this;
    }

    @Override
    public <T> T get(String parameter, Class<T> clazz) {
        return get(parameter, clazz, null);
    }

    @Override
    public <T> T get(String parameter, Class<T> clazz, T defaultValue) {
        Object value = parameters.get(parameter);
        return null == value ? defaultValue : clazz.cast(value);
    }

    @Override
    public boolean contains(String parameter) {
        return parameters.containsKey(parameter);
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return Collections.unmodifiableSet(parameters.entrySet());
    }

    /**
     * Gets the groupware calendar access for a specific account. The account is connected implicitly and remembered to be closed during
     * {@link #finish()} implicitly, if not already done.
     *
     * @param accountId The identifier to get the calendar access for
     * @return The groupware calendar access for the specified account
     */
    protected GroupwareCalendarAccess getGroupwareAccess(int accountId) throws OXException {
        CalendarAccess access = getAccess(accountId);
        if (false == GroupwareCalendarAccess.class.isInstance(access)) {
            throw CalendarExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(getAccount(accountId).getProviderId());
        }
        return (GroupwareCalendarAccess) access;
    }

    /**
     * Gets the calendar access for a specific account. The account is connected implicitly and remembered to be closed during
     * {@link #finish()} implicitly, if not already done.
     *
     * @param accountId The identifier to get the calendar access for
     * @return The calendar access for the specified account
     */
    protected CalendarAccess getAccess(int accountId) throws OXException {
        CalendarAccess access = connectedAccesses.get(I(accountId));
        return null != access ? access : getAccess(getAccount(accountId));
    }

    /**
     * Gets the groupware calendar access for a specific account. The account is connected implicitly and remembered to be closed during
     * {@link #finish()} implicitly, if not already done.
     *
     * @param account The account to get the calendar access for
     * @return The groupware calendar access for the specified account
     */
    protected GroupwareCalendarAccess getGroupwareAccess(CalendarAccount account) throws OXException {
        CalendarAccess access = getAccess(account);
        if (false == GroupwareCalendarAccess.class.isInstance(access)) {
            throw CalendarExceptionCodes.UNSUPPORTED_OPERATION_FOR_PROVIDER.create(getAccount(account.getAccountId()).getProviderId());
        }
        return (GroupwareCalendarAccess) access;
    }

    /**
     * Gets the calendar access for a specific account. The account is connected implicitly and remembered to be closed during
     * {@link #finish()} implicitly, if not already done.
     *
     * @param account The account to get the calendar access for
     * @return The calendar access for the specified account
     */
    protected CalendarAccess getAccess(CalendarAccount account) throws OXException {
        ConcurrentMap<Integer, CalendarAccess> connectedAccesses = this.connectedAccesses;
        CalendarAccess access = connectedAccesses.get(I(account.getAccountId()));
        if (null == access) {
            CalendarProvider provider = providerRegistry.getCalendarProvider(account.getProviderId());
            if (null == provider) {
                throw CalendarExceptionCodes.PROVIDER_NOT_AVAILABLE.create(account.getProviderId());
            }
            access = provider.connect(session, account, this);
            CalendarAccess existingAccess = connectedAccesses.put(I(account.getAccountId()), access);
            if (null != existingAccess) {
                access.close();
                access = existingAccess;
            }
        }
        return access;
    }

    /**
     * Gets all calendar accounts of the current session's user.
     *
     * @return The calendar accounts
     */
    protected List<CalendarAccount> getAccounts() throws OXException {
        return services.getService(CalendarAccountService.class).loadAccounts(session);
    }

    /**
     * Gets a specific calendar account.
     *
     * @param accountId The identifier of the account to get
     * @return The calendar account
     */
    protected CalendarAccount getAccount(int accountId) throws OXException {
        CalendarAccountService service = services.getService(CalendarAccountService.class);
        return service.loadAccount(session, accountId);
    }

    @Override
    public String toString() {
        return new StringBuilder("IDBasedCalendarAccess ")
            .append("[user=").append(session.getUserId()).append(", context=").append(session.getContextId())
            .append(", connectedAccesses=").append(connectedAccesses.keySet()).append(']')
        .toString();
    }

}
