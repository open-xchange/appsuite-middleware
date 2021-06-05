/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
        if (i == path.length) {
            return this;
        }
        String childName = path[i];
        for (FolderNode node : getChildren()) {
            if (node.getFolder().getFolderName().equals(childName)) {
                return ((AbstractFolderNode) node).resolveRecursively(i + 1, path);
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
        if (parent != null) {
            return parent;
        }
        return parent = load(getFolder().getParentFolderID());
    }

    protected FolderNode load(FolderObject folder) {
        FolderObject folderObject = manager.getFolderFromServer(folder, false);
        if (folderObject == null) {
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
        visitor.visit(i, this);
        for (FolderNode child : getChildren()) {
            ((AbstractFolderNode) child).recurse(i + 1, visitor);
        }
    }
}
