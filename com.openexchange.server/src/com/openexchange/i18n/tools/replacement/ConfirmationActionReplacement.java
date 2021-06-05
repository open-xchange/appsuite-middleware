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
import com.openexchange.groupware.i18n.Notifications;
import com.openexchange.i18n.tools.TemplateToken;

/**
 * {@link ConfirmationActionReplacement} - Replacement for a confirmation
 * status.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class ConfirmationActionReplacement extends LocalizedStringReplacement {

    private static String[] ACTIONS = { Notifications.CA_ACCEPTED, Notifications.CA_DECLINED,
            Notifications.CA_TENTATIVELY_ACCEPTED, Notifications.STATUS_WAITING };

    public static final int ACTION_ACCEPTED = 0;

    public static final int ACTION_DECLINED = 1;

    public static final int ACTION_TENTATIVELY_ACCEPTED = 2;

    public static final int ACTION_NONE_ACCEPTED = 3;

    /**
     * Initializes a new {@link ConfirmationActionReplacement}
     *
     * @param confirmationAction The confirmation action; supposed to be either
     *            {@link #ACTION_ACCEPTED}, {@link #ACTION_DECLINED}, or
     *            {@link #ACTION_TENTATIVELY_ACCEPTED}
     */
    public ConfirmationActionReplacement(final int confirmationAction) {
        this(confirmationAction, null);
    }

    /**
     * Initializes a new {@link ConfirmationActionReplacement}
     *
     * @param confirmationAction The confirmation action; supposed to be either
     *            {@link #ACTION_ACCEPTED}, {@link #ACTION_DECLINED}, or
     *            {@link #ACTION_TENTATIVELY_ACCEPTED}
     * @param locale The locale
     */
    public ConfirmationActionReplacement(final int confirmationAction, final Locale locale) {
        super(TemplateToken.CONFIRMATION_ACTIN, ACTIONS[confirmationAction]);
        setLocale(locale);
    }

}
