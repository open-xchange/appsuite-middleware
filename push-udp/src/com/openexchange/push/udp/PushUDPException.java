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

package com.openexchange.push.udp;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.EnumComponent;

/**
 * @author <a href="mailto:sebastian.kauss@open-xchange.org">Sebastian Kauss</a>
 */
public class PushUDPException extends AbstractOXException {

    /**
     * For Serialization.
     */
    private static final long serialVersionUID = 6320550676305333711L;

    /**
     * Initializes a new exception using the information provided by the cause.
     * 
     * @param cause the cause of the exception.
     */
    public PushUDPException(final AbstractOXException cause) {
        super(cause);
    }

    /**
     * Constructor with all parameters.
     * 
     * @param code code
     */
    public PushUDPException(final Code code) {
        super(EnumComponent.PUSHUDP, code.getCategory(), code.getDetailNumber(), code.getMessage(), null);
    }

    /**
     * Constructor with all parameters.
     * 
     * @param code code
     * @param cause the cause.
     * @param msgArgs arguments for the exception message.
     */
    public PushUDPException(final Category category, final String message, final int detailNumber, final Throwable cause) {
        super(EnumComponent.PUSHUDP, category, detailNumber, message, cause);
    }

    /**
     * Constructor with all parameters.
     * 
     * @param code code
     * @param cause the cause.
     * @param msgArgs arguments for the exception message.
     */
    public PushUDPException(final Code code, final Throwable cause, final Object... msgArgs) {
        super(EnumComponent.SESSION, code.getCategory(), code.getDetailNumber(), code.getMessage(), cause);
        setMessageArgs(msgArgs);
    }

    public PushUDPException(final EnumComponent component, final Category category, final int number, final String message, final Throwable cause, final Object[] msgArgs) {
        super(component, category, number, message, cause);
        setMessageArgs(msgArgs);
    }

    public enum Code {
        /**
         * Push UDP Exception.
         */
        PUSH_UDP_EXCEPTION("Push UDP Exception.", 1, AbstractOXException.Category.CODE_ERROR),
        /**
         * Missing Push UDP configuration.
         */
        MISSING_CONFIG("Missing Push UDP configuration.", 2, AbstractOXException.Category.SETUP_ERROR),
        /**
         * User ID is not a number: %1$s.
         */
        USER_ID_NAN("User ID is not a number: %1$s.", 3, AbstractOXException.Category.CODE_ERROR),
        /**
         * Context ID is not a number: %1$s.
         */
        CONTEXT_ID_NAN("Context ID is not a number: %1$s.", 4, AbstractOXException.Category.CODE_ERROR),
        /**
         * Magic bytes are not a number: %1$s.
         */
        MAGIC_NAN("Magic bytes are not a number: %1$s.", 5, AbstractOXException.Category.CODE_ERROR),
        /**
         * Invalid Magic bytes: %1$s.
         */
        INVALID_MAGIC("Invalid Magic bytes: %1$s.", 6, AbstractOXException.Category.CODE_ERROR),
        /**
         * Folder ID is not a number: %1$s.
         */
        FOLDER_ID_NAN("Folder ID is not a number: %1$s.", 7, AbstractOXException.Category.CODE_ERROR),
        /**
         * Module is not a number: %1$s.
         */
        MODULE_NAN("Module is not a number: %1$s.", 8, AbstractOXException.Category.CODE_ERROR),
        /**
         * Port is not a number: %1$s.
         */
        PORT_NAN("Port is not a number: %1$s.", 9, AbstractOXException.Category.CODE_ERROR),
        /**
         * Request type is not a number: %1$s.
         */
        TYPE_NAN("Request type is not a number: %1$s.", 10, AbstractOXException.Category.CODE_ERROR),
        /**
         * Length is not a number: %1$s.
         */
        LENGTH_NAN("Length is not a number: %1$s.", 11, AbstractOXException.Category.CODE_ERROR),
        /**
         * Invalid user IDs: %1$s.
         */
        INVALID_USER_IDS("Invalid user IDs: %1$s.", 12, AbstractOXException.Category.CODE_ERROR),
        /**
         * Unknown request type: %1$s.
         */
        INVALID_TYPE("Unknown request type: %1$s.", 13, AbstractOXException.Category.CODE_ERROR),
        /**
         * Missing payload in datagram package.
         */
        MISSING_PAYLOAD("Missing payload in datagram package.", 14, AbstractOXException.Category.CODE_ERROR);

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
        private Code(final String message, final int detailNumber, final Category category) {
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
