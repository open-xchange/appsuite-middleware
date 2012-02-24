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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.mailaccount.json.actions;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailSessionCache;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mailaccount.Attribute;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountDescription;
import com.openexchange.mailaccount.MailAccountExceptionCodes;
import com.openexchange.mailaccount.MailAccountStorageService;
import com.openexchange.mailaccount.json.fields.MailAccountFields;
import com.openexchange.mailaccount.json.parser.MailAccountParser;
import com.openexchange.mailaccount.json.writer.MailAccountWriter;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link UpdateAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@Action(method = RequestMethod.PUT, name = "update", description = "Update a mail account", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module.") 
}, requestBody = "A JSON object identifiying (field ID is present) and describing the account to update. See mail account data.",
responseDescription = "A JSON object representing the updated mail account. See mail account data.")
public final class UpdateAction extends AbstractMailAccountAction {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(org.apache.commons.logging.LogFactory.getLog(UpdateAction.class));

    public static final String ACTION = AJAXServlet.ACTION_UPDATE;

    /**
     * Initializes a new {@link UpdateAction}.
     */
    public UpdateAction() {
        super();
    }

    private static final EnumSet<Attribute> DEFAULT =
        EnumSet.of(
            Attribute.CONFIRMED_HAM_FULLNAME_LITERAL,
            Attribute.CONFIRMED_HAM_LITERAL,
            Attribute.CONFIRMED_SPAM_FULLNAME_LITERAL,
            Attribute.CONFIRMED_SPAM_LITERAL,
            Attribute.DRAFTS_FULLNAME_LITERAL,
            Attribute.DRAFTS_LITERAL,
            Attribute.SENT_FULLNAME_LITERAL,
            Attribute.SENT_LITERAL,
            Attribute.SPAM_FULLNAME_LITERAL,
            Attribute.SPAM_LITERAL,
            Attribute.TRASH_FULLNAME_LITERAL,
            Attribute.TRASH_LITERAL);

    private static final Set<Attribute> WEBMAIL_ALLOWED = EnumSet.of(
        Attribute.ID_LITERAL,
        Attribute.PERSONAL_LITERAL,
        Attribute.REPLY_TO_LITERAL,
        Attribute.UNIFIED_INBOX_ENABLED_LITERAL);

    @Override
    public AJAXRequestResult perform(final AJAXRequestData requestData, final ServerSession session) throws OXException {
        final JSONObject jData = (JSONObject) requestData.getData();

        try {
            final MailAccountDescription accountDescription = new MailAccountDescription();
            final Set<Attribute> fieldsToUpdate = new MailAccountParser().parse(accountDescription, jData);

            final Set<Attribute> notAllowed = new HashSet<Attribute>(fieldsToUpdate);
            notAllowed.removeAll(WEBMAIL_ALLOWED);
            if (!session.getUserConfiguration().isMultipleMailAccounts() && (!isDefaultMailAccount(accountDescription) || (!notAllowed.isEmpty()))) {
                throw
                    MailAccountExceptionCodes.NOT_ENABLED.create(
                    Integer.valueOf(session.getUserId()),
                    Integer.valueOf(session.getContextId()));
            }

            final MailAccountStorageService storageService =
                ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class, true);

            final int id = accountDescription.getId();
            if (-1 == id) {
                throw AjaxExceptionCodes.MISSING_PARAMETER.create( MailAccountFields.ID);
            }

            final MailAccount toUpdate = storageService.getMailAccount(id, session.getUserId(), session.getContextId());
            if (isUnifiedINBOXAccount(toUpdate)) {
                // Treat as no hit
                throw MailAccountExceptionCodes.NOT_FOUND.create(
                    Integer.valueOf(id),
                    Integer.valueOf(session.getUserId()),
                    Integer.valueOf(session.getContextId()));
            }

            storageService.updateMailAccount(
                accountDescription,
                fieldsToUpdate,
                session.getUserId(),
                session.getContextId(),
                session);

            if (fieldsToUpdate.removeAll(DEFAULT)) {
                /*
                 * Drop all session parameters related to default folders for this account
                 */
                MailSessionCache.getInstance(session).removeAccountParameters(id);
                /*-
                 *
                session.setParameter(MailSessionParameterNames.getParamDefaultFolderArray(id), null);
                session.setParameter(MailSessionParameterNames.getParamDefaultFolderChecked(id), null);
                 */
                /*
                 * Re-Init account's default folders
                 */
                try {
                    final MailAccess<?, ?> mailAccess = MailAccess.getInstance(session, id);
                    mailAccess.connect(false);
                    try {
                        mailAccess.getFolderStorage().checkDefaultFolders();
                    } finally {
                        mailAccess.close(true);
                    }
                } catch (final OXException e) {
                    LOG.warn(e.getMessage(), e);
                }
            }

            final JSONObject jsonAccount =
                MailAccountWriter.write(storageService.getMailAccount(id, session.getUserId(), session.getContextId()));

            return new AJAXRequestResult(jsonAccount);
        } catch (final JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create( e, e.getMessage());
        }
    }

}
