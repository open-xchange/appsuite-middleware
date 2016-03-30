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

package com.openexchange.messaging.twitter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import com.openexchange.messaging.DefaultMessagingPermission;
import com.openexchange.messaging.MessagingFolder;
import com.openexchange.messaging.MessagingPermission;
import com.openexchange.messaging.MessagingPermissions;

/**
 * {@link TwitterMessagingFolder}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class TwitterMessagingFolder implements MessagingFolder {

    private static final long serialVersionUID = -7211881893894289678L;

    /**
     * Gets the instance.
     *
     * @param user The user ID
     * @return The instance
     */
    public static TwitterMessagingFolder getInstance(final int user) {
        return new TwitterMessagingFolder(user);
    }

    private final MessagingPermission ownPermission;

    private final List<MessagingPermission> permissions;

    /**
     * Initializes a new {@link TwitterMessagingFolder}.
     */
    private TwitterMessagingFolder(final int user) {
        super();
        final MessagingPermission mp = DefaultMessagingPermission.newInstance();
        final int[] arr = TwitterMessagingService.getStaticRootPerms();
        mp.setAllPermissions(arr[0], arr[1], arr[2], arr[3]);
        mp.setAdmin(false);
        mp.setEntity(user);
        mp.setGroup(false);
        ownPermission = MessagingPermissions.unmodifiablePermission(mp);
        permissions = Arrays.asList(ownPermission);
    }

    @Override
    public Set<String> getCapabilities() {
        return Collections.emptySet();
    }

    @Override
    public String getId() {
        return MessagingFolder.ROOT_FULLNAME;
    }

    @Override
    public String getName() {
        return MessagingFolder.ROOT_FULLNAME;
    }

    @Override
    public MessagingPermission getOwnPermission() {
        return ownPermission;
    }

    @Override
    public String getParentId() {
        return null;
    }

    @Override
    public List<MessagingPermission> getPermissions() {
        return permissions;
    }

    @Override
    public boolean hasSubfolders() {
        return false;
    }

    @Override
    public boolean hasSubscribedSubfolders() {
        return false;
    }

    @Override
    public boolean isSubscribed() {
        return true;
    }

    @Override
    public int getDeletedMessageCount() {
        return 0;
    }

    @Override
    public int getMessageCount() {
        return TwitterConstants.TIMELINE_LENGTH;
    }

    @Override
    public int getNewMessageCount() {
        return 0;
    }

    @Override
    public int getUnreadMessageCount() {
        return 0;
    }

    @Override
    public boolean isDefaultFolder() {
        return false;
    }

    @Override
    public boolean isHoldsFolders() {
        return false;
    }

    @Override
    public boolean isHoldsMessages() {
        return true;
    }

    @Override
    public boolean isRootFolder() {
        return true;
    }

    @Override
    public boolean containsDefaultFolderType() {
        return true;
    }

    @Override
    public DefaultFolderType getDefaultFolderType() {
        return DefaultFolderType.MESSAGING;
    }

    @Override
    public char getSeparator() {
        return '.';
    }

}
