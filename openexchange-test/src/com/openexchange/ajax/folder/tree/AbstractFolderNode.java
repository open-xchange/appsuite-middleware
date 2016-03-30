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

package com.openexchange.ajax.folder.tree;

import java.util.Collection;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.test.FolderTestManager;


/**
 * {@link AbstractFolderNode}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public abstract class AbstractFolderNode implements FolderNode {

    private FolderObject folder;
    private FolderNode parent;
    private final AJAXClient client;
    private final FolderTestManager manager;

    public AbstractFolderNode(FolderObject underlyingObject, AJAXClient client) {
        this.folder = underlyingObject;
        this.client = client;
        this.manager = new FolderTestManager(client);
    }

    public AbstractFolderNode(int folderId, AJAXClient client) {
        this(null, client);
        setFolder(getManager().getFolderFromServer(folderId));
    }

    @Override
    public FolderObject getFolder() {
        return folder;
    }

    @Override
    public FolderNode resolve(String... path) {
        return resolveRecursively(0, path);
    }

    protected FolderNode resolveRecursively(int i, String[] path) {
        if(i == path.length) {
            return this;
        }
        String childName = path[i];
        for(FolderNode node : getChildren()) {
            if(node.getFolder().getFolderName().equals(childName)) {
                return ((AbstractFolderNode) node).resolveRecursively(i+1, path);
            }
        }

        return null;
    }

    @Override
    public FolderNode resolve(Collection<String> path) {
        return resolveRecursively(0, path.toArray(new String[path.size()]));
    }

    @Override
    public FolderNode getParent() {
        if(parent != null) {
            return parent;
        }
        return parent = load(getFolder().getParentFolderID());
    }

    protected FolderNode load(FolderObject folder) {
        FolderObject folderObject = manager.getFolderFromServer(folder, false);
        if(folderObject == null) {
            // Best effort
            return node(folder, client);
        }
        return node(folderObject, client);
    }

    protected FolderNode load(int folderId) {
        FolderObject folderObject = manager.getFolderFromServer(folderId);
        return node(folderObject, client);
    }

    protected FolderTestManager getManager() {
        return manager;
    }


    protected AJAXClient getClient() {
        return client;
    }


    protected void setFolder(FolderObject folder) {
        this.folder = folder;
    }

    protected abstract FolderNode node(FolderObject folderObject, AJAXClient client);

    @Override
    public void recurse(FolderNodeVisitor visitor) {
        recurse(0, visitor);
    }

    protected void recurse(int i, FolderNodeVisitor visitor) {
        visitor.visit(i,this);
        for(FolderNode child : getChildren()) {
            ((AbstractFolderNode)child).recurse(i+1, visitor);
        }
    }
}
