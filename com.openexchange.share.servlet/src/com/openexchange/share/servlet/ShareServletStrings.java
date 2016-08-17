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

    public static final String FILE = "file";

    public static final String FOLDER = "folder";

    // John Doe has shared the folder "Photos" with you. Please log in to view it.
    public static final String SHARE_WITH_TARGET = "%1$s has shared the %2$s \"%3$s\" with you. Please log in to view it. ";

    // We sent a message to john.doe@example.com with further instructions on how to set a new password.
    public static final String RESET_PASSWORD = "We sent a message to %1$s with further instructions on how to set a new password.";

    public static final String CHOOSE_PASSWORD = "Please set a new password to regain access.";

    public static final String SHARE_NOT_FOUND = "The share you are looking for does not exist.";

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
