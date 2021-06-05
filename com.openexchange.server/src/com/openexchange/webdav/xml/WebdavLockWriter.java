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

package com.openexchange.webdav.xml;

import com.openexchange.webdav.protocol.WebdavCollection;
import com.openexchange.webdav.protocol.WebdavLock;

public class WebdavLockWriter {

    public String lock2xml(final WebdavLock lock) {
        final StringBuffer lockXML = new StringBuffer();
        activeLock(lockXML);
        lockType(lock, lockXML);
        lockScope(lock, lockXML);
        depth(lock, lockXML);
        owner(lock, lockXML);
        timeout(lock, lockXML);
        lockToken(lock, lockXML);
        endActiveLock(lockXML);
        return lockXML.toString();
    }

    private final void endActiveLock(final StringBuffer lockXML) {
        lockXML.append("</D:activelock>");
    }

    private final void lockToken(final WebdavLock lock, final StringBuffer lockXML) {
        lockXML.append("<D:locktoken><D:href>");
        lockXML.append(lock.getToken());
        lockXML.append("</D:href></D:locktoken>");

    }

    private final void timeout(final WebdavLock lock, final StringBuffer lockXML) {
        lockXML.append("<D:timeout>");
        if (WebdavLock.NEVER == lock.getTimeout()) {
            lockXML.append("Infinite");
        } else {
            lockXML.append("Second-" + lock.getTimeout() / 1000);
        }
        lockXML.append("</D:timeout>");
    }

    private final void owner(final WebdavLock lock, final StringBuffer lockXML) {
        lockXML.append("<D:owner>");
        lockXML.append(lock.getOwner()); //TODO: OWNER NS
        lockXML.append("</D:owner>");
    }

    private final void depth(final WebdavLock lock, final StringBuffer lockXML) {
        lockXML.append("<D:depth>");
        if (lock.getDepth() == WebdavCollection.INFINITY) {
            lockXML.append("infinity");
        } else {
            lockXML.append(lock.getDepth());
        }
        lockXML.append("</D:depth>");
    }

    private final void lockScope(final WebdavLock lock, final StringBuffer lockXML) {
        lockXML.append("<D:lockscope>");
        if (lock.getScope().equals(WebdavLock.Scope.EXCLUSIVE_LITERAL)) {
            lockXML.append("<D:exclusive/>");
        } else if (lock.getScope().equals(WebdavLock.Scope.SHARED_LITERAL)) {
            lockXML.append("<D:shared/>");
        }
        lockXML.append("</D:lockscope>");
    }

    private final void lockType(final WebdavLock lock, final StringBuffer lockXML) {
        lockXML.append("<D:locktype>");
        if (lock.getType().equals(WebdavLock.Type.WRITE_LITERAL)) {
            lockXML.append("<D:write />");
        }
        lockXML.append("</D:locktype>");
    }

    private final void activeLock(final StringBuffer lockXML) {
        lockXML.append("<D:activelock>");
    }

}
