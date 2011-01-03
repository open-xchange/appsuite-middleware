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

package com.openexchange.tools.file.external;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.EnumComponent;

/**
 * Exceptions of the QuotaFileStorage.
 * 
 * @author Steffen Templin
 */
public class QuotaFileStorageException extends FileStorageException {

    /**
     * For serialization.
     */
    private static final long serialVersionUID = 5832529635500224019L;

    public QuotaFileStorageException(final AbstractOXException x) {
        super(x);
    }

    /**
     * Initializes a new exception using the information provides by the code.
     * 
     * @param code code for the exception.
     * @param messageArgs arguments that will be formatted into the message.
     */
    public QuotaFileStorageException(final Code code, final Object... messageArgs) {
        this(code, null, messageArgs);
    }

    /**
     * Initializes a new exception using the information provides by the code.
     * 
     * @param code code for the exception.
     * @param cause the cause of the exception.
     * @param messageArgs arguments that will be formatted into the message.
     */
    public QuotaFileStorageException(final Code code, final Throwable cause, final Object... messageArgs) {
        super(EnumComponent.FILESTORE, code.getCategory(), code.getDetailNumber(), null == code.getMessage() ? cause.getMessage() : code.getMessage(), cause);
        setMessageArgs(messageArgs);
    }

    /**
     * Constructor with all parameters for inheritance.
     * 
     * @param component Component.
     * @param category Category.
     * @param number detail number.
     * @param message message of the exception.
     * @param cause the cause.
     */
    protected QuotaFileStorageException(final EnumComponent component, final Category category, final int detailNumber, final String message, final Throwable cause) {
        super(component, category, detailNumber, message, cause);
    }

    /**
     * Error codes for the file storage exception.
     * 
     * @author Steffen Templin
     */
    public enum Code {
        /** Couldn't reach the filestore */
        INSTANTIATIONERROR("Couldn't reach the filestore", Category.SUBSYSTEM_OR_SERVICE_DOWN, 21),
        /** Database Query could not be realized */
        SQLSTATEMENTERROR("Database Query could not be realized", Category.CODE_ERROR, 23),
        /** The allowed Quota is reached. */
        STORE_FULL("The allowed Quota is reached.", Category.USER_INPUT, 24),
        /** Quota seems to be inconsistent. Please use consistency tool on context %1$d. */
        QUOTA_UNDERRUN("Quota seems to be inconsistent. Please use consistency tool on context %1$d.", Category.TRUNCATED, 25),
        /** Quota usage is missing for context %1$d. */
        NO_USAGE("Quota usage is missing for context %1$d.", Category.CODE_ERROR, 26),
        /** Update of quota usage for context %1$d failed. */
        UPDATE_FAILED("Update of quota usage for context %1$d failed.", Category.CODE_ERROR, 27);

        /**
         * Message of the exception.
         */
        private final String message;

        /**
         * Category of the exception.
         */
        private final Category category;

        /**
         * Detail number of the exception.
         */
        private final int detailNumber;

        /**
         * Default constructor.
         * 
         * @param message message.
         * @param category category.
         * @param detailNumber detail number.
         */
        private Code(final String message, final Category category, final int detailNumber) {
            this.message = message;
            this.category = category;
            this.detailNumber = detailNumber;
        }

        public Category getCategory() {
            return category;
        }

        public int getDetailNumber() {
            return detailNumber;
        }

        public String getMessage() {
            return message;
        }
    }
}
