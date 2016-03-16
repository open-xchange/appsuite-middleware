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

package com.openexchange.share.groupware;

import java.util.Date;
import java.util.List;
import com.openexchange.share.ShareTarget;
import com.openexchange.share.ShareTargetPath;


/**
 * A {@link TargetProxy} delegates module-specific calls to an underlying groupware object
 * that is identified by a certain {@link ShareTarget}.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.8.0
 */
public interface TargetProxy {

    /**
     * Gets the ID of the underlying groupware object.
     *
     * @return The objects ID
     */
    String getID();

    /**
     * Gets the folder ID of the underlying groupware object. If the object
     * is a folder, the ID of its parent folder is returned.
     *
     * @return The objects parent folder ID
     */
    String getFolderID();

    /**
     * Gets the title of this folder or item.
     *
     * @return The title (e.g. a folder or file name)
     */
    String getTitle();

    /**
     * Gets the existing permissions of the target.
     *
     * @return The permissions
     */
    List<TargetPermission> getPermissions();

    /**
     * Applies a list of permissions, i.e. the permissions are merged. New ones
     * are added and existing ones are updated. No permissions are removed.
     * Furthermore only the in-memory representation of this object is
     * modified. To save changes you need to use {@link TargetUpdate}.
     *
     * @param permissions The permissions to apply
     */
    void applyPermissions(List<TargetPermission> permissions);

    /**
     * Removes a list of permissions from the underlying object. Only
     * the entity is taken into account, permission bits are not compared.
     * Furthermore only the in-memory representation of this object is
     * modified. To save changes you need to use {@link TargetUpdate}.
     *
     * @param permissions The permissions to remove
     */
    void removePermissions(List<TargetPermission> permissions);

    /**
     * Marks this target proxy as "dirty", so that the underlying item is going to be updated independently of changed permission, i.e.
     * after certain changes to a guest account have been performed like changing the password or expiry time.
     */
    void touch();

    /**
     * Returns whether the underlying object has been modified.
     *
     * @return <code>true</code> if any destructive methods have been called on this instance
     *         (e.g. {@link #applyPermissions(List)}), otherwise <code>false</code>.
     */
    boolean wasModified();

    /**
     * Gets a value indicating whether the item has been marked for being "touched" or not.
     *
     * @return <code>true</code> if the underlying object was touched, <code>false</code>, otherwise
     */
    boolean wasTouched();

    /**
     * Returns the type of the {@link TargetProxy}
     *
     * @return The type of the {@link TargetProxy}
     */
    TargetProxyType getProxyType();

    /**
     * Gets a value indicating whether the underlying target permissions may be adjusted through this proxy or not.
     *
     * @return <code>true</code> if it may be adjusted, <code>false</code>, otherwise
     */
    boolean mayAdjust();

    /**
     * Gets the last modification timestamp of the target.
     *
     * @return The timestamp
     */
    Date getTimestamp();

    /**
     * Gets the according share target from the view of the user via which it has been loaded.
     *
     * @return The target
     */
    ShareTarget getTarget();

    /**
     * Gets the path for generating links to the according share target.
     *
     * @return The path
     */
    ShareTargetPath getTargetPath();

}
