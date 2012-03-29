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

package com.openexchange.mailaccount.json.writer;

import static com.openexchange.mail.utils.MailFolderUtility.prepareFullname;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.exception.OXException;
import com.openexchange.mailaccount.Attribute;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.json.fields.MailAccountFields;
import com.openexchange.mailaccount.json.fields.MailAccountGetSwitch;

/**
 * {@link MailAccountWriter} - Writes mail account as JSON data.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailAccountWriter {

    private MailAccountWriter() {
        super();
    }

    /**
     * Writes specified mail account to a JSON object.
     *
     * @param account The mail account to write
     * @return A JSON object filled with
     * @throws JSONException If writing JSON fails
     */
    public static JSONObject write(final MailAccount account) throws JSONException {
        final JSONObject json = new JSONObject();
        final int accountId = account.getId();
        json.put(MailAccountFields.ID, accountId);
        json.put(MailAccountFields.LOGIN, account.getLogin());
        // json.put(MailAccountFields.PASSWORD, account.getLogin());
        json.put(MailAccountFields.MAIL_PORT, account.getMailPort());
        json.put(MailAccountFields.MAIL_PROTOCOL, account.getMailProtocol());
        json.put(MailAccountFields.MAIL_SECURE, account.isMailSecure());
        json.put(MailAccountFields.MAIL_SERVER, account.getMailServer());
        json.put(MailAccountFields.MAIL_URL, account.generateMailServerURL());

        json.put(MailAccountFields.TRANSPORT_PORT, account.getTransportPort());
        json.put(MailAccountFields.TRANSPORT_PROTOCOL, account.getTransportProtocol());
        json.put(MailAccountFields.TRANSPORT_SECURE, account.isTransportSecure());
        json.put(MailAccountFields.TRANSPORT_SERVER, account.getTransportServer());
        json.put(MailAccountFields.TRANSPORT_URL, account.generateTransportServerURL());

        json.put(MailAccountFields.TRANSPORT_LOGIN, account.getTransportLogin());
        // json.put(MailAccountFields.TRANSPORT_PASSWORD, account.getTransportPassword());

        json.put(MailAccountFields.NAME, account.getName());
        json.put(MailAccountFields.PRIMARY_ADDRESS, account.getPrimaryAddress());
        json.put(MailAccountFields.PERSONAL, account.getPersonal());
        json.put(MailAccountFields.SPAM_HANDLER, account.getSpamHandler());
        // Folder names
        json.put(MailAccountFields.TRASH, account.getTrash());
        json.put(MailAccountFields.SENT, account.getSent());
        json.put(MailAccountFields.DRAFTS, account.getDrafts());
        json.put(MailAccountFields.SPAM, account.getSpam());
        json.put(MailAccountFields.CONFIRMED_SPAM, account.getConfirmedSpam());
        json.put(MailAccountFields.CONFIRMED_HAM, account.getConfirmedHam());
        // Folder full names
        json.put(MailAccountFields.TRASH_FULLNAME, prepareFullname(accountId, account.getTrashFullname()));
        json.put(MailAccountFields.SENT_FULLNAME, prepareFullname(accountId, account.getSentFullname()));
        json.put(MailAccountFields.DRAFTS_FULLNAME, prepareFullname(accountId, account.getDraftsFullname()));
        json.put(MailAccountFields.SPAM_FULLNAME, prepareFullname(accountId, account.getSpamFullname()));
        json.put(MailAccountFields.CONFIRMED_SPAM_FULLNAME, prepareFullname(accountId, account.getConfirmedSpamFullname()));
        json.put(MailAccountFields.CONFIRMED_HAM_FULLNAME, prepareFullname(accountId, account.getConfirmedHamFullname()));
        // Unified Mail enabled
        json.put(MailAccountFields.UNIFIED_INBOX_ENABLED, account.isUnifiedINBOXEnabled());
        // Properties
        final Map<String, String> props = account.getProperties();
        if (props.containsKey("pop3.deletewt")) {
            json.put(MailAccountFields.POP3_DELETE_WRITE_THROUGH, Boolean.parseBoolean(props.get("pop3.deletewt")));
        }
        if (props.containsKey("pop3.expunge")) {
            json.put(MailAccountFields.POP3_EXPUNGE_ON_QUIT, Boolean.parseBoolean(props.get("pop3.expunge")));
        }
        if (props.containsKey("pop3.refreshrate")) {
            json.put(MailAccountFields.POP3_REFRESH_RATE, props.get("pop3.refreshrate"));
        }
        if (props.containsKey("pop3.storage")) {
            json.put(MailAccountFields.POP3_STORAGE, props.get("pop3.storage"));
        }
        if (props.containsKey("pop3.path")) {
            json.put(MailAccountFields.POP3_PATH, props.get("pop3.path"));
        }
        return json;
    }

    private static final EnumSet<Attribute> FULL_NAMES = EnumSet.of(
        Attribute.TRASH_FULLNAME_LITERAL,
        Attribute.SENT_FULLNAME_LITERAL,
        Attribute.DRAFTS_FULLNAME_LITERAL,
        Attribute.SPAM_FULLNAME_LITERAL,
        Attribute.CONFIRMED_HAM_FULLNAME_LITERAL,
        Attribute.CONFIRMED_SPAM_FULLNAME_LITERAL);

    /**
     * Writes specified attributes for each mail account contained in given array in an own JSON array surrounded by a super JSON array.
     *
     * @param mailAccounts The mail accounts
     * @param attributes The attributes
     * @return A JSON array of JSON arrays for each account
     * @throws OXException If writing JSON fails
     */
    public static JSONArray writeArray(final MailAccount[] mailAccounts, final List<Attribute> attributes) throws OXException {
        final JSONArray rows = new JSONArray();
        for (final MailAccount account : mailAccounts) {
            final MailAccountGetSwitch getter = new MailAccountGetSwitch(account);
            final JSONArray row = new JSONArray();
            for (final Attribute attribute : attributes) {
                if (Attribute.PASSWORD_LITERAL == attribute || Attribute.TRANSPORT_PASSWORD_LITERAL == attribute) {
                    row.put(JSONObject.NULL);
                } else if (Attribute.POP3_DELETE_WRITE_THROUGH_LITERAL == attribute || Attribute.POP3_EXPUNGE_ON_QUIT_LITERAL == attribute) {
                	row.put(Boolean.parseBoolean(String.valueOf(attribute.doSwitch(getter))));
                } else if (FULL_NAMES.contains(attribute)) {
                    final Object value = attribute.doSwitch(getter);
                    if (null == value) {
                        row.put(JSONObject.NULL);
                    } else {
                        row.put(prepareFullname(account.getId(), value.toString()));
                    }
                } else {
                    final Object value  = attribute.doSwitch(getter);
                    row.put(value == null ? JSONObject.NULL : value);
                }
            }
            rows.put(row);
        }
        return rows;
    }
}
