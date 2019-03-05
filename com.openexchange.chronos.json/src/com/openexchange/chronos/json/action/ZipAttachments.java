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
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.zip.Buffer;
import com.openexchange.ajax.zip.ZipArchiveOutputStreamProvider;
import com.openexchange.ajax.zip.ZipEntryAdder;
import com.openexchange.ajax.zip.ZipUtility;
import com.openexchange.chronos.json.fields.ChronosJsonFields;
import com.openexchange.chronos.provider.composition.IDBasedCalendarAccess;
import com.openexchange.chronos.service.EventID;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.FileStorageUtility;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.java.UnsynchronizedStringReader;
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
        if (attachmentIds.isEmpty()) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create(ChronosJsonFields.Attachment.MANAGED_ID);
        }
        return createZipArchive("attachments.zip", attachmentIds, calendarAccess, requestData);
    }

    private AJAXRequestResult createZipArchive(String fullFileName, List<AttachmentId> attachmentIds, IDBasedCalendarAccess calendarAccess, AJAXRequestData requestData) throws OXException {
        ZipEntryAdder adder = new ChronosAttachmentZipEntryAdder(calendarAccess, attachmentIds, services);
        return ZipUtility.createZipArchive(adder, fullFileName, 0, requestData);
    }

    private List<AttachmentId> getAttachmentIds(AJAXRequestData requestData) throws OXException {
        try {
            JSONValue jBody = requestData.getData(JSONValue.class);
            if (null == jBody) {
                String value = requestData.getParameter("body");
                if (Strings.isEmpty(value)) {
                    EventID eventId = parseIdParameter(requestData);
                    int managedId = parseAttachmentId(requestData);
                    return Collections.singletonList(new AttachmentId(managedId, eventId));
                }

                jBody = JSONObject.parse(new UnsynchronizedStringReader(value));
            }

            if (jBody.isObject()) {
                return Collections.singletonList(parseAttachmentId(jBody.toObject()));
            }

            JSONArray jAttachments = jBody.toArray();
            int length = jAttachments.length();
            if (length <= 0) {
                return Collections.emptyList();
            }
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

    private static class ChronosAttachmentZipEntryAdder implements ZipEntryAdder {

        private final IDBasedCalendarAccess calendarAccess;
        private final List<AttachmentId> attachmentIds;
        private final ServiceLookup services;

        /**
         * Initializes a new {@link ZipEntryAdderImplementation}.
         */
        ChronosAttachmentZipEntryAdder(IDBasedCalendarAccess calendarAccess, List<AttachmentId> attachmentIds, ServiceLookup services) {
            super();
            this.calendarAccess = calendarAccess;
            this.attachmentIds = attachmentIds;
            this.services = services;
        }

        @Override
        public void addZipEntries(ZipArchiveOutputStreamProvider zipOutputProvider, Buffer buffer, Map<String, Integer> fileNamesInArchive) throws OXException {
            for (AttachmentId attachmentId : attachmentIds) {
                addAttachmentToArchive(attachmentId, zipOutputProvider, buffer.getBuflen(), buffer.getBuf(), fileNamesInArchive, calendarAccess);
            }
        }

        private void addAttachmentToArchive(AttachmentId attachmentId, ZipArchiveOutputStreamProvider zipOutputProvider, int buflen, byte[] buf, Map<String, Integer> fileNamesInArchive, IDBasedCalendarAccess calendarAccess) throws OXException {
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
                    count = Integer.valueOf(count.intValue() + 1);
                    entryName = FileStorageUtility.enhance(name, count.intValue());
                    fileNamesInArchive.put(name, count);
                } else {
                    fileNamesInArchive.put(name, Integer.valueOf(1));
                }
                ZipArchiveEntry entry = new ZipArchiveEntry(entryName);
                // TODO: entry.setTime(attachment.getCreationDate().getTime());
                ZipArchiveOutputStream zipOutput = zipOutputProvider.getZipArchiveOutputStream();
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
                throw handleIOException(e);
            } finally {
                Streams.close(attachment);
            }
        }

    }

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
