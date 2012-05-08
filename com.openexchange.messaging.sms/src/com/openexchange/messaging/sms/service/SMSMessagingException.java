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

package com.openexchange.messaging.sms.service;

import com.openexchange.exceptions.ErrorMessage;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Component;
import com.openexchange.messaging.MessagingException;

/**
 * {@link SMSMessagingException} - A sms messaging exception
 * 
 * @author <a href="mailto:dennis.sieben@open-xchange.com">Dennis Sieben</a>
 * @since Open-Xchange v6.20
 */
public class SMSMessagingException extends MessagingException {

    private static final long serialVersionUID = 6592632090566106258L;

    private static final String STR_COMPONENT = "SMS-MSG";

    /**
     * The {@link Component} for twitter messaging exception.
     */
    public static final Component SMS_MSG_COMPONENT = new Component() {

        private static final long serialVersionUID = -4936677058201275621L;

        public String getAbbreviation() {
            return STR_COMPONENT;
        }
    };

    /**
     * Initializes a new {@link SMSMessagingException}.
     * 
     * @param cause The cause
     */
    public SMSMessagingException(final AbstractOXException cause) {
        super(cause);
    }

    /**
     * Initializes a new {@link SMSMessagingException}.
     * 
     * @param message The message
     * @param cause The cause
     */
    public SMSMessagingException(final String message, final AbstractOXException cause) {
        super(SMS_MSG_COMPONENT, message, cause);
    }

    /**
     * Initializes a new {@link SMSMessagingException}.
     * 
     * @param category The category
     * @param detailNumber The detail number
     * @param message The message
     * @param cause The cause
     */
    public SMSMessagingException(final Category category, final int detailNumber, final String message, final Throwable cause) {
        super(SMS_MSG_COMPONENT, category, detailNumber, message, cause);
    }

    /**
     * Initializes a new {@link SMSMessagingException}.
     * 
     * @param message The message
     * @param cause The cause
     */
    public SMSMessagingException(final ErrorMessage message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Initializes a new {@link SMSMessagingException}.
     * 
     * @param message The message
     * @param cause The cause
     * @param messageArguments The message arguments
     */
    public SMSMessagingException(final ErrorMessage message, final Throwable cause, final Object... messageArguments) {
        super(message, cause);
        setMessageArgs(messageArguments);
    }

}
