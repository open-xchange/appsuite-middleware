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

package com.openexchange.gdpr.dataexport.provider.mail.internal;

/**
 * {@link Folder} - The mail folder abstraction.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.3
 */
public interface Folder extends FolderAccess {

    /**
     * Gets the full name.
     *
     * @return The full name ({@link #DEFAULT_FOLDER_ID} if this mail folder denotes the root folder)
     */
    String getFullname();

    /**
     * Checks if this mail folder denotes the TRASH folder.
     *
     * @return <code>true</code> if this mail folder denotes the TRASH folder; otherwise <code>false</code>
     */
    boolean isTrash();

    /**
     * Gets the name.
     *
     * @return The name
     */
    String getName();

    /**
     * Returns whether the denoted mail folder is subscribed or not.
     * <p>
     * If mailing system does not support subscription, <code>true</code> is supposed to be returned.
     *
     * @return Whether the denoted mail folder is subscribed or not
     */
    boolean isSubscribed();

    /**
     * Checks if this folder is able to hold messages.
     *
     * @return <code>true</code> if this folder is able to hold messages; otherwise <code>false</code>
     */
    boolean isHoldsMessages();

    /**
     * Checks if this folder is able to hold folders.
     *
     * @return <code>true</code> if this folder is able to hold folders; otherwise <code>false</code>
     */
    boolean isHoldsFolders();

    /**
     * Checks if this folder denotes the root folder
     *
     * @return <code>true</code> if this folder denotes the root folder; otherwise <code>false</code>
     */
    boolean isRootFolder();

    /**
     * Checks if this folder is shared.
     *
     * @return <code>true</code> if this folder is shared; otherwise <code>false</code>
     */
    boolean isShared();

    /**
     * Checks if this folder is public.
     *
     * @return <code>true</code> if this folder is public; otherwise <code>false</code>
     */
    boolean isPublic();

}
