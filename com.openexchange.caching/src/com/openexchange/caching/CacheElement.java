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

package com.openexchange.caching;

import java.io.Serializable;

/**
 * {@link CacheElement} - Every item in the cache is wrapped in a cache element. This contains information about the element: the region
 * name, the key, the value, and the element attributes.
 * <p>
 * The element attributes have lots of useful information about each element, such as when they were created, how long they have to live,
 * and if they are allowed to be spooled, etc.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface CacheElement extends Serializable {

    /**
     * Gets the cache name attribute of the cache element object. The cache name is also known as the region name.
     *
     * @return The cacheName value
     */
    public String getCacheName();

    /**
     * Gets the key attribute of the cache element object
     *
     * @return The key value
     */
    public Serializable getKey();

    /**
     * Gets the value attribute of the cache element object
     *
     * @return The value
     */
    public Serializable getVal();

    /**
     * Gets the attributes attribute of the cache element object
     *
     * @return The attributes value
     */
    public ElementAttributes getElementAttributes();

    /**
     * Sets the attributes attribute of the cache element object
     *
     * @param attr The new attributes value
     */
    public void setElementAttributes(ElementAttributes attr);

}
