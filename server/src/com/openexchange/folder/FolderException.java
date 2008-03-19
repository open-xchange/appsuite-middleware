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

package com.openexchange.folder;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.AbstractOXException.Category;

/**
 * {@link FolderException}
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public class FolderException extends AbstractOXException {

	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = 6544192373164316193L;

	public static enum Code {

		/**
		 * No folder could be found for ID %s
		 */
		FOLDER_NOT_FOUND("No folder could be found for ID %s", Category.CODE_ERROR, 1),
		/**
		 * No timestamp support on folder storage
		 */
		NO_TIMESTAMP_SUPPORT("No timestamp support on folder storage", Category.CODE_ERROR, 2),
		/**
		 * Invalid permission value: %d
		 */
		INVALID_PERMISSION("Invalid permission value: %d", Category.CODE_ERROR, 3),
		/**
		 * Missing ID
		 */
		MISSING_ID("Missing ID", Category.CODE_ERROR, 4),
		/**
		 * The folder %s has been changed in the meantime
		 */
		CONCURRENT_MODIFICATION("The folder %s has been changed in the meantime", Category.CONCURRENT_MODIFICATION, 5),
		/**
		 * No access to module %s due to user configuration
		 * <p>
		 * Requested operation was canceled because underlying user
		 * configuration denies folder access due to module restrictions
		 * </p>
		 */
		NO_MODULE_ACCESS("No access to module %s due to user configuration", Category.USER_CONFIGURATION, 6),
		/**
		 * Folder %s not visible
		 * <p>
		 * Either underlying user configuration or folder permission setting
		 * denies visibility of folder in question
		 * </p>
		 */
		NOT_VISIBLE("Folder %s is not visible.", Category.PERMISSION, 7),
		/**
		 * No admin access to folder %s
		 * <p>
		 * No necessary admin access granted for update operation
		 * </p>
		 */
		NO_ADMIN_ACCESS("No admin access to folder %s", Category.PERMISSION, 8),
		/**
		 * Not allowed to delete shared folder %s
		 * <p>
		 * A shared folder must not be deleted
		 * </p>
		 */
		NO_SHARED_FOLDER_DELETION("Not allowed to delete shared folder %s", Category.PERMISSION, 9),
		/**
		 * Not allowed to delete default folder %s
		 * <p>
		 * Default folder(s) must not be deleted
		 * </p>
		 */
		NO_DEFAULT_FOLDER_DELETION("Not allowed to delete default folder %s", Category.PERMISSION, 10),
		/**
		 * Folder %s contains a hidden subfolder which does not grant delete
		 * rights
		 */
		HIDDEN_FOLDER_ON_DELETION("Folder %s contains a hidden subfolder which does not grant delete rights",
				Category.PERMISSION, 11),
		/**
		 * Unknown module: %s
		 */
		UNKNOWN_MODULE("Unknown module: %s", Category.CODE_ERROR, 12),
		/**
		 * A runtime error occurred: %s
		 */
		RUNTIME_ERROR("A runtime error occurred: %s", Category.INTERNAL_ERROR, 13),
		/**
		 * Not allowed to delete all contained objects in folder %s
		 * <p>
		 * User is not allowed to delete all objects contained in folder in
		 * question
		 * </p>
		 */
		NOT_ALL_OBJECTS_DELETION("Not allowed to delete all contained objects in folder %s", Category.PERMISSION, 14);

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

	/**
	 * Empty arguments for {@link #FolderException(Code)} constructor.
	 */
	private static final transient Object[] EMPTY_ARGS = new Object[0];

	/**
	 * Initializes a new {@link FolderException}
	 * 
	 * @param cause
	 *            The cause
	 */
	public FolderException(final AbstractOXException cause) {
		super(cause);
	}

	/**
	 * Initializes a new {@link FolderException}
	 * 
	 * @param code
	 *            The folder error code
	 */
	public FolderException(final Code code) {
		this(code, null, EMPTY_ARGS);
	}

	/**
	 * Initializes a new {@link FolderException}
	 * 
	 * @param code
	 *            The folder error code
	 * @param messageArgs
	 *            The message arguments
	 */
	public FolderException(final Code code, final Object... messageArgs) {
		this(code, null, messageArgs);
	}

	/**
	 * Initializes a new {@link FolderException}
	 * 
	 * @param code
	 *            The folder error code
	 * @param cause
	 *            The init cause
	 * @param messageArgs
	 *            The message arguments
	 */
	public FolderException(final Code code, final Throwable cause, final Object... messageArgs) {
		super(Component.FOLDER, code.getCategory(), code.getNumber(), code.getMessage(), cause);
		super.setMessageArgs(messageArgs);
	}

	/**
	 * Initializes a new {@link FolderException}
	 * 
	 * @param category
	 *            The category
	 * @param number
	 *            The detail number
	 * @param message
	 *            The error message
	 * @param cause
	 *            The init cause
	 * @param messageArgs
	 *            The message arguments
	 */
	public FolderException(final Category category, final int number, final String message, final Throwable cause,
			final Object... messageArgs) {
		super(Component.FOLDER, category, number, message, cause);
		super.setMessageArgs(messageArgs);
	}

}
