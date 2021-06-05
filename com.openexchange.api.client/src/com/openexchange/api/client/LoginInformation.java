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

package com.openexchange.api.client;

import com.openexchange.annotation.Nullable;

/**
 * {@link LoginInformation} - Information that are obtained by logging in into a remote OX server.
 * <p>
 * <b>Note:</b> All data that can be obtained by this class comes from the remote server. Thus all
 * identifier are from the remote server and do not belong to any context, user or share in this
 * local server.
 *
 * @author <a href="mailto:daniel.becker@open-xchange.com">Daniel Becker</a>
 * @since v7.10.5
 */
public interface LoginInformation {

    /**
     * Get the remote session ID
     *
     * @return The remote session ID or <code>null</code> if not available
     */
    @Nullable
    String getRemoteSessionId();

    /**
     * Get the user or rather the guest mail address the user has on the target system
     *
     * @return The user or rather guest user mail address or <code>null</code> if not available
     */
    @Nullable
    String getRemoteMailAddress();

    /**
     * Get the user or rather guest identifier the user has on the target system
     *
     * @return The user or rather guest user ID or <code>-1</code> if not available
     */
    int getRemoteUserId();

    /**
     * Get the context identifier of the user or rather guest the user has on the target system
     *
     * @return The context ID or <code>-1</code> if not available
     */
    int getRemoteContextId();

    /**
     * Get the folder identifier of the target
     *
     * @return The folder ID or <code>null</code> if not available
     */
    @Nullable
    String getRemoteFolderId();

    /**
     * The module that has been accessed
     * 
     * @return The module
     */
    String getModule();

    /**
     * The remote item identifier that has been accessed, in case one file and not a folder was accessed
     * 
     * @return The item
     */
    String getItem();
    
    /**
     * Get the login type the remote system announced for the share 
     *
     * @return The login type, e.g. <code>guest</code> or <code>message</code>
     */
    String getLoginType();

    /**
     * Get an additional value that was gathered along the login request
     *
     * @param key The key value
     * @return The value fitting to the key
     */
    @Nullable
    String getAdditional(String key);

}
