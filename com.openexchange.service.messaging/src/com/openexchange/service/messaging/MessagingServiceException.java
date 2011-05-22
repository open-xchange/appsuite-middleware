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

package com.openexchange.service.messaging;

import com.openexchange.exceptions.ErrorMessage;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Component;

/**
 * {@link MessagingServiceException} - A messaging service exception.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class MessagingServiceException extends AbstractOXException {

    private static final long serialVersionUID = 959078050587154533L;

    private static final String STR_COMPONENT = "MESSAGING-SERVICE";

    /**
     * The {@link Component} for messaging service exception.
     */
    public static final Component COMPONENT = new Component() {

        private static final long serialVersionUID = 2016633405834163746L;

        public String getAbbreviation() {
            return STR_COMPONENT;
        }
    };

    /**
     * Initializes a new {@link MessagingServiceException}.
     * 
     * @param cause The cause
     */
    public MessagingServiceException(final AbstractOXException cause) {
        super(cause);
    }

    /**
     * Initializes a new {@link MessagingServiceException}.
     * 
     * @param message The message
     * @param cause The cause
     */
    public MessagingServiceException(final String message, final AbstractOXException cause) {
        super(COMPONENT, message, cause);
    }

    /**
     * Initializes a new {@link MessagingServiceException}.
     * 
     * @param category The category
     * @param detailNumber The detail number
     * @param message The message
     * @param cause The cause
     */
    public MessagingServiceException(final Category category, final int detailNumber, final String message, final Throwable cause) {
        super(COMPONENT, category, detailNumber, message, cause);
    }

    /**
     * Initializes a new {@link MessagingServiceException}.
     * 
     * @param message The message
     * @param cause The cause
     */
    public MessagingServiceException(final ErrorMessage message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * Initializes a new {@link MessagingServiceException}.
     * 
     * @param message The message
     * @param cause The cause
     * @param messageArguments The message arguments
     */
    public MessagingServiceException(final ErrorMessage message, final Throwable cause, final Object... messageArguments) {
        super(message, cause);
        setMessageArgs(messageArguments);
    }

    /**
     * Initializes a new {@link MessagingServiceException}.
     * 
     * @param component The component
     * @param message The message
     * @param cause The cause
     */
    protected MessagingServiceException(final Component component, final String message, final AbstractOXException cause) {
        super(component, message, cause);
    }

    /**
     * Initializes a new {@link MessagingServiceException}.
     * 
     * @param component The component
     * @param category The category
     * @param detailNumber The detail number
     * @param message The message
     * @param cause The cause
     */
    protected MessagingServiceException(final Component component, final Category category, final int detailNumber, final String message, final Throwable cause) {
        super(component, category, detailNumber, message, cause);
    }

}
