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

package com.openexchange.xing.access;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link XingExceptionMessages} - Exception messages for errors that needs to be translated.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class XingExceptionMessages implements LocalizableStrings {

    // A XING error occurred: %1$s
    public static final String XING_ERROR_MSG = "Unexpected error returned by XING service.";

    // The XING resource does not exist: %1$s
    public static final String NOT_FOUND_MSG = "The XING resource does not exist: %1$s";

    // Update denied for XING resource: %1$s
    public static final String UPDATE_DENIED_MSG = "Update denied for XING resource: %1$s";

    // Delete denied for XING resource: %1$s
    public static final String DELETE_DENIED_MSG = "Delete denied for XING resource: %1$s";

    // Missing file name.
    public static final String MISSING_FILE_NAME_MSG = "Missing file name.";

    // Missing configuration for account "%1$s".
    public static final String MISSING_CONFIG_MSG = "Missing configuration for account \"%1$s\".";

    // Bad or expired access token. Need to re-authenticate user.
    public static final String UNLINKED_ERROR_MSG = "Bad or expired access token. Need to re-authenticate user.";

    // E-Mail address is invalid
    public static final String INVALID_EMAIL_ADDRESS_MSG = "E-Mail address is invalid";

    // Already sent an invitation to E-Mail address: \"%1$s\"
    public static final String ALREADY_INVITED_MSG = "Already sent an invitation to E-Mail address: \"%1$s\"";

    // The E-Mail address already belongs to a XING user
    public static final String ALREADY_MEMBER_MSG = "The E-Mail address already belongs to a XING user";

    // Invitation attempt failed for any reason
    public static final String INVITATION_FAILED_MSG = "Invitation attempt failed for any reason";

    // The E-Mail address does not belongs to a XING user
    public static final String NOT_A_MEMBER_MSG = "The E-Mail address does not belongs to a XING user";

    // XING user is already directly connected.
    public static final String ALREADY_CONNECTED_MSG = "XING user is already directly connected.";

    //The comment size exceeds the maximum allowed number of 600 characters.
    public static final String COMMENT_SIZE_EXCEEDED_MSG = "The comment size exceeds the maximum allowed number of 600 characters.";

    // The status message size exceeds the maximum allowed number of 600 characters.
    public static final String STATUS_MESSAGE_SIZE_EXCEEDED_MSG = "The status message size exceeds the maximum allowed number of 420 characters.";

    // The status message size exceeds the maximum allowed number of 600 characters.
    public static final String TEXT_MESSAGE_SIZE_EXCEEDED_MSG = "The text message size exceeds the maximum allowed number of 140 characters.";

    // The mandatory parameter \"%1$s\ is missing in the request body.
    public static final String MANDATORY_PARAMETER_MISSING_MSG = "The mandatory parameter \"%1$s\" is missing in the request body";

    // The XING server is not available.
    public static final String XING_SERVER_UNAVAILABLE_MSG = "The XING server is not available.";

    // A XING account has already been requested for E-Mail address %1$s.
    public static final String LEAD_ALREADY_EXISTS_MSG = "A XING account has already been requested for E-Mail address %1$s.";

    // The XING app that you are using does not hold the required permissions in order to perform the requested action
    public static final String INSUFFICIENT_PRIVILEGES_MSG = "The XING app that you are using does not hold the required permissions in order to perform the requested action";

    // No XING OAuth access available for user %1$s in context %2$s.
    public static final String NO_OAUTH_ACCOUNT_MSG = "No XING OAuth access available. Please create a XING OAuth account.";

    /**
     * Initializes a new {@link XingExceptionMessages}.
     */
    private XingExceptionMessages() {
        super();
    }

}
