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

package com.openexchange.mailaccount.json;

/**
 * {@link MailAccountFields} - Provides constants for mail account JSON fields.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface MailAccountFields {

    public static final String ID = "id";

    public static final String LOGIN = "login";

    public static final String PASSWORD = "password";

    public static final String MAIL_URL = "mail_url";

    public static final String MAIL_SERVER = "mail_server";

    public static final String MAIL_PORT = "mail_port";

    public static final String MAIL_PROTOCOL = "mail_protocol";

    public static final String MAIL_SECURE = "mail_secure";

    public static final String MAIL_STARTTLS = "mail_starttls";

    public static final String MAIL_OAUTH = "mail_oauth";

    public static final String TRANSPORT_URL = "transport_url";

    public static final String TRANSPORT_SERVER = "transport_server";

    public static final String TRANSPORT_PORT = "transport_port";

    public static final String TRANSPORT_PROTOCOL = "transport_protocol";

    public static final String TRANSPORT_SECURE = "transport_secure";

    public static final String TRANSPORT_LOGIN = "transport_login";

    public static final String TRANSPORT_PASSWORD = "transport_password";

    public static final String TRANSPORT_STARTTLS = "transport_starttls";

    public static final String TRANSPORT_OAUTH = "transport_oauth";

    /**
     * Specifies if transport authentication is supposed being performed (with either dedicate credentials or the ones from mail services)
     * or not (for transport services w/o authentication capability)
     */
    public static final String TRANSPORT_AUTH = "transport_auth";

    public static final String NAME = "name";

    public static final String PRIMARY_ADDRESS = "primary_address";

    public static final String PERSONAL = "personal";

    public static final String REPLY_TO = "reply_to";

    public static final String SPAM_HANDLER = "spam_handler";

    public static final String TRASH = "trash";

    public static final String ARCHIVE = "archive";

    public static final String SENT = "sent";

    public static final String DRAFTS = "drafts";

    public static final String SPAM = "spam";

    public static final String CONFIRMED_SPAM = "confirmed_spam";

    public static final String CONFIRMED_HAM = "confirmed_ham";

    public static final String UNIFIED_INBOX_ENABLED = "unified_inbox_enabled";

    public static final String INBOX_FULLNAME = "inbox_fullname";

    public static final String TRASH_FULLNAME = "trash_fullname";

    public static final String ARCHIVE_FULLNAME = "archive_fullname";

    public static final String SENT_FULLNAME = "sent_fullname";

    public static final String DRAFTS_FULLNAME = "drafts_fullname";

    public static final String SPAM_FULLNAME = "spam_fullname";

    public static final String CONFIRMED_SPAM_FULLNAME = "confirmed_spam_fullname";

    public static final String CONFIRMED_HAM_FULLNAME = "confirmed_ham_fullname";

    public static final String POP3_REFRESH_RATE = "pop3_refresh_rate";

    public static final String POP3_EXPUNGE_ON_QUIT = "pop3_expunge_on_quit";

    public static final String POP3_DELETE_WRITE_THROUGH = "pop3_delete_write_through";

    public static final String POP3_STORAGE = "pop3_storage";

    public static final String POP3_PATH = "pop3_path";

    public static final String ADDRESSES = "addresses";

    public static final String META = "meta";

    public static final String ROOT_FOLDER = "root_folder";

    public static final String MAIL_DISABLED = "mail_disabled";

    public static final String TRANSPORT_DISABLED = "transport_disabled";

}
