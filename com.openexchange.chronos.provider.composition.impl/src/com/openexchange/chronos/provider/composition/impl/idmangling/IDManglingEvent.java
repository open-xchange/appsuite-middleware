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

package com.openexchange.chronos.provider.composition.impl.idmangling;

import com.openexchange.chronos.DelegatingEvent;
import com.openexchange.chronos.Event;

/**
 * {@link IDManglingEvent}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 * @since v7.10.0
 */
public class IDManglingEvent extends DelegatingEvent {

    private final String newFolderId;

    /**
     * Initializes a new {@link IDManglingEvent}.
     *
     * @param delegate The event delegate
     * @param newFolderId The folder new identifier to take over
     */
    public IDManglingEvent(Event delegate, String newFolderId) {
        super(delegate);
        this.newFolderId = newFolderId;
    }

    @Override
    public void setFolderId(String folderId) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsFolderId() {
        return true;
    }

    @Override
    public String getFolderId() {
        return newFolderId;
    }

    @Override
    public String toString() {
        return "IDManglingEvent [newFolderId=" + newFolderId + ", delegate=" + delegate + "]";
    }

}
