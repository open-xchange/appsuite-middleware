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

package com.openexchange.snippet;

import com.openexchange.i18n.LocalizableStrings;


/**
 * {@link SnippetStrings} - Provides localizable strings for snippet module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
public class SnippetStrings implements LocalizableStrings {

    /**
     * Initializes a new {@link SnippetStrings}.
     */
    private SnippetStrings() {
        super();
    }

    // Thrown if a user tries to create a snippet/signature referencing to invalid or even harmful image data
    public static final String INVALID_IMAGE_DATA_MSG = "Invalid or harmful image data detected";

    // Thrown if a user tries to create a snippet/signature referencing to an image which is too big
    public static final String MAXIMUM_IMAGE_SIZE_MSG = "The signature image exceeds the maximum allowed size of '%1$s'.";

    // The signature size exceeds the maximum allowed size of '%1$s'.
    public static final String MAXIMUM_SNIPPET_SIZE_MSG = "The signature size exceeds the maximum allowed size of '%1$s'.";

    // Thrown if a user tries to create a snippet/signature containing more than max. number of allowed images
    public static final String MAXIMUM_IMAGES_COUNT_MSG = "The maximum allowed number of '%1$s' images in the signature is reached.";

    // Thrown if a user must not modify a snippet/signature. Neither shared nor owned by that user.
    public static final String UPDATE_DENIED_MSG = "You are not allowed to modify the signature";

    // The user entered a very long display name, which cannot be stored due to data truncation
    public static final String DISPLAY_NAME_TOO_LONG_MSG = "The entered display name is too long. Please use a shorter one.";

    // The user entered a very long snippet identifier, which cannot be stored due to data truncation
    public static final String ID_TOO_LONG_MSG = "The entered identifier is too long. Please use a shorter one.";

    // The user entered a very long module identifier, which cannot be stored due to data truncation
    public static final String MODULE_TOO_LONG_MSG = "The entered module identifier is too long. Please use a shorter one.";

    // The user entered a very long type identifier, which cannot be stored due to data truncation
    public static final String TYPE_TOO_LONG_MSG = "The entered type identifier is too long. Please use a shorter one.";

}
