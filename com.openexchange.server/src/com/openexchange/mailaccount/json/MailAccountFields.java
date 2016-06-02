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

    public static final String TRANSPORT_URL = "transport_url";

    public static final String TRANSPORT_SERVER = "transport_server";

    public static final String TRANSPORT_PORT = "transport_port";

    public static final String TRANSPORT_PROTOCOL = "transport_protocol";

    public static final String TRANSPORT_SECURE = "transport_secure";

    public static final String TRANSPORT_LOGIN = "transport_login";

    public static final String TRANSPORT_PASSWORD = "transport_password";

    public static final String TRANSPORT_STARTTLS = "transport_starttls";

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
}
