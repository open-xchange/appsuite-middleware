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

package com.openexchange.client.onboarding.plist;

import java.util.Optional;
import com.openexchange.java.Strings;

/**
 * {@link PlistScenarioType} - An enumeration of types (or identifiers) for synthetic (non-configured) scenarios that yield a PLIST dictionary.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.4
 */
public enum PlistScenarioType {

    /**
     * The synthetic scenario identifier for yielding a PLIST dictionary for a CalDAV profile.
     */
    CALDAV("caldav"),
    /**
     * The synthetic scenario identifier for yielding a PLIST dictionary for a CardDAV profile.
     */
    CARDDAV("carddav"),
    /**
     * The synthetic scenario identifier for yielding a PLIST dictionary for a CalDAV &amp; CardDAV profile.
     */
    DAV("dav"),
    /**
     * The synthetic scenario identifier for yielding a PLIST dictionary for an Email profile.
     */
    MAIL("mail"),
    ;

    private final String scenarioId;

    private PlistScenarioType(String scenarioId) {
        this.scenarioId = scenarioId;
    }

    /**
     * Gets the identifier for this synthetic scenario.
     *
     * @return The scenario identifier
     */
    public String getScenarioId() {
        return scenarioId;
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Gets the suitable synthetic PLIST scenario type for given scenario identifier.
     *
     * @param scenarioId The scenario identifier to look-up by
     * @return The PLIST scenario type or empty
     */
    public static Optional<PlistScenarioType> plistScenarioTypeFor(String scenarioId) {
        if (Strings.isEmpty(scenarioId)) {
            return Optional.empty();
        }

        String lookUp = Strings.asciiLowerCase(scenarioId).trim();
        for (PlistScenarioType plistScenarioType : PlistScenarioType.values()) {
            if (lookUp.equals(plistScenarioType.scenarioId)) {
                return Optional.of(plistScenarioType);
            }
        }
        return Optional.empty();
    }
}
