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

import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.session.Session;
import com.openexchange.user.User;

public interface EntityLockManager extends LockManager {

    void transferLocks(Context ctx, int from_user, int to_user) throws OXException;

    @Override
    void unlock(int id, Session session) throws OXException;

    int lock(int entity, long timeout, Scope exclusive, Type write, String ownerDesc, Context ctx, User user) throws OXException;

    @Override
    List<Lock> findLocks(int entity, Session session) throws OXException;

    Map<Integer, List<Lock>> findLocks(List<Integer> entities, Session session) throws OXException;

    Map<Integer, List<Lock>> findLocks(List<Integer> entities, Context context) throws OXException;

    boolean isLocked(int entity, Context context, User userObject) throws OXException;

    @Override
    void removeAll(int entity, Session session) throws OXException;

    void relock(int lockId, long timeout, Scope scope, Type write, String owner, Context context, User userObject) throws OXException;

    void addExpiryListener(LockExpiryListener listener);

}
