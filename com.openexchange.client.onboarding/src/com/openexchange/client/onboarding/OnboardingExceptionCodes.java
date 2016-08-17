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

package com.openexchange.client.onboarding;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * {@link OnboardingExceptionCodes} - The error codes for on-boarding module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.1
 */
public enum OnboardingExceptionCodes implements DisplayableOXExceptionCode {

    /**
     * Unexpected error: %1$s
     */
    UNEXPECTED_ERROR("Unexpected error: %1$s", null, Category.CATEGORY_ERROR, 1),
    /**
     * An I/O error occurred: %1$s
     */
    IO_ERROR("An I/O error occurred: %1$s", null, Category.CATEGORY_ERROR, 2),
    /**
     * An SQL error occurred: %1$s
     */
    SQL_ERROR("An SQL error occurred: %1$s", null, Category.CATEGORY_ERROR, 3),
    /**
     * No such provider for identifier: %1$s
     */
    NOT_FOUND("No such provider for identifier: %1$s", null, Category.CATEGORY_ERROR, 4),
    /**
     * Missing the following input field: %1$s
     */
    MISSING_INPUT_FIELD("Missing the following input field: %1$s", null, Category.CATEGORY_ERROR, 5),
    /**
     * Invalid composite identifier: %1$s
     */
    INVALID_COMPOSITE_ID("Invalid composite identifier: %1$s", null, Category.CATEGORY_ERROR, 6),
    /**
     * Invalid scenario configuration for %1$s
     */
    INVALID_SCENARIO_CONFIGURATION("Invalid scenario configuration for %1$s", null, Category.CATEGORY_CONFIGURATION, 7),
    /**
     * Duplicate scenario configuration for %1$s
     */
    DUPLICATE_SCENARIO_CONFIGURATION("Duplicate scenario configuration for %1$s", null, Category.CATEGORY_CONFIGURATION, 8),
    /**
     * No such scenario for identifier: %1$s
     */
    NO_SUCH_SCENARIO("No such scenario for identifier: %1$s", null, Category.CATEGORY_CONFIGURATION, 9),
    /**
     * Scenario is disabled: %1$s
     */
    DISABLED_SCENARIO("Scenario is disabled: %1$s", null, Category.CATEGORY_CONFIGURATION, 10),
    /**
     * Scenario is invalid: %1$s. No such provider %2$s
     */
    INVALID_SCENARIO("Scenario is disabled: %1$s", null, Category.CATEGORY_CONFIGURATION, 11),
    /**
     * Invalid device identifier: %1$s
     */
    INVALID_DEVICE_ID("Invalid device identifier: %1$s", null, Category.CATEGORY_USER_INPUT, 12),
    /**
     * Provider %1$s denied execution for scenario %2$s
     */
    EXECUTION_DENIED("Provider %1$s denied execution for scenario %2$s", OnboardingExceptionMessages.EXECUTION_DENIED_MSG, Category.CATEGORY_ERROR, 13),
    /**
     * Execution for scenario %1$s failed
     */
    EXECUTION_FAILED("Execution for scenario %1$s failed", OnboardingExceptionMessages.EXECUTION_FAILED_MSG, Category.CATEGORY_ERROR, 14),
    /**
     * Provider %1$s does not support device %2$s
     */
    UNSUPPORTED_DEVICE("Provider %1$s does not support device %2$s", null, Category.CATEGORY_ERROR, 15),
    /**
     * Provider %1$s does not support action %2$s for type %3$s
     */
    UNSUPPORTED_ACTION("Action %1$s is not supported", null, Category.CATEGORY_ERROR, 16),
    /**
     * Provider %1$s does not support type %2$s
     */
    UNSUPPORTED_TYPE("Provider %1$s does not support type %2$s", null, Category.CATEGORY_ERROR, 17),
    /**
     * Missing property: %1$s
     */
    MISSING_PROPERTY("Missing property: %1$s", null, Category.CATEGORY_CONFIGURATION, 18),
    /**
     * No such type for identifier %1$s in scenario %2$s
     */
    NO_SUCH_TYPE("No such type for identifier %1$s in scenario %2$s", null, Category.CATEGORY_CONFIGURATION, 19),
    /**
     * Sent quota exceeded. You are only allowed to send 1 SMS in %1$s seconds.
     */
    SENT_QUOTA_EXCEEDED("Sent quota exceeded. You are only allowed to send 1 SMS in %1$s seconds.", OnboardingExceptionMessages.SENT_QUOTA_EXCEEDED_MSG, Category.CATEGORY_USER_INPUT, 20),
    /**
     * The download link is invalid.
     */
    INVALID_DOWNLOAD_LINK("The download link is invalid.", OnboardingExceptionMessages.INVALID_DOWNLOAD_LINK_MSG, Category.CATEGORY_USER_INPUT, 21),
    /**
     * The phone number %1$s is invalid.
     */
    INVALID_PHONE_NUMBER("The phone number %1$s is invalid.", OnboardingExceptionMessages.INVALID_PHONE_NUMBER_MSG, Category.CATEGORY_USER_INPUT, 22),
    /**
     * Invalid link type %1$s in scenario configuration for %2$s
     */
    INVALID_LINK_TYPE_IN_SCENARIO_CONFIGURATION("Invalid link type %1$s in scenario configuration for %2$s", null, Category.CATEGORY_CONFIGURATION, 23)
    ;

    /** The error code prefix for on-boarding module */
    public static final String PREFIX = "ONBRD";

    private final Category category;
    private final int number;
    private final String message;
    private final String displayMessage;

    private OnboardingExceptionCodes(String message, String displayMessage, Category category, int detailNumber) {
        this.message = message;
        this.displayMessage = displayMessage == null ? OXExceptionStrings.MESSAGE : displayMessage;
        this.number = detailNumber;
        this.category = category;
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String getDisplayMessage() {
        return displayMessage;
    }

    @Override
    public int getNumber() {
        return number;
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
    public OXException create(Throwable cause, Object... args) {
        return OXExceptionFactory.getInstance().create(this, cause, args);
    }

}
