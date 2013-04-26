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

package com.openexchange.mail.json.actions;

import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.parser.SearchTermParser;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.json.OXJSONWriter;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.MailSortField;
import com.openexchange.mail.OrderDirection;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.json.MailRequest;
import com.openexchange.mail.json.writer.MessageWriter;
import com.openexchange.mail.json.writer.MessageWriter.MailFieldWriter;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.TimeZoneUtils;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link SearchAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@Action(method = RequestMethod.PUT, name = "search", description = "Search mails", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "folder", description = "Object ID of the folder, whose contents are queried."),
    @Parameter(name = "columns", description = "A comma-separated list of columns to return. Each column is specified by a numeric column identifier. Column identifiers for appointments are defined in Detailed mail data."),
    @Parameter(name = "sort", optional=true, description = "The identifier of a column which determines the sort order of the response or the string \"thread\" to return thread-sorted messages. If this parameter is specified and holds a column number, then the parameter order must be also specified."),
    @Parameter(name = "order", optional=true, description = "\"asc\" if the response entires should be sorted in the ascending order, \"desc\" if the response entries should be sorted in the descending order. If this parameter is specified, then the parameter sort must be also specified.")
}, requestBody = "A JSON array of JSON objects each containing the search field and its search pattern: e.g.: [{\"col\": 612, \"pattern\": \"Joe\"}, {\"col\": 614, \"pattern\": \"Tuesday\"}] Supported values for col are 603 to 607 (from, to, cc, bcc and subject) and -1 for full text search.",
responseDescription = "(not IMAP: with timestamp): An array with mail data. Each array element describes one mail and is itself an array. The elements of each array contain the information specified by the corresponding identifiers in the columns parameter.")
public final class SearchAction extends AbstractMailAction {

