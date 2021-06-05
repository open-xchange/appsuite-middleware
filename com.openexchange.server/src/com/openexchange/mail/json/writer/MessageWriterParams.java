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

package com.openexchange.mail.json.writer;

import java.util.Collection;
import java.util.TimeZone;
import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.MimeFilter;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.utils.DisplayMode;
import com.openexchange.mail.utils.SizePolicy;
import com.openexchange.session.Session;

/**
 * {@link MessageWriterParams}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.0
 */
public class MessageWriterParams {

    /**
     * Creates a new builder instance
     *
     * @param accountId The identifier of the mail account
     * @param mail The mail to render
     * @param session The associated session
     * @return The new builder instance
     */
    public static Builder builder(int accountId, MailMessage mail, Session session) {
        return new Builder(accountId, mail, session);
    }

    /** The builder for an instance if <code>MessageWriterParams</code> */
    public static final class Builder {

        private final int accountId;
        private final MailMessage mail;
        private final Session session;
        private DisplayMode displayMode;
        private boolean embedded;
        private boolean asMarkup;
        private UserSettingMail settings;
        private Collection<OXException> warnings;
        private boolean token;
        private int tokenTimeout;
        private MimeFilter mimeFilter;
        private TimeZone optTimeZone;
        private SizePolicy sizePolicy;
        private int maxContentSize;
        private int maxNestedMessageLevels;
        private boolean includePlainText;
        private boolean sanitize;
        private boolean handleNestedMessageAsAttachment;

        Builder(int accountId, MailMessage mail, Session session) {
            super();
            this.accountId = accountId;
            this.mail = mail;
            this.session = session;
            asMarkup = true;
            sizePolicy = SizePolicy.NONE;
            sanitize = true;
            handleNestedMessageAsAttachment = false;
        }

        /**
         * Sets to handle nested messages as regular attachments.
         * @param handleNestedMessageAsAttachment The flag to set
         * @return This builder
         */
        public Builder setHandleNestedMessageAsAttachment(boolean handleNestedMessageAsAttachment) {
            this.handleNestedMessageAsAttachment = handleNestedMessageAsAttachment;
            return this;
        }

        /**
         * Sets the displayMode
         * @param displayMode The displayMode to set
         * @return This builder
         */
        public Builder setDisplayMode(DisplayMode displayMode) {
            this.displayMode = displayMode;
            return this;
        }

        /**
         * Sets the embedded
         * @param embedded The embedded to set
         * @return This builder
         */
        public Builder setEmbedded(boolean embedded) {
            this.embedded = embedded;
            return this;
        }

        /**
         * Sets the sanitize
         * @param sanitize The sanitize to set
         * @return This builder
         */
        public Builder setSanitize(boolean sanitize) {
            this.sanitize = sanitize;
            return this;
        }

        /**
         * Sets the as-markup flag
         * @param asMarkup The flag to set
         * @return This builder
         */
        public Builder setAsMarkup(boolean asMarkup) {
            this.asMarkup = asMarkup;
            return this;
        }

        /**
         * Sets the settings
         * @param settings The settings to set
         * @return This builder
         */
        public Builder setSettings(UserSettingMail settings) {
            this.settings = settings;
            return this;
        }

        /**
         * Sets the warnings
         * @param warnings The warnings to set
         * @return This builder
         */
        public Builder setWarnings(Collection<OXException> warnings) {
            this.warnings = warnings;
            return this;
        }

        /**
         * Sets the token
         * @param token The token to set
         * @return This builder
         */
        public Builder setToken(boolean token) {
            this.token = token;
            return this;
        }

        /**
         * Sets the tokenTimeout
         * @param tokenTimeout The tokenTimeout to set
         * @return This builder
         */
        public Builder setTokenTimeout(int tokenTimeout) {
            this.tokenTimeout = tokenTimeout;
            return this;
        }

        /**
         * Sets the mimeFilter
         * @param mimeFilter The mimeFilter to set
         * @return This builder
         */
        public Builder setMimeFilter(MimeFilter mimeFilter) {
            this.mimeFilter = mimeFilter;
            return this;
        }

        /**
         * Sets the optTimeZone
         * @param optTimeZone The optTimeZone to set
         * @return This builder
         */
        public Builder setOptTimeZone(TimeZone optTimeZone) {
            this.optTimeZone = optTimeZone;
            return this;
        }

        /**
         * Sets the size policy
         * @param exactLength The exactLength to set
         * @return This builder
         */
        public Builder setSizePolicy(SizePolicy sizePolicy) {
            this.sizePolicy = sizePolicy;
            return this;
        }

        /**
         * Sets the maxContentSize
         * @param maxContentSize The maxContentSize to set
         * @return This builder
         */
        public Builder setMaxContentSize(int maxContentSize) {
            this.maxContentSize = maxContentSize;
            return this;
        }

        /**
         * Sets the maxNestedMessageLevels
         * @param maxNestedMessageLevels The maxNestedMessageLevels to set
         * @return This builder
         */
        public Builder setMaxNestedMessageLevels(int maxNestedMessageLevels) {
            this.maxNestedMessageLevels = maxNestedMessageLevels;
            return this;
        }

        /**
         * Sets whether to include the plain-text version.
         * @param includePlainText
         * @return This builder
         */
        public Builder setIncludePlainText(boolean includePlainText) {
            this.includePlainText = includePlainText;
            return this;
        }

