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
package com.openexchange.axis2.exeptions;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.EnumComponent;

public class OXAxis2Exception extends AbstractOXException {

    /**
     * For serialization
     */
    private static final long serialVersionUID = -4143376197058972709L;

    public static enum Code {
        PROBLEM("%s", Category.CODE_ERROR, 1),
        /**
         * Property error: %s
         */
        PROPERTY_ERROR("Property error: %s", Category.SETUP_ERROR, 2),
        /**
         * Servlet registration failed
         */
        SERVLET_REGISTRATION_FAILED("axis2 servlet cannot be registered: %s", Category.CODE_ERROR, 3)
        ;

        private final String message;

        private final int detailNumber;

        private final Category category;

        private Code(final String message, final Category category, final int detailNumber) {
            this.message = message;
            this.detailNumber = detailNumber;
            this.category = category;
        }

        public Category getCategory() {
            return category;
        }

        public int getNumber() {
            return detailNumber;
        }

        public String getMessage() {
            return message;
        }

    }

    public OXAxis2Exception(final AbstractOXException cause) {
        super(cause);
    }

    public OXAxis2Exception(final Code code, final Object... messageArgs) {
        this(code, null, messageArgs);
    }

    public OXAxis2Exception(final Code code, final Throwable cause, final Object... messageArgs) {
        super(EnumComponent.AXIS2, code.getCategory(), code.getNumber(), code.getMessage(), cause);
        super.setMessageArgs(messageArgs);
    }

    public OXAxis2Exception(final Component component, final Category category, final int detailNumber, final String message, final Throwable cause) {
        super(component, category, detailNumber, message, cause);
    }

    public OXAxis2Exception(final Component component, final String message, final AbstractOXException cause) {
        super(component, message, cause);
    }

    public OXAxis2Exception(final Component component, final Code code, final Throwable cause, final Object... messageArgs) {
        super(component, code.getCategory(), code.getNumber(), code.getMessage(), cause);
        setMessageArgs(messageArgs);
    }
}
