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
 *
 */

package com.openexchange.subscribe.google.parser;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link ContactNoteStrings}
 *
 * @author <a href="mailto:philipp.schumacher@open-xchange.com">Philipp Schumacher</a>
 * @since v7.10.6
 */
public class ContactNoteStrings implements LocalizableStrings {

    /**
     * Header for other e-mail addresses in the contact's note section
     */
    public static final String OTHER_EMAIL_ADDRESSES = "Other e-mail addresses:";

    /**
     * Header for other instant messengers in the contact's note section
     */
    public static final String OTHER_IM_ADDRESSES = "Other instant messengers:";

    /**
     * Header for other phone numbers in the contact's note section
     */
    public static final String OTHER_PHONE_NUMBERS = "Other phone numbers:";

    /**
     * Header for other addresses in the contact's note section
     */
    public static final String OTHER_ADDRESSES = "Other addresses:";

    /**
     * Initializes a new {@link ContactNoteStrings}.
     */
    public ContactNoteStrings() {
        super();
    }

}
