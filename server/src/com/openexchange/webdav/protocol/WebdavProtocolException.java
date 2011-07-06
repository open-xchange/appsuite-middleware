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

package com.openexchange.webdav.protocol;

import com.openexchange.groupware.AbstractOXException;
import com.openexchange.webdav.WebdavException;

/**
 * {@link WebdavProtocolException} - Indicates a WebDAV/XML protocol error.
 * <p>
 * This is a subclass of {@link WebdavException}, therefore its error codes start at <code>1000</code>.
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class WebdavProtocolException extends WebdavException implements WebdavStatus<Object> {

    public static enum Code {

        /**
         * A WebDAV error occurred.
         */
        GENERAL_ERROR("A WebDAV error occurred.", CATEGORY_ERROR, 1000),
        /**
         * The folder %s doesn't exist.
         */
        FOLDER_NOT_FOUND("The folder %s doesn't exist.", CATEGORY_ERROR, 1001),
        /**
         * The directory already exists.
         */
        DIRECTORY_ALREADY_EXISTS("The directory already exists.", CATEGORY_ERROR, 1002),
        /**
         * No write permission.
         */
        NO_WRITE_PERMISSION("No write permission.", CATEGORY_PERMISSION_DENIED, 1003),
        /**
         * File &quot;%1$s&quot; already exists
         */
        FILE_ALREADY_EXISTS("File \"%1$s\" already exists.", CATEGORY_ERROR, 1004),
        /**
         * Collections must not have bodies.
         */
        NO_BODIES_ALLOWED("Collections must not have bodies.", CATEGORY_ERROR, 1005),
        /**
         * File "%1$s" does not exist.
         */
        FILE_NOT_FOUND("File \"%1$s\" does not exist.", CATEGORY_ERROR, 1006),
        /**
         * "%1$s" is a directory.
         */
        FILE_IS_DIRECTORY("\"%1$s\" is a directory.", CATEGORY_ERROR, 1007);

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

    private static final long serialVersionUID = 617401197355575125L;

    private final int status;

    private final transient WebdavPath url;

    /**
     * Initializes a new {@link WebdavProtocolException}.
     * 
     * @param url The WebDAV URL
     * @param status The (response) status code
     */
    public WebdavProtocolException(final WebdavPath url, final int status) {
        super(Code.GENERAL_ERROR.getCategory(), Code.GENERAL_ERROR.getNumber(), Code.GENERAL_ERROR.getMessage(), null);
        super.setMessageArgs(new Object[0]);
        this.url = url;
        this.status = status;
    }

    /**
     * Initializes a new {@link WebdavProtocolException}.
     * 
     * @param code The error code
     * @param url The WebDAV URL
     * @param status The (response) status code
     * @param messageArgs The message arguments
     */
    public WebdavProtocolException(final Code code, final WebdavPath url, final int status, final Object... messageArgs) {
        super(code.getCategory(), code.getNumber(), code.getMessage(), null);
        super.setMessageArgs(messageArgs);
        this.url = url;
        this.status = status;
    }

    /**
     * Initializes a new {@link WebdavProtocolException}.
     * 
     * @param code The error code
     * @param cause The init cause
     * @param url The WebDAV URL
     * @param status The (response) status code
     * @param messageArgs The message arguments
     */
    public WebdavProtocolException(final Code code, final Throwable cause, final WebdavPath url, final int status, final Object... messageArgs) {
        super(code.getCategory(), code.getNumber(), code.getMessage(), cause);
        super.setMessageArgs(messageArgs);
        this.url = url;
        this.status = status;
    }

    /**
     * Initializes a new {@link WebdavProtocolException}.
     * 
     * @param cause The init cause
     * @param url The WebDAV URL
     * @param status The (response) status code
     */
    public WebdavProtocolException(final Throwable cause, final WebdavPath url, final int status) {
        super(Code.GENERAL_ERROR.getCategory(), Code.GENERAL_ERROR.getNumber(), Code.GENERAL_ERROR.getMessage(), cause);
        super.setMessageArgs(new Object[0]);
        this.url = url;
        this.status = status;
    }

    /**
     * Initializes a new {@link WebdavProtocolException}.
     * 
     * @param cause The initial abstract exception
     * @param url The WebDAV URL
     * @param status The (response) status code
     */
    public WebdavProtocolException(final AbstractOXException cause, final WebdavPath url, final int status) {
        super(cause);
        this.url = url;
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public WebdavPath getUrl() {
        return url;
    }

    public Object getAdditional() {
        return null;
    }

    @Override
    public String toString() {
        final String msg = super.toString();
        return new StringBuilder(msg.length() + 64).append(msg).append(' ').append(getUrl()).append(' ').append(getStatus()).toString();
    }

}
