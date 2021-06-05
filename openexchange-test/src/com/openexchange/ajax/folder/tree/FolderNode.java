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
import java.util.List;
import com.openexchange.groupware.container.FolderObject;

/**
 * {@link FolderNode}
 * FolderNodes are arranged in a tree structure that allows one to navigate through the OX folder tree.
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public interface FolderNode {

    /**
     * @return The underlying FolderObject
     */
    public FolderObject getFolder();

    /**
     * @return The parent in the folder hierarchy or null if this node is the root node.
     */
    public FolderNode getParent();

    /**
     * @return The subnodes of this FolderNode
     */
    public List<FolderNode> getChildren();

    /**
     * Tries to resolve the given path parameters as subfolder names relative to this folder.
     * 
     * @param path, a list of folder names to resolve along
     * @return the FolderNode the resolver arrived at, or null if this path is unknown
     */
    public FolderNode resolve(String... path);

    /**
     * Utility method that links to #resolve(String[])
     */
    public FolderNode resolve(Collection<String> path);

    /**
     * Recurses breadth first through this subtree
     * 
     * @param visitor
     */
    public void recurse(FolderNodeVisitor visitor);

    public boolean isRoot();

}
