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

package com.openexchange.mailaccount.json.writer;

import static com.openexchange.mail.utils.MailFolderUtility.prepareFullname;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;
import com.openexchange.jslob.JSlob;
import com.openexchange.jslob.JSlobId;
import com.openexchange.jslob.storage.JSlobStorage;
import com.openexchange.mail.mime.QuotedInternetAddress;
import com.openexchange.mail.mime.utils.MimeMessageUtility;
import com.openexchange.mailaccount.Attribute;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.mailaccount.MailAccountExceptionCodes;
import com.openexchange.mailaccount.TransportAccount;
import com.openexchange.mailaccount.TransportAuth;
import com.openexchange.mailaccount.json.MailAccountFields;
import com.openexchange.mailaccount.json.actions.AbstractMailAccountAction;
import com.openexchange.mailaccount.json.fields.MailAccountGetSwitch;
import com.openexchange.mailaccount.json.fields.TransportAccountGetSwitch;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;

/**
 * {@link DefaultMailAccountWriter} - Writes mail account as JSON data.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class DefaultMailAccountWriter implements MailAccountFields {

    private static final EnumSet<Attribute> HIDDEN_FOR_DEFAULT = EnumSet.of(
        Attribute.MAIL_PORT_LITERAL,
        Attribute.MAIL_PROTOCOL_LITERAL,
        Attribute.MAIL_SECURE_LITERAL,
        Attribute.MAIL_SERVER_LITERAL,
        Attribute.MAIL_URL_LITERAL,

        Attribute.PASSWORD_LITERAL,
        Attribute.LOGIN_LITERAL,

        Attribute.POP3_DELETE_WRITE_THROUGH_LITERAL,
        Attribute.POP3_EXPUNGE_ON_QUIT_LITERAL,
        Attribute.POP3_PATH_LITERAL,
        Attribute.POP3_REFRESH_RATE_LITERAL,
        Attribute.POP3_STORAGE_LITERAL,

        Attribute.TRANSPORT_LOGIN_LITERAL,
        Attribute.TRANSPORT_PASSWORD_LITERAL,
        Attribute.TRANSPORT_PORT_LITERAL,
        Attribute.TRANSPORT_PROTOCOL_LITERAL,
        Attribute.TRANSPORT_SECURE_LITERAL,
        Attribute.TRANSPORT_SERVER_LITERAL,
        Attribute.TRANSPORT_URL_LITERAL,
        Attribute.TRANSPORT_AUTH_LITERAL);

    private static volatile Boolean hideDetailsForDefaultAccount;
    private static boolean hideDetailsForDefaultAccount() {
        Boolean tmp = hideDetailsForDefaultAccount;
        if (null == tmp) {
            synchronized (DefaultMailAccountWriter.class) {
                tmp = hideDetailsForDefaultAccount;
                if (null == tmp) {
                    final ConfigurationService service = ServerServiceRegistry.getInstance().getService(ConfigurationService.class);
                    tmp = Boolean.valueOf(null != service && service.getBoolProperty("com.openexchange.mail.hideDetailsForDefaultAccount", false));
                    hideDetailsForDefaultAccount = tmp;
                }
            }
        }
        return tmp.booleanValue();
    }

    private DefaultMailAccountWriter() {
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
        return write(account, hideDetailsForDefaultAccount());
    }

    /**
     * Writes specified mail account to a JSON object.
     *
     * @param account The mail account to write
     * @return A JSON object filled with
     * @throws JSONException If writing JSON fails
     */
    public static JSONObject write(final MailAccount account, final boolean hideDetailsForDefaultAccount) throws JSONException {
        if (null == account) {
            return null;
        }
        final int accountId = account.getId();
        final boolean hideForDefault = MailAccount.DEFAULT_ID == accountId && hideDetailsForDefaultAccount;
        final JSONObject json;
        if (hideForDefault) {
            json = new JSONObject(24);
            json.put(ID, accountId);
            json.put(NAME, account.getName());
            json.put(PRIMARY_ADDRESS, addr2String(account.getPrimaryAddress()));
            json.put(PERSONAL, account.getPersonal());
            json.put(SPAM_HANDLER, account.getSpamHandler());

            // Folder names
            json.put(TRASH, account.getTrash());
            json.put(SENT, account.getSent());
            json.put(DRAFTS, account.getDrafts());
            json.put(SPAM, account.getSpam());
            json.put(CONFIRMED_SPAM, account.getConfirmedSpam());
            json.put(CONFIRMED_HAM, account.getConfirmedHam());
            json.put(ARCHIVE, account.getArchive());

            // Folder full names
            json.put(TRASH_FULLNAME, prepareFullname(accountId, account.getTrashFullname()));
            json.put(SENT_FULLNAME, prepareFullname(accountId, account.getSentFullname()));
            json.put(DRAFTS_FULLNAME, prepareFullname(accountId, account.getDraftsFullname()));
            json.put(SPAM_FULLNAME, prepareFullname(accountId, account.getSpamFullname()));
            json.put(CONFIRMED_SPAM_FULLNAME, prepareFullname(accountId, account.getConfirmedSpamFullname()));
            json.put(CONFIRMED_HAM_FULLNAME, prepareFullname(accountId, account.getConfirmedHamFullname()));
            json.put(ARCHIVE_FULLNAME, prepareFullname(accountId, account.getArchiveFullname()));

            // Unified Mail enabled
            json.put(UNIFIED_INBOX_ENABLED, account.isUnifiedINBOXEnabled());
            // Properties
            final Map<String, String> props = account.getProperties();
            // Reply-to
            {
                final String replyTo = account.getReplyTo();
                if (null == replyTo) {
                    if (props.containsKey("replyto")) {
                        json.put(MailAccountFields.REPLY_TO, props.get("replyto"));
                    }
                } else {
                    json.put(REPLY_TO, replyTo);
                }
            }
            if (props.containsKey(ADDRESSES)) {
                json.put(ADDRESSES, props.get(ADDRESSES));
            }
        } else {
            json = new JSONObject(48);
            json.put(ID, accountId);

            json.put(LOGIN, account.getLogin());
            // json.put(PASSWORD, account.getLogin());

            json.put(MAIL_PORT, account.getMailPort());
            json.put(MAIL_PROTOCOL, account.getMailProtocol());
            json.put(MAIL_SECURE, account.isMailSecure());
            json.put(MAIL_SERVER, account.getMailServer());
            json.put(MAIL_URL, account.generateMailServerURL());
            json.put(MAIL_STARTTLS, account.isMailStartTls());

            {
                TransportAuth transportAuth = account.getTransportAuth();
                if (null != transportAuth) {
                    json.put(TRANSPORT_AUTH, transportAuth.getId());
                }
            }
            json.put(TRANSPORT_PORT, account.getTransportPort());
            json.put(TRANSPORT_PROTOCOL, account.getTransportProtocol());
            json.put(TRANSPORT_SECURE, account.isTransportSecure());
            json.put(TRANSPORT_SERVER, account.getTransportServer());
            json.put(TRANSPORT_URL, account.generateTransportServerURL());
            json.put(TRANSPORT_STARTTLS, account.isTransportStartTls());

            json.put(TRANSPORT_LOGIN, account.getTransportLogin());
            // json.put(TRANSPORT_PASSWORD, account.getTransportPassword());

            json.put(NAME, account.getName());
            json.put(PRIMARY_ADDRESS, addr2String(account.getPrimaryAddress()));
            json.put(PERSONAL, account.getPersonal());
            json.put(SPAM_HANDLER, account.getSpamHandler());

            // Folder names
            json.put(TRASH, account.getTrash());
            json.put(SENT, account.getSent());
            json.put(DRAFTS, account.getDrafts());
            json.put(SPAM, account.getSpam());
            json.put(CONFIRMED_SPAM, account.getConfirmedSpam());
            json.put(CONFIRMED_HAM, account.getConfirmedHam());
            json.put(ARCHIVE, account.getArchive());

            // Folder full names
            json.put(INBOX_FULLNAME, prepareFullname(accountId, "INBOX"));
            json.put(TRASH_FULLNAME, prepareFullname(accountId, account.getTrashFullname()));
            json.put(SENT_FULLNAME, prepareFullname(accountId, account.getSentFullname()));
            json.put(DRAFTS_FULLNAME, prepareFullname(accountId, account.getDraftsFullname()));
            json.put(SPAM_FULLNAME, prepareFullname(accountId, account.getSpamFullname()));
            json.put(CONFIRMED_SPAM_FULLNAME, prepareFullname(accountId, account.getConfirmedSpamFullname()));
            json.put(CONFIRMED_HAM_FULLNAME, prepareFullname(accountId, account.getConfirmedHamFullname()));
            json.put(ARCHIVE_FULLNAME, prepareFullname(accountId, account.getArchiveFullname()));

            // Unified Mail enabled
            json.put(UNIFIED_INBOX_ENABLED, account.isUnifiedINBOXEnabled());
            // Properties
            final Map<String, String> props = account.getProperties();
            if (props.containsKey("pop3.deletewt")) {
                json.put(POP3_DELETE_WRITE_THROUGH, Boolean.parseBoolean(props.get("pop3.deletewt")));
            }
            if (props.containsKey("pop3.expunge")) {
                json.put(POP3_EXPUNGE_ON_QUIT, Boolean.parseBoolean(props.get("pop3.expunge")));
            }
            if (props.containsKey("pop3.refreshrate")) {
                json.put(POP3_REFRESH_RATE, props.get("pop3.refreshrate"));
            }
            if (props.containsKey("pop3.storage")) {
                json.put(POP3_STORAGE, props.get("pop3.storage"));
            }
            if (props.containsKey("pop3.path")) {
                json.put(POP3_PATH, props.get("pop3.path"));
            }
            // Reply-to
            {
                final String replyTo = account.getReplyTo();
                if (null == replyTo) {
                    if (props.containsKey("replyto")) {
                        json.put(MailAccountFields.REPLY_TO, props.get("replyto"));
                    }
                } else {
                    json.put(REPLY_TO, replyTo);
                }
            }
            if (props.containsKey(ADDRESSES)) {
                json.put(ADDRESSES, props.get(ADDRESSES));
            }
        }
        return json;
    }

    private static final EnumSet<Attribute> FULL_NAMES = EnumSet.of(
        Attribute.TRASH_FULLNAME_LITERAL,
        Attribute.SENT_FULLNAME_LITERAL,
        Attribute.DRAFTS_FULLNAME_LITERAL,
        Attribute.SPAM_FULLNAME_LITERAL,
        Attribute.CONFIRMED_HAM_FULLNAME_LITERAL,
        Attribute.CONFIRMED_SPAM_FULLNAME_LITERAL,
        Attribute.ARCHIVE_FULLNAME_LITERAL);

    /**
     * Writes specified attributes for each mail account contained in given array in an own JSON array surrounded by a super JSON array.
     *
     * @param mailAccounts The mail accounts
     * @param attributes The attributes
     * @param session The session
     * @return A JSON array of JSON arrays for each account
     * @throws OXException If writing JSON fails
     */
    public static JSONArray writeArray(MailAccount[] mailAccounts, List<Attribute> attributes, Session session) throws OXException {
        return writeArray(mailAccounts, attributes, session, hideDetailsForDefaultAccount());
    }

    /**
     * Writes specified attributes for each mail account contained in given array in an own JSON array surrounded by a super JSON array.
     *
     * @param mailAccounts The mail accounts
     * @param attributes The attributes
     * @param session The session
     * @param hideDetailsForDefaultAccount Whether to hide details for primary account
     * @return A JSON array of JSON arrays for each account
     * @throws OXException If writing JSON fails
     */
    public static JSONArray writeArray(MailAccount[] mailAccounts, List<Attribute> attributes, Session session, boolean hideDetailsForDefaultAccount) throws OXException {
        JSONArray jRows = new JSONArray(mailAccounts.length);
        JSlobStorage jSlobStorage = AbstractMailAccountAction.getStorage();
        // Write accounts
        for (MailAccount account : mailAccounts) {
            jRows.put(writeArrayRow(account, attributes, session, hideDetailsForDefaultAccount, jSlobStorage));
        }
        return jRows;
    }

    /**
     * Writes specified attributes for each mail account contained in given array in an own JSON array surrounded by a super JSON array.
     *
     * @param account The mail account
     * @param attributes The attributes
     * @param session The session
     * @return A JSON array row for given account
     * @throws OXException If writing JSON fails
     */
    public static JSONArray writeArrayRow(MailAccount account, List<Attribute> attributes, Session session) throws OXException {
        return writeArrayRow(account, attributes, session, hideDetailsForDefaultAccount());
    }

    /**
     * Writes specified attributes for each mail account contained in given array in an own JSON array surrounded by a super JSON array.
     *
     * @param account The mail account
     * @param attributes The attributes
     * @param session The session
     * @param hideDetailsForDefaultAccount Whether to hide details for primary account
     * @return A JSON array row for given account
     * @throws OXException If writing JSON fails
     */
    public static JSONArray writeArrayRow(MailAccount account, List<Attribute> attributes, Session session, boolean hideDetailsForDefaultAccount) throws OXException {
        final JSlobStorage jSlobStorage = AbstractMailAccountAction.getStorage();
        return writeArrayRow(account, attributes, session, hideDetailsForDefaultAccount, jSlobStorage);
    }

    private static JSONArray writeArrayRow(MailAccount account, List<Attribute> attributes, Session session, boolean hideDetailsForDefaultAccount, JSlobStorage jSlobStorage) throws OXException {
        final boolean hideForDefault = hideDetailsForDefaultAccount && MailAccount.DEFAULT_ID == account.getId();
        final MailAccountGetSwitch getter = new MailAccountGetSwitch(account);
        final JSONArray jRow = new JSONArray(hideForDefault ? 32 : 64);
        for (final Attribute attribute : attributes) {
            if (hideForDefault) {
                if (HIDDEN_FOR_DEFAULT.contains(attribute)) {
                    jRow.put(JSONObject.NULL);
                } else {
                    writeAttribute(attribute, account, getter, jRow, session, jSlobStorage);
                }
            } else {
                if (Attribute.PASSWORD_LITERAL == attribute || Attribute.TRANSPORT_PASSWORD_LITERAL == attribute) {
                    jRow.put(JSONObject.NULL);
                } else if (Attribute.POP3_DELETE_WRITE_THROUGH_LITERAL == attribute || Attribute.POP3_EXPUNGE_ON_QUIT_LITERAL == attribute) {
                    jRow.put(Boolean.parseBoolean(String.valueOf(attribute.doSwitch(getter))));
                } else {
                    writeAttribute(attribute, account, getter, jRow, session, jSlobStorage);
                }
            }
        }
        return jRow;
    }

    private static void writeAttribute(Attribute attribute, MailAccount account, MailAccountGetSwitch getter, JSONArray row, Session session, JSlobStorage jSlobStorage) throws OXException {
        if (FULL_NAMES.contains(attribute)) {
            Object value = attribute.doSwitch(getter);
            if (null == value) {
                row.put(JSONObject.NULL);
            } else {
                row.put(prepareFullname(account.getId(), value.toString()));
            }
        } else if (Attribute.META == attribute) {
            JSlobId jSlobId = new JSlobId(AbstractMailAccountAction.JSLOB_SERVICE_ID, Integer.toString(account.getId()), session.getUserId(), session.getContextId());
            JSlob jSlob = jSlobStorage.opt(jSlobId);
            if (null == jSlob) {
                row.put(JSONObject.NULL);
            } else {
                row.put(jSlob.getJsonObject());
            }
        } else if (Attribute.PRIMARY_ADDRESS_LITERAL == attribute) {
            Object value  = attribute.doSwitch(getter);
            if (null == value) {
                row.put(JSONObject.NULL);
            } else {
                row.put(addr2String(value.toString()));
            }
        } else {
            Object value  = attribute.doSwitch(getter);
            row.put(value == null ? JSONObject.NULL : value);
        }
    }

    /**
     * Adds transport account's value for specified attribute to given JSON array.
     *
     * @param attribute The attribute to add
     * @param account The transport account
     * @param jAccount The JSON array
     * @throws OXException If adding attribute fails
     */
    public static void writeAttributeToArray(Attribute attribute, TransportAccount account, JSONArray jAccount) throws OXException {
        writeAttribute(attribute, account, jAccount, ARRAY_ATTR_PUTTER);
    }

    /**
     * Adds transport account's value for specified attribute to given JSON object.
     *
     * @param attribute The attribute to add
     * @param account The transport account
     * @param jAccount The JSON object
     * @throws OXException If adding attribute fails
     */
    public static void writeAttributeToObject(Attribute attribute, TransportAccount account, JSONObject jAccount) throws OXException {
        writeAttribute(attribute, account, jAccount, OBJECT_ATTR_PUTTER);
    }

    private static interface AttributePutter {

        void putAttribute(Attribute attribute, Object value, JSONValue jValue) throws JSONException;
    }

    private static final AttributePutter OBJECT_ATTR_PUTTER = new AttributePutter() {

        @Override
        public void putAttribute(Attribute attribute, Object value, JSONValue jValue) throws JSONException {
            if (null != value) {
                jValue.toObject().put(attribute.getName(), value);
            }
        }
    };

    private static final AttributePutter ARRAY_ATTR_PUTTER = new AttributePutter() {

        @Override
        public void putAttribute(Attribute attribute, Object value, JSONValue jValue) {
            jValue.toArray().put(null == value ? JSONObject.NULL : value);
        }
    };

    private static void writeAttribute(Attribute attribute, TransportAccount account, JSONValue jValue, AttributePutter putter) throws OXException {
        try {
            boolean hideForDefault = hideDetailsForDefaultAccount() && MailAccount.DEFAULT_ID == account.getId();
            if (hideForDefault) {
                if (HIDDEN_FOR_DEFAULT.contains(attribute)) {
                    putter.putAttribute(attribute, null, jValue);
                    return;
                }
            }

            TransportAccountGetSwitch getter = new TransportAccountGetSwitch(account);
            if (Attribute.PASSWORD_LITERAL == attribute || Attribute.TRANSPORT_PASSWORD_LITERAL == attribute) {
                putter.putAttribute(attribute, null, jValue);
            } else if (Attribute.META == attribute) {
                putter.putAttribute(attribute, null, jValue);
            } else if (Attribute.PRIMARY_ADDRESS_LITERAL == attribute) {
                Object value  = attribute.doSwitch(getter);
                if (null == value) {
                    putter.putAttribute(attribute, null, jValue);
                } else {
                    putter.putAttribute(attribute, addr2String(value.toString()), jValue);
                }
            } else {
                Object value  = attribute.doSwitch(getter);
                putter.putAttribute(attribute, value, jValue);
            }
        } catch (JSONException e) {
            throw MailAccountExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private static String addr2String(final String primaryAddress) {
        if (null == primaryAddress) {
            return primaryAddress;
        }
        try {
            final QuotedInternetAddress addr = new QuotedInternetAddress(primaryAddress);
            final String sAddress = addr.getAddress();
            final int pos = null == sAddress ? 0 : sAddress.indexOf('/');
            if (pos <= 0) {
                // No slash character present
                return addr.toUnicodeString();
            }
            final StringBuilder sb = new StringBuilder(32);
            final String personal = addr.getPersonal();
            if (null == personal) {
                sb.append(MimeMessageUtility.prepareAddress(sAddress.substring(0, pos)));
            } else {
                sb.append(preparePersonal(personal));
                sb.append(" <").append(MimeMessageUtility.prepareAddress(sAddress.substring(0, pos))).append('>');
            }
            return sb.toString();
        } catch (final Exception e) {
            return primaryAddress;
        }
    }

    /**
     * Prepares specified personal string by surrounding it with quotes if needed.
     *
     * @param personal The personal
     * @return The prepared personal
     */
    static String preparePersonal(final String personal) {
        return MimeMessageUtility.quotePhrase(personal, false);
    }

}
