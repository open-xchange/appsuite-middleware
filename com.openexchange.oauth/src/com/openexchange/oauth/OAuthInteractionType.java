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

package com.openexchange.oauth;

/**
 * {@link OAuthInteractionType}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum OAuthInteractionType {

    /**
     * User needs to enter a manually received PIN.
     * <p>
     * Needed arguments for {@link OAuthService#createAccount(String, OAuthInteractionType, java.util.Map, int, int) createAccount()}:
     * <ul>
     * <li>PIN</li>
     * <li>Request token</li>
     * </ul>
     *
     * @see OAuthConstants#ARGUMENT_PIN
     * @see OAuthConstants#ARGUMENT_REQUEST_TOKEN
     */
    OUT_OF_BAND("outOfBand"),
    /**
     * No user action needed.
     */
    CALLBACK("callback");

    private final String name;

    private OAuthInteractionType(final String name) {
        this.name = name;
    }

    /**
     * Gets the name
     *
     * @return The name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the interaction type for given name.
     *
     * @param name The name
     * @return The interaction type or <code>null</code> if none matches
     */
    public static OAuthInteractionType typeFor(final String name) {
        if (null == name) {
            return null;
        }
        final OAuthInteractionType[] values = OAuthInteractionType.values();
        for (final OAuthInteractionType type : values) {
            if (type.name.equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }

}
