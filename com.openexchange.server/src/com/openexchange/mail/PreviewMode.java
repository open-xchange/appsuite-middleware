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

package com.openexchange.mail;


/**
 * {@link PreviewMode} - The preview mode to which the IMAP connector should align.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v8.0.0
 */
public enum PreviewMode {

    /**
     * No preview support
     */
    NONE(null),
    /**
     * The PREVIEW=FIUZZY extension.
     */
    SNIPPET_FUZZY("SNIPPET=FUZZY"),
    /**
     * The PREVIEW=FIUZZY extension.
     */
    PREVIEW_FUZZY("PREVIEW=FUZZY"),
    /**
     * The PREVIEW extension according to RFC8970.
     */
    PREVIEW_RFC8970("PREVIEW"),
    ;

    private final String capabilityName;

    private PreviewMode(String capabilityName) {
        this.capabilityName = capabilityName;
    }

    /**
     * Gets the capability name for this preview mode.
     *
     * @return The capability name or <code>null</code>
     */
    public String getCapabilityName() {
        return capabilityName;
    }

}
