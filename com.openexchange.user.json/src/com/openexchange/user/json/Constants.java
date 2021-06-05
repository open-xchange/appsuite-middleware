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

package com.openexchange.user.json;

import com.openexchange.groupware.container.FolderObject;

/**
 * {@link Constants} - Constants for the HTTP JSON interface of the user component.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class Constants {

    /**
     * The module (appendix to servlet path).
     */
    public static final String MODULE = "user";

    /**
     * The module (appendix to servlet path).
     */
    public static final String MODULE_ME = "user/me";

    /**
     * The servlet path.
     */
    public static final String SERVLET_PATH_APPENDIX = MODULE;

    /**
     * The user address book folder identifier.
     */
    public static final int USER_ADDRESS_BOOK_FOLDER_ID = FolderObject.SYSTEM_LDAP_FOLDER_ID;

    private Constants() {
        super();
    }
}
