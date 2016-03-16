/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
