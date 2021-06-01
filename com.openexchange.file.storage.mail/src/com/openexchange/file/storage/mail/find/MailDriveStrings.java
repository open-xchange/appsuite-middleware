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

package com.openexchange.file.storage.mail.find;

import com.openexchange.i18n.LocalizableStrings;


/**
 * {@link MailDriveStrings}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class MailDriveStrings implements LocalizableStrings {

    // Search in file name.
    public static final String FACET_FILE_NAME = "File name";

    // Search in From sender.
    public static final String FACET_FROM = "From";

    // Search in To recipient.
    public static final String FACET_TO = "To";

    // Search in original mail subject
    public static final String FACET_SUBJECT = "Subject";

    // Search in file type.
    public static final String FACET_FILE_TYPE = "File type";

    // Search in file size
    public static final String FACET_FILE_SIZE = "File size";

    // ---------------------------------------------------------------------------------- //

    // Context: Searching in drive.
    // Displayed as: [Search for] 'user input' in file name.
    // The 'user input' part is always prepended, please heed this in translations.
    public static final String SEARCH_IN_FILE_NAME = "in file name";

    // Context: Searching in drive.
    // Displayed as: [Search for] 'user input' in From sender.
    // The 'user input' part is always prepended, please heed this in translations.
    public static final String SEARCH_IN_FROM= "in From sender";

    // Context: Searching in drive.
    // Displayed as: [Search for] 'user input' in To recipient.
    // The 'user input' part is always prepended, please heed this in translations.
    public static final String SEARCH_IN_TO = "in To recipient";

    // Context: Searching in drive.
    // Displayed as: [Search for] 'user input' in subject.
    // The 'user input' part is always prepended, please heed this in translations.
    public static final String SEARCH_IN_SUBJECT = "in subject";

}
