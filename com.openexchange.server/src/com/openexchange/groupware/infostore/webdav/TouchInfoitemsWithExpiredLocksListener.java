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

package com.openexchange.groupware.infostore.webdav;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.session.SessionHolder;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

/**
 * {@link TouchInfoitemsWithExpiredLocksListener}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class TouchInfoitemsWithExpiredLocksListener implements LockExpiryListener {

    private SessionHolder sessionHolder;

    private InfostoreFacade infostoreFacade;

    public TouchInfoitemsWithExpiredLocksListener(SessionHolder sessionHolder, InfostoreFacade infostoreFacade) {
        super();
        this.sessionHolder = sessionHolder;
        this.infostoreFacade = infostoreFacade;
    }

    public TouchInfoitemsWithExpiredLocksListener() {
        super();

    }

    @Override
    public void lockExpired(Lock lock) throws OXException {
        ServerSession serverSession;
        serverSession = ServerSessionAdapter.valueOf(sessionHolder.getSessionObject(), sessionHolder.getContext());
        infostoreFacade.touch(lock.getEntity(), serverSession);
    }


    public SessionHolder getSessionHolder() {
        return sessionHolder;
    }


    public void setSessionHolder(SessionHolder sessionHolder) {
        this.sessionHolder = sessionHolder;
    }


    public InfostoreFacade getInfostoreFacade() {
        return infostoreFacade;
    }


    public void setInfostoreFacade(InfostoreFacade infostoreFacade) {
        this.infostoreFacade = infostoreFacade;
    }
}
