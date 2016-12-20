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

package com.openexchange.mail.usersetting;

import java.io.Serializable;
import javax.mail.internet.idn.IDNA;
import com.openexchange.exception.OXException;
import com.openexchange.spamhandler.SpamHandlerRegistry;

/**
 * {@link UserSettingMail} - User's mail settings.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UserSettingMail implements Cloneable, Serializable {

    /**
     * {@link Signature} - The mail signature.
     *
     * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
     */
    public static final class Signature implements Cloneable, Serializable {

        /**
         * Serial Version UID
         */
        private static final long serialVersionUID = 357223875887317509L;

        private String id;

        private String signature;

        public Signature(final String id, final String signature) {
            this.id = id;
            this.signature = signature;
        }

        @Override
        public Object clone() {
            try {
                final Signature clone = (Signature) super.clone();
                clone.id = id;
                clone.signature = signature;
                return clone;
            } catch (final CloneNotSupportedException e) {
                /*
                 * Cannot occur since we are cloneable
                 */
                LOG.error("", e);
                final InternalError error = new InternalError(e.getMessage());
                error.initCause(e);
                throw error;
            }
        }

        public String getId() {
            return id;
        }

        public String getSignature() {
            return signature;
        }

    }

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = -5787223065275414178L;

    private static final transient org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(UserSettingMail.class);

    /*-
     * Integer constants for on/off options
     */
    /**
     * If this bit is set, html content is displayed on message display
     */
    public static final int INT_DISPLAY_HTML_INLINE_CONTENT = 1;

    /**
     * If this bit is set, the quote levels of a plain text message are colorized
     */
    public static final int INT_USE_COLOR_QUOTE = 1 << 1;

    /**
     * If this bit is set, emoticons like <tt>:-)</tt> are replaced with a little graphic
     */
    public static final int INT_SHOW_GRAPHIC_EMOTICONS = 1 << 2;

    /**
     * If this bit is set, no copy of a deleted message is created in default "trash" folder. The message is hard deleted and no more
     * present at all.
     */
    public static final int INT_HARD_DELETE_MSGS = 1 << 3;

    /**
     * If this bit is set, a forwarded message is appended as an <tt>message/rfc822</tt> attachment instead of an inline forward
     */
    public static final int INT_FORWARD_AS_ATTACHMENT = 1 << 4;

    /**
     * If this bit is set, a VCard created from user's contact data is appended to a sent message
     */
    public static final int INT_APPEND_VCARD = 1 << 5;

    /**
     * If this bit is set, the user is notified if a message contains a receipt acknowledgment
     */
    public static final int INT_DISPLAY_RECEIPT_NOTIFICATION = 1 << 6;

    /**
     * This constant is currently not in use
     */
    public static final int INT_MSG_PREVIEW = 1 << 7;

    /**
     * If this bit is set, the user will receive notification messages on appointment events (creation, deletion & change)
     */
    public static final int INT_NOTIFY_APPOINTMENTS = 1 << 8;

    /**
     * If this bit is set, the user will receive notification messages on task events (creation, deletion & change)
     */
    public static final int INT_NOTIFY_TASKS = 1 << 9;

    /**
     * If this bit is set, no message body text is extracted (and displayed) from the message to which the user replies
     */
    public static final int INT_IGNORE_ORIGINAL_TEXT_ON_REPLY = 1 << 10;

    /**
     * If this bit is set, no copy of a sent mail is created in default "sent" folder
     */
    public static final int INT_NO_COPY_INTO_SENT_FOLDER = 1 << 11;

    /**
     * If this bit is set, the spam feature is enabled
     */
    public static final int INT_SPAM_ENABLED = 1 << 12;

    /**
     * If this bit is set, only plain text is allowed when composing reply/forward messages. The user will see the html2text converted
     * content when replying to/inline-forwarding a html message.
     */
    public static final int INT_TEXT_ONLY_COMPOSE = 1 << 13;

    /**
     * If this bit is set, it is allowed to display images which appear in HTML content of a message.
     */
    public static final int INT_ALLOW_HTML_IMAGES = 1 << 14;

    /**
     * If this bit is set, the user will receive notification messages on appointment events (accept, decline & tentatively accepted) as the
     * appointment's owner.
     */
    public static final int INT_NOTIFY_APPOINTMENTS_CONFIRM_OWNER = 1 << 15;

    /**
     * If this bit is set, the user will receive notification messages on appointment events (accept, decline & tentatively accepted) as an
     * appointment's participant.
     */
    public static final int INT_NOTIFY_APPOINTMENTS_CONFIRM_PARTICIPANT = 1 << 16;

    /**
     * If this bit is set, the user will receive notification messages on task events (accept, decline & tentatively accepted) as the task's
     * owner.
     */
    public static final int INT_NOTIFY_TASKS_CONFIRM_OWNER = 1 << 17;

    /**
     * If this bit is set, the user will receive notification messages on task events (accept, decline & tentatively accepted) as a task's
     * participant.
     */
    public static final int INT_NOTIFY_TASKS_CONFIRM_PARTICIPANT = 1 << 18;

    /**
     * If this bit is set, the reply-all method will put all recipients except the original sender in the Cc list.
     */
    public static final int INT_REPLY_ALL_CC = 1 << 19;

    /*-
     * Other constants
     */
    public static final int MSG_FORMAT_TEXT_ONLY = 1;

    public static final int MSG_FORMAT_HTML_ONLY = 2;

    public static final int MSG_FORMAT_BOTH = 3;

    /*-
     * Member fields
     */
    private final int userId;

    private final int cid;

    private boolean modifiedDuringSession;

    private boolean displayHtmlInlineContent;

    private boolean suppressHTMLAlternativePart;

    private boolean useColorQuote;

    private boolean showGraphicEmoticons;

    private boolean hardDeleteMsgs;

    private boolean forwardAsAttachment;

    private boolean appendVCard;

    private boolean displayReceiptNotification;

    private boolean notifyAppointments;

    private boolean notifyTasks;

    private boolean notifyAppointmentsConfirmOwner;

    private boolean notifyAppointmentsConfirmParticipant;

    private boolean notifyTasksConfirmOwner;

    private boolean notifyTasksConfirmParticipant;

    private boolean msgPreview;

    private boolean ignoreOriginalMailTextOnReply;

    private boolean noCopyIntoStandardSentFolder;

    private boolean spamEnabled;

    private boolean textOnlyCompose;

    private boolean allowHTMLImages;

    private boolean replyAllCc;

    private Signature[] signatures;

    private String sendAddr;

    private String replyToAddr;

    private int msgFormat = MSG_FORMAT_TEXT_ONLY;

    private String[] displayMsgHeaders;

    private int autoLinebreak = 80;

    private String stdTrashName;

    private String stdDraftsName;

    private String stdSentName;

    private String stdSpamName;

    private String confirmedSpam;

    private String confirmedHam;

    private long uploadQuota;

    private long uploadQuotaPerFile;

    private Boolean spamHandlerFound;

    private boolean noSave;

    private boolean dropReplyForwardPrefix;

    private int attachOriginalMessage;

    /**
     * Initializes a new {@link UserSettingMail}.
     *
     * @param userId The user ID
     * @param cid The context ID
     */
    public UserSettingMail(final int userId, final int cid) {
        super();
        this.userId = userId;
        this.cid = cid;
        attachOriginalMessage = -1;
    }

    @Override
    public UserSettingMail clone() {
        try {
            final UserSettingMail clone = (UserSettingMail) super.clone();
            if (displayMsgHeaders != null) {
                clone.displayMsgHeaders = new String[displayMsgHeaders.length];
                System.arraycopy(displayMsgHeaders, 0, clone.displayMsgHeaders, 0, displayMsgHeaders.length);
            }
            if (signatures != null) {
                clone.signatures = new Signature[signatures.length];
                for (int i = 0; i < signatures.length; i++) {
                    clone.signatures[i] = (Signature) signatures[i].clone();
                }
            }
            return clone;
        } catch (final CloneNotSupportedException e) {
            LOG.error("", e);
            final InternalError error = new InternalError(e.getMessage());
            error.initCause(e);
            throw error;
        }
    }

    /**
     * Gets the character count after which a line break is added in <code>text/plain</code> messages
     *
     * @return The character count after which a line break is added
     */
    public int getAutoLinebreak() {
        return autoLinebreak;
    }

    /**
     * Gets the dropReplyForwardPrefix.
     *
     * @return The dropReplyForwardPrefix
     */
    public boolean isDropReplyForwardPrefix() {
        return dropReplyForwardPrefix;
    }

    /**
     * Sets the dropReplyForwardPrefix.
     *
     * @param dropReplyForwardPrefix The dropReplyForwardPrefix to set
     */
    public void setDropReplyForwardPrefix(final boolean dropReplyForwardPrefix) {
        this.dropReplyForwardPrefix = dropReplyForwardPrefix;
    }

    /**
     * Generates a bit pattern from this settings
     *
     * @return a bit pattern from this settings
     */
    public int getBitsValue() {
        int retval = 0;
        retval = displayHtmlInlineContent ? (retval | INT_DISPLAY_HTML_INLINE_CONTENT) : retval;
        retval = useColorQuote ? (retval | INT_USE_COLOR_QUOTE) : retval;
        retval = showGraphicEmoticons ? (retval | INT_SHOW_GRAPHIC_EMOTICONS) : retval;
        retval = hardDeleteMsgs ? (retval | INT_HARD_DELETE_MSGS) : retval;
        retval = forwardAsAttachment ? (retval | INT_FORWARD_AS_ATTACHMENT) : retval;
        retval = appendVCard ? (retval | INT_APPEND_VCARD) : retval;
        retval = displayReceiptNotification ? (retval | INT_DISPLAY_RECEIPT_NOTIFICATION) : retval;
        retval = msgPreview ? (retval | INT_MSG_PREVIEW) : retval;

        retval = notifyAppointments ? (retval | INT_NOTIFY_APPOINTMENTS) : retval;
        retval = notifyAppointmentsConfirmOwner ? (retval | INT_NOTIFY_APPOINTMENTS_CONFIRM_OWNER) : retval;
        retval = notifyAppointmentsConfirmParticipant ? (retval | INT_NOTIFY_APPOINTMENTS_CONFIRM_PARTICIPANT) : retval;

        retval = notifyTasks ? (retval | INT_NOTIFY_TASKS) : retval;
        retval = notifyTasksConfirmOwner ? (retval | INT_NOTIFY_TASKS_CONFIRM_OWNER) : retval;
        retval = notifyTasksConfirmParticipant ? (retval | INT_NOTIFY_TASKS_CONFIRM_PARTICIPANT) : retval;

        retval = ignoreOriginalMailTextOnReply ? (retval | INT_IGNORE_ORIGINAL_TEXT_ON_REPLY) : retval;
        retval = noCopyIntoStandardSentFolder ? (retval | INT_NO_COPY_INTO_SENT_FOLDER) : retval;
        retval = spamEnabled ? (retval | INT_SPAM_ENABLED) : retval;
        retval = textOnlyCompose ? (retval | INT_TEXT_ONLY_COMPOSE) : retval;
        retval = allowHTMLImages ? (retval | INT_ALLOW_HTML_IMAGES) : retval;

        retval = replyAllCc ? (retval | INT_REPLY_ALL_CC) : retval;
        return retval;
    }

    /**
     * Gets the name of the confirmed ham folder.
     * <p>
     * <b>Note</b>: This is only the name, not its full name.
     *
     * @return The name of the confirmed ham folder
     */
    public String getConfirmedHam() {
        return confirmedHam;
    }

    /**
     * Gets the name of the confirmed spam folder.
     * <p>
     * <b>Note</b>: This is only the name, not its full name.
     *
     * @return The name of the confirmed spam folder
     */
    public String getConfirmedSpam() {
        return confirmedSpam;
    }

    /**
     * Define an array of message headers which shall be displayed for this user in mail's detail view.
     *
     * @return An array of message headers which shall be displayed
     */
    public String[] getDisplayMsgHeaders() {
        if (displayMsgHeaders == null) {
            return null;
        }
        final String[] retval = new String[displayMsgHeaders.length];
        System.arraycopy(displayMsgHeaders, 0, retval, 0, displayMsgHeaders.length);
        return retval;
    }

    /**
     * Indicates the desired message format when preparing a message for transport.
     * <p>
     * The returned <code>int</code> value is one of {@link #MSG_FORMAT_TEXT_ONLY}, {@link #MSG_FORMAT_HTML_ONLY}, and
     * {@link #MSG_FORMAT_BOTH}.
     *
     * @return The desired message format
     */
    public int getMsgFormat() {
        return msgFormat;
    }

    /**
     * The desired address that shall be set as <code>Reply-To</code> header when sending messages.
     * <p>
     * If returned value is <code>null</code> or empty, the message's <code>From</code> header is used as fallback.
     *
     * @return The desired <code>Reply-To</code> address
     */
    public String getReplyToAddr() {
        return replyToAddr;
    }

    /**
     * The default send address. This address is used to set the <code>From</code> header and the <code>Sender</code> header as well.
     *
     * @return The default send address
     */
    public String getSendAddr() {
        return sendAddr;
    }

    /**
     * Gets the user-defined signatures that are either prepended or appended to a message's body.
     *
     * @return The user-defined signatures
     */
    public Signature[] getSignatures() {
        if (signatures == null) {
            return null;
        }
        final Signature[] retval = new Signature[signatures.length];
        System.arraycopy(signatures, 0, retval, 0, signatures.length);
        return retval;
    }

    /**
     * Gets the name of the draft folder.
     * <p>
     * <b>Note</b>: This is only the name, not its full name.
     *
     * @return The name of the draft folder
     */
    public String getStdDraftsName() {
        return stdDraftsName;
    }

    /**
     * Gets the name of the sent folder.
     * <p>
     * <b>Note</b>: This is only the name, not its full name.
     *
     * @return The name of the sent folder
     */
    public String getStdSentName() {
        return stdSentName;
    }

    /**
     * Gets the name of the spam folder.
     * <p>
     * <b>Note</b>: This is only the name, not its full name.
     *
     * @return The name of the spam folder
     */
    public String getStdSpamName() {
        return stdSpamName;
    }

    /**
     * Gets the name of the trash folder.
     * <p>
     * <b>Note</b>: This is only the name, not its full name.
     *
     * @return The name of the trash folder
     */
    public String getStdTrashName() {
        return stdTrashName;
    }

    /**
     * Gets the overall upload quota limit when uploading several file attachments.
     *
     * @return The overall upload quota limit.
     */
    public long getUploadQuota() {
        return uploadQuota;
    }

    /**
     * Gets the upload quota limit per file when uploading several file attachments.
     *
     * @return The upload quota limit per file.
     */
    public long getUploadQuotaPerFile() {
        return uploadQuotaPerFile;
    }

    /**
     * Checks if user's VCard shall be attached to a message on transport
     *
     * @return <code>true</code> if user's VCard shall be attached to a message on transport; otherwise <code>false</code>
     */
    public boolean isAppendVCard() {
        return appendVCard;
    }

    /**
     * Checks if user allows to display inline HTML content of a message.
     *
     * @return <code>true</code> if user allows to display inline HTML content of a message; otherwise <code>false</code>
     */
    public boolean isDisplayHtmlInlineContent() {
        return displayHtmlInlineContent;
    }

    /**
     * Whether to suppress HTML parts in text-only mode for <i>multipart/alternative</i>.
     *
     * @return <code>true</code> to suppress HTML parts in text-only mode for <i>multipart/alternative</i>; otherwise <code>false</code>
     */
    public boolean isSuppressHTMLAlternativePart() {
        return suppressHTMLAlternativePart;
    }

    /**
     * Checks if a forwarded message is supposed to be added as an attachment; otherwise it is added inline.
     *
     * @return <code>true</code> if a forwarded message is supposed to be added as an attachment; otherwise <code>false</code> if it is
     *         added inline.
     */
    public boolean isForwardAsAttachment() {
        return forwardAsAttachment || attachOriginalMessage > 0;
    }

    /**
     * Checks whether to attach original message
     *
     * @return <code>1</code> to attach original message; <code>0</code> to not attach or <code>-1</code> if not set at all
     */
    public int getAttachOriginalMessage() {
        return attachOriginalMessage;
    }

    /**
     * Checks if messages are supposed to be deleted permanently or a backup is moved to trash folder.
     *
     * @return <code>true</code> if messages are supposed to be deleted permanently; otherwise <code>false</code> to backup in trash folder
     */
    public boolean isHardDeleteMsgs() {
        return hardDeleteMsgs;
    }

    /**
     * Checks if original message's content shall be ignored in reply version to the message
     *
     * @return <code>true</code> if original message's content shall be ignored; otherwise <code>false</code> to include.
     */
    public boolean isIgnoreOriginalMailTextOnReply() {
        return ignoreOriginalMailTextOnReply;
    }

    /**
     * Internal flag to track this mail setting's modified status.
     *
     * @return <code>true</code> if modified during session (and to force a reload); otherwise <code>false</code>
     */
    public boolean isModifiedDuringSession() {
        return modifiedDuringSession;
    }

    /**
     * Currently not used
     */
    public boolean isMsgPreview() {
        return msgPreview;
    }

    /**
     * Checks if a sent message shall be copied into sent folder
     *
     * @return <code>true</code> if a sent message shall be copied into sent folder; otherwise <code>false</code>
     */
    public boolean isNoCopyIntoStandardSentFolder() {
        return noCopyIntoStandardSentFolder;
    }

    /**
     * Checks if the user will receive notification messages on appointment events (creation, deletion & change).
     *
     * @return <code>true</code> if the user will receive notification messages on appointment events (creation, deletion & change);
     *         otherwise <code>false</code>
     */
    public boolean isNotifyAppointments() {
        return notifyAppointments;
    }

    /**
     * Checks if the user will receive notification messages on appointment events (accept, decline & tentatively accepted) as the
     * appointment's owner.
     *
     * @return <code>true</code> if the user will receive notification messages on appointment events (accept, decline & tentatively
     *         accepted) as the appointment's owner; otherwise <code>false</code>.
     */
    public boolean isNotifyAppointmentsConfirmOwner() {
        return notifyAppointmentsConfirmOwner;
    }

    /**
     * Checks if the user will receive notification messages on appointment events (accept, decline & tentatively accepted) as an
     * appointment's participant.
     *
     * @return <code>true</code> if the user will receive notification messages on appointment events (accept, decline & tentatively
     *         accepted) as an appointment's participant; otherwise <code>false</code>.
     */
    public boolean isNotifyAppointmentsConfirmParticipant() {
        return notifyAppointmentsConfirmParticipant;
    }

    /**
     * Checks if the user will receive notification messages on task events (accept, decline & tentatively accepted) as the task's owner.
     *
     * @return <code>true</code> if the user will receive notification messages on task events (accept, decline & tentatively accepted) as
     *         the task's owner; otherwise <code>false</code>.
     */
    public boolean isNotifyTasksConfirmOwner() {
        return notifyTasksConfirmOwner;
    }

    /**
     * Checks if the user will receive notification messages on task events (accept, decline & tentatively accepted) as a task's
     * participant.
     *
     * @return <code>true</code> if the user will receive notification messages on task events (accept, decline & tentatively accepted) as a
     *         task's participant; otherwise <code>false</code>.
     */
    public boolean isNotifyTasksConfirmParticipant() {
        return notifyTasksConfirmParticipant;
    }

    /**
     * If the user is notified if a message contains a receipt acknowledgment.
     */
    public boolean isDisplayReceiptNotification() {
        return displayReceiptNotification;
    }

    /**
     * Checks if the user will receive notification messages on task events (creation, deletion & change).
     *
     * @return <code>true</code> if the user will receive notification messages on task events (creation, deletion & change); otherwise
     *         <code>false</code>
     */
    public boolean isNotifyTasks() {
        return notifyTasks;
    }

    /**
     * Indicates if user wants to see graphical emoticons rather than corresponding textual representation
     *
     * @return <code>true</code> if user wants to see graphical emoticons rather than corresponding textual representation; otherwise
     *         <code>false</code>
     */
    public boolean isShowGraphicEmoticons() {
        return showGraphicEmoticons;
    }

    /**
     * Checks if user has spam enabled. Spam is enabled if both an appropriate spam handler is defined by user's mail provider
     * <small><b>AND</b></small> its mail settings enable spam.
     *
     * @return <code>true</code> if user has spam enabled; otherwise <code>false</code>
     */
    public boolean isSpamEnabled() {
        if (null == spamHandlerFound) {
            try {
                spamHandlerFound = Boolean.valueOf(SpamHandlerRegistry.hasSpamHandler(userId, cid));
            } catch (OXException e) {
                LOG.error("", e);
            }
        }
        return ((null != spamHandlerFound && spamHandlerFound.booleanValue()) && spamEnabled);
    }

    /**
     * Checks if user has spam enabled.
     * <p>
     * <b>Note</b>: This method does not check if an appropriate spam handler is defined by user's mail provider. To reliably check full
     * spam support call {@link #isSpamEnabled()}.
     *
     * @return <code>true</code> if user has spam enabled; otherwise <code>false</code>
     */
    public boolean isSpamOptionEnabled() {
        return spamEnabled;
    }

    /**
     * Currently not used.
     */
    public boolean isTextOnlyCompose() {
        return textOnlyCompose;
    }

    /**
     * Indicates if user allows to display images which appear in HTML content of a message.
     *
     * @return <code>true</code> if user allows to display images; otherwise <code>false</code>
     */
    public boolean isAllowHTMLImages() {
        return allowHTMLImages;
    }

    /**
     * Indicates if the reply-all method will put all recipients except the original sender in the Cc list. Normally, recipients in the To
     * header of the original message will also appear in the To list.
     *
     * @return <code>true</code> if the reply-all method will put all recipients except the original sender in the Cc list; otherwise
     *         <code>false</code>.
     */
    public boolean isReplyAllCc() {
        return replyAllCc;
    }

    /**
     * Indicates if user wants to see reply quotes inside a message's content indented in a color dependent on quote level.
     *
     * @return <code>true</code> to indent in color; otherwise <code>false</code>
     */
    public boolean isUseColorQuote() {
        return useColorQuote;
    }

    /**
     * Checks if this instance of {@link UserSettingMail} is allowed to being saved to storage
     *
     * @return <code>true</code> if this instance of {@link UserSettingMail} is allowed to being saved to storage; otherwise
     *         <code>false</code>
     */
    public boolean isNoSave() {
        return noSave;
    }

    /**
     * Parses given bit pattern and applies it to this settings
     *
     * @param onOffOptions The bit pattern
     */
    public void parseBits(final int onOffOptions) {
        displayHtmlInlineContent = ((onOffOptions & INT_DISPLAY_HTML_INLINE_CONTENT) > 0);
        useColorQuote = ((onOffOptions & INT_USE_COLOR_QUOTE) > 0);
        showGraphicEmoticons = ((onOffOptions & INT_SHOW_GRAPHIC_EMOTICONS) > 0);
        hardDeleteMsgs = ((onOffOptions & INT_HARD_DELETE_MSGS) > 0);
        forwardAsAttachment = ((onOffOptions & INT_FORWARD_AS_ATTACHMENT) > 0);
        appendVCard = ((onOffOptions & INT_APPEND_VCARD) > 0);
        displayReceiptNotification = ((onOffOptions & INT_DISPLAY_RECEIPT_NOTIFICATION) > 0);
        msgPreview = ((onOffOptions & INT_MSG_PREVIEW) > 0);

        notifyAppointments = ((onOffOptions & INT_NOTIFY_APPOINTMENTS) > 0);
        notifyAppointmentsConfirmOwner = ((onOffOptions & INT_NOTIFY_APPOINTMENTS_CONFIRM_OWNER) > 0);
        notifyAppointmentsConfirmParticipant = ((onOffOptions & INT_NOTIFY_APPOINTMENTS_CONFIRM_PARTICIPANT) > 0);

        notifyTasks = ((onOffOptions & INT_NOTIFY_TASKS) > 0);
        notifyTasksConfirmOwner = ((onOffOptions & INT_NOTIFY_TASKS_CONFIRM_OWNER) > 0);
        notifyTasksConfirmParticipant = ((onOffOptions & INT_NOTIFY_TASKS_CONFIRM_PARTICIPANT) > 0);

        ignoreOriginalMailTextOnReply = ((onOffOptions & INT_IGNORE_ORIGINAL_TEXT_ON_REPLY) > 0);
        noCopyIntoStandardSentFolder = ((onOffOptions & INT_NO_COPY_INTO_SENT_FOLDER) > 0);
        spamEnabled = ((onOffOptions & INT_SPAM_ENABLED) > 0);
        textOnlyCompose = ((onOffOptions & INT_TEXT_ONLY_COMPOSE) > 0);
        allowHTMLImages = ((onOffOptions & INT_ALLOW_HTML_IMAGES) > 0);

        replyAllCc = ((onOffOptions & INT_REPLY_ALL_CC) > 0);
    }

    public void setAppendVCard(final boolean appendVCard) {
        this.appendVCard = appendVCard;
        modifiedDuringSession = true;
    }

    public void setAutoLinebreak(final int autoLineBreak) {
        autoLinebreak = autoLineBreak >= 0 ? autoLineBreak : 0;
        modifiedDuringSession = true;
    }

    public void setConfirmedHam(final String confirmedHam) {
        this.confirmedHam = confirmedHam;
    }

    public void setConfirmedSpam(final String confirmedSpam) {
        this.confirmedSpam = confirmedSpam;
    }

    public void setDisplayHtmlInlineContent(final boolean htmlPreview) {
        displayHtmlInlineContent = htmlPreview;
        modifiedDuringSession = true;
    }

    /**
     * Sets whether to suppress HTML parts in text-only mode for <i>multipart/alternative</i>.
     *
     * @param suppressHTMLAlternativePart <code>true</code> to suppress HTML parts in text-only mode for <i>multipart/alternative</i>; otherwise <code>false</code>
     */
    public void setSuppressHTMLAlternativePart(final boolean suppressHTMLAlternativePart) {
        this.suppressHTMLAlternativePart = suppressHTMLAlternativePart;
    }

    public void setDisplayMsgHeaders(final String[] displayMsgHeaders) {
        if (displayMsgHeaders == null) {
            this.displayMsgHeaders = null;
            modifiedDuringSession = true;
            return;
        }
        this.displayMsgHeaders = new String[displayMsgHeaders.length];
        System.arraycopy(displayMsgHeaders, 0, this.displayMsgHeaders, 0, displayMsgHeaders.length);
        modifiedDuringSession = true;
    }

    public void setForwardAsAttachment(final boolean forwardAsAttachment) {
        this.forwardAsAttachment = forwardAsAttachment;
        modifiedDuringSession = true;
    }

    public void setAttachOriginalMessage(final boolean attachOriginalMessage) {
        this.attachOriginalMessage = attachOriginalMessage ? 1 : 0;
        modifiedDuringSession = true;
    }

    public void setHardDeleteMsgs(final boolean hardDeleteMessages) {
        hardDeleteMsgs = hardDeleteMessages;
        modifiedDuringSession = true;
    }

    public void setIgnoreOriginalMailTextOnReply(final boolean appendOriginalMailTextToReply) {
        ignoreOriginalMailTextOnReply = appendOriginalMailTextToReply;
        modifiedDuringSession = true;
    }

    void setModifiedDuringSession(final boolean modifiedDuringSession) {
        this.modifiedDuringSession = modifiedDuringSession;
    }

    public void setMsgFormat(final int msgFormat) {
        this.msgFormat = msgFormat;
        modifiedDuringSession = true;
    }

    public void setMsgPreview(final boolean msgPreview) {
        this.msgPreview = msgPreview;
        modifiedDuringSession = true;
    }

    public void setNoCopyIntoStandardSentFolder(final boolean noCopyIntoStandardSentFolder) {
        this.noCopyIntoStandardSentFolder = noCopyIntoStandardSentFolder;
        modifiedDuringSession = true;
    }

    public void setNotifyAppointments(final boolean notifyAppointments) {
        this.notifyAppointments = notifyAppointments;
        modifiedDuringSession = true;
    }

    /**
     * Sets if the user is notified if a message contains a receipt acknowledgment.
     */
    public void setDisplayReceiptNotification(final boolean displayReceiptNotification) {
        this.displayReceiptNotification = displayReceiptNotification;
        modifiedDuringSession = true;
    }

    public void setNotifyTasks(final boolean notifyTasks) {
        this.notifyTasks = notifyTasks;
        modifiedDuringSession = true;
    }

    public void setReplyToAddr(final String replyToAddr) {
        this.replyToAddr = IDNA.toIDN(replyToAddr);
        modifiedDuringSession = true;
    }

    public void setSendAddr(final String sendAddr) {
        this.sendAddr = IDNA.toIDN(sendAddr);
        modifiedDuringSession = true;
    }

    public void setShowGraphicEmoticons(final boolean showGraphicEmoticons) {
        this.showGraphicEmoticons = showGraphicEmoticons;
        modifiedDuringSession = true;
    }

    public void setSignatures(final Signature[] signatures) {
        if (signatures == null) {
            this.signatures = null;
            modifiedDuringSession = true;
            return;
        }
        this.signatures = new Signature[signatures.length];
        System.arraycopy(signatures, 0, this.signatures, 0, signatures.length);
        modifiedDuringSession = true;
    }

    public void setSpamEnabled(final boolean spamEnabled) {
        this.spamEnabled = spamEnabled;
        modifiedDuringSession = true;
    }

    public void setStdDraftsName(final String stdDraftsName) {
        this.stdDraftsName = stdDraftsName;
        modifiedDuringSession = true;
    }

    public void setStdSentName(final String stdSentName) {
        this.stdSentName = stdSentName;
        modifiedDuringSession = true;
    }

    public void setStdSpamName(final String stdSpamName) {
        this.stdSpamName = stdSpamName;
        modifiedDuringSession = true;
    }

    public void setStdTrashName(final String stdTrashName) {
        this.stdTrashName = stdTrashName;
        modifiedDuringSession = true;
    }

    public void setTextOnlyCompose(final boolean textOnlyCompose) {
        this.textOnlyCompose = textOnlyCompose;
        modifiedDuringSession = true;
    }

    public void setAllowHTMLImages(final boolean allowHTMLImages) {
        this.allowHTMLImages = allowHTMLImages;
        modifiedDuringSession = true;
    }

    /**
     * Sets whether the reply-all method will put all recipients except the original sender in the Cc list. Normally, recipients in the To
     * header of the original message will also appear in the To list.
     *
     * @param replyAllCc <code>true</code> if the reply-all method will put all recipients except the original sender in the Cc list;
     *            otherwise <code>false</code>.
     */
    public void setReplyAllCc(final boolean replyAllCc) {
        this.replyAllCc = replyAllCc;
    }

    public void setUploadQuota(final long uploadQuota) {
        this.uploadQuota = uploadQuota;
        modifiedDuringSession = true;
    }

    public void setUploadQuotaPerFile(final long uploadQuotaPerFile) {
        this.uploadQuotaPerFile = uploadQuotaPerFile;
        modifiedDuringSession = true;
    }

    public void setUseColorQuote(final boolean useColorQuote) {
        this.useColorQuote = useColorQuote;
        modifiedDuringSession = true;
    }

    /**
     * Sets the notifyAppointmentsConfirmOwner
     *
     * @param notifyAppointmentsConfirmOwner the notifyAppointmentsConfirmOwner to set
     */
    public void setNotifyAppointmentsConfirmOwner(final boolean notifyAppointmentsConfirmOwner) {
        this.notifyAppointmentsConfirmOwner = notifyAppointmentsConfirmOwner;
        modifiedDuringSession = true;
    }

    /**
     * Sets the notifyAppointmentsConfirmParticipant
     *
     * @param notifyAppointmentsConfirmParticipant the notifyAppointmentsConfirmParticipant to set
     */
    public void setNotifyAppointmentsConfirmParticipant(final boolean notifyAppointmentsConfirmParticipant) {
        this.notifyAppointmentsConfirmParticipant = notifyAppointmentsConfirmParticipant;
        modifiedDuringSession = true;
    }

    /**
     * Sets the notifyTasksConfirmOwner
     *
     * @param notifyTasksConfirmOwner the notifyTasksConfirmOwner to set
     */
    public void setNotifyTasksConfirmOwner(final boolean notifyTasksConfirmOwner) {
        this.notifyTasksConfirmOwner = notifyTasksConfirmOwner;
        modifiedDuringSession = true;
    }

    /**
     * Sets the notifyTasksConfirmParticipant
     *
     * @param notifyTasksConfirmParticipant the notifyTasksConfirmParticipant to set
     */
    public void setNotifyTasksConfirmParticipant(final boolean notifyTasksConfirmParticipant) {
        this.notifyTasksConfirmParticipant = notifyTasksConfirmParticipant;
        modifiedDuringSession = true;
    }

    /**
     * Sets the <code>no-save</code> attribute. If set to <code>true</code> this instance of {@link UserSettingMail} cannot be saved to
     * storage.
     *
     * @param noSave <code>true</code> to deny saving this instance of {@link UserSettingMail} to storage; otherwise <code>false</code>
     */
    public void setNoSave(final boolean noSave) {
        this.noSave = noSave;
    }

    /**
     * Gets the user identifier.
     *
     * @return The user identifier
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Gets the context identifier.
     *
     * @return The context identifier
     */
    public int getCid() {
        return cid;
    }

}
