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

package com.openexchange.gdpr.dataexport;

import java.util.Date;
import java.util.UUID;

/**
 * {@link Message} - Represents a message that is supposed to be added to data export diagnostics report.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public class Message {

    /**
     * Creates a new builder for an instance of <code>Message</code>.
     *
     * @return The builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /** The builder for an instance of <code>Message</code> */
    public static class Builder {

        private Date timeStamp;
        private String moduleId;
        private final StringBuilder messageBuilder;
        private UUID id;

        /**
         * Initializes a new {@link Builder}.
         */
        Builder() {
            super();
            timeStamp = new Date();
            messageBuilder = new StringBuilder();
        }

        /**
         * Sets the message identifier
         * <p>
         * If not set, a random identifier is assigned to crafted message.
         *
         * @param id The message identifier to set
         * @return This builder
         */
        public Builder withId(UUID id) {
            this.id = id;
            return this;
        }

        /**
         * Sets the message.
         * <p>
         * Note: Message is completely reset to given one.
         *
         * @param message The message to set
         * @return This builder
         */
        public Builder withMessage(String message) {
            messageBuilder.setLength(0);
            if (message != null) {
                messageBuilder.append(message);
            }
            return this;
        }

        /**
         * Appends the given text to this builder's message.
         *
         * @param text The text to append
         * @return This builder
         */
        public Builder appendToMessage(String text) {
            if (text != null) {
                messageBuilder.append(text);
            }
            return this;
        }

        /**
         * Appends the given integer to this builder's message.
         *
         * @param i The integer to append
         * @return This builder
         */
        public Builder appendToMessage(int i) {
            messageBuilder.append(i);
            return this;
        }

        /**
         * Sets the time stamp
         *
         * @param timestamp The time stamp to set
         * @return This builder
         */
        public Builder withTimeStamp(Date timeStamp) {
            this.timeStamp = timeStamp;
            return this;
        }

        /**
         * Sets the module identifier
         *
         * @param moduleId The module identifier to set
         * @return This builder
         */
        public Builder withModuleId(String moduleId) {
            this.moduleId = moduleId;
            return this;
        }

        /**
         * Creates the instance of <code>Message</code> from this builder's arguments
         *
         * @return The <code>Message</code> instance
         */
        public Message build() {
            if (id == null) {
                id = UUID.randomUUID();
            }
            return new Message(id, messageBuilder.toString(), timeStamp, moduleId);
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final UUID id;
    private final String message;
    private final Date timeStamp;
    private final String moduleId;
    private final int hash;

    /**
     * Initializes a new {@link Message}.
     *
     * @param id The message identifier
     * @param message The message
     * @param timeStamp The time stamp
     * @param moduleId The module identifier
     */
    Message(UUID id, String message, Date timeStamp, String moduleId) {
        super();
        this.id = id;
        this.message = message;
        this.timeStamp = timeStamp;
        this.moduleId = moduleId;
        int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        this.hash = result;
    }

    /**
     * Gets the message identifier
     *
     * @return The message identifier
     */
    public UUID getId() {
        return id;
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
     * Gets the time stamp
     *
     * @return The time stamp
     */
    public Date getTimeStamp() {
        return timeStamp;
    }

    /**
     * Gets the module identifier
     *
     * @return The module identifier
     */
    public String getModuleId() {
        return moduleId;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb.append('[');
        if (message != null) {
            sb.append("message=``").append(message).append("\u00b4\u00b4, ");
        }
        if (timeStamp != null) {
            sb.append("timeStamp=\"").append(timeStamp).append("\", ");
        }
        if (moduleId != null) {
            sb.append("moduleId=\"").append(moduleId).append("\"");
        }
        sb.append(']');
        return sb.toString();
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Message other = (Message) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        } else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

}
