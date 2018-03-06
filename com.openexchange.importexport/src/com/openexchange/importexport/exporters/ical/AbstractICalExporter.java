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

package com.openexchange.importexport.exporters.ical;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.exception.OXException;
import com.openexchange.importexport.exceptions.ImportExportExceptionCodes;
import com.openexchange.importexport.formats.Format;
import com.openexchange.importexport.helpers.SizedInputStream;
import com.openexchange.java.Streams;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractICalExporter}
 *
 * @author <a href="mailto:Jan-Oliver.Huhn@open-xchange.com">Jan-Oliver Huhn</a>
 * @since v7.10.0
 */
public abstract class AbstractICalExporter implements ICalExport {

    public AbstractICalExporter() {
        super();
    }

    public AbstractICalExporter(String folderId, Map<String, List<String>> batchIds) {
        super();
        this.folderId = folderId;
        this.batchIds = batchIds;
    }

    private String folderId;

    private Map<String, List<String>> batchIds;

    /**
     * Exports the requested batch of data
     *
     * @param session The user session
     * @param out The output stream
     * @return ThresholdFileHolder The file holder
     * @throws OXException if export fails
     */
    abstract protected ThresholdFileHolder exportBatchData(ServerSession session, OutputStream out) throws OXException;

    /**
     * Exports the requested folder
     *
     * @param session The user session
     * @param out The output stream
     * @return ThresholdFileHolder The file holder
     * @throws OXException if export fails
     */
    abstract protected ThresholdFileHolder exportFolderData(ServerSession session, OutputStream out) throws OXException;

    @Override
    public SizedInputStream exportData(ServerSession session, AJAXRequestData requestData, boolean isSaveToDisk, String filename) throws OXException {
        if (null != requestData) {
            // Try to stream
            try {
                OutputStream out = requestData.optOutputStream();
                if (null != out) {
                    requestData.setResponseHeader("Content-Type", isSaveToDisk ? "application/octet-stream" : Format.ICAL.getMimeType() + "; charset=UTF-8");
                    requestData.setResponseHeader("Content-Disposition", "attachment" + filename);
                    requestData.removeCachingHeader();
                    getExportDataSource(session, out);
                    return null;
                }
            } catch (IOException e) {
                throw ImportExportExceptionCodes.ICAL_CONVERSION_FAILED.create(e);
            }
        }

        ThresholdFileHolder sink = null;
        boolean error = true;
        try {
            sink = getExportDataSource(session, null);
            error = false;
            return new SizedInputStream(sink.getClosingStream(), sink.getLength(), Format.ICAL);
        } finally {
            if (error) {
                Streams.close(sink);
            }
        }
    }

    @Override
    public ThresholdFileHolder getExportDataSource(ServerSession session, OutputStream out) throws OXException {
        return isBatchExport() ? exportBatchData(session, out) : exportFolderData(session, out);
    }

    private boolean isBatchExport() {
        return !getBatchIds().isEmpty();
    }

    public String getFolderId() {
        return folderId;
    }

    public Map<String, List<String>> getBatchIds() {
        return batchIds;
    }

}
