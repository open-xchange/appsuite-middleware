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

package com.openexchange.groupware.settings;

import com.openexchange.exception.OXException;

/**
 * This class represents a single setting.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public interface Setting extends Ranked {

    /**
     * @return the multi value.
     */
    Object[] getMultiValue();

    boolean isEmptyMultivalue();

    /**
     * @return the single value.
     */
    Object getSingleValue();

    /**
     * @param value The value to set.
     * @throws OXException if setting the value is not allowed on this element.
     */
    void setSingleValue(final Object value) throws OXException;

    /**
     * @param value Value to add.
     * @throws OXException if setting the value is not allowed on this element.
     */
    void addMultiValue(final Object value) throws OXException;

    /**
     * @throws OXException if setting the value is not allowed on this element.
     */
    void setEmptyMultiValue() throws OXException;

    /**
     * @return Returns the name.
     */
    String getName();

    /**
     * Returns the sub setting that has the given name.
     * @param elementName Name of the sub setting.
     * @return the sub setting or <code>null</code> if it doesn't exist.
     */
    Setting getElement(final String elementName);

    /**
     * @return Returns the id.
     */
    int getId();

    /**
     * @return Returns the leaf.
     */
    boolean isLeaf();

    /**
     * Removes the sub element from this element.
     * @param child sub element to remove.
     * @throws OXException if removing the child is not allowed on this element.
     */
    void removeElement(final Setting child) throws OXException;

    /**
     * @return the sub elements of this element.
     */
    Setting[] getElements();

    /**
     * @return <code>true</code> if this setting is used in server and gui and
     * <code>false</code> if the setting is only used in gui.
     */
    boolean isShared();

    /**
     * @return the path for this setting.
     */
    String getPath();

    /**
     * @return the parent
     */
    Setting getParent();

    /**
     * @return the shared
     */
    IValueHandler getShared();

}
