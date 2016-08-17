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

package com.openexchange.exception;

import static com.openexchange.exception.OXExceptionConstants.CATEGORY_CONFLICT;
import static com.openexchange.exception.OXExceptionConstants.CATEGORY_ERROR;
import static com.openexchange.exception.OXExceptionConstants.CATEGORY_PERMISSION_DENIED;
import static com.openexchange.exception.OXExceptionConstants.CATEGORY_USER_INPUT;
import static com.openexchange.exception.OXExceptionConstants.CODE_DEFAULT;
import static com.openexchange.exception.OXExceptionConstants.PREFIX_GENERAL;
import com.openexchange.exception.OXException.Generic;

/**
 * {@link OXExceptions} - Utility class for {@link OXException}.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class OXExceptions {

    /**
     * Initializes a new {@link OXExceptions}.
     */
    public OXExceptions() {
        super();
    }

    /**
     * Checks if specified <code>OXException</code>'s (first) category matches {@link OXExceptionConstants#CATEGORY_USER_INPUT USER_INPUT category}.
     *
     * @param e The <code>OXException</code> instance to check
     * @return <code>true</code> if category matches; otherwise <code>false</code>
     */
    public static boolean isUserInput(OXException e) {
        return isCategory(CATEGORY_USER_INPUT, e);
    }

    /**
     * Checks if specified <code>OXException</code>'s (first) category matches {@link OXExceptionConstants#CATEGORY_PERMISSION_DENIED PERMISSION_DENIED category}.
     *
     * @param e The <code>OXException</code> instance to check
     * @return <code>true</code> if category matches; otherwise <code>false</code>
     */
    public static boolean isPermissionDenied(OXException e) {
        return isCategory(CATEGORY_PERMISSION_DENIED, e);
    }

    /**
     * Checks if specified <code>OXException</code>'s (first) category matches given category.
     *
     * @param category The category
     * @param e The <code>OXException</code> instance to check
     * @return <code>true</code> if category matches; otherwise <code>false</code>
     */
    public static boolean isCategory(Category category, OXException e) {
        if (null == category || null == e) {
            return false;
        }

        Category cat = e.getCategory();
        return null != cat && category.getType().equals(cat.getType());
    }

    // ---------------------------------------------------------------------------------------------------------------------------------

    /**
     * Checks if given {@link OXException} instance contains either a socket or an I/O error in its cause chain.
     *
     * @param e The <tt>OXException</tt> instance to check
     * @return <code>true</code> if <tt>OXException</tt> instance contains either a socket or an I/O error in its cause chain; otherwise
     *         <code>false</code>
     */
    public static boolean containsCommunicationError(OXException e) {
        if (null == e) {
            return false;
        }
        return containsCommunicationError0(e.getCause());
    }

    private static boolean containsCommunicationError0(Throwable t) {
        if (null == t) {
            return false;
        }
        if ((t instanceof java.io.IOError) || (t instanceof java.io.IOException) || (t instanceof java.net.SocketException)) {
            // Whatever... Timeout, bind error, no route to host, connect error, connection reset, ...
            return true;
        }
        return containsCommunicationError0(t.getCause());
    }

    // -----------------------------------------------------------------------------------------------------------------------------------

    /**
     * Creates a general exception.
     *
     * @param logMessage The log message
     * @return A general exception.
     */
    public static OXException general(final String logMessage) {
        return new OXException(CODE_DEFAULT, OXExceptionStrings.MESSAGE).setLogMessage(logMessage).setCategory(CATEGORY_ERROR).setPrefix(PREFIX_GENERAL);
    }

    /**
     * Creates a general exception.
     *
     * @param logMessage The log message
     * @param cause The cause
     * @return A general exception.
     */
    public static OXException general(final String logMessage, final Throwable cause) {
        return new OXException(CODE_DEFAULT, OXExceptionStrings.MESSAGE, cause).setLogMessage(logMessage).setCategory(CATEGORY_ERROR).setPrefix(PREFIX_GENERAL);
    }

    /**
     * Creates a not-found exception.
     *
     * @param id The identifier of the missing object
     * @return A not-found exception.
     */
    public static OXException notFound(final String id) {
        return new OXException(1, OXExceptionStrings.MESSAGE_NOT_FOUND, id).setCategory(CATEGORY_USER_INPUT).setPrefix(PREFIX_GENERAL).setGeneric(Generic.NOT_FOUND);
    }

    /**
     * Creates a module-denied exception.
     *
     * @param module The identifier of the module
     * @return A module-denied exception.
     */
    public static OXException noPermissionForModule(final String module) {
        return new OXException(1, OXExceptionStrings.MESSAGE_PERMISSION_MODULE, module).setCategory(CATEGORY_USER_INPUT).setPrefix(PREFIX_GENERAL).setGeneric(Generic.NO_PERMISSION);
    }

    /**
     * Creates a folder-denied exception.
     *
     * @return A folder-denied exception.
     */
    public static OXException noPermissionForFolder() {
        return new OXException(1, OXExceptionStrings.MESSAGE_PERMISSION_FOLDER).setCategory(CATEGORY_PERMISSION_DENIED).setPrefix(PREFIX_GENERAL).setGeneric(Generic.NO_PERMISSION);
    }

    /**
     * Creates a missing-field exception.
     *
     * @param name The field name
     * @return A missing-field exception.
     */
    public static OXException mandatoryField(final String name) {
        return new OXException(CODE_DEFAULT, OXExceptionStrings.MESSAGE_MISSING_FIELD, name).setCategory(CATEGORY_ERROR).setPrefix(PREFIX_GENERAL).setGeneric(
            Generic.MANDATORY_FIELD);
    }

    /**
     * Creates a missing-field exception.
     *
     * @param code The code number
     * @param name The field name
     * @return A missing-field exception.
     */
    public static OXException mandatoryField(final int code, final String name) {
        return new OXException(code, OXExceptionStrings.MESSAGE_MISSING_FIELD, name).setCategory(CATEGORY_ERROR).setPrefix(PREFIX_GENERAL).setGeneric(Generic.MANDATORY_FIELD);
    }

    /**
     * Creates a general conflict exception.
     *
     * @return A general conflict exception.
     */
    public static OXException conflict() {
        return new OXException(1, OXExceptionStrings.MESSAGE_CONFLICT).setCategory(CATEGORY_CONFLICT).setPrefix(PREFIX_GENERAL).setGeneric(Generic.CONFLICT);
    }

    // -----------------------------------------------------------------------------------------------------------------------------------

}
