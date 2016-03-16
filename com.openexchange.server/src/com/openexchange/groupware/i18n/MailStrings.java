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

package com.openexchange.groupware.i18n;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link MailStrings} - Provides locale-specific string constants for mail module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MailStrings implements LocalizableStrings {

    /**
     * Constructor must be public to enable i18n tests to read the fields with reflection.
     */
    public MailStrings() {
        super();
    }

    // The internationalized name for the INBOX mail folder
    public static final String INBOX = "Inbox"
        .intern();

    // The internationalized name for the trash mail folder
    public static final String TRASH = "Trash"
        .intern();

    // The internationalized name for the drafts mail folder
    public static final String DRAFTS = "Drafts"
        .intern();

    // The internationalized name for the sent mail folder
    public static final String SENT = "Sent objects"
        .intern();

    // The alternative internationalized name for the sent mail folder
    public static final String SENT_ALT = "Sent items"
        .intern();

    // The internationalized name for the spam mail folder
    public static final String SPAM = "Spam"
        .intern();

    // The internationalized name for the archive mail folder
    public static final String ARCHIVE = "Archive"
        .intern();

    // The internationalized name for the confirmed-spam mail folder
    public static final String CONFIRMED_SPAM = "Confirmed spam"
        .intern();

    // The alternative internationalized name for the confirmed-spam mail folder
    public static final String CONFIRMED_SPAM_ALT = "confirmed-spam"
        .intern();

    // The internationalized name for the confirmed-spam mail folder
    public static final String CONFIRMED_HAM = "Confirmed ham"
        .intern();

    // The alternative internationalized name for the confirmed-spam mail folder
    public static final String CONFIRMED_HAM_ALT = "confirmed-ham"
        .intern();


    // The internationalized subject for a read acknowledgement
    public static final String ACK_SUBJECT = "Read acknowledgement";

    // The text body of a received read acknowledgement
    // #DATE# is replaced with the sent date of referenced email
    // #RECIPIENT# is replaced with personal and/or email address of recipient(s)
    // #SUBJECT# is replaced with referenced email's subject
    public static final String ACK_NOTIFICATION_TEXT = "This is a delivery receipt for the mail that you sent on #DATE# to #RECIPIENT# with subject \"#SUBJECT#\".\n\nNote: This delivery receipt only acknowledges that the message was displayed on the recipients computer. There is no guarantee that the recipient has read or understood the message contents.";

    // The prefix text on reply.
    //
    // Supports the following placeholders:
    // #DATE# is replaced with the sent date of referenced email
    // #TIME# is replaced with time of referenced email
    // #SENDER# is replaced with personal and/or email address of the sender
    // #FROM# is replaced with personal and/or email address of original message's From header
    // #TO# is replaced with personal and/or email address(es) of original message's To header
    // #CC# is replaced with personal and/or email address(es) of original message's Cc header
    // #CC_LINE# is replaced with personal and/or email address(es) of original message's Cc header prefixed with "\nCc: " (which needs not to be internationalized)
    // #SUBJECT# is replaced with referenced email's subject
    public static final String REPLY_PREFIX = "On #DATE# at #TIME# #SENDER# wrote:";

    // The prefix text on inline forward.
    //
    // Supports the following placeholders:
    // #DATE# is replaced with the sent date of referenced email
    // #TIME# is replaced with time of referenced email
    // #SENDER# is replaced with personal and/or email address of the sender
    // #FROM# is replaced with personal and/or email address of original message's From header
    // #TO# is replaced with personal and/or email address(es) of original message's To header
    // #CC# is replaced with personal and/or email address(es) of original message's Cc header
    // #CC_LINE# is replaced with personal and/or email address(es) of original message's Cc header prefixed with "\nCc: " (which needs not to be internationalized)
    // #SUBJECT# is replaced with referenced email's subject
    public static final String FORWARD_PREFIX = "---------- Original Message ----------\nFrom: #FROM#\nTo: #TO##CC_LINE#\nDate: #DATE# at #TIME#\nSubject: #SUBJECT#";

    // The internationalized default subject.
    public static final String DEFAULT_SUBJECT = "[No Subject]";

    // The internationalized text put into text body of an email of which attachments exceed user's quota limitation
    // Hints to the available attachments for affected message
    public static final String PUBLISHED_ATTACHMENTS_PREFIX = "The available attachments for this E-Mail can be accessed via the links:";

    // The internationalized text put into text body of an email of which attachments exceed user's quota limitation
    // Indicates the elapsed date for affected message's attachments
    public static final String PUBLISHED_ATTACHMENTS_APPENDIX = "The links will be deleted on #DATE#";

    // The text put into the description field of a published E-Mail attachment
    public static final String PUBLISHED_ATTACHMENT_INFO = "This file has been published for E-Mail \"#SUBJECT#\" sent on #DATE# to #TO#";

    // The greeting used for form mails. E.g.
    // "Dear Sir or Madam Jane Doe"
    public static final String GREETING = "Dear Sir or Madam";

    // The name for trash folder
    public static final String DEFAULT_TRASH = "Trash"
        .intern();

    // The name for trash folder
    public static final String DEFAULT_DRAFTS = "Drafts"
        .intern();

    // The name for trash folder
    public static final String DEFAULT_SENT = "Sent objects"
        .intern();

    // The name for trash folder
    public static final String DEFAULT_SPAM = "Spam"
        .intern();

    // The name for trash folder
    public static final String DEFAULT_ARCHIVE = "Archive"
        .intern();

    // The name for "Unified Mail" root folder
    public static final String UNIFIED_MAIL = "Unified mail"
        .intern();

}
