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

package com.openexchange.groupware.userconfiguration;

import com.openexchange.api2.OXException;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.EnumComponent;
import com.openexchange.groupware.AbstractOXException.Category;

public class UserConfigurationException extends OXException {

	private static final long serialVersionUID = 5579597483110227098L;

	public static enum UserConfigurationCode {

		/**
		 * An SQL error occurred: %1$s
		 */
		SQL_ERROR("An SQL error occurred: %1$s", Category.CODE_ERROR, 1),
		/**
		 * A DBPooling error occurred
		 */
		DBPOOL_ERROR("A DBPooling error occurred", Category.CODE_ERROR, 2),
		/**
		 * Configuration for user %s could not be found in context %d
		 */
		NOT_FOUND("Configuration for user %s could not be found in context %d", Category.CODE_ERROR, 3),
		/**
		 * Missing property %1$s in system.properties.
		 */
		MISSING_SETTING("Missing property %1$s in system.properties.", Category.SETUP_ERROR, 4),
		/**
		 * Class %1$s can not be found.
		 */
		CLASS_NOT_FOUND("Class %1$s can not be found.", Category.SETUP_ERROR, 5),
		/**
		 * Instantiating the class failed.
		 */
		INSTANTIATION_FAILED("Instantiating the class failed.", Category.CODE_ERROR, 6),
		/**
		 * Cache initialization failed. Region: %1$s
		 */
		CACHE_INITIALIZATION_FAILED("Cache initialization failed. Region: %1$s", Category.CODE_ERROR, 7),
		/**
		 * User configuration could not be put into cache: %1$s
		 */
		CACHE_PUT_ERROR("User configuration could not be put into cache: %1$s", Category.CODE_ERROR, 8),
		/**
		 * User configuration cache could not be cleared: %1$s
		 */
		CACHE_CLEAR_ERROR("User configuration cache could not be cleared: %1$s", Category.CODE_ERROR, 9),
		/**
		 * User configuration could not be removed from cache: %1$s
		 */
		CACHE_REMOVE_ERROR("User configuration could not be removed from cache: %1$s", Category.CODE_ERROR, 9),
		/**
		 * Mail settings for user %s could not be found in context %d
		 */
		MAIL_SETTING_NOT_FOUND("Mail settings for user %s could not be found in context %d", Category.CODE_ERROR, 10);

		private final String message;

		private final int detailNumber;

		private final Category category;

		private UserConfigurationCode(final String message, final Category category, final int detailNumber) {
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

	public UserConfigurationException(final AbstractOXException exc) {
		super(exc);
	}

	public UserConfigurationException(final UserConfigurationCode code, final Throwable cause,
			final Object... messageArgs) {
		super(EnumComponent.USER_SETTING, code.category, code.detailNumber, code.message, cause);
		super.setMessageArgs(messageArgs);
	}

	public UserConfigurationException(final UserConfigurationCode code, final Object... messageArgs) {
		super(EnumComponent.USER_SETTING, code.category, code.detailNumber, code.message, null);
		super.setMessageArgs(messageArgs);
	}

}
