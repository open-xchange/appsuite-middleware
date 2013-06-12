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

package com.openexchange.ajax.requesthandler.responseRenderers;

import static com.openexchange.java.Streams.close;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.apache.tika.Tika;
import org.apache.tika.config.TikaConfig;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.jpeg.JpegDirectory;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.FileHolder;
import com.openexchange.ajax.container.IFileHolder;
import com.openexchange.ajax.container.ThresholdFileHolder;
import com.openexchange.ajax.helper.DownloadUtility;
import com.openexchange.ajax.helper.DownloadUtility.CheckedDownload;
import com.openexchange.ajax.helper.HTMLDetector;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.ResponseRenderer;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.java.StringAllocator;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.tools.images.ImageTransformationService;
import com.openexchange.tools.images.ImageTransformations;
import com.openexchange.tools.images.ScaleType;
import com.openexchange.tools.servlet.http.Tools;

/**
 * {@link FileResponseRenderer}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class FileResponseRenderer implements ResponseRenderer {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(FileResponseRenderer.class);

    private static final int BUFLEN = 2048;

    private static final String PARAMETER_CONTENT_DISPOSITION = "content_disposition";
    private static final String PARAMETER_CONTENT_TYPE = "content_type";

    private static final String SAVE_AS_TYPE = "application/octet-stream";

    private volatile ImageTransformationService scaler;

    private static final String DELIVERY = AJAXServlet.PARAMETER_DELIVERY;

    private static final String DOWNLOAD = "download";
    private static final String VIEW = "view";

    private static final String MULTIPART_BOUNDARY = "MULTIPART_BYTERANGES";

    private static final Pattern PATTERN_BYTE_RANGES = Pattern.compile("^bytes=\\d*-\\d*(,\\d*-\\d*)*$");

    private final Tika tika;

    /**
     * Initializes a new {@link FileResponseRenderer}.
     */
    public FileResponseRenderer() {
        super();
        tika = new Tika(TikaConfig.getDefaultConfig());
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
    public void setScaler(final ImageTransformationService scaler) {
        this.scaler = scaler;
    }

    @Override
    public boolean handles(final AJAXRequestData request, final AJAXRequestResult result) {
        return (result.getResultObject() instanceof IFileHolder);
    }

    @Override
    public void write(final AJAXRequestData request, final AJAXRequestResult result, final HttpServletRequest req, final HttpServletResponse resp) {
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
        writeFileHolder(file, request, result, req, resp);
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
    public void writeFileHolder(final IFileHolder fileHolder, final AJAXRequestData requestData, final AJAXRequestResult result, final HttpServletRequest req, final HttpServletResponse resp) {
        IFileHolder file = fileHolder;
        final String fileName = file.getName();
        final long length;
        final List<Closeable> closeables = new LinkedList<Closeable>();
        InputStream documentData = null;
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
            String delivery = AJAXServlet.sanitizeParam(req.getParameter(DELIVERY));
            if (delivery == null) {
                delivery = file.getDelivery();
            }
            String contentType = AJAXServlet.encodeUrl(req.getParameter(PARAMETER_CONTENT_TYPE), true);
            boolean contentTypeByParameter = false;
            if (null == contentType) {
                if (DOWNLOAD.equalsIgnoreCase(delivery)) {
                    contentType = SAVE_AS_TYPE;
                } else {
                    contentType = fileContentType;
                }
            } else {
                contentTypeByParameter = true;
            }
            contentType = unquote(contentType);
            String contentDisposition = AJAXServlet.encodeUrl(req.getParameter(PARAMETER_CONTENT_DISPOSITION));
            if (null == contentDisposition) {
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
            file = transformIfImage(requestData, file, delivery);
            if (null == file) {
                // Quit with 404
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found.");
                return;
            }
            // Set binary input stream
            {
                final InputStream stream = file.getStream();
                if (null != stream) {
                    if ((stream instanceof ByteArrayInputStream) || (stream instanceof BufferedInputStream)) {
                        documentData = stream;
                    } else {
                        documentData = new BufferedInputStream(stream);
                    }
                }
            }
            // Check for null
            if (null == documentData) {
                // Quit with 404
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found.");
                return;
            }
            final String userAgent = AJAXServlet.sanitizeParam(req.getHeader("user-agent"));
            if (SAVE_AS_TYPE.equals(contentType) || DOWNLOAD.equalsIgnoreCase(delivery)) {
                // Write as a common file download: application/octet-stream
                final StringAllocator sb = new StringAllocator(32);
                sb.append(isEmpty(contentDisposition) ? "attachment" : checkedContentDisposition(contentDisposition.trim(), file));
                DownloadUtility.appendFilenameParameter(fileName, null, userAgent, sb);
                resp.setHeader("Content-Disposition", sb.toString());
                resp.setContentType(SAVE_AS_TYPE);
                length = file.getLength();
            } else {
                // Determine what Content-Type is indicated by file name
                String contentTypeByFileName;
                if (null == fileName) {
                    // Not known
                    contentTypeByFileName = null;
                } else {
                    contentTypeByFileName = MimeType2ExtMap.getContentType(fileName);
                    if (SAVE_AS_TYPE.equals(contentTypeByFileName)) {
                        // Not known
                        contentTypeByFileName = null;
                    }
                }
                // Generate checked download
                final CheckedDownload checkedDownload;
                {
                    long fileLength = file.getLength();
                    String cts;
                    if (null == fileContentType || SAVE_AS_TYPE.equals(fileContentType)) {
                        if (null == contentTypeByFileName) {
                            // Let Tika detect the Content-Type
                            final ByteArrayOutputStream baos = Streams.stream2ByteArrayOutputStream(documentData);
                            // We know for sure
                            fileLength = baos.size();
                            documentData = Streams.asInputStream(baos);
                            cts = tika.detect(Streams.asInputStream(baos));
                            if ("text/plain".equals(cts)) {
                                cts = HTMLDetector.containsHTMLTags(baos.toByteArray()) ? "text/html" : cts;
                            }
                        } else {
                            cts = contentTypeByFileName;
                        }
                    } else {
                        if ((null != contentTypeByFileName) && !equalPrimaryTypes(fileContentType, contentTypeByFileName)) {
                            // Differing Content-Types sources
                            final ThresholdFileHolder temp = new ThresholdFileHolder();
                            closeables.add(temp);
                            temp.write(documentData);
                            // We know for sure
                            fileLength = temp.getLength();
                            documentData = temp.getStream();
                            cts = detectMimeType(temp.getStream());
                            if ("text/plain".equals(cts)) {
                                final byte[] bytes = Streams.stream2bytes(temp.getStream());
                                cts = HTMLDetector.containsHTMLTags(bytes) ? "text/html" : cts;
                            }
                        } else {
                            cts = fileContentType;
                        }
                    }
                    checkedDownload = DownloadUtility.checkInlineDownload(documentData, fileLength, fileName, cts, contentDisposition, userAgent);
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
                } else if (delivery.equalsIgnoreCase(VIEW) && null != fileName) {
                    final StringAllocator sb = new StringAllocator(32);
                    sb.append("inline");
                    DownloadUtility.appendFilenameParameter(fileName, null, userAgent, sb);
                    resp.setHeader("Content-Disposition", sb.toString());
                }
                /*
                 * Set Content-Length if possible
                 */
                length = checkedDownload.getSize();
                if (length > 0) {
                    resp.setHeader("Accept-Ranges", "bytes");
                    resp.setHeader("Content-Length", Long.toString(length));
                }
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
                if (null != contentTypeByFileName) {
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
                    resp.setContentType(preferredContentType);
                    contentType = preferredContentType;
                } else {
                    if (SAVE_AS_TYPE.equals(preferredContentType) || equalPrimaryTypes(preferredContentType, contentType)) {
                        // Set if sanitize-able
                        if (!trySetSanitizedContentType(contentType, preferredContentType, resp)) {
                            contentType = preferredContentType;
                        }
                    } else {
                        // Specified Content-Type does NOT match file's real MIME type
                        final ThresholdFileHolder temp = new ThresholdFileHolder();
                        closeables.add(temp);
                        temp.write(documentData);
                        documentData = temp.getStream();
                        preferredContentType = detectMimeType(temp.getStream());
                        if ("text/plain".equals(preferredContentType)) {
                            final byte[] bytes = Streams.stream2bytes(temp.getStream());
                            preferredContentType = HTMLDetector.containsHTMLTags(bytes) ? "text/html" : preferredContentType;
                        }
                        // One more time...
                        if (equalPrimaryTypes(preferredContentType, contentType)) {
                            // Set if sanitize-able
                            if (!trySetSanitizedContentType(contentType, preferredContentType, resp)) {
                                contentType = preferredContentType;
                            }
                        } else {
                            // Ignore it due to security reasons (see bug #25343)
                            final StringAllocator sb = new StringAllocator(128);
                            sb.append("Denied parameter \"").append(PARAMETER_CONTENT_TYPE);
                            sb.append("\" due to security constraints (requested \"");
                            sb.append(contentType).append("\" , but is \"").append(preferredContentType).append("\").");
                            LOG.warn(sb.toString());
                            resp.setContentType(preferredContentType);
                            contentType = preferredContentType;
                        }
                    }
                }
            }
            /*
             * Browsers don't like the Pragma header the way we usually set this. Especially if files are sent to the browser. So removing
             * pragma header.
             */
            Tools.removeCachingHeader(resp);
            if (delivery == null || !delivery.equalsIgnoreCase(DOWNLOAD)) {
                /*
                 * ETag present and caching?
                 */
                final String eTag = result.getHeader("ETag");
                if (null != eTag) {
                    final long expires = result.getExpires();
                    Tools.setETag(eTag, expires > 0 ? new Date(System.currentTimeMillis() + expires) : null, resp);
                } else {
                    final long expires = result.getExpires();
                    if (expires < 0) {
                        Tools.setExpiresInOneYear(resp);
                    } else if (expires > 0) {
                        Tools.setExpires(new Date(System.currentTimeMillis() + expires), resp);
                    }
                }
            }
            /*
             * Output binary content
             */
            try {
                final ServletOutputStream outputStream = resp.getOutputStream();
                final String sRange;
                if (length > 0 && null != (sRange = req.getHeader("Range"))) {
                    // Taken from http://balusc.blogspot.co.uk/2009/02/fileservlet-supporting-resume-and.html
                    // Range header should match format "bytes=n-n,n-n,n-n...". If not, then return 416.
                    if (!PATTERN_BYTE_RANGES.matcher(sRange).matches()) {
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
                        resp.setHeader("Content-Range", new StringAllocator("bytes ").append(r.start).append('-').append(r.end).append('/').append(r.total).toString());

                        // Copy full range.
                        copy(documentData, outputStream, r.start, r.length);
                    } else if (ranges.size() == 1) {

                        // Return single part of file.
                        final Range r = ranges.get(0);
                        resp.setHeader("Content-Range", new StringAllocator("bytes ").append(r.start).append('-').append(r.end).append('/').append(r.total).toString());
                        resp.setHeader("Content-Length", Long.toString(r.length));
                        resp.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT); // 206.

                        // Copy single part range.
                        copy(documentData, outputStream, r.start, r.length);
                    } else {
                        // Return multiple parts of file.
                        final String boundary = MULTIPART_BOUNDARY;
                        resp.setContentType(new StringAllocator("multipart/byteranges; boundary=").append(boundary).toString());
                        resp.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT); // 206.

                        // Copy multi part range.
                        for (final Range r : ranges) {
                            // Add multipart boundary and header fields for every range.
                            outputStream.println();
                            outputStream.println(new StringAllocator("--").append(boundary).toString());
                            outputStream.println(new StringAllocator("Content-Type: ").append(contentType).toString());
                            outputStream.println(new StringAllocator("Content-Range: bytes ").append(r.start).append('-').append(r.end).append('/').append(r.total).toString());

                            // Copy single part range of multi part range.
                            copy(documentData, outputStream, r.start, r.length);
                        }

                        // End with multipart boundary.
                        outputStream.println();
                        outputStream.println(new StringAllocator("--").append(boundary).append("--").toString());
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
                            StringAllocator sb = new StringAllocator("Transferred ").append((length > count ? "less" : "more"));
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
                outputStream.flush();
            } catch (final java.net.SocketException e) {
                final String lmsg = toLowerCase(e.getMessage());
                if ("broken pipe".equals(lmsg) || "connection reset".equals(lmsg)) {
                    // Assume client-initiated connection closure
                    LOG.debug("Underlying (TCP) protocol communication aborted while trying to output file" + (isEmpty(fileName) ? "" : " " + fileName), e);
                } else {
                    LOG.warn("Lost connection to client while trying to output file" + (isEmpty(fileName) ? "" : " " + fileName), e);
                }
            } catch (final com.sun.mail.util.MessageRemovedIOException e) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "Message not found.");
            } catch (final IOException e) {
                if ("connection reset by peer".equals(toLowerCase(e.getMessage()))) {
                    /*-
                     * The client side has abruptly aborted the connection.
                     * That can have many causes which are not controllable by us.
                     *
                     * For instance, the peer doesn't understand what it received and therefore closes its socket.
                     * For the next write attempt by us, the peer's TCP stack will issue an RST,
                     * which results in this exception and message at the sender.
                     */
                    LOG.debug("Client dropped connection while trying to output file" + (isEmpty(fileName) ? "" : " " + fileName), e);
                } else {
                    LOG.warn("Lost connection to client while trying to output file" + (isEmpty(fileName) ? "" : " " + fileName), e);
                }
            }
        } catch (final Exception e) {
            LOG.error("Exception while trying to output file" + (isEmpty(fileName) ? "" : " " + fileName), e);
        } finally {
            close(file, documentData);
            close(closeables);
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

    /** Checks if transformation is needed */
    private boolean isTransformationNeeded(final AJAXRequestData request, final IFileHolder file, final String delivery) throws OXException, IOException {

        // this check is only for jpeg images, other image formats are always transformated
        boolean transformationNeeded = !file.getContentType().toLowerCase().startsWith("image/jpeg");
        if (!transformationNeeded) {
            transformationNeeded = request.isSet("cropWidth") || request.isSet("cropHeight");
        }
        if (!transformationNeeded) {
            // we need repetitive access to the stream for further testing
            final InputStream stream = file.getStream();
            if (null == stream) {
                LOG.warn("(Possible) Image file misses stream data");
                return false;
            }
            BufferedInputStream inputStream = null;
            if (file.repetitive()) {
                inputStream = new BufferedInputStream(stream);
            } else if (stream.markSupported() && file.getLength() > 0 && file.getLength() < 0x20000) {
                // mark supported, but only allowing files < 128kb
                inputStream = new BufferedInputStream(stream, (int) file.getLength());
            }
            if (inputStream == null) {
                // no repetitive stream available... transformation must be done
                transformationNeeded = true;
            }
            else {

                try {
                    // retrieve MetaData to check if width, height or rotate requires a transformation
                    final com.drew.metadata.Metadata metadata = ImageMetadataReader.readMetadata(inputStream, false);
                    if (metadata == null)
                        transformationNeeded = true;
                    else {
                        // check for rotation
                        int orientation = 1;
                        final ExifIFD0Directory exifDirectory = metadata.getDirectory(ExifIFD0Directory.class);
                        if(exifDirectory!=null)
                            orientation = exifDirectory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
                        if(orientation!=1) {
                            final Boolean rotate = request.isSet("rotate") ? request.getParameter("rotate", Boolean.class) : null;
                            if (null == rotate && false == DOWNLOAD.equalsIgnoreCase(delivery) || null != rotate && rotate.booleanValue())
                                transformationNeeded = true;
                        }

                        // check width & height
                        final JpegDirectory jpegDirectory = metadata.getDirectory(JpegDirectory.class);
                        if (null == jpegDirectory)
                            transformationNeeded = true;
                        else {
                            // check width & height
                            final int width = jpegDirectory.getImageWidth();
                            final int height = jpegDirectory.getImageHeight();
                            final int maxWidth = request.isSet("width") ? request.getParameter("width", int.class).intValue() : 0;
                            final int maxHeight = request.isSet("height") ? request.getParameter("height", int.class).intValue() : 0;
                            final ScaleType scaleType = ScaleType.getType(request.getParameter("scaleType"));

                            // transformation only required if the image size exceeds the requested size
                            if(scaleType==ScaleType.CONTAIN) {
                                transformationNeeded = (maxWidth > 0 && width > maxWidth) || (maxHeight > 0 && height > maxHeight);
                            }
                            // cover... the size must have the exact size
                            else {
                                transformationNeeded = (maxWidth > 0 && width != maxWidth) || (maxHeight > 0 && height != maxHeight);
                            }
                        }
                    }
                } catch (final ImageProcessingException e) {
                    transformationNeeded = true;
                } catch (final MetadataException e) {
                    transformationNeeded = true;
                }
                if (!file.repetitive() && stream.markSupported()) {
                    stream.reset();
                }
            }
        }
        return transformationNeeded;
    }

    private IFileHolder transformIfImage(final AJAXRequestData request, final IFileHolder file, final String delivery) throws IOException, OXException {
        /*
         * check input
         */
        final ImageTransformationService scaler = this.scaler;
        if (null == scaler || false == isImage(file)) {
            return file;
        }

        if(!isTransformationNeeded(request, file, delivery)) {
            return file;
        }

        /*
         * build transformations
         */
        final InputStream stream = file.getStream();
        if (null == stream) {
            LOG.warn("(Possible) Image file misses stream data");
            return file;
        }
        // mark stream if possible
        final boolean markSupported = file.repetitive() ? false : stream.markSupported();
        if (markSupported) {
            stream.mark(131072); // 128KB
        }
        // start transformations: scale, rotate, ...
        final ImageTransformations transformations = scaler.transfom(stream);
        // rotate by default when not delivering as download
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
            final int maxHeight = request.isSet("height") ? request.getParameter("height", int.class).intValue() : 0;
            final ScaleType scaleType = ScaleType.getType(request.getParameter("scaleType"));
            transformations.scale(maxWidth, maxHeight, scaleType);
        }
        // compress by default when not delivering as download
        final Boolean compress = request.isSet("compress") ? request.getParameter("compress", Boolean.class) : null;
        if ((null == compress && false == DOWNLOAD.equalsIgnoreCase(delivery)) || (null != compress && compress.booleanValue())) {
            transformations.compress();
        }
        /*
         * transform
         */
        final InputStream transformed = transformations.getInputStream(file.getContentType());
        if (null == transformed) {
            LOG.warn("Got no resulting input stream from transformation, trying to recover original input");
            if (markSupported) {
                try {
                    stream.reset();
                    return file;
                } catch (final Exception e) {
                    LOG.warn("Error resetting input stream", e);
                }
            }
            LOG.error("Unable to transform image from " + file);
            return file.repetitive() ? file : null;
        }
        return new FileHolder(transformed, -1, file.getContentType(), file.getName());
    }

    /**
     * Checks specified <i>Content-Disposition</i> value against passed {@link IFileHolder file}.
     * <p>
     * E.g. <code>"inline"</code> is not allowed for <code>"text/html"</code> MIME type.
     *
     * @param contentDisposition The <i>Content-Disposition</i> value to cehck
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
        if (null == in) {
            return null;
        }
        try {
            return tika.detect(in);
        } finally {
            close(in);
        }
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
     * Copy the given byte range of the given input to the given output.
     *
     * @param inputStream The input to copy the given range to the given output for.
     * @param output The output to copy the given range from the given input for.
     * @param start Start of the byte range.
     * @param length Length of the byte range.
     * @throws IOException If something fails at I/O level.
     */
    private void copy(final InputStream inputStream, final OutputStream output, final long start, final long length) throws IOException {
        // Write partial range.
        final InputStream input;
        if (!(inputStream instanceof BufferedInputStream) && !(inputStream instanceof ByteArrayInputStream)) {
            input = new BufferedInputStream(inputStream, 8192);
        } else {
            input = inputStream;
        }
        // Discard previous bytes
        for (int i = 0; i < start; i++) {
            if (input.read() < 0) {
                // Stream does not provide enough bytes
                throw new IOException("Start index " + start + " out of range. Got only " + i);
            }
            // Valid byte read... Continue.
        }
        long toRead = length;

        final byte[] buffer = new byte[BUFLEN];
        int read;
        while ((read = input.read(buffer)) > 0) {
            if ((toRead -= read) > 0) {
                output.write(buffer, 0, read);
            } else {
                output.write(buffer, 0, (int) toRead + read);
                break;
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

}
