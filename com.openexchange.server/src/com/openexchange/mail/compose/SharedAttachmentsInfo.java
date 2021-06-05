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
        return new Builder(null);
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
        Builder(SharedAttachmentsInfo sharedAttachmentsInfo) {
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
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (autoDelete ? 1231 : 1237);
        result = prime * result + (enabled ? 1231 : 1237);
        result = prime * result + ((expiryDate == null) ? 0 : expiryDate.hashCode());
        result = prime * result + ((language == null) ? 0 : language.hashCode());
        result = prime * result + ((password == null) ? 0 : password.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof SharedAttachmentsInfo)) {
            return false;
        }
        SharedAttachmentsInfo other = (SharedAttachmentsInfo) obj;
        if (autoDelete != other.autoDelete) {
            return false;
        }
        if (enabled != other.enabled) {
            return false;
        }
        if (expiryDate == null) {
            if (other.expiryDate != null) {
                return false;
            }
        } else if (!expiryDate.equals(other.expiryDate)) {
            return false;
        }
        if (language == null) {
            if (other.language != null) {
                return false;
            }
        } else if (!language.equals(other.language)) {
            return false;
        }
        if (password == null) {
            if (other.password != null) {
                return false;
            }
        } else if (!password.equals(other.password)) {
            return false;
        }
        return true;
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
