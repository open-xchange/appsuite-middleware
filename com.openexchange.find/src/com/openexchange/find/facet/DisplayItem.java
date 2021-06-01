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

package com.openexchange.find.facet;

import java.io.Serializable;

/**
 * Interface for several kinds objects that can be displayed by a client. Inheritors must implement hashCode() and equals().
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 * @since v7.6.0
 */
public interface DisplayItem extends Serializable{

    /**
     * A display item must contain a default display name.
     * <p>
     * Clients may use it for displaying the item if they have no implementation to handle the concrete item type.
     *
     * @return The default display name. Never <code>null</code>.
     */
    String getDisplayName();

    /**
     * Delegate to given visitor.
     *
     * @param visitor The visitor to delegate to
     */
    void accept(DisplayItemVisitor visitor);

}
