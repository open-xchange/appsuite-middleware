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

package com.openexchange.data.conversion.ical;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.EnumComponent;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public class ConversionWarning extends AbstractOXException {

    /**
     * For serialization.
     */
    private static final long serialVersionUID = -7593693106963732974L;

    private String message;
    private Object[] args;

    /**
     * @deprecated use {@link #ConversionWarning(Code, Object...)}.
     */
    @Deprecated
    public ConversionWarning(String message, Object...args) {
        this.message = message;
        this.args = args;
    }

    /**
     * @deprecated use {@link #getMessage()}.
     */
    @Deprecated
    public String getFormattedMessage() {
        return String.format(this.message, args);
    }

    public ConversionWarning(final Code code, final Object... args) {
        super(EnumComponent.ICAL, code.getCategory(), code.getNumber(),
            code.getMessage(), null);
        setMessageArgs(args);
    }

    public enum Code {
        /**
         * Unable to convert task status "%1$s".
         */
        INVALID_STATUS("Unable to convert task status \"%1$s\".", Category.USER_INPUT, 1);

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
        private final int number;

        /**
         * Default constructor.
         * @param message message.
         * @param category category.
         * @param number detail number.
         */
        private Code(final String message, final Category category,
            final int number) {
            this.message = message;
            this.category = category;
            this.number = number;
        }

        /**
         * @return the message
         */
        public String getMessage() {
            return message;
        }

        /**
         * @return the category
         */
        public Category getCategory() {
            return category;
        }

        /**
         * @return the number
         */
        public int getNumber() {
            return number;
        }
    }
}
