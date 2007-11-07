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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.tools.file;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.AbstractOXException.Category;

/**
 * 
 * @author marcus
 *
 */
public class QuotaFileStorageException extends FileStorageException {

    /**
     * Serialization.
     */
    private static final long serialVersionUID = -1459280511961165849L;

    public QuotaFileStorageException(AbstractOXException  x){
    	super(x);
    }
    
    /**
     * @param code
     * @param messageArgs
     */
    public QuotaFileStorageException(Code code, Object... messageArgs) {
        this(code, null, messageArgs);
    }

    /**
     * @param code
     * @param cause
     * @param messageArgs
     */
    public QuotaFileStorageException(Code code, Throwable cause,
        Object... messageArgs) {
        super(Component.FILESTORE, code.category, code.detailNumber,
            code.message, cause);
        setMessageArgs(messageArgs);
    }

    /**
     * Error codes for the quota file storage exception.
     * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
     */
    public enum Code {
        /**
         * Invalid constructor parameter at %1$d with type %2$s.
         */
        INVALID_PARAMETER("Invalid constructor parameter at %1$d with type %2$s.",
            Category.CODE_ERROR, 1),
        SQL_EXCEPTION("An invalid SQL query was sent to the server.", Category.CODE_ERROR, 2),
        TOO_LARGE("The file cannot be added to filestore. File size: %s Quota: %s Used: %s", Category.USER_INPUT, 3),
        UNDERLYING_EXCEPTION("Got a FileStorageException for FileStorage with id %s in context %s. Underlying message is: %s. Check the StackTrace.", Category.INTERNAL_ERROR, 4);


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
         * @param message message.
         * @param category category.
         * @param detailNumber detail number.
         */
        private Code(final String message, final Category category,
            final int detailNumber) {
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
