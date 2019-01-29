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
 *     Copyright (C) 2018-2020 OX Software GmbH
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

package com.openexchange.microsoft.graph.onedrive.exception;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * {@link MicrosoftGraphDriveServiceExceptionCodes}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.2
 */
public enum MicrosoftGraphDriveServiceExceptionCodes implements DisplayableOXExceptionCode {
    /**
     * <li>The folder does not exist.</li>
     * <li>The folder with id '%1$s' was not found.</li>
     */
    FOLDER_NOT_FOUND("The folder with id '%1$s' was not found", MicrosoftGraphDriveServiceExceptionMessages.FOLDER_NOT_EXISTS, Category.CATEGORY_ERROR, 1),
    /**
     * <li>An error occurred inside the server which prevented it from fulfilling the request.</li>
     * <li>A JSON error occurred: %1$s</li>
     */
    JSON_ERROR("A JSON error occurred: %1$s", Category.CATEGORY_ERROR, 2),
    /**
     * <li>An error occurred inside the server which prevented it from fulfilling the request.</li>
     * <li>A folder named '%1$s' already exists under the folder with id '%2$s'</li>
     */
    FOLDER_ALREADY_EXISTS("A folder named '%1$s' already exists under the folder with id '%2$s'", Category.CATEGORY_ERROR, 3),
    /**
     * <li>You are not allowed to delete the root folder.</li>
     * <li>The root folder cannot be deleted</li>
     */
    CANNOT_DELETE_ROOT_FOLDER("The root folder cannot be deleted", MicrosoftGraphDriveServiceExceptionMessages.CANNOT_DELETE_ROOT_FOLDER, Category.CATEGORY_ERROR, 4),
    /**
     * <li>An error occurred inside the server which prevented it from fulfilling the request.</li>
     * <li>The 'folder' field is missing from the entity object. Either the entity is not a folder, or the response is erroneous.</li>
     */
    NOT_A_FOLDER("The 'folder' field is missing from the entity object. Either the entity is not a folder, or the response is erroneous.", Category.CATEGORY_ERROR, 5),

    ;

    private final String message;
    private final String displayMessage;
    private final Category category;
    private final int number;

    /**
     * Initialises a new {@link MicrosoftGraphDriveServiceExceptionCodes}.
     * 
     * @param message
     * @param category
     * @param number
     */
    private MicrosoftGraphDriveServiceExceptionCodes(String message, Category category, int number) {
        this(message, null, category, number);
    }

    /**
     * Initialises a new {@link MicrosoftGraphDriveServiceExceptionCodes}.
     * 
     * @param message
     * @param displayMessage
     * @param category
     * @param number
     */
    private MicrosoftGraphDriveServiceExceptionCodes(String message, String displayMessage, Category category, int number) {
        this.message = message;
        this.displayMessage = displayMessage != null ? displayMessage : OXExceptionStrings.MESSAGE;
        this.category = category;
        this.number = number;
    }

    @Override
    public String getPrefix() {
        return "MS-GRAPH-ONE-DRIVE-SERVICE";
    }

    @Override
    public int getNumber() {
        return number;
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
    public Category getCategory() {
        return category;
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
