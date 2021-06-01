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

package com.openexchange.mailfilter.exceptions;

import com.openexchange.i18n.LocalizableStrings;


/**
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class MailFilterExceptionMessages implements LocalizableStrings {

    public static final String INVALID_REDIRECT_ADDRESS_MSG = "The redirect address \"%1$s\" is not valid.";

    public static final String REJECTED_REDIRECT_ADDRESS_MSG = "The redirect address \"%1$s\" has been rejected.";

    public static final String INVALID_SIEVE_RULE_MSG = "Invalid SIEVE rule specified. Please review your mail filter rules";

    public static final String INVALID_SIEVE_RULE2_MSG = "Please review your mail filter rules as they seem to be invalid. Response from server: \"%1$s\"";

    public static final String MAILFILTER_NOT_AVAILABLE_MSG = "Managing mail filter rules is not available for your account.";

    public static final String TOO_MANY_REDIRECT_MSG = "The maximum amount of redirect rules is reached. Please delete or deactivate another redirect rule first.";

}
