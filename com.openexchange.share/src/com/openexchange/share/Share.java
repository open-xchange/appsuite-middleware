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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.share;

import java.util.Date;
import com.openexchange.groupware.modules.Module;


/**
 * {@link Share}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.1
 */
public interface Share {

    /**
     * Gets the share's unique token, usually an unformatted UID string.
     *
     * @return The token
     */
    String getToken();

    /**
     * Gets the identifier of the context the share resides in.
     *
     * @return The context ID
     */
    int getContextID();

    /**
     * Gets the groupware module of the share's target folder.
     *
     * @return The module
     */
    Module getModule();

    /**
     * Gets the identifier of the share's folder.
     *
     * @return The folder ID
     */
    String getFolder();

    /**
     * Gets a value indicating whether the share points to a folder or a single item.
     *
     * @return <code>true</code> if the share points to a folder, <code>false</code>, otherwise
     */
    boolean isFolder();

    /**
     * Gets the identifier of the share's item in case the share is not a folder share.
     *
     * @return The item ID, or <code>null</code> if the share references a folder
     */
    String getItem();

    /**
     * Gets the creation date of the share.
     *
     * @return The creation date
     */
    Date getCreated();

    /**
     * Gets the identifier of the user that initially created the share.
     *
     * @return The ID of the user who created the share
     */
    int getCreatedBy();

    /**
     * Gets the date when the share was last modified.
     *
     * @return The last modification date
     */
    Date getLastModified();

    /**
     * Gets the identifier of the user that performed the last modification on the share.
     *
     * @return The ID of the user who made the last modification on the share
     */
    int getModifiedBy();

    /**
     * If defined, gets the date when this share expires, i.e. it should be no longer accessible.
     *
     * @return The expiry date of the share, or <code>null</code> if not defined
     */
    Date getExpires();

    /**
     * Gets a value indicating whether this share is expired or not.
     *
     * @return <code>true</code> if the share is expired, <code>false</code>, otherwise
     */
    boolean isExpired();

    /**
     * Gets the identifier of the guest user that is allowed to access this share.
     *
     * @return The ID of the guest user
     */
    int getGuest();

    /**
     * Gets the authentication mode used to restrict access to the share.
     *
     * @return The authentication mode
     */
    AuthenticationMode getAuthentication();

}
