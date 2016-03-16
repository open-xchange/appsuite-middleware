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
 *    trademarks of the OX Software GmbH group of companies.
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
    NO_CONNECTION("Cannot get database connection.", Category.CATEGORY_SERVICE_DOWN, 1, OXExceptionStrings.SQL_ERROR_MSG),
    /**
     * SQL Problem: "%1$s".
     */
    SQL_ERROR("SQL problem: \"%1$s\"", Category.CATEGORY_ERROR, 2, OXExceptionStrings.SQL_ERROR_MSG),
    /**
     * Cannot find resource group with identifier %1$d.
     */
    RESOURCEGROUP_NOT_FOUND("Cannot find resource group with identifier %1$d.", Category.CATEGORY_USER_INPUT, 3, ResourceExceptionMessage.RESOURCEGROUP_NOT_FOUND_MSG_DISPLAY),
    /**
     * Found resource groups with same identifier %1$d.
     */
    RESOURCEGROUP_CONFLICT("Found resource groups with same identifier %1$d.", Category.CATEGORY_USER_INPUT, 4, ResourceExceptionMessage.RESOURCEGROUP_CONFLICT_MSG_DISPLAY),
    /**
     * Cannot find resource with identifier %1$d.
     */
    RESOURCE_NOT_FOUND("Cannot find resource with identifier %1$d.", Category.CATEGORY_USER_INPUT, 5, ResourceExceptionMessage.RESOURCE_NOT_FOUND_MSG_DISPLAY),
    /**
     * Found resource(s) with same identifier %1$s.
     */
    RESOURCE_CONFLICT("Found resource(s) with same identifier %1$s.", Category.CATEGORY_USER_INPUT, 6, ResourceExceptionMessage.RESOURCE_CONFLICT_MSG_DISPLAY),
    /**
     * No resource given.
     */
    NULL("No resource given.", Category.CATEGORY_ERROR, 7),
    /**
     * Missing mandatory field(s) in given resource.
     */
    MANDATORY_FIELD("Missing mandatory field(s) in given resource.", Category.CATEGORY_USER_INPUT, 8, ResourceExceptionMessage.MANDATORY_FIELD_MSG_DISPLAY),
    /**
     * No permission to modify resources in context %1$s
     */
    PERMISSION("No permission to modify resources in context %1$s", Category.CATEGORY_PERMISSION_DENIED, 9, ResourceExceptionMessage.PERMISSION_MSG_DISPLAY),
    /**
     * Found resource(s) with same email address %1$s.
     */
    RESOURCE_CONFLICT_MAIL("Found resource(s) with same email address %1$s.", Category.CATEGORY_USER_INPUT, 10, ResourceExceptionMessage.RESOURCE_CONFLICT_MAIL_MSG_DISPLAY),
    /**
     * Invalid resource identifier: %1$s
     */
    INVALID_RESOURCE_IDENTIFIER("Invalid resource identifier: %1$s", Category.CATEGORY_USER_INPUT, 11, ResourceExceptionMessage.INVALID_RESOURCE_IDENTIFIER_MSG_DISPLAY),
    /**
     * Invalid resource email address: %1$s
     */
    INVALID_RESOURCE_MAIL("Invalid resource E-Mail address: %1$s", Category.CATEGORY_USER_INPUT, 12, ResourceExceptionMessage.INVALID_RESOURCE_MAIL_MSG_DISPLAY),
    /**
     * The resource has been changed in the meantime
     */
    CONCURRENT_MODIFICATION("The resource has been changed in the meantime: %1$s", Category.CATEGORY_CONFLICT, 13, ResourceExceptionMessage.CONCURRENT_MODIFICATION_MSG_DISPLAY);

    /** Message of the exception. */
    private final String message;

    /** Category of the exception. */
    private final Category category;

    /** Detail number of the exception. */
    private final int detailNumber;

    /** Message displayed to the user */
    private final String displayMessage;

    /**
     * Initializes a new {@link ResourceExceptionCode}.
     */
    private ResourceExceptionCode(final String message, final Category category, final int detailNumber) {
        this(message, category, detailNumber, null);
    }

    /**
     * Initializes a new {@link ResourceExceptionCode}.
     */
    private ResourceExceptionCode(final String message, final Category category, final int detailNumber, final String displayMessage) {
        this.message = message;
        this.category = category;
        this.detailNumber = detailNumber;
        this.displayMessage = displayMessage == null ? OXExceptionStrings.MESSAGE : displayMessage;
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
