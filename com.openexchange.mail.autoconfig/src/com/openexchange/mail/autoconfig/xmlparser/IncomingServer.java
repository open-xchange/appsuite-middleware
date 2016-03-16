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

package com.openexchange.mail.autoconfig.xmlparser;

/**
 * {@link IncomingServer}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class IncomingServer extends Server {

    private IncomingType type;

    public enum IncomingType {
        POP3(Server.POP3), IMAP(Server.IMAP);

        private final String keyword;

        private IncomingType(String keyword) {
            this.keyword = keyword;
        }

        public static IncomingType getIncomingType(String keyword) {
            if (keyword.equalsIgnoreCase(Server.POP3)) {
                return POP3;
            }
            if (keyword.equalsIgnoreCase(Server.IMAP)) {
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
