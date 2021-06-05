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

package com.openexchange.mail.compose.mailstorage;

import com.openexchange.i18n.LocalizableStrings;


/**
 * {@link MailStorageCompositionSpaceStrings} - Localizable strings for the mail-storage backed composition spaces.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.5
 */
public class MailStorageCompositionSpaceStrings implements LocalizableStrings {

    /**
     * Initializes a new {@link MailStorageCompositionSpaceStrings}.
     */
    private MailStorageCompositionSpaceStrings() {
        super();
    }

    // The prefix used when creating the folder, which carries the file attachments that are published via share link to recipients
    // E.g. "[Draft] My mail for Bob"
    public static final String DRAFT_PREFIX = "Draft";

    // The fall-back folder name that is used in case user has not yet specified a subject for E-Mail in composition, but decided to create
    // a Drive Mail.
    //
    // Supports the following placeholders:
    // #DATE# is replaced with the date when user started mail composition
    // #TIME# is replaced with time of the date when user started mail composition
    //
    // E.g. "E-Mail from 2020-09-08 at 8:35am"
    public static final String FALLBACK_FOLDER_NAME = "E-Mail from #DATE# at #TIME#";

}
