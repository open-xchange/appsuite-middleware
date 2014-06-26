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


package com.openexchange.resource;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * Error codes for the resource exception.
 */
public enum ResourceExceptionCode implements DisplayableOXExceptionCode {
    /**
     * A database connection Cannot be obtained.
     */
    NO_CONNECTION(ResourceExceptionCode.NO_CONNECTION_MSG, Category.CATEGORY_SERVICE_DOWN, 1, OXExceptionStrings.SQL_ERROR_MSG),
    /**
     * SQL Problem: "%1$s".
     */
    SQL_ERROR(ResourceExceptionCode.SQL_ERROR_MSG, Category.CATEGORY_ERROR, 2, OXExceptionStrings.SQL_ERROR_MSG),
    /**
     * Cannot find resource group with identifier %1$d.
     */
    RESOURCEGROUP_NOT_FOUND(ResourceExceptionCode.RESOURCEGROUP_NOT_FOUND_MSG, Category.CATEGORY_USER_INPUT, 3, ResourceExceptionMessage.RESOURCEGROUP_NOT_FOUND_MSG_DISPLAY),
    /**
     * Found resource groups with same identifier %1$d.
     */
    RESOURCEGROUP_CONFLICT(ResourceExceptionCode.RESOURCEGROUP_CONFLICT_MSG, Category.CATEGORY_USER_INPUT, 4, ResourceExceptionMessage.RESOURCEGROUP_CONFLICT_MSG_DISPLAY),
    /**
     * Cannot find resource with identifier %1$d.
     */
    RESOURCE_NOT_FOUND(ResourceExceptionCode.RESOURCE_NOT_FOUND_MSG, Category.CATEGORY_USER_INPUT, 5, ResourceExceptionMessage.RESOURCE_NOT_FOUND_MSG_DISPLAY),
    /**
     * Found resource(s) with same identifier %1$s.
     */
    RESOURCE_CONFLICT(ResourceExceptionCode.RESOURCE_CONFLICT_MSG, Category.CATEGORY_USER_INPUT, 6, ResourceExceptionMessage.RESOURCE_CONFLICT_MSG_DISPLAY),
    /**
     * No resource given.
     */
    NULL(ResourceExceptionCode.NULL_MSG, Category.CATEGORY_ERROR, 7),
    /**
     * Missing mandatory field(s) in given resource.
     */
    MANDATORY_FIELD(ResourceExceptionCode.MANDATORY_FIELD_MSG, Category.CATEGORY_ERROR, 8),
    /**
     * No permission to modify resources in context %1$s
     */
    PERMISSION(ResourceExceptionCode.PERMISSION_MSG, Category.CATEGORY_PERMISSION_DENIED, 9, ResourceExceptionMessage.PERMISSION_MSG_DISPLAY),
    /**
     * Found resource(s) with same email address %1$s.
     */
    RESOURCE_CONFLICT_MAIL(ResourceExceptionCode.RESOURCE_CONFLICT_MAIL_MSG, Category.CATEGORY_ERROR, 10),
    /**
     * Invalid resource identifier: %1$s
     */
    INVALID_RESOURCE_IDENTIFIER(ResourceExceptionCode.INVALID_RESOURCE_IDENTIFIER_MSG, Category.CATEGORY_USER_INPUT, 11, ResourceExceptionMessage.INVALID_RESOURCE_IDENTIFIER_MSG_DISPLAY),
    /**
     * Invalid resource email address: %1$s
     */
    INVALID_RESOURCE_MAIL(ResourceExceptionCode.INVALID_RESOURCE_MAIL_MSG, Category.CATEGORY_USER_INPUT, 12, ResourceExceptionMessage.INVALID_RESOURCE_MAIL_MSG_DISPLAY),
    /**
     * The resource has been changed in the meantime
     */
    CONCURRENT_MODIFICATION(ResourceExceptionCode.CONCURRENT_MODIFICATION_MSG, Category.CATEGORY_CONFLICT, 13, ResourceExceptionMessage.CONCURRENT_MODIFICATION_MSG_DISPLAY);

    /**
     * A database connection Cannot be obtained.
     */
    private final static String NO_CONNECTION_MSG = "Cannot get database connection.";

    /**
     * SQL Problem: "%1$s".
     */
    private final static String SQL_ERROR_MSG = "SQL problem: \"%1$s\"";

    /**
     * Cannot find resource group with identifier %1$d.
     */
    private final static String RESOURCEGROUP_NOT_FOUND_MSG = "Cannot find resource group with identifier %1$d.";

    /**
     * Found resource groups with same identifier %1$d.
     */
    private final static String RESOURCEGROUP_CONFLICT_MSG = "Found resource groups with same identifier %1$d.";

    /**
     * Cannot find resource with identifier %1$d.
     */
    private final static String RESOURCE_NOT_FOUND_MSG = "Cannot find resource with identifier %1$d.";

    /**
     * Found resource(s) with same identifier %1$s.
     */
    private final static String RESOURCE_CONFLICT_MSG = "Found resource(s) with same identifier %1$s.";

    /**
     * No resource given.
     */
    private final static String NULL_MSG = "No resource given.";

    /**
     * Missing mandatory field(s) in given resource.
     */
    private final static String MANDATORY_FIELD_MSG = "Missing mandatory field(s) in given resource.";

    /**
     * No permission to modify resources in context %1$s
     */
    private final static String PERMISSION_MSG = "No permission to modify resources in context %1$s";

    /**
     * Found resource(s) with same email address %1$s.
     */
    private final static String RESOURCE_CONFLICT_MAIL_MSG = "Found resource(s) with same email address %1$s.";

    /**
     * Invalid resource identifier: %1$s
     */
    private final static String INVALID_RESOURCE_IDENTIFIER_MSG = "Invalid resource identifier: %1$s";

    /**
     * Invalid resource email address: %1$s
     */
    private final static String INVALID_RESOURCE_MAIL_MSG = "Invalid resource E-Mail address: %1$s";

    /**
     * The resource has been changed in the meantime: %1$s
     */
    private final static String CONCURRENT_MODIFICATION_MSG = "The resource has been changed in the meantime: %1$s";

    /**
     * Message of the exception.
     */
    final String message;

    /**
     * Category of the exception.
     */
    final Category category;

    /**
     * Detail number of the exception.
     */
    final int detailNumber;

    /**
     * Message displayed to the user
     */
    private String displayMessage;

    /**
     * Default constructor.
     *
     * @param message message.
     * @param category category.
     * @param detailNumber detail number.
     */
    private ResourceExceptionCode(final String message, final Category category, final int detailNumber) {
        this(message, category, detailNumber, null);
    }

    /**
     * Default constructor.
     * 
     * @param message
     * @param category
     * @param detailNumber
     * @param displayMessage
     */
    private ResourceExceptionCode(final String message, final Category category, final int detailNumber, final String displayMessage) {
        this.message = message;
        this.category = category;
        this.detailNumber = detailNumber;
        this.displayMessage = displayMessage != null ? displayMessage : OXExceptionStrings.MESSAGE;
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public String getPrefix() {
        return "RES";
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
     * {@inheritDoc}
     */
    @Override
    public String getDisplayMessage() {
        return this.displayMessage;
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
