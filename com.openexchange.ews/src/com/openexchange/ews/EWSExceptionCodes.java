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

package com.openexchange.ews;

import com.microsoft.schemas.exchange.services._2006.messages.ResponseMessageType;
import com.microsoft.schemas.exchange.services._2006.types.ResponseClassType;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionCode;
import com.openexchange.exception.OXExceptionFactory;

/**
 * {@link EWSExceptionCodes}
 * 
 * Exception codes for the EWS client.
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public enum EWSExceptionCodes implements OXExceptionCode {
    
    /** Got %1$d instead of %2$d response messages */
    UNEXPECTED_RESPONSE_COUNT(EWSExceptionMessages.UNEXPECTED_RESPONSE_COUNT_MSG, Category.CATEGORY_ERROR, 1),
    
    /** Got no response messages */
    NO_RESPONSE(EWSExceptionMessages.NO_RESPONSE_MSG, Category.CATEGORY_ERROR, 2),
    
    /** EWS error: %1$s (%2$s) */
    EWS_ERROR(EWSExceptionMessages.EWS_ERROR_MSG, Category.CATEGORY_ERROR, 3),
    
    /** EWS warning: %1$s (%2$s) */
    EWS_WARNING(EWSExceptionMessages.EWS_WARNING_MSG, Category.CATEGORY_WARNING, 4),
        
    /** Object \"%1$s\" not found */
    NOT_FOUND(EWSExceptionMessages.NOT_FOUND_MSG, Category.CATEGORY_USER_INPUT, 5),
        
    /** The name \"%1$s\" is ambiguous */
    AMBIGUOUS_NAME(EWSExceptionMessages.AMBIGUOUS_NAME_MSG, Category.CATEGORY_CONFLICT, 6),
        
    /** An external error occured: %1$s */
    EXTERNAL_ERROR(EWSExceptionMessages.EXTERNAL_ERROR_MSG, Category.CATEGORY_ERROR, 7),
        
    ;

    private static final String PREFIX = "EWS";

    private final Category category;
    private final int number;
    private final String message;

    private EWSExceptionCodes(String message, Category category, int detailNumber) {
        this.message = message;
        number = detailNumber;
        this.category = category;
    }

    @Override
    public String getPrefix() {
        return PREFIX;
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
        return number;
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
    public OXException create(Throwable cause, Object... args) {
        return OXExceptionFactory.getInstance().create(this, cause, args);
    }
    
    /**
     * Creates a new {@link OXException} instance based on the supplied EWS response message.
     * 
     * @param responseMessage The EWS response message to create the exception for
     * @return The OX exception, or <code>null</code> if there was no error
     */
    public static OXException create(ResponseMessageType responseMessage) {
        if (null != responseMessage && false == ResponseClassType.SUCCESS.equals(responseMessage.getResponseClass())) {
            if (ResponseClassType.WARNING.equals(responseMessage.getResponseClass())) {
                return EWSExceptionCodes.EWS_WARNING.create(responseMessage.getMessageText(), responseMessage.getResponseCode());                
            } else {
                return EWSExceptionCodes.EWS_ERROR.create(responseMessage.getMessageText(), responseMessage.getResponseCode());                
            }
        } else {
            return null;
        }
    }

}
