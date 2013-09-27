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

import com.openexchange.exception.Category;
import com.openexchange.exception.LogLevel;
import com.openexchange.exception.LogLevelAwareOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;

/**
 * The error codes for settings.
 */
public enum SettingExceptionCodes implements LogLevelAwareOXExceptionCode {
    /** Cannot get connection to database. */
    NO_CONNECTION(SettingExceptionMessage.NO_CONNECTION_MSG, Category.CATEGORY_SERVICE_DOWN, 1),
    /** An SQL problem occures while reading information from the config database. */
    SQL_ERROR(SettingExceptionMessage.SQL_ERROR, Category.CATEGORY_ERROR, 2),
    /** Writing the setting %1$s is not permitted. */
    NO_WRITE(SettingExceptionMessage.NO_WRITE_MSG, Category.CATEGORY_PERMISSION_DENIED, 3),
    /** Unknown setting path %1$s. */
    UNKNOWN_PATH(SettingExceptionMessage.UNKNOWN_PATH_MSG, Category.CATEGORY_ERROR, 4),
    /** Setting "%1$s" is not a leaf one. */
    NOT_LEAF(SettingExceptionMessage.NOT_LEAF_MSG, Category.CATEGORY_ERROR, 5),
    /** Exception while parsing JSON. */
    JSON_READ_ERROR(SettingExceptionMessage.JSON_READ_ERROR_MSG, Category.CATEGORY_ERROR, 6),
    /** Problem while initialising configuration tree. */
    INIT(SettingExceptionMessage.INIT_MSG, Category.CATEGORY_ERROR, 8),
    /** Invalid value %s written to setting %s. */
    INVALID_VALUE(SettingExceptionMessage.INVALID_VALUE_MSG, Category.CATEGORY_USER_INPUT, 9, LogLevel.ERROR),
    /** Found duplicate database identifier %d. Not adding preferences item. */
    DUPLICATE_ID(SettingExceptionMessage.DUPLICATE_ID_MSG, Category.CATEGORY_ERROR, 10),
    /** Found duplicate path %s. */
    DUPLICATE_PATH(SettingExceptionMessage.DUPLICATE_PATH_MSG, Category.CATEGORY_ERROR, 12),
    /** Subsystem error. */
    SUBSYSTEM(SettingExceptionMessage.SUBSYSTEM_MSG, Category.CATEGORY_SERVICE_DOWN, 13),
    /** Not allowed operation. */
    NOT_ALLOWED(SettingExceptionMessage.NOT_ALLOWED_MSG, Category.CATEGORY_ERROR, 14),
    /** Reached maximum retries writing setting %s. */
    MAX_RETRY(SettingExceptionMessage.MAX_RETRY_MSG, Category.CATEGORY_TRY_AGAIN, 15);

    private final String message;
    private final Category category;
    private final int detailNumber;
    private final LogLevel logLevel;

    private SettingExceptionCodes(final String message, final Category category, final int detailNumber) {
        this(message, category, detailNumber, null);
    }

    private SettingExceptionCodes(final String message, final Category category, final int detailNumber, final LogLevel logLevel) {
        this.message = message;
        this.category = category;
        this.detailNumber = detailNumber;
        this.logLevel = logLevel;
    }

    @Override
    public LogLevel getLogLevel() {
        return logLevel;
    }

    @Override
    public String getPrefix() {
        return "USS";
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public int getNumber() {
        return detailNumber;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(final OXException e) {
        return OXExceptionFactory.getInstance().equals(this, e);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @return The newly created {@link OXException} instance
     */
    public OXException create() {
        return OXExceptionFactory.getInstance().create(this, new Object[0]);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final Object... args) {
        return OXExceptionFactory.getInstance().create(this, (Throwable) null, args);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     *
     * @param cause The optional initial cause
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final Throwable cause, final Object... args) {
        return OXExceptionFactory.getInstance().create(this, cause, args);
    }
}
