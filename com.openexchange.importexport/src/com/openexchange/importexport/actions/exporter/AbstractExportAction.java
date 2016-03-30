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

package com.openexchange.importexport.actions.exporter;

import static com.openexchange.java.Autoboxing.I2i;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.FileHolder;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.importexport.exporters.Exporter;
import com.openexchange.importexport.formats.Format;
import com.openexchange.importexport.helpers.SizedInputStream;
import com.openexchange.importexport.json.ExportRequest;
import com.openexchange.tools.session.ServerSession;

public abstract class AbstractExportAction implements AJAXActionService {

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        return perform(new ExportRequest(requestData, session));
    }

    public abstract Format getFormat();

    public abstract Exporter getExporter();

    private static final String PARAMETER_CONTENT_TYPE = "content_type";
    private static final String DELIVERY = AJAXServlet.PARAMETER_DELIVERY;
    private static final String SAVE_AS_TYPE = "application/octet-stream";
    private static final String DOWNLOAD = "download";

    private AJAXRequestResult perform(ExportRequest req) throws OXException {
        final List<Integer> cols = req.getColumns();
        final SizedInputStream sis = getExporter().exportData(req.getSession(), getFormat(), req.getFolder(), cols != null ? I2i(cols) : null, getOptionalParams(req));
        if (null == sis) {
            // Streamed
            return new AJAXRequestResult(AJAXRequestResult.DIRECT_OBJECT, "direct").setType(AJAXRequestResult.ResultType.DIRECT);
        }

        final FileHolder fileHolder = new FileHolder(sis, sis.getSize(), sis.getFormat().getMimeType(), "export." + sis.getFormat().getExtension());
        fileHolder.setDisposition("attachment");
        req.getRequest().setFormat("file");
        return new AJAXRequestResult(fileHolder, "file");
    }

    protected Map<String, Object> getOptionalParams(ExportRequest req) {
        final Map<String, Object> optionalParams;
        final AJAXRequestData request = req.getRequest();

        final boolean responseAccess = request.isHttpServletResponseAvailable();
        if (responseAccess) {
            optionalParams = new HashMap<String, Object>(4);
            optionalParams.put("__requestData", request);
            String contentType = request.getParameter(PARAMETER_CONTENT_TYPE);
            String delivery = request.getParameter(DELIVERY);
            if (SAVE_AS_TYPE.equals(contentType) || DOWNLOAD.equalsIgnoreCase(delivery)) {
                optionalParams.put("__saveToDisk", Boolean.TRUE);
            }
        } else {
            optionalParams = null;
        }

        return optionalParams;
    }
}
