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

import static com.openexchange.java.Charsets.toAsciiBytes;
import static com.openexchange.java.Charsets.toAsciiString;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import com.openexchange.ajax.container.IFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.Converter;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.conversion.DataProperties;
import com.openexchange.conversion.SimpleData;
import com.openexchange.exception.OXException;
import com.openexchange.java.Charsets;
import com.openexchange.java.Streams;
import com.openexchange.java.StringAllocator;
import com.openexchange.log.LogFactory;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.utils.DisplayMode;
import com.openexchange.preview.PreviewDocument;
import com.openexchange.preview.PreviewOutput;
import com.openexchange.preview.PreviewService;
import com.openexchange.preview.cache.CachedPreview;
import com.openexchange.preview.cache.CachedPreviewDocument;
import com.openexchange.preview.cache.PreviewCache;
import com.openexchange.server.services.ServerServiceRegistry;
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

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(AbstractPreviewResultConverter.class));

    private static final AtomicReference<PreviewCache> CACHE_REF = new AtomicReference<PreviewCache>();

    /**
     * Sets the preview cache reference.
     *
     * @param ref The reference
     */
    public static void setPreviewCache(final PreviewCache ref) {
        CACHE_REF.set(ref);
    }

    /**
     * Gets the preview cache reference.
     *
     * @return The preview cache or <code>null</code> if absent
     */
    protected static PreviewCache getPreviewCache() {
        return CACHE_REF.get();
    }

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

    /**
     * Generates the key for preview cache.
     *
     * @param eTag The ETag identifier
     * @param requestData The request data
     * @param optParameters Optional parameters to consider
     * @return The appropriate cache key
     */
    protected String generatePreviewCacheKey(final String eTag, final AJAXRequestData requestData) {
        final StringAllocator sb = new StringAllocator(512);
        sb.append(requestData.getModule());
        sb.append('-').append(requestData.getAction());
        sb.append('-').append(requestData.getSession().getContextId());
        List<String> parameters = new ArrayList<String>(requestData.getParameters().keySet());
        Collections.sort(parameters);

        for (final String name : parameters) {
            if (!name.equalsIgnoreCase("session") && !name.equalsIgnoreCase("action")) {
                sb.append(name).append('=');
                final String parameter = requestData.getParameter(name);
                if (!isEmpty(parameter)) {
                    sb.append('-').append(parameter);
                }
            }
        }
        try {
            return eTag + asHex(MessageDigest.getInstance("MD5").digest(sb.toString().getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            // Shouldn't happen
            LOG.error(e.getMessage(),e);
        } catch (NoSuchAlgorithmException e) {
            // Shouldn't happen
            LOG.error(e.getMessage(),e);
        }
        return sb.toString();
    }

    private static final char[] HEX_CHARS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    /**
     * Turns array of bytes into string representing each byte as unsigned hex number.
     *
     * @param hash Array of bytes to convert to hex-string
     * @return Generated hex string
     */
    public static String asHex(final byte[] hash) {
        final int length = hash.length;
        final char[] buf = new char[length * 2];
        for (int i = 0, x = 0; i < length; i++) {
            buf[x++] = HEX_CHARS[(hash[i] >>> 4) & 0xf];
            buf[x++] = HEX_CHARS[hash[i] & 0xf];
        }
        return new String(buf);
    }

    @Override
    public void convert(final AJAXRequestData requestData, final AJAXRequestResult result, final ServerSession session, final Converter converter) throws OXException {
        IFileHolder fileHolder = null;
        try {
            // Check cache first
            final PreviewCache previewCache = getPreviewCache();
            final String eTag = requestData.getETag();
            final boolean isValidEtag = !isEmpty(eTag);
            if (null != previewCache && isValidEtag) {
                final String cacheKey = generatePreviewCacheKey(eTag, requestData);
                final CachedPreview cachedPreview = previewCache.get(cacheKey, 0, session.getContextId());
                if (null != cachedPreview) {
                    /*
                     * Get content according to output format
                     */
                    final byte[] bytes;
                    {
                        final InputStream in = cachedPreview.getInputStream();
                        if (null == in) {
                            bytes = cachedPreview.getBytes();
                        } else {
                            bytes = Streams.stream2bytes(in);
                        }
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
            /*
             * Obtain preview document
             */
            final PreviewDocument previewDocument;
            {
                final PreviewService previewService = ServerServiceRegistry.getInstance().getService(PreviewService.class);

                final DataProperties dataProperties = new DataProperties(4);
                dataProperties.put(DataProperties.PROPERTY_CONTENT_TYPE, fileHolder.getContentType());
                dataProperties.put(DataProperties.PROPERTY_DISPOSITION, fileHolder.getDisposition());
                dataProperties.put(DataProperties.PROPERTY_NAME, fileHolder.getName());
                dataProperties.put(DataProperties.PROPERTY_SIZE, Long.toString(fileHolder.getLength()));

                int pages = -1;
                if (requestData.containsParameter("pages")) {
                    pages = requestData.getIntParameter("pages");
                }
                previewDocument = previewService.getPreviewFor(new SimpleData<InputStream>(fileHolder.getStream(), dataProperties), getOutput(), session, pages);
                // Put to cache
                if (null != previewCache && isValidEtag) {
                    final List<String> content = previewDocument.getContent();
                    if (null != content) {
                        final int size = content.size();
                        if (size > 0) {
                            final String cacheKey = generatePreviewCacheKey(eTag, requestData);
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
                                public Void call() throws OXException {
                                    final CachedPreview preview = new CachedPreview(bytes, fileName, fileType, bytes.length);
                                    previewCache.save(cacheKey, preview, 0, session.getContextId());
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
            LOG.warn(new com.openexchange.java.StringAllocator(64).append("Unknown value in parameter ").append(PARAMETER_VIEW).append(": ").append(view).append(
                ". Using user's mail settings as fallback."));
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

    /** Check for an empty string */
    private static boolean isEmpty(final String string) {
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

    /**
     * Finds the first occurrence of the pattern in the text.
     */
    private int indexOf(final byte[] data, final byte[] pattern, final int[] computeFailure) {
        return indexOf(data, pattern, 0, computeFailure);
    }

    /**
     * Finds the first occurrence of the pattern in the text.
     */
    private int indexOf(final byte[] data, final byte[] pattern, final int fromIndex, final int[] computedFailure) {
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
    private int[] computeFailure(final byte[] pattern) {
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

}
