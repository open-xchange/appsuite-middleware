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

package com.openexchange.spamhandler.cloudmark;

/**
 * {@link WrapOptions} - Specifies wrapping options for the message that is forwarded to spam/ham address.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class WrapOptions {

    /**
     * Creates a new builder instance.
     *
     * @return The new builder instance
     */
    public static Builder builder() {
        return new Builder();
    }

    /** The builder for an instance of <code>WrapOptions</code> */
    public static class Builder {

        private boolean wrap;
        private String subject;

        /**
         * Initializes a new {@link Builder}.
         */
        Builder() {
            super();
            wrap = false;
            subject = null;
        }

        /**
         * Sets the wrap
         *
         * @param wrap The wrap to set
         * @return This builder
         */
        public Builder withWrap(boolean wrap) {
            this.wrap = wrap;
            return this;
        }

        /**
         * Sets the subject
         *
         * @param subject The subject to set
         * @return This builder
         */
        public Builder withSubject(String subject) {
            this.subject = subject;
            return this;
        }

        public WrapOptions build() {
            return new WrapOptions(wrap, subject);
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------------

    private final boolean wrap;
    private final String subject;

    /**
     * Initializes a new {@link WrapOptions}.
     */
    WrapOptions(boolean wrap, String subject) {
        super();
        this.wrap = wrap;
        this.subject = subject;
    }

    /**
     * Checks if forwarded message is supposed to be wrapped.
     *
     * @return <code>true</code> to wrap; otherwiae <code>false</code>
     */
    public boolean isWrap() {
        return wrap;
    }

    /**
     * Gets the optional subject for the wrapped message.
     *
     * @return The subject or <code>null</code>
     */
    public String optSubject() {
        return subject;
    }

}
