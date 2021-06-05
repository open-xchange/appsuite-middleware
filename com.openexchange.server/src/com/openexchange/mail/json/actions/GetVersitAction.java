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

import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.contact.vcard.VCardUtil;
import com.openexchange.data.conversion.ical.internal.ICalUtil;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.CommonObject;
import com.openexchange.json.OXJSONWriter;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.dataobjects.MailPart;
import com.openexchange.mail.json.MailRequest;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link GetVersitAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@RestrictedAction(module = AbstractMailAction.MODULE, type = RestrictedAction.Type.WRITE)
public final class GetVersitAction extends AbstractMailAction {

    /**
     * Initializes a new {@link GetVersitAction}.
     *
     * @param services
     */
    public GetVersitAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(MailRequest req) throws OXException {
        try {
            final ServerSession session = req.getSession();
            /*
             * Read in parameters
             */
            final String folderPath = req.checkParameter(AJAXServlet.PARAMETER_FOLDERID);
            final String uid = req.checkParameter(AJAXServlet.PARAMETER_ID);
            // final String msgUID =
            // paramContainer.checkStringParam(PARAMETER_ID);
            final String partIdentifier = req.checkParameter(Mail.PARAMETER_MAILATTCHMENT);
            /*
             * Get mail interface
             */
            final MailServletInterface mailInterface = getMailInterface(req);
            final CommonObject[] insertedObjs;
            {
                final MailPart versitPart = mailInterface.getMessageAttachment(folderPath, uid, partIdentifier, false);
                /*
                 * Save dependent on content type
                 */
                final List<CommonObject> retvalList = new ArrayList<CommonObject>();
                if (versitPart.getContentType().isMimeType(MimeTypes.MIME_TEXT_X_VCARD) || versitPart.getContentType().isMimeType(
                    MimeTypes.MIME_TEXT_VCARD)) {
                    /*
                     * Save VCard
                     */
                    retvalList.add(VCardUtil.importContactToDefaultFolder(versitPart.getInputStream(), session));
                } else if (versitPart.getContentType().isMimeType(MimeTypes.MIME_TEXT_X_VCALENDAR) || versitPart.getContentType().isMimeType(
                    MimeTypes.MIME_TEXT_CALENDAR)) {
                    /*
                     * Save ICalendar
                     */
                    retvalList.addAll(ICalUtil.importToDefaultFolder(versitPart.getInputStream(), session));
                } else {
                    throw MailExceptionCode.UNSUPPORTED_VERSIT_ATTACHMENT.create(versitPart.getContentType());
                }
                insertedObjs = retvalList.toArray(new CommonObject[retvalList.size()]);
            }
            final OXJSONWriter jsonWriter = new OXJSONWriter();
            jsonWriter.array();
            final JSONObject jo = new JSONObject();
            for (CommonObject current : insertedObjs) {
                jo.reset();
                jo.put(DataFields.ID, current.getObjectID());
                jo.put(FolderChildFields.FOLDER_ID, current.getParentFolderID());
                jsonWriter.value(jo);
            }
            jsonWriter.endArray();
            final AJAXRequestResult data = new AJAXRequestResult(jsonWriter.getObject(), "json");
            return data;
        } catch (RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

}
