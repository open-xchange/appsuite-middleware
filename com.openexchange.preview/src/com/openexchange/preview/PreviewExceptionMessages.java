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

package com.openexchange.preview;

import com.openexchange.exception.OXException;
import com.openexchange.i18n.LocalizableStrings;

/**
 * Exception messages for {@link OXException} that must be translated.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class PreviewExceptionMessages implements LocalizableStrings {

    public static final String TRUNCATED_MSG = "The following field(s) are too long: %1$s. Please shorten the values and try again";

    public static final String UNABLE_TO_CHANGE_DATA_MSG = "The data you provided (%1$s) cannot be changed.";

    // Thumbnail image not available.
    public static final String THUMBNAIL_NOT_AVAILABLE = "Thumbnail image not available.";

    // No preview service for MIME type %1$s
    public static final String NO_PREVIEW_SERVICE = "The preview service for MIME type %1$s is currently not available. Please try again later";

    /**
     * Prevent instantiation.
     */
    private PreviewExceptionMessages() {
        super();
    }
}
