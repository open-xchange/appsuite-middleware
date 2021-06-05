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

package com.openexchange.folderstorage;

import java.util.Set;
import com.google.common.collect.ImmutableSet;
import com.openexchange.mailaccount.UnifiedInboxManagement;

/**
 * {@link RemoveAfterAccessFolder} - A folder which is removed from cache (if elapsed) after it was accessed.
 * <p>
 * Applies only to locally (not globally) cached folders; meaning {@link #isCacheable()} MUST return <code>true</code> AND
 * {@link #isGlobalID()} MUST return <code>false</code>.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface RemoveAfterAccessFolder extends Folder {

    /**
     * Set of ignorable mail protocol identifiers.
     */
    public static final Set<String> IGNORABLES = ImmutableSet.of("pop3", UnifiedInboxManagement.PROTOCOL_UNIFIED_INBOX);

    /**
     * Whether to load subfolders.
     *
     * @return <code>true</code> to load subfolders; otherwise <code>false</code>
     */
    boolean loadSubfolders();

    /**
     * Gets the user identifier.
     *
     * @return The user identifier or <code>-1</code>
     */
    int getUserId();

    /**
     * Gets the context identifier.
     *
     * @return The context identifier or <code>-1</code>
     */
    int getContextId();

}
