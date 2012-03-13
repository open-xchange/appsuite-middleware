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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.mq;

import javax.jms.JMSException;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;

/**
 * {@link MQExceptionCodes} - Enumeration of all {@link OXException}s known in Message Queue (MQ) module.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum MQExceptionCodes implements OXExceptionCode {

    /**
     * An error occurred: %1$s
     */
    UNEXPECTED_ERROR(MQExceptionMessages.UNEXPECTED_ERROR_MSG, CATEGORY_ERROR, 1),
    /**
     * An I/O error occurred: %1$s
     */
    IO_ERROR(MQExceptionMessages.IO_ERROR_MSG, CATEGORY_ERROR, 2),
    /**
     * No connection factory found for name: %1$s
     */
    CF_NOT_FOUND(MQExceptionMessages.CF_NOT_FOUND_MSG, CATEGORY_ERROR, 3),
    /**
     * No such queue or could not be created: %1$s
     */
    QUEUE_NOT_FOUND(MQExceptionMessages.QUEUE_NOT_FOUND_MSG, CATEGORY_ERROR, 4),
    /**
     * No such topic or could not be created: %1$s
     */
    TOPIC_NOT_FOUND(MQExceptionMessages.TOPIC_NOT_FOUND_MSG, CATEGORY_ERROR, 5),
    /**
     * A JMS error occurred: %1$s
     */
    JMS_ERROR(MQExceptionMessages.JMS_ERROR_MSG, CATEGORY_ERROR, 6),
    /**
     * Illegal state: %1$s
     */
    ILLEGAL_STATE(MQExceptionMessages.ILLEGAL_STATE_MSG, CATEGORY_ERROR, 7),
    /**
     * A filter expression has not been validated.
     */
    INVALID_SELECTOR(MQExceptionMessages.INVALID_SELECTOR_MSG, CATEGORY_ERROR, 8),
    /**
     * Either no such queue or a topic or could not be created: %1$s
     */
    DESTINATION_NOT_FOUND(MQExceptionMessages.DESTINATION_NOT_FOUND_MSG, CATEGORY_ERROR, 9),
    /**
     * A security problem occurred: %1$s
     */
    SECURITY_ERROR(MQExceptionMessages.SECURITY_ERROR_MSG, CATEGORY_PERMISSION_DENIED, 10),

    ;

    /**
     * Handles specified {@link JMSException} instance and generates an appropriate {@link OXException} instance for it.
     * 
     * @param jmsException The JMS error to handle
     * @return The appropriate {@link OXException} instance
     */
    public static OXException handleJMSException(final JMSException jmsException) {
        if (jmsException instanceof javax.jms.IllegalStateException) {
            return ILLEGAL_STATE.create(jmsException, jmsException.getMessage());
        }
        if (jmsException instanceof javax.jms.InvalidSelectorException) {
            return INVALID_SELECTOR.create(jmsException, jmsException.getMessage());
        }
        if (jmsException instanceof javax.jms.InvalidDestinationException) {
            return DESTINATION_NOT_FOUND.create(jmsException, jmsException.getMessage());
        }
        return JMS_ERROR.create(jmsException, jmsException.getMessage());
    }

    /**
     * The error code prefix for Message Queue (MQ) module.
     */
    public static final String MQ = "MQ";

    private final Category category;

    private final int detailNumber;

    private final String message;

    private MQExceptionCodes(final String message, final Category category, final int detailNumber) {
        this.message = message;
        this.detailNumber = detailNumber;
        this.category = category;
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public int getNumber() {
        return detailNumber;
    }

    @Override
    public String getPrefix() {
        return MQ;
    }

    @Override
    public boolean equals(final OXException e) {
        return OXExceptionFactory.getInstance().equals(this, e);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     * 
     * @return The newly created {@link OXException} instance
     */
    public OXException create() {
        return OXExceptionFactory.getInstance().create(this, new Object[0]);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     * 
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final Object... args) {
        return OXExceptionFactory.getInstance().create(this, (Throwable) null, args);
    }

    /**
     * Creates a new {@link OXException} instance pre-filled with this code's attributes.
     * 
     * @param cause The optional initial cause
     * @param args The message arguments in case of printf-style message
     * @return The newly created {@link OXException} instance
     */
    public OXException create(final Throwable cause, final Object... args) {
        return OXExceptionFactory.getInstance().create(this, cause, args);
    }
}
