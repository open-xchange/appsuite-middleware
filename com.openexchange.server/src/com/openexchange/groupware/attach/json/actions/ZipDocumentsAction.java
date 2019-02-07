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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.groupware.attach.json.actions;

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
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.helper.DownloadUtility;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageUtility;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.java.IOs;
import com.openexchange.java.Streams;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.server.ServiceLookup;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ZipDocumentsAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.2
 */
public class ZipDocumentsAction extends AbstractAttachmentAction {

    /**
     * Initializes a new {@link ZipDocumentsAction}.
     */
    public ZipDocumentsAction(ServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        List<AttachmentInfo> attachmentInfos = parseAttachmentInfos(requestData);
        return createZipArchive("attachments.zip", attachmentInfos, requestData, session);
    }

    private AJAXRequestResult createZipArchive(String fullFileName, List<AttachmentInfo> attachmentInfos, AJAXRequestData requestData, ServerSession session) throws OXException {
        try {
            ATTACHMENT_BASE.startTransaction();

            if (requestData.setResponseHeader("Content-Type", "application/zip")) {
                try {
                    // Prepare further response headers
                    StringBuilder sb = new StringBuilder(512);
                    sb.append("attachment");
                    DownloadUtility.appendFilenameParameter(fullFileName, "application/zip", requestData.getUserAgent(), sb);
                    requestData.setResponseHeader("Content-Disposition", sb.toString());

                    // Create ZIP archive
                    long bytesWritten = writeZipArchive(attachmentInfos, requestData.optOutputStream(), session);

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
                writeZipArchive(attachmentInfos, fileHolder.asOutputStream(), session);

                requestData.setFormat("file");
                AJAXRequestResult requestResult = new AJAXRequestResult(fileHolder, "file");
                fileHolder = null;
                return requestResult;
            } finally {
                Streams.close(fileHolder);
            }
        } finally {
            try {
                ATTACHMENT_BASE.finish();
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    private long writeZipArchive(List<AttachmentInfo> attachmentInfos, OutputStream out, ServerSession session) throws OXException {
        ZipArchiveOutputStream zipOutput = new ZipArchiveOutputStream(out);
        zipOutput.setEncoding("UTF-8");
        zipOutput.setUseLanguageEncodingFlag(true);
        try {
            // The buffer to use
            int buflen = 65536;
            byte[] buf = new byte[buflen];

            // The map for used names
            Map<String, Integer> fileNamesInArchive = new HashMap<String, Integer>();

            for (AttachmentInfo attachmentInfo : attachmentInfos) {
                addAttachmentToArchive(attachmentInfo, zipOutput, buflen, buf, fileNamesInArchive, session);
            }
            return zipOutput.getBytesWritten();
        } finally {
            // Complete the ZIP file
            Streams.close(zipOutput);
        }
    }

    private void addAttachmentToArchive(AttachmentInfo attachmentInfo, ZipArchiveOutputStream zipOutput, int buflen, byte[] buf, Map<String, Integer> fileNamesInArchive, ServerSession session) throws OXException {
        AttachmentMetadata attachment = ATTACHMENT_BASE.getAttachment(session, attachmentInfo.folderId, attachmentInfo.attachedId, attachmentInfo.moduleId, attachmentInfo.id, session.getContext(), session.getUser(), session.getUserConfiguration());
        InputStream in = ATTACHMENT_BASE.getAttachedFile(session, attachmentInfo.folderId, attachmentInfo.attachedId, attachmentInfo.moduleId, attachmentInfo.id, session.getContext(), session.getUser(), session.getUserConfiguration());
        try {
            String name = attachment.getFilename();
            if (null == name) {
                final List<String> extensions = MimeType2ExtMap.getFileExtensions(attachment.getFileMIMEType());
                name = extensions == null || extensions.isEmpty() ? "attachment.dat" : "attachment." + extensions.get(0);
            }
            String entryName = name;
            Integer count = fileNamesInArchive.get(name);
            if (null != count) {
                entryName = FileStorageUtility.enhance(name, count++);
                fileNamesInArchive.put(name, count);
            } else {
                fileNamesInArchive.put(name, 1);
            }
            ZipArchiveEntry entry = new ZipArchiveEntry(entryName);
            entry.setTime(attachment.getCreationDate().getTime());
            zipOutput.putArchiveEntry(entry);
            /*
             * Transfer bytes from the file to the ZIP file
             */
            long size = 0;
            for (int read; (read = in.read(buf, 0, buflen)) > 0;) {
                zipOutput.write(buf, 0, read);
                size += read;
            }
            entry.setSize(size);
            /*
             * Complete the entry
             */
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
            Streams.close(in);
        }
    }

    private List<AttachmentInfo> parseAttachmentInfos(AJAXRequestData requestData) throws OXException {
        JSONValue jBody = requestData.getData(JSONValue.class);
        if (null == jBody) {
            int folderId = requireNumber(requestData, AJAXServlet.PARAMETER_FOLDERID);
            int attachedId = requireNumber(requestData, AJAXServlet.PARAMETER_ATTACHEDID);
            int moduleId = requireNumber(requestData, AJAXServlet.PARAMETER_MODULE);
            int id = requireNumber(requestData, AJAXServlet.PARAMETER_ID);
            return Collections.singletonList(new AttachmentInfo(folderId, attachedId, moduleId, id));
        }

        try {
            {
                JSONObject jAttachment = jBody.toObject();
                if (null != jAttachment) {
                    return Collections.singletonList(fromJson(jAttachment));
                }
            }

            JSONArray jAttachments = jBody.toArray();
            int length = jAttachments.length();
            List<AttachmentInfo> attachmentInfos = new ArrayList<ZipDocumentsAction.AttachmentInfo>(length);
            for (int i = 0; i < length; i++) {
                JSONObject jAttachment = jAttachments.optJSONObject(i);
                if (null != jAttachment) {
                    attachmentInfos.add(fromJson(jAttachment));
                } else {
                    // Expect folder, object and module as URL parameter and attachment identifier from array element
                    int folderId = requireNumber(requestData, AJAXServlet.PARAMETER_FOLDERID);
                    int attachedId = requireNumber(requestData, AJAXServlet.PARAMETER_ATTACHEDID);
                    int moduleId = requireNumber(requestData, AJAXServlet.PARAMETER_MODULE);
                    int id = jAttachments.getInt(i);
                    attachmentInfos.add(new AttachmentInfo(folderId, attachedId, moduleId, id));
                }
            }
            return attachmentInfos;
        } catch (JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private static AttachmentInfo fromJson(JSONObject jAttachment) throws JSONException {
        return new AttachmentInfo(
            jAttachment.getInt(AJAXServlet.PARAMETER_FOLDERID),
            jAttachment.getInt(AJAXServlet.PARAMETER_ATTACHEDID),
            jAttachment.getInt(AJAXServlet.PARAMETER_MODULE),
            jAttachment.getInt(AJAXServlet.PARAMETER_ID));
    }

    // -------------------------------------------------------------------------------------------------------------------------------------

    private static class AttachmentInfo {

        final int folderId;
        final int attachedId;
        final int moduleId;
        final int id;

        AttachmentInfo(int folderId, int attachedId, int moduleId, int id) {
            super();
            this.folderId = folderId;
            this.attachedId = attachedId;
            this.moduleId = moduleId;
            this.id = id;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder(32);
            builder.append("[folderId=").append(folderId).append(", attachedId=").append(attachedId).append(", moduleId=").append(moduleId).append(", id=").append(id).append("]");
            return builder.toString();
        }
    }

}
