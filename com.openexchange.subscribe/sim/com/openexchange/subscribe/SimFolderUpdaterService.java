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

package com.openexchange.subscribe;

import java.util.Collection;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.generic.FolderUpdaterService;
import com.openexchange.groupware.generic.TargetFolderDefinition;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorAdapter;


/**
 * {@link SimFolderUpdaterService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class SimFolderUpdaterService implements FolderUpdaterService<Object> {

    private TargetFolderDefinition target;
    private Collection<?> data;
    private FolderObject folder;
    private boolean handles;
    private boolean usesMultipleStrategy = false;

    @Override
    public boolean handles(final FolderObject folder) {
        this.folder = folder;
        return handles;
    }

    @Override
    public void save(final SearchIterator<Object> data, final TargetFolderDefinition target) throws OXException {
        this.data = SearchIteratorAdapter.toList(data);
        this.target = target;
    }

    public void setTarget(final TargetFolderDefinition target) {
        this.target = target;
    }

    public TargetFolderDefinition getTarget() {
        return target;
    }

    public Collection<?> getData() {
        return data;
    }


    public void setData(final Collection<?> data) {
        this.data = data;
    }


    public FolderObject getFolder() {
        return folder;
    }


    public void setFolder(final FolderObject folder) {
        this.folder = folder;
    }


    public boolean isHandles() {
        return handles;
    }


    public void setHandles(final boolean handles) {
        this.handles = handles;
    }

    @Override
    public boolean usesMultipleStrategy() {
        return usesMultipleStrategy;
    }

    public void setUsesMultipleStrategy(final boolean bool){
        this.usesMultipleStrategy = bool;
    }

}
