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

import static com.openexchange.groupware.upload.impl.UploadUtility.getSize;
import static com.openexchange.java.Strings.isEmpty;
import java.io.EOFException;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.apache.commons.fileupload.FileItemHeaders;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.FileUploadBase.FileSizeLimitExceededException;
import org.apache.commons.fileupload.FileUploadBase.FileUploadIOException;
import org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException;
import org.apache.james.mime4j.field.contenttype.parser.ContentTypeParser;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.upload.EmptyStreamedUploadFileIterator;
import com.openexchange.groupware.upload.StreamedUpload;
import com.openexchange.groupware.upload.StreamedUploadFile;
import com.openexchange.groupware.upload.StreamedUploadFileIterator;
import com.openexchange.groupware.upload.StreamedUploadFileListener;
import com.openexchange.java.Streams;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.session.Session;


/**
 * {@link MultipartStreamedUpload}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.10.1
 */
public class MultipartStreamedUpload implements StreamedUpload {
    
    /**
     * Thrown in case <code>requireStartingFormField</code> parameter is set to <code>true</code> and multipart upload does not start with a
     * simple form field.
     */
    public static final class MissingStartingFormField extends RuntimeException {

        private final FileItemStream item;

        MissingStartingFormField(FileItemStream item) {
            super("missing starting simple form field");
            this.item = item;
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }
        
        /**
         * Gets the first file item, which is <b>not</b> a simple form field.
         *
         * @return The file item
         */
        public FileItemStream getItem() {
            return item;
        }
    }
    
    // -------------------------------------------------------------------------------------------------------------------------------------

    private final Map<String, String> formFields;
    private boolean iteratorCreated;
    final FileItemIterator iter;
    final String uuid;
    final List<StreamedUploadFileListener> listeners;
    final Session session;
    final String fileName;
    final String charEnc;
    final String action;
    FileItemStream current;

    /**
     * Initializes a new {@link MultipartStreamedUpload}.
     * 
     * @throws MissingStartingFormField If <code>requireStartingFormField</code> parameter is set to <code>true</code> and multipart upload
     * does not start with a simple form field.
     */
    public MultipartStreamedUpload(FileItemIterator iter, String uuid, List<StreamedUploadFileListener> listeners, String action, String fileName, String requestCharacterEncoding, boolean requireStartingFormField, Session session) throws OXException {
        super();
        formFields = new HashMap<String, String>();
        iteratorCreated = false;
        this.iter = iter;
        this.uuid = uuid;
        this.listeners = listeners;
        this.action = action;
        this.fileName = fileName;
        this.charEnc = null == requestCharacterEncoding ? ServerConfig.getProperty(Property.DefaultEncoding) : requestCharacterEncoding;
        this.session = session;

        // Consume form fields
        try {
            while (null == current && iter.hasNext()) {
                FileItemStream item = iter.next();
                if (item.isFormField()) {
                    addFormField(item.getFieldName(), Streams.stream2string(item.openStream(), charEnc));
                } else {
                    if (requireStartingFormField && formFields.isEmpty()) {
                        // No simple form field added, yet
                        throw new MissingStartingFormField(item);
                    }
                    String name = item.getName();
                    if (!isEmpty(name)) {
                        current = item;
                    }
                }
            }
        } catch (MissingStartingFormField e) {
            UploadException exception = UploadException.UploadCode.UPLOAD_FAILED.create(e, action);
            for (StreamedUploadFileListener listener : listeners) {
                listener.onUploadFailed(uuid, exception, session);
            }
            throw e;
        } catch (Exception e) {
            throw handleException(uuid, e, action, session, listeners);
        }
    }

    /**
     * Adds a form field's name-value-pair.
     *
     * @param name The field's name.
     * @param value The field's value.
     */
    public void addFormField(String name, String value) {
        if (null != name) {
            formFields.put(name, value);
        }
    }

    /**
     * Removes the form field whose name equals specified field name.
     *
     * @param name The field name.
     * @return The removed form field's value or <code>null</code>.
     */
    public String removeFormField(String name) {
        return null == name ? null : formFields.remove(name);
    }

    /**
     * Clears all form fields.
     */
    public void clearFormFields() {
        formFields.clear();
    }

    @Override
    public String getFormField(String fieldName) {
        return formFields.get(fieldName);
    }

    @Override
    public Iterator<String> getFormFieldNames() {
        return formFields.keySet().iterator();
    }

    @Override
    public boolean hasAny() {
        if (iteratorCreated) {
            // Already consumed
            throw new IllegalStateException("Already consumed");
        }

        return null != current;
    }

