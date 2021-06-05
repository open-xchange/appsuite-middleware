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

package com.openexchange.mail.json.actions;

import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
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
@RestrictedAction(module = AbstractMailAction.MODULE, type = RestrictedAction.Type.READ)
public final class GetUpdatesAction extends AbstractMailAction {

    /**
     * Initializes a new {@link GetUpdatesAction}.
     *
     * @param services
     */
    public GetUpdatesAction(ServiceLookup services) {
        super(services);
    }

    private final transient MailFieldWriter WRITER_ID = MessageWriter.getMailFieldWriters(new MailListField[] { MailListField.ID })[0];

    @Override
    protected AJAXRequestResult perform(MailRequest req) throws OXException {
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
                ColumnCollection columnCollection = req.checkColumnsAndHeaders(true);
                int[] columns = columnCollection.getFields();
                int userId = session.getUserId();
                int contextId = session.getContextId();
                MailServletInterface mailInterface = getMailInterface(req);

                columns = prepareColumns(columns);
                if (!bIgnoreModified) {
                    final MailMessage[] modified = mailInterface.getUpdatedMessages(folderId, columns);
                    final MailFieldWriter[] writers = MessageWriter.getMailFieldWriters(MailListField.getFields(columns));
                    for (MailMessage mail : modified) {
                        final JSONArray ja = new JSONArray();
                        if (mail != null) {
                            for (MailFieldWriter writer : writers) {
                                writer.writeField(ja, mail, 0, false, mailInterface.getAccountID(), userId, contextId, timeZone);
                            }
                            jsonWriter.value(ja);
                        }
                    }
                }
                if (!bIgnoreDelete) {
                    final MailMessage[] deleted = mailInterface.getDeletedMessages(folderId, columns);
                    for (MailMessage mail : deleted) {
                        final JSONArray ja = new JSONArray();
                        WRITER_ID.writeField(ja, mail, 0, false, mailInterface.getAccountID(), userId, contextId, timeZone);
                        jsonWriter.value(ja);
                    }
                }
            }
            jsonWriter.endArray();
            return new AJAXRequestResult(jsonWriter.getObject(), "json");
        } catch (RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

}
