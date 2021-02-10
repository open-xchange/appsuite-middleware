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
