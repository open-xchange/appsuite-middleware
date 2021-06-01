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
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.annotation.restricted.RestrictedAction;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.upload.impl.UploadEvent;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.compose.old.OldCompositionSpace;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.dataobjects.compose.ComposedMailMessage;
import com.openexchange.mail.json.MailRequest;
import com.openexchange.mail.json.parser.MessageParser;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AutosaveAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@RestrictedAction(module = AbstractMailAction.MODULE, type = RestrictedAction.Type.WRITE)
public final class AutosaveAction extends AbstractMailAction {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AutosaveAction.class);

    /**
     * Initializes a new {@link AutosaveAction}.
     *
     * @param services
     */
    public AutosaveAction(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(MailRequest req) throws OXException {
        try {
            final ServerSession session = req.getSession();
            String csid = req.getParameter(AJAXServlet.PARAMETER_CSID);
            final MailServletInterface mailInterface = getMailInterface(req);
            MailPath msgIdentifier = null;
            final List<OXException> warnings = new ArrayList<OXException>();
            {
                final JSONObject jsonMailObj = (JSONObject) req.getRequest().requireData();
                if (null == csid) {
                    csid = jsonMailObj.optString("csid", null);
                }
                /*
                 * Parse with default account's transport provider
                 */
                ComposedMailMessage composedMail = MessageParser.parse4Draft(jsonMailObj, (UploadEvent) null, session, MailAccount.DEFAULT_ID, warnings);
                if ((composedMail.getFlags() & MailMessage.FLAG_DRAFT) == 0) {
                    LOG.debug("Missing \\Draft flag on action=autosave in JSON message object");
                    composedMail.setFlag(MailMessage.FLAG_DRAFT, true);
                }
                if ((composedMail.getFlags() & MailMessage.FLAG_DRAFT) != MailMessage.FLAG_DRAFT) {
                    throw MailExceptionCode.UNEXPECTED_ERROR.create("No new message on action=edit");
                }
                // MailPath msgref = composedMail.getMsgref();
                /*
                 * ... and autosave draft
                 */
                int accountId;
                if (composedMail.containsFrom()) {
                    accountId = resolveFrom2Account(session, composedMail.getFrom()[0], false, true);
                } else {
                    accountId = MailAccount.DEFAULT_ID;
                }
                /*
                 * Check if detected account has a drafts folder
                 */
                if (mailInterface.getDraftsFolder(accountId) == null) {
                    if (MailAccount.DEFAULT_ID == accountId) {
                        // Huh... No drafts folder in default account
                        throw MailExceptionCode.FOLDER_NOT_FOUND.create("Drafts");
                    }
                    LOG.warn("Mail account {} for user {} in context {} has no drafts folder. Saving draft to default account's draft folder.", I(accountId), I(session.getUserId()), I(session.getContextId()));
                    // No drafts folder in detected mail account; auto-save to default account
                    accountId = MailAccount.DEFAULT_ID;
                    composedMail.setFolder(mailInterface.getDraftsFolder(accountId));
                }
                msgIdentifier = mailInterface.saveDraft(composedMail, true, accountId);

                if (null != csid) {
                    OldCompositionSpace space = OldCompositionSpace.getCompositionSpace(csid, session);
                    space.addDraftEditFor(msgIdentifier);
                }
            }
            if (msgIdentifier == null) {
                throw MailExceptionCode.DRAFT_FAILED_UNKNOWN.create();
            }
            /*
             * Fill JSON response object
             */
            final AJAXRequestResult result = new AJAXRequestResult(msgIdentifier, "string");
            result.addWarnings(warnings);
            return result;
        } catch (RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        } catch (OXException e) {
            if (MailExceptionCode.COPY_TO_SENT_FOLDER_FAILED_QUOTA.equals(e)) {
                throw MailExceptionCode.UNABLE_TO_SAVE_DRAFT_QUOTA.create();
            }
            throw e;
        }
    }

}
