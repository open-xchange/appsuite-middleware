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

package com.openexchange.chronos.provider.internal.share;

import static com.openexchange.chronos.provider.internal.Constants.QUALIFIED_ACCOUNT_ID;
import java.util.List;
import com.openexchange.chronos.exception.CalendarExceptionCodes;
import com.openexchange.exception.OXException;
import com.openexchange.tools.id.IDMangler;

/**
 * {@link IDMangling}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class IDMangling {

    /**
     * Initializes a new {@link IDMangling}.
     */
    private IDMangling() {
        super();
    }

    public static String optRelativeFolderId(String folderId) {
        try {
            return getRelativeFolderId(folderId);
        } catch (OXException e) {
            return null;
        }
    }

    public static String getUniqueFolderId(String folderId) {
        if (folderId.startsWith(QUALIFIED_ACCOUNT_ID)) {
            return folderId;
        }
        return QUALIFIED_ACCOUNT_ID + '/' + folderId;
    }

    public static String getRelativeFolderId(String folderId) throws OXException {
        if (null == folderId) {
            throw CalendarExceptionCodes.UNSUPPORTED_FOLDER.create(folderId, "");
        }
        try {
            return String.valueOf(Integer.parseInt(folderId));
        } catch (NumberFormatException e) {
            if (false == folderId.startsWith(QUALIFIED_ACCOUNT_ID)) {
                throw CalendarExceptionCodes.UNSUPPORTED_FOLDER.create(e, folderId, "");
            }
            List<String> components = IDMangler.unmangle(folderId);
            if (3 != components.size()) {
                throw CalendarExceptionCodes.UNSUPPORTED_FOLDER.create(folderId, "");
            }
            return getRelativeFolderId(components.get(2));
        }
    }

}
