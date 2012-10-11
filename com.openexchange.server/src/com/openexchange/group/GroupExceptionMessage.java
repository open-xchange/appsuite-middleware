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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.group;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link GroupExceptionMessage}
 * 
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class GroupExceptionMessage implements LocalizableStrings {

    /**
     * Initializes a new {@link GroupExceptionMessage}.
     */
    private GroupExceptionMessage() {
        super();
    }

    // A database connection Cannot be obtained.
    public final static String NO_CONNECTION_MSG = "Cannot get database connection.";

    // SQL Problem: "%1$s".
    public final static String SQL_ERROR_MSG = "SQL Problem: \"%1$s\"";

    // No group given.
    public final static String NULL_MSG = "No group given.";

    // The mandatory field %1$s is not defined.
    public final static String MANDATORY_MISSING_MSG = "The mandatory field %1$s is not defined.";

    // The simple name contains invalid characters: "%1$s".
    public final static String NOT_ALLOWED_SIMPLE_NAME_MSG = "The simple name contains invalid characters: \"%1$s\".";

    // Another group with the same identifier name exists: %1$d.
    public final static String DUPLICATE_MSG = "Another group with the same identifier name exists: %1$d.";

    // Group contains a not existing member %1$d.
    public final static String NOT_EXISTING_MEMBER_MSG = "Group contains a not existing member %1$d.";

    // Group contains invalid data: "%1$s".
    public final static String INVALID_DATA_MSG = "Group contains invalid data: \"%1$s\".";

    // You are not allowed to create groups.
    public final static String NO_CREATE_PERMISSION_MSG = "You are not allowed to create groups.";

    // Edit Conflict. Your change cannot be completed because somebody else has made a conflicting change to the same item. Please refresh
    // or synchronize and try again.
    public final static String MODIFIED_MSG = "Edit Conflict. Your change cannot be completed because somebody else has made a conflicting" +
    		" change to the same item. " + "Please refresh or synchronize and try again.";

    // You are not allowed to change groups.
    public final static String NO_MODIFY_PERMISSION_MSG = "You are not allowed to change groups.";

    // You are not allowed to delete groups.
    public final static String NO_DELETE_PERMISSION_MSG = "You are not allowed to delete groups.";

    // Group "%1$s" can not be deleted.
    public final static String NO_GROUP_DELETE_MSG = "Group \"%1$s\" can not be deleted.";

    // Group "%1$s" can not be changed.
    public final static String NO_GROUP_UPDATE_MSG = "Group \"%1$s\" can not be changed.";

}
