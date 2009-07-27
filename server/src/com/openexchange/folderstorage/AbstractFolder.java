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
 * {@link AbstractFolder} - An abstract folder.
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractFolder implements Folder {

    protected String treeId;

    protected String id;

    protected String name;

    protected String parent;

    protected Permission[] permissions;

    protected String[] subfolders;

    protected boolean subscribed;

    protected ContentType contentType;

    protected Type type;

    /**
     * Initializes an empty {@link AbstractFolder}.
     */
    protected AbstractFolder() {
        super();
    }

    public ContentType getContentType() {
        return contentType;
    }

    public String getID() {
        return id;
    }

    public String getLocalizedName(final Locale locale) {
        return name;
    }

    public String getName() {
        return name;
    }

    public String getParentID() {
        return parent;
    }

    public Permission[] getPermissions() {
        return permissions;
    }

    public String[] getSubfolderIDs() {
        return subfolders;
    }

    public String getTreeID() {
        return treeId;
    }

    public Type getType() {
        return type;
    }

    public void setContentType(final ContentType contentType) {
        this.contentType = contentType;
    }

    public void setID(final String id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setParentID(final String parentId) {
        this.parent = parentId;
    }

    public void setPermissions(final Permission[] permissions) {
        this.permissions = permissions;
    }

    public void setSubfolderIDs(final String[] subfolderIds) {
        this.subfolders = subfolderIds;
    }

    public void setTreeID(final String id) {
        this.treeId = id;
    }

    public void setType(final Type type) {
        this.type = type;
    }

    public boolean isSubscribed() {
        return subscribed;
    }

    public void setSubscribed(final boolean subscribed) {
        this.subscribed = subscribed;
    }

}
