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

package com.openexchange.quota;

import com.openexchange.i18n.LocalizableStrings;


/**
 * {@link QuotaExceptionMessages} - Exception messages for quota module that needs to be translated.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class QuotaExceptionMessages implements LocalizableStrings {

    public static final String QUOTA_EXCEEDED_MSG = "Quota exceeded. Please delete some items in order to create new ones. Please note: The quota refers to all items of all users in this context.";

    public static final String QUOTA_EXCEEDED_CALENDAR_MSG = "Quota exceeded for calendar. Quota limit: %2$s. Quota used: %1$s. Please delete some appointments in order to create new ones. Please note: The quota refers to all appointments of all users in this context.";

    public static final String QUOTA_EXCEEDED_CONTACTS_MSG = "Quota exceeded for contacts. Quota limit: %2$s. Quota used: %1$s. Please delete some contacts in order to create new ones. Please note: The quota refers to all contacts of all users in this context.";

    public static final String QUOTA_EXCEEDED_TASKS_MSG = "Quota exceeded for tasks. Quota limit: %2$s. Quota used: %1$s. Please delete some tasks in order to create new ones. Please note: The quota refers to all tasks of all users in this context.";

    public static final String QUOTA_EXCEEDED_FILES_MSG = "Quota exceeded for files. Quota limit: %2$s. Quota used: %1$s. Please delete some files in order to create new ones. Please note: The quota refers to all files of all users in this context.";

    public static final String QUOTA_EXCEEDED_ATTACHMENTS_MSG = "Quota exceeded for attachments. Quota limit: %2$s. Quota used: %1$s. Please delete some attachments in order to create new ones. Please note: The quota refers to all attachments of all users in this context.";

    public static final String QUOTA_EXCEEDED_SHARES_MSG = "Quota exceeded for shares. Quota limit: %2$s. Quota used: %1$s. Please delete some shares in order to create new ones. Please note: The quota refers to all shares of one user in this context.";

    public static final String QUOTA_EXCEEDED_SHARE_LINKS_MSG = "Quota exceeded for share links. Quota limit: %2$s. Quota used: %1$s. Please delete some shares in order to create new ones. Please note: The quota refers to all shares of one user in this context.";

    public static final String QUOTA_EXCEEDED_INVITE_GUESTS_MSG = "Quota exceeded for guest users. Quota limit: %2$s. Quota used: %1$s. Please delete some shares in order to create new ones. Please note: The quota refers to all shares of one user in this context.";

    public static final String QUOTA_EXCEEDED_SNIPPETS_MSG = "Quota exceeded for signatures. Quota limit: %2$s. Quota used: %1$s. Please delete some signatures in order to create new ones.";

    public static final String QUOTA_EXCEEDED_SIGNATURES_MSG = "The storage limit for signatures is %1$s. Delete old signatures to be able to store a new one.";

    /**
     * Initializes a new {@link QuotaExceptionMessages}.
     */
    private QuotaExceptionMessages() {
        super();
    }

}
