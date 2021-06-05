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

package com.openexchange.mailaccount.internal;

import com.openexchange.mail.dataobjects.MailFolder;
import com.openexchange.mail.utils.MailFolderUtility;

/**
 * {@link CustomMailAccount} - Represents a custom mail account.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class CustomMailAccount extends AbstractMailAccount {

    private static final long serialVersionUID = -4029984573026712984L;

    /**
     * Initializes a new {@link CustomMailAccount}.
     *
     * @param id The account identifier
     */
    public CustomMailAccount(int id) {
        super();
        this.id = id;
        rootFolder = MailFolderUtility.prepareFullname(id, MailFolder.ROOT_FOLDER_ID);
    }

    @Override
    public boolean isDefaultAccount() {
        return false;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(128);
        sb.append(CustomMailAccount.class.getSimpleName()).append(super.toString());
        return sb.toString();
    }
}
