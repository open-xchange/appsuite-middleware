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

package com.openexchange.mailaccount.json.actions;

import static com.openexchange.tools.sql.DBUtils.closeSQLStuff;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.databaseold.Database;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.jslob.DefaultJSlob;
import com.openexchange.jslob.JSlobId;
import com.openexchange.mail.MailSessionCache;
import com.openexchange.mail.api.MailAccess;
import com.openexchange.mail.mime.MimeMailExceptionCode;
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
public final class UpdateAction extends AbstractMailAccountAction implements MailAccountFields {

    private static final org.apache.commons.logging.Log LOG = com.openexchange.log.Log.valueOf(com.openexchange.log.LogFactory.getLog(UpdateAction.class));

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
    protected AJAXRequestResult innerPerform(final AJAXRequestData requestData, final ServerSession session, final JSONValue jData) throws OXException, JSONException {
        final MailAccountDescription accountDescription = new MailAccountDescription();
        final List<OXException> warnings = new LinkedList<OXException>();
        final Set<Attribute> fieldsToUpdate = MailAccountParser.getInstance().parse(accountDescription, jData.toObject(), warnings);

        final Set<Attribute> notAllowed = new HashSet<Attribute>(fieldsToUpdate);
        notAllowed.removeAll(WEBMAIL_ALLOWED);
        final int contextId = session.getContextId();
        if (!session.getUserConfiguration().isMultipleMailAccounts() && (!isDefaultMailAccount(accountDescription) || (!notAllowed.isEmpty()))) {
            throw
                MailAccountExceptionCodes.NOT_ENABLED.create(
                Integer.valueOf(session.getUserId()),
                Integer.valueOf(contextId));
        }

        final MailAccountStorageService storageService =
            ServerServiceRegistry.getInstance().getService(MailAccountStorageService.class, true);

        final int id = accountDescription.getId();
        if (-1 == id) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create( MailAccountFields.ID);
        }

        final MailAccount toUpdate = storageService.getMailAccount(id, session.getUserId(), contextId);
        if (isUnifiedINBOXAccount(toUpdate)) {
            // Treat as no hit
            throw MailAccountExceptionCodes.NOT_FOUND.create(
                Integer.valueOf(id),
                Integer.valueOf(session.getUserId()),
                Integer.valueOf(contextId));
        }

        boolean clearStamp = false;
        {
            // Don't check for POP3 account due to access restrictions (login only allowed every n minutes)
            final boolean pop3 = accountDescription.getMailProtocol().toLowerCase(Locale.ENGLISH).startsWith("pop3");
            if (fieldsToUpdate.contains(Attribute.MAIL_URL_LITERAL) && !toUpdate.generateMailServerURL().equals(accountDescription.generateMailServerURL())) {
                if (!pop3 && !ValidateAction.checkMailServerURL(accountDescription, session, warnings)) {
                    final OXException warning = MimeMailExceptionCode.CONNECT_ERROR.create(accountDescription.getMailServer(), accountDescription.getLogin());
                    warning.setCategory(Category.CATEGORY_WARNING);
                    warnings.add(0, warning);
                }
                clearStamp |= (pop3 && !toUpdate.getMailServer().equals(accountDescription.getMailServer()));
            }
            if (fieldsToUpdate.contains(Attribute.TRANSPORT_URL_LITERAL) && !toUpdate.generateTransportServerURL().equals(accountDescription.generateTransportServerURL())) {
                if (!pop3 && !ValidateAction.checkTransportServerURL(accountDescription, session, warnings)) {
                    final String transportLogin = accountDescription.getTransportLogin();
                    final OXException warning = MimeMailExceptionCode.CONNECT_ERROR.create(accountDescription.getTransportServer(), transportLogin == null ? accountDescription.getLogin() : transportLogin);
                    warning.setCategory(Category.CATEGORY_WARNING);
                    warnings.add(0, warning);
                }
                clearStamp |= (pop3 && !toUpdate.getTransportServer().equals(accountDescription.getTransportServer()));
            }
        }

        storageService.updateMailAccount(
            accountDescription,
            fieldsToUpdate,
            session.getUserId(),
            contextId,
            session);

        {
            final JSONObject jBody = jData.toObject();
            if (jBody.hasAndNotNull(META)) {
                final JSONObject jMeta = jBody.optJSONObject(META);
                getStorage().store(new JSlobId(JSLOB_SERVICE_ID, Integer.toString(id), session.getUserId(), session.getContextId()), new DefaultJSlob(jMeta));
            }
        }

        if (clearStamp) {
            final Connection con = Database.get(contextId, true);
            PreparedStatement stmt = null;
            try {
                // Delete possibly existing mapping
                stmt = con.prepareStatement("DELETE FROM user_mail_account_properties WHERE cid = ? AND user = ? AND id = ? AND name = ?");
                int pos = 1;
                stmt.setInt(pos++, contextId);
                stmt.setInt(pos++, session.getUserId());
                stmt.setInt(pos++, id);
                stmt.setString(pos++, "pop3.lastaccess");
                stmt.executeUpdate();
            } catch (final SQLException e) {
                throw MailAccountExceptionCodes.SQL_ERROR.create(e, e.getMessage());
            } finally {
                closeSQLStuff(null, stmt);
                Database.back(contextId, true, con);
            }
        }

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
                MailAccess<?, ?> mailAccess = null;
                try {
                    mailAccess = MailAccess.getInstance(session, id);
                    mailAccess.connect(false);
                    mailAccess.getFolderStorage().checkDefaultFolders();
                } finally {
                    if (null != mailAccess) {
                        mailAccess.close(true);
                    }
                }
            } catch (final OXException e) {
                LOG.warn(e.getMessage(), e);
            }
        }

        final JSONObject jsonAccount =
            MailAccountWriter.write(storageService.getMailAccount(id, session.getUserId(), contextId));

        return new AJAXRequestResult(jsonAccount).addWarnings(warnings);
    }

}
