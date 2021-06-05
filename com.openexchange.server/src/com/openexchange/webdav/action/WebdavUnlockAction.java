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

package com.openexchange.webdav.action;

import javax.servlet.http.HttpServletResponse;
import com.openexchange.groupware.infostore.webdav.InfostoreWebdavFactory;
import com.openexchange.java.Strings;
import com.openexchange.webdav.protocol.WebdavLock;
import com.openexchange.webdav.protocol.WebdavProtocolException;

public class WebdavUnlockAction extends AbstractAction {

    @Override
    public void perform(final WebdavRequest req, final WebdavResponse res) throws WebdavProtocolException {
        /*
         * check lock token
         */
        String token = Strings.unchar(req.getHeader("Lock-Token"), '<', '>');
        if (Strings.isEmpty(token)) {
            throw WebdavProtocolException.generalError(req.getUrl(), HttpServletResponse.SC_BAD_REQUEST);
        }
        /*
         * check lock exists
         */
        WebdavLock lock = req.getResource().getLock(token);
        if (null == lock) {
            throw WebdavProtocolException.generalError(req.getUrl(), HttpServletResponse.SC_CONFLICT);
        }
        /*
         * check if current user holds the lock
         */
        if (WebdavLock.Scope.EXCLUSIVE_LITERAL.equals(lock.getScope())) {
            WebdavLock ownLock = req.getResource().getOwnLock(token);
            if (null == ownLock) {
                throw WebdavProtocolException.generalError(req.getUrl(), HttpServletResponse.SC_FORBIDDEN);
            }
            if (0 < ownLock.getOwnerID()) {
                InfostoreWebdavFactory factory = (InfostoreWebdavFactory) req.getFactory();
                int currentUserID = factory.getSessionHolder().getSessionObject().getUserId();
                if (ownLock.getOwnerID() != currentUserID &&
                    factory.getSessionHolder().getContext().getMailadmin() != currentUserID) {
                    throw WebdavProtocolException.generalError(req.getUrl(), HttpServletResponse.SC_FORBIDDEN);
                }
            }
        }
        /*
         * perform unlock
         */
        req.getResource().unlock(token);
        res.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }

}
