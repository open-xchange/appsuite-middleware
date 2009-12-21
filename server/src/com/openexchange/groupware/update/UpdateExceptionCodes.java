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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.groupware.update;

import static com.openexchange.groupware.update.UpdateExceptionMessages.*;
import com.openexchange.exceptions.OXErrorMessage;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.update.internal.UpdateExceptionFactory;

/**
 * {@link UpdateExceptionCodes}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public enum UpdateExceptionCodes implements OXErrorMessage {

    /**
     * Current version number %1$s is already lower than or equal to desired version number %2$s.
     */
    ONLY_REDUCE(ONLY_REDUCE_MSG, Category.USER_INPUT, 13),
    /**
     * A SQL problem occurred: %1$s.
     */
    SQL_PROBLEM(SQL_PROBLEM_MSG, Category.CODE_ERROR, 14),
    /**
     * Error loading update task "%1$s".
     */
    LOADING_TASK_FAILED(LOADING_TASK_FAILED_MSG, Category.USER_INPUT, 15),
    /**
     * Unknown schema name: %1$s.
     */
    UNKNOWN_SCHEMA(UNKNOWN_SCHEMA_MSG, Category.USER_INPUT, 16),
    /**
     * %1$s.
     */
    OTHER_PROBLEM("%1$s", Category.CODE_ERROR, 97),
    /**
     * Processed a wrong number of rows in database. Expected %1$d rows but worked on %2$d rows.
     */
    WRONG_ROW_COUNT(WRONG_ROW_COUNT_MSG, Category.CODE_ERROR, 98),
    /**
     * Updating schema %1$s failed. Cause: %2$s.
     */
    UPDATE_FAILED(UPDATE_FAILED_MSG, Category.CODE_ERROR, 99);

    final String message;

    final Category category;

    final int number;

    private UpdateExceptionCodes(String message, Category category, int number) {
        this.message = message;
        this.category = category;
        this.number = number;
    }

    public Category getCategory() {
        return category;
    }

    public int getDetailNumber() {
        return number;
    }

    public String getHelp() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getMessage() {
        return message;
    }

    public UpdateException create(Object... messageArgs) {
        return UpdateExceptionFactory.getInstance().create(this, messageArgs);
    }

    public UpdateException create(Throwable cause, Object... messageArgs) {
        return UpdateExceptionFactory.getInstance().create(this, cause, messageArgs);
    }
}
