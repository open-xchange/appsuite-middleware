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

package com.openexchange.filestore.sproxyd;

import com.openexchange.i18n.LocalizableStrings;


/**
 * {@link SproxydExceptionMessages} - Exception messages for Sproxyd module that needs to be translated.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class SproxydExceptionMessages implements LocalizableStrings {

    // No such document: %1$s
    public static final String NO_SUCH_DOCUMENT_MSG = "No such document: %1$s";

    // No such chunk: %1$s
    public static final String NO_SUCH_CHUNK_MSG = "No such chunk: %1$s";

    // No next chunk for chunk: %1$s
    public static final String NO_NEXT_CHUNK_MSG = "No next chunk for chunk: %1$s";

    /**
     * Initializes a new {@link SproxydExceptionMessages}.
     */
    private SproxydExceptionMessages() {
        super();
    }

}
