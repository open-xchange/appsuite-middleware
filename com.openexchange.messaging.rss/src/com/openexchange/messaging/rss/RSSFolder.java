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

package com.openexchange.messaging.rss;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import com.openexchange.messaging.DefaultMessagingPermission;
import com.openexchange.messaging.MessagingFolder;
import com.openexchange.messaging.MessagingPermission;
import com.openexchange.messaging.MessagingPermissions;


/**
 * {@link RSSFolder}
 * Copy of the TwitterMessagingFolder
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class RSSFolder implements MessagingFolder {

    private static final long serialVersionUID = -4345785692481994664L;

    private final MessagingPermission ownPermission;

    private final List<MessagingPermission> permissions;

    public RSSFolder(final int user) {
        super();
        final MessagingPermission mp = DefaultMessagingPermission.newInstance();
        final int[] arr = RSSMessagingService.getStaticRootPerms();
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
        return -1;
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
