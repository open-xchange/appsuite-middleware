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

package com.openexchange.folderstorage.database.contentType;

import com.openexchange.folderstorage.ContentType;

/**
 * {@link UnboundContentType} - The folder storage content type for unbound.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UnboundContentType implements ContentType {

    private static final UnboundContentType instance = new UnboundContentType();

    /**
     * Gets the {@link UnboundContentType} instance.
     *
     * @return The {@link UnboundContentType} instance
     */
    public static UnboundContentType getInstance() {
        return instance;
    }

    /**
     * Initializes a new {@link UnboundContentType}.
     */
    private UnboundContentType() {
        super();
    }

    @Override
    public String toString() {
        return "unbound";
    }

    @Override
    public int getModule() {
        // From FolderObject.UNBOUND
        return 4;
    }

    @Override
    public int getPriority() {
        return 0;
    }

}
