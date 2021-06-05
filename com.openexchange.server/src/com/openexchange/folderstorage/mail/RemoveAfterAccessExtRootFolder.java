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

package com.openexchange.folderstorage.mail;

import com.openexchange.exception.OXException;
import com.openexchange.folderstorage.RemoveAfterAccessFolder;
import com.openexchange.mailaccount.MailAccount;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link RemoveAfterAccessExtRootFolder} - A mail folder especially for root folder of an external account implementing
 * {@link RemoveAfterAccessFolder}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class RemoveAfterAccessExtRootFolder extends ExternalMailAccountRootFolder implements RemoveAfterAccessFolder {

    private static final long serialVersionUID = -7259106085690350497L;

    /**
     * Initializes a new {@link RemoveAfterAccessExtRootFolder} from given mail account.
     * <p>
     * Subfolder identifiers and tree identifier are not set within this constructor.
     *
     * @param mailAccount The underlying mail account
     * @param mailConfig The mail configuration
     * @param session The session
     * @throws OXException If creation fails
     */
    public RemoveAfterAccessExtRootFolder(final MailAccount mailAccount, /*final MailConfig mailConfig,*/ final ServerSession session) throws OXException {
        super(mailAccount, /*mailConfig,*/ session);
    }

    @Override
    public boolean loadSubfolders() {
        // return true;
        return false;
    }

    @Override
    public RemoveAfterAccessExtRootFolder clone() {
        return (RemoveAfterAccessExtRootFolder) super.clone();
    }

    @Override
    public boolean isCacheable() {
        return true;
    }

    @Override
    public int getUserId() {
        return userId;
    }

    @Override
    public int getContextId() {
        return contexctId;
    }

}
