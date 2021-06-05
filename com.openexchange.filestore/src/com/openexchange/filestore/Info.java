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

package com.openexchange.filestore;


/**
 * {@link Info} - The info passed along with obtaining a certain file storage;<br>
 * e.g. revealing the intended purpose for the requested file storage representation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.4
 */
public class Info {

    private static final Info INFO_ADMINISTRATIVE = new Info(Purpose.ADMINISTRATIVE);
    private static final Info INFO_GENERAL = new Info(Purpose.GENERAL);
    private static final Info INFO_DRIVE = new Info(Purpose.DRIVE);

    /**
     * Gets the administrative info
     *
     * @return The administrative info
     */
    public static Info administrative() {
        return INFO_ADMINISTRATIVE;
    }

    /**
     * Gets the general (context-only) info
     *
     * @return The general (context-only) info
     */
    public static Info general() {
        return INFO_GENERAL;
    }

    /**
     * Gets the Drive info
     *
     * @return The Drive info
     */
    public static Info drive() {
        return INFO_DRIVE;
    }

    // -------------------------------------------------------------

    private final Purpose purpose;

    private Info(Purpose purpose) {
        super();
        this.purpose = purpose;
    }

    /**
     * Gets the purpose
     *
     * @return The purpose
     */
    public Purpose getPurpose() {
        return purpose;
    }

}
