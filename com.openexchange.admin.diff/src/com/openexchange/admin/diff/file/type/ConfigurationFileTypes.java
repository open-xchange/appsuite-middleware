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

package com.openexchange.admin.diff.file.type;

/**
 * Defines all configuration files that are handled from the configuration diff tool.
 * 
 * @author <a href="mailto:martin.schneider@open-xchange.com">Martin Schneider</a>
 * @since 7.6.1
 */
public enum ConfigurationFileTypes {

    PROPERTY("properties"),

    IN("in"),

    YAML("yaml"),

    YML("yml"),

    CONF("conf"),

    CNF("cnf"),

    CCF("ccf"),

    XML("xml"),

    SH("sh"),

    PERFMAP("perfMap"),

    TYPES("types"),

    NO_EXTENSION(""),

    ;

    /**
     * Array with all configuration files that can be diffed.
     */
    public static String[] CONFIGURATION_FILE_TYPE = new String[ConfigurationFileTypes.values().length];
    static {
        for (int i = 0; i < ConfigurationFileTypes.values().length; i++) {
            CONFIGURATION_FILE_TYPE[i] = ConfigurationFileTypes.values()[i].fileExtension;
        }
    }

    /**
     * Array with all configuration files that can be diffed with dot.
     */
    public static String[] CONFIGURATION_FILE_TYPE_WITH_DOT = new String[ConfigurationFileTypes.values().length];
    static {
        for (int i = 0; i < ConfigurationFileTypes.values().length; i++) {
            String fileExtension = ConfigurationFileTypes.values()[i].fileExtension;
            if (!fileExtension.equalsIgnoreCase("")) {
                fileExtension = "." + fileExtension;
            }
            CONFIGURATION_FILE_TYPE_WITH_DOT[i] = fileExtension;
        }
    }

    /**
     * The extension of the file
     */
    private final String fileExtension;

    /**
     * Initializes a new {@link ConfigurationFileTypes}.
     * 
     * @param fileExtension
     */
    private ConfigurationFileTypes(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public final String getFileExtension() {
        return fileExtension;
    }
}