    @Override
    public StreamedUploadFileIterator getUploadFiles() {
        if (iteratorCreated) {
            // Already consumed
            throw new IllegalStateException("Already consumed");
        }

        iteratorCreated = true;
        FileItemStream item = current;
        if (null == item) {
            // No upload files
            return EmptyStreamedUploadFileIterator.getInstance();
        }

        return new FileItemStreamedUploadFileIterator(this);
    }

    // ------------------------------------------------------------------------------------------------------------------------

    private static class FileItemStreamedUploadFileIterator implements StreamedUploadFileIterator {

        private final MultipartStreamedUpload streamedUpload;
        private final String uuid;
        private final Session session;
        private final List<StreamedUploadFileListener> listeners;
        private final String action;
        private final FileItemIterator iter;

        /**
         * Initializes a new {@link MultipartStreamedUpload.FileItemStreamedUploadFileIterator}.
         */
        FileItemStreamedUploadFileIterator(MultipartStreamedUpload streamedUpload) {
            super();
            this.streamedUpload = streamedUpload;
            iter = streamedUpload.iter;
            uuid = streamedUpload.uuid;
            session = streamedUpload.session;
            listeners = streamedUpload.listeners;
            action = streamedUpload.action;
        }

        @Override
        public boolean hasNext() throws OXException {
            if (streamedUpload.current != null) {
                return true;
            }

            try {
                // Advance this iterator
                while (null == streamedUpload.current && iter.hasNext()) {
                    FileItemStream next = iter.next();
                    if (next.isFormField()) {
                        streamedUpload.addFormField(next.getFieldName(), Streams.stream2string(next.openStream(), streamedUpload.charEnc));
                    } else {
                        String name = next.getName();
                        if (!isEmpty(name)) {
                            streamedUpload.current = next;
                        }
                    }
                }

                if (null == streamedUpload.current) {
                    // No further uploads available. Signal success
                    for (StreamedUploadFileListener listener : listeners) {
                        listener.onUploadSuceeded(uuid, streamedUpload, session);
                    }
                }

                return (streamedUpload.current != null);
            } catch (Exception e) {
                throw handleException(uuid, e, action, session, listeners);
            }
        }

        @Override
        public StreamedUploadFile next() throws OXException {
            FileItemStream item = streamedUpload.current;
            if (null == item) {
                throw new NoSuchElementException();
            }
            streamedUpload.current = null;

            try {
                StreamedUploadFileImpl uploadFile = new StreamedUploadFileImpl();
                uploadFile.setFieldName(item.getFieldName());
                String fileName = isEmpty(streamedUpload.fileName) ? item.getName() : streamedUpload.fileName;
                uploadFile.setFileName(fileName);

                FileItemHeaders headers = item.getHeaders();
                String contentId = headers.getHeader("Content-Id");
                uploadFile.setContentId(contentId);

                // Deduce MIME type from passed file name
                String mimeType = MimeType2ExtMap.getContentType(fileName, null);

                // Set associated MIME type
                {
                    // Check if we are forced to select the MIME type as signaled by file item
                    String forcedMimeType = headers.getHeader("X-Forced-MIME-Type");
                    if (null == forcedMimeType) {
                        String itemContentType = item.getContentType();
                        ContentType contentType = UploadUtility.getContentTypeSafe(itemContentType);
                        if (null == contentId && contentType != null) {
                            contentId = contentType.getParameter("cid");
                            uploadFile.setContentId(contentId);
                        }

                        if (null == contentType) {
                            uploadFile.setContentType(null == mimeType ? itemContentType : mimeType);
                        } else {
                            uploadFile.setContentType(contentType.getBaseType());
                        }
                    } else if (AJAXRequestDataTools.parseBoolParameter(forcedMimeType)) {
                        uploadFile.setContentType(item.getContentType());
                    } else {
                        // Valid MIME type specified?
                        try {
                            ContentTypeParser parser = new ContentTypeParser(new StringReader(forcedMimeType));
                            parser.parseAll();
                            uploadFile.setContentType(new StringBuilder(parser.getType()).append('/').append(parser.getSubType()).toString());
                        } catch (Exception e) {
                            // Assume invalid value
                            uploadFile.setContentType(null == mimeType ? item.getContentType() : mimeType);
                        }
                    }
                }

                // Signal basic info prior to processing
                for (StreamedUploadFileListener listener : listeners) {
                    try {
                        listener.onBeforeUploadProcessed(uuid, fileName, uploadFile.getFieldName(), uploadFile.getContentType(), session);
                    } catch (OXException e) {
                        // Do not signal this OXException to listeners as it was created by one of the listeners itself
                        throw new DontHandleException(e);
                    }
                }

                // Assign opened stream
                uploadFile.setStream(item.openStream());
                item = null;

                // Signal success after processing
                for (StreamedUploadFileListener listener : listeners) {
                    try {
                        listener.onAfterUploadProcessed(uuid, uploadFile, session);
                    } catch (OXException e) {
                        // Do not signal this OXException to listeners as it was created by one of the listeners itself
                        throw new DontHandleException(e);
                    }
                }

                return uploadFile;
            } catch (DontHandleException e) {
                throw e.getOxException();
            } catch (Exception e) {
                throw handleException(uuid, e, action, session, listeners);
            }
        }
    }

