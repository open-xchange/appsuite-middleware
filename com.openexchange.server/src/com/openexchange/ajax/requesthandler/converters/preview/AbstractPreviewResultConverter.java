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

package com.openexchange.ajax.requesthandler.converters.preview;

import static com.openexchange.java.Charsets.toAsciiBytes;
import static com.openexchange.java.Charsets.toAsciiString;
import static com.openexchange.java.Strings.isEmpty;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import com.openexchange.ajax.container.FileHolder;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.ajax.requesthandler.cache.CachedResource;
import com.openexchange.ajax.requesthandler.cache.ResourceCache;
import com.openexchange.ajax.requesthandler.cache.ResourceCaches;
import com.openexchange.ajax.requesthandler.responseRenderers.FileResponseRenderer;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataProperties;
import com.openexchange.conversion.SimpleData;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.ldap.User;
import com.openexchange.java.Charsets;
import com.openexchange.java.Reference;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.utils.DisplayMode;
import com.openexchange.preview.ContentTypeChecker;
import com.openexchange.preview.Delegating;
import com.openexchange.preview.PreviewDocument;
import com.openexchange.preview.PreviewExceptionCodes;
import com.openexchange.preview.PreviewOutput;
import com.openexchange.preview.PreviewService;
import com.openexchange.preview.RemoteInternalPreviewDocument;
import com.openexchange.preview.RemoteInternalPreviewService;
import com.openexchange.preview.cache.CachedPreviewDocument;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.Task;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link AbstractPreviewResultConverter}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractPreviewResultConverter implements ResultConverter {

    /** The logger constant */
    static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger(AbstractPreviewResultConverter.class);

    private static final Charset UTF8 = Charsets.UTF_8;
    private static final byte[] DELIM = new byte[] { '\r', '\n' };

    /**
     * The <code>"view"</code> parameter.
     */
    protected static final String PARAMETER_VIEW = "view";

    /**
     * The <code>"edit"</code> parameter.
     */
    protected static final String PARAMETER_EDIT = "edit";

    /**
     * Initializes a new {@link AbstractPreviewResultConverter}.
     */
    protected AbstractPreviewResultConverter() {
        super();
    }

    @Override
    public String getInputFormat() {
        return "file";
    }

    @Override
    public void convert(final AJAXRequestData requestData, final AJAXRequestResult result, final ServerSession session, final Converter converter) throws OXException {
        IFileHolder fileHolder = null;
        try {
            // Check cache first
            final ResourceCache resourceCache;
            {
                final ResourceCache tmp = ResourceCaches.getResourceCache();
                resourceCache = null == tmp ? null : (tmp.isEnabledFor(session.getContextId(), session.getUserId()) ? tmp : null);
            }
            final String eTag = requestData.getETag();
            final boolean isValidEtag = !Strings.isEmpty(eTag);
            final String previewLanguage = getUserLanguage(session);
            if (null != resourceCache && isValidEtag && AJAXRequestDataTools.parseBoolParameter("cache", requestData, true)) {
                final String cacheKey = ResourceCaches.generatePreviewCacheKey(eTag, requestData, previewLanguage);
                final CachedResource cachedPreview = resourceCache.get(cacheKey, 0, session.getContextId());
                if (null != cachedPreview) {
                    /*
                     * Get content according to output format
                     */
                    final byte[] bytes;
                    InputStream in = null;
                    try {
                        in = cachedPreview.getInputStream();
                        if (null == in) {
                            bytes = cachedPreview.getBytes();
                        } else {
                            bytes = Streams.stream2bytes(in);
                        }
                    } finally {
                        Streams.close(in);
                    }
                    /*
                     * Convert meta data to a map
                     */
                    final Map<String, String> map = new HashMap<String, String>(4);
                    map.put("resourcename", cachedPreview.getFileName());
                    map.put("content-type", cachedPreview.getFileType());
                    // Decode contents
                    final List<String> contents;
                    {
                        final int[] computedFailure = computeFailure(DELIM);
                        int prev = 0;
                        int pos;
                        if ((pos = indexOf(bytes, DELIM, prev, computedFailure)) >= 0) {
                            // Multiple contents
                            contents = new LinkedList<String>();
                            final ByteArrayOutputStream baos = new ByteArrayOutputStream(8192 << 1);
                            do {
                                baos.reset();
                                prev = pos + DELIM.length;
                                pos = indexOf(bytes, DELIM, prev, computedFailure);
                                if (pos >= 0) {
                                    baos.write(bytes, prev, pos);
                                } else {
                                    baos.write(bytes, prev, bytes.length);
                                }
                                contents.add(new String(Base64.decodeBase64(toAsciiBytes(toAsciiString(baos.toByteArray()))), UTF8));
                            } while (pos >= 0);
                        } else {
                            // Single content
                            contents = Collections.singletonList(new String(Base64.decodeBase64(toAsciiBytes(toAsciiString(bytes))), UTF8));
                        }
                    }
                    // Set preview document
                    result.setResultObject(new CachedPreviewDocument(contents, map), getOutputFormat());
                    return;
                }
            }

            // No cached preview available
            {
                final Object resultObject = result.getResultObject();
                if (!(resultObject instanceof IFileHolder)) {
                    throw AjaxExceptionCodes.UNEXPECTED_RESULT.create(IFileHolder.class.getSimpleName(), null == resultObject ? "null" : resultObject.getClass().getSimpleName());
                }
                fileHolder = (IFileHolder) resultObject;
            }

            // Check file holder's content
            if (0 == fileHolder.getLength()) {
                throw AjaxExceptionCodes.UNEXPECTED_ERROR.create("File holder has not content, hence no preview can be generated.");
            }

            // Obtain preview document
            final PreviewDocument previewDocument;
            {
                InputStream stream = fileHolder.getStream();
                final Reference<InputStream> ref = new Reference<InputStream>();
                if (streamIsEof(stream, null)) {
                    Streams.close(stream, fileHolder);
                    throw AjaxExceptionCodes.UNEXPECTED_ERROR.create("File holder has not content, hence no preview can be generated.");
                }
                stream = ref.getValue();

                final PreviewService previewService = ServerServiceRegistry.getInstance().getService(PreviewService.class);

                final DataProperties dataProperties = new DataProperties(4);
                dataProperties.put(DataProperties.PROPERTY_CONTENT_TYPE, getContentType(fileHolder, previewService instanceof ContentTypeChecker ? (ContentTypeChecker) previewService : null));
                dataProperties.put(DataProperties.PROPERTY_DISPOSITION, fileHolder.getDisposition());
                dataProperties.put(DataProperties.PROPERTY_NAME, fileHolder.getName());
                dataProperties.put(DataProperties.PROPERTY_SIZE, Long.toString(fileHolder.getLength()));

                int pages = -1;
                if (requestData.containsParameter("pages")) {
                    pages = requestData.getIntParameter("pages");
                }
                previewDocument = previewService.getPreviewFor(new SimpleData<InputStream>(stream, dataProperties), getOutput(), session, pages);
                // Put to cache
                if (null != resourceCache && isValidEtag && AJAXRequestDataTools.parseBoolParameter("cache", requestData, true)) {
                    final List<String> content = previewDocument.getContent();
                    if (null != content) {
                        final int size = content.size();
                        if (size > 0) {
                            final String cacheKey = ResourceCaches.generatePreviewCacheKey(eTag, requestData, previewLanguage);
                            final byte[] bytes;
                            if (1 == content.size()) {
                                bytes = toAsciiBytes(toAsciiString(Base64.encodeBase64(content.get(0).getBytes(UTF8))));
                            } else {
                                final ByteArrayOutputStream baos = Streams.newByteArrayOutputStream(8192 << 1);
                                baos.write(toAsciiBytes(toAsciiString(Base64.encodeBase64(content.get(0).getBytes(UTF8)))));
                                final byte[] delim = DELIM;
                                for (int i = 1; i < size; i++) {
                                    baos.write(delim);
                                    baos.write(toAsciiBytes(toAsciiString(Base64.encodeBase64(content.get(i).getBytes(UTF8)))));
                                }
                                bytes = baos.toByteArray();
                            }
                            final String fileName = fileHolder.getName();
                            final String fileType = fileHolder.getContentType();
                            // Specify task
                            final Task<Void> task = new AbstractTask<Void>() {
                                @Override
                                public Void call() {
                                    try {
                                        final CachedResource preview = new CachedResource(bytes, fileName, fileType, bytes.length);
                                        resourceCache.save(cacheKey, preview, 0, session.getContextId());
                                    } catch (OXException e) {
                                        LOGGER.warn("Could not cache preview.", e);
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
                                    throw (ex instanceof OXException ? (OXException) ex : AjaxExceptionCodes.UNEXPECTED_ERROR.create(
                                        ex,
                                        ex.getMessage()));
                                }
                            } else {
                                threadPool.submit(task);
                            }
                        }
                    }
                }
            }
            if (requestData.getIntParameter("save") == 1) {
                // TODO:
//                /*-
//                 * Preview document should be saved.
//                 * We set the request format to file and return a FileHolder
//                 * containing the preview document.
//                 */
//                requestData.setFormat("file");
//                final byte[] documentBytes = previewDocument.getContent().getBytes();
//                final InputStream is = new ByteArrayInputStream(documentBytes);
//                final String contentType = previewDocument.getMetaData().get("content-type");
//                final String fileName = previewDocument.getMetaData().get("resourcename");
//                final FileHolder responseFileHolder = new FileHolder(is, documentBytes.length, contentType, fileName);
//                result.setResultObject(responseFileHolder, "file");
            } else {
                result.setResultObject(previewDocument, getOutputFormat());
            }
        } catch (final IOException e) {
            throw AjaxExceptionCodes.IO_ERROR.create(e, e.getMessage());
        } catch (final RuntimeException e) {
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            Streams.close(fileHolder);
        }
    }

    private static final Set<String> BOOLS = new HashSet<String>(Arrays.asList("true", "yes", "y", "on", "1"));

    /**
     * Parses specified value to a <code>boolean</code>:<br>
     * <code>true</code> if given value is not <code>null</code> and equals ignore-case to one of the values "true", "yes", "y", "on", or "1".
     *
     * @param value The value to parse
     * @return The parsed <code>boolean</code> value
     */
    protected static boolean parseBool(final String value) {
        if (null == value) {
            return false;
        }
        return BOOLS.contains(value.trim().toLowerCase(Locale.US));
    }

    private static final Set<String> INVALIDS = MimeTypes.INVALIDS;

    /**
     * Gets the checked MIME type from given file.
     *
     * @param fileHolder The file
     * @param checker The optional checker
     * @return The checked MIME type
     */
    protected static String getContentType(final IFileHolder fileHolder, final ContentTypeChecker checker) {
        String contentType = fileHolder.getContentType();
        if (isEmpty(contentType)) {
            // Determine Content-Type by file name
            return MimeType2ExtMap.getContentType(fileHolder.getName());
        }
        // Cut to base type & sanitize
        contentType = sanitizeContentType(getLowerCaseBaseType(contentType));
        contentType = MimeTypes.checkedMimeType(contentType, fileHolder.getName(), INVALIDS);
        if (isEmpty(contentType) || INVALIDS.contains(contentType) || (null != checker && !checker.isValid(contentType))) {
            // Determine Content-Type by file name
            contentType = MimeType2ExtMap.getContentType(fileHolder.getName());
        }
        return contentType == null ? "application/octet-stream" : contentType;
    }

    private static String sanitizeContentType(final String contentType) {
        if (null == contentType) {
            return null;
        }
        try {
            return new ContentType(contentType).getBaseType();
        } catch (final OXException e) {
            return contentType;
        }
    }

    private static String getLowerCaseBaseType(final String contentType) {
        if (null == contentType) {
            return null;
        }
        final int pos = contentType.indexOf(';');
        return Strings.toLowerCase(pos > 0 ? contentType.substring(0, pos) : contentType).trim();
    }

    private static final String VIEW_RAW = "raw";

    private static final String VIEW_TEXT = "text";

    private static final String VIEW_TEXT_NO_HTML_ATTACHMENT = "textNoHtmlAttach";

    private static final String VIEW_HTML = "html";

    private static final String VIEW_HTML_BLOCKED_IMAGES = "noimg";

    /**
     * Detects display mode dependent on passed arguments.
     *
     * @param modifyable Whether content is intended being modified by client
     * @param view The view parameter
     * @param usm The user settings
     * @return The display mode
     */
    protected static DisplayMode detectDisplayMode(final boolean modifyable, final String view, final UserSettingMail usm) {
        if (null == view) {
            return modifyable ? DisplayMode.MODIFYABLE : DisplayMode.DISPLAY;
        }
        final DisplayMode displayMode;
        if (VIEW_RAW.equals(view)) {
            displayMode = DisplayMode.RAW;
        } else if (VIEW_TEXT_NO_HTML_ATTACHMENT.equals(view)) {
            usm.setDisplayHtmlInlineContent(false);
            usm.setSuppressHTMLAlternativePart(true);
            displayMode = modifyable ? DisplayMode.MODIFYABLE : DisplayMode.DISPLAY;
        } else if (VIEW_TEXT.equals(view)) {
            usm.setDisplayHtmlInlineContent(false);
            displayMode = modifyable ? DisplayMode.MODIFYABLE : DisplayMode.DISPLAY;
        } else if (VIEW_HTML.equals(view)) {
            usm.setDisplayHtmlInlineContent(true);
            usm.setAllowHTMLImages(true);
            displayMode = modifyable ? DisplayMode.MODIFYABLE : DisplayMode.DISPLAY;
        } else if (VIEW_HTML_BLOCKED_IMAGES.equals(view)) {
            usm.setDisplayHtmlInlineContent(true);
            usm.setAllowHTMLImages(false);
            displayMode = modifyable ? DisplayMode.MODIFYABLE : DisplayMode.DISPLAY;
        } else {
            LOGGER.warn("Unknown value in parameter {}: {}. Using user's mail settings as fallback.", PARAMETER_VIEW, view);
            displayMode = modifyable ? DisplayMode.MODIFYABLE : DisplayMode.DISPLAY;
        }
        return displayMode;
    }

    /**
     * Gets the desired output format.
     *
     * @return The output format
     */
    public abstract PreviewOutput getOutput();

    /**
     * Finds the first occurrence of the pattern in the text.
     */
    private int indexOf(final byte[] data, final byte[] pattern, final int[] computeFailure) {
        return indexOf(data, pattern, 0, computeFailure);
    }

    /**
     * Finds the first occurrence of the pattern in the text.
     */
    private int indexOf(byte[] data, final byte[] pattern, int fromIndex, int[] computedFailure) {
        final int[] failure = null == computedFailure ? computeFailure(pattern) : computedFailure;
        int j = 0;
        final int dLen = data.length;
        if (dLen == 0) {
            return -1;
        }
        final int pLen = pattern.length;
        for (int i = fromIndex; i < dLen; i++) {
            while (j > 0 && pattern[j] != data[i]) {
                j = failure[j - 1];
            }
            if (pattern[j] == data[i]) {
                j++;
            }
            if (j == pLen) {
                return i - pLen + 1;
            }
        }
        return -1;
    }

    /**
     * Computes the failure function using a boot-strapping process, where the pattern is matched against itself.
     */
    private int[] computeFailure(byte[] pattern) {
        final int length = pattern.length;
        final int[] failure = new int[length];
        int j = 0;
        for (int i = 1; i < length; i++) {
            while (j > 0 && pattern[j] != pattern[i]) {
                j = failure[j - 1];
            }
            if (pattern[j] == pattern[i]) {
                j++;
            }
            failure[i] = j;
        }

        return failure;
    }

    /**
     * Checks if passed stream signals EOF.
     *
     * @param in The stream to check
     * @param ref The stream reference
     * @return <code>true</code> if passed stream signals EOF; otherwise <code>false</code>
     * @throws IOException If an I/O error occurs
     */
    public static boolean streamIsEof(InputStream in, Reference<InputStream> ref) throws IOException {
        if (null == in) {
            return true;
        }
        final PushbackInputStream pin = Streams.pushbackInputStreamFor(in);
        final int read = pin.read();
        if (read < 0) {
            return true;
        }
        pin.unread(read);
        ref.setValue(pin);
        return false;
    }

    /**
     * Get the user language based on the current {@link Session}.
     *
     * @param session The session used for the language lookup.
     * @return <code>null</code> or the preferred language of the user in a "en-gb" like notation.
     */
    public static String getUserLanguage(ServerSession session) {
        User sessionUser = session.getUser();
        return null == sessionUser ? null : sessionUser.getPreferredLanguage();
    }

    /**
     * Checks if a {@link ResourceCache} is enabled fo the given context.
     *
     * @param contextId The context identifier
     * @return true if a {@link ResourceCache} is enabled for the context
     * @throws OXException if check fails
     */
    public static boolean isResourceCacheEnabled(int contextId) throws OXException {
        return isResourceCacheEnabled(contextId, -1);
    }

    /**
     * Checks if a {@link ResourceCache} is enabled fo the given context and user.
     *
     * @param contextId The context identifier
     * @param userId The user identifier
     * @return true if a {@link ResourceCache} is enabled for the context, user
     * @throws OXException if check fails
     */
    public static boolean isResourceCacheEnabled(int contextId, int userId) throws OXException {
        boolean isEnabled = false;
        final ResourceCache cache = ResourceCaches.getResourceCache();
        if (cache != null) {
            isEnabled = cache.isEnabledFor(contextId, userId);
        }
        return isEnabled;
    }

    /**
     * Get the {@link ResourceCache}.
     *
     * @return The preview cache or null if cache is either absent or not enabled for context, user
     */
    public static ResourceCache getResourceCache(int contextId, int userId) {
        try {
            return isResourceCacheEnabled(contextId, userId) ? ResourceCaches.getResourceCache() : null;
        } catch (OXException e) {
            LOGGER.warn("Failed to check if ResourceCache is enabled for context {} and user {}", contextId, userId, e);
        }
        return null;
    }

    /**
     * Try to get a context global cached resource.
     *
     * @param session The current session
     * @param cacheKey The cacheKey
     * @param resourceCache The resourceCache to use
     * @return Null or the found {@link CachedResource}
     */
    public static CachedResource getCachedResourceForContext(ServerSession session, String cacheKey, ResourceCache resourceCache) {
        CachedResource cachedResource= null;
        final int contextId = session.getContextId();
        if(!Strings.isEmpty(cacheKey)) {
            if(resourceCache != null) {
                try {
                    cachedResource = resourceCache.get(cacheKey, 0, contextId);
                } catch (OXException e) {
                    LOGGER.debug("Error while trying to look up CachedResource with key {} in context {}", e);
                }
            }
        }
        return cachedResource;
    }

    /**
     * (Optionally) Gets the {@link RemoteInternalPreviewService} representation for given arguments.
     *
     * @param previewService The preview service
     * @param fileHolder The associated file holder
     * @param output The desired preview output
     * @return The {@code RemoteInternalPreviewService} representation or <code>null</code>
     */
    public static RemoteInternalPreviewService getRemoteInternalPreviewServiceFrom(PreviewService previewService, IFileHolder fileHolder, PreviewOutput output) {
        return getRemoteInternalPreviewServiceFrom(previewService, fileHolder.getName(), output);
    }

    /**
     * (Optionally) Gets the {@link RemoteInternalPreviewService} representation for given arguments.
     *
     * @param previewService The preview service
     * @param fileName The name of the file
     * @param output The desired preview output
     * @return The {@code RemoteInternalPreviewService} representation or <code>null</code>
     */
    public static RemoteInternalPreviewService getRemoteInternalPreviewServiceFrom(PreviewService previewService, String fileName, PreviewOutput output) {
        if (previewService instanceof RemoteInternalPreviewService) {
            return (RemoteInternalPreviewService) previewService;
        }

        return getRemoteInternalPreviewServiceWithMime0(previewService, new FileNameMimeTypeProvider(fileName), output);
    }

    /**
     * (Optionally) Gets the {@link RemoteInternalPreviewService} representation for given arguments.
     *
     * @param previewService The preview service
     * @param mimeType The MIME type of the file
     * @param output The desired preview output
     * @return The {@code RemoteInternalPreviewService} representation or <code>null</code>
     */
    public static RemoteInternalPreviewService getRemoteInternalPreviewServiceWithMime(PreviewService previewService, String mimeType, PreviewOutput output) {
        if (previewService instanceof RemoteInternalPreviewService) {
            return (RemoteInternalPreviewService) previewService;
        }

        return getRemoteInternalPreviewServiceWithMime0(previewService, new DirectMimeTypeProvider(mimeType), output);
    }

    // ------------------------------------------------------------------------------------------------------------------------------------

    private static interface MimeTypeProvider {

        String getMimeType();
    }

    private static final class DirectMimeTypeProvider implements MimeTypeProvider {

        private final String mimeType;

        DirectMimeTypeProvider(String mimeType) {
            super();
            this.mimeType = mimeType;
        }

        @Override
        public String getMimeType() {
            return mimeType;
        }
    }

    private static final class FileNameMimeTypeProvider implements MimeTypeProvider {

        private final String fileName;

        FileNameMimeTypeProvider(String fileName) {
            super();
            this.fileName = fileName;
        }

        @Override
        public String getMimeType() {
            // Try to determine MIME type by file name
            return MimeType2ExtMap.getContentType(fileName, null);
        }
    }

    private static RemoteInternalPreviewService getRemoteInternalPreviewServiceWithMime0(PreviewService previewService, MimeTypeProvider mimeTypeProvider, PreviewOutput output) {
        // PreviewService object is no direct RemoteInternalPreviewService instance. Check if it is an instance of Delegating
        if (!(previewService instanceof Delegating)) {
            return null;
        }

        String mimeType = mimeTypeProvider.getMimeType();
        if (null == mimeType) {
            // No MIME type available
            return null;
        }

        // Determine candidate
        try {
            PreviewService bestFit = ((Delegating) previewService).getBestFitOrDelegate(mimeType, output);
            if (bestFit instanceof RemoteInternalPreviewService) {
                return (RemoteInternalPreviewService) bestFit;
            }
        } catch (OXException e) {
            LOGGER.debug("Error while trying to look up 'best fit' from RemoteInternalPeviewService in context {}", e);
        }

        // No suitable candidate found...
        return null;
    }

    // ------------------------------------------------------------------------------------------------------------------------------------

    /**
     * Try to get a cached resource from preview service.
     *
     * @param session The current session
     * @param cacheKey The cacheKey
     * @param resourceCache The resourceCache to use
     * @return Null or the found {@link CachedResource}
     */
    public static CachedResource getCachedResourceFromPreviewService(ServerSession session, AJAXRequestResult result, AJAXRequestData requestData, PreviewService previewService, PreviewOutput output) {
        try {
            // Check for IFileHolder instance
            IFileHolder fileHolder = getFileHolderFromResult(result, true);

            // Check for RemoteInternalPreviewService instance
            RemoteInternalPreviewService candidate = getRemoteInternalPreviewServiceFrom(previewService, fileHolder, output);
            if (null == candidate) {
                return null;
            }

            // Prepare properties for preview generation
            DataProperties dataProperties = new DataProperties(12);
            String srcMimeType = getContentType(fileHolder, previewService instanceof ContentTypeChecker ? (ContentTypeChecker) previewService : null);

            dataProperties.put(DataProperties.PROPERTY_CONTENT_TYPE, srcMimeType);
            dataProperties.put(DataProperties.PROPERTY_DISPOSITION, fileHolder.getDisposition());
            dataProperties.put(DataProperties.PROPERTY_NAME, fileHolder.getName());
            dataProperties.put(DataProperties.PROPERTY_SIZE, Long.toString(fileHolder.getLength()));
            dataProperties.put("PreviewType", requestData.getModule().equals("files") ? "DetailView" : "Thumbnail");
            dataProperties.put("PreviewWidth", requestData.getParameter("width"));
            dataProperties.put("PreviewHeight", requestData.getParameter("height"));
            dataProperties.put("PreviewDelivery", requestData.getParameter("delivery"));
            dataProperties.put("PreviewScaleType", requestData.getParameter("scaleType"));
            dataProperties.put("PreviewLanguage", getUserLanguage(session));

            // Generate preview
            Data<InputStream> data = new SimpleData<InputStream>(fileHolder.getStream(), dataProperties);
            PreviewDocument previewDocument = candidate.getCachedPreviewFor(data, output, session, 1);
            if (null != previewDocument) {
                byte[] thumbnailBuffer = null;

                // we like to have the result as a byte buffer
                if (previewDocument instanceof RemoteInternalPreviewDocument) {
                    thumbnailBuffer = ((RemoteInternalPreviewDocument) previewDocument).getThumbnailBuffer();
                } else {
                    InputStream thumbnailStm = previewDocument.getThumbnail();
                    if (null != thumbnailStm) {
                        try {
                            thumbnailBuffer = IOUtils.toByteArray(thumbnailStm);
                        } catch (IOException e) {
                            throw PreviewExceptionCodes.ERROR.create(e, e.getMessage());
                        } finally {
                            Streams.close(thumbnailStm);
                        }
                    }
                }

                if (null != thumbnailBuffer) {
                    Map<String, String> metadata = previewDocument.getMetaData();
                    String fileName = metadata.get("resourcename");
                    String contentType = metadata.get("content-type");
                    return new CachedResource(thumbnailBuffer, fileName, contentType, thumbnailBuffer.length);
                }
            }
        } catch (OXException e) {
            LOGGER.debug("Error while trying to look up CachedResource from RemotePreviewService", e);
        }

        return null;
    }

    /**
     * Trigger the conversion of a resource from the preview service.
     * The trigger call is handled asynchronously, future conversion
     * calls to the preview service may then be handled immediately
     * from the cache.
     *
     * @param session The current session
     * @param result The AJAX result
     * @param requestData The AJAX request data
     * @param previewService The remote preview service to use
     * @param output The desired preview output
     */
    public static void triggerPreviewService(ServerSession session, AJAXRequestResult result, AJAXRequestData requestData, PreviewService previewService, PreviewOutput output) {
        try {
            // Check for IFileHolder instance
            IFileHolder fileHolder = getFileHolderFromResult(result, true);

            // Check for RemoteInternalPreviewService instance
            RemoteInternalPreviewService candidate = getRemoteInternalPreviewServiceFrom(previewService, fileHolder, output);
            if (null == candidate) {
                return;
            }

            triggerPreviewService(session, fileHolder, requestData, candidate, output);
        } catch (OXException e) {
            LOGGER.debug("Error while triggering RemotePreviewService", e);
        }
    }

    /**
     * Trigger the conversion of a resource from the preview service.
     * The trigger call is handled asynchronously, future conversion
     * calls to the preview service may then be handled immediately
     * from the cache.
     *
     * @param session The current session
     * @param fileHolder The file holder
     * @param requestData The AJAX request data
     * @param previewService The remote preview service to use
     * @param output The desired preview output
     */
    public static void triggerPreviewService(ServerSession session, IFileHolder fileHolder, AJAXRequestData requestData, RemoteInternalPreviewService previewService, PreviewOutput output) {
        try {
            // Prepare properties for preview generation
            String srcMimeType = getContentType(fileHolder, previewService instanceof ContentTypeChecker ? (ContentTypeChecker) previewService : null);

            DataProperties dataProperties = new DataProperties(12);
            dataProperties.put(DataProperties.PROPERTY_CONTENT_TYPE, srcMimeType);
            dataProperties.put(DataProperties.PROPERTY_DISPOSITION, fileHolder.getDisposition());
            dataProperties.put(DataProperties.PROPERTY_NAME, fileHolder.getName());
            dataProperties.put(DataProperties.PROPERTY_SIZE, Long.toString(fileHolder.getLength()));
            dataProperties.put("PreviewType", requestData.getModule().equals("files") ? "DetailView" : "Thumbnail");
            dataProperties.put("PreviewWidth", requestData.getParameter("width"));
            dataProperties.put("PreviewHeight", requestData.getParameter("height"));
            dataProperties.put("PreviewDelivery", requestData.getParameter("delivery"));
            dataProperties.put("PreviewScaleType", requestData.getParameter("scaleType"));
            dataProperties.put("PreviewLanguage", getUserLanguage(session));

            // Generate preview
            Data<InputStream> data = new SimpleData<InputStream>(fileHolder.getStream(), dataProperties);
            previewService.triggerGetPreviewFor(data, output, session, 1);
        } catch (OXException e) {
            LOGGER.debug("Error while triggering RemotePreviewService", e);
        }
    }

    /**
     * Detects if specified preview service should be called via a current thread or worker thread strategy.
     *
     * @param previewService The preview service to check
     * @return The await time (worker thread) or <code>0</code> (current thread)
     */
    public static long getAwaitThreshold(PreviewService previewService) {
        return previewService instanceof RemoteInternalPreviewService ? ((RemoteInternalPreviewService) previewService).getTimeToWaitMillis() : 0L;
    }

    /**
     * Add the default thumbnail as result to the current response
     *
     * @param requestData The current {@link AJAXRequestData} needed to set format and prevent further transformation.
     * @param result The current {@link AJAXRequestResult}
     */
    public static void setDefaulThumbnail(final AJAXRequestData requestData, final AJAXRequestResult result) {
        setJpegThumbnail(requestData, result, PreviewConst.DEFAULT_THUMBNAIL);
    }

    /**
     * Add the 1x1 white jpeg thumbnail as result to the current response. This indicates an accepted thumbnail request that can't deliver
     * an immediate response from cache but initiated the generation of the needed thumbnail.
     * <p>
     * TODO: Remove when UI can properly handle <code>202 - Retry-After</code> responses
     *
     * @param requestData The current {@link AJAXRequestData} needed to set format and prevent further transformation.
     * @param result The current {@link AJAXRequestResult}
     */
    public static void setMissingThumbnail(final AJAXRequestData requestData, final AJAXRequestResult result) {
        setJpegThumbnail(requestData, result, PreviewConst.MISSING_THUMBNAIL);
    }

    private static void setJpegThumbnail(final AJAXRequestData requestData, final AJAXRequestResult result, byte[] thumbnailBytes) {
        requestData.setFormat("file");
        InputStream thumbnail = Streams.newByteArrayInputStream(thumbnailBytes);
        requestData.putParameter("transformationNeeded", "false");
        final FileHolder responseFileHolder = new FileHolder(thumbnail, thumbnailBytes.length, "image/jpeg", "thumbs.jpg");
        result.setResultObject(responseFileHolder, "file");
    }

    /**
     * Get the {@link IFileHolder} from the given {@link AJAXRequestResult}.
     *
     * @param result The AJAX result
     * @return The {@link IFileHolder} from the given {@link AJAXRequestResult}.
     * @throws OXException if the result object isn't compatible
     */
    public static IFileHolder getFileHolderFromResult(AJAXRequestResult result) throws OXException {
        return getFileHolderFromResult(result, false);
    }

    /**
     * Gets the expected (and non-empty) {@link IFileHolder} instance from given AJAX result.
     *
     * @param result The AJAX result to get the <code>IFileHolder</code> instance from
     * @param checkLength Whether to check file holder's content length
     * @return The <code>IFileHolder</code> instance
     * @throws OXException If a <code>IFileHolder</code> instance cannot be returned from AJAX result
     */
    public static IFileHolder getFileHolderFromResult(AJAXRequestResult result, boolean checkLength) throws OXException {
        // Ensure result object is a IFileHolder instance
        Object resultObject = result.getResultObject();
        if (!(resultObject instanceof IFileHolder)) {
            throw AjaxExceptionCodes.UNEXPECTED_RESULT.create(IFileHolder.class.getSimpleName(), null == resultObject ? "null" : resultObject.getClass().getSimpleName());
        }

        // ... that is not empty
        IFileHolder fileHolder = (IFileHolder) resultObject;
        if (checkLength && (0 == fileHolder.getLength())) {
            // Get rid off resource warning
            Streams.close(fileHolder);
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create("File holder has not content, hence no preview can be generated.");
        }
        return fileHolder;
    }

    /**
     * Check if the client actively wants to prevent caching.
     *
     * @param requestData The requestData
     * @return True if the client didn't actively specify caching=false
     */
    public static boolean useCache(AJAXRequestData requestData) {
        return AJAXRequestDataTools.parseBoolParameter("cache", requestData, true);
    }

    /**
     * Check if the ETag is valid iow. non empty
     *
     * @param eTag The ETag to check
     * @return True if !Strings.isEmpty(eTag)
     */
    public static boolean isValidETag(String eTag) {
        return !Strings.isEmpty(eTag);
    }

    /**
     * Detect if the {@link PreviewDocument} needs further transformations or not and set <code>transformationNeeded</code> parameter on the
     * given {@link AJAXRequestData} that will be evaluated by the {@link FileResponseRenderer} on the way back to the client.
     *
     * @param requestData The {@link AJAXRequestData} that will be evaluated by the {@link FileResponseRenderer} on the way back to the
     *            client.
     * @param previewDocument The current {@link PreviewDocument}
     */
    public static void preventTransformations(AJAXRequestData requestData, PreviewDocument previewDocument) {
        if ("com.openexchange.documentpreview.OfficePreviewDocument".equals(previewDocument.getClass().getName())) {
            preventTransformations(requestData);
        }
    }

    /**
     * Set <code>transformationNeeded</code> parameter to <code>false</code> on the given {@link AJAXRequestData}.
     *
     * @param requestData The {@link AJAXRequestData} that will be evaluated by the {@link FileResponseRenderer} on the way back to the
     *            client.
     */
    public static void preventTransformations(AJAXRequestData requestData) {
        requestData.putParameter("transformationNeeded", "false");
    }

}
