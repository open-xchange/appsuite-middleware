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

package com.openexchange.tools.oxfolder;

import com.openexchange.exception.OXException;

/**
 * OXFolderPermissionException
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class OXFolderPermissionException extends OXException {

    private static final long serialVersionUID = -5108199975949161729L;

    private static final transient Object EMPTY_ARGS = new Object[0];

    public OXFolderPermissionException(final OXFolderExceptionCode code) {
        this(code, EMPTY_ARGS);
    }

    public OXFolderPermissionException(final OXFolderExceptionCode code, final Object... messageArgs) {
        this(code, null, messageArgs);
    }

    public OXFolderPermissionException(final OXFolderExceptionCode code, final Exception cause, final Object... messageArgs) {
        super();
        copyFrom(OXException.noPermissionForFolder());
        setLogMessage(code.getMessage(), messageArgs);
    }

}
