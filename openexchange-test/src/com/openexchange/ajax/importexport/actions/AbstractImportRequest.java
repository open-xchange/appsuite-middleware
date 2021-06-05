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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.ajax.framework.AbstractAJAXResponse;
import com.openexchange.ajax.framework.Header;
import com.openexchange.importexport.Format;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public abstract class AbstractImportRequest<T extends AbstractAJAXResponse> implements AJAXRequest<T> {

    /**
     * URL of the tasks AJAX interface.
     */
    public static final String IMPORT_URL = "/ajax/import";

    private final Action action;
    private final int folderId;
    private final InputStream upload;
    private final Parameter[] additionalParameters;

    /**
     * Initializes a new {@link AbstractImportRequest}.
     *
     * @param action The import action
     * @param folderId The target folder identifier
     * @param upload The input stream to upload
     */
    public AbstractImportRequest(Action action, int folderId, InputStream upload) {
        this(action, folderId, upload, (Parameter[]) null);
    }

    /**
     * Initializes a new {@link AbstractImportRequest}.
     *
     * @param action The import action
     * @param folderId The target folder identifier
     * @param upload The input stream to upload
     * @param additionalParameters Additional parameters to include in the request
     */
    public AbstractImportRequest(Action action, int folderId, InputStream upload, Parameter... additionalParameters) {
        super();
        this.action = action;
        this.folderId = folderId;
        this.upload = upload;
        this.additionalParameters = additionalParameters;
    }

    @Override
    public Object getBody() {
        return null;
    }

    @Override
    public Method getMethod() {
        return Method.UPLOAD;
    }

    @Override
    public Header[] getHeaders() {
        return NO_HEADER;
    }

    @Override
    public Parameter[] getParameters() {
        List<Parameter> parameters = new ArrayList<Parameter>();
        parameters.add(new Parameter(AJAXServlet.PARAMETER_ACTION, action.getName()));
        parameters.add(new Parameter(AJAXServlet.PARAMETER_FOLDERID, folderId));
        parameters.add(new FileParameter("file", action.fileName, upload, action.format.getMimeType()));
        if (null != additionalParameters && 0 < additionalParameters.length) {
            parameters.addAll(Arrays.asList(additionalParameters));
        }
        return parameters.toArray(new Parameter[parameters.size()]);
    }

    protected enum Action {
        CSV("CSV", "contacts.csv", Format.CSV),
        ICal("ICAL", "ical.ics", Format.ICAL),
        VCard("VCARD", "vcard.vcf", Format.VCARD),
        OUTLOOK_CSV("OUTLOOK_CSV", "contacts.csv", Format.OUTLOOK_CSV);

        private final String name;
        final String fileName;
        final Format format;

        private Action(final String name, final String fileName, final Format format) {
            this.name = name;
            this.fileName = fileName;
            this.format = format;
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }
    }

    @Override
    public String getServletPath() {
        return IMPORT_URL;
    }
}
