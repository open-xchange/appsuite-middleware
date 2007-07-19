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

package com.openexchange.tools.servlet;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Component;

/**
 * OXServletException
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class OXServletException extends AbstractOXException {

	/**
	 * serialVersionUID
	 */
	private static final long serialVersionUID = 3931776129684819019L;

	/**
	 * Code
	 *
	 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
	 *
	 */
	public static enum Code {

		/**
		 * Missing property %s in 'system.properties'
		 */
		MISSING_SERVLET_DIR("Missing property %s in 'system.properties'", Category.SETUP_ERROR, 1),
		/**
		 * Servlet mapping directory does not exist: %s
		 */
		DIR_NOT_EXISTS("Servlet mapping directory does not exist: %s", Category.SETUP_ERROR, 2),
		/**
		 * File is not a directory: %s
		 */
		NO_DIRECTORY("File is not a directory: %s", Category.SETUP_ERROR, 3),
		/**
		 * Servlet mappings could not be loaded due to following error: %s
		 */
		SERVLET_MAPPINGS_NOT_LOADED("Servlet mappings could not be loaded due to following error: %s", Category.CODE_ERROR, 4);

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
		 * 
		 * @param message
		 *            message.
		 * @param category
		 *            category.
		 * @param detailNumber
		 *            detail number.
		 */
		private Code(final String message, final Category category, final int detailNumber) {
			this.message = message;
			this.category = category;
			this.number = detailNumber;
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
	
	public OXServletException(final AbstractOXException cause) {
		super(cause);
	}
	
	public OXServletException(final Code code, final Object... messageArgs) {
		this(code, null, messageArgs);
	}

	public OXServletException(final Code code, final Throwable cause, final Object... messageArgs) {
		super(Component.SERVLET, code.category, code.number, code.message, cause);
		setMessageArgs(messageArgs);
	}

}
