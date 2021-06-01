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
    public static final String SENT = "Sent"
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
