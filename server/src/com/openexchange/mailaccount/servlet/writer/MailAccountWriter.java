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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.mailaccount.servlet.writer;

import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.mailaccount.Attribute;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.servlet.fields.MailAccountFields;
import com.openexchange.mailaccount.servlet.fields.MailAccountGetSwitch;

/**
 * {@link MailAccountWriter} - TODO Short description of this class' purpose.
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
     * @param account The mail account to written from mail account's data
     * @return A JSON object filled with
     * @throws JSONException If writing JSON fails
     */
    public static JSONObject write(final MailAccount account) throws JSONException {
        final JSONObject json = new JSONObject();
        json.put(MailAccountFields.ID, account.getId());
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
        json.put(MailAccountFields.SPAM_HANDLER, account.getSpamHandler());
        // Folder names
        json.put(MailAccountFields.TRASH, account.getTrash());
        json.put(MailAccountFields.SENT, account.getSent());
        json.put(MailAccountFields.DRAFTS, account.getDrafts());
        json.put(MailAccountFields.SPAM, account.getSpam());
        json.put(MailAccountFields.CONFIRMED_SPAM, account.getConfirmedSpam());
        json.put(MailAccountFields.CONFIRMED_HAM, account.getConfirmedHam());
        // Folder fullnames
        json.put(MailAccountFields.TRASH_FULLNAME, account.getTrashFullname());
        json.put(MailAccountFields.SENT_FULLNAME, account.getSentFullname());
        json.put(MailAccountFields.DRAFTS_FULLNAME, account.getDraftsFullname());
        json.put(MailAccountFields.SPAM_FULLNAME, account.getSpamFullname());
        json.put(MailAccountFields.CONFIRMED_SPAM_FULLNAME, account.getConfirmedSpamFullname());
        json.put(MailAccountFields.CONFIRMED_HAM_FULLNAME, account.getConfirmedHamFullname());
        // Unified INBOX enabled
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

    public static JSONArray writeArray(final MailAccount[] userMailAccounts, final List<Attribute> attributes) {
        final JSONArray rows = new JSONArray();
        for (final MailAccount account : userMailAccounts) {
            final MailAccountGetSwitch getter = new MailAccountGetSwitch(account);
            final JSONArray row = new JSONArray();
            for (final Attribute attribute : attributes) {
                if (Attribute.PASSWORD_LITERAL == attribute || Attribute.TRANSPORT_PASSWORD_LITERAL == attribute) {
                    row.put(JSONObject.NULL);
                } else if (Attribute.POP3_DELETE_WRITE_THROUGH_LITERAL == attribute || Attribute.POP3_EXPUNGE_ON_QUIT_LITERAL == attribute) {
                	row.put(Boolean.parseBoolean(String.valueOf(attribute.doSwitch(getter))));
                } else {
                    row.put(attribute.doSwitch(getter));
                }
            }
            rows.put(row);
        }
        return rows;
    }
}
