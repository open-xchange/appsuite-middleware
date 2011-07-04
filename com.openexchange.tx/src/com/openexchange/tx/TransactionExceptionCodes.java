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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.tx;

import static com.openexchange.tx.TransactionExceptionMessages.CANNOT_COMMIT_MSG;
import static com.openexchange.tx.TransactionExceptionMessages.CANNOT_FINISH_MSG;
import static com.openexchange.tx.TransactionExceptionMessages.CANNOT_ROLLBACK_MSG;
import static com.openexchange.tx.TransactionExceptionMessages.NO_COMPLETE_ROLLBACK_MSG;
import com.openexchange.exception.Category;
import com.openexchange.exception.LogLevel;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionStrings;

/**
 * The error code enumeration for transaction errors.
 */
public enum TransactionExceptionCodes implements OXExceptionCode {

    /** This transaction could not be fully undone. Some components are probably not consistent anymore. Run the recovery tool! */
    NO_COMPLETE_ROLLBACK(NO_COMPLETE_ROLLBACK_MSG, Category.CATEGORY_ERROR, 1),
    /** Cannot commit transaction to write DB */
    CANNOT_COMMIT(CANNOT_COMMIT_MSG, Category.CATEGORY_SERVICE_DOWN, 400),
    /** Cannot rollback transaction in write DB */
    CANNOT_ROLLBACK(CANNOT_ROLLBACK_MSG, Category.CATEGORY_SERVICE_DOWN, 401),
    /** Cannot finish transaction */
    CANNOT_FINISH(CANNOT_FINISH_MSG, Category.CATEGORY_SERVICE_DOWN, 402);

    private final String message;

    private final Category category;

    private final int number;

    private final boolean display;

    private TransactionExceptionCodes(final String message, final Category category, final int number) {
        this.message = message;
        this.category = category;
        this.number = number;
        display = category.getLogLevel().implies(LogLevel.DEBUG);
    }

    public int getNumber() {
        return number;
    }

    public String getPrefix() {
        return "TX";
    }

    public String getMessage() {
        return message;
    }

    public String getHelp() {
        return null;
    }

    public Category getCategory() {
        return category;
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     * 
     * @return The newly created {@link OXException} instance
     */
    public OXException create() {
        return create(new Object[0]);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     * 
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final Object... args) {
        return create((Throwable) null, args);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     * 
     * @param cause The optional initial cause
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final Throwable cause, final Object... args) {
        final OXException ret;
        if (display) {
            ret = new OXException(number, message, cause, args);
        } else {
            ret =
                new OXException(
                    number,
                    Category.EnumType.TRY_AGAIN.equals(category.getType()) ? OXExceptionStrings.MESSAGE_RETRY : OXExceptionStrings.MESSAGE,
                    new Object[0]);
        }
        return ret.addCategory(category).setPrefix(getPrefix());
    }

}
