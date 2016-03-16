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
