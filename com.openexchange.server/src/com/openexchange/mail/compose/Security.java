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

    /** The builder for an instance of <code>Security</code> */
    public static class Builder {

        private boolean encrypt;
        private boolean pgpInline;
        private boolean sign;
        private String  language;
        private String  message;
        private String  pin;
        private String  msgRef;

        /**
         * Initializes a new {@link Builder}.
         */
        Builder() {
            super();
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

        public Security build() {
            return new Security(encrypt, pgpInline, sign, language, message, pin, msgRef);
        }
    }

    // ----------------------------------------------------------------------------------------------------------------------------

    /** The constant for disabled security */
    public static final Security DISABLED = new Security(false, false, false, null, null, null, null);

    private final boolean encrypt;
    private final boolean pgpInline;
    private final boolean sign;
    private final String  language;
    private final String  message;
    private final String  pin;
    private final String  msgRef;

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
    Security(boolean encrypt, boolean pgpInline, boolean sign, String language, String message, String pin, String msgRef) {
        super();
        this.encrypt = encrypt;
        this.pgpInline = pgpInline;
        this.sign = sign;
        this.language = language;
        this.message = message;
        this.pin = pin;
        this.msgRef = msgRef;
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
        builder2.append("]");
        return builder2.toString();
    }

}
