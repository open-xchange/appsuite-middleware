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
     * Serial version UID
     */
    private static final long serialVersionUID = 3074890171981933102L;

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
    public static <P extends MailPermission> P newInstance(final Class<? extends P> clazz) throws OXException {
        /*
         * Create a new mail permission
         */
        try {
            return clazz.getConstructor(CONSTRUCTOR_ARGS).newInstance();
        } catch (final SecurityException e) {
            throw MailExceptionCode.INSTANTIATION_PROBLEM.create(e, clazz.getName());
        } catch (final NoSuchMethodException e) {
            throw MailExceptionCode.INSTANTIATION_PROBLEM.create(e, clazz.getName());
        } catch (final IllegalArgumentException e) {
            throw MailExceptionCode.INSTANTIATION_PROBLEM.create(e, clazz.getName());
        } catch (final InstantiationException e) {
            throw MailExceptionCode.INSTANTIATION_PROBLEM.create(e, clazz.getName());
        } catch (final IllegalAccessException e) {
            throw MailExceptionCode.INSTANTIATION_PROBLEM.create(e, clazz.getName());
        } catch (final InvocationTargetException e) {
            throw MailExceptionCode.INSTANTIATION_PROBLEM.create(e, clazz.getName());
        }
    }
}
