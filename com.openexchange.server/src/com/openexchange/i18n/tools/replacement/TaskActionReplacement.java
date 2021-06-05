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
 * {@link TaskActionReplacement} - Replacement for an action.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class TaskActionReplacement extends LocalizedStringReplacement {

    private static String[] ACTIONS = { Notifications.TASK_CREATE_TITLE, Notifications.TASK_UPDATE_TITLE,
            Notifications.TASK_DELETE_TITLE, Notifications.TASK_ACCEPTED_TITLE, Notifications.TASK_DECLINED_TITLE,
            Notifications.TASK_TENTATIVE_TITLE };

    public static final int ACTION_NEW = 0;

    public static final int ACTION_CHANGED = 1;

    public static final int ACTION_DELETED = 2;

    public static final int ACTION_ACCEPTED = 3;

    public static final int ACTION_DECLINED = 4;

    public static final int ACTION_TENTATIVE = 5;

    /**
     * Initializes a new {@link TaskActionReplacement}
     *
     * @param taskAction The task action; supposed to be either
     *            {@link #ACTION_NEW}, {@link #ACTION_CHANGED},
     *            {@link #ACTION_DELETED}, {@link #ACTION_ACCEPTED},
     *            {@link #ACTION_DECLINED}, or {@link #ACTION_TENTATIVE}
     */
    public TaskActionReplacement(final int taskAction) {
        this(taskAction, null);
    }

    /**
     * Initializes a new {@link TaskActionReplacement}
     *
     * @param taskAction The task action; supposed to be either
     *            {@link #ACTION_NEW}, {@link #ACTION_CHANGED},
     *            {@link #ACTION_DELETED}, {@link #ACTION_ACCEPTED},
     *            {@link #ACTION_DECLINED}, or {@link #ACTION_TENTATIVE}
     * @param locale The locale
     */
    public TaskActionReplacement(final int taskAction, final Locale locale) {
        super(TemplateToken.ACTION, ACTIONS[taskAction]);
        setLocale(locale);
    }
}
