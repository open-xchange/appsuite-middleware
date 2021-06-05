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

package com.openexchange.share.core;

import java.sql.Connection;
import com.openexchange.folderstorage.FolderServiceDecorator;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.session.Session;
import com.openexchange.user.User;


/**
 * {@link HandlerParameters}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public class HandlerParameters {

    private Session session;

    private Connection writeCon;

    private FolderServiceDecorator folderServiceDecorator;

    private Context context;

    private User user;


    public Session getSession() {
        return session;
    }


    public void setSession(Session session) {
        this.session = session;
    }


    public Connection getWriteCon() {
        return writeCon;
    }


    public void setWriteCon(Connection writeCon) {
        this.writeCon = writeCon;
    }


    public FolderServiceDecorator getFolderServiceDecorator() {
        return folderServiceDecorator;
    }


    public void setFolderServiceDecorator(FolderServiceDecorator folderServiceDecorator) {
        this.folderServiceDecorator = folderServiceDecorator;
    }


    public Context getContext() {
        return context;
    }


    public void setContext(Context context) {
        this.context = context;
    }


    public User getUser() {
        return user;
    }


    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Returns whether we are in an administrative context, i.e. no session is available and according
     * calls to the concrete module have to be used.
     *
     * @return <code>true</code> if administrative methods must be used.
     *         {@link HandlerParameters#getSession()} will return <code>null</code> in this case.
     */
    public boolean isAdministrative() {
        return session == null;
    }

}
