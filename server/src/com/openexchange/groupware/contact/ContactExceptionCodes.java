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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.groupware.contact;

import static com.openexchange.groupware.contact.ContactExceptionMessages.*;
import com.openexchange.exceptions.OXErrorMessage;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.contact.internal.ContactExceptionFactory;

/**
 * {@link ContactExceptionCodes}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public enum ContactExceptionCodes implements OXErrorMessage {

    /** Invalid E-Mail address: '%s'. Please correct the E-Mail address. */
    INVALID_EMAIL(INVALID_EMAIL_MSG, Category.USER_INPUT, 100),
    /** Unable to scale this contact image. Either the file type is not supported or the image is too large. Your mime type is %1$s and your image size is %2$d. The max. allowed image size is %3$d. */
    IMAGE_SCALE_PROBLEM(IMAGE_SCALE_PROBLEM_MSG, Category.USER_INPUT, 101),
    /** You are not allowed to store this contact in a non-contact folder: folder id %1$d in context %2$d with user %3$d */
    NON_CONTACT_FOLDER(NON_CONTACT_FOLDER_MSG, Category.PERMISSION, 103),
    /** You do not have permission to create objects in this folder %1$d in context %2$d with user %3$d */
    NO_PERMISSION(NO_PERMISSION_MSG, Category.PERMISSION, 104),
    /** Got a -1 ID from IDGenerator */
    ID_GENERATION_FAILED(ID_GENERATION_FAILED_MSG, Category.CODE_ERROR, 107),
    /** Unable to scale image down. */
    IMAGE_DOWNSCALE_FAILED(IMAGE_DOWNSCALE_FAILED_MSG, Category.CODE_ERROR, 108),
    /** Invalid SQL Query: %s */
    INVALID_SQL_QUERY(INVALID_SQL_QUERY_MSG, Category.CODE_ERROR, 109),
    /** Unable to pick up a connection from the DBPool */
    INIT_CONNECTION_FROM_DBPOOL(INIT_CONNECTION_FROM_DBPOOL_MSG, Category.SUBSYSTEM_OR_SERVICE_DOWN, 151),
    /** The image you tried to attach is not a valid picture. It may be broken or is not a valid file. */
    NOT_VALID_IMAGE(NOT_VALID_IMAGE_MSG, Category.TRY_AGAIN, 158),
    /** Mime type is not defined. */
    MIME_TYPE_NOT_DEFINED(MIME_TYPE_NOT_DEFINED_MSG, Category.USER_INPUT, 170),
    /** A contact with private flag cannot be stored in a public folder. Folder: %1$d context %2$d user %3$d */
    PFLAG_IN_PUBLIC_FOLDER(PFLAG_IN_PUBLIC_FOLDER_MSG, Category.USER_INPUT, 171),
    /** Image size too large. Image size: %1$d. Max. size: %2$d. */
    IMAGE_TOO_LARGE(IMAGE_TOO_LARGE_MSG, Category.USER_INPUT, 172)
    ;

    private String message;
    private Category category;
    private int number;

    private ContactExceptionCodes(String message, Category category, int number) {
        this.message = message;
        this.category = category;
        this.number = number;
    }

    public int getDetailNumber() {
        return number;
    }

    public String getMessage() {
        return message;
    }

    public String getHelp() {
        // TODO Auto-generated method stub
        return null;
    }

    public Category getCategory() {
        return category;
    }

    public ContactException create(Object... args) {
        return ContactExceptionFactory.getInstance().create(this, args);
    }

    public ContactException create(Throwable cause, Object... args) {
        return ContactExceptionFactory.getInstance().create(this, cause, args);
    }
}
