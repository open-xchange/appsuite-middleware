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

package com.openexchange.api2;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.EnumComponent;

/**
 * OXException.
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */
public class OXException extends AbstractOXException {

	/**
     * For serialization.
	 */
    private static final long serialVersionUID = -7503880425356541331L;

	/**
     * @deprecated use constructor with component, category and detailNumber.
	 */
    public OXException() {
		super();
	}

    /**
     * @deprecated use constructor with component, category and detailNumber.
     */
	public OXException(final String message) {
		super(message);
	}

	/**
     * Used by OXConcurrentModificationException.
     * @param component Component.
     * @param message Message.
	 */
    protected OXException(final EnumComponent component, final String message) {
		super(component, message);
	}

    /**
     * @deprecated use constructor with component, category and detailNumber.
     */
	public OXException(final String message, final Throwable cause) {
		super(message, cause);
	}

    /**
     * @deprecated use constructor with component, category and detailNumber.
     */
	public OXException(final EnumComponent component, final String message,
        final Throwable cause) {
		super(component, message, cause);
	}

    /**
     * @deprecated use constructor with component, category and detailNumber.
     */
	public OXException(final Throwable cause) {
		super(cause);
	}

    /**
     * @deprecated use constructor with component, category and detailNumber.
     */
	public OXException(final EnumComponent component, final Exception exc) {
		super(component, exc);
	}

    /**
     * {@inheritDoc}
	 */
    public OXException(final AbstractOXException exc) {
        super(exc);
    }

    /**
     * Constructor with all parameters.
     * @param component Component.
     * @param category Category.
     * @param number detail number.
     * @param message message of the exception.
     * @param cause the cause.
     * @param messageArgs arguments for the exception message.
     */
    public OXException(final EnumComponent component, final Category category,
        final int number, final String message, final Throwable cause, final Object... messageArgs) {
        super(component, category, number, message, cause);
        super.setMessageArgs(messageArgs);
    }
}
