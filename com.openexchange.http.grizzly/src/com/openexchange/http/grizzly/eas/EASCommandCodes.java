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

package com.openexchange.http.grizzly.eas;

import static com.openexchange.java.Strings.toUpperCase;
import java.util.HashSet;
import java.util.Set;


/**
 * 'EAS command code' byte mapping to command naming enumeration
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public class EASCommandCodes {

    public enum EASCommands {
        FOLDER_CREATE(EASCommandCodes.FOLDER_CREATE, "FolderCreate"),
        FOLDER_DELETE(EASCommandCodes.FOLDER_DELETE, "FolderDelete"),
        FOLDER_SYNC(EASCommandCodes.FOLDER_SYNC, "FolderSync"),
        FOLDER_UPDATE(EASCommandCodes.FOLDER_UPDATE, "FolderUpdate"),
        GET_ATTACHMENT(EASCommandCodes.GET_ATTACHMENT, "GetAttachment"),
        GET_ITEM_ESTIMATE(EASCommandCodes.GET_ITEM_ESTIMATE, "GetItemEstimate"),
        ITEM_OPERATIONS(EASCommandCodes.ITEM_OPERATIONS, "ItemOperations"),
        MEETING_RESPONSE(EASCommandCodes.MEETING_RESPONSE, "MeetingResponse"),
        MOVE_ITEMS(EASCommandCodes.MOVE_ITEMS, "MoveItems"),
        PING(EASCommandCodes.PING, "Ping"),
        PROVISION(EASCommandCodes.PROVISION, "Provision"),
        RESOLVE_RECIPIENTS(EASCommandCodes.RESOLVE_RECIPIENTS, "ResolveRecipients"),
        SEARCH(EASCommandCodes.SEARCH, "Search"),
        SEND_MAIL(EASCommandCodes.SEND_MAIL, "SendMail"),
        SETTINGS(EASCommandCodes.SETTINGS, "Settings"),
        SMART_FORWARD(EASCommandCodes.SMART_FORWARD, "SmartForward"),
        SMART_REPLY(EASCommandCodes.SMART_REPLY, "SmartReply"),
        SYNC(EASCommandCodes.SYNC, "Sync"),
        VALIDATE_CERT(EASCommandCodes.VALIDATE_CERT, "ValidateCert"),

        ;

        /** The associated byte constant */
        final int theByte;

        /** The associated command name */
        final String commandName;

        private EASCommands(final int theByte, final String commandName) {
            this.theByte = theByte;
            this.commandName = commandName;
        }

        /**
         * Gets the associated bit constant.
         *
         * @return The byte
         */
        public int getByte() {
            return theByte;
        }

        /**
         * Gets the command name.
         *
         * @return The command name
         */
        public String getCommandName() {
            return commandName;
        }

        /**
         * Gets the permission by specified identifier.
         *
         * @param name The identifier
         * @return The permission or <code>null</code>
         */
        public static EASCommands get(String name) {
            if (null == name) {
                return null;
            }
            final String upperCase = toUpperCase(name);
            for (EASCommands p : values()) {
                if (p.name().equals(upperCase)) {
                    return p;
                }
            }
            return null;
        }

        /**
         * Gets the permission by specified identifier.
         *
         * @param name The identifier
         * @return The permission or <code>null</code>
         */
        public static Set<EASCommands> get(Set<String> names) {
            Set<EASCommands> commands = new HashSet<EASCommands>();

            if (null == names) {
                return commands;
            }

            for (String name : names) {
                EASCommands easCommands = EASCommands.get(name);
                if (easCommands != null) {
                    commands.add(easCommands);
                }
            }
            return commands;
        }
    };


    public static final int SYNC = 0;

    public static final int SEND_MAIL = 1;

    public static final int SMART_FORWARD = 2;

    public static final int SMART_REPLY = 3;

    public static final int GET_ATTACHMENT = 4;

    public static final int FOLDER_SYNC = 9;

    public static final int FOLDER_CREATE = 10;

    public static final int FOLDER_DELETE = 11;

    public static final int FOLDER_UPDATE = 12;

    public static final int MOVE_ITEMS = 13;

    public static final int GET_ITEM_ESTIMATE = 14;

    public static final int MEETING_RESPONSE = 15;

    public static final int SEARCH = 16;

    public static final int SETTINGS = 17;

    public static final int PING = 18;

    public static final int ITEM_OPERATIONS = 19;

    public static final int PROVISION = 20;

    public static final int RESOLVE_RECIPIENTS = 21;

    public static final int VALIDATE_CERT = 22;
}
