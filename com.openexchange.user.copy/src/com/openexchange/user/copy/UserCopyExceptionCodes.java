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

package com.openexchange.user.copy;

import com.openexchange.exception.Category;
import com.openexchange.exception.DisplayableOXExceptionCode;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionFactory;
import com.openexchange.exception.OXExceptionStrings;

/**
 * Lists all possible exceptions that can occur when a user is moved.
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public enum UserCopyExceptionCodes implements DisplayableOXExceptionCode {

    /** Unable to determine next update task to execute. Enqueued: %1$s. To sort: %2$s. */
    UNRESOLVABLE_DEPENDENCIES("Unable to determine next copy task to execute. Enqueued: %1$s. To sort: %2$s.", Category.CATEGORY_ERROR, 1,
        null),

    /** SQL Problem. */
    SQL_PROBLEM("SQL problem.", Category.CATEGORY_ERROR, 2, OXExceptionStrings.SQL_ERROR_MSG),

    /** Severe problem occurred. */
    UNKNOWN_PROBLEM("Unexpected problem occurred.", Category.CATEGORY_ERROR, 3, null),

    /** Problem with UserService. */
    USER_SERVICE_PROBLEM("Problem with UserService.", Category.CATEGORY_ERROR, 4, null),

    /** A private folder (%1$s) without existing parent (%2$s) was found. */
    MISSING_PARENT_FOLDER("A private folder (%1$s) without existing parent (%2$s) was found.", Category.CATEGORY_ERROR, 5,null),

    /** Database pooling error. */
    DB_POOLING_PROBLEM("Database pooling error.", Category.CATEGORY_ERROR, 6, OXExceptionStrings.SQL_ERROR_MSG),

    /** Problem with FileStorage. */
    FILE_STORAGE_PROBLEM("Problem with FileStorage.", Category.CATEGORY_ERROR, 7, null),

    /** Could not generate a new sequence id for type %1$s. */
    ID_PROBLEM("Could not generate a new sequence id for type %1$s.", Category.CATEGORY_ERROR, 8, null),

    /** Did not find contact for user %1$s in context %2$s. */
    USER_CONTACT_MISSING("Did not find contact for user %1$s in context %2$s.", Category.CATEGORY_ERROR, 9, null),

    /** Could not save user's mail settings. */
    SAVE_MAIL_SETTINGS_PROBLEM("Could not save user's mail settings.", Category.CATEGORY_ERROR, 10, null),

     /** A user named %1$s already exists in destination context %2$s. */
    USER_ALREADY_EXISTS("A user named %1$s already exists in destination context %2$s.", Category.CATEGORY_USER_INPUT, 11,
        UserCopyExceptionMessages.USER_ALREADY_EXISTS_MSG),

    /** The user's files are owned by user %1$s in source context %2$s. Please set individual or context-associated file storage first. */
    FILE_STORAGE_CONFLICT("The user's files are owned by user %1$s in source context %2$s. Please set individual or context-associated file storage first.", Category.CATEGORY_USER_INPUT, 12,
       UserCopyExceptionMessages.FILE_STORAGE_CONFLICT_MSG),

    /** The user %1$s in source context %2$s does use Unified Quota and therefore cannot be copied. */
    UNIFIED_QUOTA_CONFLICT("The user %1$s in source context %2$s does use Unified Quota and therefore cannot be copied.", Category.CATEGORY_USER_INPUT, 13,
       UserCopyExceptionMessages.UNIFIED_QUOTA_CONFLICT_MSG),
    ;

    private static final String PREFIX = "UCP";
    
    /**
     * Gets the <code>"UCP"</code> prefix
     *
     * @return The <code>"UCP"</code> prefix
     */
    public static String prefix() {
        return PREFIX;
    }
    
    private final String message;
    private final Category category;
    private final int number;
    private String displayMessage;


    private UserCopyExceptionCodes(final String message, final Category category, final int number, String displayMessage) {
        this.message = message;
        this.category = category;
        this.number = number;
        this.displayMessage = displayMessage != null ? displayMessage : OXExceptionStrings.MESSAGE;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public Category getCategory() {
        return category;
    }

    @Override
    public String getDisplayMessage() {
        return displayMessage;
    }

    public OXException create(final Object... args) {
        return OXExceptionFactory.getInstance().create(this, args);
    }

    public OXException create(final Throwable cause, final Object... args) {
        return OXExceptionFactory.getInstance().create(this, cause, args);
    }

    @Override
    public boolean equals(final OXException e) {
        return OXExceptionFactory.getInstance().equals(this, e);
    }

    @Override
    public int getNumber() {
        return number;
    }

    @Override
    public String getPrefix() {
        return PREFIX;
    }
}
