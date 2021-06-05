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

package com.openexchange.drive.client.windows.service;


/**
 * {@link Constants} provides several constants for the windows drive updater
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.1
 */
public class Constants {

    private Constants() {
        // private constructor to prevent initialization
    }

    /**
     * The updater template configuration key
     */
    public static final String TMPL_UPDATER_CONFIG = "com.openexchange.drive.updater.tmpl";
    /**
     * The updater template default value
     */
    public static final String TMPL_UPDATER_DEFAULT = "oxdrive_update.tmpl";
    /**
     * The address of the download servlet
     */
    public static final String DOWNLOAD_SERVLET = "drive/client/windows/download";
    /**
     * The address of the update servlet
     */
    public static final String UPDATE_SERVLET = "drive/client/windows/v1/update.xml";
    /**
     * The address of the install servlet
     */
    public static final String INSTALL_SERVLET = "drive/client/windows/install";

    /**
     * The configuration key of the regex expression for binary '.exe' files.
     */
    public static final String PROP_BINARY_REGEX_EXE = "com.openexchange.drive.windows.binaryRegex.exe";
    /**
     * The configuration key of the regex expression for binary '.msi' files.
     */
    public static final String PROP_BINARY_REGEX_MSI = "com.openexchange.drive.windows.binaryRegex.msi";

    public static final String AUTO_EXTRACTER = "7zS.sfx";

    /**
     * The path to the branding configurations.
     */
    public static final String BRANDINGS_PATH = "com.openexchange.drive.updater.path";
    /**
     * The branding configuration key.
     */
    public static final String BRANDING_CONF = "com.openexchange.drive.update.branding";

    /**
     * The name of the branding id field
     */
    public static final String BRANDING_ID = "id";
    /**
     * The name of the branding name field
     */
    public static final String BRANDING_NAME = "name";
    /**
     * The name of the branding version field
     */
    public static final String BRANDING_VERSION = "version";
    /**
     * The name of the branding minimum version field
     */
    public static final String BRANDING_MINIMUM_VERSION = "minimumVersion";
    /**
     * The name of the branding release date field
     */
    public static final String BRANDING_RELEASE = "release";
}
