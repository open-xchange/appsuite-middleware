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

package com.openexchange.management;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * Management error codes.
 */
public enum ManagementExceptionCode implements DisplayableOXExceptionCode {

    /**
     * MBean registration denied: ManagementAgent is not running.
     */
    NOT_RUNNING(ManagementExceptionCode.NOT_RUNNING_MSG, CATEGORY_CONFIGURATION, 1),
    /**
     * Malformed object name: %1$s
     */
    MALFORMED_OBJECT_NAME(ManagementExceptionCode.MALFORMED_OBJECT_NAME_MSG, CATEGORY_ERROR, 2),
    /**
     * Not compliant MBean: %1$s
     */
    NOT_COMPLIANT_MBEAN(ManagementExceptionCode.NOT_COMPLIANT_MBEAN_MSG, CATEGORY_ERROR, 3),
    /**
     * MBean registration error: %1$s
     */
    MBEAN_REGISTRATION(ManagementExceptionCode.MBEAN_REGISTRATION_MSG, CATEGORY_ERROR, 4),
    /**
     * MBean already exists: %1$s.
     */
    ALREADY_EXISTS(ManagementExceptionCode.ALREADY_EXISTS_MSG, CATEGORY_ERROR, 5),
    /**
     * MBean not found: %1$s.
     */
    NOT_FOUND(ManagementExceptionCode.NOT_FOUND_MSG, CATEGORY_ERROR, 6),
    /**
     * Malformed URL: %1$s
     */
    MALFORMED_URL(ManagementExceptionCode.MALFORMED_URL_MSG, CATEGORY_ERROR, 7),
    /**
     * An I/O error occurred: %1$s
     */
    IO_ERROR(ManagementExceptionCode.IO_ERROR_MSG, CATEGORY_ERROR, 8),
    /**
     * Unknown host error: %1$s
     */
    UNKNOWN_HOST_ERROR(ManagementExceptionCode.UNKNOWN_HOST_ERROR_MSG, CATEGORY_ERROR, 9),
    /**
     * Remote error: %1$s
     */
    REMOTE_ERROR(ManagementExceptionCode.REMOTE_ERROR_MSG, CATEGORY_ERROR, 9),
    /**
     * A JMX connector is already bound to URL %1$s.
     */
    JMX_URL_ALREADY_BOUND(ManagementExceptionCode.JMX_URL_ALREADY_BOUND_MSG, CATEGORY_ERROR, 10),
    /**
     * The following needed service is missing: "%1$s"
     */
    NEEDED_SERVICE_MISSING(ManagementExceptionCode.NEEDED_SERVICE_MISSING_MSG, CATEGORY_SERVICE_DOWN, 11);

    /**
     * MBean registration denied: ManagementAgent is not running.
     */
    private final static String NOT_RUNNING_MSG = "MBean registration denied: ManagementAgent is not running.";

    /**
     * Malformed object name: %1$s
     */
    private final static String MALFORMED_OBJECT_NAME_MSG = "Malformed object name: %1$s";

    /**
     * Not compliant MBean: %1$s
     */
    private final static String NOT_COMPLIANT_MBEAN_MSG = "Not compliant MBean: %1$s";

    /**
     * MBean registration error: %1$s
     */
    private final static String MBEAN_REGISTRATION_MSG = "MBean registration error: %1$s";

    /**
     * MBean already exists: %1$s.
     */
    private final static String ALREADY_EXISTS_MSG = "MBean already exists: %1$s";

    /**
     * MBean not found: %1$s.
     */
    private final static String NOT_FOUND_MSG = "MBean not found: %1$s";

    /**
     * Malformed URL: %1$s
     */
    private final static String MALFORMED_URL_MSG = "Malformed URL: %1$s";

    /**
     * An I/O error occurred: %1$s
     */
    private final static String IO_ERROR_MSG = "An I/O error occurred: %1$s";

    /**
     * Unknown host error: %1$s
     */
    private final static String UNKNOWN_HOST_ERROR_MSG = "Unknown host error: %1$s";

    /**
     * Remote error: %1$s
     */
    private final static String REMOTE_ERROR_MSG = "Remote error: %1$s";

    /**
     * A JMX connector is already bound to URL %1$s.
     */
    private final static String JMX_URL_ALREADY_BOUND_MSG = "A JMX connector is already bound to URL %1$s.";

    /**
     * The following needed service is missing: \"%1$s\"
     */
    public static final String NEEDED_SERVICE_MISSING_MSG = "The following needed service is missing: \"%1$s\"";

    private final String message;

    private final int detailNumber;

    private final Category category;

    /**
     * Message displayed to the user
     */
    private String displayMessage;

    /**
     * Initializes a new {@link ManagementExceptionCode}.
     * 
     * @param message
     * @param category
     * @param detailNumber
     */
    private ManagementExceptionCode(final String message, final Category category, final int detailNumber) {
        this(message, category, detailNumber, null);
    }

    /**
     * Initializes a new {@link ManagementExceptionCode}.
     * 
     * @param message
     * @param category
     * @param detailNumber
     * @param displayMessage
     */
    private ManagementExceptionCode(final String message, final Category category, final int detailNumber, final String displayMessage) {
        this.message = message;
        this.detailNumber = detailNumber;
        this.category = category;
        this.displayMessage = displayMessage == null ? OXExceptionStrings.MESSAGE : displayMessage;
    }

    @Override
    public String getPrefix() {
        return "JMX";
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
