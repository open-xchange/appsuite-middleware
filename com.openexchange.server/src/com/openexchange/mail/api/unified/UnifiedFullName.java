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

package com.openexchange.mail.api.unified;

import com.openexchange.mailaccount.UnifiedInboxManagement;

/**
 * {@link UnifiedFullName} - The enumeration for available folders for a unified view.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.6.1
 */
public enum UnifiedFullName {

    /**
     * Full name of unified INBOX.
     */
    INBOX(UnifiedInboxManagement.INBOX),
    /**
     * Full name of unified Trash.
     */
    TRASH(UnifiedInboxManagement.TRASH),
    /**
     * Full name of unified Sent.
     */
    SENT(UnifiedInboxManagement.SENT),
    /**
     * Full name of unified Spam.
     */
    SPAM(UnifiedInboxManagement.SPAM),
    /**
     * Full name of unified Drafts.
     */
    DRAFTS(UnifiedInboxManagement.DRAFTS), ;

    private final String fullName;

    private UnifiedFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Gets the full name
     *
     * @return The full name
     */
    public String getFullName() {
        return fullName;
    }

}
