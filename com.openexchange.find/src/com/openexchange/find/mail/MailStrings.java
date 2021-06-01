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

package com.openexchange.find.mail;

import com.openexchange.i18n.LocalizableStrings;


/**
 * Mail-specific strings are potentially displayed in client applications and
 * should therefore be localized.
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public class MailStrings implements LocalizableStrings {

    // Context: Searching in mail.
    // Displayed as: [Search for] 'user input' in subject.
    // The 'user input' part is always prepended, please heed this in translations.
    public static final String FACET_SUBJECT = "in subject";

    // Context: Searching in mail.
    // Displayed as: [Search for] 'user input' in mail text.
    // The 'user input' part is always prepended, please heed this in translations.
    public static final String FACET_MAIL_TEXT = "in mail text";

    // Search for mail conversations by people
    public static final String FACET_PEOPLE = "People";

    // Search for mails sent from or to ...
    public static final String FACET_FROM_AND_TO = "From/To";

    // Search for mails sent from ...
    public static final String FACET_FROM = "From";

    // Search for mails sent to ...
    public static final String FACET_TO = "To";

    public static final String FACET_FILENAME_NAME = "in attachment file names";

    // Has attachment
    public static final String FACET_ATTACHMENT = "Attachments";

    public static final String HAS_ATTACHMENT_TRUE = "Has attachment";

    public static final String HAS_ATTACHMENT_FALSE = "Has no attachment";

}
