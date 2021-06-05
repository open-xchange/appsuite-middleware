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

package com.openexchange.ajax.importexport.actions;

import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.importexport.ImportResult;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class ICalImportResponse extends AbstractAJAXResponse {

    private ImportResult[] imports;

    /**
     * @param response
     */
    public ICalImportResponse(final Response response) {
        super(response);
    }

    /**
     * @return the imports
     */
    public ImportResult[] getImports() {
        return imports;
    }

    /**
     * @param imports the imports to set
     */
    public void setImports(final ImportResult[] imports) {
        this.imports = imports;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasError() {
        if (super.hasError()) {
            return true;
        }
        boolean retval = false;
        if (null != imports) {
            for (int i = 0; !retval && i < imports.length; i++) {
                retval = imports[i].hasError();
            }
        }
        return retval;
    }

    @Override
    public OXException getException() {
        OXException e = super.getException();
        if (null != e) {
            return e;
        }
        if (null != imports) {
            for (int i = 0; null == e && i < imports.length; i++) {
                e = imports[i].getException();
            }
        }
        return e;
    }
}
