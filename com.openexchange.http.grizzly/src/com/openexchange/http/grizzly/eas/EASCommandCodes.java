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

package com.openexchange.http.grizzly.eas;

import static com.openexchange.java.Strings.toUpperCase;
import java.util.EnumSet;
import java.util.Set;


/**
 * 'EAS command code' byte mapping to command naming enumeration
 *
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.8.0
 */
public enum EASCommandCodes {

    FOLDER_CREATE(10, "FolderCreate"),
    FOLDER_DELETE(11, "FolderDelete"),
    FOLDER_SYNC(9, "FolderSync"),
    FOLDER_UPDATE(12, "FolderUpdate"),
    GET_ATTACHMENT(4, "GetAttachment"),
    GET_ITEM_ESTIMATE(14, "GetItemEstimate"),
    ITEM_OPERATIONS(19, "ItemOperations"),
    MEETING_RESPONSE(15, "MeetingResponse"),
    MOVE_ITEMS(13, "MoveItems"),
    PING(18, "Ping"),
    PROVISION(20, "Provision"),
    RESOLVE_RECIPIENTS(21, "ResolveRecipients"),
    SEARCH(16, "Search"),
    SEND_MAIL(1, "SendMail"),
    SETTINGS(17, "Settings"),
    SMART_FORWARD(2, "SmartForward"),
    SMART_REPLY(3, "SmartReply"),
    SYNC(0, "Sync"),
    VALIDATE_CERT(22, "ValidateCert"),

    ;

    /** The associated byte constant */
    private final int theByte;

    /** The associated command name */
    private final String commandName;

    private EASCommandCodes(final int theByte, final String commandName) {
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
    public static EASCommandCodes get(String name) {
        if (null == name) {
            return null;
        }
        final String upperCase = toUpperCase(name);
        for (EASCommandCodes p : values()) {
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
    public static Set<EASCommandCodes> get(Set<String> names) {
        Set<EASCommandCodes> commands = EnumSet.noneOf(EASCommandCodes.class);

        if (null == names) {
            return commands;
        }

        for (String name : names) {
            EASCommandCodes easCommands = EASCommandCodes.get(name);
            if (easCommands != null) {
                commands.add(easCommands);
            }
        }
        return commands;
    }
}
