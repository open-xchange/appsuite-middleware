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

import java.util.Date;
import java.util.Locale;

/**
 * {@link SharedAttachmentsInfo} - Represents the information for sending shared attachments.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class SharedAttachmentsInfo {

    /**
     * Creates a new builder for an instance of <code>SharedAttachmentsInfo</code>
     *
     * @return The new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /** The builder for an instance of <code>SharedAttachmentsInfo</code> */
    public static class Builder {

        private Locale language;
        private boolean enabled;
        private boolean autoDelete;
        private Date expiryDate;
        private String password;

        /**
         * Initializes a new {@link Builder}.
         */
        Builder() {
            super();
        }

        public Builder withLanguage(Locale language) {
            this.language = language;
            return this;
        }

        public Builder withEnabled(boolean enabled) {
            this.enabled = enabled;
            return this;
        }

        public Builder withAutoDelete(boolean autoDelete) {
            this.autoDelete = autoDelete;
            return this;
        }

        public Builder withExpiryDate(Date expiryDate) {
            this.expiryDate = expiryDate;
            return this;
        }

        public Builder withPassword(String password) {
            this.password = password;
            return this;
        }

        public SharedAttachmentsInfo build() {
            return new SharedAttachmentsInfo(language, enabled, autoDelete, expiryDate, password);
        }
    }

    // ----------------------------------------------------------------------------------------------------------------------------

    /** The constant for disabled shared attachments */
    public static final SharedAttachmentsInfo DISABLED = new SharedAttachmentsInfo(null, false, false, null, null);

    private final Locale language;
    private final boolean enabled;
    private final boolean autoDelete;
    private final Date expiryDate;
    private final String password;

    SharedAttachmentsInfo(Locale locale, boolean enabled, boolean autoDelete, Date expiryDate, String password) {
        super();
        this.language = locale;
        this.enabled = enabled;
        this.autoDelete = autoDelete;
        this.expiryDate = expiryDate;
        this.password = password;
    }

    /**
     * Gets the language
     *
     * @return The language
     */
    public Locale getLanguage() {
        return language;
    }

    /**
     * Gets the disabled flag
     *
     * @return The disabled flag
     */
    public boolean isDisabled() {
        return !enabled;
    }

    /**
     * Gets the enabled flag
     *
     * @return The enabled flag
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Gets the auto-delete flag
     *
     * @return The auto-delete flag
     */
    public boolean isAutoDelete() {
        return autoDelete;
    }

    /**
     * Gets the expiry date
     *
     * @return The expiry date
     */
    public Date getExpiryDate() {
        return expiryDate;
    }

    /**
     * Gets the password
     *
     * @return The password
     */
    public String getPassword() {
        return password;
    }

    @Override
    public String toString() {
        StringBuilder builder2 = new StringBuilder();
        builder2.append("[");
        if (language != null) {
            builder2.append("language=").append(language).append(", ");
        }
        builder2.append("enabled=").append(enabled).append(", autoDelete=").append(autoDelete).append(", ");
        if (expiryDate != null) {
            builder2.append("expiryDate=").append(expiryDate).append(", ");
        }
        if (password != null) {
            builder2.append("password=").append(password);
        }
        builder2.append("]");
        return builder2.toString();
    }

}
