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

package com.openexchange.importexport.importers.ical;

import java.util.Map;
import com.openexchange.folderstorage.UserizedFolder;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractICalImporter}
 *
 * @author <a href="mailto:Jan-Oliver.Huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.10.0
 */
public abstract class AbstractICalImporter implements ICalImport {

    private ServerSession session;
    private UserizedFolder userizedFolder;

    public AbstractICalImporter(ServerSession session, UserizedFolder userizedFolder) {
        super();
        this.session = session;
        this.userizedFolder = userizedFolder;
    }

    /**
     * Gets a value whether the supplied parameters indicate that UIDs should be ignored during import or not.
     *
     * @param optionalParams The optional parameters as passed from the import request, may be <code>null</code>
     * @return <code>true</code> if UIDs should be ignored, <code>false</code>, otherwise
     */
    public static boolean isIgnoreUIDs(Map<String, String[]> optionalParams) {
        if (null != optionalParams) {
            String[] value = optionalParams.get("ignoreUIDs");
            if (null != value && 0 < value.length) {
                return Boolean.valueOf(value[0]).booleanValue();
            }
        }
        return false;
    }

    public ServerSession getSession() {
        return session;
    }

    public UserizedFolder getUserizedFolder() {
        return userizedFolder;
    }

}
