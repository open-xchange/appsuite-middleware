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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.mail.compose;

import java.util.Collections;
import java.util.List;
import com.google.common.collect.ImmutableList;
import com.openexchange.mail.MailPath;
import com.openexchange.mail.compose.Message.ContentType;
import com.openexchange.mail.compose.Message.Priority;
import com.openexchange.mail.usersetting.UserSettingMail;

/**
 * {@link OpenCompositionSpaceParameters} - Provides parameters for opening a new composition space.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class OpenCompositionSpaceParameters {

    /**
     * Creates a new builder instance for composing a new message.
     *
     * @param mailSetting The user's mail settings
     * @return A new builder instance
     */
    public static Builder builderForNew(UserSettingMail mailSetting) {
        return new Builder(Type.NEW, Collections.emptyList(), mailSetting);
    }

    /**
     * Creates a new builder instance for composing a reply message.
     *
     * @param replyAll <code>true</code> to indicate reply-all; otherwise <code>false</code>
     * @param replyFor The path for the original message
     * @param mailSetting The user's mail settings
     * @return A new builder instance
     * @throws IllegalArgumentException If path for the original message is <code>null</code>
     */
    public static Builder builderForReply(boolean replyAll, MailPath replyFor, UserSettingMail mailSetting) {
        if (null == replyFor) {
            throw new IllegalArgumentException("Mail path must not be null");
        }
        return new Builder(replyAll ? Type.REPLY_ALL : Type.REPLY, Collections.singletonList(replyFor), mailSetting);
    }

    /**
     * Creates a new builder instance for composing a forward message.
     *
     * @param forwardsFor The paths for the original messages
     * @param mailSetting The user's mail settings
     * @return A new builder instance
     * @throws IllegalArgumentException If paths for the original messages is <code>null</code> or empty
     */
    public static Builder builderForForward(List<MailPath> forwardsFor, UserSettingMail mailSetting) {
        if (null == forwardsFor|| forwardsFor.isEmpty()) {
            throw new IllegalArgumentException("Mail paths must not be null or empty");
        }
        return new Builder(Type.FORWARD, ImmutableList.copyOf(forwardsFor), mailSetting);
    }

    /**
     * Creates a new builder instance for composing a draft-edit message.
     *
     * @param editFor The path for the original message
     * @param mailSetting The user's mail settings
     * @return A new builder instance
     * @throws IllegalArgumentException If path for the original message is <code>null</code>
     */
    public static Builder builderForEdit(MailPath editFor, UserSettingMail mailSetting) {
        if (null == editFor) {
            throw new IllegalArgumentException("Mail path must not be null");
        }
        return new Builder(Type.EDIT, Collections.singletonList(editFor), mailSetting);
    }

    /**
     * Creates a new builder instance for composing a draft-copy message.
     *
     * @param copyFor The path for the original message
     * @param mailSetting The user's mail settings
     * @return A new builder instance
     * @throws IllegalArgumentException If path for the original message is <code>null</code>
     */
    public static Builder builderForCopy(MailPath copyFor, UserSettingMail mailSetting) {
        if (null == copyFor) {
            throw new IllegalArgumentException("Mail path must not be null");
        }
        return new Builder(Type.COPY, Collections.singletonList(copyFor), mailSetting);
    }

    /**
     * Creates a new builder instance for composing a re-send message.
     *
     * @param resendFor The path for the original message
     * @param mailSetting The user's mail settings
     * @return A new builder instance
     * @throws IllegalArgumentException If path for the original message is <code>null</code>
     */
    public static Builder builderForResend(MailPath resendFor, UserSettingMail mailSetting) {
        if (null == resendFor) {
            throw new IllegalArgumentException("Mail path must not be null");
        }
        return new Builder(Type.RESEND, Collections.singletonList(resendFor), mailSetting);
    }

    /** The builder for an instance of <code>OpenCompositionSpaceParameters</code> */
    public static class Builder {

        private final Type type;
        private final List<MailPath> referencedMails;
        private final UserSettingMail mailSettings;
        private boolean appendVCard;
        private boolean appendOriginalAttachments;
        private Priority priority;
        private ContentType contentType;
        private boolean requestReadReceipt;
        private SharedAttachmentsInfo sharedAttachmentsInfo;
        private Security security;

        Builder(Type type, List<MailPath> referencedMails, UserSettingMail mailSettings) {
            super();
            this.type = type;
            this.referencedMails = referencedMails;
            this.mailSettings = mailSettings;
            priority = Priority.NORMAL;
            sharedAttachmentsInfo = SharedAttachmentsInfo.DISABLED;
            security = Security.DISABLED;
        }

        public Builder withSharedAttachmentsInfo(SharedAttachmentsInfo sharedAttachmentsInfo) {
            this.sharedAttachmentsInfo = sharedAttachmentsInfo;
            return this;
        }

        public Builder withSecurity(Security security) {
            this.security = security;
            return this;
        }

        public Builder withRequestReadReceipt(boolean requestReadReceipt) {
            this.requestReadReceipt = requestReadReceipt;
            return this;
        }

        public Builder withAppendVCard(boolean appendVCard) {
            this.appendVCard = appendVCard;
            return this;
        }

        public Builder withAppendOriginalAttachments(boolean appendOriginalAttachments) {
            this.appendOriginalAttachments = appendOriginalAttachments;
            return this;
        }

        public Builder withPriority(Priority priority) {
            this.priority = priority;
            return this;
        }

        public Builder withContentType(ContentType contentType) {
            this.contentType = contentType;
            return this;
        }

        public OpenCompositionSpaceParameters build() {
            return new OpenCompositionSpaceParameters(type, referencedMails, appendVCard, appendOriginalAttachments, priority, contentType, requestReadReceipt, sharedAttachmentsInfo, security, mailSettings);
        }

    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final Type type;
    private final List<MailPath> referencedMails;
    private final boolean appendVCard;
    private final boolean appendOriginalAttachments;
    private final boolean requestReadReceipt;
    private final Priority priority;
    private final ContentType contentType;
    private final SharedAttachmentsInfo sharedAttachmentsInfo;
    private final Security security;
    private final UserSettingMail mailSettings;

    /**
     * Initializes a new {@link OpenCompositionSpaceParameters}.
     */
    OpenCompositionSpaceParameters(Type type, List<MailPath> referencedMails, boolean appendVCard, boolean appendOriginalAttachments, Priority priority, ContentType contentType, boolean requestReadReceipt, SharedAttachmentsInfo sharedAttachmentsInfo, Security security, UserSettingMail mailSettings) {
        super();
        this.type = type;
        this.referencedMails = referencedMails;
        this.appendVCard = appendVCard;
        this.appendOriginalAttachments = appendOriginalAttachments;
        this.priority = priority;
        this.contentType = contentType;
        this.requestReadReceipt = requestReadReceipt;
        this.sharedAttachmentsInfo = sharedAttachmentsInfo;
        this.security = security;
        this.mailSettings = mailSettings;
    }

    /**
     * Gets the user's mail settings
     *
     * @return The mail settings
     */
    public UserSettingMail getMailSettings() {
        return mailSettings;
    }

    /**
     * Gets the security
     *
     * @return The security
     */
    public Security getSecurity() {
        return security;
    }

    /**
     * Gets the shared attachments information
     *
     * @return The shared attachments information
     */
    public SharedAttachmentsInfo getSharedAttachmentsInfo() {
        return sharedAttachmentsInfo;
    }

    /**
     * Gets the request-read-receipt flag
     *
     * @return <code>true</code> to boolean request a read receipt; otherwise <code>false</code>
     */
    public boolean isRequestReadReceipt() {
        return requestReadReceipt;
    }

    /**
     * Gets the type
     *
     * @return The type
     */
    public Type getType() {
        return type;
    }

    /**
     * Gets the paths to the referenced mails
     * <p>
     * <div style="margin-left: 0.1in; margin-right: 0.5in; margin-bottom: 0.1in; background-color:#FFDDDD;">
     * Required if {@link #getType() type} is not {@link Type#NEW NEW}.
     * </div>
     *
     * @return The paths to the referenced mails
     */
    public List<MailPath> getReferencedMails() {
        return referencedMails;
    }

    /**
     * Checks whether user's vCard is supposed to be attached.
     *
     * @return <code>true</code> to attach user's vCard; otherwise <code>false</code>
     */
    public boolean isAppendVCard() {
        return appendVCard;
    }

    /**
     * Whether the attachments from referenced mail are supposed to be appended.
     *
     * @return <code>true</code> to append original attachments; otherwise <code>false</code>
     */
    public boolean isAppendOriginalAttachments() {
        return appendOriginalAttachments;
    }

    /**
     * Gets the priority
     *
     * @return The priority
     */
    public Priority getPriority() {
        return priority;
    }

    /**
     * Gets the content type
     *
     * @return The content type
     */
    public ContentType getContentType() {
        return contentType;
    }

}
