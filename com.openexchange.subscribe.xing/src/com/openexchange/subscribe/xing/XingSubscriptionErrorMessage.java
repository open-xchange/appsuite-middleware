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

package com.openexchange.subscribe.xing;

import com.openexchange.exceptions.OXErrorMessage;
import com.openexchange.groupware.AbstractOXException.Category;


/**
 * {@link XingSubscriptionErrorMessage}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public enum XingSubscriptionErrorMessage implements OXErrorMessage {
    INVALID_LOGIN(Category.USER_INPUT, 1, "Please correct the password and try again", "The password you entered was wrong"),
    COMMUNICATION_PROBLEM(Category.SUBSYSTEM_OR_SERVICE_DOWN, 2, "Make sure, that the XING-Service is still available, and there are no major changes on the website", "XING-Service unavailable"),
    INVALID_WORKFLOW(Category.SETUP_ERROR, 3, "Please correct the steps of this workflow so that output of one step and input of the next step match", "The steps of this crawling workflow do not fit together"),
    ERROR_WHEN_DOWNLOADING_VCARDS(Category.SUBSYSTEM_OR_SERVICE_DOWN, 4, "This error should not repeat ,please try again.", "Error while downloading contact information.");

    private Category category;
    private int errorCode;
    private String help;
    private String message;
    
    public static final XingSubscriptionExceptionFactory EXCEPTIONS = new XingSubscriptionExceptionFactory();
    
    private XingSubscriptionErrorMessage(Category category, int errorCode, String help, String message) {
        this.category = category;
        this.errorCode = errorCode;
        this.help = help;
        this.message = message;
    }
    
    public Category getCategory() {
        return category;
    }

    public int getDetailNumber() {
        return errorCode;
    }

    public String getHelp() {
        return help;
    }

    public String getMessage() {
        return message;
    }
    
    public XingSubscriptionException create(Throwable cause, Object...args) {
        return EXCEPTIONS.create(this, cause, args);
    }
    
    public XingSubscriptionException create(Object...args) {
        return EXCEPTIONS.create(this, args);
    }
}
