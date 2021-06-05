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

package com.openexchange.folderstorage;

import com.openexchange.exception.OXException;


/**
 * {@link AfterReadAwareFolderStorage} - Enhances {@link FolderStorage} interface.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface AfterReadAwareFolderStorage extends FolderStorage {

    public static enum Mode {
        READ,
        WRITE,
        WRITE_AFTER_READ,
        ;
    }

    /**
     * Starts a transaction on folder storage.
     *
     * @param parameters The parameters
     * @param modify <code>true</code> if started transaction is supposed to modify storage's content; otherwise <code>false</code>
     * @return <code>true</code> if call started the transaction; otherwise <code>false</code> if transaction has already been started
     *         before
     * @throws OXException If storage parameters cannot be returned
     */
    boolean startTransaction(StorageParameters parameters, Mode mode) throws OXException;

}
