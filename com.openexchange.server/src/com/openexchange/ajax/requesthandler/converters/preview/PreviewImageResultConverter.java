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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.ajax.requesthandler.converters.preview;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import com.openexchange.ajax.container.ByteArrayFileHolder;
import com.openexchange.ajax.container.FileHolder;
import com.openexchange.ajax.container.IFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.conversion.DataProperties;
import com.openexchange.conversion.SimpleData;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.java.StringAllocator;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.preview.ContentTypeChecker;
import com.openexchange.preview.PreviewDocument;
import com.openexchange.preview.PreviewExceptionCodes;
import com.openexchange.preview.PreviewOutput;
import com.openexchange.preview.PreviewService;
import com.openexchange.preview.cache.CachedPreview;
import com.openexchange.preview.cache.PreviewCache;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link PreviewImageResultConverter}
 *
 * @author <a href="mailto:martin.herfurth@open-xchange.com">Martin Herfurth</a>
 */
public class PreviewImageResultConverter extends AbstractPreviewResultConverter {

    /**
     * Initializes a new {@link PreviewImageResultConverter}.
     */
    public PreviewImageResultConverter() {
        super();
    }

    @Override
    public String getOutputFormat() {
        return "preview_image";
    }

    @Override
    public Quality getQuality() {
        return Quality.GOOD;
    }

    @Override
    public PreviewOutput getOutput() {
        return PreviewOutput.IMAGE;
    }

    @Override
    public void convert(final AJAXRequestData requestData, final AJAXRequestResult result, final ServerSession session, final Converter converter) throws OXException {
        try {
            // Check cache first
            final PreviewCache previewCache = AbstractPreviewResultConverter.getPreviewCache();
            final String eTag = requestData.getETag();
            final boolean isValidEtag = !isEmpty(eTag);
            if (null != previewCache && isValidEtag) {
                final String cacheKey = generatePreviewCacheKey(eTag, requestData, new String[0]);
                final CachedPreview cachedPreview = previewCache.get(cacheKey, session.getUserId(), session.getContextId());
                if (null != cachedPreview) {
                    requestData.setFormat("file");
                    // Create appropriate IFileHolder
                    String contentType = cachedPreview.getFileType();
                    if (null == contentType) {
                        contentType = "image/jpeg";
                    }
                    final InputStream inputStream = cachedPreview.getInputStream();
                    if (null == inputStream) {
                        final ByteArrayFileHolder responseFileHolder = new ByteArrayFileHolder(cachedPreview.getBytes());
                        responseFileHolder.setContentType(contentType);
                        responseFileHolder.setName(cachedPreview.getFileName());
                        result.setResultObject(responseFileHolder, "file");
                    } else {
                        final FileHolder responseFileHolder = new FileHolder(inputStream, cachedPreview.getSize(), contentType, cachedPreview.getFileName());
                        result.setResultObject(responseFileHolder, "file");
                    }
                    return;
                }
            }
            // No cached preview available
            final Object resultObject = result.getResultObject();
            if (!(resultObject instanceof IFileHolder)) {
                throw AjaxExceptionCodes.UNEXPECTED_RESULT.create(IFileHolder.class.getSimpleName(), null == resultObject ? "null" : resultObject.getClass().getSimpleName());
            }
            final IFileHolder fileHolder = (IFileHolder) resultObject;

            final PreviewService previewService = ServerServiceRegistry.getInstance().getService(PreviewService.class);

            final DataProperties dataProperties = new DataProperties(7);
            dataProperties.put(DataProperties.PROPERTY_CONTENT_TYPE, getContentType(fileHolder, previewService instanceof ContentTypeChecker ? (ContentTypeChecker) previewService : null));
            dataProperties.put(DataProperties.PROPERTY_DISPOSITION, fileHolder.getDisposition());
            dataProperties.put(DataProperties.PROPERTY_NAME, fileHolder.getName());
            dataProperties.put(DataProperties.PROPERTY_SIZE, Long.toString(fileHolder.getLength()));
            dataProperties.put("PreviewType", requestData.getModule().equals("files") ? "DetailView" : "Thumbnail");
            dataProperties.put("PreviewWidth", requestData.getParameter("width"));
            dataProperties.put("PreviewHeight", requestData.getParameter("height"));
            final PreviewDocument previewDocument = previewService.getPreviewFor(new SimpleData<InputStream>(fileHolder.getStream(), dataProperties), getOutput(), session, 1);

            requestData.setFormat("file");

            InputStream thumbnail = previewDocument.getThumbnail();
            if (null == thumbnail) {
                // No thumbnail available
                throw PreviewExceptionCodes.THUMBNAIL_NOT_AVAILABLE.create();
            }
            // (Asynchronously) Put to cache if ETag is available
            final String fileName = previewDocument.getMetaData().get("resourcename");
            int size = -1;
            if (null != previewCache && isValidEtag) {
                final byte[] bytes = Streams.stream2bytes(thumbnail);
                thumbnail = Streams.newByteArrayInputStream(bytes);
                size = bytes.length;
                // Specify task
                final String cacheKey = generatePreviewCacheKey(eTag, requestData, new String[0]);
                final AbstractTask<Void> task = new AbstractTask<Void>() {

                    @Override
                    public Void call() throws OXException {
                        final CachedPreview preview = new CachedPreview(bytes, fileName, "image/jpeg", bytes.length);
                        previewCache.save(cacheKey, preview, session.getUserId(), session.getContextId());
                        return null;
                    }
                };
                // Acquire thread pool service
                final ThreadPoolService threadPool = ServerServiceRegistry.getInstance().getService(ThreadPoolService.class);
                if (null == threadPool) {
                    final Thread thread = Thread.currentThread();
                    boolean ran = false;
                    task.beforeExecute(thread);
                    try {
                        task.call();
                        ran = true;
                        task.afterExecute(null);
                    } catch (final Exception ex) {
                        if (!ran) {
                            task.afterExecute(ex);
                        }
                        // Else the exception occurred within
                        // afterExecute itself in which case we don't
                        // want to call it again.
                        throw (ex instanceof OXException ? (OXException) ex : AjaxExceptionCodes.UNEXPECTED_ERROR.create(ex, ex.getMessage()));
                    }
                } else {
                    threadPool.submit(task);
                }
            }
            // Set response object
            final FileHolder responseFileHolder = new FileHolder(thumbnail, size, "image/jpeg", fileName);
            result.setResultObject(responseFileHolder, "file");
        } catch (final IOException e) {
            throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        }
    }

