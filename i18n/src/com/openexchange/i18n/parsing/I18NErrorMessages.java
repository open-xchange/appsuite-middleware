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

package com.openexchange.i18n.parsing;

import com.openexchange.exceptions.OXErrorMessage;
import com.openexchange.groupware.AbstractOXException.Category;

/**
 * @author Francisco Laguna <francisco.laguna@open-xchange.com>
 */
public enum I18NErrorMessages implements OXExceptionCode {

    UNEXPECTED_TOKEN(101, I18NErrorStrings.UNEXPECTED_TOKEN, I18NErrorStrings.CHECK_FILE, CATEGORY_CONFIGURATION),
    UNEXPECTED_TOKEN_CONSUME(102, I18NErrorStrings.UNEXPECTED_TOKEN, I18NErrorStrings.CHECK_FILE, CATEGORY_CONFIGURATION),
    EXPECTED_NUMBER(103, I18NErrorStrings.EXPECTED_NUMBER, I18NErrorStrings.CHECK_FILE, CATEGORY_CONFIGURATION),
    MALFORMED_TOKEN(104, I18NErrorStrings.MALFORMED_TOKEN, I18NErrorStrings.CHECK_FILE, CATEGORY_CONFIGURATION),
    IO_EXCEPTION(105, I18NErrorStrings.IO_EXCEPTION, I18NErrorStrings.FILE_ACCESS, CATEGORY_CONFIGURATION);

    public static I18NExceptions FACTORY = new I18NExceptions();

    private Category category;

    private String help;

    private String message;

    private int errorCode;

    I18NErrorMessages(final int errorCode, final String message, final String help, final Category category) {
        this.category = category;
        this.help = help;
        this.message = message;
        this.errorCode = errorCode;
    }

    public int getDetailNumber() {
        return errorCode;
    }

    public String getMessage() {
        return message;
    }

    public String getHelp() {
        return help;
    }

    public Category getCategory() {
        return category;
    }

    public void throwException(final Throwable cause, final Object... args) throws I18NException {
        throw FACTORY.create(this, cause, args);
    }

    public void throwException(final Object... args) throws I18NException {
        throwException(null, args);
    }

}
