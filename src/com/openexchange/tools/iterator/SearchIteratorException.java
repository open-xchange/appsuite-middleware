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



package com.openexchange.tools.iterator;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Component;

/**
 *   SearchIteratorException
 * TODO Error codes
 * @author <a href="mailto:sebastian.kauss@netline-is.de">Sebastian Kauss</a>
 */
public class SearchIteratorException extends AbstractOXException {

	private static final long serialVersionUID = -4303608920163984898L;

	public static enum SearchIteratorCode {
		
		/**
		 * An SQL error occurred: %s
		 */
		SQL_ERROR("An SQL error occurred: %s", Category.CODE_ERROR, 1),
		/**
		 * A DBPool error occurred: %s
		 */
		DBPOOLING_ERROR("A DBPool error occurred: %s", Category.CODE_ERROR, 2),
		/**
		 * Operation not allowed on a closed SearchIterator
		 */
		CLOSED("Operation not allowed on a closed SearchIterator", Category.CODE_ERROR, 3),
        /**
         * Not implemented
         */
        NOT_IMPLEMENTED("Mapping for %d not implemented", Category.CODE_ERROR, 4),
        
        /**
         * FreeBusyResults Calc issue
         */
        CALCULATION_ERROR("FreeBusyResults calculation problem with oid: %d", Category.CODE_ERROR, 5),
        /**
         * Invalid constructor argument. Instance of %s not supported
         */
        INVALID_CONSTRUCTOR_ARG("Invalid constructor argument. Instance of %s not supported", Category.CODE_ERROR, 6);
                
		
		private final String message;
		
		private final int detailNumber;
		
		private final Category category;
		
		private SearchIteratorCode(final String message, final Category category, final int detailNumber) {
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
	
	/**
	 * @deprecated
	 */
	public SearchIteratorException() {
		super();
	}
	
	/**
	 * @deprecated
	 */
	public SearchIteratorException(String message) {
		super(message);
	}
	
	/**
	 * @deprecated
	 */
	public SearchIteratorException(String message, Exception exc) {
		super(message, exc);
	}
	
	/**
	 * @deprecated
	 */
	public SearchIteratorException(Exception exc) {
		super(exc);
	}
	
	public SearchIteratorException(final SearchIteratorCode code, final Component component) {
		this(code, component, new Object[0]);
	}
	
	public SearchIteratorException(final SearchIteratorCode code, final Component component, final Object... messageArgs) {
		this(code, null, component, messageArgs);
	}
	
	public SearchIteratorException(final SearchIteratorCode code, final Throwable cause, final Component component, final Object... messageArgs) {
		super(component, code.category, code.detailNumber, code.message, cause);
		super.setMessageArgs(messageArgs);
	}

    /**
     * Initializes a new exception using the information provides by the cause.
     * @param cause the cause of the exception.
     */
    public SearchIteratorException(final AbstractOXException cause) {
        super(cause);
    }

}
