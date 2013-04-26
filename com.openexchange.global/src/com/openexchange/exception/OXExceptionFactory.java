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

package com.openexchange.exception;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * {@link OXExceptionFactory} - A factory for {@link OXException} instances.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class OXExceptionFactory {

    private static final OXExceptionFactory INSTANCE = new OXExceptionFactory();

    /**
     * Gets the factory instance.
     *
     * @return The factory instance.
     */
    public static OXExceptionFactory getInstance() {
        return INSTANCE;
    }

    /**
     * Initializes a new {@link OXExceptionFactory}.
     */
    private OXExceptionFactory() {
        super();
    }

    /**
     * Checks if attributes of specified {@link OXException} instance or the ones from any of its causes (recursive check) matches given
     * code's ones.
     *
     * @param code The code to check against
     * @param e The exception to check
     * @return <code>true</code> if specified {@link OXException}'s attributes matches given code's ones; otherwise <code>false</code>
     */
    public boolean equals(final OXExceptionCode code, final OXException e) {
        return recEquals(code.getPrefix(), code.getNumber(), e);
    }

    private static boolean recEquals(final String prefix, final int code, final OXException e) {
        if (prefix.equals(e.getPrefix()) && e.getCode() == code) {
            return true;
        }
        final Throwable cause = e.getCause();
        if (!(cause instanceof OXException)) {
            return false;
        }
        return recEquals(prefix, code, (OXException) cause);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with specified code's attributes.
     *
     * @param code The exception code
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final OXExceptionCode code) {
        return create(code, new Object[0]);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with specified code's attributes.
     *
     * @param code The exception code
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final OXExceptionCode code, final Object... args) {
        return create(code, (Throwable) null, args);
    }

    /**
     * The set containing category types appropriate for being displayed.
     */
    public static final Set<Category.EnumType> DISPLAYABLE = Collections.unmodifiableSet(EnumSet.of(
        Category.EnumType.CAPACITY,
        Category.EnumType.CONFLICT,
        Category.EnumType.CONNECTIVITY,
        Category.EnumType.PERMISSION_DENIED,
        Category.EnumType.SERVICE_DOWN,
        Category.EnumType.TRUNCATED,
        Category.EnumType.TRY_AGAIN,
        Category.EnumType.USER_INPUT,
        Category.EnumType.WARNING));

    /**
     * Creates a new {@link OXException} instance pre-filled with specified code's attributes.
     *
     * @param code The exception code
     * @param cause The optional initial cause
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final OXExceptionCode code, final Throwable cause, final Object... args) {
        final Category category = code.getCategory();
        final OXException ret;
        if (category.getLogLevel().implies(LogLevel.DEBUG)) {
            ret = new OXException(code.getNumber(), code.getMessage(), cause, args);
        } else {
            if (DISPLAYABLE.contains(category.getType())) {
                // Displayed message is equal to logged one
                final String message = code.getMessage();
                ret = new OXException(code.getNumber(), message, cause, args).setLogMessage(message, args);
            } else {
                ret = new OXException(
                    code.getNumber(),
                    Category.EnumType.TRY_AGAIN.equals(category.getType()) ? OXExceptionStrings.MESSAGE_RETRY : OXExceptionStrings.MESSAGE,
                    cause,
                    new Object[0]).setLogMessage(code.getMessage(), args);
            }
        }
        if (code instanceof LogLevelAwareOXExceptionCode) {
            ret.setLogLevel(((LogLevelAwareOXExceptionCode) code).getLogLevel());
        }
        return ret.addCategory(category).setPrefix(code.getPrefix());
    }

}
