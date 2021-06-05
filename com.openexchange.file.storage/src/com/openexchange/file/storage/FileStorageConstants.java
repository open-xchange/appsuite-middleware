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

package com.openexchange.file.storage;

import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;

/**
 * {@link FileStorageConstants} - Provides constants for file storage.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface FileStorageConstants {

    /**
     * The name of login property.
     */
    static final String CONF_PROPERTY_LOGIN = "login";

    /**
     * The name of password property.
     */
    static final String CONF_PROPERTY_PASSWORD = "password";

    /**
     * An empty file iterator.
     */
    static final SearchIterator<File> EMPTY_ITER = SearchIteratorAdapter.emptyIterator();

    /** The meta-data key that holds encrypted information; type if java.lang.Boolean */
    public static final String METADATA_KEY_ENCRYPTED = "Encrypted";

}