    private static final Set<String> INVALIDS = Collections.<String> unmodifiableSet(new HashSet<String>(Arrays.asList(
        "application/octet-stream",
        "application/force-download",
        "application/binary",
        "application/x-download",
        "application/octet-stream",
        "application/vnd",
        "application/vnd.ms-word.document.12n",
        "application/vnd.ms-word.document.12",
        "application/odt",
        "application/x-pdf")));

    private String getContentType(final IFileHolder fileHolder, final ContentTypeChecker checker) {
        String contentType = fileHolder.getContentType();
        if (isEmpty(contentType)) {
            // Determine Content-Type by file name
            return MimeType2ExtMap.getContentType(fileHolder.getName());
        }
        // Cut to base type & sanitize
        contentType = sanitizeContentType(getLowerCaseBaseType(contentType));
        if (INVALIDS.contains(contentType) || (null != checker && !checker.isValid(contentType))) {
            // Determine Content-Type by file name
            contentType = MimeType2ExtMap.getContentType(fileHolder.getName());
        }
        return contentType == null ? "application/octet-stream" : contentType;
    }

    private String sanitizeContentType(final String contentType) {
        if (null == contentType) {
            return null;
        }
        try {
            return new ContentType(contentType).toString();
        } catch (final OXException e) {
            return contentType;
        }
    }

    private String getLowerCaseBaseType(final String contentType) {
        if (null == contentType) {
            return null;
        }
        final int pos = contentType.indexOf(';');
        return toLowerCase(pos > 0 ? contentType.substring(0, pos) : contentType).trim();
    }

    /** ASCII-wise to lower-case */
    private String toLowerCase(final CharSequence chars) {
        final int length = chars.length();
        final StringAllocator builder = new StringAllocator(length);
        for (int i = 0; i < length; i++) {
            final char c = chars.charAt(i);
            builder.append((c >= 'A') && (c <= 'Z') ? (char) (c ^ 0x20) : c);
        }
        return builder.toString();
    }

    /** Checks for an empty string */
    private boolean isEmpty(final String string) {
        if (null == string) {
            return true;
        }
        final int len = string.length();
        boolean isWhitespace = true;
        for (int i = 0; isWhitespace && i < len; i++) {
            isWhitespace = Character.isWhitespace(string.charAt(i));
        }
        return isWhitespace;
    }

}
