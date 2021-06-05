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

package com.openexchange.ajax.importexport;

import java.io.IOException;
import org.json.JSONException;
import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.importexport.actions.CSVImportRequest;
import com.openexchange.ajax.importexport.actions.CSVImportResponse;
import com.openexchange.ajax.importexport.actions.ICalImportRequest;
import com.openexchange.ajax.importexport.actions.ICalImportResponse;
import com.openexchange.ajax.importexport.actions.OutlookCSVImportRequest;
import com.openexchange.ajax.importexport.actions.OutlookCSVImportResponse;
import com.openexchange.ajax.importexport.actions.VCardImportRequest;
import com.openexchange.ajax.importexport.actions.VCardImportResponse;
import com.openexchange.exception.OXException;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class Tools {

    /**
     * Prevent instantiation.
     */
    private Tools() {
        super();
    }

    public static CSVImportResponse importCSV(final AJAXClient client, final CSVImportRequest request) throws OXException, IOException, JSONException {
        return Executor.execute(client, request);
    }

    public static ICalImportResponse importICal(final AJAXClient client, final ICalImportRequest request) throws OXException, IOException, JSONException {
        return Executor.execute(client, request);
    }

    public static VCardImportResponse importVCard(final AJAXClient client, final VCardImportRequest request) throws OXException, IOException, JSONException {
        return Executor.execute(client, request);
    }

    public static OutlookCSVImportResponse importOutlookCSV(final AJAXClient client, final OutlookCSVImportRequest request) throws OXException, IOException, JSONException {
        return Executor.execute(client, request);
    }

}