        /**
         * Creates the {@link MessageWriterParams} instance from this builder's arguments
         *
         * @return The <code>MessageWriterParams</code> instance
         */
        public MessageWriterParams build() {
            return new MessageWriterParams(accountId, mail, displayMode, sanitize, embedded, asMarkup, session, settings, warnings, token, tokenTimeout, mimeFilter, optTimeZone, sizePolicy, maxContentSize, maxNestedMessageLevels, includePlainText, handleNestedMessageAsAttachment);
        }
    }

    // --------------------------------------------------------------------------------------------------------------

    private final int accountId;
    private final MailMessage mail;
    private final DisplayMode displayMode;
    private final boolean embedded;
    private final boolean asMarkup;
    private final Session session;
    private final UserSettingMail settings;
    private final Collection<OXException> warnings;
    private final boolean token;
    private final int tokenTimeout;
    private final MimeFilter mimeFilter;
    private final TimeZone optTimeZone;
    private final SizePolicy sizePolicy;
    private final int maxContentSize;
    private final int maxNestedMessageLevels;
    private final boolean includePlaintext;
    private final boolean sanitize;
    private final boolean handleNestedMessageAsAttachment;

    /**
     * Initializes a new {@link MessageWriterParams}.
     */
    MessageWriterParams(int accountId, MailMessage mail, DisplayMode displayMode, boolean sanitize, boolean embedded, boolean asMarkup, Session session, UserSettingMail settings, Collection<OXException> warnings, boolean token, int tokenTimeout, MimeFilter mimeFilter, TimeZone optTimeZone, SizePolicy sizePolicy, int maxContentSize, int maxNestedMessageLevels, boolean includePlaintext, boolean handleNestedMessageAsAttachment) {
        super();
        this.accountId = accountId;
        this.mail = mail;
        this.displayMode = displayMode;
        this.sanitize = sanitize;
        this.embedded = embedded;
        this.asMarkup = asMarkup;
        this.session = session;
        this.settings = settings;
        this.warnings = warnings;
        this.token = token;
        this.tokenTimeout = tokenTimeout;
        this.mimeFilter = mimeFilter;
        this.optTimeZone = optTimeZone;
        this.sizePolicy = sizePolicy;
        this.maxContentSize = maxContentSize;
        this.maxNestedMessageLevels = maxNestedMessageLevels;
        this.includePlaintext = includePlaintext;
        this.handleNestedMessageAsAttachment = handleNestedMessageAsAttachment;
    }

    /**
     * Checks whether nested messages are handled as regular attachments.
     *
     * @return <code>true</code> to handle as attachments; otherwise <code>false</code>
     */
    public boolean isHandleNestedMessageAsAttachment() {
        return handleNestedMessageAsAttachment;
    }

    /**
     * Gets the accountId
     *
     * @return The accountId
     */
    public int getAccountId() {
        return accountId;
    }

    /**
     * Gets the mail
     *
     * @return The mail
     */
    public MailMessage getMail() {
        return mail;
    }

    /**
     * Gets the displayMode
     *
     * @return The displayMode
     */
    public DisplayMode getDisplayMode() {
        return displayMode;
    }

    /**
     * Gets the embedded flag.
     *
     * @return <code>true</code> for embedded display (CSS prefixed, &lt;body&gt; replaced with &lt;div&gt;); otherwise <code>false</code>
     */
    public boolean isEmbedded() {
        return embedded;
    }

    /**
     * Checks whether HTML/CSS content is supposed to be sanitized (against white-list)
     *
     * @return <code>true</code> to sanitize; otherwise <code>false</code>
     */
    public boolean isSanitize() {
        return sanitize;
    }

    /**
     * Gets the as-markup flag.
     *
     * @return <code>true</code> if the content is supposed to be rendered as HTML (be it HTML or plain text); otherwise <code>false</code> to keep content as-is (plain text is left as such)
     */
    public boolean isAsMarkup() {
        return asMarkup;
    }

    /**
     * Gets the session
     *
     * @return The session
     */
    public Session getSession() {
        return session;
    }

    /**
     * Gets the settings
     *
     * @return The settings
     */
    public UserSettingMail getSettings() {
        return settings;
    }

    /**
     * Gets the warnings
     *
     * @return The warnings
     */
    public Collection<OXException> getWarnings() {
        return warnings;
    }

    /**
     * Gets the token
     *
     * @return The token
     */
    public boolean isToken() {
        return token;
    }

    /**
     * Gets the tokenTimeout
     *
     * @return The tokenTimeout
     */
    public int getTokenTimeout() {
        return tokenTimeout;
    }

    /**
     * Gets the mimeFilter
     *
     * @return The mimeFilter
     */
    public MimeFilter getMimeFilter() {
        return mimeFilter;
    }

    /**
     * Gets the optTimeZone
     *
     * @return The optTimeZone
     */
    public TimeZone getOptTimeZone() {
        return optTimeZone;
    }

    /**
     * Gets the exactLength
     *
     * @return The exactLength
     */
    public SizePolicy getSizePolicy() {
        return sizePolicy;
    }

    /**
     * Gets the maxContentSize
     *
     * @return The maxContentSize
     */
    public int getMaxContentSize() {
        return maxContentSize;
    }

    /**
     * Gets the maxNestedMessageLevels
     *
     * @return The maxNestedMessageLevels
     */
    public int getMaxNestedMessageLevels() {
        return maxNestedMessageLevels;
    }

    /**
     * Gets the includePlaintext
     *
     * @return The includePlaintext
     */
    public boolean isIncludePlaintext() {
        return includePlaintext;
    }

}
