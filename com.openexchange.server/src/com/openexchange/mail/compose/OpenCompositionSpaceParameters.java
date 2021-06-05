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
     * @param newType The new type
     * @param mailSetting The user's mail settings
     * @return A new builder instance
     */
    public static Builder builderForNew(Type newType, UserSettingMail mailSetting) {
        return new Builder(newType == null ? Type.NEW : newType, Collections.emptyList(), mailSetting);
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
        private ClientToken clientToken;

        Builder(Type type, List<MailPath> referencedMails, UserSettingMail mailSettings) {
            super();
            this.type = type;
            this.referencedMails = referencedMails;
            this.mailSettings = mailSettings;
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

        public Builder withClientToken(ClientToken clientToken) {
            this.clientToken = clientToken;
            return this;
        }

        public OpenCompositionSpaceParameters build() {
            if (clientToken == null || clientToken.isAbsent()) {
                clientToken = ClientToken.generate();
            }
            return new OpenCompositionSpaceParameters(type, referencedMails, appendVCard, appendOriginalAttachments, priority, contentType, requestReadReceipt, sharedAttachmentsInfo, security, mailSettings, clientToken);
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
    private final ClientToken clientToken;

    /**
     * Initializes a new {@link OpenCompositionSpaceParameters}.
     */
    OpenCompositionSpaceParameters(Type type, List<MailPath> referencedMails, boolean appendVCard, boolean appendOriginalAttachments, Priority priority, ContentType contentType, boolean requestReadReceipt, SharedAttachmentsInfo sharedAttachmentsInfo, Security security, UserSettingMail mailSettings, ClientToken clientToken) {
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
        this.clientToken = clientToken;
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
     * @return The priority or <code>null</code> if not set
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

    /**
     * Gets the client token
     *
     * @return The client token
     */
    public ClientToken getClientToken() {
        return clientToken;
    }

}
