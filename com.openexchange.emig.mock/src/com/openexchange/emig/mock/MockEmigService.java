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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.emig.mock;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import com.openexchange.context.ContextService;
import com.openexchange.emig.EmigExceptionCodes;
import com.openexchange.emig.EmigService;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.ldap.LdapExceptionCode;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserExceptionCode;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.user.UserService;


/**
 * {@link MockEmigService} - EMig fully enabled...
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.4.2
 */
public final class MockEmigService implements EmigService {

    private final AtomicReference<Set<String>> nonEmigDomainsRef;
    private final ServiceLookup services;

    /**
     * Initializes a new {@link MockEmigService}.
     */
    public MockEmigService(final ServiceLookup services) {
        super();
        this.services = services;
        nonEmigDomainsRef = new AtomicReference<Set<String>>(Collections.<String> emptySet());
    }

    /**
     * Applies specified list of non-emig domains.
     *
     * @param sNonEmigDomains The comma-separated list of non-emig domains
     */
    public void applyNonEmigDomains(final String sNonEmigDomains) {
        if (Strings.isEmpty(sNonEmigDomains)) {
            nonEmigDomainsRef.set(Collections.<String> emptySet());
            return;
        }

        // Parse string
        String str = sNonEmigDomains;
        if (str.startsWith("\"") && str.endsWith("\"")) {
            str = str.substring(1, str.length() - 1);
        }
        final String[] domains = Strings.splitByComma(str);
        final Set<String> set = new LinkedHashSet<String>(domains.length);
        for (final String domain : domains) {
            set.add(Strings.toLowerCase(domain));
        }
    }

    @Override
    public boolean isEMIG_Session(String userIdentifier) throws OXException {
        return checkMailAddress(userIdentifier, true);
    }

    @Override
    public boolean isEMIG_MSA(String serverName, String mailFrom, String debugLoginname) throws OXException {
        return checkMailAddress(mailFrom, false);
    }

    @Override
    public int[] isEMIG_Recipient(String[] mailAddresses) throws OXException {
        if (null == mailAddresses) {
            return new int[0];
        }
        final int length = mailAddresses.length;
        if (0 == length) {
            return new int[0];
        }
        final int[] retval = new int[length];
        for (int i = 0; i < length; i++) {
            retval[i] = checkMailAddress(mailAddresses[i], false) ? 2 : 1;
        }
        return retval;
    }

    private boolean checkMailAddress(String address, boolean isUserId) throws OXException {
        if (Strings.isEmpty(address)) {
            return false;
        }

        final Set<String> set = nonEmigDomainsRef.get();
        if (null == set || set.isEmpty()) {
            return true;
        }

        final ContextService contextService = services.getOptionalService(ContextService.class);
        if (null == contextService) {
            throw ServiceExceptionCode.absentService(ContextService.class);
        }

        final UserService userService = services.getOptionalService(UserService.class);
        if (null == userService) {
            throw ServiceExceptionCode.absentService(UserService.class);
        }

        final String[] splitted = split(address);
        final int ctxId = contextService.getContextId(splitted[0]);
        if (ContextStorage.NOT_FOUND == ctxId) {
            throw isUserId ? EmigExceptionCodes.INVALID_USER_IDENTIFER.create(address) : EmigExceptionCodes.EMAIL_PARSE_ERROR.create(address);
        }
        final Context ctx = contextService.getContext(ctxId);
        final int userId;
        try {
            userId = userService.getUserId(splitted[1], ctx);
        } catch (final OXException e) {
            if (UserExceptionCode.PROPERTY_MISSING.getPrefix().equals(e.getPrefix()) && LdapExceptionCode.USER_NOT_FOUND.getNumber() == e.getCode()) {
                throw isUserId ? EmigExceptionCodes.INVALID_USER_IDENTIFER.create(address) : EmigExceptionCodes.EMAIL_PARSE_ERROR.create(address);
            }
            throw e;
        }
        final User user = userService.getUser(userId, ctx);
        for (final String alias : user.getAliases()) {
            final int pos = alias.indexOf('@');
            if (set.contains(Strings.toLowerCase(pos > 0 ? alias.substring(pos + 1) : alias))) {
                return false;
            }
        }
        return true;
    }

    /**
     * Splits user name and context.
     *
     * @param loginInfo combined information separated by an <code>@</code> sign.
     * @return a string array with context and user name (in this order).
     */
    private String[] split(final String loginInfo) {
        return split(loginInfo, '@');
    }

    /**
     * Splits user name and context.
     * @param loginInfo combined information separated by an <code>@</code> sign.
     * @param separator for splitting user name and context.
     * @return a string array with context and user name (in this order).
     * @throws OXException if no separator is found.
     */
    private String[] split(final String loginInfo, final char separator) {
        final int pos = loginInfo.lastIndexOf(separator);
        final String[] splitted;
        if (-1 == pos) {
            splitted = new String[] { "defaultcontext", loginInfo };
        } else {
            splitted = new String[] { loginInfo.substring(pos + 1), loginInfo.substring(0, pos) };
        }
        return splitted;
    }

}
