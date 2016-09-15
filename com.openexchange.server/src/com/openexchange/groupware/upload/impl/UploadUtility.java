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

package com.openexchange.groupware.upload.impl;

import static com.openexchange.java.Strings.isEmpty;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItemIterator;
import org.apache.commons.fileupload.FileItemStream;
import org.apache.commons.fileupload.FileUploadBase;
import org.apache.commons.fileupload.FileUploadBase.FileSizeLimitExceededException;
import org.apache.commons.fileupload.FileUploadBase.FileUploadIOException;
import org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.james.mime4j.field.contenttype.parser.ContentTypeParser;
import org.slf4j.Logger;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.upload.UploadFile;
import com.openexchange.upload.UploadFileListener;
import com.openexchange.java.Streams;
import com.openexchange.java.util.UUIDs;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.osgi.ServiceListing;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;
import com.openexchange.tools.servlet.http.Tools;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;

/**
 * {@link UploadUtility} - Utility class for uploads.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class UploadUtility {

    /** The logger */
    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(UploadUtility.class);

    private static final TIntObjectMap<String> M = new TIntObjectHashMap<String>(13);

    static {
        int pos = 0;
        M.put(pos++, "");
        M.put(pos++, "Kilo");
        M.put(pos++, "Mega");
        M.put(pos++, "Giga");
        M.put(pos++, "Tera");
        M.put(pos++, "Peta");
        M.put(pos++, "Exa");
        M.put(pos++, "Zetta");
        M.put(pos++, "Yotta");
        M.put(pos++, "Xenna");
        M.put(pos++, "W-");
        M.put(pos++, "Vendeka");
        M.put(pos++, "U-");
    }

    private static final AtomicReference<ScheduledTimerTask> TIMER_TASK_REFERENCE = new AtomicReference<ScheduledTimerTask>();

    /**
     * Initializes a new {@link UploadUtility}
     */
    private UploadUtility() {
        super();
    }

    /**
     * Performs shut-down operations.
     */
    public static void shutDown() {
        ScheduledTimerTask timerTask;
        do {
            timerTask = TIMER_TASK_REFERENCE.get();
            if (null == timerTask) {
                return;
            }
        } while (!TIMER_TASK_REFERENCE.compareAndSet(timerTask, null));
        timerTask.cancel(true);
    }

    /**
     * Converts given number of bytes to a human readable format.
     *
     * @param size The number of bytes
     * @return The number of bytes in a human readable format
     */
    public static String getSize(long size) {
        return getSize(size, 2, false, true);
    }

    /**
     * Converts given number of bytes to a human readable format.
     *
     * @param size The number of bytes
     * @param precision The number of digits allowed after dot
     * @param longName <code>true</code> to use unit's long name (e.g. <code>Megabytes</code>) or short name (e.g. <code>MB</code>)
     * @param realSize <code>true</code> to bytes' real size of <code>1024</code> used for detecting proper unit; otherwise
     *            <code>false</code> to narrow unit with <code>1000</code>.
     * @return The number of bytes in a human readable format
     */
    public static String getSize(long size, int precision, boolean longName, boolean realSize) {
        int pos = 0;
        double decSize = size;
        final int base = realSize ? 1024 : 1000;
        while (decSize > base) {
            decSize /= base;
            pos++;
        }
        final int num = (int) Math.pow(10, precision);
        final StringBuilder sb = new StringBuilder(8).append(((Math.round(decSize * num)) / (double) num)).append(' ');
        if (longName) {
            sb.append(getSizePrefix(pos)).append("bytes");
        } else {
            final String prefix = getSizePrefix(pos);
            if (0 == prefix.length()) {
                sb.append("bytes");
            } else {
                sb.append(String.valueOf(prefix.charAt(0))).append('B');
            }
        }
        return sb.toString();
    }

    private static String getSizePrefix(int pos) {
        final String prefix = M.get(pos);
        if (prefix != null) {
            return prefix;
        }
        return "?-";
    }

    private static final AtomicReference<ServiceListing<UploadFileListener>> LISTENERS = new AtomicReference<>();

    /**
     * Sets the specified listing of upload file listeners
     *
     * @param listing The listing to set
     */
    public static void setUploadFileListenerLsting(ServiceListing<UploadFileListener> listing) {
        LISTENERS.set(listing);
    }

    // ----------------------------------------------- Parse/process an upload request -----------------------------------------------------

    /**
     * 1MB threshold.
     */
    private static final int SIZE_THRESHOLD = 1048576;

    /**
     * Creates a new {@code ServletFileUpload} instance using {@link ServerConfig.Property#UploadDirectory} for
     * uploaded files that exceed the threshold of 1MB.
     * <p>
     * The returned <code>ServletFileUpload</code> instance is suitable to for
     * {@link FileUploadBase#parseRequest(org.apache.commons.fileupload.RequestContext) parseRequest()} invocation.
     *
     * @param maxFileSize The maximum allowed size of a single uploaded file
     * @param maxOverallSize The maximum allowed size of a complete request
     * @return A new {@code ServletFileUpload} instance
     */
    public static ServletFileUpload newThresholdFileUploadBase(long maxFileSize, long maxOverallSize) {
        // Create the upload event
        DiskFileItemFactory factory = new DiskFileItemFactory();
        // Set factory constraints; threshold for single files
        factory.setSizeThreshold(SIZE_THRESHOLD);
        factory.setRepository(new File(ServerConfig.getProperty(Property.UploadDirectory)));
        // Create a new file upload handler
        ServletFileUpload sfu = new ServletFileUpload(factory);
        // Set the maximum allowed size of a single uploaded file
        sfu.setFileSizeMax(maxFileSize);
        // Set overall request size constraint
        sfu.setSizeMax(maxOverallSize);
        return sfu;
    }

    /**
     * Creates a new {@code ServletFileUpload} instance.
     * <p>
     * The returned <code>ServletFileUpload</code> instance is <b>only</b> suitable to for
     * {@link FileUploadBase#getItemIterator(org.apache.commons.fileupload.RequestContext) getItemIterator()} invocation.
     * <p>
     * <div style="background-color:#FFDDDD; padding:6px; margin:0px;">
     * <b>NOTE</b>:<br>
     * An attempt calling {@link FileUploadBase#parseRequest(org.apache.commons.fileupload.RequestContext) parseRequest()} with the returned
     * <code>ServletFileUpload</code> instance will throw a {@code FileUploadException}.
     * </div>
     *
     * @param maxFileSize The maximum allowed size of a single uploaded file
     * @param maxOverallSize The maximum allowed size of a complete request
     * @return A new {@code ServletFileUpload} instance
     */
    public static ServletFileUpload newFileUploadBase(long maxFileSize, long maxOverallSize) {
        // Create a new file upload handler
        ServletFileUpload sfu = new ServletFileUpload();
        // Set the maximum allowed size of a single uploaded file
        sfu.setFileSizeMax(maxFileSize);
        // Set overall request size constraint
        sfu.setSizeMax(maxOverallSize);
        return new IteratorOnlyServletFileUpload(sfu);
    }

    /**
     * (Statically) Processes specified request's upload provided that request is of content type <code>multipart/*</code>.
     *
     * @param req The request whose upload shall be processed
     * @param maxFileSize The maximum allowed size of a single uploaded file or <code>-1</code>
     * @param maxOverallSize The maximum allowed size of a complete request or <code>-1</code>
     * @return The processed instance of {@link UploadEvent}
     * @throws OXException Id processing the upload fails
     */
    public static UploadEvent processUpload(HttpServletRequest req, long maxFileSize, long maxOverallSize) throws OXException {
        if (!Tools.isMultipartContent(req)) {
            // No multipart content
            throw UploadException.UploadCode.NO_MULTIPART_CONTENT.create();
        }

        // Check action parameter existence
        String action;
        try {
            action = AJAXServlet.getAction(req);
        } catch (OXException e) {
            throw UploadException.UploadCode.UPLOAD_FAILED.create(e);
        }

        // Parse the upload request
        FileItemIterator iter;
        try {
            // Get file upload...
            // ... and add some "extra space" as Apache Fileupload considers the maximum allowed size of a complete request (incl. form fields)
            ServletFileUpload upload = newFileUploadBase(maxFileSize, maxOverallSize > 0 ? maxOverallSize + 1024 : maxOverallSize);
            // Check request's character encoding
            if (null == req.getCharacterEncoding()) {
                String defaultEnc = ServerConfig.getProperty(Property.DefaultEncoding);
                try {
                    // Might be ineffective if already fully parsed
                    req.setCharacterEncoding(defaultEnc);
                } catch (Exception e) { /* Ignore */
                }
                upload.setHeaderEncoding(defaultEnc);
            }
            // Parse multipart request
            iter = upload.getItemIterator(req);
        } catch (FileSizeLimitExceededException e) {
            throw UploadFileSizeExceededException.create(e.getActualSize(), e.getPermittedSize(), true);
        } catch (SizeLimitExceededException e) {
            throw UploadSizeExceededException.create(e.getActualSize(), e.getPermittedSize(), true);
        } catch (FileUploadException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                String message = cause.getMessage();
                if (null != message && message.startsWith("Max. byte count of ")) {
                    // E.g. Max. byte count of 10240 exceeded.
                    int pos = message.indexOf(" exceeded", 19 + 1);
                    String limit = message.substring(19, pos);
                    throw UploadException.UploadCode.MAX_UPLOAD_SIZE_EXCEEDED_UNKNOWN.create(cause, getSize(Long.parseLong(limit), 2, false, true));
                }
            } else if (cause instanceof EOFException) {
                // Stream closed/ended unexpectedly
                throw UploadException.UploadCode.UNEXPECTED_EOF.create(cause, cause.getMessage());
            }
            throw UploadException.UploadCode.UPLOAD_FAILED.create(e, null == cause ? e.getMessage() : (null == cause.getMessage() ? e.getMessage() : cause.getMessage()));
        } catch (IOException e) {
            throw UploadException.UploadCode.UPLOAD_FAILED.create(e, action);
        }

        // Get the currently available listeners
        List<UploadFileListener> listeners = Collections.emptyList();
        {
            ServiceListing<UploadFileListener> listing = LISTENERS.get();
            if (null != listing) {
                listeners = new ArrayList<>(listing.getServiceList());
            }
        }

        // Yield an ID
        String uuid = UUIDs.getUnformattedStringFromRandom();

        // Create the upload event
        UploadEvent uploadEvent = new UploadEvent();
        uploadEvent.setAction(action);
        uploadEvent.setAffiliationId(UploadEvent.MAIL_UPLOAD);

        boolean error = true;
        try {
            // Fill upload event instance
            String charEnc;
            {
                String rce = req.getCharacterEncoding();
                charEnc = null == rce ? ServerConfig.getProperty(Property.DefaultEncoding) : rce;
            }

            String uploadDir = ServerConfig.getProperty(Property.UploadDirectory);
            String fileName = req.getParameter("filename");
            long current = 0L;

            while (iter.hasNext()) {
                FileItemStream item = iter.next();
                if (item.isFormField()) {
                    uploadEvent.addFormField(item.getFieldName(), Streams.stream2string(item.openStream(), charEnc));
                } else {
                    String name = item.getName();
                    if (!isEmpty(name)) {
                        try {
                            UploadFile uf = processUploadedFile(item, uploadDir, isEmpty(fileName) ? name : fileName, current, maxFileSize, maxOverallSize, uuid, listeners);
                            current += uf.getSize();
                            uploadEvent.addUploadFile(uf);
                        } catch (OXException e) {
                            // Do not signal this OXException to listeners as it was created by one of the listeners itself
                            throw e;
                        }
                    }
                }
            }
            if (maxOverallSize > 0 && current > maxOverallSize) {
                throwException(uuid, UploadSizeExceededException.create(current, maxOverallSize, true), listeners);
            }
            if (uploadEvent.getAffiliationId() < 0) {
                throwException(uuid, UploadException.UploadCode.MISSING_AFFILIATION_ID.create(action), listeners);
            }

            // Signal success
            for (UploadFileListener listener : listeners) {
                listener.onUploadSuceeded(uuid);
            }

            // Everything went well
            error = false;

            return uploadEvent;
        } catch (FileSizeLimitExceededException e) {
            throwException(uuid, UploadFileSizeExceededException.create(e.getActualSize(), e.getPermittedSize(), true), listeners);
        } catch (SizeLimitExceededException e) {
            throwException(uuid, UploadSizeExceededException.create(e.getActualSize(), e.getPermittedSize(), true), listeners);
        } catch (FileUploadException e) {
            Throwable cause = e.getCause();
            if (cause instanceof IOException) {
                String message = cause.getMessage();
                if (null != message && message.startsWith("Max. byte count of ")) {
                    // E.g. Max. byte count of 10240 exceeded.
                    int pos = message.indexOf(" exceeded", 19 + 1);
                    String limit = message.substring(19, pos);
                    throwException(uuid, UploadException.UploadCode.MAX_UPLOAD_SIZE_EXCEEDED_UNKNOWN.create(cause, getSize(Long.parseLong(limit), 2, false, true)), listeners);
                }
            } else if (cause instanceof EOFException) {
                // Stream closed/ended unexpectedly
                throwException(uuid, UploadException.UploadCode.UNEXPECTED_EOF.create(cause, cause.getMessage()), listeners);
            }
            throwException(uuid, UploadException.UploadCode.UPLOAD_FAILED.create(e, null == cause ? e.getMessage() : (null == cause.getMessage() ? e.getMessage() : cause.getMessage())), listeners);
        } catch (FileUploadIOException e) {
            // Might wrap a size-limit-exceeded error
            Throwable cause = e.getCause();
            if (cause instanceof FileSizeLimitExceededException) {
                FileSizeLimitExceededException exc = (FileSizeLimitExceededException) cause;
                throwException(uuid, UploadFileSizeExceededException.create(exc.getActualSize(), exc.getPermittedSize(), true), listeners);
            }
            if (cause instanceof SizeLimitExceededException) {
                SizeLimitExceededException exc = (SizeLimitExceededException) cause;
                throwException(uuid, UploadSizeExceededException.create(exc.getActualSize(), exc.getPermittedSize(), true), listeners);
            }
            throwException(uuid, UploadException.UploadCode.UPLOAD_FAILED.create(e, action), listeners);
        } catch (IOException e) {
            Throwable cause = e.getCause();
            if (cause instanceof java.util.concurrent.TimeoutException) {
                throwException(uuid, UploadException.UploadCode.UNEXPECTED_TIMEOUT.create(e, new Object[0]), listeners);
            }
            throwException(uuid, UploadException.UploadCode.UPLOAD_FAILED.create(e, action), listeners);
        } catch (RuntimeException e) {
            throwException(uuid, UploadException.UploadCode.UPLOAD_FAILED.create(e, action), listeners);
        } finally {
            if (error) {
                uploadEvent.cleanUp();
            }
        }

        return null; // To keep compiler happy
    }

    private static void throwException(String uuid, OXException exception, List<UploadFileListener> listeners) throws OXException {
        for (UploadFileListener listener : listeners) {
            listener.onUploadFailed(uuid, exception);
        }
        throw exception;
    }

    private static final String PREFIX = "openexchange-upload-" + com.openexchange.exception.OXException.getServerId() + "-";

    private static UploadFile processUploadedFile(FileItemStream item, String uploadDir, String fileName, long current, long maxFileSize, long maxOverallSize, String uuid, List<UploadFileListener> listeners) throws IOException, FileUploadException, OXException {
        UploadFile retval = new UploadFileImpl();
        retval.setFieldName(item.getFieldName());
        retval.setFileName(fileName);

        // Deduce MIME type from passed file name
        String mimeType = MimeType2ExtMap.getContentType(fileName, null);

        // Set associated MIME type
        {
            // Check if we are forced to select the MIME type as signaled by file item
            String forcedMimeType = item.getHeaders().getHeader("X-Forced-MIME-Type");
            if (null == forcedMimeType) {
                retval.setContentType(null == mimeType ? item.getContentType() : mimeType);
            } else if (AJAXRequestDataTools.parseBoolParameter(forcedMimeType)) {
                retval.setContentType(item.getContentType());
            } else {
                // Valid MIME type specified?
                try {
                    ContentTypeParser parser = new ContentTypeParser(new StringReader(forcedMimeType));
                    parser.parseAll();
                    retval.setContentType(new StringBuilder(parser.getType()).append('/').append(parser.getSubType()).toString());
                } catch (Exception e) {
                    // Assume invalid value
                    retval.setContentType(null == mimeType ? item.getContentType() : mimeType);
                }
            }
        }

        // Signal basic info prior to processing
        for (UploadFileListener listener : listeners) {
            listener.onBeforeUploadProcessed(uuid, fileName, retval.getFieldName(), retval.getContentType());
        }

        // Track size
        long size = 0;

        // Check if overall size is already exceeded
        if (maxOverallSize > 0 && current > maxOverallSize) {
            // Count current bytes
            size = Streams.countInputStream(item.openStream());
            retval.setSize(size);
            return retval;
        }

        // Create temporary file
        File tmpFile = File.createTempFile(PREFIX, null, new File(uploadDir));
        tmpFile.deleteOnExit();

        // Start upload evicter (if not yet done)
        startUploadEvicterIfNotYetDone();

        // Write to temporary file
        InputStream in = null;
        OutputStream out = null;
        boolean error = true;
        try {
            in = Streams.getNonEmpty(item.openStream());
            // Check if readable...
            if (null == in) {
                // Empty file item...
                LOG.warn("Detected empty upload file {}.", retval.getFileName());
            } else {
                out = new FileOutputStream(tmpFile, false);
                int buflen = 65536;
                byte[] buf = new byte[buflen];
                for (int read; (read = in.read(buf, 0, buflen)) > 0;) {
                    out.write(buf, 0, read);
                    size += read;

                    if (maxFileSize > 0) {
                        if (size > maxFileSize) {
                            // Close resources and count remaining bytes
                            Streams.close(out);
                            tmpFile.delete();
                            size += Streams.countInputStream(in);
                            throw new FileSizeLimitExceededException("File size exceeded", size, maxFileSize);
                        }
                    }
                    if (maxOverallSize > 0) {
                        if ((current + size) > maxOverallSize) {
                            // Close resources and count remaining bytes
                            Streams.close(out);
                            tmpFile.delete();
                            size += Streams.countInputStream(in);
                            retval.setSize(size);
                            return retval;
                        }
                    }
                }
                out.flush();
            }

            // Signal success after processing
            for (UploadFileListener listener : listeners) {
                listener.onAfterUploadProcessed(uuid, retval);
            }

            error = false;
        } finally {
            Streams.close(in, out);
            if (error) {
                tmpFile.delete();
            }
        }

        // Apply temporary file and its size
        retval.setSize(size);
        retval.setTmpFile(tmpFile);
        return retval;
    }

    private static void startUploadEvicterIfNotYetDone() {
        ScheduledTimerTask timerTask = TIMER_TASK_REFERENCE.get();
        if (null == timerTask) {
            synchronized (UploadUtility.class) {
                timerTask = TIMER_TASK_REFERENCE.get();
                if (null == timerTask) {
                    try {
                        UploadEvicter evicter = new UploadEvicter(PREFIX, LOG);
                        TimerService timerService = ServerServiceRegistry.getInstance().getService(TimerService.class);
                        long delay = 300000; // 5 minutes
                        timerTask = timerService.scheduleWithFixedDelay(evicter, 3000, delay);
                        TIMER_TASK_REFERENCE.set(timerTask);
                    } catch (Exception e) {
                        LOG.warn("Failed to initialze {}", UploadEvicter.class.getSimpleName(), e);
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------------------------------------------------------------------------

    /**
     * Checks for expired upload files.
     * <p>
     * Derived from <a href="http://www.jroller.com/javabean/entry/solving_an_outofmemoryerror_java_6">Java 6 DeleteOnExitHook memory leak</a> from Cedrik LIME.
     */
    private static final class UploadEvicter implements Runnable {

        private final org.slf4j.Logger logger;
        private final String prefix;
        private final Object mutex;
        private final LinkedHashSet<String> files;
        private int filesLastSize = 0; // contains files.size() from last iteration

        /**
         * Initializes a new {@link UploadUtility.UploadEvicter}.
         *
         * @throws IllegalStateException If initialization fails
         */
        UploadEvicter(String prefix, org.slf4j.Logger logger) {
            super();
            this.logger = logger;
            this.prefix = prefix;
            try {
                Class<?> clazz = Class.forName("java.io.DeleteOnExitHook");
                Field[] declaredFields = clazz.getDeclaredFields();
                Field filesField = getFieldFrom("files", declaredFields);
                filesField.setAccessible(true);
                LinkedHashSet<String> files = (LinkedHashSet<String>) filesField.get(null);
                if (null == files) {
                    throw new IllegalStateException("Can't initialize. Are you running Java 6+ or within a restricted SecurityManager?");
                }
                this.files = files;
                mutex = isJavaVersionGreaterThan160_20() ? clazz : files;
                synchronized (mutex) {
                    filesLastSize = files.size();
                }
            } catch (IllegalStateException e) {
                throw e;
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public void run() {
            try {
                synchronized (mutex) {
                    // Remove non-existing files from DeleteOnExitHook.files
                    logger.debug("DeleteOnExitHook went from {} to {} file entries.", Integer.valueOf(filesLastSize), Integer.valueOf(files.size()));
                    int existent = 0, removed = 0;
                    int total = removed + existent;
                    for (Iterator<String> iterator = files.iterator(); iterator.hasNext() && total < filesLastSize;) {
                        String fileName = iterator.next();
                        if (fileName == null || !new File(fileName).exists()) {
                            // No file by given filename exists: remove (old), useless entry
                            logger.trace("Removing file entry {}", fileName);
                            iterator.remove();
                            ++removed;
                        } else {
                            ++existent;
                        }
                        ++total;
                    }
                    if (removed > 0) {
                        logger.debug("removed {}/{} entries", Integer.valueOf(removed), Integer.valueOf(total));
                    }
                    filesLastSize = files.size();

                    // Check orphaned files
                    if (filesLastSize > 0) {
                        long stamp = System.currentTimeMillis() - 1800000; // Older than 30 minutes
                        List<String> toBeDeleted = new ArrayList<String>(files);
                        Collections.reverse(toBeDeleted);
                        for (String filename : toBeDeleted) {
                            File file = new File(filename);
                            if (file.getName().startsWith(prefix) && file.lastModified() < stamp) {
                                logger.debug("Found expired file entry {}. Deleting...", filename);
                                if (!file.delete() && file.exists()) {
                                    logger.warn("Temporary file could not be deleted: {}", file.getPath());
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                // Ignore
            }
        }

        private Field getFieldFrom(String name, Field[] declaredFields) {
            for (Field field : declaredFields) {
                if (name.equals(field.getName())) {
                    return field;
                }
            }
            return null;
        }

        private boolean isJavaVersionGreaterThan160_20() {
            try {
                float javaClassVersion = Float.parseFloat(getSystemProperty("java.class.version"));
                // Java <= 5
                if (javaClassVersion <= 49.0f) {
                    return false;
                }
                // Java >= 7
                if (javaClassVersion > 50.0f) {
                    return true;
                }
                // This is Java 6. Get patch level version.
                // Assume JAVA_VERSION is 1.6.0_x
                String javaVersion = getJavaVersionTrimmed();
                assert javaVersion.startsWith("1.6") : javaVersion;
                String patchLevelStr = javaVersion.substring(javaVersion.indexOf('_') + 1);
                return Integer.parseInt(patchLevelStr) > 20;
            } catch (RuntimeException e) {
                // Assume "yes"
                return true;
            }
        }

        /**
         * Trims the text of the java version to start with numbers.
         *
         * @return the trimmed java version
         */
        private String getJavaVersionTrimmed() {
            String javaVersion = getSystemProperty("java.version");
            if (javaVersion != null) {
                for (int i = 0; i < javaVersion.length(); i++) {
                    char ch = javaVersion.charAt(i);
                    if (ch >= '0' && ch <= '9') {
                        return javaVersion.substring(i);
                    }
                }
            }
            return null;
        }

        /**
         * Gets a System property, defaulting to <code>null</code> if the property cannot be read.
         *
         * @param property The system property name
         * @return the system property value or <code>null</code> if a security problem occurs
         */
        private String getSystemProperty(String property) {
            try {
                return System.getProperty(property);
            } catch (SecurityException ex) {
                logger.warn("Encountered a SecurityException reading the system property '{}'; returning null instead.", property, ex);
                return null;
            }
        }
    }

}
