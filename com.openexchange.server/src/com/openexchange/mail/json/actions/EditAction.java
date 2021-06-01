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

import static com.openexchange.java.Autoboxing.I;
import java.util.ArrayList;
import java.util.List;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.upload.impl.UploadEvent;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailJSONField;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.compose.old.OldCompositionSpace;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.json.MailRequest;
import com.openexchange.mail.json.parser.MessageParser;
import com.openexchange.mail.mime.MimeMailException;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link EditAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@RestrictedAction(module = AbstractMailAction.MODULE, type = RestrictedAction.Type.WRITE)
public final class EditAction extends AbstractMailAction {

    private static final org.slf4j.Logger LOG =
        org.slf4j.LoggerFactory.getLogger(EditAction.class);

    /**
     * Initializes a new {@link EditAction}.
     * @param services
     */
    public EditAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(MailRequest req) throws OXException {
        AJAXRequestData request = req.getRequest();
        List<OXException> warnings = new ArrayList<OXException>();

        try {
            ServerSession session = req.getSession();
            UserSettingMail usm = session.getUserSettingMail();
            long maxFileSize = usm.getUploadQuotaPerFile();
            if (maxFileSize <= 0) {
                maxFileSize = -1L;
            }
            long maxSize = usm.getUploadQuota();
            if (maxSize <= 0) {
                maxSize = -1L;
            }
            if (!request.hasUploads(maxFileSize, maxSize)) {
                throw AjaxExceptionCodes.UNKNOWN_ACTION.create("edit");
            }
            String csid = req.getParameter(AJAXServlet.PARAMETER_CSID);
            UploadEvent uploadEvent = request.getUploadEvent();
            /*
             * Edit draft
             */
            MailPath msgIdentifier = null;
            {
                JSONObject jsonMailObj = new JSONObject(uploadEvent.getFormField(AJAXServlet.UPLOAD_FORMFIELD_MAIL));
                if (null == csid) {
                    csid = jsonMailObj.optString("csid", null);
                }
                /*
                 * Resolve "From" to proper mail account
                 */
                InternetAddress from;
                try {
                    from = MessageParser.getFromField(jsonMailObj)[0];
                } catch (AddressException e) {
                    throw MimeMailException.handleMessagingException(e);
                }
                int accountId = resolveFrom2Account(session, from, false, true);
                /*
                 * Check if detected account has drafts
                 */
                MailServletInterface msi = getMailInterface(req);
                if (msi.getDraftsFolder(accountId) == null) {
                    if (MailAccount.DEFAULT_ID == accountId) {
                        // Huh... No drafts folder in default account
                        throw MailExceptionCode.FOLDER_NOT_FOUND.create("Drafts");
                    }
                    LOG.warn("Mail account {} for user {} in context {} has no drafts folder. Saving draft to default account's draft folder.", I(accountId), I(session.getUserId()), I(session.getContextId()));
                    // No drafts folder in detected mail account; auto-save to default account
                    accountId = MailAccount.DEFAULT_ID;
                }

                if (!jsonMailObj.hasAndNotNull(MailJSONField.FLAGS.getKey()) || (jsonMailObj.getInt(MailJSONField.FLAGS.getKey()) & MailMessage.FLAG_DRAFT) <= 0) {
                    throw MailExceptionCode.UNEXPECTED_ERROR.create("No new message on action=edit");
                }

                /*
                 * Parse with default account's transport provider
                 */
                ComposedMailMessage composedMail = MessageParser.parse4Draft(jsonMailObj, uploadEvent, session, MailAccount.DEFAULT_ID, warnings);
                MailPath msgref = composedMail.getMsgref();
                /*
                 * ... and edit draft
                 */
                msgIdentifier = msi.saveDraft(composedMail, false, accountId);

                if (null != csid && null != msgref) {
                    OldCompositionSpace space = OldCompositionSpace.getCompositionSpace(csid, session);
                    space.addCleanUp(msgref);
                }
            }
            if (msgIdentifier == null) {
                throw MailExceptionCode.SEND_FAILED_UNKNOWN.create();
            }
            /*
             * Create JSON response object
             */
            AJAXRequestResult result = new AJAXRequestResult(msgIdentifier, "string");
            result.addWarnings(warnings);
            return result;
        } catch (OXException e) {
            final String uid = getUidFromException(e);
            if (MailExceptionCode.MAIL_NOT_FOUND.equals(e) && "undefined".equalsIgnoreCase(uid)) {
                throw MailExceptionCode.PROCESSING_ERROR.create(e, new Object[0]);
            }
            throw e;
        } catch (JSONException e) {
            throw MailExceptionCode.JSON_ERROR.create(e, e.getMessage());
        } catch (RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

}
