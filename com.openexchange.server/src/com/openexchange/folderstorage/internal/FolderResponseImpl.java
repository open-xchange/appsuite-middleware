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

package com.openexchange.folderstorage.internal;

import java.util.Collection;
import java.util.Collections;
import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.FolderResponse;

/**
 * {@link FolderResponseImpl}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since Open-Xchange v6.16
 */
public final class FolderResponseImpl<R> implements FolderResponse<R> {

    /**
     * Generates a new {@link FolderResponse}.
     *
     * @param response The response object
     * @param warnings The warnings
     * @return A new {@link FolderResponse}
     */
    public static <R> FolderResponse<R> newFolderResponse(final R response, final Collection<OXException> warnings) {
        return new FolderResponseImpl<R>(response, warnings);
    }

    private final R response;

    private final Collection<OXException> warnings;

    /**
     * Initializes a new {@link FolderResponseImpl}.
     *
     * @param response The response object
     * @param warnings The warnings
     */
    private FolderResponseImpl(final R response, final Collection<OXException> warnings) {
        super();
        this.response = response;
        this.warnings = null == warnings ? Collections.<OXException> emptySet() : warnings;
    }

    @Override
    public R getResponse() {
        return response;
    }

    @Override
    public Collection<OXException> getWarnings() {
        return warnings;
    }

}
