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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import java.util.Locale;

/**
 * {@link Folder} - A folder.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public interface Folder {

    /**
     * Indicates if this folder is virtual.
     * 
     * @return <code>true</code> if this folder is virtual; otherwise <code>false</code>
     */
    public boolean isVirtual();

    /**
     * Gets the tree ID.
     * 
     * @return The tree ID or <code>null</code> if not available
     */
    public String getTreeID();

    /**
     * Sets the tree ID.
     * 
     * @param id The tree ID to set
     */
    public void setTreeID(String id);

    /**
     * Gets the ID.
     * 
     * @return The ID or <code>null</code> if not available
     */
    public String getID();

    /**
     * Sets the ID.
     * 
     * @param id The ID to set
     */
    public void setID(String id);

    /**
     * Gets the parent ID.
     * 
     * @return The parent ID or <code>null</code> if not available
     */
    public String getParentID();

    /**
     * Sets the parent ID.
     * 
     * @param parentId The parent ID to set
     */
    public void setParentID(String parentId);

    /**
     * Gets the subfolder IDs.
     * <p>
     * {@link FolderStorage#getSubfolders(String)} is supposed to be used if <code>null</code> is returned.
     * 
     * @return The subfolder IDs or <code>null</code> if not available
     */
    public String[] getSubfolderIDs();

    /**
     * Sets the subfolder IDs.
     * 
     * @param subfolderIds The subfolder IDs to set
     */
    public void setSubfolderIDs(String[] subfolderIds);

    /**
     * Gets the name.
     * 
     * @return The name or <code>null</code> if not available
     */
    public String getName();

    /**
     * Sets the name.
     * 
     * @param name The name to set
     */
    public void setName(String name);

    /**
     * Gets the locale-sensitive name.
     * 
     * @param locale The locale
     * @return The locale-sensitive name or <code>null</code> if not available
     */
    public String getLocalizedName(Locale locale);

    /**
     * Gets the permissions.
     * 
     * @return The permissions or <code>null</code> if not available
     */
    public Permission[] getPermissions();

    /**
     * Sets the permissions.
     * 
     * @param permissions The permissions to set
     */
    public void setPermissions(Permission[] permissions);

    /**
     * Gets the content type.
     * 
     * @return The content type or <code>null</code> if not available
     */
    public ContentType getContentType();

    /**
     * Sets the content type.
     * 
     * @param contentType The content type to set
     */
    public void setContentType(ContentType contentType);

    /**
     * Gets the type.
     * 
     * @return The type or <code>null</code> if not available
     */
    public Type getType();

    /**
     * Sets the type.
     * 
     * @param type The type to set
     */
    public void setType(Type type);

    /**
     * Indicates if this folder is subscribed.
     * 
     * @return <code>true</code> if this folder is subscribed; otherwise <code>false</code>
     */
    public boolean isSubscribed();

    /**
     * Sets if this folder is subscribed.
     * 
     * @param subscribed <code>true</code> if this folder is subscribed; otherwise <code>false</code>
     */
    public void setSubscribed(boolean subscribed);
}
