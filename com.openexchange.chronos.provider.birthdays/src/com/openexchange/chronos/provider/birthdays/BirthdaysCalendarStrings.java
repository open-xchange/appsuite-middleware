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

package com.openexchange.chronos.provider.birthdays;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link BirthdaysCalendarStrings}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class BirthdaysCalendarStrings implements LocalizableStrings {

    /**
     * The name of the contact birthdays calendar provider.
     */
    public static final String PROVIDER_NAME = "Birthday Calendar";

    /**
     * The display name for the actual birthdays calendar folder.
     */
    public static final String CALENDAR_NAME = "Birthdays";

    /**
     * The description for the actual birthdays calendar folder.
     */
    public static final String CALENDAR_DESCRIPTION = "A calendar that automatically populates with the known birthdays of everyone in your address books.";

    /**
     * The event summary displayed for the birthday of a contact, with the first parameter being replaced by the contact's display name.
     * <code>\ud83c\udf82</code> is the <i>birthday cake</i> character (<code>U+1F382</code>).
     */
    public static final String EVENT_SUMMARY = "\ud83c\udf82 %1$s";

    /**
     * The event description displayed for the birthday of a contact if the full date of birth is known, with the first parameter being
     * replaced by the contact's birthday in its localized medium date format.
     */
    public static final String EVENT_DESCRIPTION = "* %1$s";

    /**
     * The event summary displayed for the n-th birthday of a contact, with the first parameter being replaced by the contact's display
     * name, and the second one with the contact's age. <code>\ud83c\udf82</code> is the <i>birthday cake</i> character
     * (<code>U+1F382</code>).
     */
    public static final String EVENT_SUMMARY_WITH_AGE = "\ud83c\udf82 %1$s (%2$d. birthday)";

    /**
     * Prevent instantiation.
     */
    private BirthdaysCalendarStrings() {
        super();
    }

}
