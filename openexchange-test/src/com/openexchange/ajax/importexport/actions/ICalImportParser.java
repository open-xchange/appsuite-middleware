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

import org.json.JSONArray;
import org.json.JSONException;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractUploadParser;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.importexport.ImportResult;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class ICalImportParser extends AbstractUploadParser<ICalImportResponse> {

    private final boolean failOnError;

    /**
     * @param failOnError
     */
    public ICalImportParser(final boolean failOnError) {
        super(failOnError);
        this.failOnError = failOnError;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected ICalImportResponse createResponse(final Response response) throws JSONException {
        final ICalImportResponse retval = new ICalImportResponse(response);
        final Object data = response.getData();
        if (data instanceof JSONArray) {
            final JSONArray array = (JSONArray) data;
            final ImportResult[] results = new ImportResult[array.length()];
            for (int i = 0; i < array.length(); i++) {
                results[i] = ImportExportParser.parse(array.getString(i));
            }
            retval.setImports(results);
            for (final ImportResult result : results) {
                final OXException e = result.getException();
                final String msg = e == null ? null : e.getMessage();
                if (failOnError) {
                    assertFalse(msg, result.hasError());
                }
            }
        } else if (failOnError) {
            fail("Wrong data in response.");
        }
        return retval;
    }
}
