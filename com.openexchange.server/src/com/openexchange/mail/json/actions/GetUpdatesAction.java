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

import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.json.OXJSONWriter;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailListField;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.json.MailRequest;
import com.openexchange.mail.json.utils.ColumnCollection;
import com.openexchange.mail.json.writer.MessageWriter;
import com.openexchange.mail.json.writer.MessageWriter.MailFieldWriter;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.TimeZoneUtils;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link GetUpdatesAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@Action(method = RequestMethod.GET, name = "updates", description = "Get updated mails", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module.")
}, responseDescription = "Just an empty JSON array is going to be returned since this action cannot be applied to IMAP.")
public final class GetUpdatesAction extends AbstractMailAction {

    /**
     * Initializes a new {@link GetUpdatesAction}.
     *
     * @param services
     */
    public GetUpdatesAction(final ServiceLookup services) {
        super(services);
    }

    private final transient MailFieldWriter WRITER_ID = MessageWriter.getMailFieldWriters(new MailListField[] { MailListField.ID })[0];

    @Override
    protected AJAXRequestResult perform(final MailRequest req) throws OXException {
        try {
            final ServerSession session = req.getSession();
            /*
             * Read in parameters
             */
            final String folderId = req.checkParameter(Mail.PARAMETER_MAILFOLDER);
            final String ignore = req.getParameter(AJAXServlet.PARAMETER_IGNORE);
            String tmp = req.getParameter(Mail.PARAMETER_TIMEZONE);
            final TimeZone timeZone = isEmpty(tmp) ? null : TimeZoneUtils.getTimeZone(tmp.trim());
            tmp = null;
            boolean bIgnoreDelete = false;
            boolean bIgnoreModified = false;
            if (ignore != null && ignore.indexOf("deleted") != -1) {
                bIgnoreDelete = true;
            }
            if (ignore != null && ignore.indexOf("changed") != -1) {
                bIgnoreModified = true;
            }
            final OXJSONWriter jsonWriter = new OXJSONWriter();
            jsonWriter.array();
            if (!bIgnoreModified || !bIgnoreDelete) {
                ColumnCollection columnCollection = req.checkColumnsAndHeaders();
                int[] columns = columnCollection.getFields();
                String[] headers = columnCollection.getHeaders();
                int userId = session.getUserId();
                int contextId = session.getContextId();
                MailServletInterface mailInterface = getMailInterface(req);

                columns = prepareColumns(columns);
                if (!bIgnoreModified) {
                    final MailMessage[] modified = mailInterface.getUpdatedMessages(folderId, columns);
                    final MailFieldWriter[] writers = MessageWriter.getMailFieldWriters(MailListField.getFields(columns));
                    for (final MailMessage mail : modified) {
                        final JSONArray ja = new JSONArray();
                        if (mail != null) {
                            for (final MailFieldWriter writer : writers) {
                                writer.writeField(ja, mail, 0, false, mailInterface.getAccountID(), userId, contextId, timeZone);
                            }
                            jsonWriter.value(ja);
                        }
                    }
                }
                if (!bIgnoreDelete) {
                    final MailMessage[] deleted = mailInterface.getDeletedMessages(folderId, columns);
                    for (final MailMessage mail : deleted) {
                        final JSONArray ja = new JSONArray();
                        WRITER_ID.writeField(ja, mail, 0, false, mailInterface.getAccountID(), userId, contextId, timeZone);
                        jsonWriter.value(ja);
                    }
                }
            }
            jsonWriter.endArray();
            return new AJAXRequestResult(jsonWriter.getObject(), "json");
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

}
