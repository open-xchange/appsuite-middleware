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

package com.openexchange.groupware.upload.impl;

import java.io.InputStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.upload.StreamedUpload;
import com.openexchange.groupware.upload.StreamedUploadFile;
import com.openexchange.groupware.upload.StreamedUploadFileIterator;
import com.openexchange.groupware.upload.StreamedUploadFileListener;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.session.Session;


/**
 * {@link SingleStreamedUpload}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.1
 */
public class SingleStreamedUpload implements StreamedUpload {

    InputStream in;
    final String contentType;
    final String fileName;
    final List<StreamedUploadFileListener> listeners;
    final String uuid;
    final String action;
    final Session session;

    /**
     * Initializes a new {@link SingleStreamedUpload}.
     */
    public SingleStreamedUpload(InputStream in, String contentType, String fileName, String uuid, List<StreamedUploadFileListener> listeners, String action, Session session) {
        super();
        this.in = in;
        this.contentType = contentType;
        this.fileName = fileName;
        this.uuid = uuid;
        this.listeners = listeners;
        this.action = action;
        this.session = session;
    }

    @Override
    public String getFormField(String fieldName) {
        return null;
    }

    @Override
    public String getFirstFormField() {
        return null;
    }

    @Override
    public Iterator<String> getFormFieldNames() {
        return Collections.emptyIterator();
    }

    @Override
    public boolean hasAny() {
        if (null == in) {
            // Already consumed
            throw new IllegalStateException("Already consumed");
        }

        return true;
    }

    @Override
    public StreamedUploadFileIterator getUploadFiles() {
        if (null == in) {
            // Already consumed
            throw new IllegalStateException("Already consumed");
        }

        return new SingleStreamedUploadFileIterator(this);
    }

    // ------------------------------------------------------------------------------------------------------------------------

    private static class SingleStreamedUploadFileIterator implements StreamedUploadFileIterator {

        private final SingleStreamedUpload streamedUpload;
        private final String fileName;
        private final String contentType;
        private final List<StreamedUploadFileListener> listeners;

        /**
         * Initializes a new {@link SingleStreamedUpload.SingleStreamedUploadFileIterator}.
         */
        SingleStreamedUploadFileIterator(SingleStreamedUpload streamedUpload) {
            super();
            this.streamedUpload = streamedUpload;
            fileName = streamedUpload.fileName;
            contentType = streamedUpload.contentType;
            listeners = streamedUpload.listeners;
        }

        @Override
        public boolean hasNext() {
            return streamedUpload.in != null;
        }

        @Override
        public StreamedUploadFile next() throws OXException {
            InputStream in = streamedUpload.in;
            if (null == in) {
                throw new NoSuchElementException();
            }

            streamedUpload.in = null;

            try {
                StreamedUploadFileImpl uploadFile = new StreamedUploadFileImpl();
                uploadFile.setFileName(fileName);

                // Deduce MIME type from passed file name
                String mimeType = MimeType2ExtMap.getContentType(fileName, null);

                // Set associated MIME type
                {
                    // Check if we are forced to select the MIME type as signaled by file item
                    ContentType safeContentType = UploadUtility.getContentTypeSafe(contentType);
                    if (null == safeContentType) {
                        uploadFile.setContentType(null == mimeType ? contentType : mimeType);
                    } else {
                        uploadFile.setContentType(safeContentType.getBaseType());
                    }
                }

                // Signal basic info prior to processing
                for (StreamedUploadFileListener listener : listeners) {
                    try {
                        listener.onBeforeUploadProcessed(streamedUpload.uuid, fileName, uploadFile.getFieldName(), uploadFile.getContentType(), streamedUpload.session);
                    } catch (OXException e) {
                        // Do not signal this OXException to listeners as it was created by one of the listeners itself
                        throw new DontHandleException(e);
                    }
                }

                // Assign opened stream
                uploadFile.setStream(in);

                // Signal success after processing
                for (StreamedUploadFileListener listener : listeners) {
                    try {
                        listener.onAfterUploadProcessed(streamedUpload.uuid, uploadFile, streamedUpload.session);
                    } catch (OXException e) {
                        // Do not signal this OXException to listeners as it was created by one of the listeners itself
                        throw new DontHandleException(e);
                    }
                }

                // No further uploads available. Signal success
                for (StreamedUploadFileListener listener : listeners) {
                    listener.onUploadSuceeded(streamedUpload.uuid, streamedUpload, streamedUpload.session);
                }

                return uploadFile;
            } catch (DontHandleException e) {
                throw e.getOxException();
            } catch (Exception e) {
                throw MultipartStreamedUpload.handleException(streamedUpload.uuid, e, streamedUpload.action, streamedUpload.session, listeners);
            }
        }

    }

}
