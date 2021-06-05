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

package com.openexchange.find.drive;

import com.openexchange.i18n.LocalizableStrings;


/**
 * {@link DriveStrings}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class DriveStrings implements LocalizableStrings {

    // Search in file name.
    public static final String FACET_FILE_NAME = "File name";

    // Search in file content.
    public static final String FACET_FILE_CONTENT = "File content";

    // Search in folders.
    public static final String FACET_FOLDERS = "Folders";

    // Search in Persons.
    public static final String FACET_CONTACTS = "Contacts";

    // Search in folder type (private, public, shared, external)
    public static final String FACET_FOLDER_TYPE = "Folder type";

    // Search in file description.
    public static final String FACET_FILE_DESCRIPTION = "File description";

    // Search in file type.
    public static final String FACET_FILE_TYPE = "File type";

    // Search in file size
    public static final String FACET_FILE_SIZE = "File size";

    // Search in folder name
    public static final String FACET_FOLDER_NAME = "Folder name";

    // -------------------------- i18n strings for file types ---------------------------- //

    public static final String FILE_TYPE_IMAGES = "Images";

    public static final String FILE_TYPE_AUDIO = "Audio";

    public static final String FILE_TYPE_VIDEO = "Video";

    public static final String FILE_TYPE_DOCUMENTS = "Documents";

    public static final String FILE_TYPE_OTHER = "Other";

    // ---------------------------------------------------------------------------------- //

    // Public folders
    public static final String FOLDER_TYPE_PUBLIC = "Public folders";

    // Private folders
    public static final String FOLDER_TYPE_PRIVATE = "Private folders";

    // Shared folders
    public static final String FOLDER_TYPE_SHARED = "Shared folders";

    // Context: Searching in drive.
    // Displayed as: [Search for] 'user input' in file name.
    // The 'user input' part is always prepended, please heed this in translations.
    public static final String SEARCH_IN_FILE_NAME = "in file name";

    // Context: Searching in drive.
    // Displayed as: [Search for] 'user input' in file description.
    // The 'user input' part is always prepended, please heed this in translations.
    public static final String SEARCH_IN_FILE_DESC = "in file description";

    // Context: Searching in drive.
    // Displayed as: [Search for] 'user input' in file content.
    // The 'user input' part is always prepended, please heed this in translations.
    public static final String SEARCH_IN_FILE_CONTENT = "in file content";

    // Context: Searching in drive.
    // Displayed as: [Search for] 'user input' in folder name.
    // The 'user input' part is always prepended, please heed this in translations.
    public static final String SEARCH_IN_FOLDER_NAME = "in folder name";

}
