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

import java.io.InputStream;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class CSVImportRequest extends AbstractImportRequest<CSVImportResponse> {

    private final boolean failOnError;

    /**
     * Default constructor.
     */
    public CSVImportRequest(final int folderId, final InputStream csv) {
        this(folderId, csv, true);
    }

    public CSVImportRequest(final int folderId, final InputStream csv, final boolean failOnError) {
        this(folderId, csv, failOnError, (Parameter[]) null);
    }

    /**
     * Initializes a new {@link CSVImportRequest}.
     *
     * @param folderId The target folder identifier
     * @param csv The input stream to upload
     * @param failOnError <code>true</code> to fail the testcase in case of error in the response, <code>false</code>, otherwise
     * @param additionalParameters Additional parameters to include in the request
     */
    public CSVImportRequest(int folderId, InputStream csv, boolean failOnError, Parameter... additionalParameters) {
        super(Action.CSV, folderId, csv, additionalParameters);
        this.failOnError = failOnError;
    }

    @Override
    public CSVImportParser getParser() {
        return new CSVImportParser(failOnError);
    }
}
