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

package com.openexchange.mail.permission;

import java.lang.reflect.InvocationTargetException;
import com.openexchange.exception.OXException;
import com.openexchange.mail.MailExceptionCode;
import com.openexchange.server.impl.OCLPermission;

/**
 * {@link MailPermission} - The mail permission defining a set of access rights on a mail folder for a certain entity.
 * <p>
 * This depends on if mailing system supports any kind of access control for entities; e.g. for IMAP it is the ACL capability. If no access
 * control is defined by mailing system, {@link DefaultMailPermission} is used which grants full access and therefore bypasses access
 * control.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class MailPermission extends OCLPermission {

    /**
     * serialVersionUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * Initializes a new {@link MailPermission}
     */
    protected MailPermission() {
        super();
    }

    /**
     * Checks if permission allows to rename the folder.
     * <p>
     * Returns <code>-1</code> if there's no special rename permission. Then rename is granted if {@link #isFolderAdmin()} returns
     * <code>true</code>. Otherwise <code>1</code> is returned if rename is granted; <code>0</code> means no rename permission.
     *
     * @return
     */
    public int canRename() {
        return -1;
    }

    /**
     * Checks if permission allows to store the <code>"seen"</code> flag permanently across sessions.
     *
     * @return <code>-1</code> if there's no special store <code>"seen"</code> flag permission, <code>1</code> if store <code>"seen"</code> flag permission is granted; otherwise <code>0</code> if not
     */
    public int canStoreSeenFlag() {
        return -1;
    }

    private static final Class<?>[] CONSTRUCTOR_ARGS = new Class[0];

    /**
     * Gets a new mail permission instance
     *
     * @param <P> The permission sub-type
     * @param clazz The permission class
     * @return A new mail permission instance
     * @throws OXException If instantiation fails
     */
    public static <P extends MailPermission> P newInstance(Class<? extends P> clazz) throws OXException {
        /*
         * Create a new mail permission
         */
        try {
            return clazz.getConstructor(CONSTRUCTOR_ARGS).newInstance();
        } catch (SecurityException e) {
            throw MailExceptionCode.INSTANTIATION_PROBLEM.create(e, clazz.getName());
        } catch (NoSuchMethodException e) {
            throw MailExceptionCode.INSTANTIATION_PROBLEM.create(e, clazz.getName());
        } catch (IllegalArgumentException e) {
            throw MailExceptionCode.INSTANTIATION_PROBLEM.create(e, clazz.getName());
        } catch (InstantiationException e) {
            throw MailExceptionCode.INSTANTIATION_PROBLEM.create(e, clazz.getName());
        } catch (IllegalAccessException e) {
            throw MailExceptionCode.INSTANTIATION_PROBLEM.create(e, clazz.getName());
        } catch (InvocationTargetException e) {
            throw MailExceptionCode.INSTANTIATION_PROBLEM.create(e, clazz.getName());
        }
    }
}
