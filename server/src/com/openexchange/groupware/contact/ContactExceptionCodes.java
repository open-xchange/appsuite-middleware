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
    /** Mime type is not defined. */
    MIME_TYPE_NOT_DEFINED(MIME_TYPE_NOT_DEFINED_MSG, Category.USER_INPUT, 170),
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
