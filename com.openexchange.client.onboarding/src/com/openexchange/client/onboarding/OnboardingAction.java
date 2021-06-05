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

package com.openexchange.client.onboarding;


/**
 * {@link OnboardingAction} - The on-boarding action.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public enum OnboardingAction {

    /**
     * The download on-boarding action; required data/resources are provided through a file download
     */
    DOWNLOAD("download"),
    /**
     * The E-Mail on-boarding action; required data/resources are provided through an E-Mail
     */
    EMAIL("email"),
    /**
     * The SMS on-boarding action; required data/resources are provided through an SMS
     */
    SMS("sms"),
    /**
     * The display on-boarding action; required data/resources are displayed to the user
     */
    DISPLAY("display"),
    /**
     * The link on-boarding action; required data/resources are provided through an URI
     */
    LINK("link"),
    ;

    private final String id;

    private OnboardingAction(String id) {
        this.id = id;
    }

    /**
     * Gets the identifier
     *
     * @return The identifier
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the on-boarding action for specified identifier
     *
     * @param id The identifier to look-up
     * @return The associated on-boarding action or <code>null</code>
     */
    public static OnboardingAction actionFor(String id) {
        if (null == id) {
            return null;
        }

        for (OnboardingAction type : values()) {
            if (id.equals(type.getId())) {
                return type;
            }
        }
        return null;
    }

}
