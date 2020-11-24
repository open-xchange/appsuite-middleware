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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.contact.provider;

import static com.openexchange.contact.provider.ContactsProviderExceptionMessages.ACCOUNT_NOT_FOUND_MSG;
import static com.openexchange.contact.provider.ContactsProviderExceptionMessages.CONCURRENT_MODIFICATION_MSG;
import static com.openexchange.contact.provider.ContactsProviderExceptionMessages.CONTACT_NOT_FOUND_MSG;
import static com.openexchange.contact.provider.ContactsProviderExceptionMessages.FOLDER_NOT_FOUND_MSG;
import static com.openexchange.contact.provider.ContactsProviderExceptionMessages.MANDATORY_FIELD_MSG;
import static com.openexchange.contact.provider.ContactsProviderExceptionMessages.MISSING_CAPABILITY_MSG;
import static com.openexchange.contact.provider.ContactsProviderExceptionMessages.PROVIDER_NOT_AVAILABLE_MSG;
import static com.openexchange.contact.provider.ContactsProviderExceptionMessages.UNSUPPORTED_FOLDER_MSG;
import static com.openexchange.contact.provider.ContactsProviderExceptionMessages.UNSUPPORTED_OPERATION_FOR_PROVIDER_MSG;
import static com.openexchange.exception.OXExceptionStrings.MESSAGE;
import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * {@link ContactsProviderExceptionCodes} - Complements the {@link ContactExceptionCodes}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @since v7.10.5
 */
public enum ContactsProviderExceptionCodes implements DisplayableOXExceptionCode {

    /**
     * <li>The requested contacts account was not found.</li>
     * <li>Account not found [account %1$d]</li>
     */
    ACCOUNT_NOT_FOUND("Account not found [account %1$d]", ACCOUNT_NOT_FOUND_MSG, Category.CATEGORY_USER_INPUT, 2001),
    /**
     * <li>The operation could not be completed due to insufficient capabilities.</li>
     * <li>Missing capability [%1$s]</li>
     */
    MISSING_CAPABILITY("Missing capability [%1$s]", MISSING_CAPABILITY_MSG, Category.CATEGORY_PERMISSION_DENIED, 2002),
    /**
     * <li>The supplied folder is not supported. Please select a valid folder and try again.</li>
     * <li>Unsupported folder [folder %1$s, content type %2$s]</li>
     */
    UNSUPPORTED_FOLDER("Unsupported folder [folder %1$s, content type %2$s]", UNSUPPORTED_FOLDER_MSG, Category.CATEGORY_USER_INPUT, 2003),
    /**
     * <li>The contacts provider '%1$s' is not available.</li>
     * <li>Missing contacts provider [provider: %1$s]</li>
     */
    PROVIDER_NOT_AVAILABLE("Missing contacts provider [provider: %1$s]", PROVIDER_NOT_AVAILABLE_MSG, Category.CATEGORY_SERVICE_DOWN, 2004),
    /**
     * <li>The requested operation is not supported for contacts provider '%1$s'.</li>
     * <li>Unsupported operation for contacts provider [provider %1$s]</li>
     */
    UNSUPPORTED_OPERATION_FOR_PROVIDER("Unsupported operation for contacts provider [provider %1$s]", UNSUPPORTED_OPERATION_FOR_PROVIDER_MSG, Category.CATEGORY_USER_INPUT, 2005),
    /**
     * <li>Error while reading/writing data from/to the database.</li>
     * <li>Unexpected database error [%1$s]</li>
     */
    DB_ERROR("Unexpected database error [%1$s]", OXExceptionStrings.SQL_ERROR_MSG, Category.CATEGORY_ERROR, 2006),
    /**
     * <li>Error while reading/writing data from/to the database.</li>
     * <li>Unexpected database error, try again [%1$s]</li>
     */
    DB_ERROR_TRY_AGAIN("Unexpected database error, try again [%1$s]", OXExceptionStrings.SQL_ERROR_MSG, Category.CATEGORY_TRY_AGAIN, 2007),
    /**
     * <li>Error while reading/writing data from/to the database.</li>
     * <li>Contacts account data not written in storage</li>
     */
    ACCOUNT_NOT_WRITTEN("Contacts account data not written in storage", OXExceptionStrings.SQL_ERROR_MSG, Category.CATEGORY_CONFLICT, 2008),
    /**
     * <li>The operation could not be completed due to a concurrent modification. Please reload the data and try again.</li>
     * <li>Concurrent modification [id %1$s, client timestamp %2$d, actual timestamp %3$d]</li>
     */
    CONCURRENT_MODIFICATION("Concurrent modification [id %1$s, client timestamp %2$d, actual timestamp %3$d]", CONCURRENT_MODIFICATION_MSG, Category.CATEGORY_CONFLICT, 2009),
    /**
     * <li>The requested contact was not found.</li>
     * <li>Contact not found in folder [folder %1$s, id %2$s]</li>
     */
    CONTACT_NOT_FOUND_IN_FOLDER("Contact not found in folder [folder %1$s, id %2$s]", CONTACT_NOT_FOUND_MSG, Category.CATEGORY_USER_INPUT, 2010),
    /**
     * <li>The requested folder was not found.</li>
     * <li>Folder not found [folder %1$s]</li>
     */
    FOLDER_NOT_FOUND("Folder not found [folder %1$s]", FOLDER_NOT_FOUND_MSG, Category.CATEGORY_USER_INPUT, 2011),
    /**
     * <li>An error occurred inside the server which prevented it from fulfilling the request.</li>
     * <li>Unexpected error [%1$s]</li>
     */
    UNEXPECTED_ERROR("Unexpected error [%1$s]", OXExceptionStrings.MESSAGE, Category.CATEGORY_ERROR, 2012),
    /**
     * <li>The field '%1$s' is mandatory. Please supply a valid value and try again.</li>
     * <li>Mandatory field missing [field %1$s]</li>
     */
    MANDATORY_FIELD("Mandatory field missing [field %1$s]", MANDATORY_FIELD_MSG, Category.CATEGORY_USER_INPUT, 2013),
    /**
     * <li>An error occurred inside the server which prevented it from fulfilling the request.</li>
     * <li>Invalid range limits specified: leftHandLimit should not be greater than the rightHandLimit.</li>
     */
    INVALID_RANGE_LIMITS("Invalid range limits specified: leftHandLimit should not be greater than the rightHandLimit.", OXExceptionStrings.MESSAGE, Category.CATEGORY_ERROR, 2014),

    ;

    public static final String PREFIX = "CON".intern();

    private String message;
    private String displayMessage;
    private Category category;
    private int number;

    /**
     * Initializes a new {@link ContactsProviderExceptionCodes}.
     * 
     * @param message The internal message
     * @param displayMessage The display message
     * @param category The error category
     * @param number The error number
     */
    private ContactsProviderExceptionCodes(String message, String displayMessage, Category category, int number) {
        this.message = message;
        this.displayMessage = null != displayMessage ? displayMessage : MESSAGE;
        this.category = category;
        this.number = number;
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
    public String getPrefix() {
        return PREFIX;
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
