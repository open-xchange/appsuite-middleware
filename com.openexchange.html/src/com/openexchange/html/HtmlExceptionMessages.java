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

package com.openexchange.html;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link HtmlExceptionMessages} - Translatable messages for {@link HtmlExceptionCodes}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public final class HtmlExceptionMessages implements LocalizableStrings {

    // The HTML content is too big for being parsed and sanitized for a safe display
    public static final String TOO_BIG_MSG = "The HTML content is too big and cannot be displayed";

    // The HTML content seems to be corrupted/invalid and therefore fails being parsed and sanitized for a safe display
    public static final String CORRUPT_MSG = "The HTML content is invalid and cannot be displayed";

    // Thrown when parser failed or timed-out while processing HTML, in turn it cannot be safely displayed
    public static final String PARSING_FAILED_MSG = "The HTML content cannot be safely displayed";

    // Thrown when parser failed or timed-out while processing HTML, and plain-text is returned instead
    public static final String PARSING_FAILED_WITH_FAILOVERMSG = "This message can only be displayed as plain text";

    /**
     * Prevent instantiation.
     */
    private HtmlExceptionMessages() {
        super();
    }

}
