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

package com.openexchange.mail.json.actions;

import static com.openexchange.tools.Collections.newHashMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.json.MailRequest;
import com.openexchange.mail.json.utils.ColumnCollection;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link ListAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@Action(method = RequestMethod.PUT, name = "list", description = "Get a list of mails", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "columns", description = "A comma-separated list of columns to return. Each column is specified by a numeric column identifier. Column identifiers for mails are defined in Detailed mail data. The alias \"list\" uses the predefined columnset [20, 1, 5, 2, 500, 501, 502, 505, 523, 525, 526, 527, 542, 555, 102, 602, 592, 101, 551, 552, 543, 547, 548, 549, 556, 569]"),
    @Parameter(name = "headers", description = "(preliminary) A comma-separated list of header names. Each name requests denoted header from each mail")
}, requestBody = "An array with one object for each requested mail. Each object contains the fields folder and id.",
responseDescription = "Response (not IMAP: with timestamp): An array with mail data. Each array element describes one mail and is itself an array. The elements of each array contain the information specified by the corresponding identifiers in the columns parameter followed by requested headers.")
public final class ListAction extends AbstractMailAction {

    /**
     * Initializes a new {@link ListAction}.
     *
     * @param services
     */
    public ListAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final MailRequest req) throws OXException {
        try {
            // Read in parameters
            ColumnCollection columnCollection = req.checkColumnsAndHeaders();
            int[] columns = columnCollection.getFields();
            String[] headers = columnCollection.getHeaders();
            /*
             * Get map
             */
            final Map<String, List<String>> idMap = fillMapByArray((JSONArray) req.getRequest().getData());
            if (idMap.isEmpty()) {
                /*
                 * Request body is an empty JSON array
                 */
                return new AJAXRequestResult(Collections.<MailMessage> emptyList(), "mail");
            }
            /*
             * Get mail interface
             */
            final MailServletInterface mailInterface = getMailInterface(req);
            /*
             * Start response
             */
            final List<MailMessage> list = new LinkedList<MailMessage>();
            columns = prepareColumns(columns);
            for (Map.Entry<String, List<String>> entry : idMap.entrySet()) {
                MailMessage[] mails = mailInterface.getMessageList(entry.getKey(), toArray(entry.getValue()), columns, headers);
                int accountID = mailInterface.getAccountID();
                for (int i = 0; i < mails.length; i++) {
                    MailMessage mail = mails[i];
                    if (mail != null) {
                        if (!mail.containsAccountId()) {
                            mail.setAccountId(accountID);
                        }
                        list.add(mail);
                    }
                }
            }
            return new AJAXRequestResult(list, "mail");
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private static final Map<String, List<String>> fillMapByArray(final JSONArray idArray) throws JSONException, OXException {
        if (null == idArray) {
            throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
        }

        final int length = idArray.length();
        if (length <= 0) {
            return Collections.emptyMap();
        }
        final Map<String, List<String>> idMap = newHashMap(4);
        final String parameterFolderId = AJAXServlet.PARAMETER_FOLDERID;
        final String parameterId = AJAXServlet.PARAMETER_ID;
        String folder;
        List<String> list;
        {
            final JSONObject idObject = idArray.getJSONObject(0);
            folder = ensureString(parameterFolderId, idObject);
            list = new ArrayList<String>(length);
            idMap.put(folder, list);
            list.add(ensureString(parameterId, idObject));
        }
        for (int i = 1; i < length; i++) {
            final JSONObject idObject = idArray.getJSONObject(i);
            final String fld = ensureString(parameterFolderId, idObject);
            if (!folder.equals(fld)) {
                folder = fld;
                final List<String> tmp = idMap.get(folder);
                if (tmp == null) {
                    list = new ArrayList<String>(length);
                    idMap.put(folder, list);
                } else {
                    list = tmp;
                }
            }
            list.add(ensureString(parameterId, idObject));
        }
        return idMap;
    }

    private static String ensureString(final String key, final JSONObject jo) throws OXException {
        if (!jo.hasAndNotNull(key)) {
            throw MailExceptionCode.MISSING_PARAMETER.create(key);
        }
        return jo.optString(key);
    }

    private static String[] toArray(final Collection<String> c) {
        return c.toArray(new String[c.size()]);
    }

}
