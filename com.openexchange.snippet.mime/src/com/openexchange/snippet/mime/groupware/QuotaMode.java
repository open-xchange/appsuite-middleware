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

package com.openexchange.snippet.mime.groupware;

import com.openexchange.java.Strings;

/**
 * {@link QuotaMode} - The quota mode for MIME-backed snippets.
 * <p>
 * {@link #CONTEXT} quota mode is the default.
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.0
 */
public enum QuotaMode {

    /** MIME-backed snippets do contribute to the quota of the context-associated file storage (default) */
    CONTEXT("context"),
    /** MIME-backed snippets do use a dedicated quota and thus do <b><i>not</i></b> contribute to the quota of the context-associated file storage */
    DEDICATED("dedicated");

    private final String name;

    private QuotaMode(String name) {
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
     * Gets the quota mode for specified name.
     *
     * @param name The name to look-up
     * @return The name-associated quota mode <i>or</i> {@link #CONTEXT} if name is unknown/invalid
     */
    public static QuotaMode getModeByName(String name) {
        if (Strings.isEmpty(name)) {
            return CONTEXT;
        }
        for (QuotaMode mode : QuotaMode.values()) {
            if (mode.name.equalsIgnoreCase(name)) {
                return mode;
            }
        }
        return CONTEXT;
    }

}
