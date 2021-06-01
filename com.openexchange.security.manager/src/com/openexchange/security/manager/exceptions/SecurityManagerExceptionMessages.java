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

package com.openexchange.security.manager.exceptions;

import com.openexchange.i18n.LocalizableStrings;

/**
 * {@link SecurityManagerExceptionMessages}
 *
 * @author <a href="mailto:greg.hill@open-xchange.com">Greg Hill</a>
 * @since v7.10.3
 */
public class SecurityManagerExceptionMessages implements LocalizableStrings {

    // Problem parsing the security policy file
    public static final String PROBLEM_POLICY_FILE = "Problem parsing security policy file: %1$s";

    // Error when updating the security manager
    public static final String PROBLEM_UPDATING_SECURITY_POLICIES = "Problem updating security manager.  %1$s";

    public SecurityManagerExceptionMessages() {
        super();
    }

}
