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

package com.openexchange.groupware.contexts;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * The context stores all attributes that are necessary for components dealing with context specific data. This are especially which
 * database stores the data of the context, the unique numerical identifier used in the relational database to assign persistent stored data
 * to their contexts and is the base distinguished name used in the directory service to separate contexts. Objects implementing this
 * interface must implement {@link java.lang.Object#equals(java.lang.Object)} and {@link java.lang.Object#hashCode()} because this interface
 * is used as key for maps.
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public interface Context extends FileStorageInfo, Serializable {

    /**
     * Returns the unique identifier of the context.
     *
     * @return unique identifier of the context.
     */
    int getContextId();

    /**
     * @return the name of the context.
     */
    String getName();

    /**
     * @return the login information of a context.
     */
    String[] getLoginInfo();

    /**
     * Returns the unique identifier of context's admin.
     *
     * @return unique identifier of the context's admin
     */
    int getMailadmin();

    /**
     * Returns if a context is enabled. All sessions that belong to a disabled context have to die as fast as possible to be able to
     * maintain these contexts.
     *
     * @return <code>true</code> if the context is enabled, <code>false</code> otherwise.
     */
    boolean isEnabled();

    /**
     * Returns if a context is being updated. This will be <code>true</code> if the schema is being updated the context is stored in.
     *
     * @return <code>true</code> if an update takes place.
     */
    boolean isUpdating();

    /**
     * Contexts can be put into read only mode if the master database server is not reachable. This method indicates if currently the master
     * is not reachable.
     *
     * @return <code>true</code> if the master database server is not reachable.
     */
    boolean isReadOnly();

    /**
     * Gets the context attributes as an unmodifiable map.
     * <p>
     * Each attribute may point to multiple values.
     *
     * @return The context attributes
     */
    Map<String, List<String>> getAttributes();

}
