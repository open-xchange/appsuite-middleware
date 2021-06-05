/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.exception;

import java.util.List;
import java.util.Set;
import com.google.common.collect.ImmutableSet;
import com.openexchange.exception.interception.OXExceptionArguments;
import com.openexchange.exception.interception.OXExceptionInterceptor;
import com.openexchange.exception.interception.internal.OXExceptionInterceptorRegistration;
import com.openexchange.log.LogProperties;

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
     * @throws IllegalArgumentException If given code is <code>null</code>
     */
    public boolean equals(final OXExceptionCode code, final OXException e) {
        if (null == code) {
            throw new IllegalArgumentException("Code must not be null");
        }
        if (null == e) {
            return false;
        }
        return recEquals(code.getPrefix(), code.getNumber(), e);
    }

    private static boolean recEquals(final String prefix, final int code, final OXException e) {
        if (e.getCode() == code && prefix.equals(e.getPrefix())) {
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
    public OXException create(OXExceptionCode code) {
        return create(code, new Object[0]);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with specified code's attributes.
     *
     * @param code The exception code
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(OXExceptionCode code, Object... args) {
        return create(code, (Throwable) null, args);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with specified code's attributes.
     *
     * @param code The exception code
     * @param cause The optional initial cause
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(OXExceptionCode code, Throwable cause, Object... args) {
        return create(code, null, cause, args);
    }

    /**
     * The set containing category types appropriate for being displayed.
     */
    public static final Set<Category.EnumType> DISPLAYABLE = ImmutableSet.of(
        Category.EnumType.CAPACITY,
        Category.EnumType.CONFLICT,
        Category.EnumType.CONNECTIVITY,
        Category.EnumType.PERMISSION_DENIED,
        Category.EnumType.SERVICE_DOWN,
        Category.EnumType.TRUNCATED,
        Category.EnumType.TRY_AGAIN,
        Category.EnumType.USER_INPUT,
        Category.EnumType.WARNING);

    /**
     * Creates a new {@link OXException} instance pre-filled with specified code's attributes.
     *
     * @param code The exception code
     * @param category The optional category to use
     * @param cause The optional initial cause
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(OXExceptionCode code, Category category, Throwable cause, Object... args) {
        return create(code, category, cause, true, args);
    }

    private OXException create(OXExceptionCode code, Category category, Throwable cause, boolean intercept, Object... args) {
        // Determine category
        Category cat = null == category ? code.getCategory() : category;

        // Initialize OXException instance
        OXException ret;
        if (DisplayableOXExceptionCode.class.isInstance(code)) {
            ret = new OXException(code.getNumber(), ((DisplayableOXExceptionCode) code).getDisplayMessage(), cause, args).setLogMessage(code.getMessage(), args);
        } else {
            if (cat.getLogLevel().implies(LogLevel.DEBUG)) {
                ret = new OXException(code.getNumber(), code.getMessage(), cause, args);
            } else {
                if (DISPLAYABLE.contains(cat.getType())) {
                    // Displayed message is equal to logged one
                    ret = new OXException(code.getNumber(), code.getMessage(), cause, args).setLogMessage(code.getMessage(), args);
                } else {
                    final String displayMessage = Category.EnumType.TRY_AGAIN.equals(cat.getType()) ? OXExceptionStrings.MESSAGE_RETRY : OXExceptionStrings.MESSAGE;
                    ret = new OXException(code.getNumber(), displayMessage, cause, new Object[0]).setLogMessage(code.getMessage(), args);
                }
            }
        }

        // Apply rest
        ret.addCategory(cat).setPrefix(code.getPrefix()).setExceptionCode(code);

        // Check for interception
        if (intercept) {
            String module = LogProperties.getLogProperty(LogProperties.Name.AJAX_MODULE);
            String action = LogProperties.getLogProperty(LogProperties.Name.AJAX_ACTION);

            List<OXExceptionInterceptor> interceptors = OXExceptionInterceptorRegistration.getInstance().getResponsibleInterceptors(module, action);
            for (OXExceptionInterceptor interceptor : interceptors) {
                OXExceptionArguments newArgs = interceptor.intercept(ret);
                if (null != newArgs) {
                    ret = create(newArgs.getCode(), newArgs.getCategory(), newArgs.getCause(), false, newArgs.getArgs());
                }
            }
        }

        return ret;
    }

}
