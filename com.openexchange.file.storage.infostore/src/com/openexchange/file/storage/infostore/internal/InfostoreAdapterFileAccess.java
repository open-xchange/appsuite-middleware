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

package com.openexchange.file.storage.infostore.internal;

import com.openexchange.file.storage.FileStorageAccountAccess;
import com.openexchange.file.storage.infostore.AbstractInfostoreFileAccess;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.InfostoreSearchEngine;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.user.User;

/**
 * {@link InfostoreAdapterFileAccess}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class InfostoreAdapterFileAccess extends AbstractInfostoreFileAccess {

    private final Context ctx;
    private final User user;
    private final FileStorageAccountAccess accountAccess;
    private final int hash;

    /**
     * Initializes a new {@link InfostoreAdapterFileAccess}.
     *
     * @param session
     * @param infostore2
     */
    public InfostoreAdapterFileAccess(final ServerSession session, final InfostoreFacade infostore, final InfostoreSearchEngine search, final FileStorageAccountAccess accountAccess) {
        super(session, infostore, search);
        this.ctx = session.getContext();
        this.user = session.getUser();
        this.accountAccess = accountAccess;

        final int prime = 31;
        int result = 1;
        result = prime * result + ((accountAccess == null) ? 0 : accountAccess.getAccountId().hashCode());
        result = prime * result + ((ctx == null) ? 0 : ctx.getContextId());
        result = prime * result + ((user == null) ? 0 : user.getId());
        hash = result;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof InfostoreAdapterFileAccess)) {
            return false;
        }
        InfostoreAdapterFileAccess other = (InfostoreAdapterFileAccess) obj;
        if (accountAccess == null) {
            if (other.accountAccess != null) {
                return false;
            }
        } else if (!accountAccess.getAccountId().equals(other.accountAccess.getAccountId())) {
            return false;
        }
        if (ctx == null) {
            if (other.ctx != null) {
                return false;
            }
        } else if (ctx.getContextId() != other.ctx.getContextId()) {
            return false;
        }
        if (user == null) {
            if (other.user != null) {
                return false;
            }
        } else if (user.getId() != other.user.getId()) {
            return false;
        }
        return true;
    }

    @Override
    public FileStorageAccountAccess getAccountAccess() {
        return accountAccess;
    }

}
