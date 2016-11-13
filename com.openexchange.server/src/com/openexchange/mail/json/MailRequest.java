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

package com.openexchange.mail.json;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import com.google.common.collect.ImmutableSet;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.exception.OXException;
import com.openexchange.java.Strings;
import com.openexchange.java.util.Tools;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailJSONField;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.config.MailProperties;
import com.openexchange.mail.json.actions.AbstractMailAction;
import com.openexchange.mail.json.utils.Column;
import com.openexchange.mail.json.utils.ColumnCollection;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link MailRequest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailRequest {

    private static final String PARAMETER_HEADERS = Mail.PARAMETER_HEADERS;
    private static final String PARAMETER_COLUMNS = AJAXServlet.PARAMETER_COLUMNS;

    /**
     * Constant for not-found number.
     */
    public static final int NOT_FOUND = -9999;

    private final ServerSession session;
    private final AJAXRequestData requestData;
    private MailServletInterface mailServletInterface;

    /**
     * Initializes a new {@link MailRequest}.
     *
     * @param session The session
     * @param request The request
     */
    public MailRequest(final AJAXRequestData request, final ServerSession session) {
        super();
        this.requestData = request;
        this.session = session;
    }


    /**
     * Gets the newly opened {@link MailServletInterface} associated with this request
     *
     * @return The {@code MailServletInterface} instance or <code>null</code>
     */
    public MailServletInterface getMailServletInterface() {
        return mailServletInterface;
    }

    /**
     * Sets the newly opened {@link MailServletInterface} associated with this request
     *
     * @param openedMailServletInterface The {@code MailServletInterface} instance
     */
    public void setMailServletInterface(MailServletInterface openedMailServletInterface) {
        this.mailServletInterface = openedMailServletInterface;
    }

    private static final Set<String> ALIASES_MAX = ImmutableSet.of("max", "maximum");

    /**
     * Gets the <code>max</code> parameter.
     *
     * @return The <code>max</code> parameter
     * @throws OXException If <code>max</code> is not a number
     */
    public long getMax() throws OXException {
        for (String name : ALIASES_MAX) {
            String value = requestData.getParameter(name);
            if (null != value) {
                try {
                    return Long.parseLong(value.trim());
                } catch (NumberFormatException e) {
                    throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(name, value);
                }
            }
        }
        return -1L;
    }

    /**
     * Gets the value mapped to given parameter name.
     *
     * @param name The parameter name
     * @return The value mapped to given parameter name
     * @throws NullPointerException If name is <code>null</code>
     * @throws OXException If no such parameter exists
     */
    public String checkParameter(final String name) throws OXException {
        return requestData.nonEmptyParameter(name);
    }

    /**
     * Gets the value mapped to given parameter name.
     *
     * @param name The parameter name
     * @return The value mapped to given parameter name or <code>null</code> if not present
     * @throws NullPointerException If name is <code>null</code>
     */
    public String getParameter(final String name) {
        return requestData.getParameter(name);
    }

    /**
     * Gets optional <code>boolean</code> parameter.
     *
     * @param name The parameter name
     * @return The <code>boolean</code>
     */
    public boolean optBool(final String name) {
        return AJAXRequestDataTools.parseBoolParameter(requestData.getParameter(name));
    }

    /**
     * Gets optional <code>boolean</code> parameter.
     *
     * @param name The parameter name
     * @param def The default value to return if such a parameter is absent
     * @return The <code>boolean</code>
     */
    public boolean optBool(final String name, final boolean def) {
        final String parameter = requestData.getParameter(name);
        if (null == parameter) {
            return def;
        }
        return AJAXRequestDataTools.parseBoolParameter(parameter);
    }

    /**
     * Gets optional <code>int</code> parameter.
     *
     * @param name The parameter name
     * @return The <code>int</code>
     * @throws OXException If parameter is an invalid number value
     */
    public int optInt(final String name) throws OXException {
        final String parameter = requestData.getParameter(name);
        if (null == parameter) {
            return NOT_FOUND;
        }
        try {
            return Integer.parseInt(parameter.trim());
        } catch (final NumberFormatException e) {
            throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(name, parameter);
        }
    }

    /**
     * Checks for columns.
     *
     * @param requestData The associated request data
     * @return The column collection
     * @throws OXException If parameter is missing
     */
    public static ColumnCollection requireColumnsAndHeaders(AJAXRequestData requestData) throws OXException {
        String parameter = requestData.getParameter(PARAMETER_COLUMNS);
        if (null == parameter) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(PARAMETER_COLUMNS);
        }

        List<Column> l;
        if (parameter.equals("all")) {
            l = new ArrayList<Column>(Arrays.asList(AbstractMailAction.COLUMNS_ALL_ALIAS));
        } else if (parameter.equals("list")) {
            l = new ArrayList<Column>(Arrays.asList(AbstractMailAction.COLUMNS_LIST_ALIAS));
        } else {
            String[] sa = SPLIT.split(parameter, 0);
            l = new ArrayList<Column>(sa.length);
            for (String s : sa) {
                int field = Tools.getUnsignedInteger(s);
                if (field > 0) {
                    if (field == MailListField.DATE.getField()) {
                        field = MailProperties.getInstance().isPreferSentDate() ? MailListField.SENT_DATE.getField() : MailListField.RECEIVED_DATE.getField();
                    }
                    l.add(Column.field(field));
                } else {
                    if (MailJSONField.DATE.getKey().equalsIgnoreCase(s)) {
                        field = MailProperties.getInstance().isPreferSentDate() ? MailListField.SENT_DATE.getField() : MailListField.RECEIVED_DATE.getField();
                        l.add(Column.field(field));
                    } else {
                        l.add(Column.header(s));
                    }
                }
            }
        }

        parameter = requestData.getParameter(PARAMETER_HEADERS);
        if (null != parameter) {
            String[] sa = SPLIT.split(parameter, 0);
            for (String s : sa) {
                l.add(Column.header(s));
            }
        }

        return new ColumnCollection(l);
    }

    /**
     * Checks for columns.
     *
     * @return The column collection
     * @throws OXException If parameter is missing
     */
    public ColumnCollection checkColumnsAndHeaders() throws OXException {
        return requireColumnsAndHeaders(requestData);
    }

    /**
     * Split pattern for CSV.
     */
    private static final Pattern SPLIT = Pattern.compile(" *, *");

    /**
     * Checks for presence of comma-separated <code>int</code> list.
     *
     * @param name The parameter name
     * @return The <code>int</code> array
     * @throws OXException If an error occurs
     */
    public int[] checkIntArray(final String name) throws OXException {
        final String parameter = requestData.getParameter(name);
        if (null == parameter) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(name);
        }
        if (name.equals(PARAMETER_COLUMNS)) {
            if (parameter.equals("all")) {
                return AbstractMailAction.FIELDS_ALL_ALIAS;
            }
            if (parameter.equals("list")) {
                return AbstractMailAction.FIELDS_LIST_ALIAS;
            }
        }
        final String[] sa = SPLIT.split(parameter, 0);
        final int[] ret = new int[sa.length];
        for (int i = 0; i < sa.length; i++) {
            try {
                ret[i] = Integer.parseInt(sa[i].trim());
            } catch (final NumberFormatException e) {
                throw AjaxExceptionCodes.INVALID_PARAMETER_VALUE.create(name, parameter);
            }
        }
        return ret;
    }

    /**
     * Gets the appropriate sort field for given sort string.
     *
     * @param sort The sort string to examine
     * @return The appropriate sort field
     * @throws OXException If sort field cannot be returned
     */
    public int getSortFieldFor(String sort) throws OXException {
        if (Strings.isEmpty(sort)) {
            return MailListField.RECEIVED_DATE.getField();
        }
        String parseMe = sort.trim();

        if (MailJSONField.DATE.getKey().equalsIgnoreCase(parseMe)) {
            return MailProperties.getInstance().isPreferSentDate() ? MailListField.SENT_DATE.getField() : MailListField.RECEIVED_DATE.getField();
        }

        try {
            int field = Integer.parseInt(parseMe);
            if (MailListField.DATE.getField() == field) {
                return (MailProperties.getInstance().isPreferSentDate() ? MailListField.SENT_DATE.getField() : MailListField.RECEIVED_DATE.getField());
            }

            return field < 0 ? MailListField.RECEIVED_DATE.getField() : field;
        } catch (NumberFormatException e) {
            throw MailExceptionCode.INVALID_INT_VALUE.create(e, AJAXServlet.PARAMETER_SORT);
        }
    }

    /**
     * Checks for presence of comma-separated <code>String</code> list.
     *
     * @param name The parameter name
     * @return The <code>String</code> array
     * @throws OXException If parameter is absdent
     */
    public String[] checkStringArray(final String name) throws OXException {
        final String parameter = requestData.getParameter(name);
        if (null == parameter) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(name);
        }
        return SPLIT.split(parameter, 0);
    }

    /**
     * Checks for presence of comma-separated <code>String</code> list.
     *
     * @param name The parameter name
     * @return The <code>String</code> array
     */
    public String[] optStringArray(final String name) {
        final String parameter = requestData.getParameter(name);
        if (null == parameter) {
            return null;
        }
        return SPLIT.split(parameter, 0);
    }

    /**
     * Gets the request.
     *
     * @return The request
     */
    public AJAXRequestData getRequest() {
        return requestData;
    }

    /**
     * Gets the session.
     *
     * @return The session
     */
    public ServerSession getSession() {
        return session;
    }
}
