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

package com.openexchange.tools.servlet;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.EnumComponent;

/**
 * Exception for problems in servlets.
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class OXJSONException extends AbstractOXException {

    private static final long serialVersionUID = 3931776129684819019L;

    public OXJSONException(final AbstractOXException cause) {
        super(cause);
    }

    /**
     * Initializes a new exception using the information provided by the code.
     * 
     * @param code code for the exception.
     * @param messageArgs arguments that will be formatted into the message.
     */
    public OXJSONException(final Code code, final Object... messageArgs) {
        this(code, null, messageArgs);
    }

    /**
     * Initializes a new exception using the information provided by the code.
     * 
     * @param code code for the exception.
     * @param cause the cause of the exception.
     * @param messageArgs arguments that will be formatted into the message.
     */
    public OXJSONException(final Code code, final Throwable cause, final Object... messageArgs) {
        super(EnumComponent.SERVLET, code.getCategory(), code.getNumber(), null == code.getMessage() ? cause.getMessage() : code.getMessage(), cause);
        setMessageArgs(messageArgs);
    }

    /**
     * Error codes for servlet exceptions.
     */
    public static enum Code {
        /** Exception while writing JSON. */
        JSON_WRITE_ERROR("Exception while writing JSON.", Category.CODE_ERROR, 1),
        /** Exception while parsing JSON: "%s". */
        JSON_READ_ERROR("Exception while parsing JSON: \"%s\".", Category.CODE_ERROR, 2),
        /** Invalid cookie. */
        INVALID_COOKIE("Invalid cookie.", Category.TRY_AGAIN, 3),
        /** Exception while building JSON. */
        JSON_BUILD_ERROR("Exception while building JSON.", Category.CODE_ERROR, 4),
        /** Value "%1$s" of attribute %s contains non digit characters. */
        CONTAINS_NON_DIGITS("Value \"%1$s\" of attribute %2$s contains non digit characters.", Category.USER_INPUT, 5),
        /** Too many digits within field %1$s. */
        TOO_BIG_NUMBER("Too many digits within field %1$s.", Category.USER_INPUT, 6),
        /** Unable to parse value "%1$s" within field %2$s as a number. */
        NUMBER_PARSING("Unable to parse value \"%1$s\" within field %2$s as a number.", Category.CODE_ERROR, 7),
        /** Invalid value \"%2$s\" in JSON attribute \"%1$s\". */
        INVALID_VALUE("Invalid value \"%2$s\" in JSON attribute \"%1$s\".", Category.USER_INPUT, 8);

        private final String message;
        private final Category category;
        private final int number;

        private Code(final String message, final Category category, final int detailNumber) {
            this.message = message;
            this.category = category;
            number = detailNumber;
        }

        public Category getCategory() {
            return category;
        }

        public String getMessage() {
            return message;
        }

        public int getNumber() {
            return number;
        }
    }
}
