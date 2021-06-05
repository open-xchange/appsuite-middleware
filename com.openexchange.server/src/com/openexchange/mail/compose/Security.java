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

/**
 * {@link Security} - Provides security information for a message.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class Security {

    /**
     * Creates a new builder for an instance of <code>Security</code>
     *
     * @return The new builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a new builder for an instance of <code>Security</code> pre-filled with the arguments from given <code>Security</code> instance.
     *
     * @return The new builder
     * @throws IllegalArgumentException If given <code>Security</code> instance is <code>null</code>
     */
    public static Builder builder(Security other) {
        if (other == null) {
            throw new IllegalArgumentException("Security object must not be null");
        }
        return new Builder(other);
    }

    /** The builder for an instance of <code>Security</code> */
    public static class Builder {

        private boolean encrypt;
        private boolean pgpInline;
        private boolean sign;
        private String language;
        private String message;
        private String pin;
        private String msgRef;
        private String authToken;

        /**
         * Initializes a new {@link Builder}.
         */
        Builder() {
            super();
        }

        /**
         * Initializes a new {@link Builder}.
         */
        Builder(Security other) {
            this();
            encrypt = other.isEncrypt();
            pgpInline = other.isPgpInline();
            sign = other.isSign();
            language = other.getLanguage();
            message = other.getMessage();
            pin = other.getPin();
            msgRef = other.getMsgRef();
            authToken = other.getAuthToken();
        }

        public Builder withEncrypt(boolean encrypt) {
            this.encrypt = encrypt;
            return this;
        }

        public Builder withPgpInline(boolean pgpInline) {
            this.pgpInline = pgpInline;
            return this;
        }

        public Builder withSign(boolean sign) {
            this.sign = sign;
            return this;
        }

        public Builder withLanguage(String language) {
            this.language = language;
            return this;
        }

        public Builder withMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder withPin(String pin) {
            this.pin = pin;
            return this;
        }

        public Builder withMsgRef(String msgRef) {
            this.msgRef = msgRef;
            return this;
        }

        public Builder withAuthToken(String authToken) {
            this.authToken = authToken;
            return this;
        }

        public Security build() {
            return new Security(encrypt, pgpInline, sign, language, message, pin, msgRef, authToken);
        }
    }

    // ----------------------------------------------------------------------------------------------------------------------------

    /** The constant for disabled security */
    public static final Security DISABLED = new Security(false, false, false, null, null, null, null, null);

    private final boolean encrypt;
    private final boolean pgpInline;
    private final boolean sign;
    private final String language;
    private final String message;
    private final String pin;
    private final String msgRef;
    private final String authToken;

    /**
     * Initializes a new {@link Security}.
     *
     * @param encrypt <code>true</code> to encrypt; otherwise <code>false</code>
     * @param pgpInline <code>true</code> for PGP inline; otherwise <code>false</code>
     * @param sign <code>true</code> to sign; otherwise <code>false</code>
     * @param language The language identifier
     * @param message An arbitrary message
     * @param msgRef Message reference ID for guest emails
     * @param pin The PIN code
     */
    Security(boolean encrypt, boolean pgpInline, boolean sign, String language, String message, String pin, String msgRef, String authToken) {
        super();
        this.encrypt = encrypt;
        this.pgpInline = pgpInline;
        this.sign = sign;
        this.language = language;
        this.message = message;
        this.pin = pin;
        this.msgRef = msgRef;
        this.authToken = authToken;
    }

    /**
     * Signals <code>true</code> if all security options are disabled.
     *
     * @return <code>true</code> if all security options are disabled; otherwise <code>false</code>
     */
    public boolean isDisabled() {
        return false == encrypt && false == pgpInline && false == sign && null == language && null == message && null == pin;
    }

    /**
     * Gets the language
     *
     * @return The language
     */
    public String getLanguage() {
        return language;
    }

    /**
     * Gets the message
     *
     * @return The message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Gets the PIN code
     *
     * @return The PIN code
     */
    public String getPin() {
        return pin;
    }

    /**
     * Gets the encrypt flag
     *
     * @return <code>true</code> to encrypt; otherwise <code>false</code>
     */
    public boolean isEncrypt() {
        return encrypt;
    }

    /**
     * Gets the flag for PGP inline
     *
     * @return <code>true</code> for PGP inline; otherwise <code>false</code>
     */
    public boolean isPgpInline() {
        return pgpInline;
    }

    /**
     * Gets the flag whether to sign to message
     *
     * @return <code>true</code> to sign; otherwise <code>false</code>
     */
    public boolean isSign() {
        return sign;
    }

    /**
     * Gets the msg reference String
     *
     * @return
     */
    public String getMsgRef() {
        return msgRef;
    }

    /**
     * Gets the auth token.
     *
     * @return The auth token
     */
    public String getAuthToken() {
        return authToken;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (encrypt ? 1231 : 1237);
        result = prime * result + (pgpInline ? 1231 : 1237);
        result = prime * result + (sign ? 1231 : 1237);
        result = prime * result + ((language == null) ? 0 : language.hashCode());
        result = prime * result + ((message == null) ? 0 : message.hashCode());
        result = prime * result + ((msgRef == null) ? 0 : msgRef.hashCode());
        result = prime * result + ((pin == null) ? 0 : pin.hashCode());
        result = prime * result + ((authToken == null) ? 0 : authToken.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof Security)) {
            return false;
        }
        Security other = (Security) obj;
        if (encrypt != other.encrypt) {
            return false;
        }
        if (pgpInline != other.pgpInline) {
            return false;
        }
        if (sign != other.sign) {
            return false;
        }
        if (language == null) {
            if (other.language != null) {
                return false;
            }
        } else if (!language.equals(other.language)) {
            return false;
        }
        if (message == null) {
            if (other.message != null) {
                return false;
            }
        } else if (!message.equals(other.message)) {
            return false;
        }
        if (msgRef == null) {
            if (other.msgRef != null) {
                return false;
            }
        } else if (!msgRef.equals(other.msgRef)) {
            return false;
        }
        if (pin == null) {
            if (other.pin != null) {
                return false;
            }
        } else if (!pin.equals(other.pin)) {
            return false;
        }
        if (authToken == null) {
            if (other.authToken != null) {
                return false;
            }
        } else if (!authToken.equals(other.authToken)) {
            return false;
        }

        return true;
    }

    @Override
    public String toString() {
        StringBuilder builder2 = new StringBuilder();
        builder2.append("[encrypt=").append(encrypt).append(", pgpInline=").append(pgpInline).append(", sign=").append(sign).append(", ");
        if (language != null) {
            builder2.append("language=").append(language).append(", ");
        }
        if (message != null) {
            builder2.append("message=").append(message).append(", ");
        }
        if (pin != null) {
            builder2.append("pin=").append(pin);
        }
        if (msgRef != null) {
            builder2.append("msgRef=").append(msgRef);
        }
        if(authToken != null) {
            builder2.append("authToken=***");
        }
        builder2.append("]");
        return builder2.toString();
    }

}
