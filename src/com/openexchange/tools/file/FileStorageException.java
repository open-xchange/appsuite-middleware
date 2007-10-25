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

/**
 * Exceptions of the FileStorage.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class FileStorageException extends AbstractOXException {

    /**
     * For serialization.
     */
    private static final long serialVersionUID = 7098813337802054897L;

    public FileStorageException(AbstractOXException x) {
    	super(x);
    }
    
    /**
     * Initializes a new exception using the information provides by the code.
     * @param code code for the exception.
     * @param messageArgs arguments that will be formatted into the message.
     */
    public FileStorageException(final Code code, final Object... messageArgs) {
        this(code, null, messageArgs);
    }

    /**
     * Initializes a new exception using the information provides by the code.
     * @param code code for the exception.
     * @param cause the cause of the exception.
     * @param messageArgs arguments that will be formatted into the message.
     */
    public FileStorageException(final Code code, final Throwable cause,
        final Object... messageArgs) {
        super(Component.FILESTORE, code.category, code.detailNumber,
            null == code.message ? cause.getMessage() : code.message,
            cause);
        setMessageArgs(messageArgs);
    }

    /**
     * Constructor with all parameters for inheritance.
     * @param component Component.
     * @param category Category.
     * @param number detail number.
     * @param message message of the exception.
     * @param cause the cause.
     */
    protected FileStorageException(final Component component,
        final Category category, final int detailNumber, final String message,
        final Throwable cause) {
        super(component, category, detailNumber, message, cause);
    }

    /**
     * Error codes for the file storage exception.
     * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
     */
    public enum Code {
        /**
         * A property is missing.
         */
        PROPERTY_MISSING("Cannot find property %s.", Category.SETUP_ERROR, 1),
        /**
         * Class can not be found.
         */
        CLASS_NOT_FOUND("Class %s can not be loaded.", Category.SETUP_ERROR, 2),
        /**
         * An IO error occurred: %s
         */
        IOERROR("An IO error occurred: %s", Category.SUBSYSTEM_OR_SERVICE_DOWN,
            3),
        /**
         * May be used to turn the IOException of getInstance into a proper
         * AbstractOXException
         */
        INSTANTIATIONERROR("Couldn't reach the filestore: %s",
            Category.SUBSYSTEM_OR_SERVICE_DOWN, 4),
        /**
         * Invalid constructor parameter at %1$d with type %2$s.
         */
        INVALID_PARAMETER("Invalid constructor parameter at %1$d with type %2$s.",
            Category.CODE_ERROR, 5),
        /**
         * Cannot create directory \"%1$s\" for FileStorage.
         */
        CREATE_DIR_FAILED("Cannot create directory \"%1$s\" for FileStorage.",
            Category.SETUP_ERROR, 6),
        /**
         * 'Depth' must be >= 1 but is %1$d.
         */
        INVALID_DEPTH("'Depth' must be >= 1 but is %1$d.",
            Category.CODE_ERROR, 7),
        /**
         * Entries must be >= 1 but is %1$d.
         */
        INVALID_ENTRIES("'Entries' must be >= 1 but is %1$d.",
            Category.CODE_ERROR, 8),
        /**
         * Unsupported encoding.
         */
        ENCODING("Unsupported encoding.", Category.CODE_ERROR, 9),
        /**
         * Number parsing problem.
         */
        NO_NUMBER("Number parsing problem.", Category.CODE_ERROR, 10),
        /**
         * File storage is full.
         */
        STORE_FULL("File storage is full.", Category.EXTERNAL_RESOURCE_FULL, 11),
        /**
         * Depth mismatch while computing next entry.
         */
        DEPTH_MISMATCH("'Depth' mismatch while computing next entry.",
            Category.CODE_ERROR, 12),
        /**
         * Cannot remove lock file.
         */
        UNLOCK("Cannot remove lock file.", Category.SUBSYSTEM_OR_SERVICE_DOWN,
            13),
        /**
         * Cannot create lock file.
         */
        LOCK("Cannot create lock file.", Category.SUBSYSTEM_OR_SERVICE_DOWN, 14),
        /**
         * Cannot create file %1$s.
         */
        CREATE_FAILED("Cannot create file %1$s.",
            Category.SUBSYSTEM_OR_SERVICE_DOWN, 15),
        /**
         * Eliminating the FileStorage failed.
         */
        NOT_ELIMINATED("Eliminating the FileStorage failed.", Category
            .SUBSYSTEM_OR_SERVICE_DOWN, 16);

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
