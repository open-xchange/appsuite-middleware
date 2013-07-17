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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.json.OXJSONWriter;
import com.openexchange.mail.FullnameArgument;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.json.MailRequest;
import com.openexchange.mail.utils.MailFolderUtility;
import com.openexchange.server.ServiceLookup;

/**
 * {@link DeleteAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@Action(method = RequestMethod.PUT, name = "delete", description = "Delete mails", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "timestamp", description = "Timestamp of the last update of the deleted mails.")
}, requestBody = "An array of objects providing folder IDs and object IDs of the deleted mails. [{ \"folder\":\"default0/INBOX\", \"id\":\"123\" } ... { \"folder\":\"default0/MyFolder\", \"id\":\"134\" }]",
responseDescription = "An array with object IDs of mails which were modified after the specified timestamp and were therefore not deleted.")
public final class DeleteAction extends AbstractMailAction {

    /**
     * Initializes a new {@link DeleteAction}.
     *
     * @param services
     */
    public DeleteAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final MailRequest req) throws OXException {
        try {
            //final ServerSession session = req.getSession();
            /*
             * Read in parameters
             */
            final boolean hardDelete = "1".equals(req.getParameter(AJAXServlet.PARAMETER_HARDDELETE));
            final JSONArray jsonIDs = (JSONArray) req.getRequest().requireData();
            /*
             * Get mail interface
             */
            final MailServletInterface mailInterface = getMailInterface(req);
            final OXJSONWriter jsonWriter = new OXJSONWriter();
            /*
             * Start response
             */
            jsonWriter.array();
            final int length = jsonIDs.length();
            if (length > 0) {
                final List<MailPath> l = new ArrayList<MailPath>(length);
                for (int i = 0; i < length; i++) {
                    final JSONObject obj = jsonIDs.getJSONObject(i);
                    final FullnameArgument fa = MailFolderUtility.prepareMailFolderParam(obj.getString(AJAXServlet.PARAMETER_FOLDERID));
                    l.add(new MailPath(fa.getAccountId(), fa.getFullname(), obj.getString(AJAXServlet.PARAMETER_ID)));
                }
                Collections.sort(l, MailPath.COMPARATOR);
                String lastFldArg = l.get(0).getFolderArgument();
                final List<String> arr = new ArrayList<String>(length);
                for (int i = 0; i < length; i++) {
                    final MailPath current = l.get(i);
                    final String folderArgument = current.getFolderArgument();
                    if (!lastFldArg.equals(folderArgument)) {
                        /*
                         * Delete all collected UIDs til here and reset
                         */
                        final String[] uids = arr.toArray(new String[arr.size()]);
                        mailInterface.deleteMessages(lastFldArg, uids, hardDelete);
                        arr.clear();
                        lastFldArg = folderArgument;
                    }
                    arr.add(current.getMailID());
                }
                if (arr.size() > 0) {
                    final String[] uids = arr.toArray(new String[arr.size()]);
                    mailInterface.deleteMessages(lastFldArg, uids, hardDelete);
                }
            }
            jsonWriter.endArray();
            return new AJAXRequestResult(jsonWriter.getObject(), "json");
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

}