    static OXException handleException(String uuid, Exception exception, String action, Session session, List<StreamedUploadFileListener> listeners) {
        if (exception instanceof FileSizeLimitExceededException) {
            FileSizeLimitExceededException e = (FileSizeLimitExceededException) exception;
            return handleOXException(uuid, UploadFileSizeExceededException.create(e.getActualSize(), e.getPermittedSize(), true), session, listeners);
        } else if (exception instanceof SizeLimitExceededException) {
            SizeLimitExceededException e = (SizeLimitExceededException) exception;
            return handleOXException(uuid, UploadSizeExceededException.create(e.getActualSize(), e.getPermittedSize(), true), session, listeners);
        } else if (exception instanceof FileUploadException) {
            FileUploadException e = (FileUploadException) exception;
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                String message = cause.getMessage();
                if (null != message && message.startsWith("Max. byte count of ")) {
                    // E.g. Max. byte count of 10240 exceeded.
                    int pos = message.indexOf(" exceeded", 19 + 1);
                    String limit = message.substring(19, pos);
                    return handleOXException(uuid, UploadException.UploadCode.MAX_UPLOAD_SIZE_EXCEEDED_UNKNOWN.create(cause, getSize(Long.parseLong(limit), 2, false, true)), session, listeners);
                }
            } else if (cause instanceof EOFException) {
                // Stream closed/ended unexpectedly
                return handleOXException(uuid, UploadException.UploadCode.UNEXPECTED_EOF.create(cause, cause.getMessage()), session, listeners);
            }
            return handleOXException(uuid, UploadException.UploadCode.UPLOAD_FAILED.create(e, null == cause ? e.getMessage() : (null == cause.getMessage() ? e.getMessage() : cause.getMessage())), session, listeners);
        } else if (exception instanceof FileUploadIOException) {
            FileUploadIOException e = (FileUploadIOException) exception;
            // Might wrap a size-limit-exceeded error
            Throwable cause = e.getCause();
            if (cause instanceof FileSizeLimitExceededException) {
                FileSizeLimitExceededException exc = (FileSizeLimitExceededException) cause;
                return handleOXException(uuid, UploadFileSizeExceededException.create(exc.getActualSize(), exc.getPermittedSize(), true), session, listeners);
            }
            if (cause instanceof SizeLimitExceededException) {
                SizeLimitExceededException exc = (SizeLimitExceededException) cause;
                return handleOXException(uuid, UploadSizeExceededException.create(exc.getActualSize(), exc.getPermittedSize(), true), session, listeners);
            }
            return handleOXException(uuid, UploadException.UploadCode.UPLOAD_FAILED.create(e, action), session, listeners);
        } else if (exception instanceof EOFException) {
            // Stream closed/ended unexpectedly
            EOFException e = (EOFException) exception;
            return handleOXException(uuid, UploadException.UploadCode.UNEXPECTED_EOF.create(e, e.getMessage()), session, listeners);
        } else if (exception instanceof IOException) {
            IOException e = (IOException) exception;
            Throwable cause = e.getCause();
            if (cause instanceof java.util.concurrent.TimeoutException) {
                return handleOXException(uuid, UploadException.UploadCode.UNEXPECTED_TIMEOUT.create(e, new Object[0]), session, listeners);
            }
            return handleOXException(uuid, UploadException.UploadCode.UPLOAD_FAILED.create(e, action), session, listeners);
        } else if (exception instanceof OXException) {
            OXException e = (OXException) exception;
            return handleOXException(uuid, e, session, listeners);
        } else {
            return handleOXException(uuid, UploadException.UploadCode.UPLOAD_FAILED.create(exception, action), session, listeners);
        }
    }

    /**
     * Advertises specified exception to listeners and re-throws it
     *
     * @param uuid The upload's UUID
     * @param exception The exception to advertise
     * @param session The associated session
     * @param listeners The listeners to notify
     */
    static OXException handleOXException(String uuid, OXException exception, Session session, List<StreamedUploadFileListener> listeners) {
        for (StreamedUploadFileListener listener : listeners) {
            listener.onUploadFailed(uuid, exception, session);
        }
        return exception;
    }

}
