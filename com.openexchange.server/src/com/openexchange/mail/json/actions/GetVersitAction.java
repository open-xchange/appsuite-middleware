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

import java.util.ArrayList;
import java.util.List;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.fields.DataFields;
import com.openexchange.ajax.fields.FolderChildFields;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.contact.internal.VCardUtil;
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
public final class GetVersitAction extends AbstractMailAction {

    /**
     * Initializes a new {@link GetVersitAction}.
     *
     * @param services
     */
    public GetVersitAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final MailRequest req) throws OXException {
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
            for (final CommonObject current : insertedObjs) {
                jo.reset();
                jo.put(DataFields.ID, current.getObjectID());
                jo.put(FolderChildFields.FOLDER_ID, current.getParentFolderID());
                jsonWriter.value(jo);
            }
            jsonWriter.endArray();
            final AJAXRequestResult data = new AJAXRequestResult(jsonWriter.getObject(), "json");
            return data;
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (final JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        }
    }

}
