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

package com.openexchange.ajax.requesthandler.responseRenderers;

import static com.openexchange.java.Streams.close;
import static com.openexchange.java.Strings.isEmpty;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.AJAXUtility;
import com.openexchange.ajax.container.ByteArrayFileHolder;
import com.openexchange.ajax.container.ByteArrayInputStreamClosure;
import com.openexchange.ajax.container.FileHolder;
import com.openexchange.ajax.container.IFileHolder;
import com.openexchange.ajax.container.IFileHolder.RandomAccess;
import com.openexchange.ajax.container.InputStreamReadable;
import com.openexchange.ajax.container.PushbackReadable;
import com.openexchange.ajax.container.Readable;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.helper.DownloadUtility;
import com.openexchange.ajax.helper.DownloadUtility.CheckedDownload;
import com.openexchange.ajax.helper.ImageUtils;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.ResponseRenderer;
import com.openexchange.ajax.requesthandler.cache.CachedResource;
import com.openexchange.ajax.requesthandler.cache.ResourceCache;
import com.openexchange.ajax.requesthandler.cache.ResourceCaches;
import com.openexchange.ajax.requesthandler.converters.preview.AbstractPreviewResultConverter;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.PropertyEvent;
import com.openexchange.config.PropertyListener;
import com.openexchange.exception.OXException;
import com.openexchange.java.HTMLDetector;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.threadpool.AbstractTask;
import com.openexchange.threadpool.ThreadPoolService;
import com.openexchange.tools.images.Constants;
import com.openexchange.tools.images.ImageTransformationService;
import com.openexchange.tools.images.ImageTransformationUtility;
import com.openexchange.tools.images.ImageTransformations;
import com.openexchange.tools.images.ScaleType;
import com.openexchange.tools.images.TransformedImage;
import com.openexchange.tools.images.transformations.ImageTransformationDeniedIOException;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link FileResponseRenderer}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class FileResponseRenderer implements ResponseRenderer {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FileResponseRenderer.class);

    /** The default in-memory threshold of 1MB. */
    private static final int DEFAULT_IN_MEMORY_THRESHOLD = 1024 * 1024; // 1MB

    private static final int INITIAL_CAPACITY = 8192;

    private static final int BUFLEN = 10240;

    private static final String PARAMETER_CONTENT_DISPOSITION = "content_disposition";

    private static final String PARAMETER_CONTENT_TYPE = "content_type";

    private static final String SAVE_AS_TYPE = "application/octet-stream";

    private volatile ImageTransformationService scaler;

    private static final String DELIVERY = AJAXServlet.PARAMETER_DELIVERY;

    private static final String DOWNLOAD = "download";

    private static final String VIEW = "view";

    private static final String MULTIPART_BOUNDARY = "MULTIPART_BYTERANGES";

    private static final Pattern PATTERN_BYTE_RANGES = Pattern.compile("^bytes=\\d*-\\d*(,\\d*-\\d*)*$");

    private final AtomicReference<File> tmpDirReference;

    /**
     * Initializes a new {@link FileResponseRenderer}.
     */
    public FileResponseRenderer() {
        super();
        final AtomicReference<File> tmpDirReference = new AtomicReference<File>();
        this.tmpDirReference = tmpDirReference;
        final ServerServiceRegistry registry = ServerServiceRegistry.getInstance();
        // Get configuration service
        final ConfigurationService cs = registry.getService(ConfigurationService.class);
        if (null == cs) {
            throw new IllegalStateException("Missing configuration service");
        }
        final String path = cs.getProperty("UPLOAD_DIRECTORY", new PropertyListener() {

            @Override
            public void onPropertyChange(final PropertyEvent event) {
                if (PropertyEvent.Type.CHANGED.equals(event.getType())) {
                    tmpDirReference.set(getTmpDirByPath(event.getValue()));
                }
            }
        });
        tmpDirReference.set(getTmpDirByPath(path));
    }

    @Override
    public int getRanking() {
        return 0;
    }

    /**
     * Sets the image scaler.
     *
     * @param scaler The image scaler
     */
    public void setScaler(ImageTransformationService scaler) {
        this.scaler = scaler;
    }

    @Override
    public boolean handles(AJAXRequestData request, AJAXRequestResult result) {
        return (result.getResultObject() instanceof IFileHolder);
    }

    @Override
    public void write(AJAXRequestData request, AJAXRequestResult result, HttpServletRequest req, HttpServletResponse resp) {
        IFileHolder file = (IFileHolder) result.getResultObject();
        // Check if file is actually supplied by the request URL.
        if (file == null || hasNoFileItem(file)) {
            try {
                // Do your thing if the file is not supplied to the request URL or if there is no file item associated with specified file
                // Throw an exception, or send 404, or show default/warning page, or just ignore it.
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            } catch (final IOException e) {
                LOG.error("Exception while trying to write HTTP response.", e);
            }
            return;
        }
        try {
            writeFileHolder(file, request, result, req, resp);
        } finally {
            postProcessingTasks(file);
        }
    }

    private void postProcessingTasks(IFileHolder file) {
        List<Runnable> tasks = file.getPostProcessingTasks();
        if (null != tasks && !tasks.isEmpty()) {
            for (Runnable task : tasks) {
                task.run();
            }
        }
    }

    /**
     * Writes specified file holder.
     *
     * @param fileHolder The file holder
     * @param requestData The AJAX request data
     * @param result The AJAX response
     * @param req The HTTP request
     * @param resp The HTTP response
     */
    public void writeFileHolder(IFileHolder fileHolder, AJAXRequestData requestData, AJAXRequestResult result, HttpServletRequest req, HttpServletResponse resp) {
        IFileHolder file = fileHolder;
        final String fileName = file.getName();
        final long length;
        final List<Closeable> closeables = new LinkedList<Closeable>();
        Readable documentData = null;
        try {
            final String fileContentType = file.getContentType();
            /*-
             *
            if (null != fileName && toLowerCase(fileContentType).startsWith("audio/")) {
                final String mappedContentType = MimeType2ExtMap.getContentType(fileName);
                if (!SAVE_AS_TYPE.equals(mappedContentType)) {
                    fileContentType = mappedContentType;
                }
            }
             *
             */
            // Check certain parameters
            String delivery = AJAXUtility.sanitizeParam(req.getParameter(DELIVERY));
            if (delivery == null) {
                delivery = file.getDelivery();
            }
            String contentType = AJAXUtility.encodeUrl(req.getParameter(PARAMETER_CONTENT_TYPE), true);
            boolean contentTypeByParameter = false;
            if (Strings.isEmpty(contentType)) {
                if (DOWNLOAD.equalsIgnoreCase(delivery)) {
                    contentType = SAVE_AS_TYPE;
                } else {
                    contentType = fileContentType;
                }
            } else {
                contentTypeByParameter = true;
            }
            contentType = unquote(contentType);
            // Delivery is set to "view", but Content-Type is indicated as application/octet-stream
            if (VIEW.equalsIgnoreCase(delivery) && (null != contentType && contentType.startsWith(SAVE_AS_TYPE))) {
                contentType = getContentTypeByFileName(fileName);
                if (null == contentType) {
                    // Not known
                    contentType = SAVE_AS_TYPE;
                }
            }
            String contentDisposition = AJAXUtility.encodeUrl(req.getParameter(PARAMETER_CONTENT_DISPOSITION));
            if (Strings.isEmpty(contentDisposition)) {
                if (VIEW.equalsIgnoreCase(delivery)) {
                    contentDisposition = "inline";
                } else if (DOWNLOAD.equalsIgnoreCase(delivery)) {
                    contentDisposition = "attachment";
                } else {
                    contentDisposition = file.getDisposition();
                }
            }
            contentDisposition = unquote(contentDisposition);
            // Write to Servlet's output stream
            file = transformIfImage(requestData, result, file, delivery);
            if (null == file) {
                // Quit with 404
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found.");
                return;
            }
            // Set binary input stream
            {
                RandomAccess ra = file.getRandomAccess();
                if (null == ra) {
                    InputStream stream = file.getStream();
                    if (null != stream) {
                        if ((stream instanceof ByteArrayInputStream) || (stream instanceof BufferedInputStream)) {
                            documentData = new InputStreamReadable(stream);
                        } else {
                            documentData = new InputStreamReadable(new BufferedInputStream(stream, 65536));
                        }
                    }
                } else {
                    documentData = ra;
                }
            }
            // Check for null
            if (null == documentData) {
                // Quit with 404
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found.");
                return;
            }
            final String userAgent = AJAXUtility.sanitizeParam(req.getHeader("user-agent"));
            if (DOWNLOAD.equalsIgnoreCase(delivery) || (SAVE_AS_TYPE.equals(contentType) && !VIEW.equalsIgnoreCase(delivery))) {
                // Write as a common file download: application/octet-stream
                final StringBuilder sb = new StringBuilder(32);
                sb.append(isEmpty(contentDisposition) ? "attachment" : checkedContentDisposition(contentDisposition.trim(), file));
                DownloadUtility.appendFilenameParameter(fileName, null, userAgent, sb);
                resp.setHeader("Content-Disposition", sb.toString());
                resp.setContentType(SAVE_AS_TYPE);
                length = file.getLength();
                setContentLengthHeader(length, resp);
            } else {
                // Determine what Content-Type is indicated by file name
                String contentTypeByFileName = getContentTypeByFileName(fileName);
                // Generate checked download
                final CheckedDownload checkedDownload;
                {
                    long fileLength = file.getLength();
                    String cts;
                    if (null == fileContentType || SAVE_AS_TYPE.equals(fileContentType)) {
                        if (null == contentTypeByFileName) {
                            // Let Tika detect the Content-Type
                            ThresholdFileHolder temp = new ThresholdFileHolder(DEFAULT_IN_MEMORY_THRESHOLD, INITIAL_CAPACITY);
                            closeables.add(temp);
                            temp.write(documentData);
                            // We know for sure
                            fileLength = temp.getLength();
                            documentData = temp.getClosingRandomAccess();
                            cts = detectMimeType(temp.getStream());
                            if ("text/plain".equals(cts)) {
                                cts = HTMLDetector.containsHTMLTags(temp.getStream(), true) ? "text/html" : cts;
                            }
                        } else {
                            cts = contentTypeByFileName;
                        }
                    } else {
                        if ((null != contentTypeByFileName) && !equalPrimaryTypes(fileContentType, contentTypeByFileName)) {
                            // Differing Content-Types sources
                            final ThresholdFileHolder temp = new ThresholdFileHolder(DEFAULT_IN_MEMORY_THRESHOLD, INITIAL_CAPACITY);
                            closeables.add(temp);
                            temp.write(documentData);
                            // We know for sure
                            fileLength = temp.getLength();
                            documentData = temp.getClosingRandomAccess();
                            cts = detectMimeType(temp.getStream());
                            if ("text/plain".equals(cts)) {
                                cts = HTMLDetector.containsHTMLTags(temp.getStream(), true) ? "text/html" : cts;
                            }
                        } else {
                            cts = fileContentType;
                        }
                    }
                    checkedDownload = DownloadUtility.checkInlineDownload(documentData, fileLength, fileName, cts, contentDisposition, userAgent, requestData.getSession());
                }
                /*
                 * Set stream
                 */
                documentData = checkedDownload.getInputStream();
                /*
                 * Set headers...
                 */
                if (delivery == null || !delivery.equalsIgnoreCase(VIEW)) {
                    if (isEmpty(contentDisposition)) {
                        resp.setHeader("Content-Disposition", checkedDownload.getContentDisposition());
                    } else {
                        if (contentDisposition.indexOf(';') >= 0) {
                            resp.setHeader("Content-Disposition", contentDisposition.trim());
                        } else {
                            final String disposition = checkedDownload.getContentDisposition();
                            final int pos = disposition.indexOf(';');
                            if (pos >= 0) {
                                resp.setHeader("Content-Disposition", contentDisposition.trim() + disposition.substring(pos));
                            } else {
                                resp.setHeader("Content-Disposition", contentDisposition.trim());
                            }
                        }
                    }
                } else if (checkedDownload.isAttachment()) {
                    // Force attachment download
                    resp.setHeader("Content-Disposition", checkedDownload.getContentDisposition());
                } else if (delivery.equalsIgnoreCase(VIEW) && null != fileName) {
                    final StringBuilder sb = new StringBuilder(32);
                    sb.append("inline");
                    DownloadUtility.appendFilenameParameter(fileName, null, userAgent, sb);
                    resp.setHeader("Content-Disposition", sb.toString());
                }
                /*
                 * Set Content-Length if possible
                 */
                length = checkedDownload.getSize();
                setContentLengthHeader(length, resp);
                /*-
                 * Determine preferred Content-Type
                 *
                 * 1. Ensure contentTypeByFileName has a valid value
                 *
                 * 2. Reset checkedContentType if
                 *   - it is set to "application/octet-stream"
                 *   - it's primary type is not equal to contentTypeByFileName's primary type; e.g. both start with "text/"
                 */
                String preferredContentType = checkedDownload.getContentType();
                if (null != contentTypeByFileName && !checkedDownload.isAttachment()) {
                    if (preferredContentType.startsWith(SAVE_AS_TYPE) || !equalPrimaryTypes(preferredContentType, contentTypeByFileName)) {
                        try {
                            final ContentType tmp = new ContentType(preferredContentType);
                            tmp.setBaseType(contentTypeByFileName);
                            preferredContentType = tmp.toString();
                        } catch (final Exception e) {
                            preferredContentType = contentTypeByFileName;
                        }
                    }
                }

                if (!contentTypeByParameter || contentType == null || SAVE_AS_TYPE.equals(contentType)) {
                    // Either no Content-Type parameter specified or set to "application/octet-stream"
                    resp.setContentType(preferredContentType);
                    contentType = preferredContentType;
                } else {
                    // A Content-Type parameter is specified...
                    if (SAVE_AS_TYPE.equals(preferredContentType) || equalTypes(preferredContentType, contentType)) {
                        // Set if sanitize-able
                        if (!trySetSanitizedContentType(contentType, preferredContentType, resp)) {
                            contentType = preferredContentType;
                        }
                    } else {
                        // Specified Content-Type does NOT match file's real MIME type. Ignore it due to security reasons (see bug #25343)
                        final StringBuilder sb = new StringBuilder(128);
                        sb.append("Denied parameter \"").append(PARAMETER_CONTENT_TYPE);
                        sb.append("\" due to security constraints (requested \"");
                        sb.append(contentType).append("\" , but is \"").append(preferredContentType).append("\").");
                        LOG.warn(sb.toString());
                        resp.setContentType(preferredContentType);
                        contentType = preferredContentType;
                    }
                }
            }
            /*
             * Browsers don't like the Pragma header the way we usually set this. Especially if files are sent to the browser. So removing
             * pragma header.
             */
            boolean keepCachingHeaders = false;
            if (requestData.isSet("keepCachingHeaders")) {
                keepCachingHeaders = requestData.getParameter("keepCachingHeaders", Boolean.class).booleanValue();
            }
            if (!keepCachingHeaders) {
                Tools.removeCachingHeader(resp);
            }
            if (delivery == null || !delivery.equalsIgnoreCase(DOWNLOAD)) {
                /*
                 * ETag present and caching?
                 */
                final String eTag = result.getHeader("ETag");
                if (null != eTag) {
                    final long expires = result.getExpires();
                    Tools.setETag(eTag, expires > 0 ? expires : -1L, resp);
                } else {
                    final long expires = result.getExpires();
                    if (expires > 0) {
                        Tools.setExpires(expires, resp);
                    }
                }
                /*
                 * Last-Modified
                 */
                final String sLastModified = result.getHeader("Last-Modified");
                if (null != sLastModified) {
                    resp.setHeader("Last-Modified", sLastModified);
                }
            }
            /*
             * Output binary content
             */
            try {
                final ServletOutputStream outputStream = resp.getOutputStream();
                final String sRange = req.getHeader("Range");
                if ((length > 0) && (null != sRange)) {
                    // Taken from http://balusc.blogspot.co.uk/2009/02/fileservlet-supporting-resume-and.html
                    // Range header should match format "bytes=n-n,n-n,n-n...". If not, then return 416.
                    if (!Tools.isByteRangeHeader(sRange)) {
                        resp.setHeader("Content-Range", "bytes */" + length); // Required in 416.
                        resp.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                        return;
                    }
                    // If-Range header should either match ETag or be greater then LastModified. If not,
                    // then return full file.
                    boolean full = false;
                    {
                        final String ifRange = requestData.getHeader("If-Range");
                        final String eTag = result.getHeader("ETag");
                        if (ifRange != null && !ifRange.equals(eTag)) {
                            try {
                                final long ifRangeTime = req.getDateHeader("If-Range"); // Throws IAE if invalid.
                                if (ifRangeTime != -1 && ifRangeTime < result.getExpires()) {
                                    full = true;
                                }
                            } catch (final IllegalArgumentException ignore) {
                                full = true;
                            }
                        }
                    }
                    // If any valid If-Range header, then process each part of byte range.
                    final List<Range> ranges;
                    if (full) {
                        ranges = Collections.emptyList();
                    } else {
                        final String[] parts = Strings.splitByComma(sRange.substring(6));
                        ranges = new ArrayList<Range>(parts.length);
                        for (final String part : parts) {
                            // Assuming a file with length of 100, the following examples returns bytes at:
                            // 50-80 (50 to 80), 40- (40 to length=100), -20 (length-20=80 to length=100).
                            final int dashPos = part.indexOf('-');
                            long start = sublong(part, 0, dashPos);
                            long end = sublong(part, dashPos + 1, part.length());

                            if (start == -1) {
                                start = length - end;
                                end = length - 1;
                            } else if (end == -1 || end > length - 1) {
                                end = length - 1;
                            }

                            // Check if Range is syntactically valid. If not, then return 416.
                            if (start > end) {
                                resp.setHeader("Content-Range", "bytes */" + length); // Required in 416.
                                resp.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                                return;
                            }

                            // Add range.
                            ranges.add(new Range(start, end, length));
                        }
                    }
                    if (full || ranges.isEmpty()) {
                        // Return full file.
                        final Range r = new Range(0L, length - 1, length);
                        resp.setHeader("Content-Range", new StringBuilder("bytes ").append(r.start).append('-').append(r.end).append('/').append(r.total).toString());

                        // Copy full range.
                        copy(documentData, outputStream, r.start, r.length);
                    } else if (ranges.size() == 1) {

                        // Return single part of file.
                        final Range r = ranges.get(0);
                        resp.setHeader("Content-Range", new StringBuilder("bytes ").append(r.start).append('-').append(r.end).append('/').append(r.total).toString());
                        resp.setHeader("Content-Length", Long.toString(r.length));
                        resp.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT); // 206.

                        // Copy single part range.
                        copy(documentData, outputStream, r.start, r.length);
                    } else {
                        // Return multiple parts of file.
                        final String boundary = MULTIPART_BOUNDARY;
                        resp.setContentType(new StringBuilder("multipart/byteranges; boundary=").append(boundary).toString());
                        resp.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT); // 206.

                        // Copy multi part range.
                        for (final Range r : ranges) {
                            // Add multipart boundary and header fields for every range.
                            outputStream.println();
                            outputStream.println(new StringBuilder("--").append(boundary).toString());
                            outputStream.println(new StringBuilder("Content-Type: ").append(contentType).toString());
                            outputStream.println(new StringBuilder("Content-Range: bytes ").append(r.start).append('-').append(r.end).append('/').append(r.total).toString());

                            // Copy single part range of multi part range.
                            copy(documentData, outputStream, r.start, r.length);
                        }

                        // End with multipart boundary.
                        outputStream.println();
                        outputStream.println(new StringBuilder("--").append(boundary).append("--").toString());
                    }
                } else {
                    // Check for "off"/"len" parameters
                    final int off = AJAXRequestDataTools.parseIntParameter(req.getParameter("off"), -1);
                    final int amount = AJAXRequestDataTools.parseIntParameter(req.getParameter("len"), -1);
                    if (off >= 0 && amount > 0) {
                        try {
                            resp.setHeader("Content-Length", Long.toString(amount));
                            copy(documentData, outputStream, off, amount);
                        } catch (final OffsetOutOfRangeIOException e) {
                            setHeaderSafe("Content-Range", "bytes */" + e.getAvailable(), resp); // Required in 416.
                            resp.sendError(HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
                            return;
                        }
                    } else {
                        final int len = BUFLEN;
                        final byte[] buf = new byte[len];
                        if (length > 0) {
                            // Check actual transferred number of bytes against provided length
                            long count = 0L;
                            for (int read; (read = documentData.read(buf, 0, len)) > 0;) {
                                outputStream.write(buf, 0, read);
                                count += read;
                            }
                            if (length != count) {
                                final StringBuilder sb = new StringBuilder("Transferred ").append((length > count ? "less" : "more"));
                                sb.append(" bytes than signaled through \"Content-Length\" response header. File download may get paused (less) or be corrupted (more).");
                                sb.append(" Associated file \"").append(fileName).append("\" with indicated length of ").append(length).append(", but is ").append(count);
                                LOG.warn(sb.toString());
                            }
                        } else {
                            for (int read; (read = documentData.read(buf, 0, len)) > 0;) {
                                outputStream.write(buf, 0, read);
                            }
                        }
                    }
                }
                outputStream.flush();
            } catch (final java.net.SocketException e) {
                final String lmsg = toLowerCase(e.getMessage());
                if ("broken pipe".equals(lmsg) || "connection reset".equals(lmsg)) {
                    // Assume client-initiated connection closure
                    LOG.debug("Underlying (TCP) protocol communication aborted while trying to output file{}", (isEmpty(fileName) ? "" : " " + fileName), e);
                } else {
                    LOG.warn("Lost connection to client while trying to output file{}", (isEmpty(fileName) ? "" : " " + fileName), e);
                }
            } catch (final com.sun.mail.util.MessageRemovedIOException e) {
                sendErrorSafe(HttpServletResponse.SC_NOT_FOUND, "Message not found.", resp);
            } catch (final IOException e) {
                final String lcm = toLowerCase(e.getMessage());
                if ("connection reset by peer".equals(lcm) || "broken pipe".equals(lcm)) {
                    /*-
                     * The client side has abruptly aborted the connection.
                     * That can have many causes which are not controllable by us.
                     *
                     * For instance, the peer doesn't understand what it received and therefore closes its socket.
                     * For the next write attempt by us, the peer's TCP stack will issue an RST,
                     * which results in this exception and message at the sender.
                     */
                    LOG.debug("Client dropped connection while trying to output file{}", (isEmpty(fileName) ? "" : " " + fileName), e);
                } else {
                    LOG.warn("Lost connection to client while trying to output file{}", (isEmpty(fileName) ? "" : " " + fileName), e);
                }
            }
        } catch (final OXException e) {
            String message = isEmpty(fileName) ? "Exception while trying to output file" : new StringBuilder("Exception while trying to output file ").append(fileName).toString();
            LOG.error(message, e);
            if (AjaxExceptionCodes.BAD_REQUEST.equals(e)) {
                Throwable cause = e;
                while (cause.getCause() != null) {
                    cause = cause.getCause();
                }
                final String causeMsg = cause.getMessage();
                sendErrorSafe(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null == causeMsg ? message : causeMsg, resp);
            } else {
                sendErrorSafe(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message, resp);
            }
        } catch (ImageTransformationDeniedIOException e) {
            // Quit with 406
            String message = isEmpty(fileName) ? "Exception while trying to output image" : new StringBuilder("Exception while trying to output image ").append(fileName).toString();
            LOG.error(message, e);
            sendErrorSafe(HttpServletResponse.SC_NOT_ACCEPTABLE, e.getMessage(), resp);
        } catch (final Exception e) {
            String message = isEmpty(fileName) ? "Exception while trying to output file" : new StringBuilder("Exception while trying to output file ").append(fileName).toString();
            LOG.error(message, e);
            sendErrorSafe(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message, resp);
        } finally {
            close(documentData, file);
            close(closeables);
        }
    }

    private void setContentLengthHeader(final long length, final HttpServletResponse resp) {
        if (length > 0) {
            resp.setHeader("Accept-Ranges", "bytes");
            resp.setHeader("Content-Length", Long.toString(length));
        } else {
            resp.setHeader("Accept-Ranges", "none");
        }
    }

    private String getContentTypeByFileName(final String fileName) {
        return null == fileName ? null : MimeType2ExtMap.getContentType(fileName, null);
    }

    private void sendErrorSafe(int sc, String msg, final HttpServletResponse resp) {
        try {
            Tools.sendErrorPage(resp, sc, msg);
        } catch (final Exception e) {
            // Ignore
        }
    }

    /** Attempts to set a sanitized <code>Content-Type</code> header value to given HTTP response. */
    private boolean trySetSanitizedContentType(final String contentType, final String fallbackContentType, final HttpServletResponse resp) {
        try {
            resp.setContentType(new ContentType(contentType).getBaseType());
            return true;
        } catch (final Exception e) {
            // Ignore
            resp.setContentType(fallbackContentType);
        }
        return false;
    }

    private void setHeaderSafe(final String name, final String value, final HttpServletResponse resp) {
        try {
            resp.setHeader(name, value);
        } catch (final Exception e) {
            // Ignore
        }
    }

    private IFileHolder transformIfImage(final AJAXRequestData request, final AJAXRequestResult result, final IFileHolder fileHolder, final String delivery) throws IOException, OXException {
        /*
         * check input
         */
        final ImageTransformationService scaler = this.scaler;
        if (null == scaler || false == isImage(fileHolder)) {
            return fileHolder;
        }

        // the optional parameter "transformationNeeded" is set by the PreviewImageResultConverter if no transformation is needed.
        // This is done if the preview was generated by the com.openexchage.documentpreview.OfficePreviewDocument service
        if (request.isSet("transformationNeeded") && (request.getParameter("transformationNeeded", Boolean.class).booleanValue() == false)) {
            return fileHolder;
        }

        // Check if there is any need left to trigger image transformation
        IFileHolder file = fileHolder;
        {
            boolean transform = false;
            if (request.isSet("cropWidth") || request.isSet("cropHeight")) {
                transform = true;
            }
            if (!transform && (request.isSet("width") || request.isSet("height"))) {
                transform = true;
            }
            if (!transform) {
                final Boolean rotate = request.isSet("rotate") ? request.getParameter("rotate", Boolean.class) : null;
                if (null != rotate && rotate.booleanValue()) {
                    transform = true;
                }
            }
            if (!transform) {
                final Boolean compress = request.isSet("compress") ? request.getParameter("compress", Boolean.class) : null;
                if (null != compress && compress.booleanValue()) {
                    transform = true;
                }
            }
            // Rotation/compression only required for JPEG
            if (!transform) {
                final String formatName = toLowerCase(ImageTransformationUtility.getImageFormat(fileHolder.getContentType()));
                if (("jpeg".equals(formatName) || "jpg".equals(formatName)) && !DOWNLOAD.equalsIgnoreCase(delivery)) {
                    // Ensure IFileHolder is repetitive
                    if (!file.repetitive()) {
                        file = new ThresholdFileHolder(file);
                    }

                    // Acquire stream and check for possible compression
                    InputStream stream = file.getStream();
                    if (null == stream) {
                        // Huh...?
                        LOG.warn("(Possible) Image file misses stream data");
                        return file;
                    }
                    if (ImageTransformationUtility.requiresRotateTransformation(stream)) {
                        transform = true;
                    }
                }
            }

            if (!transform) {
                return file;
            }
        }

        // Check cache first
        final ResourceCache resourceCache;
        {
            final ResourceCache tmp = ResourceCaches.getResourceCache();
            resourceCache = null == tmp ? null : (tmp.isEnabledFor(request.getSession().getContextId(), request.getSession().getUserId()) ? tmp : null);
        }
        // Get eTag from result that provides the IFileHolder
        final String eTag = result.getHeader("ETag");
        final boolean isValidEtag = !isEmpty(eTag);
        final String previewLanguage = AbstractPreviewResultConverter.getUserLanguage(request.getSession());
        if (null != resourceCache && isValidEtag && AJAXRequestDataTools.parseBoolParameter("cache", request, true)) {
            final String cacheKey = ResourceCaches.generatePreviewCacheKey(eTag, request, previewLanguage);
            final CachedResource cachedResource = resourceCache.get(cacheKey, 0, request.getSession().getContextId());
            if (null != cachedResource) {
                // Scaled version already cached
                // Create appropriate IFileHolder
                String contentType = cachedResource.getFileType();
                if (null == contentType) {
                    contentType = "image/jpeg";
                }
                final IFileHolder ret;
                {
                    final InputStream inputStream = cachedResource.getInputStream();
                    if (null == inputStream) {
                        @SuppressWarnings("resource")
                        final ByteArrayFileHolder responseFileHolder = new ByteArrayFileHolder(cachedResource.getBytes());
                        responseFileHolder.setContentType(contentType);
                        responseFileHolder.setName(cachedResource.getFileName());
                        ret = responseFileHolder;
                    } else {
                        // From stream
                        ret = new FileHolder(inputStream, cachedResource.getSize(), contentType, cachedResource.getFileName());
                    }
                }
                return ret;
            }
        }

        // OK, so far we assume image transformation is needed
        // Ensure IFileHolder is repetitive
        if (!file.repetitive()) {
            file = new ThresholdFileHolder(file);
        }

        // Validate...
        {
            InputStream stream = file.getStream();
            if (null == stream) {
                LOG.warn("(Possible) Image file misses stream data");
                return file;
            }

            // Check for an animated .gif image
            if (ImageUtils.isAnimatedGif(stream)) {
                return fileHolder;
            }
        }

        // Start transformations: scale, rotate, ...
        ImageTransformations transformations = scaler.transfom(file, request.getSession().getSessionID());

        // Rotate by default when not delivering as download
        final Boolean rotate = request.isSet("rotate") ? request.getParameter("rotate", Boolean.class) : null;
        if (null == rotate && false == DOWNLOAD.equalsIgnoreCase(delivery) || null != rotate && rotate.booleanValue()) {
            transformations.rotate();
        }
        if (request.isSet("cropWidth") || request.isSet("cropHeight")) {
            final int cropX = request.isSet("cropX") ? request.getParameter("cropX", int.class).intValue() : 0;
            final int cropY = request.isSet("cropY") ? request.getParameter("cropY", int.class).intValue() : 0;
            final int cropWidth = request.getParameter("cropWidth", int.class).intValue();
            final int cropHeight = request.getParameter("cropHeight", int.class).intValue();
            transformations.crop(cropX, cropY, cropWidth, cropHeight);
        }
        if (request.isSet("width") || request.isSet("height")) {
            final int maxWidth = request.isSet("width") ? request.getParameter("width", int.class).intValue() : 0;
            if (maxWidth > Constants.getMaxWidth()) {
                throw AjaxExceptionCodes.BAD_REQUEST.create("Width " + maxWidth + " exceeds max. supported width " + Constants.getMaxWidth());
            }
            final int maxHeight = request.isSet("height") ? request.getParameter("height", int.class).intValue() : 0;
            if (maxHeight > Constants.getMaxHeight()) {
                throw AjaxExceptionCodes.BAD_REQUEST.create("Height " + maxHeight + " exceeds max. supported height " + Constants.getMaxHeight());
            }
            final ScaleType scaleType = ScaleType.getType(request.getParameter("scaleType"));
            try {
                transformations.scale(maxWidth, maxHeight, scaleType);
            } catch (final IllegalArgumentException e) {
                throw AjaxExceptionCodes.BAD_REQUEST_CUSTOM.create(e, e.getMessage());
            }
        }
        // Compress by default when not delivering as download
        final Boolean compress = request.isSet("compress") ? request.getParameter("compress", Boolean.class) : null;
        if ((null == compress && false == DOWNLOAD.equalsIgnoreCase(delivery)) || (null != compress && compress.booleanValue())) {
            transformations.compress();
        }
        /*
         * Transform
         */
        boolean cachingAdvised = false;
        try {
            String fileContentType = file.getContentType();
            if (null == fileContentType || !Strings.toLowerCase(fileContentType).startsWith("image/")) {
                final String contentTypeByFileName = getContentTypeByFileName(file.getName());
                if (null != contentTypeByFileName) {
                    fileContentType = contentTypeByFileName;
                }
            }
            final byte[] transformed;
            try {
                TransformedImage transformedImage = transformations.getTransformedImage(fileContentType);
                int expenses = transformedImage.getTransformationExpenses();
                if (expenses >= ImageTransformations.HIGH_EXPENSE) {
                    cachingAdvised = true;
                }

                transformed = transformedImage.getImageData();
            } catch (final IOException ioe) {
                if ("Unsupported Image Type".equals(ioe.getMessage())) {
                    return handleFailure(file);
                }
                // Rethrow...
                throw ioe;
            }
            if (null == transformed) {
                LOG.debug("Got no resulting input stream from transformation, trying to recover original input");
                return handleFailure(file);
            }
            // Return immediately if not cacheable
            if (!cachingAdvised || null == resourceCache || !isValidEtag || !AJAXRequestDataTools.parseBoolParameter("cache", request, true)) {
                return new FileHolder(Streams.newByteArrayInputStream(transformed), -1, fileContentType, file.getName());
            }

            // (Asynchronously) Add to cache if possible
            final int size = transformed.length;
            final String cacheKey = ResourceCaches.generatePreviewCacheKey(eTag, request, previewLanguage);
            final ServerSession session = request.getSession();
            final String fileName = file.getName();
            final String contentType = fileContentType;
            final AbstractTask<Void> task = new AbstractTask<Void>() {

                @Override
                public Void call() {
                    try {
                        final CachedResource preview = new CachedResource(transformed, fileName, contentType, size);
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
            // Return
            return new FileHolder(new ByteArrayInputStreamClosure(transformed), size, contentType, fileName);
        } catch (final RuntimeException e) {
            if (LOG.isDebugEnabled() && file.repetitive()) {
                try {
                    final File tmpFile = writeBrokenImage2Disk(file);
                    LOG.error("Unable to transform image from {}. Unparseable image file is written to disk at: {}", file.getName(), tmpFile.getPath(), e);
                } catch (final Exception x) {
                    LOG.error("Unable to transform image from {}", file.getName(), e);
                }
            } else {
                LOG.error("Unable to transform image from {}", file.getName(), e);
            }
            return file.repetitive() ? file : null;
        }
    }

    private IFileHolder handleFailure(IFileHolder file) {
        LOG.warn("Unable to transform image from {}", file.getName());
        return file.repetitive() ? file : null;
    }

    /**
     * Checks specified <i>Content-Disposition</i> value against passed {@link IFileHolder file}.
     * <p>
     * E.g. <code>"inline"</code> is not allowed for <code>"text/html"</code> MIME type.
     *
     * @param contentDisposition The <i>Content-Disposition</i> value to check
     * @param file The file
     * @return The checked <i>Content-Disposition</i> value
     */
    private String checkedContentDisposition(final String contentDisposition, final IFileHolder file) {
        final String ct = toLowerCase(file.getContentType()); // null-safe
        if (null == ct || ct.startsWith("text/htm")) {
            final int pos = contentDisposition.indexOf(';');
            return pos > 0 ? "attachment" + contentDisposition.substring(pos) : "attachment";
        }
        return contentDisposition;
    }

    private boolean isImage(final IFileHolder file) {
        if (0 == file.getLength()) {
            // File signals no available data
            return false;
        }
        String contentType = file.getContentType();
        if (null == contentType || !contentType.startsWith("image/")) {
            final String fileName = file.getName();
            if (fileName == null || !(contentType = MimeType2ExtMap.getContentType(fileName)).startsWith("image/")) {
                return false;
            }
        }
        return true;
    }

    private String toLowerCase(final CharSequence chars) {
        if (null == chars) {
            return null;
        }
        final int length = chars.length();
        final StringBuilder builder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            final char c = chars.charAt(i);
            builder.append((c >= 'A') && (c <= 'Z') ? (char) (c ^ 0x20) : c);
        }
        return builder.toString();
    }

    /**
     * Removes single or double quotes from a string if its quoted.
     *
     * @param s The value to be unquoted
     * @return The unquoted value or <code>null</code>
     */
    private String unquote(final String s) {
        if (!isEmpty(s) && ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'")))) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    private String getPrimaryType(final String contentType) {
        if (isEmpty(contentType)) {
            return contentType;
        }
        final int pos = contentType.indexOf('/');
        return pos > 0 ? contentType.substring(0, pos) : contentType;
    }

    private boolean equalPrimaryTypes(final String contentType1, final String contentType2) {
        if (null == contentType1 || null == contentType2) {
            return false;
        }
        return toLowerCase(getPrimaryType(contentType1)).startsWith(toLowerCase(getPrimaryType(contentType2)));
    }

    private String detectMimeType(final InputStream in) throws IOException {
        return AJAXUtility.detectMimeType(in);
    }

    private boolean equalTypes(final String contentType1, final String contentType2) {
        if (null == contentType1 || null == contentType2) {
            return false;
        }
        return com.openexchange.java.Strings.toLowerCase(getType(contentType1)).startsWith(com.openexchange.java.Strings.toLowerCase(getType(contentType2)));
    }

    private String getType(final String contentType) {
        if (isEmpty(contentType)) {
            return contentType;
        }
        final int pos = contentType.indexOf(';');
        return pos > 0 ? contentType.substring(0, pos) : contentType;
    }

    /**
     * Returns a substring of the given string value from the given begin index to the given end index as a long.
     * <p>
     * If the substring is empty, then <code>-1</code> will be returned
     *
     * @param value The string value to return a substring as long for.
     * @param beginIndex The begin index of the substring to be returned as long.
     * @param endIndex The end index of the substring to be returned as long.
     * @return A substring of the given string value as long or <code>-1</code> if substring is empty.
     */
    private long sublong(final String value, final int beginIndex, final int endIndex) {
        final String substring = value.substring(beginIndex, endIndex);
        return (substring.length() > 0) ? Long.parseLong(substring) : -1;
    }

    /**
     * Copies the given byte range of the given input to the given output.
     *
     * @param inputStream The input to copy the given range to the given output for.
     * @param output The output to copy the given range from the given input for.
     * @param start Start of the byte range.
     * @param length Length of the byte range.
     * @throws IOException If something fails at I/O level.
     */
    private void copy(Readable inputStream, OutputStream output, long start, long length) throws IOException {
        if (inputStream instanceof IFileHolder.RandomAccess) {
            copy((IFileHolder.RandomAccess) inputStream, output, start, length);
            return;
        }

        // Write partial range.
        // Discard previous bytes
        byte[] buffer = new byte[1];
        for (int i = 0; i < start; i++) {
            if (inputStream.read(buffer, 0, 1) < 0) {
                // Stream does not provide enough bytes
                throw new OffsetOutOfRangeIOException(start, i);
            }
            // Valid byte read... Continue.
        }
        long toRead = length;

        int buflen = BUFLEN;
        buffer = new byte[buflen];
        int read;
        while ((read = inputStream.read(buffer, 0, buflen)) > 0) {
            if ((toRead -= read) > 0) {
                output.write(buffer, 0, read);
            } else {
                output.write(buffer, 0, (int) toRead + read);
                break;
            }
        }
    }

    /**
     * Copies the given byte range of the given input to the given output.
     *
     * @param input The input to copy the given range to the given output for.
     * @param output The output to copy the given range from the given input for.
     * @param start Start of the byte range.
     * @param length Length of the byte range.
     * @throws IOException If something fails at I/O level.
     */
    private static void copy(IFileHolder.RandomAccess input, OutputStream output, long start, long length) throws IOException {
        int buflen = BUFLEN;
        byte[] buffer = new byte[buflen];
        int read;

        if (input.length() == length) {
            // Write full range.
            while ((read = input.read(buffer, 0, buflen)) > 0) {
                output.write(buffer, 0, read);
            }
        } else {
            // Write partial range.
            input.seek(start);   // ----> OffsetOutOfRangeIOException
            long toRead = length;

            // Check first byte
            @SuppressWarnings("resource")
            PushbackReadable readMe = new PushbackReadable(input);
            {
                byte[] bs = new byte[1];
                int first = readMe.read(bs);
                if (first <= 0) {
                    // Not enough bytes
                    throw new OffsetOutOfRangeIOException(start, input.length());
                }

                // Unread first byte
                readMe.unread(bs[0] & 0xff);
            }

            while ((read = readMe.read(buffer, 0, buflen)) > 0) {
                if ((toRead -= read) > 0) {
                    output.write(buffer, 0, read);
                } else {
                    output.write(buffer, 0, (int) toRead + read);
                    break;
                }
            }
        }
    }

    private boolean hasNoFileItem(final IFileHolder file) {
        final String fileMIMEType = file.getContentType();
        return ((isEmpty(fileMIMEType) || SAVE_AS_TYPE.equals(fileMIMEType)) && isEmpty(file.getName()) && (file.getLength() <= 0L));
    }

    private static final class Range {

        /** The begin position (inclusive) */
        final long start;

        /** The end position (inclusive) */
        final long end;

        /** The length */
        final long length;

        /** The total length */
        final long total;

        Range(final long start, final long end, final long total) {
            super();
            this.start = start;
            this.end = end;
            length = end - start + 1;
            this.total = total;
        }

    } // End of class Range

    private static final class OffsetOutOfRangeIOException extends IOException {

        private static final long serialVersionUID = 8094333124726048736L;

        private final long off;

        private final long available;

        /**
         * Initializes a new {@link OffsetOutOfRangeIOException}.
         */
        public OffsetOutOfRangeIOException(final long off, final long available) {
            super("Offset " + off + " out of range. Got only " + available);
            this.off = off;
            this.available = available;
        }

        @Override
        public synchronized Throwable fillInStackTrace() {
            return this;
        }

        /**
         * Gets the off
         *
         * @return The off
         */
        public long getOff() {
            return off;
        }

        /**
         * Gets the available
         *
         * @return The available
         */
        public long getAvailable() {
            return available;
        }

    } // End of class OffsetOutOfRangeIOException

    /**
     * Gets the appropriate directory to save to.
     *
     * @param path The path as indicated by configuration
     * @return The directory
     */
    static File getTmpDirByPath(final String path) {
        if (null == path) {
            throw new IllegalArgumentException("Path is null. Probably property \"UPLOAD_DIRECTORY\" is not set.");
        }
        final File tmpDir = new File(path);
        if (!tmpDir.exists()) {
            throw new IllegalArgumentException("Directory " + path + " does not exist.");
        }
        if (!tmpDir.isDirectory()) {
            throw new IllegalArgumentException(path + " is not a directory.");
        }
        return tmpDir;
    }

    private static final class FileManagementPropertyListener implements PropertyListener {

        private final AtomicReference<File> ttmpDirReference;

        FileManagementPropertyListener(final AtomicReference<File> tmpDirReference) {
            super();
            ttmpDirReference = tmpDirReference;
        }

        @Override
        public void onPropertyChange(final PropertyEvent event) {
            if (PropertyEvent.Type.CHANGED.equals(event.getType())) {
                ttmpDirReference.set(getTmpDirByPath(event.getValue()));
            }
        }
    }

    private File writeBrokenImage2Disk(final IFileHolder file) throws IOException, OXException, FileNotFoundException {
        String suffix = null;
        {
            final String name = file.getName();
            if (null != name) {
                final int pos = name.lastIndexOf('.');
                if (pos > 0 && pos < name.length() - 1) {
                    suffix = name.substring(pos);
                }
            }
            if (null == suffix) {
                final String contentType = file.getContentType();
                if (null != contentType) {
                    suffix = "." + MimeType2ExtMap.getFileExtension(contentType);
                }
            }
        }
        return write2Disk(file, "brokenimage-", suffix);
    }

    private File write2Disk(final IFileHolder file, final String prefix, final String suffix) throws IOException, OXException, FileNotFoundException {
        final File directory = tmpDirReference.get();
        final File newFile = File.createTempFile(null == prefix ? "open-xchange-" : prefix, null == suffix ? ".tmp" : suffix, directory);
        final InputStream is = file.getStream();
        final OutputStream out = new FileOutputStream(newFile);
        try {
            final int len = 8192;
            final byte[] buf = new byte[len];
            for (int read; (read = is.read(buf, 0, len)) > 0;) {
                out.write(buf, 0, read);
            }
            out.flush();
        } finally {
            Streams.close(is, out);
        }
        return newFile;
    }

}
