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

package com.openexchange.mail.json.writer;

import java.util.Collection;
import java.util.TimeZone;
import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.mime.MimeFilter;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.utils.DisplayMode;
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
        private UserSettingMail settings;
        private Collection<OXException> warnings;
        private boolean token;
        private int tokenTimeout;
        private MimeFilter mimeFilter;
        private TimeZone optTimeZone;
        private boolean exactLength;
        private int maxContentSize;
        private int maxNestedMessageLevels;
        private boolean includePlainText;

        Builder(int accountId, MailMessage mail, Session session) {
            super();
            this.accountId = accountId;
            this.mail = mail;
            this.session = session;
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
         * Sets the exactLength
         * @param exactLength The exactLength to set
         * @return This builder
         */
        public Builder setExactLength(boolean exactLength) {
            this.exactLength = exactLength;
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
            return new MessageWriterParams(accountId, mail, displayMode, embedded, session, settings, warnings, token, tokenTimeout, mimeFilter, optTimeZone, exactLength, maxContentSize, maxNestedMessageLevels, includePlainText);
        }
    }

    // --------------------------------------------------------------------------------------------------------------

    private final int accountId;
    private final MailMessage mail;
    private final DisplayMode displayMode;
    private final boolean embedded;
    private final Session session;
    private final UserSettingMail settings;
    private final Collection<OXException> warnings;
    private final boolean token;
    private final int tokenTimeout;
    private final MimeFilter mimeFilter;
    private final TimeZone optTimeZone;
    private final boolean exactLength;
    private final int maxContentSize;
    private final int maxNestedMessageLevels;
    private final boolean includePlaintext;

    /**
     * Initializes a new {@link MessageWriterParams}.
     */
    MessageWriterParams(int accountId, MailMessage mail, DisplayMode displayMode, boolean embedded, Session session, UserSettingMail settings, Collection<OXException> warnings, boolean token, int tokenTimeout, MimeFilter mimeFilter, TimeZone optTimeZone, boolean exactLength, int maxContentSize, int maxNestedMessageLevels, boolean includePlaintext) {
        super();
        this.accountId = accountId;
        this.mail = mail;
        this.displayMode = displayMode;
        this.embedded = embedded;
        this.session = session;
        this.settings = settings;
        this.warnings = warnings;
        this.token = token;
        this.tokenTimeout = tokenTimeout;
        this.mimeFilter = mimeFilter;
        this.optTimeZone = optTimeZone;
        this.exactLength = exactLength;
        this.maxContentSize = maxContentSize;
        this.maxNestedMessageLevels = maxNestedMessageLevels;
        this.includePlaintext = includePlaintext;
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
     * Gets the embedded
     *
     * @return The embedded
     */
    public boolean isEmbedded() {
        return embedded;
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
    public boolean isExactLength() {
        return exactLength;
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
