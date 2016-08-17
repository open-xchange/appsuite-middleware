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

package com.openexchange.quota;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * {@link QuotaExceptionCodes} - Enumeration of all {@link OXException}s known in quota module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum QuotaExceptionCodes implements DisplayableOXExceptionCode {

    /**
     * An error occurred: %1$s
     */
    UNEXPECTED_ERROR("An error occurred: %1$s", OXExceptionStrings.MESSAGE, CATEGORY_ERROR, 1),
    /**
     * An I/O error occurred: %1$s
     */
    IO_ERROR("An I/O error occurred: %1$s", OXExceptionStrings.MESSAGE, CATEGORY_ERROR, 2),
    /**
     * Quota exceeded. Please delete some items in order to create new ones.
     */
    QUOTA_EXCEEDED("Quota exceeded", QuotaExceptionMessages.QUOTA_EXCEEDED_MSG, CATEGORY_USER_INPUT, 3),
    /**
     * Quota exceeded for calendar. Quota used: %1$s. Quota limit: %2$s.
     */
    QUOTA_EXCEEDED_CALENDAR("Quota exceeded for calendar. Quota used: %1$s. Quota limit: %2$s.", QuotaExceptionMessages.QUOTA_EXCEEDED_CALENDAR_MSG, CATEGORY_USER_INPUT, 3), // Yes, the same error code number as "QUOTA_EXCEEDED"
    /**
     * Quota exceeded for contacts. Quota used: %1$s. Quota limit: %2$s.
     */
    QUOTA_EXCEEDED_CONTACTS("Quota exceeded for contacts. Quota used: %1$s. Quota limit: %2$s.", QuotaExceptionMessages.QUOTA_EXCEEDED_CONTACTS_MSG, CATEGORY_USER_INPUT, 3), // Yes, the same error code number as "QUOTA_EXCEEDED"
    /**
     * Quota exceeded for tasks. Quota used: %1$s. Quota limit: %2$s.
     */
    QUOTA_EXCEEDED_TASKS("Quota exceeded for tasks. Quota used: %1$s. Quota limit: %2$s.", QuotaExceptionMessages.QUOTA_EXCEEDED_TASKS_MSG, CATEGORY_USER_INPUT, 3), // Yes, the same error code number as "QUOTA_EXCEEDED"
    /**
     * Quota exceeded for files. Quota used: %1$s. Quota limit: %2$s.
     */
    QUOTA_EXCEEDED_FILES("Quota exceeded for files. Quota used: %1$s. Quota limit: %2$s.", QuotaExceptionMessages.QUOTA_EXCEEDED_FILES_MSG, CATEGORY_USER_INPUT, 3), // Yes, the same error code number as "QUOTA_EXCEEDED"
    /**
     * Quota exceeded for attachments. Quota used: %1$s. Quota limit: %2$s.
     */
    QUOTA_EXCEEDED_ATTACHMENTS("Quota exceeded for attachments. Quota used: %1$s. Quota limit: %2$s.", QuotaExceptionMessages.QUOTA_EXCEEDED_ATTACHMENTS_MSG, CATEGORY_USER_INPUT, 3), // Yes, the same error code number as "QUOTA_EXCEEDED"
    /**
     * Quota exceeded for shares. Quota used: %1$s. Quota limit: %2$s.
     */
    QUOTA_EXCEEDED_SHARES("Quota exceeded for shares. Quota used: %1$s. Quota limit: %2$s.", QuotaExceptionMessages.QUOTA_EXCEEDED_SHARES_MSG, CATEGORY_USER_INPUT, 3), // Yes, the same error code number as "QUOTA_EXCEEDED"
    /**
     * Quota exceeded for share links. Quota used: %1$s. Quota limit: %2$s.
     */
    QUOTA_EXCEEDED_SHARE_LINKS("Quota exceeded for share links. Quota used: %1$s. Quota limit: %2$s.", QuotaExceptionMessages.QUOTA_EXCEEDED_SHARE_LINKS_MSG, CATEGORY_USER_INPUT, 3), // Yes, the same error code number as "QUOTA_EXCEEDED"
    /**
     * Quota exceeded for guest users. Quota used: %1$s. Quota limit: %2$s.
     */
    QUOTA_EXCEEDED_INVITE_GUESTS("Quota exceeded for guest users. Quota used: %1$s. Quota limit: %2$s.", QuotaExceptionMessages.QUOTA_EXCEEDED_INVITE_GUESTS_MSG, CATEGORY_USER_INPUT, 3), // Yes, the same error code number as "QUOTA_EXCEEDED"
    /**
     * Quota exceeded for snippets. Quota used: %1$s. Quota limit: %2$s.
     */
    QUOTA_EXCEEDED_SNIPPETS("Quota exceeded for snippets. Quota used: %1$s. Quota limit: %2$s.", QuotaExceptionMessages.QUOTA_EXCEEDED_SNIPPETS_MSG, CATEGORY_USER_INPUT, 3), // Yes, the same error code number as "QUOTA_EXCEEDED"
    /**
     * No account %1$s exists for module %2$s.
     */
    UNKNOWN_ACCOUNT("No account $1%s exists for module %2$s.", OXExceptionStrings.MESSAGE, CATEGORY_ERROR, 4),
    ;

    /**
     * The error code prefix for quota module.
     */
    public static final String PREFIX = "QUOTA";

    private final Category category;

    private final int detailNumber;

    private final String message;

    private final String displayMessage;

    private QuotaExceptionCodes(final String message, String displayMessage, final Category category, final int detailNumber) {
        this.message = message;
        this.displayMessage = displayMessage;
        this.detailNumber = detailNumber;
        this.category = category;
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
        return detailNumber;
    }

    @Override
    public String getPrefix() {
        return PREFIX;
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
