/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
import com.openexchange.importexport.formats.Format;

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
    public AbstractImportRequest(Action action, int folderId, InputStream upload, Parameter...additionalParameters) {
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

        private final String name, fileName;
        private final Format format;
        private Action(final String name, final String fileName,
            final Format format) {
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
