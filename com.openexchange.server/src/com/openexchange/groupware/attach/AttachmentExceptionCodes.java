/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.groupware.attach;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;
import com.openexchange.groupware.EnumComponent;

/**
 * {@link AttachmentExceptionCodes}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public enum AttachmentExceptionCodes implements DisplayableOXExceptionCode {

    /** Attachment cannot be saved. File store limit is exceeded. */
    OVER_LIMIT("Attachment cannot be saved. File store limit is exceeded.", AttachmentExceptionMessages.OVER_LIMIT_MSG,
        CATEGORY_CAPACITY, 1),
    
    /** Invalid SQL Query: %s */
    SQL_PROBLEM("Invalid SQL Query: %s", OXExceptionStrings.SQL_ERROR_MSG, CATEGORY_ERROR, 100),
    
    /** Could not save file to the file store. */
    SAVE_FAILED("Could not save file to the file store.", AttachmentExceptionMessages.SAVE_FAILED_MSG, CATEGORY_SERVICE_DOWN, 400),
    
    /** Attachments must contain a file. */
    FILE_MISSING("Attachments must contain a file.", AttachmentExceptionMessages.FILE_MISSING_MSG, CATEGORY_USER_INPUT, 401),
    
    /** Cannot generate ID for new attachment: %s */
    GENERATIING_ID_FAILED("Cannot generate ID for new attachment: %s", null, CATEGORY_ERROR, 402),
    
    /** Could not retrieve file: %s */
    READ_FAILED("Could not retrieve file: %s", AttachmentExceptionMessages.READ_FAILED_MSG, CATEGORY_SERVICE_DOWN, 404),
    
    /** The attachment you requested no longer exists. Please refresh the view. */
    ATTACHMENT_NOT_FOUND("The attachment you requested no longer exists. Please refresh the view.",
        AttachmentExceptionMessages.ATTACHMENT_NOT_FOUND_MSG, CATEGORY_USER_INPUT, 405),
        
    /** Could not delete attachment. */
    DELETE_FAILED("Could not delete attachment.", AttachmentExceptionMessages.DELETE_FAILED_MSG, CATEGORY_ERROR, 407),
    
    /** Could not find an attachment with the file_id %s. Either the file is orphaned or belongs to another module. */
    ATTACHMENT_WITH_FILEID_NOT_FOUND("Could not find an attachment with the file id %s. Either the file is orphaned or"
        + " belongs to another module.", null, CATEGORY_ERROR, 408),
        
    /** Could not delete files from filestore. Context: %d. */
    FILE_DELETE_FAILED("Could not delete files from file store. Context: %d.", AttachmentExceptionMessages.DELETE_FAILED_MSG,
        CATEGORY_SERVICE_DOWN, 416),
        
    /** Validation failed: %s */
    INVALID_CHARACTERS("Validation failed: %s", AttachmentExceptionMessages.INVALID_CHARACTERS_MSG, CATEGORY_USER_INPUT, 418),
    
    /** An error occurred executing the search in the database. */
    SEARCH_PROBLEM("An error occurred executing the search in the database.", null, CATEGORY_ERROR, 420),
    
    /** Unable to access the filestore. */
    FILESTORE_DOWN("Unable to access the file store.", AttachmentExceptionMessages.FILESTORE_DOWN_MSG, CATEGORY_SERVICE_DOWN, 421),
    
    /** Writing to filestore failed. */
    FILESTORE_WRITE_FAILED("Writing to file store failed.", AttachmentExceptionMessages.SAVE_FAILED_MSG, CATEGORY_SERVICE_DOWN, 422),
    
    /** Changes done to the object this attachment was added to could not be undone. Your database is probably inconsistent, run the consistency tool. */
    UNDONE_FAILED("Changes done to the object this attachment was added to could not be undone. Your database is probably"
        + " inconsistent, run the consistency tool.", null, CATEGORY_ERROR, 600),
        
    /** An error occurred attaching to the given object. */
    ATTACH_FAILED("An error occurred attaching to the given object.", null, CATEGORY_ERROR, 601),
    
    /** The Object could not be detached because the update to an underlying object failed. */
    DETACH_FAILED("The object could not be detached because the update to an underlying object failed.", null, CATEGORY_ERROR, 602),
    
    /** Invalid parameter sent in request. Parameter '%1$s' was '%2$s' which does not look like a number. */
    INVALID_REQUEST_PARAMETER("Invalid parameter sent in request. Parameter '%1$s' was '%2$s' which does not look like a number.",
        AttachmentExceptionMessages.INVALID_REQUEST_PARAMETER_MSG, CATEGORY_USER_INPUT, 701),
        
    /** Conflicting services registered for context %1$i and folder %2$i */
    SERVICE_CONFLICT("Conflicting services registered for context %1$i and folder %2$i", null, CATEGORY_CONFIGURATION, 900);

    private String message;
    private String displayMessage;
    private Category category;
    private int number;
    
    private AttachmentExceptionCodes(String message, Category category, int number) {
        this(message, null, category, number);
    }

    private AttachmentExceptionCodes(String message, String displayMessage, Category category, int number) {
        this.message = message;
        this.displayMessage = displayMessage != null ? displayMessage : OXExceptionStrings.MESSAGE;
        this.category = category;
        this.number = number;
    }

    @Override
    public String getPrefix() {
        return EnumComponent.ATTACHMENT.getAbbreviation();
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
