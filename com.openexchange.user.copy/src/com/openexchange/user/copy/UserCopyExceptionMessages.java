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

package com.openexchange.user.copy;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link UserCopyExceptionMessages}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class UserCopyExceptionMessages implements LocalizableStrings {

    public static final String UNRESOLVABLE_DEPENDENCIES_MSG = "Unable to determine next copy task to execute. Enqueued: %1$s. To sort: %2$s.";

    public static final String SQL_PROBLEM_MSG = "SQL problem.";

    public static final String UNKNOWN_PROBLEM_MSG = "Unexpected problem occurred.";
    
    public static final String USER_SERVICE_PROBLEM_MSG = "Problem with UserService.";
    
    public static final String MISSING_PARENT_FOLDER_MSG = "A private folder (%1$s) without existing parent (%2$s) was found.";
    
    public static final String DB_POOLING_PROBLEM_MSG = "Database pooling error.";
    
    public static final String FILE_STORAGE_PROBLEM_MSG = "Problem with FileStorage.";
    
    public static final String ID_PROBLEM_MSG = "Could not generate a new sequence id for type %1$s.";
    
    public static final String USER_CONTACT_MISSING_MSG = "Did not find contact for user %1$s in context %2$s.";
    
    public static final String SAVE_MAIL_SETTINGS_PROBLEM_MSG = "Could not save user's mail settings.";

    public static final String USER_ALREADY_EXISTS_MSG = "A user named %1$s already exists in destination context %2$s.";
    

    private UserCopyExceptionMessages() {
        super();
    }
}
