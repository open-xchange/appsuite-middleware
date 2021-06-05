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

package com.openexchange.unifiedinbox;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link NameStrings}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class NameStrings implements LocalizableStrings {

    // The name for the INBOX folder
    public static final String NAME_INBOX = "Inbox";

    // The name for the drafts folder
    public static final String NAME_DRAFTS = "Drafts";

    // The name for the sent folder
    public static final String NAME_SENT = "Sent items";

    // The name for the spam folder
    public static final String NAME_SPAM = "Spam";

    // The name for the trash folder
    public static final String NAME_TRASH = "Trash";

    public NameStrings() {
        super();
    }
}
