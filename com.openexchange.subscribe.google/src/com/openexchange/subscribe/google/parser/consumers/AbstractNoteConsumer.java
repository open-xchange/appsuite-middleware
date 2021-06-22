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

package com.openexchange.subscribe.google.parser.consumers;

import com.openexchange.groupware.container.Contact;
import com.openexchange.i18n.I18nService;
import com.openexchange.java.Strings;

/**
 * {@link AbstractNoteConsumer}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.6
 */
abstract class AbstractNoteConsumer {

    private final I18nService i18nService;

    /**
     * Initialises a new {@link AbstractNoteConsumer}.
     */
    public AbstractNoteConsumer(I18nService i18nService) {
        super();
        this.i18nService = i18nService;
    }

    /**
     * Adds the specified value to as a note to the specified contact. It localises the specified header
     * and adds the note either as first or appends it while taking into consideration the firstValueInNotes flag
     *
     * @param contact The contact
     * @param header The header
     * @param value The value
     * @param firstValueInNotes whether the value is the first one, or shall appended
     * @return the <code>firstValueInNotesState</code> unchanged if the value is <code>null</code>; <code>false</code> otherwise
     */
    boolean addValueToNote(Contact contact, String header, String value, boolean firstValueInNotes) {
        if (Strings.isEmpty(value)) {
            return firstValueInNotes;
        }
        String localisedHeader = getI18nService().getLocalized(header);
        if (contact.containsNote()) {
            if (firstValueInNotes) {
                contact.setNote(contact.getNote() + "\n\n" + localisedHeader + "\n" + value);
            } else {
                contact.setNote(contact.getNote() + "\n" + value);
            }
        } else {
            if (firstValueInNotes) {
                contact.setNote(localisedHeader + "\n" + value);
            } else {
                contact.setNote("\n" + value);
            }
        }
        return false;
    }

    /**
     * Gets the i18nService
     *
     * @return The i18nService
     */
    public I18nService getI18nService() {
        return i18nService;
    }
}
