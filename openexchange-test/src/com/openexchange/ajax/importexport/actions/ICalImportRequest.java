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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import com.openexchange.java.Charsets;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class ICalImportRequest extends AbstractImportRequest<ICalImportResponse> {

    private final boolean failOnError;

    /**
     * @param folderId
     * @param upload
     */
    public ICalImportRequest(final int folderId, final InputStream iCal) {
        this(folderId, iCal, true);
    }

    public ICalImportRequest(final int folderId, final InputStream iCal, final boolean failOnError) {
        super(Action.ICal, folderId, iCal);
        this.failOnError = failOnError;
    }

    public ICalImportRequest(final int folderId, final String iCal) {
        this(folderId, new ByteArrayInputStream(Charsets.getBytes(iCal, Charsets.UTF_8)), true);
    }

    public ICalImportRequest(final int folderId, final String iCal, final boolean failOnError) {
        this(folderId, new ByteArrayInputStream(Charsets.getBytes(iCal, Charsets.UTF_8)), failOnError);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ICalImportParser getParser() {
        return new ICalImportParser(failOnError);
    }
}
