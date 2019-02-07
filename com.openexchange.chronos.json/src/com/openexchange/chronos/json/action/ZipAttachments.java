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

package com.openexchange.chronos.json.action;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.ajax.helper.DownloadUtility;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccess;
import com.openexchange.chronos.service.EventID;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageUtility;
import com.openexchange.java.IOs;
import com.openexchange.java.Streams;
import com.openexchange.mime.MimeTypeMap;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link ZipAttachments}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class ZipAttachments extends ChronosAction {

    /**
     * Initializes a new {@link ZipAttachments}.
     *
     * @param services The service look-up
     */
    ZipAttachments(ServiceLookup services) {
        super(services);
    }

    @Override
    protected AJAXRequestResult perform(IDBasedCalendarAccess calendarAccess, AJAXRequestData requestData) throws OXException {
        // Gather the parameters
        List<AttachmentId> attachmentIds = getAttachmentIds(requestData);
        return createZipArchive("attachments.zip", attachmentIds, calendarAccess, requestData);
    }

    private AJAXRequestResult createZipArchive(String fullFileName, List<AttachmentId> attachmentIds, IDBasedCalendarAccess calendarAccess, AJAXRequestData requestData) throws OXException {
        // Check if it is possible to directly write to output stream
        if (requestData.setResponseHeader("Content-Type", "application/zip")) {
            try {
                // Prepare further response headers
                StringBuilder sb = new StringBuilder(512);
                sb.append("attachment");
                DownloadUtility.appendFilenameParameter(fullFileName, "application/zip", requestData.getUserAgent(), sb);
                requestData.setResponseHeader("Content-Disposition", sb.toString());

                // Create ZIP archive
                long bytesWritten = writeZipArchive(attachmentIds, requestData.optOutputStream(), calendarAccess);

                // Streamed
                AJAXRequestResult result = new AJAXRequestResult(AJAXRequestResult.DIRECT_OBJECT, "direct").setType(AJAXRequestResult.ResultType.DIRECT);
                if (bytesWritten != 0) {
                    result.setResponseProperty("X-Content-Size", Long.valueOf(bytesWritten));
                }
                return result;
            } catch (final IOException e) {
                throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
            }
        }


        ThresholdFileHolder fileHolder = new ThresholdFileHolder();
        try {
            fileHolder.setDisposition("attachment");
            fileHolder.setName(fullFileName);
            fileHolder.setContentType("application/zip");
            fileHolder.setDelivery("download");

            // Create ZIP archive
            writeZipArchive(attachmentIds, fileHolder.asOutputStream(), calendarAccess);

            requestData.setFormat("file");
            AJAXRequestResult requestResult = new AJAXRequestResult(fileHolder, "file");
            fileHolder = null;
            return requestResult;
        } finally {
            Streams.close(fileHolder);
        }
    }

    private long writeZipArchive(List<AttachmentId> attachmentIds, OutputStream out, IDBasedCalendarAccess calendarAccess) throws OXException {
        ZipArchiveOutputStream zipOutput = new ZipArchiveOutputStream(out);
        zipOutput.setEncoding("UTF-8");
        zipOutput.setUseLanguageEncodingFlag(true);
        try {
            // The buffer to use
            int buflen = 65536;
            byte[] buf = new byte[buflen];

            // The map for used names
            Map<String, Integer> fileNamesInArchive = new HashMap<String, Integer>();

            for (AttachmentId attachmentId : attachmentIds) {
                addAttachmentToArchive(attachmentId, zipOutput, buflen, buf, fileNamesInArchive, calendarAccess);
            }
            return zipOutput.getBytesWritten();
        } finally {
            // Complete the ZIP file
            Streams.close(zipOutput);
        }
    }

    private void addAttachmentToArchive(AttachmentId attachmentId, ZipArchiveOutputStream zipOutput, int buflen, byte[] buf, Map<String, Integer> fileNamesInArchive, IDBasedCalendarAccess calendarAccess) throws OXException {
        // Get the attachment and prepare the response
        IFileHolder attachment = null;
        try {
            attachment = calendarAccess.getAttachment(attachmentId.eventId, attachmentId.mid);

            // Get attachment's file name
            String name = attachment.getName();
            if (null == name) {
                MimeTypeMap mimeTypeMap = services.getOptionalService(MimeTypeMap.class);
                List<String> extensions = null == mimeTypeMap ? null : mimeTypeMap.getFileExtensions(attachment.getContentType());
                name = extensions == null || extensions.isEmpty() ? "attachment.dat" : "attachment." + extensions.get(0);
            }

            // Determine unique name for the ZIP entry
            String entryName = name;
            Integer count = fileNamesInArchive.get(name);
            if (null != count) {
                entryName = FileStorageUtility.enhance(name, count++);
                fileNamesInArchive.put(name, count);
            } else {
                fileNamesInArchive.put(name, 1);
            }
            ZipArchiveEntry entry = new ZipArchiveEntry(entryName);
            // TODO: entry.setTime(attachment.getCreationDate().getTime());
            zipOutput.putArchiveEntry(entry);

            // Transfer bytes from the attachment to the ZIP file
            InputStream in = attachment.getStream();
            try {
                long size = 0;
                for (int read; (read = in.read(buf, 0, buflen)) > 0;) {
                    zipOutput.write(buf, 0, read);
                    size += read;
                }
                entry.setSize(size);
            } finally {
                Streams.close(in);
            }

            // Complete the entry
            zipOutput.closeArchiveEntry();
        } catch (IOException e) {
            OXException oxe = OXException.general(e.getMessage(), e);
            if (IOs.isConnectionReset(e)) {
                /*-
                 * A "java.io.IOException: Connection reset by peer" is thrown when the other side has abruptly aborted the connection in midst of a transaction.
                 *
                 * That can have many causes which are not controllable from the Middleware side. E.g. the end-user decided to shutdown the client or change the
                 * server abruptly while still interacting with your server, or the client program has crashed, or the enduser's Internet connection went down,
                 * or the enduser's machine crashed, etc, etc.
                 */
                oxe.markLightWeight();
            }
            throw oxe;
        } finally {
            Streams.close(attachment);
        }

    }

    private List<AttachmentId> getAttachmentIds(AJAXRequestData requestData) throws OXException {
        JSONValue jBody = requestData.getData(JSONValue.class);
        if (null == jBody) {
            EventID eventId = parseIdParameter(requestData);
            String managedId = requestData.getParameter("managedId");
            int mid = Integer.parseInt(managedId);
            return Collections.singletonList(new AttachmentId(mid, eventId));
        }

        try {
            if (jBody.isObject()) {
                return Collections.singletonList(parseAttachmentId(jBody.toObject()));
            }

            JSONArray jAttachments = jBody.toArray();
            int length = jAttachments.length();
            List<AttachmentId> attachmentIds = new ArrayList<>(length);
            for (int i = 0; i < length; i++) {
                JSONObject jAttachment = jAttachments.optJSONObject(i);
                if (null != jAttachment) {
                    attachmentIds.add(parseAttachmentId(jAttachment));
                } else {
                    EventID eventId = parseIdParameter(requestData);
                    String managedId = jAttachments.getString(i);
                    int mid = Integer.parseInt(managedId);
                    attachmentIds.add(new AttachmentId(mid, eventId));
                }
            }
            return attachmentIds;
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private AttachmentId parseAttachmentId(JSONObject jAttachmentId) throws OXException, JSONException {
        EventID eventId = parseIdParameter(jAttachmentId);
        int mid = jAttachmentId.getInt("managedId");
        return new AttachmentId(mid, eventId);
    }

    // ------------------------------------------------------------------------------------------------------------------------------

    private static class AttachmentId {

        final EventID eventId;
        final int mid;

        AttachmentId(int mid, EventID eventId) {
            super();
            this.mid = mid;
            this.eventId = eventId;
        }
    }

}
