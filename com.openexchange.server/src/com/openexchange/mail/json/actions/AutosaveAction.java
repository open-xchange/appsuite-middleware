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
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.upload.impl.UploadEvent;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.mail.compose.CompositionSpace;
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
public final class AutosaveAction extends AbstractMailAction {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AutosaveAction.class);

    /**
     * Initializes a new {@link AutosaveAction}.
     *
     * @param services
     */
    public AutosaveAction(final ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(final MailRequest req) throws OXException {
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
                    LOG.warn("Mail account {} for user {} in context {} has no drafts folder. Saving draft to default account's draft folder.", accountId, session.getUserId(), session.getContextId());
                    // No drafts folder in detected mail account; auto-save to default account
                    accountId = MailAccount.DEFAULT_ID;
                    composedMail.setFolder(mailInterface.getDraftsFolder(accountId));
                }
                msgIdentifier = mailInterface.saveDraft(composedMail, true, accountId);

                if (null != csid) {
                    CompositionSpace space = CompositionSpace.getCompositionSpace(csid, session);
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
        } catch (final RuntimeException e) {
            throw MailExceptionCode.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

}
