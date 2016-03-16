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

package com.openexchange.user.json.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.i18n.Users;
import com.openexchange.groupware.ldap.User;
import com.openexchange.i18n.tools.StringHelper;
import com.openexchange.java.Strings;
import com.openexchange.server.ServiceLookup;
import com.openexchange.server.impl.EffectivePermission;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.json.filter.DoNothingUserCensorship;
import com.openexchange.user.json.filter.NoGlobalAdressBookUserCensorship;
import com.openexchange.user.json.filter.UserCensorship;

/**
 * {@link AbstractUserAction} - An abstract user action.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractUserAction implements AJAXActionService {

    private static final String ALL = "*";

    /** The service look-up */
    protected final ServiceLookup services;

    /**
     * Initializes a new {@link AbstractUserAction}.
     */
    protected AbstractUserAction(ServiceLookup services) {
        super();
        this.services = services;
    }

    /**
     * Split a comma-separated string.
     */
    private static final Pattern SPLIT = Pattern.compile(" *, *");

    /**
     * Gets the attribute parameters.
     *
     * @param expectedParameterNames The expected parameter names
     * @param request The request
     * @return The attribute parameters
     * @throws OXException If parsing attribute parameters fails
     */
    protected static Map<String, List<String>> getAttributeParameters(final Set<String> expectedParameterNames, final AJAXRequestData request) throws OXException {
        final Iterator<Entry<String, String>> nonMatchingParameters = request.getNonMatchingParameters(expectedParameterNames);
        if (!nonMatchingParameters.hasNext()) {
            return Collections.emptyMap();
        }
        final Map<String, List<String>> attributeParameters = new LinkedHashMap<String, List<String>>();
        do {
            final Entry<String, String> entry = nonMatchingParameters.next();
            final String key = entry.getKey();
            List<String> list = attributeParameters.get(key);
            if (null == list) {
                list = new ArrayList<String>(4);
                attributeParameters.put(key, list);
            }
            final String value = entry.getValue();
            final int pos = value.indexOf('*');
            if (pos < 0) {
                final String[] strings = SPLIT.split(value, 0);
                for (final String string : strings) {
                    list.add(string);
                }
            } else {
                if (value.length() > 1) {
                    throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create( key, value);
                }
                list.add(ALL);
            }
        } while (nonMatchingParameters.hasNext());
        return attributeParameters;
    }

    /**
     * Parses specified parameter into an <code>Long</code>.
     *
     * @param parameterName The parameter name
     * @param request The request
     * @return The parsed <code>Long</code> value or <code>null</code> if not present
     * @throws OXException If parameter is invalid in given request
     */
    protected static Long parseLongParameter(final String parameterName, final AJAXRequestData request) throws OXException {
        String tmp = request.getParameter(parameterName);
        if (null == tmp) {
            return null;
        }
        tmp = tmp.trim();
        try {
            return Long.valueOf(tmp);
        } catch (final NumberFormatException e) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create( parameterName, tmp);
        }
    }

    /**
     * Parses specified parameter into an <code>long</code>.
     *
     * @param parameterName The parameter name
     * @param request The request
     * @return The parsed <code>long</code> value
     * @throws OXException If parameter is invalid in given request
     */
    protected static long checkLongParameter(final String parameterName, final AJAXRequestData request) throws OXException {
        String tmp = request.getParameter(parameterName);
        if (null == tmp) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create( parameterName);
        }
        tmp = tmp.trim();
        try {
            return Long.parseLong(tmp);
        } catch (final NumberFormatException e) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create( parameterName, tmp);
        }
    }

    /**
     * Parses specified parameter into an <code>int</code>.
     *
     * @param parameterName The parameter name
     * @param request The request
     * @return The parsed <code>int</code> value or <code>-1</code> if not present
     * @throws OXException If parameter is invalid in given request
     */
    protected static int parseIntParameter(final String parameterName, final AJAXRequestData request) throws OXException {
        String tmp = request.getParameter(parameterName);
        if (null == tmp) {
            return -1;
        }
        tmp = tmp.trim();
        try {
            return Integer.parseInt(tmp);
        } catch (final NumberFormatException e) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create( parameterName, tmp);
        }
    }

    /**
     * Parses specified parameter into an <code>int</code>.
     *
     * @param parameterName The parameter name
     * @param request The request
     * @return The parsed <code>int</code> value
     * @throws OXException If parameter is not present or invalid in given request
     */
    protected static int checkIntParameter(final String parameterName, final AJAXRequestData request) throws OXException {
        String tmp = request.getParameter(parameterName);
        if (null == tmp) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create( parameterName);
        }
        tmp = tmp.trim();
        try {
            return Integer.parseInt(tmp);
        } catch (final NumberFormatException e) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create( parameterName, tmp);
        }
    }

    /**
     * Parses specified parameter into an <code>int</code>.
     *
     * @param parameterName The parameter name
     * @param request The request
     * @return The parsed <code>int</code> value
     * @throws OXException If parameter is not present or invalid in given request
     */
    protected static String checkStringParameter(final String parameterName, final AJAXRequestData request) throws OXException {
        String tmp = request.getParameter(parameterName);
        if (null == tmp) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create( parameterName);
        }
        return tmp.trim();
    }

    private static final Pattern PAT = Pattern.compile(" *, *");

    private static final UserCensorship DO_NOTHING_USER_CENSORSHIP = new DoNothingUserCensorship();
    private static final UserCensorship NO_GLOBAL_ADDRESSBOOK_USER_CENSORSHIP = new NoGlobalAdressBookUserCensorship();

    /**
     * Parses specified parameter into an array of <code>int</code>.
     *
     * @param parameterName The parameter name
     * @param request The request
     * @return The parsed array of <code>int</code>
     * @throws OXException If parameter is not present in given request
     */
    protected static int[] parseIntArrayParameter(final String parameterName, final AJAXRequestData request) throws OXException {
        final String tmp = request.getParameter(parameterName);
        if (null == tmp) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create( parameterName);
        }
        final String[] sa = PAT.split(tmp, 0);
        final int[] columns = new int[sa.length];
        for (int i = 0; i < sa.length; i++) {
            columns[i] = Integer.parseInt(sa[i]);
        }
        return columns;
    }

    /**
     * Parses specified optional parameter into an array of <code>int</code>.
     *
     * @param parameterName The parameter name
     * @param request The request
     * @return The parsed array of <code>int</code>; a zero length array is returned if parameter is missing
     */
    protected static int[] parseOptionalIntArrayParameter(final String parameterName, final AJAXRequestData request) {
        final String tmp = request.getParameter(parameterName);
        if (null == tmp) {
            return new int[0];
        }
        final String[] sa = PAT.split(tmp, 0);
        final int[] columns = new int[sa.length];
        for (int i = 0; i < sa.length; i++) {
            columns[i] = Integer.parseInt(sa[i]);
        }
        return columns;
    }

    /**
     * Creates a virtual contact based on some properties available in the supplied user.
     *
     * @param session The session
     * @param user The user to create the contact for
     * @return The contact
     */
    protected static Contact getVirtualContact(ServerSession session, User user) {
        Contact contact = new Contact();
        contact.setInternalUserId(user.getId());
        contact.setObjectID(user.getContactId());
        contact.setCreatedBy(user.getCreatedBy());
        contact.setEmail1(user.getMail());
        contact.setGivenName(user.getGivenName());
        contact.setSurName(user.getSurname());
        if (user.isGuest() && Strings.isEmpty(user.getMail()) &&
            (Strings.isEmpty(user.getDisplayName()) || Users.GUEST.equals(user.getDisplayName()))) {
            Locale locale = session.getUser().getLocale();
            if (null != locale) {
                contact.setDisplayName(StringHelper.valueOf(locale).getString(Users.GUEST));
            } else {
                contact.setDisplayName(Users.GUEST);
            }
        } else {
            contact.setDisplayName(Strings.isEmpty(user.getDisplayName()) ? user.getMail() : user.getDisplayName());
        }
        return contact;
    }

    protected static void censor(final ServerSession session, final User[] user) throws OXException {
        final UserCensorship censorship = getUserCensorship(session);
        for(int i = 0; i < user.length; i++) {
            if(user[i].getId() == session.getUserId()) {
                continue;
            }
            user[i] = censorship.censor(user[i]);
        }
    }

    protected static User censor(final ServerSession session, final User user) throws OXException {
        if(user.getId() == session.getUserId()) {
            return user;
        }
        final UserCensorship censorship = getUserCensorship(session);
        return censorship.censor(user);
    }

    protected static UserCensorship getUserCensorship(final ServerSession session) throws OXException {
        if (canSeeGlobalAddressBook(session)) {
            return DO_NOTHING_USER_CENSORSHIP;
        }

        return NO_GLOBAL_ADDRESSBOOK_USER_CENSORSHIP;
    }

    private static boolean canSeeGlobalAddressBook(final ServerSession session) throws OXException {
        final OXFolderAccess folderAccess = new OXFolderAccess(session.getContext());
        final EffectivePermission folderPermission = folderAccess.getFolderPermission(FolderObject.SYSTEM_LDAP_FOLDER_ID, session.getUserId(), session.getUserPermissionBits());
        return folderPermission.canReadAllObjects() && folderPermission.isFolderVisible();
    }
}
