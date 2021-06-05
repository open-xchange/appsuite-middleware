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

package com.openexchange.tools.oxfolder;

/**
 * {@link OXFolderDefaultMode}
 * Describes all possible default folder modes on user creation.
 *
 * @author <a href="mailto:vitali.sjablow@open-xchange.com">Vitali Sjablow</a>
 * @since v7.8.4
 */
public enum OXFolderDefaultMode {
    DEFAULT("default"),
    NORMAL("normal"),
    NONE("none");

    private String text;

    private OXFolderDefaultMode(String text) {
        this.text = text;
    }

    public String getText() {
        return text;
    }

    /**
     * Translates the given text to a corresponding {@link OXFolderDefaultMode}
     * 
     * @param text, the text to handle
     * @return the {@link OXFolderDefaultMode} enumeration value
     */
    public static OXFolderDefaultMode fromString(String text) {
        for (OXFolderDefaultMode mode : OXFolderDefaultMode.values()) {
            if (text.equalsIgnoreCase(mode.getText())) {
                return mode;
            }
        }
        return null;
    }
}
