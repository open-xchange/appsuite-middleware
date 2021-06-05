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

/**
 * Extend all extensions from this class
 *
 * @author d7
 */
@SuppressWarnings("deprecation")
public abstract class OXCommonExtension implements OXCommonExtensionInterface {

    private static final long serialVersionUID = -4307192636151887795L;

    private String errortext;

    /**
     * This method is used to get the errors which appear while processing an extension. This is especially used
     * for getData methods
     */
    @Override
    public String getExtensionError() {
        return this.errortext;
    }

    /**
     * This method is used to set the errors which appear while processing an extension. This is especially used
     * for getData methods
     */
    @Override
    public void setExtensionError(final String errortext) {
        this.errortext = errortext;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(super.toString());
        sb.append(this.errortext);
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((errortext == null) ? 0 : errortext.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }
}
