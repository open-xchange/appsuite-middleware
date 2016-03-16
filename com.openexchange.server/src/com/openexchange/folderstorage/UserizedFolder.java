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

package com.openexchange.folderstorage;

import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.session.Session;

/**
 * {@link UserizedFolder} - Extends/overwrites {@link Folder} interface methods with user-sensitive methods.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface UserizedFolder extends ParameterizedFolder, AltNameAwareFolder {

    /**
     * Gets the context.
     *
     * @return The context
     */
    Context getContext();

    /**
     * Gets the user.
     *
     * @return The user
     */
    User getUser();

    /**
     * Gets the session.
     *
     * @return The session or <code>null</code> if the folder was requested without a session
     */
    Session getSession();

    /**
     * Gets the subfolder IDs.
     * <p>
     * <b>Note</b>: In opposite to {@link Folder#getSubfolderIDs()} this method does not return complete list of subfolder identifiers.
     * Since a user-sensitive folder is only meant to indicate if it contains any subfolder at all, it only serves the condition:
     *
     * <pre>
     * final boolean hasSubfolders = userizedFolder.getSubfolderIDs() &gt; 0
     * </pre>
     *
     * @return The subfolder IDs or <code>null</code> if not available
     */
    @Override
    String[] getSubfolderIDs();

    /**
     * Gets the permission for requesting user.
     *
     * @return The permission for requesting user
     */
    Permission getOwnPermission();

    /**
     * Sets the permission for requesting user.
     *
     * @param ownPermission The permission for requesting user
     */
    void setOwnPermission(Permission ownPermission);

    /**
     * Gets the last-modified date in UTC.
     *
     * @return The last-modified date in UTC
     */
    Date getLastModifiedUTC();

    /**
     * Sets the last-modified date in UTC.
     *
     * @param lastModifiedUTC The last-modified date in UTC
     */
    void setLastModifiedUTC(Date lastModifiedUTC);

    /**
     * Gets the creation date in UTC.
     *
     * @return The creation date in UTC
     */
    Date getCreationDateUTC();

    /**
     * Sets the creation date in UTC.
     *
     * @param creationDateUTC The creation date in UTC
     */
    void setCreationDateUTC(Date creationDateUTC);

    /**
     * Gets the locale for this user-sensitive folder.
     *
     * @return The locale for this user-sensitive folder
     */
    Locale getLocale();

    /**
     * Sets the locale for this user-sensitive folder.
     *
     * @param locale The locale for this user-sensitive folder
     */
    void setLocale(Locale locale);

    /**
     * Signals whether to prefer alternative App Suite folder names.
     *
     * @return <code>true</code> for alternative folder names; otherwise <code>false</code>
     */
    boolean isAltNames();

    /**
     * Sets whether to prefer alternative App Suite folder names.
     *
     * @param altNames <code>true</code> for alternative folder names; otherwise <code>false</code>
     */
    void setAltNames(boolean altNames);

    /**
     * Sets the dynamic metadata
     */
    @Override
    void setMeta(Map<String, Object> meta);

    /**
     * @return the dynamic properties
     */
    @Override
    Map<String, Object> getMeta();

    /**
     * Sets the parameters reference.
     *
     * @param parameters The parameters to set
     */
    void setParameters(final ConcurrentMap<String, Object> parameters);

    /**
     * Gets the parameters reference.
     *
     * @return The parameters reference
     */
    ConcurrentMap<String, Object> getParameters();

}
