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
