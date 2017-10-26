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
