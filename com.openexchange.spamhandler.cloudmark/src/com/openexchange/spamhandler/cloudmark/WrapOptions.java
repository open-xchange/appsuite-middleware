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
