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

package com.openexchange.config.cascade;

import java.util.Map;
import com.openexchange.exception.OXException;

/**
 * {@link ConfigView} - A configuration view.
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> Added JavaDoc
 */
public interface ConfigView {

    /**
     * Sets denoted property
     *
     * @param scope The property's scope
     * @param property The property's name
     * @param value The property's value
     * @throws OXException If setting property fails for any reason
     */
    <T> void set(String scope, String property, T value) throws OXException;

    /**
     * Gets coerced property value.
     *
     * @param property The property name
     * @param coerceTo The type to coerce to
     * @return The coerced value or <code>null</code>
     * @throws OXException If such a property does not exist
     */
    <T> T get(String property, Class<T> coerceTo) throws OXException;

    /**
     * (Optionally) Gets coerced property value.
     *
     * @param property The property name
     * @param coerceTo The type to coerce to
     * @param defaultValue The default value
     * @return The coerced value or <code>defaultValue</code> if absent
     * @throws OXException If returning property fails
     */
    <T> T opt(String property, Class<T> coerceTo, T defaultValue) throws OXException;

    /**
     * Gets coerced property.
     *
     * @param scope The property's scope
     * @param property The property's name
     * @param coerceTo The type to coerce to
     * @return The coerced property
     * @throws OXException If returning property fails
     */
    <T> ConfigProperty<T> property(String scope, String property, Class<T> coerceTo) throws OXException;

    /**
     * Gets coerced composed property (all scopes combined).
     *
     * @param property The property's name
     * @param coerceTo The type to coerce to
     * @return The coerced composed property
     * @throws OXException If returning composed property fails
     */
    <T> ComposedConfigProperty<T> property(String property, Class<T> coerceTo) throws OXException;

    /**
     * Gets all available properties.
     *
     * @return All available properties
     * @throws OXException If operation fails
     */
    Map<String, ComposedConfigProperty<String>> all() throws OXException;

}
