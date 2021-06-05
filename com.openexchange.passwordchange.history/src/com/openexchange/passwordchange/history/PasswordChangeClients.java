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

package com.openexchange.passwordchange.history;

import java.util.LinkedList;
import java.util.List;

/**
 * {@link PasswordChangeClients} - Well known clients
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.0
 */
public enum PasswordChangeClients {

    /** Password changes transmitted through the App Suite UI */
    APP_SUITE("open-xchange-appsuite", "App Suite UI", "appsuite", "app_suite"),

    /** Password changes made by the Provisioning Interface */
    PROVISIONING("provisioning-api", "Provisioning API", "provisioning"),
    
    /** Password changes transmitted through the OX6 UI */
    OX6("com.openexchange.ox.gui.dhtml", "OX6 UI", "ox6"),
    
    /** Password changes made the OxtenderV2 */
    OXTENDER_2("OpenXchange.HTTPClient.OXAddIn", "Connector for Microsoft Outlook")

    ;

    private final String       identifier;
    private final String       displayName;
    private final List<String> matchers;

    /**
     * 
     * Initializes a new {@link PasswordChangeClients}.
     * 
     * @param identifier The identifier written to a client
     * @param displayName The human readable name of the client
     * @param matchers The possible strings to match a client with
     */
    private PasswordChangeClients(String identifier, String displayName, String... matchers) {
        this.identifier = identifier;
        this.displayName = displayName;
        this.matchers = new LinkedList<>();
        for (String matcher : matchers) {
            this.matchers.add(matcher);
        }
        this.matchers.add(identifier);
    }

    /**
     * Get the name the database uses to save the data
     * 
     * @return The identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Get a more human readable version of the identifier
     * 
     * @return A human readable representing the identifier
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Matches a string to well known client
     * 
     * @param toMatch The string to match to a client
     * @return A {@link PasswordChangeClients} or <code>null</code>
     */
    public static PasswordChangeClients match(String toMatch) {
        for (PasswordChangeClients client : PasswordChangeClients.values()) {
            for (String matcher : client.matchers) {
                if (matcher.equalsIgnoreCase(toMatch) || matcher.contains(toMatch)) {
                    return client;
                }
            }
        }
        return null;
    }
}
