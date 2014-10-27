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

import java.util.List;

/**
 * {@link GuestShare}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.8.0
 */
public interface GuestShare {

    /**
     * Gets the identifier of the guest user.
     *
     * @return The guest user ID
     */
    int getGuestID();

    /**
     * Gets the identifier of the context of the guest.
     *
     * @return The context ID
     */
    int getContextID();

    /**
     * Gets the authentication mode used for the guest user.
     *
     * @return The authentication mode
     */
    AuthenticationMode getAuthentication();

    /**
     * Gets the token associated with the guest user.
     *
     * @return The token
     */
    String getToken();

    /**
     * Gets a list of all share targets the guest has access to.
     *
     * @return The share targets
     */
    List<ShareTarget> getTargets();

    /**
     * Gets the common module identifier if all contained share targets are pointing to the same module.
     *
     * @return The common module ID, or <code>0</code> if the modules are different between the share targets
     */
    int getCommonModule();

    /**
     * Gets the common folder identifier if all contained share targets are pointing to the same folder.
     *
     * @return The common folder ID, or <code>null</code> if the folders are different between the share targets
     */
    String getCommonFolder();

    /**
     * Resolves a contained share target based on the supplied relative path info.
     *
     * @param path The share-relative path to the target
     * @return The target, or <code>null</code> if not found
     */
    ShareTarget resolveTarget(String path);

    /**
     * Gets a value indicating whether this guest share holds more than one share target or not.
     *
     * @return <code>true</code> if there are at least 2 contained share targets, <code>false</code>, otherwise
     */
    boolean isMultiTarget();

    /**
     * Gets the single share target in case this guest share represents no "multi" target.
     *
     * @return The single share target in case there is exactly one contained target in this guest share, <code>null</code>, otherwise
     */
    ShareTarget getSingleTarget();

}
