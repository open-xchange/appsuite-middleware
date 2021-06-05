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

package com.openexchange.mail.messaging;

import java.util.List;
import java.util.Set;
import com.openexchange.messaging.MessagingFolder;
import com.openexchange.messaging.MessagingPermission;


/**
 * {@link MailMessagingFolder}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailMessagingFolder implements MessagingFolder {

    private static final long serialVersionUID = 7625901999652670705L;

    /**
     * Initializes a new {@link MailMessagingFolder}.
     */
    public MailMessagingFolder() {
        super();
    }

    @Override
    public boolean containsDefaultFolderType() {
        return false;
    }

    @Override
    public Set<String> getCapabilities() {
        return null;
    }

    @Override
    public DefaultFolderType getDefaultFolderType() {
        return null;
    }

    @Override
    public int getDeletedMessageCount() {
        return 0;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public int getMessageCount() {
        return 0;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public int getNewMessageCount() {
        return 0;
    }

    @Override
    public MessagingPermission getOwnPermission() {
        // Nothing to do
        return null;
    }

    @Override
    public String getParentId() {
        // Nothing to do
        return null;
    }

    @Override
    public List<MessagingPermission> getPermissions() {
        // Nothing to do
        return null;
    }

    @Override
    public char getSeparator() {
        // Nothing to do
        return 0;
    }

    @Override
    public int getUnreadMessageCount() {
        // Nothing to do
        return 0;
    }

    @Override
    public boolean hasSubfolders() {
        // Nothing to do
        return false;
    }

    @Override
    public boolean hasSubscribedSubfolders() {
        // Nothing to do
        return false;
    }

    @Override
    public boolean isDefaultFolder() {
        // Nothing to do
        return false;
    }

    @Override
    public boolean isHoldsFolders() {
        // Nothing to do
        return false;
    }

    @Override
    public boolean isHoldsMessages() {
        // Nothing to do
        return false;
    }

    @Override
    public boolean isRootFolder() {
        // Nothing to do
        return false;
    }

    @Override
    public boolean isSubscribed() {
        // Nothing to do
        return false;
    }

}
