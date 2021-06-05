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

package com.openexchange.mail.json.compose.share;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link ShareComposeStrings} - The i18n string literals for share compose module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class ShareComposeStrings implements LocalizableStrings {

    /**
     * Initializes a new {@link ShareComposeStrings}.
     */
    private ShareComposeStrings() {
        super();
    }

    // The default name for a folder
    public static final String DEFAULT_NAME_FOLDER = "Folder";

    // The default name for a file
    public static final String DEFAULT_NAME_FILE = "File";

    // -----------------------------------------------------------------------------------------------------------------------------------

    // The name of the folder holding the attachments, which were shared to other recipients.
    public static final String FOLDER_NAME_SHARED_MAIL_ATTACHMENTS = "My shared mail attachments";

    public static final String SHARED_ATTACHMENTS_INTRO_SINGLE = "%1$s has shared the following file with you:";

    public static final String SHARED_ATTACHMENTS_INTRO_MULTI = "%1$s has shared the following files with you:";

    public static final String VIEW_FILE = "View file";

    public static final String VIEW_FILES = "View files";

    // The internationalized text put into text body of an email of which attachments exceed user's quota limitation
    // Indicates the elapsed date for affected message's attachments
    public static final String SHARED_ATTACHMENTS_EXPIRATION = "The link will expire on %1$s";

    // The internationalized text put into text body of an email of which attachments exceed user's quota limitation
    // Indicates the password for affected message's attachments
    public static final String SHARED_ATTACHMENTS_PASSWORD = "Please use the following password: %1$s";

    // Fall-back name if a file has no valid name
    public static final String DEFAULT_FILE_NAME = "Unnamed";

}
