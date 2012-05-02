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

package com.openexchange.groupware.settings;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link SettingExceptionMessage}
 * 
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 */
public class SettingExceptionMessage implements LocalizableStrings {

    /**
     * Initializes a new {@link SettingExceptionMessage}.
     */
    private SettingExceptionMessage() {
        super();
    }

    /** Cannot get connection to database. */
    public final static String NO_CONNECTION_MSG = "Cannot get connection to database.";

    /** An SQL problem occures while reading information from the config database. */
    public final static String SQL_ERROR = null;

    /** Writing the setting %1$s is not permitted. */
    public final static String NO_WRITE_MSG = "Writing the setting %1$s is not permitted.";

    /** Unknown setting path %1$s. */
    public final static String UNKNOWN_PATH_MSG = "Unknown setting path %1$s.";

    /** Setting "%1$s" is not a leaf one. */
    public final static String NOT_LEAF_MSG = "Setting \"%1$s\" is not a leaf one.";

    /** Exception while parsing JSON. */
    public final static String JSON_READ_ERROR_MSG = "Exception while parsing JSON.";

    /** Problem while initialising configuration tree. */
    public final static String INIT_MSG = "Problem while initialising configuration tree.";

    /** Invalid value %s written to setting %s. */
    public final static String INVALID_VALUE_MSG = "Invalid value %s written to setting %s.";

    /** Found duplicate database identifier %d. Not adding preferences item. */
    public final static String DUPLICATE_ID_MSG = "Found duplicate database identifier %d. Not adding preferences item.";

    /** Found duplicate path %s. */
    public final static String DUPLICATE_PATH_MSG = "Found duplicate path %s.";

    /** Subsystem error. */
    public final static String SUBSYSTEM_MSG = "Error during use of a subsystem";

    /** Not allowed operation. */
    public final static String NOT_ALLOWED_MSG = "Not allowed operation.";

    /** Reached maximum retries writing setting %s. */
    public final static String MAX_RETRY_MSG = "Reached maximum retries writing setting %s.";

}
