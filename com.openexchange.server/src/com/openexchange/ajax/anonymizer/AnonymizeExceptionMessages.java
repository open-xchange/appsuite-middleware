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

package com.openexchange.ajax.anonymizer;

import com.openexchange.i18n.LocalizableStrings;


/**
 * {@link AnonymizeExceptionMessages} - Exception messages for anonymize module that needs to be translated.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AnonymizeExceptionMessages implements LocalizableStrings {

    // No appropriate anonymizer available.
    public static final String NO_SUCH_ANONYMIZER_MSG = "No appropriate anonymizer available.";

    /**
     * Initializes a new {@link AnonymizeExceptionMessages}.
     */
    private AnonymizeExceptionMessages() {
        super();
    }

}
