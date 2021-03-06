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

package com.openexchange.groupware.upload.impl;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * {@link UploadException} - Indicates an error during an upload.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class UploadException extends OXException {

    private static final long serialVersionUID = 8590042770250274015L;

    /**
     * The upload error code enumeration.
     */
    public static enum UploadCode implements DisplayableOXExceptionCode {
        /**
         * File upload failed: %1$s
         */
        UPLOAD_FAILED("File upload failed: %1$s", UploadExceptionMessage.UPLOAD_FAILED_MSG, CATEGORY_ERROR, 1),
        /**
         * Missing affiliation id
         */
        MISSING_AFFILIATION_ID("Missing affiliation id", UploadExceptionMessage.MISSING_AFFILIATION_ID_MSG, CATEGORY_ERROR, 2),
        /**
         * Unknown action value: %1$s
         */
        UNKNOWN_ACTION_VALUE("Unknown action value: %1$s", UploadExceptionMessage.UNKNOWN_ACTION_VALUE_MSG, CATEGORY_ERROR, 3),
        /**
         * Header "content-type" does not indicate multipart content
         */
        NO_MULTIPART_CONTENT("Header \"content-type\" does not indicate multipart content", UploadExceptionMessage.NO_MULTIPART_CONTENT_MSG,
            CATEGORY_ERROR, 4),
        /**
         * Request rejected because its size (%1$s) exceeds the maximum configured size of %2$s
         */
        MAX_UPLOAD_SIZE_EXCEEDED("Request rejected because its size (%1$s) exceeds the maximum configured size of %2$s",
            UploadExceptionMessage.MAX_UPLOAD_SIZE_EXCEEDED_MSG, CATEGORY_USER_INPUT, 5),
        /**
         * Request rejected because its size exceeds the maximum configured size of %1$s
         */
        MAX_UPLOAD_SIZE_EXCEEDED_UNKNOWN("Request rejected because its size exceeds the maximum configured size of %1$s",
            UploadExceptionMessage.MAX_UPLOAD_SIZE_EXCEEDED_UNKNOWN_MSG, CATEGORY_USER_INPUT, 5),
        /**
         * Missing parameter %1$s
         */
        MISSING_PARAM("Missing parameter %1$s", UploadExceptionMessage.MISSING_PARAM_MSG, CATEGORY_ERROR, 6),
        /**
         * Unknown module: %1$d
         */
        UNKNOWN_MODULE("Unknown module: %1$d", UploadExceptionMessage.UNKNOWN_MODULE_MSG, CATEGORY_ERROR, 7),
        /**
         * An uploaded file referenced by %1$s could not be found
         */
        UPLOAD_FILE_NOT_FOUND("An uploaded file referenced by %1$s could not be found", UploadExceptionMessage.UPLOAD_FILE_NOT_FOUND_MSG,
            CATEGORY_USER_INPUT, 8),
        /**
         * Invalid action value: %1$s
         */
        INVALID_ACTION_VALUE("Invalid action value: %1$s", UploadExceptionMessage.INVALID_ACTION_VALUE_MSG, CATEGORY_ERROR, 9),
        /**
         * Upload file with id %1$s could not be found
         */
        FILE_NOT_FOUND("Upload file with id %1$s could not be found", UploadExceptionMessage.FILE_NOT_FOUND_MSG, CATEGORY_USER_INPUT, 10),
        /**
         * Upload file's content type "%1$s" does not fit to given file filter "%2$s"
         */
        INVALID_FILE_TYPE("Upload file's content type \"%1$s\" does not match given file filter \"%2$s\"", UploadExceptionMessage.INVALID_FILE_TYPE_MSG, CATEGORY_USER_INPUT, 11),
        /**
         * Upload file is invalid or illegal
         */
        INVALID_FILE("Upload file is invalid or illegal", UploadExceptionMessage.INVALID_FILE_MSG, CATEGORY_USER_INPUT, 11),
        /**
         * An error occurred: %1$s
         */
        UNEXPECTED_ERROR("An error occurred: %1$s", OXExceptionStrings.MESSAGE, CATEGORY_ERROR, 12),
        /**
         * Connection has been closed unexpectedly. Please try again.
         */
        UNEXPECTED_EOF("Connection has been closed unexpectedly. Please try again.", UploadExceptionMessage.UNEXPECTED_EOF_MSG, CATEGORY_TRY_AGAIN, 13),
        /**
         * Request rejected because file size (%1$s) exceeds the maximum configured file size of %2$s
         */
        MAX_UPLOAD_FILE_SIZE_EXCEEDED("Request rejected because file size (%1$s) exceeds the maximum configured file size of %2$s",
            UploadExceptionMessage.MAX_UPLOAD_FILE_SIZE_EXCEEDED_MSG, CATEGORY_USER_INPUT, 14),
        /**
         * Request rejected because file size exceeds the maximum configured file size of %1$s
         */
        MAX_UPLOAD_FILE_SIZE_EXCEEDED_UNKNOWN("Request rejected because file size exceeds the maximum configured file size of %1$s",
            UploadExceptionMessage.MAX_UPLOAD_FILE_SIZE_EXCEEDED_UNKNOWN_MSG, CATEGORY_USER_INPUT, 14),
        /**
         * Read timed out. Waited too long for incoming data from client.
         */
        UNEXPECTED_TIMEOUT("Read timed out. Waited too long for incoming data from client.", UploadExceptionMessage.UNEXPECTED_TIMEOUT_MSG, CATEGORY_ERROR, 15),
        /**
         * Image upload denied. Size is too big. (current=%1$s, max=%2$s)
         */
        IMAGE_TOO_BIG("Image upload denied. Size is too big. (current=%1$s, max=%2$s)", UploadExceptionMessage.IMAGE_TOO_BIG_MSG, CATEGORY_USER_INPUT, 16),
        /**
         * Image upload denied. Resolution is too high. (current=%1$s, max=%2$s)
         */
        IMAGE_RESOLUTION_TOO_HIGH("Image upload denied. Resolution is too high. (current=%1$s, max=%2$s)", UploadExceptionMessage.IMAGE_RESOLUTION_TOO_HIGH_MSG, CATEGORY_USER_INPUT, 17),
        /**
         * Failed to stream-wise processes request's upload
         * <p>
         * This error's {@link OXException#getArgument(String) arguments} contains the legacy <code>com.openexchange.groupware.upload.impl.UploadEvent</code> referenced by name <code>"__uploadEvent"</code> as fall-back
         */
        FAILED_STREAMED_UPLOAD("Failed to stream-wise processes request's upload", OXExceptionStrings.MESSAGE, CATEGORY_ERROR, 18)

        ;

        private static final String PREFIX = "UPL";

        /**
         * Gets the prefix.
         *
         * @return The prefix
         */
        public static String prefix() {
            return PREFIX;
        }

        private final String message;

        private final String displayMessage;

        private final Category category;

        private final int detailNumber;

        private UploadCode(final String message, final String displayMessage, final Category category, final int detailNumber) {
            this.message = message;
            this.displayMessage = displayMessage != null ? displayMessage : OXExceptionStrings.MESSAGE;
            this.category = category;
            this.detailNumber = detailNumber;
        }

        @Override
        public String getPrefix() {
            return PREFIX;
        }

        @Override
        public final Category getCategory() {
            return category;
        }

        @Override
        public final int getNumber() {
            return detailNumber;
        }

        @Override
        public final String getMessage() {
            return message;
        }

        @Override
        public String getDisplayMessage() {
            return displayMessage;
        }

        @Override
        public boolean equals(final OXException e) {
            return OXExceptionFactory.getInstance().equals(this, e);
        }

        /**
         * Creates a new {@link UploadException} instance pre-filled with this code's attributes.
         *
         * @return The newly created {@link UploadException} instance
         */
        public UploadException create() {
            return create(new Object[0]);
        }

        /**
         * Creates a new {@link UploadException} instance pre-filled with this code's attributes.
         *
         * @param args The message arguments in case of printf-style message
         * @return The newly created {@link UploadException} instance
         */
        public UploadException create(final Object... args) {
            return create(null, args);
        }

        /**
         * Creates a new {@link UploadException} instance pre-filled with this code's attributes.
         *
         * @param cause The optional initial cause
         * @param args The message arguments in case of printf-style message
         * @return The newly created {@link UploadException} instance
         */
        public UploadException create(final Throwable cause, final Object... args) {
            Category cat = category;
            UploadException ret = new UploadException(getNumber(), getDisplayMessage(), cause, args);
            ret.setLogMessage(getMessage(), args);

            // Apply rest
            ret.addCategory(cat).setPrefix(getPrefix()).setExceptionCode(this);

            return ret;
        }
    }

    private String action;

    /**
     * Initializes a new {@link UploadException}.
     *
     * @param code The code
     * @param displayMessage The display message
     * @param cause The cause
     * @param displayArgs The arguments for display message
     */
    protected UploadException(final int code, final String displayMessage, final Throwable cause, final Object... displayArgs) {
        super(code, displayMessage, cause, displayArgs);
    }

    /**
     * Sets the action string.
     *
     * @param action The action string
     * @return This exception with action string applied
     */
    public UploadException setAction(final String action) {
        this.action = action;
        return this;
    }

    /**
     * Gets the action string.
     *
     * @return The action string
     */
    public String getAction() {
        return action;
    }

}
