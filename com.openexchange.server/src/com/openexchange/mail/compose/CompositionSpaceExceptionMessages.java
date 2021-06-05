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

import com.openexchange.i18n.LocalizableStrings;

/**
 *
 * {@link CompositionSpaceExceptionMessages}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since 7.10.1
 */
public class CompositionSpaceExceptionMessages implements LocalizableStrings {

    /**
     * Initializes a new {@link CompositionSpaceExceptionMessages}.
     */
    private CompositionSpaceExceptionMessages() {
        super();
    }

    // This error message is returned to the user in case he/she wants to apply a change to an in-compose message,
    // which is only applicable in case a reply is generated, but in-compose message is not a reply
    public static final String NO_REPLY_FOR_MSG = "The operation cannot be performed because composed message is not a reply";

    // Found no such attachment %1$s in composition space %2$s
    public static final String NO_SUCH_ATTACHMENT_IN_COMPOSITION_SPACE_MSG = "No such attachment";

    // Attachments must not be shared
    public static final String SHARED_ATTACHMENTS_NOT_ALLOWED_MSG = "Attachments must not be shared";

    // A user must not create any new composition spaces
    public static final String MAX_NUMBER_OF_COMPOSITION_SPACE_REACHED_MSG = "Maximum number of composition spaces is reached. Please terminate existing open spaces in order to open new ones.";

    // Missing key which is required to decrypt the content of a composition space
    public static final String MISSING_KEY_MSG = "Found no suitable key for composition space. Please re-compose your E-Mail.";

    // The user entered a very long subject, which cannot be stored due to data truncation
    public static final String SUBJECT_TOO_LONG_MSG = "The entered subject is too long. Please use a shorter one.";

    // The user entered a very long From address, which cannot be stored due to data truncation
    public static final String FROM_TOO_LONG_MSG = "The entered \"from\" address is too long.";

    // The user entered a very long Sender address, which cannot be stored due to data truncation
    public static final String SENDER_TOO_LONG_MSG = "The entered \"sender\" address is too long.";

    // The user entered a very long To addresses, which cannot be stored due to data truncation
    public static final String TO_TOO_LONG_MSG = "The entered \"to\" addresses are too long.";

    // The user entered a very long Cc addresses, which cannot be stored due to data truncation
    public static final String CC_TOO_LONG_MSG = "The entered \"cc\" addresses are too long.";

    // The user entered a very long Bcc addresses, which cannot be stored due to data truncation
    public static final String BCC_TOO_LONG_MSG = "The entered \"bcc\" addresses are too long.";

    // The user entered a very long Reply-To address, which cannot be stored due to data truncation
    public static final String REPLY_TO_TOO_LONG_MSG = "The entered \"Reply-To\" address is too long.";

    // The user started a Drive Mail once and re-accesses the associated composition space. However, the folder containing the attachments to share (via a share link) does no more exist.
    public static final String MISSING_SHARED_ATTACHMENTS_FOLDER_MSG = "The folder containing the attachments to share no longer exists.";

    // The user started a Drive Mail once and re-accesses the associated composition space. However, there are either attachments not existing in composition space or there are references to attachments that do not exist
    public static final String INCONSISTENT_SHARED_ATTACHMENTS_MSG = "Available attachments in the folder containing the attachments to share are inconsistent.";

}
