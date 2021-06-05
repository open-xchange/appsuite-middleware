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

package com.openexchange.i18n.misc;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link Messages} - A collection of arbitrary strings that are ought to be translated.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.4
 */
public class Messages implements LocalizableStrings {

    /**
     * Initializes a new {@link Messages}.
     */
    private Messages() {
        super();
    }

    public static final String USER_HAS_MENTIONED_YOU_SUBJECT = "%1$s has mentioned you in a document \"%2$s\"";

    // <Fullusername or empty><(or empty)<useremail><) or empty> has mentioned you in a comment in the following document:
    public static final String USER_HAS_MENTIONED_YOU = "%1$s (%2$s) has mentioned you in a comment in the following document:";

    public static final String CLICK_BUTTON = "Please click the button below to open the document.";

    public static final String CLICK_BUTTON_LABEL = "Go to comment";

}
