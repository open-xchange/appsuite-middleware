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
import java.util.Set;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.session.Session;

/**
 * {@link StorageParameters} - The storage parameters to perform a certain storage operation.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface StorageParameters {

    /**
     * The parameter name to indicate fast access to storage.
     *
     * @type java.lang.Boolean
     */
    public static final String PARAM_ACCESS_FAST = "__accessFast";

    /**
     * Adds a warning to this storage parameters.<br>
     * <b><small>NOTE</small></b>: Category is set to {@link Category#WARNING} if not done, yet.
     *
     * @param warning The warning to add
     */
    void addWarning(OXException warning);

    /**
     * Checks if this storage parameters contain warnings.
     *
     * @return <code>true</code> if this storage parameters contain warnings; otherwise <code>false</code>
     */
    boolean hasWarnings();

    /**
     * Gets the warnings of this storage parameters as an unmodifiable {@link Set set}.
     *
     * @return The warnings as an unmodifiable set
     */
    Set<OXException> getWarnings();

    /**
     * Gets the context.
     *
     * @return The context
     */
    Context getContext();

    /**
     * Convenience method to get the context identifier.
     *
     * @return The context identifier
     * @see #getContext()
     */
    int getContextId();

    /**
     * Gets the user.
     *
     * @return The user
     */
    User getUser();

    /**
     * Convenience method to get the user identifier.
     *
     * @return The user identifier
     * @see #getUser()
     */
    int getUserId();

    /**
     * Gets the session.
     *
     * @return The session or <code>null</code>
     */
    Session getSession();

    /**
     * Gets the optional decorator.
     *
     * @return The decorator or <code>null</code>
     */
    FolderServiceDecorator getDecorator();

    /**
     * Sets the decorator.
     *
     * @param decorator The decorator
     */
    void setDecorator(FolderServiceDecorator decorator);

    /**
     * Gets a <b>copy</b> of the requestor's last-modified time stamp.
     *
     * @return A <b>copy</b> of the requestor's last-modified time stamp or <code>null</code>
     */
    Date getTimeStamp();

    /**
     * Sets the requestor's last-modified time stamp.
     * <p>
     * <b>Note</b>: Given time stamp is copied if not <code>null</code>.
     *
     * @param timeStamp The requestor's last-modified time stamp or <code>null</code> to remove
     */
    void setTimeStamp(Date timeStamp);

    /**
     * Gets the parameter bound to given name.
     *
     * @param folderType The folder type
     * @param name The parameter name
     * @return The parameter bound to given name
     */
    <P> P getParameter(FolderType folderType, String name);

    /**
     * Removes the parameter bound to given name.
     *
     * @param folderType The folder type
     * @param name The parameter name
     * @return The parameter previously bound to given name
     */
    <P> P removeParameter(FolderType folderType, String name);

    /**
     * Puts given parameter. Any existing parameters bound to given name are replaced. A <code>null</code> value means to remove the
     * parameter.
     * <p>
     * A <code>null</code> value removes the parameter.
     *
     * @param folderType The folder type
     * @param name The parameter name
     * @param value The parameter value
     * @return The previous value associated with the name, or <code>null</code> null if there was no mapping before
     */
    Object putParameter(FolderType folderType, String name, Object value);

    /**
     * (Atomically) Puts given parameter only if the specified name is not already associated with a value.
     * <p>
     * A <code>null</code> value is not permitted.
     *
     * @param folderType The folder type
     * @param name The parameter name
     * @param value The parameter value
     * @throws IllegalArgumentException If value is <code>null</code>
     * @return <code>true</code> if put was successful; otherwise <code>false</code>
     */
    boolean putParameterIfAbsent(FolderType folderType, String name, Object value);

    /**
     * Sets a committed marker to this storage parameters.
     */
    void markCommitted();

    /**
     * Gets the trace of the thread that set the committed marker to this storage parameters.
     *
     * @return The trace
     */
    String getCommittedTrace();

    /**
     * Gets the optional ignoreCache value.
     *
     * @return The ignoreCache value or <code>null</code>
     */
    Boolean getIgnoreCache();

    /**
     * Sets the optional ignoreCache.
     *
     * @param ignoreCache The value indicating if underlaying storages should ignore the cache
     */
    void setIgnoreCache(Boolean ignoreCache);

}
