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

import java.util.List;
import com.openexchange.exception.OXException;


/**
 * {@link BasicProperty}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a> Added JavaDoc
 */
public interface BasicProperty {

    /**
     * Sets this property's value
     *
     * @param value The value
     * @throws OXException If setting value fails
     */
    void set(String value) throws OXException;

    /**
     * Gets this property's value
     *
     * @return The value
     * @throws OXException If retrieving value fails
     */
    String get() throws OXException;

    /**
     * Sets specified meta data.
     *
     * @param metadataName The meta data's name
     * @param value The meta data's value
     * @throws OXException If setting meta data fails
     */
    void set(String metadataName, String value) throws OXException;

    /**
     * Gets specified meta data.
     *
     * @param metadataName The meta data's name
     * @return The meta data's value or <code>null</code> if absent
     * @throws OXException
     */
    String get(String metadataName) throws OXException;

    /**
     * Indicates whether this property is defined.
     *
     * @return <code>true</code> if defined; otherwise <code>false</code>
     * @throws OXException If checking defined state fails
     */
    boolean isDefined() throws OXException;

    /**
     * Gets the listing of all available meta data names.
     *
     * @return The meta data names
     * @throws OXException If listing cannot be returned
     */
    List<String> getMetadataNames() throws OXException;
}
