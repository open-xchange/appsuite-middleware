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
package com.openexchange.admin.rmi.extensions;

import java.io.Serializable;

/**
 *
 *
 * @author d7
 * @deprecated
 */
@Deprecated
public interface OXCommonExtensionInterface extends Serializable {
    /**
     * If an error has occured you get the error text of the extension
     * here
     * @return a string containing the error text
     */
    public String getExtensionError();

    /**
     * If an error has occured you set the error text of the extension
     * here
     */
    public void setExtensionError(final String errortext);

    /**
     * Used to return a string representation of the underlying object
     * @return
     */
    @Override
    public String toString();

    @Override
    public boolean equals(final Object obj);

}
