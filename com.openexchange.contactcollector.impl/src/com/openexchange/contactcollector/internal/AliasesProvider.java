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

package com.openexchange.contactcollector.internal;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import com.openexchange.concurrent.TimeoutConcurrentMap;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.alias.UserAliasStorage;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.user.UserService;


/**
 * {@link AliasesProvider}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AliasesProvider {

    /** The logger constant */
    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AliasesProvider.class);

    private static final AliasesProvider INSTANCE = new AliasesProvider();

    /**
     * Gets the instance.
     *
     * @return The instance
     */
    public static AliasesProvider getInstance() {
        return INSTANCE;
    }

    /*-
     * Member stuff
     */

    private TimeoutConcurrentMap<Integer, Future<Set<InternetAddress>>> aliasesMap;

    /**
     * Initializes a new {@link AliasesProvider}.
     */
    public AliasesProvider() {
        super();
    }

    /**
     * Gets all aliases of all users of specified context.
     *
     * @param context The context
     * @param userService The user service
     * @param aliasStorage The optional alias storage to use
     * @return All aliases
     * @throws Exception If an error occurs
     */
    public Set<InternetAddress> getContextAliases(final Context context, final UserService userService, final UserAliasStorage aliasStorage) throws Exception {
        final Integer key = Integer.valueOf(context.getContextId());
        Future<Set<InternetAddress>> f = aliasesMap.get(key);
        if (null == f) {
            final FutureTask<Set<InternetAddress>> ft = new FutureTask<Set<InternetAddress>>(new Callable<Set<InternetAddress>>() {

                @Override
                public Set<InternetAddress> call() throws Exception {
                    // First check alias storage availability
                    if (null != aliasStorage) {
                        Set<String> aliases = aliasStorage.getAliases(context.getContextId());
                        if (aliases.isEmpty()) {
                            return Collections.emptySet();
                        }

                        Set<InternetAddress> set = new HashSet<InternetAddress>(aliases.size());
                        for (String aliase : aliases) {
                            try {
                                set.add(new QuotedInternetAddress(aliase, false));
                            } catch (AddressException e) {
                                LOG.debug("Alias could not be parsed to an internet address: {}", aliase, e);
                            }
                        }
                        return set;
                    }

                    // All context-known users' aliases
                    int[] allUserIDs = userService.listAllUser(context);
                    Set<InternetAddress> aliases = new HashSet<InternetAddress>(allUserIDs.length * 8);
                    for (int allUserID : allUserIDs) {
                        aliases.addAll(getAliases(userService.getUser(allUserID, context), context, aliasStorage));
                    }
                    return aliases;
                }
            });
            // Put (if absent) with 5 minutes time-to-live.
            f = aliasesMap.putIfAbsent(key, ft, 300);
            if (f == null) {
                f = ft;
                ft.run();
            }
        }
        try {
            return f.get();
        } catch (final InterruptedException e) {
            // Cannot occur
            throw e;
        } catch (final ExecutionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof Exception) {
                throw (Exception) cause;
            }
            throw new Exception(cause);
        }
    }

    /**
     * Gets the aliases of a specified user.
     *
     * @param user The user whose aliases shall be returned
     * @param context The user-associated context
     * @param aliasStorage The optional alias storage to use
     * @return The aliases of a specified user
     */
    public Set<InternetAddress> getAliases(User user, Context context, UserAliasStorage aliasStorage) {
        if (null != aliasStorage) {
            try {
                Set<String> aliases = aliasStorage.getAliases(context.getContextId(), user.getId());
                if (aliases.isEmpty()) {
                    return Collections.emptySet();
                }

                Set<InternetAddress> set = new HashSet<InternetAddress>(aliases.size());
                for (String aliase : aliases) {
                    try {
                        set.add(new QuotedInternetAddress(aliase, false));
                    } catch (final AddressException e) {
                        LOG.debug("Alias could not be parsed to an internet address: {}", aliase, e);
                    }
                }
                return set;
            } catch (OXException e) {
                LOG.debug("Aliases could not be fetched from alias service.", e);
            }
        }

        String[] aliases = user.getAliases();
        if (null == aliases || aliases.length <= 0) {
            return Collections.emptySet();
        }

        Set<InternetAddress> set = new HashSet<InternetAddress>(aliases.length);
        for (String aliase : aliases) {
            try {
                set.add(new QuotedInternetAddress(aliase, false));
            } catch (AddressException e) {
                LOG.debug("Alias could not be parsed to an internet address: {}", aliase, e);
            }
        }
        return set;
    }

    /**
     * Starts this contact collector service implementation.
     *
     * @throws OXException If a needed service is missing
     */
    public void start() throws OXException {
        aliasesMap = new TimeoutConcurrentMap<Integer, Future<Set<InternetAddress>>>(60, true);
    }

    /**
     * Stops this contact collector service implementation.
     */
    public void stop() {
        if (null != aliasesMap) {
            aliasesMap.dispose();
            aliasesMap = null;
        }
    }


}
