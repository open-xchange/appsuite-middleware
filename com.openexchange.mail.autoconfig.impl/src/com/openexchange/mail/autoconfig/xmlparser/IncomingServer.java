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

package com.openexchange.mail.autoconfig.xmlparser;

/**
 * {@link IncomingServer}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class IncomingServer extends Server {

    private IncomingType type;

    public enum IncomingType {
        POP3(Server.POP3_STRING), IMAP(Server.IMAP_STRING);

        private final String keyword;

        private IncomingType(String keyword) {
            this.keyword = keyword;
        }

        public static IncomingType getIncomingType(String keyword) {
            if (keyword.equalsIgnoreCase(Server.POP3_STRING)) {
                return POP3;
            }
            if (keyword.equalsIgnoreCase(Server.IMAP_STRING)) {
                return IMAP;
            }
            return null;
        }

        /**
         * Gets the keyword
         *
         * @return The keyword
         */
        public String getKeyword() {
            return keyword;
        }
    }

    /**
     * Gets the type
     *
     * @return The type
     */
    public IncomingType getType() {
        return type;
    }

    /**
     * Sets the type
     *
     * @param type The type to set
     */
    @Override
    public void setType(String type) {
        this.type = IncomingType.getIncomingType(type);
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder(1024);
        builder.append("IncomingServer [");
        if (getHostname() != null) {
            builder.append("hostname=").append(getHostname()).append(", ");
        }
        builder.append("port=").append(getPort()).append(", ");
        if (getSocketType() != null) {
            builder.append("socketType=").append(getSocketType().getKeyword()).append(", ");
        }
        if (getUsername() != null) {
            builder.append("username=").append(getUsername()).append(", ");
        }
        if (getAuthentication() != null) {
            builder.append("authentication=").append(getAuthentication());
        }
        if (type != null) {
            builder.append("type=").append(type);
        }
        builder.append("]");
        return builder.toString();
    }

}
