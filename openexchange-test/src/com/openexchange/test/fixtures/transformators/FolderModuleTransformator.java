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

package com.openexchange.test.fixtures.transformators;

import static com.openexchange.java.Autoboxing.I;
import com.openexchange.ajax.Folder;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link FolderModuleTransformator}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class FolderModuleTransformator implements Transformator {

    @Override
    public Object transform(String value) throws OXException {
        if (value.equalsIgnoreCase(Folder.MODULE_CALENDAR)) {
            return I(FolderObject.CALENDAR);
        }
        if (value.equalsIgnoreCase(Folder.MODULE_CONTACT)) {
            return I(FolderObject.CONTACT);
        }
        if (value.equalsIgnoreCase(Folder.MODULE_INFOSTORE)) {
            return I(FolderObject.INFOSTORE);
        }
        if (value.equalsIgnoreCase(Folder.MODULE_MAIL)) {
            return I(FolderObject.MAIL);
        }
        if (value.equalsIgnoreCase(Folder.MODULE_TASK)) {
            return I(FolderObject.TASK);
        }
        if (value.equalsIgnoreCase(Folder.MODULE_SYSTEM)) {
            return I(FolderObject.SYSTEM_TYPE);
        }
        return Integer.valueOf(value);
    }

}
