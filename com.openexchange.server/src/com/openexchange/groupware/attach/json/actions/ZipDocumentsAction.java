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

package com.openexchange.groupware.attach.json.actions;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.zip.Buffer;
import com.openexchange.ajax.zip.ZipArchiveOutputStreamProvider;
import com.openexchange.ajax.zip.ZipEntryAdder;
import com.openexchange.ajax.zip.ZipUtility;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageUtility;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.java.UnsynchronizedStringReader;
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
     *
     * @param serviceLookup The service look-up
     */
    public ZipDocumentsAction(ServiceLookup serviceLookup) {
        super(serviceLookup);
    }

    @Override
    public AJAXRequestResult perform(AJAXRequestData requestData, ServerSession session) throws OXException {
        List<AttachmentInfo> attachmentInfos = parseAttachmentInfos(requestData);
        if (attachmentInfos.isEmpty()) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(AJAXServlet.PARAMETER_ID);
        }
        return createZipArchive("attachments.zip", attachmentInfos, requestData, session);
    }

    private AJAXRequestResult createZipArchive(String fullFileName, List<AttachmentInfo> attachmentInfos, AJAXRequestData requestData, ServerSession session) throws OXException {
        try {
            ATTACHMENT_BASE.startTransaction();

            ZipEntryAdder adder = new AttachmentZipEntryAdder(attachmentInfos, session, ATTACHMENT_BASE);
            return ZipUtility.createZipArchive(adder, fullFileName, 0, requestData);
        } finally {
            try {
                ATTACHMENT_BASE.finish();
            } catch (Exception e) {
                // Ignore
            }
        }
    }

    private List<AttachmentInfo> parseAttachmentInfos(AJAXRequestData requestData) throws OXException {
        try {
            JSONValue jBody = requestData.getData(JSONValue.class);
            if (null == jBody) {
                String value = requestData.getParameter("body");
                if (Strings.isEmpty(value)) {
                    int folderId = requireNumber(requestData, AJAXServlet.PARAMETER_FOLDERID);
                    int attachedId = requireNumber(requestData, AJAXServlet.PARAMETER_ATTACHEDID);
                    int moduleId = requireNumber(requestData, AJAXServlet.PARAMETER_MODULE);
                    int id = requireNumber(requestData, AJAXServlet.PARAMETER_ID);
                    return Collections.singletonList(new AttachmentInfo(folderId, attachedId, moduleId, id));
                }

                jBody = JSONObject.parse(new UnsynchronizedStringReader(value));
            }

            {
                JSONObject jAttachment = jBody.toObject();
                if (null != jAttachment) {
                    return Collections.singletonList(fromJson(jAttachment));
                }
            }

            JSONArray jAttachments = jBody.toArray();
            int length = jAttachments.length();
            if (length <= 0) {
                return Collections.emptyList();
            }
            List<AttachmentInfo> attachmentInfos = new ArrayList<ZipDocumentsAction.AttachmentInfo>(length);
            AttachmentInfo helper = null;
            for (int i = 0; i < length; i++) {
                JSONObject jAttachment = jAttachments.optJSONObject(i);
                if (null != jAttachment) {
                    attachmentInfos.add(fromJson(jAttachment));
                } else {
                    // Expect folder, object and module as URL parameter and attachment identifier from array element
                    if (helper == null) {
                        int folderId = requireNumber(requestData, AJAXServlet.PARAMETER_FOLDERID);
                        int attachedId = requireNumber(requestData, AJAXServlet.PARAMETER_ATTACHEDID);
                        int moduleId = requireNumber(requestData, AJAXServlet.PARAMETER_MODULE);
                        helper = new AttachmentInfo(folderId, attachedId, moduleId, -1);
                    }
                    int id = jAttachments.getInt(i);
                    attachmentInfos.add(new AttachmentInfo(helper.folderId, helper.attachedId, helper.moduleId, id));
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

    private static class AttachmentZipEntryAdder implements ZipEntryAdder {

        private final List<AttachmentInfo> attachmentInfos;
        private final ServerSession session;
        private final AttachmentBase attachmentBase;

        /**
         * Initializes a new {@link ZipEntryAdderImplementation}.
         */
        AttachmentZipEntryAdder(List<AttachmentInfo> attachmentInfos, ServerSession session, AttachmentBase attachmentBase) {
            this.attachmentInfos = attachmentInfos;
            this.session = session;
            this.attachmentBase = attachmentBase;
        }

        @Override
        public void addZipEntries(ZipArchiveOutputStreamProvider zipOutputProvider, Buffer buffer, Map<String, Integer> fileNamesInArchive) throws OXException {
            for (AttachmentInfo attachmentInfo : attachmentInfos) {
                addAttachmentToArchive(attachmentInfo, zipOutputProvider, buffer.getBuflen(), buffer.getBuf(), fileNamesInArchive, session);
            }
        }

        private void addAttachmentToArchive(AttachmentInfo attachmentInfo, ZipArchiveOutputStreamProvider zipOutputProvider, int buflen, byte[] buf, Map<String, Integer> fileNamesInArchive, ServerSession session) throws OXException {
            AttachmentMetadata attachment = attachmentBase.getAttachment(session, attachmentInfo.folderId, attachmentInfo.attachedId, attachmentInfo.moduleId, attachmentInfo.id, session.getContext(), session.getUser(), session.getUserConfiguration());
            InputStream in = attachmentBase.getAttachedFile(session, attachmentInfo.folderId, attachmentInfo.attachedId, attachmentInfo.moduleId, attachmentInfo.id, session.getContext(), session.getUser(), session.getUserConfiguration());
            try {
                String name = attachment.getFilename();
                if (null == name) {
                    final List<String> extensions = MimeType2ExtMap.getFileExtensions(attachment.getFileMIMEType());
                    name = extensions == null || extensions.isEmpty() ? "attachment.dat" : "attachment." + extensions.get(0);
                }
                String entryName = name;
                Integer count = fileNamesInArchive.get(name);
                if (null != count) {
                    count = Integer.valueOf(count.intValue() + 1);
                    entryName = FileStorageUtility.enhance(name, count.intValue());
                    fileNamesInArchive.put(name, count);
                } else {
                    fileNamesInArchive.put(name, Integer.valueOf(1));
                }
                ZipArchiveEntry entry = new ZipArchiveEntry(entryName);
                entry.setTime(attachment.getCreationDate().getTime());
                ZipArchiveOutputStream zipOutput = zipOutputProvider.getZipArchiveOutputStream();
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
                throw handleIOException(e);
            } finally {
                Streams.close(in);
            }
        }
    }

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
