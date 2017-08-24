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
