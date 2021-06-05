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

package com.openexchange.groupware.settings.impl;

import com.openexchange.exception.OXException;
import com.openexchange.groupware.settings.IValueHandler;
import com.openexchange.groupware.settings.Setting;
import com.openexchange.groupware.settings.SettingExceptionCodes;

/**
 * {@link TreeSetting}
 *
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 */
public class TreeSetting extends AbstractSetting<TreeSetting> {

    public TreeSetting(String name, int id, IValueHandler shared) {
        super(name, id, shared);
    }

    @Override
    public Object[] getMultiValue() {
        return null;
    }

    @Override
    public boolean isEmptyMultivalue() {
        return true;
    }

    @Override
    public Object getSingleValue() {
        return null;
    }

    @Override
    public void setSingleValue(Object value) throws OXException {
        throw SettingExceptionCodes.NOT_ALLOWED.create();
    }

    @Override
    public void addMultiValue(Object value) throws OXException {
        throw SettingExceptionCodes.NOT_ALLOWED.create();
    }

    @Override
    public void setEmptyMultiValue() throws OXException {
        throw SettingExceptionCodes.NOT_ALLOWED.create();
    }

    @Override
    public void removeElement(final Setting child) throws OXException {
        if (!(child instanceof TreeSetting)) {
            throw SettingExceptionCodes.NOT_ALLOWED.create();
        }
        removeElement((TreeSetting) child);
    }

    public void removeElement(final TreeSetting child) {
        removeElementInternal(child);
    }
}
