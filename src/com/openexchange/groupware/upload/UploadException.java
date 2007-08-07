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

package com.openexchange.groupware.upload;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Component;

/**
 * UploadException
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class UploadException extends AbstractOXException {

	private static final long serialVersionUID = 8590042770250274015L;
	
	private final String action;
	
	public static enum UploadCode {
		/**
		 * File upload failed: %s
		 */
		UPLOAD_FAILED("File upload failed: %s", Category.INTERNAL_ERROR, 1),
		/**
		 * Missing affiliation id
		 */
		MISSING_AFFILIATION_ID("Missing affiliation id", Category.CODE_ERROR, 2),
		/**
		 * Unknown action value: %s
		 */
		UNKNOWN_ACTION_VALUE("Unknown action value: %s", Category.CODE_ERROR, 3),
		/**
		 * Header "content-type" does not indicate multipart content
		 */
		NO_MULTIPART_CONTENT("Header \"content-type\" does not indicate multipart content", Category.CODE_ERROR,
				4),
		/**
		 * Request rejected because its size (%d) exceeds the maximum configured
		 * size of %d
		 */
		MAX_UPLOAD_SIZE_EXCEEDED("Request rejected because its size (%d) exceeds the maximum configured size of %d",
				Category.USER_INPUT, 5);

		private final String message;

		private final Category category;

		private final int detailNumber;

		private UploadCode(final String message, final Category category, final int detailNumber) {
			this.message = message;
			this.category = category;
			this.detailNumber = detailNumber;
		}

		public final Category getCategory() {
			return category;
		}

		public final int getNumber() {
			return detailNumber;
		}

		public final String getMessage() {
			return message;
		}
	}

	public UploadException(final UploadCode uploadCode, final String action, final Throwable cause) {
		super(Component.UPLOAD, uploadCode.category, uploadCode.detailNumber, uploadCode.message, cause);
		super.setMessageArgs(cause.getMessage());
		this.action = action;
	}
	
	public UploadException(final UploadCode uploadCode, final String action, final Object... messageArgs) {
		super(Component.UPLOAD, uploadCode.category, uploadCode.detailNumber, uploadCode.message, null);
		super.setMessageArgs(messageArgs);
		this.action = action;
	}
	
	public UploadException(final UploadCode uploadCode, final String action) {
		super(Component.UPLOAD, uploadCode.category, uploadCode.detailNumber, uploadCode.message, null);
		super.setMessageArgs(new Object[0]);
		this.action = action;
	}
	
	public String getAction() {
		return action;
	}

}
