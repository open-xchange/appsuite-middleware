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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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
import com.openexchange.ajax.container.ByteArrayFileHolder;
import com.openexchange.ajax.container.FileHolder;
import com.openexchange.ajax.container.IFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.cache.CachedResource;
import com.openexchange.ajax.requesthandler.cache.ResourceCache;
import com.openexchange.ajax.requesthandler.cache.ResourceCaches;
import com.openexchange.conversion.DataProperties;
import com.openexchange.conversion.SimpleData;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.preview.ContentTypeChecker;
import com.openexchange.preview.PreviewDocument;
import com.openexchange.preview.PreviewExceptionCodes;
import com.openexchange.preview.PreviewOutput;
import com.openexchange.preview.PreviewService;
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

    /** The logger constant */
    static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(PreviewImageResultConverter.class);

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
            final ResourceCache resourceCache;
            {
                final ResourceCache tmp = ResourceCaches.getResourceCache();
                resourceCache = null == tmp ? null : (tmp.isEnabledFor(session.getContextId(), session.getUserId()) ? tmp : null);
            }

            // Get eTag from result that provides the IFileHolder
            final String eTag = result.getHeader("ETag");
            final boolean isValidEtag = !Strings.isEmpty(eTag);
            final String previewLanguage = getUserLanguage(session);
            if (null != resourceCache && isValidEtag && AJAXRequestDataTools.parseBoolParameter("cache", requestData, true)) {
                final String cacheKey = ResourceCaches.generatePreviewCacheKey(eTag, requestData, previewLanguage);
                final CachedResource cachedPreview = resourceCache.get(cacheKey, 0, session.getContextId());
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

            // No cached preview available -- get the preview document from appropriate 'PreviewService'
            final PreviewDocument previewDocument;
            {
                InputStream stream = null;
                IFileHolder fileHolder = null;
                try {
                    final Object resultObject = result.getResultObject();
                    if (!(resultObject instanceof IFileHolder)) {
                        throw AjaxExceptionCodes.UNEXPECTED_RESULT.create(IFileHolder.class.getSimpleName(), null == resultObject ? "null" : resultObject.getClass().getSimpleName());
                    }
                    fileHolder = (IFileHolder) resultObject;

                    // Check file holder's content
                    stream = fileHolder.getStream();
                    {
                        if (0 == fileHolder.getLength()) {
                            Streams.close(stream, fileHolder);
                            stream = null;
                            setDefaulThumbnail(requestData, result);
                            return;
                        }
                        final Ref<InputStream> ref = new Ref<InputStream>();
                        if (streamIsEof(stream, ref)) {
                            Streams.close(stream, fileHolder);
                            stream = null;
                            setDefaulThumbnail(requestData, result);
                            return;
                        }
                        stream = ref.getValue();
                    }

                    // Obtain preview
                    final PreviewService previewService = ServerServiceRegistry.getInstance().getService(PreviewService.class);
                    final DataProperties dataProperties = new DataProperties(12);
                    dataProperties.put(DataProperties.PROPERTY_CONTENT_TYPE, getContentType(fileHolder, previewService instanceof ContentTypeChecker ? (ContentTypeChecker) previewService : null));
                    dataProperties.put(DataProperties.PROPERTY_DISPOSITION, fileHolder.getDisposition());
                    dataProperties.put(DataProperties.PROPERTY_NAME, fileHolder.getName());
                    dataProperties.put(DataProperties.PROPERTY_SIZE, Long.toString(fileHolder.getLength()));
                    dataProperties.put("PreviewType", requestData.getModule().equals("files") ? "DetailView" : "Thumbnail");
                    dataProperties.put("PreviewWidth", requestData.getParameter("width"));
                    dataProperties.put("PreviewHeight", requestData.getParameter("height"));
                    dataProperties.put("PreviewDelivery", requestData.getParameter("delivery"));
                    dataProperties.put("PreviewScaleType", requestData.getParameter("scaleType"));
                    dataProperties.put("PreviewLanguage", previewLanguage);
                    previewDocument = previewService.getPreviewFor(new SimpleData<InputStream>(stream, dataProperties), getOutput(), session, 1);
                } finally {
                    Streams.close(stream, fileHolder);
                }
            }

            // Check result
            if (null == previewDocument) {
                // No thumbnail available
                throw PreviewExceptionCodes.THUMBNAIL_NOT_AVAILABLE.create(new NullPointerException("previewDocument is null"), new Object[0]);
            }

            // Check thumbnail stream
            requestData.setFormat("file");
            InputStream thumbnail = previewDocument.getThumbnail();
            if (null == thumbnail) {
                // No thumbnail available
                throw PreviewExceptionCodes.THUMBNAIL_NOT_AVAILABLE.create();
            }

            // Prepare response
            if(previewDocument.getClass().getName().equals("com.openexchange.documentpreview.OfficePreviewDocument")) {
                requestData.putParameter("transformationNeeded", "false");
            }

            // (Asynchronously) Put to cache if ETag is available
            final String fileName = previewDocument.getMetaData().get("resourcename");
            int size = -1;
            if (null != resourceCache && isValidEtag && AJAXRequestDataTools.parseBoolParameter("cache", requestData, true)) {
                final byte[] bytes = Streams.stream2bytes(thumbnail);
                thumbnail = Streams.newByteArrayInputStream(bytes);
                size = bytes.length;
                // Specify task
                final String cacheKey = ResourceCaches.generatePreviewCacheKey(eTag, requestData);
                final AbstractTask<Void> task = new AbstractTask<Void>() {
                    @Override
                    public Void call() {
                        try {
                            final CachedResource preview = new CachedResource(bytes, fileName, "image/jpeg", bytes.length);
                            resourceCache.save(cacheKey, preview, 0, session.getContextId());
                        } catch (OXException e) {
                            LOG.warn("Could not cache preview.", e);
                        }

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

    private String getUserLanguage(final ServerSession session) {
        final User sessionUser = session.getUser();
        return null == sessionUser ? null : sessionUser.getPreferredLanguage();
    }

    private void setDefaulThumbnail(final AJAXRequestData requestData, final AJAXRequestResult result) {
        requestData.setFormat("file");
        final byte[] bytes = PreviewConst.DEFAULT_THUMBNAIL;
        InputStream thumbnail = Streams.newByteArrayInputStream(bytes);
        requestData.putParameter("transformationNeeded", "false");
        final FileHolder responseFileHolder = new FileHolder(thumbnail, bytes.length, "image/jpeg", "thumbs.jpg");
        result.setResultObject(responseFileHolder, "file");
    }

}
