/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
