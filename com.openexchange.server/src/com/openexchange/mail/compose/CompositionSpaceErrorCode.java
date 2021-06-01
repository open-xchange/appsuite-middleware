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

package com.openexchange.mail.compose;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 *
 * {@link CompositionSpaceErrorCode} - Error codes for composition space module.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.10.1
 */
public enum CompositionSpaceErrorCode implements DisplayableOXExceptionCode {

    /**
     * An error occurred: %1$s
     */
    ERROR("An error occurred: %1$s", null, CATEGORY_ERROR, 1),
    /**
     * An SQL error occurred: %1$s
     */
    SQL_ERROR("An SQL error occurred: %1$s", null, CATEGORY_ERROR, 2),
    /**
     * An I/O error occurred: %1$s
     */
    IO_ERROR("An I/O error occurred: %1$s", null, CATEGORY_ERROR, 3),
    /**
     * Unable to access the file storage
     */
    FILESTORE_DOWN("Unable to access the file storage", null, CATEGORY_ERROR, 4),
    /**
     * Found no suitable attachment storage
     */
    NO_ATTACHMENT_STORAGE("Found no suitable attachment storage", null, CATEGORY_ERROR, 5),
    /**
     * Found no such resource in attachment storage for identifier: %1$s
     */
    NO_SUCH_ATTACHMENT_RESOURCE("Found no such resource in attachment storage for identifier: %1$s", null, CATEGORY_ERROR, 6),
    /**
     * Found no such composition space for identifier: %1$s
     */
    NO_SUCH_COMPOSITION_SPACE("Found no such composition space for identifier: %1$s", null, CATEGORY_ERROR, 7),
    /**
     * The operation cannot be performed because composed message is not a reply.
     */
    NO_REPLY_FOR("The operation cannot be performed because composed message is not a reply.", CompositionSpaceExceptionMessages.NO_REPLY_FOR_MSG, CATEGORY_USER_INPUT, 8),
    /**
     * Found no such attachment %1$s in composition space %2$s
     */
    NO_SUCH_ATTACHMENT_IN_COMPOSITION_SPACE("Found no such attachment %1$s in composition space %2$s", CompositionSpaceExceptionMessages.NO_SUCH_ATTACHMENT_IN_COMPOSITION_SPACE_MSG, CATEGORY_ERROR, 9),
    /**
     * Concurrent Update Exception.
     */
    CONCURRENT_UPDATE("Concurrent Update Exception.", null, CATEGORY_ERROR, 10),
    /**
     * Maximum number of composition spaces is reached: %1$s
     */
    MAX_NUMBER_OF_COMPOSITION_SPACE_REACHED("Maximum number of composition spaces is reached: %1$s", CompositionSpaceExceptionMessages.MAX_NUMBER_OF_COMPOSITION_SPACE_REACHED_MSG, CATEGORY_USER_INPUT, 11),
    /**
     * Found no suitable key storage
     */
    NO_KEY_STORAGE("Found no suitable key storage", null, CATEGORY_ERROR, 12),
    /**
     * Found no suitable key for composition space %1$s
     */
    MISSING_KEY("Found no suitable key for composition space %1$s", CompositionSpaceExceptionMessages.MISSING_KEY_MSG, CATEGORY_TRY_AGAIN, 13),
    /**
     * Composition space could not be opened
     */
    OPEN_FAILED("Composition space could not be opened", null, CATEGORY_ERROR, 14),
    /**
     * The entered subject is too long. Please use a shorter one.
     */
    SUBJECT_TOO_LONG("The entered subject is too long. Please use a shorter one.", CompositionSpaceExceptionMessages.SUBJECT_TOO_LONG_MSG, CATEGORY_USER_INPUT, 15),
    /**
     * The entered "From" address is too long. Please use a shorter one.
     */
    FROM_TOO_LONG("The entered \"From\" address is too long.", CompositionSpaceExceptionMessages.FROM_TOO_LONG_MSG, CATEGORY_USER_INPUT, 16),
    /**
     * The entered "Sender" address is too long. Please use a shorter one.
     */
    SENDER_TOO_LONG("The entered \"Sender\" address is too long.", CompositionSpaceExceptionMessages.SENDER_TOO_LONG_MSG, CATEGORY_USER_INPUT, 17),
    /**
     * The entered "To" addresses are too long. Please use a shorter one.
     */
    TO_TOO_LONG("The entered \"To\" addresses are too long.", CompositionSpaceExceptionMessages.TO_TOO_LONG_MSG, CATEGORY_USER_INPUT, 18),
    /**
     * The entered "Cc" addresses are too long. Please use a shorter one.
     */
    CC_TOO_LONG("The entered \"Cc\" addresses are too long.", CompositionSpaceExceptionMessages.CC_TOO_LONG_MSG, CATEGORY_USER_INPUT, 19),
    /**
     * The entered "Bcc" addresses are too long. Please use a shorter one.
     */
    BCC_TOO_LONG("The entered \"Bcc\" addresses are too long.", CompositionSpaceExceptionMessages.BCC_TOO_LONG_MSG, CATEGORY_USER_INPUT, 20),
    /**
     * The entered "Reply-To" address is too long. Please use a shorter one.
     */
    REPLY_TO_TOO_LONG("The entered \"Reply-To\" address is too long.", CompositionSpaceExceptionMessages.REPLY_TO_TOO_LONG_MSG, CATEGORY_USER_INPUT, 21),
    /**
     * The folder "%1$s" containing the attachments to share does no more exist for composition space %2$s.
     */
    MISSING_SHARED_ATTACHMENTS_FOLDER("The folder \"%1$s\" containing the attachments to share does no more exist for composition space %2$s.", CompositionSpaceExceptionMessages.MISSING_SHARED_ATTACHMENTS_FOLDER_MSG, CATEGORY_WARNING, 22),
    /**
     * Available attachments in shared attachments folder "%1$s" are inconsistent with the ones held by composition space %2$s.
     */
    INCONSISTENT_SHARED_ATTACHMENTS("Available attachments in shared attachments folder \"%1$s\" are inconsistent with the ones held by composition space %2$s.", CompositionSpaceExceptionMessages.INCONSISTENT_SHARED_ATTACHMENTS_MSG, CATEGORY_WARNING, 23),
    /**
     * Failed to retrieve resource from attachment storage with identifier: %1$s
     */
    FAILED_RETRIEVAL_ATTACHMENT_RESOURCE("Found no such resource in attachment storage for identifier: %1$s", null, CATEGORY_ERROR, 24),
    /**
     * Failed to retrieve key for composition space with identifier: %1$s
     */
    FAILED_RETRIEVAL_KEY("Failed to retrieve key for composition space with identifier: %1$s", null, CATEGORY_ERROR, 25),

    ;

    private static final String PREFIX = "MSGCS";

    private final String message;
    private final String displayMessage;
    private final Category category;
    private final int number;

    private CompositionSpaceErrorCode(String message, String displayMessage, Category category, int number) {
        this.message = message;
        this.displayMessage = displayMessage != null ? displayMessage : OXExceptionStrings.MESSAGE;
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
