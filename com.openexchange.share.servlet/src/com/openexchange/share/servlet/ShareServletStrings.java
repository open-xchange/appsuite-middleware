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

package com.openexchange.share.servlet;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link ShareServletStrings}
 *
 * @author <a href="mailto:jan.bauerdick@open-xchange.com">Jan Bauerdick</a>
 * @since v7.8.0
 */
public class ShareServletStrings implements LocalizableStrings {

    public static final String GUEST = "guest";

    // John Doe has shared the folder "Photos" with you. Please enter the share password to view it.
    public static final String SHARE_FOLDER_WITH_TARGET = "%1$s has shared the folder \"%2$s\" with you. Please enter the share password to view it.";

    // John Doe has shared the file "document.pdf" with you. Please enter the share password to view it.
    public static final String SHARE_FILE_WITH_TARGET = "%1$s has shared the file \"%2$s\" with you. Please enter the share password to view it.";

    // John Doe has shared the folder "Photos" with you. Please enter your password to view it.
    public static final String SHARE_FOLDER_WITH_TARGET_AND_GUEST_PASSWORD = "%1$s has shared the folder \"%2$s\" with you. Please enter your password to view it.";

    // John Doe has shared the file "document.pdf" with you. Please enter your password to view it.
    public static final String SHARE_FILE_WITH_TARGET_AND_GUEST_PASSWORD = "%1$s has shared the file \"%2$s\" with you. Please enter your password to view it.";

    // The folder "Photos" is shared with you. Please enter the share password to view it.
    public static final String SHARE_FOLDER_WITH_TARGET_UNKNOWN_SHARING_USER = "The folder \"%1$s\" is shared with you. Please enter the share password to view it.";

    // The file "document.pdf" is shared with you. Please enter the share password to view it.
    public static final String SHARE_FILE_WITH_TARGET_UNKNOWN_SHARING_USER = "The file \"%1$s\" is shared with you. Please enter the share password to view it.";

    // The folder "Photos" is shared with you. Please enter your password to view it.
    public static final String SHARE_FOLDER_WITH_TARGET_AND_GUEST_PASSWORD_UNKNOWN_SHARING_USER = "The folder \"%1$s\" is shared with you. Please enter your password to view it.";

    // The file "document.pdf" is shared with you. Please enter your password to view it.
    public static final String SHARE_FILE_WITH_TARGET_AND_GUEST_PASSWORD_UNKNOWN_SHARING_USER = "The file \"%1$s\" is shared with you. Please enter your password to view it.";

    // Login when opening a share protected with a password
    public static final String SHARE_PASSWORD = "Please enter your password to log into this account.";

    // We sent a message to john.doe@example.com with further instructions on how to set a new password.
    public static final String RESET_PASSWORD = "We sent a message to %1$s with further instructions on how to set a new password.";

    public static final String CHOOSE_PASSWORD = "Please set a new password to regain access.";

    public static final String SHARE_NOT_FOUND = "The share you are looking for does not exist.";

    public static final String SHARE_NOT_ACCESSIBLE = "This share cannot be accessed with your Web Browser.";

    public static final String INVALID_REQUEST = "We were unable to process your request.";

    public static final String INVALID_TARGET_WITHOUT_PASSWORD = "The folder or item you are looking for is no longer available. Please click \"continue\" to view your other shares.";

    public static final String NO_GUEST_PASSWORD_REQUIRED = "Your guest account is currently not protected with a password. Resetting password will be aborted.";

    // Access to the file you're looking for has been removed. Please contact the owner if you want access again.
    public static final String NO_ACCESS_TO_FILE_CONTACT_OWNER = "Access to the file you're looking for has been removed. Please contact the owner if you want access again.";

    // Access to the file you're looking for has been removed. Please contact <John Doe> if you want access again.
    public static final String NO_ACCESS_TO_FILE_CONTACT_PERSON = "Access to the file you're looking for has been removed. Please contact %1$s if you want access again.";

    // Access to the folder you're looking for has been removed. Please contact the owner if you want access again.
    public static final String NO_ACCESS_TO_FOLDER_CONTACT_OWNER = "Access to the folder you're looking for has been removed. Please contact the owner if you want access again.";

    // Access to the folder you're looking for has been removed. Please contact <John Doe> if you want access again.
    public static final String NO_ACCESS_TO_FOLDER_CONTACT_PERSON = "Access to the folder you're looking for has been removed. Please contact %1$s if you want access again.";

