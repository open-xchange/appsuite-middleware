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

package com.openexchange.file.storage.mail;

import com.openexchange.file.storage.FileStorageConstants;

/**
 * {@link MailDriveConstants} - Provides useful constants for Mail Drive file storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public final class MailDriveConstants implements FileStorageConstants {

    /**
     * Initializes a new {@link MailDriveConstants}.
     */
    private MailDriveConstants() {
        super();
    }

    /**
     * The identifier for Mail Drive file storage service.
     */
    public static final String ID = "maildrive";

    /**
     * The special account identifier <code>"0"</code>.
     */
    public static final String ACCOUNT_ID = "0";

}
