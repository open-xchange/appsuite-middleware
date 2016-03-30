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
 *    trademarks of the OX Software GmbH group of companies.
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
 *     Copyright (C) 2016-2020 OX Software GmbH
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


package com.openexchange.groupware.userconfiguration;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * User configuration error codes.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public enum UserConfigurationCodes implements DisplayableOXExceptionCode {

    /**
     * A SQL error occurred: %1$s
     */
    SQL_ERROR("A SQL error occurred: %1$s", null, Category.CATEGORY_ERROR, 1),
    
    /**
     * A DBPooling error occurred
     */
    DBPOOL_ERROR("A DBPooling error occurred", null, Category.CATEGORY_ERROR, 2),
    
    /**
     * Configuration for user %1$s could not be found in context %2$d
     */
    NOT_FOUND("Configuration for user %1$s could not be found in context %2$d", UserConfigurationExceptionMessage.NOT_FOUND_MSG,
        Category.CATEGORY_ERROR, 3),
        
    /**
     * Missing property %1$s in system.properties.
     */
    MISSING_SETTING("Missing property %1$s in system.properties.", null, Category.CATEGORY_CONFIGURATION, 4),
    
    /**
     * Class %1$s can not be found.
     */
    CLASS_NOT_FOUND("Class %1$s can not be found.", null, Category.CATEGORY_CONFIGURATION, 5),
    
    /**
     * Instantiating the class failed.
     */
    INSTANTIATION_FAILED("Instantiating the class failed.", null, Category.CATEGORY_ERROR, 6),
    
    /**
     * Cache initialization failed. Region: %1$s
     */
    CACHE_INITIALIZATION_FAILED("Cache initialization failed. Region: %1$s", null, Category.CATEGORY_ERROR, 7),
    
    /**
     * User configuration could not be put into cache: %1$s
     */
    CACHE_PUT_ERROR("User configuration could not be put into cache: %1$s", UserConfigurationExceptionMessage.CACHE_PUT_ERROR_MSG,
        Category.CATEGORY_ERROR, 8),
        
    /**
     * User configuration cache could not be cleared: %1$s
     */
    CACHE_CLEAR_ERROR("User configuration cache could not be cleared: %1$s", UserConfigurationExceptionMessage.CACHE_CLEAR_ERROR_MSG,
        Category.CATEGORY_ERROR, 9),
        
    /**
     * User configuration could not be removed from cache: %1$s
     */
    CACHE_REMOVE_ERROR("User configuration could not be removed from cache: %1$s", UserConfigurationExceptionMessage.CACHE_REMOVE_ERROR_MSG,
        Category.CATEGORY_ERROR, 9),
        
    /**
     * Mail settings for user %1$s could not be found in context %2$d
     */
    MAIL_SETTING_NOT_FOUND("Mail settings for user %1$s could not be found in context %2$d",
        UserConfigurationExceptionMessage.MAIL_SETTING_NOT_FOUND_MSG, Category.CATEGORY_ERROR, 10);

    private String message;
    
    private String displayMessage;

    private int detailNumber;

    private Category category;

    private UserConfigurationCodes(String message, String displayMessage, Category category, int detailNumber) {
        this.message = message;
        this.displayMessage = displayMessage != null ? displayMessage : OXExceptionStrings.MESSAGE;
        this.detailNumber = detailNumber;
        this.category = category;
    }

    @Override
    public String getPrefix() {
        return "USS";
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public int getNumber() {
        return detailNumber;
    }

    @Override
    public String getMessage() {
        return message;
    }
    
    @Override
    public String getDisplayMessage() {
        return displayMessage;
    }

    @Override
    public boolean equals(OXException e) {
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
    public OXException create(Object... args) {
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

}
