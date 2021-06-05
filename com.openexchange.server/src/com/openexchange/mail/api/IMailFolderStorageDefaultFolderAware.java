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

package com.openexchange.mail.api;

import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.mail.utils.StorageUtility;

/**
 * {@link IMailFolderStorageDefaultFolderAware}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.0
 */
public interface IMailFolderStorageDefaultFolderAware {

    static final String TRASH = "trash";
    static final String SENT = "sent";
    static final String SPAM = "spam";
    static final String DRAFTS = "drafts";
    static final String ARCHIVE = "archive";

    /**
     * Retrieves the available special-use (aka default) folders from the mail server.
     *
     * @return A mapping with the available special use folder names
     * @throws OXException If user's default folder could not be checked
     * @see StorageUtility#INDEX_TRASH
     * @see StorageUtility#INDEX_DRAFTS
     * @see StorageUtility#INDEX_SENT
     * @see StorageUtility#INDEX_SPAM
     * @see StorageUtility#INDEX_ARCHIVE
     */
    Map<String, String> getSpecialUseFolder() throws OXException;

}
