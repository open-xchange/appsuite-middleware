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

package com.openexchange.importexport.helpers;

import static com.openexchange.java.Autoboxing.I;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.Types;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.importexport.exceptions.ImportExportExceptionCodes;

/**
 * This sad little class translates has the sad little task to translate between different constants that are used to identify types of
 * modules. So, in case yo
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 */
public final class ModuleTypeTranslator {

    private ModuleTypeTranslator() {
        super();
    }

    /**
     * Translates a FolderObject value to a Types value.
     */
    public static int getTypesConstant(final int folderObjectConstant) throws OXException {
        switch (folderObjectConstant) {
        case FolderObject.CONTACT:
            return Types.CONTACT;
        case FolderObject.INFOSTORE:
            return Types.INFOSTORE;
        case FolderObject.MAIL:
            return Types.EMAIL;
        case FolderObject.TASK:
            return Types.TASK;
        case FolderObject.CALENDAR:
            return Types.APPOINTMENT;
        default:
            throw ImportExportExceptionCodes.NO_TYPES_CONSTANT.create(I(folderObjectConstant));
        }
    }

    /**
     * Translates a Types value to a FolderObject value
     */
    public static int getFolderObjectConstant(final int typeConstant) throws OXException {
        switch (typeConstant) {
        case Types.CONTACT:
            return FolderObject.CONTACT;
        case Types.INFOSTORE:
            return FolderObject.INFOSTORE;
        case Types.EMAIL:
            return FolderObject.MAIL;
        case Types.TASK:
            return FolderObject.TASK;
        case Types.APPOINTMENT:
            return FolderObject.CALENDAR;
        default:
            throw ImportExportExceptionCodes.NO_FOLDEROBJECT_CONSTANT.create(I(typeConstant));
        }
    }
}
