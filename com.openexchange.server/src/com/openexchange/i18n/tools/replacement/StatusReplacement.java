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

package com.openexchange.i18n.tools.replacement;

import java.util.Locale;
import com.openexchange.groupware.container.CalendarObject;
import com.openexchange.groupware.i18n.Notifications;
import com.openexchange.i18n.tools.TemplateToken;

/**
 * {@link StatusReplacement} - Replacement for a confirmation status.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class StatusReplacement extends LocalizedStringReplacement {

    public static final int STATUS_ACCEPTED = CalendarObject.ACCEPT;

    public static final int STATUS_DECLINED = CalendarObject.DECLINE;

    public static final int STATUS_TENTATIVE = CalendarObject.TENTATIVE;

    public static final int STATUS_WAITING = CalendarObject.NONE;

    private static String[] STATUSES = new String[4];

    static {
        STATUSES[STATUS_WAITING] = Notifications.STATUS_WAITING;
        STATUSES[STATUS_ACCEPTED] = Notifications.STATUS_ACCEPTED;
        STATUSES[STATUS_DECLINED] = Notifications.STATUS_DECLINED;
        STATUSES[STATUS_TENTATIVE] = Notifications.STATUS_TENTATIVE;
    }

    /**
     * Initializes a new {@link StatusReplacement}
     *
     * @param status The status; supposed to be either {@link #STATUS_ACCEPTED},
     *            {@link #STATUS_DECLINED}, {@link #STATUS_TENTATIVE}, or
     *            {@link #STATUS_WAITING}
     */
    public StatusReplacement(final int status) {
        this(status, null);
    }

    /**
     * Initializes a new {@link StatusReplacement}
     *
     * @param status The status; supposed to be either {@link #STATUS_ACCEPTED},
     *            {@link #STATUS_DECLINED}, {@link #STATUS_TENTATIVE}, or
     *            {@link #STATUS_WAITING}
     * @param locale The locale
     */
    public StatusReplacement(final int status, final Locale locale) {
        super(TemplateToken.STATUS, STATUSES[status]);
        setLocale(locale);
    }

}