    /**
     * Initializes a new {@link SearchAction}.
     *
     * @param services
     */
    public SearchAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final MailRequest req) throws OXException {
        try {
            final ServerSession session = req.getSession();
            /*
             * Read in parameters
             */
            final String folderId = req.checkParameter(Mail.PARAMETER_MAILFOLDER);
            final int[] columns = req.checkIntArray(AJAXServlet.PARAMETER_COLUMNS);
            final String sort = req.getParameter(AJAXServlet.PARAMETER_SORT);
            final String order = req.getParameter(AJAXServlet.PARAMETER_ORDER);
            if (sort != null && order == null) {
                throw MailExceptionCode.MISSING_PARAM.create(AJAXServlet.PARAMETER_ORDER);
            }
            final boolean ignoreDeleted = !req.optBool("deleted", true);
            String stz = req.getParameter(Mail.PARAMETER_TIMEZONE);
            final TimeZone timeZone = isEmpty(stz) ? null : TimeZoneUtils.getTimeZone(stz.trim());
            stz = null;
            final JSONValue searchValue = (JSONValue) req.getRequest().getData();
            /*
             * Get mail interface
             */
            final MailServletInterface mailInterface = getMailInterface(req);
            /*
             * Perform search dependent on passed JSON value
             */
            final AJAXRequestResult result;
            if (searchValue.isArray()) {
                /*
                 * Parse body into a JSON array
                 */
                final JSONArray ja = searchValue.toArray();
                final int length = ja.length();
                if (length > 0) {
                    final int[] searchCols = new int[length];
                    final String[] searchPats = new String[length];
                    for (int i = 0; i < length; i++) {
                        final JSONObject tmp = ja.getJSONObject(i);
                        searchCols[i] = tmp.getInt(Mail.PARAMETER_COL);
                        searchPats[i] = tmp.getString(AJAXServlet.PARAMETER_SEARCHPATTERN);
                    }
                    /*
                     * Search mails
                     */
                    final MailFieldWriter[] writers = MessageWriter.getMailFieldWriter(MailListField.getFields(columns));
                    final int userId = session.getUserId();
                    final int contextId = session.getContextId();
                    int orderDir = OrderDirection.ASC.getOrder();
                    if (order != null) {
                        if (order.equalsIgnoreCase("asc")) {
                            orderDir = OrderDirection.ASC.getOrder();
                        } else if (order.equalsIgnoreCase("desc")) {
                            orderDir = OrderDirection.DESC.getOrder();
                        } else {
                            throw MailExceptionCode.INVALID_INT_VALUE.create(AJAXServlet.PARAMETER_ORDER);
                        }
                    }
                    final OXJSONWriter jsonWriter = new OXJSONWriter();
                    /*
                     * Start response
                     */
                    jsonWriter.array();
                    SearchIterator<MailMessage> it = null;
                    try {
                        if (("thread".equalsIgnoreCase(sort))) {
                            it =
                                mailInterface.getThreadedMessages(
                                    folderId,
                                    null,
                                    MailSortField.RECEIVED_DATE.getField(),
                                    orderDir,
                                    searchCols,
                                    searchPats,
                                    true,
                                    columns);
                            final int size = it.size();
                            for (int i = 0; i < size; i++) {
                                final MailMessage mail = it.next();
                                if (mail != null && (!ignoreDeleted || !mail.isDeleted())) {
                                    final JSONArray arr = new JSONArray();
                                    for (final MailFieldWriter writer : writers) {
                                        writer.writeField(arr, mail, 0, false, mailInterface.getAccountID(), userId, contextId, timeZone);
                                    }
                                    jsonWriter.value(arr);
                                }
                            }
                        } else {
                            final int sortCol = sort == null ? MailListField.RECEIVED_DATE.getField() : Integer.parseInt(sort);
                            it = mailInterface.getMessages(folderId, null, sortCol, orderDir, searchCols, searchPats, true, columns);
                            final int size = it.size();
                            for (int i = 0; i < size; i++) {
                                final MailMessage mail = it.next();
                                if (mail != null && (!ignoreDeleted || !mail.isDeleted())) {
                                    final JSONArray arr = new JSONArray();
                                    for (final MailFieldWriter writer : writers) {
                                        writer.writeField(arr, mail, 0, false, mailInterface.getAccountID(), userId, contextId, timeZone);
                                    }
                                    jsonWriter.value(arr);
                                }
                            }
                        }
                    } finally {
                        if (it != null) {
                            it.close();
                        }
                    }
                    jsonWriter.endArray();
                    result = new AJAXRequestResult(jsonWriter.getObject(), "json");
                } else {
                    result = new AJAXRequestResult(new JSONArray(), "json");
                }
            } else {
                final JSONArray searchArray = searchValue.toObject().getJSONArray(Mail.PARAMETER_FILTER);
                /*
                 * Pre-Select field writers
                 */
                final MailFieldWriter[] writers = MessageWriter.getMailFieldWriter(MailListField.getFields(columns));
                final int userId = session.getUserId();
                final int contextId = session.getContextId();
                int orderDir = OrderDirection.ASC.getOrder();
                if (order != null) {
                    if (order.equalsIgnoreCase("asc")) {
                        orderDir = OrderDirection.ASC.getOrder();
                    } else if (order.equalsIgnoreCase("desc")) {
                        orderDir = OrderDirection.DESC.getOrder();
                    } else {
                        throw MailExceptionCode.INVALID_INT_VALUE.create(AJAXServlet.PARAMETER_ORDER);
                    }
                }
                final int sortCol = sort == null ? MailListField.RECEIVED_DATE.getField() : Integer.parseInt(sort);
                final SearchIterator<MailMessage> it =
                    mailInterface.getMessages(folderId, null, sortCol, orderDir, SearchTermParser.parse(searchArray), true, columns);
                final int size = it.size();
                final OXJSONWriter jsonWriter = new OXJSONWriter();
                /*
                 * Start response
                 */
                jsonWriter.array();
                for (int i = 0; i < size; i++) {
                    final MailMessage mail = it.next();
                    if (mail != null && (!ignoreDeleted || !mail.isDeleted())) {
                        final JSONArray arr = new JSONArray();
                        for (final MailFieldWriter writer : writers) {
                            writer.writeField(arr, mail, 0, false, mailInterface.getAccountID(), userId, contextId, timeZone);
                        }
                        jsonWriter.value(arr);
                    }
                }
                jsonWriter.endArray();
                result = new AJAXRequestResult(jsonWriter.getObject(), "json");
            }
            return result;
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

}
