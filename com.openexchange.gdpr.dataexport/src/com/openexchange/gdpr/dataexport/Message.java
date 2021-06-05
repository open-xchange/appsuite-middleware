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

    public static enum Type {
        /** The neutral type */
        NEUTRAL,
        /** The type to signal "permission denied" error */
        PERMISSION_DENIED,
        ;
    }

    /**
     * Creates a new builder for an instance of <code>Message</code>.
     *
     * @return The builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Creates a new builder for an instance of <code>Message</code> having message type preset to "permission denied".
     *
     * @return The builder
     */
    public static Builder builderWithPermissionDeniedType() {
        return new Builder().withType(Type.PERMISSION_DENIED);
    }

    /** The builder for an instance of <code>Message</code> */
    public static class Builder {

        private Date timeStamp;
        private String moduleId;
        private final StringBuilder messageBuilder;
        private UUID id;
        private Type type;

        /**
         * Initializes a new {@link Builder}.
         */
        Builder() {
            super();
            timeStamp = new Date();
            messageBuilder = new StringBuilder();
            type = Type.NEUTRAL;
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
         * Sets the message type
         *
         * @param type The type
         * @return This builder
         */
        public Builder withType(Type type) {
            this.type = type;
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
            return new Message(id, messageBuilder.toString(), timeStamp, moduleId, type);
        }
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private final UUID id;
    private final String message;
    private final Date timeStamp;
    private final String moduleId;
    private final Type type;
    private final int hash;

    /**
     * Initializes a new {@link Message}.
     *
     * @param id The message identifier
     * @param message The message
     * @param timeStamp The time stamp
     * @param moduleId The module identifier
     * @param type The message type
     */
    Message(UUID id, String message, Date timeStamp, String moduleId, Type type) {
        super();
        this.id = id;
        this.message = message;
        this.timeStamp = timeStamp;
        this.moduleId = moduleId;
        this.type = type;
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
     * Gets the message type.
     *
     * @return The type
     */
    public Type getType() {
        return type;
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