    // Access to the share you're looking for has been removed. Please contact the owner if you want access again.
    public static final String NO_ACCESS_TO_SHARE_CONTACT_OWNER = "Access to the share you're looking for has been removed. Please contact the owner if you want access again.";

    // Access to the share you're looking for has been removed. Please contact <John Doe> if you want access again.
    public static final String NO_ACCESS_TO_SHARE_CONTACT_PERSON = "Access to the share you're looking for has been removed. Please contact %1$s if you want access again.";

    // Access to the file you're looking for has been removed. Please contact the owner if you want access again. To view your other shared files, click "Continue".
    public static final String NO_ACCESS_TO_FILE_CONTACT_OWNER_CONTINUE = "Access to the file you're looking for has been removed. Please contact the owner if you want access again. To view your other shared files, click \"Continue\".";

    // Access to the file you're looking for has been removed. Please contact <John Doe> if you want access again. To view your other shared files, click "Continue".
    public static final String NO_ACCESS_TO_FILE_CONTACT_PERSON_CONTINUE = "Access to the file you're looking for has been removed. Please contact %1$s if you want access again. To view your other shared files, click \"Continue\".";

    // Access to the folder you're looking for has been removed. Please contact the owner if you want access again. To view your other shared files, click "Continue".
    public static final String NO_ACCESS_TO_FOLDER_CONTACT_OWNER_CONTINUE = "Access to the folder you're looking for has been removed. Please contact the owner if you want access again. To view your other shared files, click \"Continue\".";

    // Access to the folder you're looking for has been removed. Please contact <John Doe> if you want access again. To view your other shared files, click "Continue".
    public static final String NO_ACCESS_TO_FOLDER_CONTACT_PERSON_CONTINUE = "Access to the folder you're looking for has been removed. Please contact %1$s if you want access again. To view your other shared files, click \"Continue\".";

    // Access to the share you're looking for has been removed. Please contact the owner if you want access again. To view your other shared files, click "Continue".
    public static final String NO_ACCESS_TO_SHARE_CONTACT_OWNER_CONTINUE = "Access to the share you're looking for has been removed. Please contact the owner if you want access again. To view your other shared files, click \"Continue\".";

    // Access to the share you're looking for has been removed. Please contact <John Doe> if you want access again. To view your other shared files, click "Continue".
    public static final String NO_ACCESS_TO_SHARE_CONTACT_PERSON_CONTINUE = "Access to the share you're looking for has been removed. Please contact %1$s if you want access again. To view your other shared files, click \"Continue\".";

    // Access to the file you're looking for has been removed. Please contact the owner if you want access again. To view your other shared files, please log in.
    public static final String NO_ACCESS_TO_FILE_CONTACT_OWNER_LOG_IN = "Access to the file you're looking for has been removed. Please contact the owner if you want access again. To view your other shared files, please log in.";

    // Access to the file you're looking for has been removed. Please contact <John Doe> if you want access again. To view your other shared files, please log in.
    public static final String NO_ACCESS_TO_FILE_CONTACT_PERSON_LOG_IN = "Access to the file you're looking for has been removed. Please contact %1$s if you want access again. To view your other shared files, please log in.";

    // Access to the folder you're looking for has been removed. Please contact the owner if you want access again. To view your other shared files, please log in.
    public static final String NO_ACCESS_TO_FOLDER_CONTACT_OWNER_LOG_IN = "Access to the folder you're looking for has been removed. Please contact the owner if you want access again. To view your other shared files, please log in.";

    // Access to the folder you're looking for has been removed. Please contact <John Doe> if you want access again. To view your other shared files, please log in.
    public static final String NO_ACCESS_TO_FOLDER_CONTACT_PERSON_LOG_IN = "Access to the folder you're looking for has been removed. Please contact %1$s if you want access again. To view your other shared files, please log in.";

    // Access to the share you're looking for has been removed. Please contact the owner if you want access again. To view your other shared files, please log in.
    public static final String NO_ACCESS_TO_SHARE_CONTACT_OWNER_LOG_IN = "Access to the share you're looking for has been removed. Please contact the owner if you want access again. To view your other shared files, please log in.";

    // Access to the share you're looking for has been removed. Please contact <John Doe> if you want access again. To view your other shared files, please log in.
    public static final String NO_ACCESS_TO_SHARE_CONTACT_PERSON_LOG_IN = "Access to the share you're looking for has been removed. Please contact %1$s if you want access again. To view your other shared files, please log in.";

}
