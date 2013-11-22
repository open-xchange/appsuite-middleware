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

package com.openexchange.messaging.mail;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * {@link MailConstants}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.18
 */
public final class MailConstants {

    /**
     * The type for forward.
     */
    public static final String TYPE_FORWARD = "forward";

    /**
     * The type for reply.
     */
    public static final String TYPE_REPLY = "reply";

    /**
     * The type for reply-all.
     */
    public static final String TYPE_REPLY_ALL = "replyAll";

    /*
     * -----------------------------------------------
     */

    /**
     * The configuration property name for mail login.
     */
    public static final String MAIL_LOGIN = "login";

    /**
     * The configuration property name for mail password.
     */
    public static final String MAIL_PASSWORD = "password";

    /**
     * The configuration property name for confirmed-ham.
     */
    public static final String MAIL_CONFIRMED_HAM = "confirmedHam";

    /**
     * The configuration property name for confirmed-ham full name.
     */
    public static final String MAIL_CONFIRMED_HAM_FULLNAME = "confirmedHamFullname";

    /**
     * The configuration property name for confirmed-spam.
     */
    public static final String MAIL_CONFIRMED_SPAM = "confirmedSpam";

    /**
     * The configuration property name for confirmed-spam full name.
     */
    public static final String MAIL_CONFIRMED_SPAM_FULLNAME = "confirmedSpamFullname";

    /**
     * The configuration property name for drafts.
     */
    public static final String MAIL_DRAFTS = "drafts";

    /**
     * The configuration property name for drafts full name.
     */
    public static final String MAIL_DRAFTS_FULLNAME = "draftsFullname";

    /**
     * The configuration property name for sent.
     */
    public static final String MAIL_SENT = "sent";

    /**
     * The configuration property name for sent full name.
     */
    public static final String MAIL_SENT_FULLNAME = "sentFullname";

    /**
     * The configuration property name for spam.
     */
    public static final String MAIL_SPAM = "spam";

    /**
     * The configuration property name for spam full name.
     */
    public static final String MAIL_SPAM_FULLNAME = "spamFullname";

    /**
     * The configuration property name for trash.
     */
    public static final String MAIL_TRASH = "trash";

    /**
     * The configuration property name for archive.
     */
    public static final String MAIL_ARCHIVE = "archive";

    /**
     * The configuration property name for trash full name.
     */
    public static final String MAIL_TRASH_FULLNAME = "trashFullname";

    /**
     * The configuration property name for archive full name.
     */
    public static final String MAIL_ARCHIVE_FULLNAME = "archiveFullname";

    public static final String MAIL_PORT = "mailPort";

    public static final String MAIL_PROTOCOL = "mailProtocol";

    public static final String MAIL_SECURE = "mailSecure";

    public static final String MAIL_SERVER = "mailServer";

    public static final String MAIL_PERSONAL = "personal";

    public static final String MAIL_REPLY_TO = "replyTo";

    public static final String MAIL_PRIMARY_ADDRESS = "primaryAddress";

    public static final String TRANSPORT_LOGIN = "transportLogin";

    public static final String TRANSPORT_PASSWORD = "transportPassword";

    public static final String TRANSPORT_PORT = "transportPort";

    public static final String TRANSPORT_PROTOCOL = "transportProtocol";

    public static final String TRANSPORT_SECURE = "transportSecure";

    public static final String TRANSPORT_SERVER = "transportServer";

    public static final String UNIFIED_MAIL_ENABLED = "unifiedMailEnabled";

    /**
     * Initializes a new {@link MailConstants}.
     */
    private MailConstants() {
        super();
    }

    public static final Set<String> ALL =
        new HashSet<String>(Arrays.asList(
            MAIL_LOGIN,
            MAIL_PASSWORD,
            MAIL_CONFIRMED_HAM,
            MAIL_CONFIRMED_HAM_FULLNAME,
            MAIL_CONFIRMED_SPAM,
            MAIL_CONFIRMED_SPAM_FULLNAME,
            MAIL_DRAFTS,
            MAIL_DRAFTS_FULLNAME,
            MAIL_SENT,
            MAIL_SENT_FULLNAME,
            MAIL_SPAM,
            MAIL_SPAM_FULLNAME,
            MAIL_TRASH,
            MAIL_TRASH_FULLNAME,
            MAIL_PORT,
            MAIL_PROTOCOL,
            MAIL_SECURE,
            MAIL_SERVER,
            MAIL_PERSONAL,
            MAIL_PRIMARY_ADDRESS,
            TRANSPORT_LOGIN,
            TRANSPORT_PASSWORD,
            TRANSPORT_PORT,
            TRANSPORT_PROTOCOL,
            TRANSPORT_SECURE,
            TRANSPORT_SERVER,
            UNIFIED_MAIL_ENABLED));

    public static final String CAPABILITY_THREAD_REFERENCES = "THREAD=REFERENCES";

}
