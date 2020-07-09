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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.appsuite.client;

import com.openexchange.annotation.Nullable;

/**
 * {@link LoginInformation}
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
     * Get an additional value that was gathered along the login request
     *
     * @param key The key value
     * @return The value fitting to the key
     */
    @Nullable
    String getAdditional(String key);

}
