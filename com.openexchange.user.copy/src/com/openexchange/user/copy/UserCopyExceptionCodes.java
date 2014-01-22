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

package com.openexchange.user.copy;

import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;

/**
 * Lists all possible exceptions that can occur when a user is moved.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public enum UserCopyExceptionCodes implements OXExceptionCode {

    /** Unable to determine next update task to execute. Enqueued: %1$s. To sort: %2$s. */
    UNRESOLVABLE_DEPENDENCIES(UserCopyExceptionMessages.UNRESOLVABLE_DEPENDENCIES_MSG, Category.CATEGORY_ERROR, 1),
    /** SQL Problem. */
    SQL_PROBLEM(UserCopyExceptionMessages.SQL_PROBLEM_MSG, Category.CATEGORY_ERROR, 2),
    /** Severe problem occurred. */
    UNKNOWN_PROBLEM(UserCopyExceptionMessages.UNKNOWN_PROBLEM_MSG, Category.CATEGORY_ERROR, 3),
    /** Problem with UserService. */
    USER_SERVICE_PROBLEM(UserCopyExceptionMessages.USER_SERVICE_PROBLEM_MSG, Category.CATEGORY_ERROR, 4),
    /** A private folder (%1$s) without existing parent (%2$s) was found. */
    MISSING_PARENT_FOLDER(UserCopyExceptionMessages.MISSING_PARENT_FOLDER_MSG, Category.CATEGORY_ERROR, 5),
    /** Database pooling error. */
    DB_POOLING_PROBLEM(UserCopyExceptionMessages.DB_POOLING_PROBLEM_MSG, Category.CATEGORY_ERROR, 6),
    /** Problem with FileStorage. */
    FILE_STORAGE_PROBLEM(UserCopyExceptionMessages.FILE_STORAGE_PROBLEM_MSG, Category.CATEGORY_ERROR, 7),
    /** Could not generate a new sequence id for type %1$s. */
    ID_PROBLEM(UserCopyExceptionMessages.ID_PROBLEM_MSG, Category.CATEGORY_ERROR, 8),
    /** Did not find contact for user %1$s in context %2$s. */
    USER_CONTACT_MISSING(UserCopyExceptionMessages.USER_CONTACT_MISSING_MSG, Category.CATEGORY_ERROR, 9),
    /** Could not save user's mail settings. */
    SAVE_MAIL_SETTINGS_PROBLEM(UserCopyExceptionMessages.SAVE_MAIL_SETTINGS_PROBLEM_MSG, Category.CATEGORY_ERROR, 10),
     /** A user named %1$s already exists in destination context %2$s. */
    USER_ALREADY_EXISTS(UserCopyExceptionMessages.USER_ALREADY_EXISTS_MSG, Category.CATEGORY_USER_INPUT, 11),
    ;

    private final String message;
    private final Category category;
    private final int number;


    private UserCopyExceptionCodes(final String message, final Category category, final int number) {
        this.message = message;
        this.category = category;
        this.number = number;
    }

    public String getMessage() {
        return message;
    }

    public Category getCategory() {
        return category;
    }

    public OXException create(final Object... args) {
        return OXExceptionFactory.getInstance().create(this, args);
    }

    public OXException create(final Throwable cause, final Object... args) {
        return OXExceptionFactory.getInstance().create(this, cause, args);
    }

    public boolean equals(final OXException e) {
        return OXExceptionFactory.getInstance().equals(this, e);
    }

    public int getNumber() {
        return number;
    }

    public String getPrefix() {
        return "UCP";
    }
}
