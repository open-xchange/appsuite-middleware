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

package com.openexchange.ajp13.coyote;

import static com.openexchange.ajp13.AJPv13Response.writeHeaderSafe;
import static com.openexchange.ajp13.AJPv13Utility.decodeUrl;
import static com.openexchange.tools.servlet.http.Cookies.extractDomainValue;
import static com.openexchange.tools.servlet.http.Cookies.getDomainValue;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URLEncoder;
import java.nio.charset.UnsupportedCharsetException;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajp13.AJPv13Config;
import com.openexchange.ajp13.AJPv13RequestHandler;
import com.openexchange.ajp13.AJPv13Response;
import com.openexchange.ajp13.AJPv13ServiceRegistry;
import com.openexchange.ajp13.AJPv13Utility;
import com.openexchange.ajp13.AjpLongRunningRegistry;
import com.openexchange.ajp13.coyote.util.ByteChunk;
import com.openexchange.ajp13.coyote.util.CookieParser;
import com.openexchange.ajp13.coyote.util.HexUtils;
import com.openexchange.ajp13.coyote.util.MessageBytes;
import com.openexchange.ajp13.exception.AJPv13Exception;
import com.openexchange.ajp13.exception.AJPv13MaxPackgeSizeException;
import com.openexchange.ajp13.servlet.http.HttpErrorServlet;
import com.openexchange.ajp13.servlet.http.HttpServletManager;
import com.openexchange.ajp13.servlet.http.HttpSessionManagement;
import com.openexchange.ajp13.watcher.AJPv13TaskMonitor;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.java.Charsets;
import com.openexchange.log.Log;
import com.openexchange.log.LogProperties;
import com.openexchange.log.Props;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;
import com.openexchange.tools.exceptions.ExceptionUtils;
import com.openexchange.tools.servlet.UploadServletException;
import com.openexchange.tools.servlet.http.Cookies;
import com.openexchange.tools.stream.UnsynchronizedByteArrayOutputStream;

/**
 * {@link AjpProcessor} - The AJP processor adapted from Tomcat's Coyote AJP connector.
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class AjpProcessor implements com.openexchange.ajp13.watcher.Task {

    protected static final org.apache.commons.logging.Log LOG = Log.loggerFor(AjpProcessor.class);

    private static final boolean TRACE = LOG.isTraceEnabled();

    protected static final boolean DEBUG = LOG.isDebugEnabled();

    private static final AtomicLong NUMBER = new AtomicLong();

    private static final String HTTPS = "https";

    private static enum Stage {
        STAGE_AWAIT, STAGE_PREPARE, STAGE_SERVICE, STAGE_SERVICE_ENDED, STAGE_ENDED;
    }

    /**
     * The current processor stage.
     */
    private volatile Stage stage;

    /**
     * Required secret.
     */
    private String requiredSecret;

    /**
     * Associated servlet.
     */
    private HttpServlet servlet = null;

    /**
     * Request object.
     */
    protected final HttpServletRequestImpl request;

    /**
     * Response object.
     */
    protected final HttpServletResponseImpl response;

    /**
     * The request input buffer.
     */
    private volatile SocketInputBuffer inputBuffer;

    /**
     * The response output buffer.
     */
    private final SocketOutputBuffer outputBuffer;

    /**
     * The socket timeout used when reading the first block of the request header.
     */
    protected final int packetSize;

    /**
     * Header message. Note that this header is merely the one used during the processing of the first message of a "request", so it might
     * not be a request header. It will stay unchanged during the processing of the whole request.
     */
    private final AjpMessage requestHeaderMessage;

    /**
     * Message used for response header composition.
     */
    //protected final AjpMessage responseHeaderMessage;

    /**
     * Body message.
     */
    private final AjpMessage bodyMessage;

    /**
     * Body message.
     */
    protected final MessageBytes bodyBytes;

    /**
     * State flag.
     */
    private volatile boolean started = false;

    /**
     * Error flag.
     */
    protected volatile boolean error = false;

    /**
     * Socket associated with the current connection.
     */
    private Socket socket;

    /**
     * Input stream.
     */
    private InputStream input;

    /**
     * Output stream.
     */
    protected OutputStream output;

    /**
     * The number of milliseconds waiting for a subsequent request before closing the connection.
     */
    private int keepAliveTimeout = -1;

    /**
     * The main read-write-lock.
     * <p>
     * A writer can acquire the read lock, but not vice-versa.
     */
    private final ReadWriteLock mainLock;

    /**
     * The soft lock for non-blocking access to output/input stream.
     * <p>
     * A writer can acquire this read lock.
     */
    protected final Lock softLock;

    /**
     * Use Tomcat authentication ?
     */
    private boolean tomcatAuthentication = true;

    /**
     * The socket timeout used when reading the first block of the request header.
     */
    private long readTimeout;

    /**
     * The byte sink used for processing.
     */
    protected final ByteArrayOutputStream sink;

    /**
     * Byte chunk for certs.
     */
    private final MessageBytes certificates;

    /**
     * End of stream flag.
     */
    protected boolean endOfStream = false;

    /**
     * Body empty flag.
     */
    protected volatile boolean empty = true;

    /**
     * First read.
     */
    protected volatile boolean first = true;

    /**
     * Replay read.
     */
    private volatile boolean replay = false;

    /**
     * Finished response.
     */
    private volatile boolean finished = false;

    /**
     * The HTTP session (JSESSIONID) cookie.
     */
    private Cookie httpSessionCookie;

    /**
     * Whether client joined an existing HTTP session.
     */
    private boolean httpSessionJoined;

    /**
     * The identifier of the currently associated servlet.
     */
    private final StringBuilder servletId;

    /**
     * Control for AJP processor.
     */
    private volatile Future<Object> control;

    /**
     * The thread currently processing.
     */
    private volatile Thread thread;

    /**
     * The scheduled keep-alive task.
     */
    private volatile ScheduledTimerTask scheduledKeepAliveTask;

    /**
     * The servlet path (which is not the request path). The servlet path is defined in servlet mapping configuration.
     */
    private String servletPath;

    /**
     * Gets this processor's number.
     */
    private final Long number;

    /**
     * The last write access.
     */
    protected volatile long lastWriteAccess;

    /**
     * Direct buffer used for sending right away a get body message.
     */
    private final byte[] getBodyMessageArray;

    /**
     * The listener monitor.
     */
    private final AJPv13TaskMonitor listenerMonitor;

    /**
     * The ping counter.
     */
    private volatile int pingCount;

    /**
     * Whether e secure connection is enforced.
     */
    private final boolean forceHttps;

    /**
     * Flag whether to restrict number of long-running request. <code>false</code> by default.
     */
    private boolean restrictLongRunning;

    /**
     * Direct buffer used for sending right away a pong message.
     */
    private static final byte[] pongMessageArray;

    /**
     * End message array.
     */
    private static final byte[] endMessageArray;

    /**
     * End message array with "reuse" flag set to <code>false</code>.
     */
    private static final byte[] endMessageArrayNoReuse;

    /**
     * Flush message array.
     */
    private static final byte[] flushMessageArray;

    // ----------------------------------------------------- Static Initializer

    static {

        // Set the read body message buffer
        final AjpMessage pongMessage = new AjpMessage(16);
        pongMessage.reset();
        pongMessage.appendByte(Constants.JK_AJP13_CPONG_REPLY);
        pongMessage.end();
        pongMessageArray = new byte[pongMessage.getLen()];
        System.arraycopy(pongMessage.getBuffer(), 0, pongMessageArray, 0, pongMessage.getLen());

        // Allocate the end message array
        final AjpMessage endMessage = new AjpMessage(16);
        endMessage.reset();
        endMessage.appendByte(Constants.JK_AJP13_END_RESPONSE);
        endMessage.appendByte(1);
        endMessage.end();
        endMessageArray = new byte[endMessage.getLen()];
        System.arraycopy(endMessage.getBuffer(), 0, endMessageArray, 0, endMessage.getLen());

        // Allocate the end message array with reuse flag not set
        final AjpMessage endMessageNoReuse = new AjpMessage(16);
        endMessageNoReuse.reset();
        endMessageNoReuse.appendByte(Constants.JK_AJP13_END_RESPONSE);
        endMessageNoReuse.appendByte(0); // No "reuse" flag
        endMessageNoReuse.end();
        endMessageArrayNoReuse = new byte[endMessageNoReuse.getLen()];
        System.arraycopy(endMessageNoReuse.getBuffer(), 0, endMessageArrayNoReuse, 0, endMessageNoReuse.getLen());

        // Allocate the flush message array
        final AjpMessage flushMessage = new AjpMessage(16);
        flushMessage.reset();
        flushMessage.appendByte(Constants.JK_AJP13_SEND_BODY_CHUNK);
        flushMessage.appendInt(0);
        flushMessage.appendByte(0);
        flushMessage.end();
        flushMessageArray = new byte[flushMessage.getLen()];
        System.arraycopy(flushMessage.getBuffer(), 0, flushMessageArray, 0, flushMessage.getLen());

    }

    /**
     * Initializes a new {@link AjpProcessor}.
     *
     * @param packetSize The packet size
     * @param listenerMonitor The listener monitor
     * @param forceHttps Whether HTTPS is enforced
     */
    public AjpProcessor(final int packetSize, final AJPv13TaskMonitor listenerMonitor, final boolean forceHttps) {
        super();
        restrictLongRunning = false;
        this.forceHttps = forceHttps;
        bodyBytes = MessageBytes.newInstance();
        sink = new UnsynchronizedByteArrayOutputStream(Constants.MAX_PACKET_SIZE);
        certificates = MessageBytes.newInstance();
        mainLock = new ReentrantReadWriteLock();
        softLock = mainLock.readLock();
        lastWriteAccess = 0L;
        this.listenerMonitor = listenerMonitor;
        this.number = Long.valueOf(NUMBER.incrementAndGet());
        servletId = new StringBuilder(16);
        /*
         * Create request/response
         */
        response = new HttpServletResponseImpl(this);
        request = new HttpServletRequestImpl(response);
        /*
         * Apply input/output
         */
        this.packetSize = packetSize;
        final SocketInputBuffer inputBuffer = new SocketInputBuffer();
        this.inputBuffer = inputBuffer;
        request.setInputBuffer(inputBuffer);
        outputBuffer = new SocketOutputBuffer();
        response.setOutputBuffer(outputBuffer);
        /*
         * Initialize rest
         */
        requestHeaderMessage = new AjpMessage(packetSize);
        //responseHeaderMessage = new AjpMessage(packetSize);
        bodyMessage = new AjpMessage(packetSize);
        /*
         * Set the get body message buffer
         */
        final AjpMessage getBodyMessage = new AjpMessage(16);
        getBodyMessage.reset();
        getBodyMessage.appendByte(Constants.JK_AJP13_GET_BODY_CHUNK);
        /*
         * Adjust allowed size if packetSize != default (Constants.MAX_PACKET_SIZE)
         */
        getBodyMessage.appendInt(Constants.MAX_READ_SIZE + packetSize - Constants.MAX_PACKET_SIZE);
        getBodyMessage.end();
        getBodyMessageArray = new byte[getBodyMessage.getLen()];
        System.arraycopy(getBodyMessage.getBuffer(), 0, getBodyMessageArray, 0, getBodyMessage.getLen());
        /*
         * Cause loading of HexUtils
         */
        final int foo = HexUtils.DEC[0];
        if (TRACE) {
            LOG.trace(Integer.valueOf(foo));
        }
    }

    /**
     * Sets the flag whether to restrict long-running requests.
     *
     * @param restrictLongRunning <code>true</code> to restrict long-running requests; else <code>false</code>
     */
    public AjpProcessor setRestrictLongRunning(final boolean restrictLongRunning) {
        this.restrictLongRunning = restrictLongRunning;
        return this;
    }

    public void startKeepAlivePing() {
        final TimerService timer = AJPv13ServiceRegistry.getInstance().getService(TimerService.class);
        if (null == timer) {
            throw new IllegalStateException("Missing timer service!");
        }
        final int max = AJPv13Config.getKeepAliveTime();
        scheduledKeepAliveTask =
            timer.scheduleWithFixedDelay(new KeepAliveRunnable(this, max), max, max, TimeUnit.MILLISECONDS);
    }

    public void stopKeepAlivePing() {
        final ScheduledTimerTask scheduledKeepAliveTask = this.scheduledKeepAliveTask;
        if (null != scheduledKeepAliveTask) {
            scheduledKeepAliveTask.cancel(false);
            this.scheduledKeepAliveTask = null;
            /*
             * Task is automatically purged from TimerService by PurgeRunnable
             */
        }
    }

    // ------------------------------------------------------------- Properties

    @Override
    public boolean isWaitingOnAJPSocket() {
        return Stage.STAGE_AWAIT == stage;
    }

    @Override
    public boolean isProcessing() {
        return Stage.STAGE_SERVICE == stage;
    }

    @Override
    public long getProcessingStartTime() {
        return request.getStartTime();
    }

    @Override
    public boolean isLongRunning() {
        return isEASPingCommand();
    }

    @Override
    public StackTraceElement[] getStackTrace() {
        return thread.getStackTrace();
    }

    @Override
    public Thread getThread() {
        return thread;
    }

    @Override
    public String getThreadName() {
        return thread.getName();
    }

    @Override
    public Long getNum() {
        return number;
    }

    @Override
    public long getLastWriteAccess() {
        return lastWriteAccess;
    }

    public boolean getTomcatAuthentication() {
        return tomcatAuthentication;
    }

    public void setTomcatAuthentication(final boolean tomcatAuthentication) {
        this.tomcatAuthentication = tomcatAuthentication;
    }

    /**
     * Sets the required secret.
     *
     * @param requiredSecret The secret
     */
    public void setRequiredSecret(final String requiredSecret) {
        this.requiredSecret = requiredSecret;
    }

    public int getKeepAliveTimeout() {
        return keepAliveTimeout;
    }

    public void setKeepAliveTimeout(final int timeout) {
        keepAliveTimeout = timeout;
    }

    /**
     * Sets the control object.
     *
     * @param control The control
     */
    public void setControl(final Future<Object> control) {
        this.control = control;
    }

    /**
     * Checks if this AJP processor was canceled before it completed normally.
     *
     * @return <code>true</code> if this AJP processor was canceled before it completed normally; otherwise <code>false</code>
     */
    public boolean isCancelled() {
        return control.isCancelled();
    }

    /**
     * Checks if this AJP processor completed. Completion may be due to normal termination, an exception, or cancellation -- in all of these
     * cases, this method will return <code>true</code>.
     *
     * @return <code>true</code> if this AJP processor completed; otherwise <code>false</code>
     */
    public boolean isDone() {
        return control.isDone();
    }

    /**
     * Cancels this AJP processor; meaning to close the client socket and to stop its execution.
     */
    @Override
    public void cancel() {
        /*
         * Is a response expected?
         */
        if (Stage.STAGE_AWAIT != stage) {
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Request elapsed, try again.");
            action(ActionCode.CLIENT_FLUSH, null);
            action(ActionCode.CLOSE, Boolean.FALSE);
            action(ActionCode.STOP, null);
        }
        /*
         * Drop socket
         */
        final Socket s = socket;
        if (null != s) {
            try {
                closeQuitely(s);
            } finally {
                socket = null;
            }
        }
        /*
         * Cancel via control, too
         */
        final Future<Object> f = control;
        if (f != null) {
            f.cancel(true);
            control = null;
        }
    }

    private static void closeQuitely(final Socket s) {
        try {
            s.close();
        } catch (final Exception e) {
            if (DEBUG) {
                LOG.debug("Socket could not be closed. Probably due to a broken socket connection (e.g. broken pipe).", e);
            }
        }
    }

    // --------------------------------------------------------- Public Methods

    /**
     * Get the request associated with this processor.
     *
     * @return The request
     */
    public HttpServletRequestImpl getRequest() {
        return request;
    }

    /**
     * Get the response associated with this processor.
     *
     * @return The response
     */
    public HttpServletResponseImpl getResponse() {
        return response;
    }

    /**
     * Gets the HTTP session cookie.
     *
     * @return The HTTP session cookie
     */
    public Cookie getHttpSessionCookie() {
        return httpSessionCookie;
    }

    /**
     * Checks if the client joined a previously existing HTTP session.
     *
     * @return <code>true</code> if the client joined a previously existing HTTP session; otherwise <code>false</code>
     */
    public boolean isHttpSessionJoined() {
        return httpSessionJoined;
    }

    /**
     * Process pipelined HTTP requests using the specified input and output streams.
     *
     * @throws IOException If an error occurs during an I/O operation
     */
    public void process(final Socket socket) throws IOException {
        stage = Stage.STAGE_AWAIT;
        final long st = System.currentTimeMillis();
        // Setting up the socket
        this.socket = socket;
        input = socket.getInputStream();
        output = socket.getOutputStream();
        int soTimeout = -1;
        if (keepAliveTimeout > 0) {
            soTimeout = socket.getSoTimeout();
        }
        /*
         * Error flag
         */
        error = false;
        final Thread thread = this.thread = Thread.currentThread();
        final boolean logPropsEnabled = LogProperties.isEnabled();
        while (started && !error && !thread.isInterrupted()) {
            /*
             * Parsing the request header
             */
            if (logPropsEnabled) {
                /*
                 * Gather logging info
                 */
                final Props properties = LogProperties.getLogProperties();
                properties.put(LogProperties.Name.AJP_THREAD_NAME, thread.getName());
                properties.put(LogProperties.Name.AJP_REMOTE_PORT, Integer.valueOf(socket.getPort()));
                properties.put(LogProperties.Name.AJP_REMOTE_ADDRESS, socket.getInetAddress().getHostAddress());
            }
            final boolean debug = LOG.isDebugEnabled();
            try {
                stage = Stage.STAGE_AWAIT;
                listenerMonitor.incrementNumWaiting();
                /*
                 * Set keep alive timeout if enabled
                 */
                if (keepAliveTimeout > 0) {
                    socket.setSoTimeout(keepAliveTimeout);
                }
                /*
                 * Get first message of the request
                 */
                if (!readMessage(requestHeaderMessage)) {
                    /*
                     * This means a connection timeout
                     */
                    stage = Stage.STAGE_ENDED;
                    break;
                }
                /*
                 * Set back timeout if keep alive timeout is enabled
                 */
                if (keepAliveTimeout > 0) {
                    socket.setSoTimeout(soTimeout);
                }
                /*-
                 * Check message type, process right away and break if
                 * not regular request processing
                 */
                final int type = requestHeaderMessage.getByte();
                if (debug) {
                    final String ajpReqName =
                        Constants.JK_AJP13_CPING_REQUEST == type ? "CPing" : (Constants.JK_AJP13_FORWARD_REQUEST == type ? "Forward-Request" : "unknown");
                    LOG.debug("First " + ajpReqName + " AJP message successfully read from stream.");
                }
                if (type == Constants.JK_AJP13_CPING_REQUEST && 1 == requestHeaderMessage.getLen()) {
                    softLock.lock();
                    try {
                        output.write(pongMessageArray);
                        lastWriteAccess = System.currentTimeMillis();
                        // output.flush();
                    } catch (final IOException e) {
                        error = true;
                    } finally {
                        softLock.unlock();
                    }
                    continue;
                } else if (type != Constants.JK_AJP13_FORWARD_REQUEST) {
                    /*-
                     * Invalid/unknown prefix code
                     *
                     * Usually the servlet didn't read the previous request body
                     */
                    if (debug) {
                        LOG.debug("Unexpected message: " + type);
                    }
                    continue;
                }
                /*
                 * So far a valid forward-request package
                 */
                request.setStartTime(System.currentTimeMillis());
            } catch (final InterruptedIOException e) {
                LOG.debug("ajpprocessor.io.read-timeout", e);
                error = true;
                break;
            } catch (final IOException e) {
                LOG.debug("ajpprocessor.io.error", e);
                error = true;
                break;
            } catch (final Throwable t) {
                // 400 - Bad Request
                if (debug) {
                    LOG.warn("400 - Bad Request: Parsing forward-request failed", t);
                } else {
                    LOG.warn("400 - Bad Request: Parsing forward-request failed");
                }
                response.setStatus(400);
                error = true;
            } finally {
                listenerMonitor.decrementNumWaiting();
            }
            if (!error) {
                /*
                 * Setting up filters, and parse some request headers
                 */
                stage = Stage.STAGE_PREPARE;
                try {
                    /*
                     * Parse AJP FORWARD-REQUEST package
                     */
                    prepareRequest();
                } catch (final IndexOutOfBoundsException indexException) {
                    /*-
                     * Parsing of forward-request failed
                     *
                     * Usually the servlet didn't read the previous request body
                     */
                    if (debug) {
                        requestHeaderMessage.dump("Invalid forward-request detected");
                    }
                    continue;
                } catch (final IllegalStateException stateException) {
                    /*-
                     * Max. number of HTTP sessions reached.
                     *
                     * Status code (503) indicating that the HTTP server is
                     * temporarily overloaded, and unable to handle the request.
                     */
                    response.setStatus(503, "HTTP server is temporarily overloaded. Try again later.");
                    error = true;
                } catch (final NullPointerException npe) {
                    LOG.error("Null dereference occurred.", npe);
                    response.setStatus(400);
                    error = true;
                } catch (final Throwable t) {
                    // 400 - Bad Request
                    {
                        final com.openexchange.java.StringAllocator sb = new com.openexchange.java.StringAllocator(512);
                        sb.append("400 - Bad Request: Error preparing forward-request: ").append(t.getClass().getName());
                        sb.append(" message=").append(t.getMessage());
                        if (Log.appendTraceToMessage()) {
                            final String lineSeparator = System.getProperty("line.separator");
                            sb.append(lineSeparator);
                            appendStackTrace(t.getStackTrace(), sb, lineSeparator);
                            LOG.warn(sb.toString());
                        } else {
                            LOG.warn(sb.toString(), t);
                        }
                    }
                    response.setStatus(400);
                    error = true;
                }
            }
            /*
             * Process the request in the servlet
             */
            boolean longRunningAccepted = false;
            if (!error) {
                try {
                    /*
                     * Enter service stage...
                     */
                    lastWriteAccess = System.currentTimeMillis();
                    stage = Stage.STAGE_SERVICE;
                    listenerMonitor.incrementNumProcessing();
                    /*
                     * Form data?
                     */
                    if (request.isFormData()) {
                        /*
                         * Read all form data from servlet input stream...
                         */
                        final int buflen = 2048;
                        final byte[] buf = new byte[buflen];
                        sink.reset();
                        final ServletInputStream inputStream = request.getInputStream();
                        for (int read; (read = inputStream.read(buf, 0, buflen)) > 0;) {
                            sink.write(buf, 0, read);
                        }
                        String charEnc = request.getCharacterEncoding();
                        if (charEnc == null) {
                            charEnc = ServerConfig.getProperty(ServerConfig.Property.DefaultEncoding);
                            if (charEnc == null) {
                                charEnc = "ISO-8859-1";
                            }
                        }
                        final byte[] bytes = sink.toByteArray();
                        try {
                            parseQueryString(new String(bytes, Charsets.forName(charEnc)));
                        } catch (final UnsupportedCharsetException e) {
                            parseQueryString(new String(bytes, Charsets.ISO_8859_1));
                        }
                        /*
                         * Apply already read data to request to make them re-available.
                         */
                        request.dumpToBuffer(bytes);
                    }
                    if (restrictLongRunning && isLongRunning() && !(longRunningAccepted = AjpLongRunningRegistry.getInstance().registerLongRunning(request))) {
                        /*
                         * Only one per host/port!
                         */
                        response.setStatus(503, "Only one long-running request is permitted at once. Please retry later.");
                        error = true;
                    } else {
                       /*
                        * Call Servlet's service() method
                        */
                        servlet.service(request, response);
                        if (!started) {
                            // Stopped in the meantime
                            return;
                        }
                        response.flushBuffer();
                        listenerMonitor.addProcessingTime(System.currentTimeMillis() - request.getStartTime());
                        listenerMonitor.incrementNumRequests();
                    }
                } catch (final UploadServletException e) {
                    /*
                     * Log ServletException's own root cause separately
                     */
                    final Throwable rootCause = e.getRootCause();
                    if (null != rootCause) {
                        LOG.error(rootCause.getMessage(), rootCause);
                    }
                    /*
                     * Now log actual UploadServletException...
                     */
                    LOG.error(e.getMessage(), e);
                    /*
                     * ... and write bytes
                     */
                    final byte[] bytes = e.getData().getBytes("UTF-8");
                    final ByteChunk byteChunk = new ByteChunk(packetSize);
                    byteChunk.append(bytes, 0, bytes.length);
                    outputBuffer.doWrite(byteChunk);
                } catch (final InterruptedIOException e) {
                    error = true;
                } catch (final IOException e) {
                    // Ignore
                } catch (final Throwable t) {
                    ExceptionUtils.handleThrowable(t);
                    final com.openexchange.java.StringAllocator tmp = new com.openexchange.java.StringAllocator(128).append("Error processing request: ");
                    appendRequestInfo(tmp);
                    LOG.error(tmp.toString(), t);
                    // 500 - Internal Server Error
                    response.setStatus(500);
                    error = true;
                } finally {
                    stage = Stage.STAGE_SERVICE_ENDED;
                    listenerMonitor.decrementNumProcessing();
                    if (longRunningAccepted) {
                        AjpLongRunningRegistry.getInstance().deregisterLongRunning(request);
                    }
                    /*
                     * Drop logging info
                     */
                    if (logPropsEnabled) {
                        LogProperties.removeLogProperties();
                    }
                }
            }
            /*
             * Finish the response if not done yet
             */
            if (!finished) {
                try {
                    finish();
                } catch (final IOException e) {
                    // Lost connection
                    if (debug) {
                        LOG.debug("Lost connection to web server.", e);
                    }
                    error = true;
                } catch (final Throwable t) {
                    ExceptionUtils.handleThrowable(t);
                    final com.openexchange.java.StringAllocator tmp = new com.openexchange.java.StringAllocator(128).append("Error processing request: ");
                    appendRequestInfo(tmp);
                    LOG.error(tmp.toString(), t);
                    error = true;
                }
            }
            /*
             * If there was an error, make sure the request is counted as an error, and update the statistics counter
             */
            if (error) {
                response.setStatus(500);
            }
            stage = Stage.STAGE_AWAIT;
            recycle();
        }
        /*
         * Terminate AJP connection
         */
        stage = Stage.STAGE_ENDED;
        recycle();
        input = null;
        output = null;
        this.thread = null;
        this.socket = null;
        final long duration = System.currentTimeMillis() - st;
        listenerMonitor.addUseTime(duration);
        /*
         * Drop logging info
         */
        if (logPropsEnabled) {
            LogProperties.removeLogProperties();
        }
    }

    /**
     * Appends request information.
     *
     * @param builder The builder to append to
     */
    protected void appendRequestInfo(final com.openexchange.java.StringAllocator builder) {
        builder.append("request-URI=''");
        builder.append(request.getRequestURI());
        builder.append("'', query-string=''");
        builder.append(request.getQueryString());
        builder.append("''");
    }

    // ----------------------------------------------------- ActionHook Methods

    /**
     * Send an action to the connector.
     *
     * @param actionCode The action type
     * @param param The action parameter
     */
    public void action(final ActionCode actionCode, final Object param) {
        switch (actionCode) {
        case COMMIT:
            {
                if (response.isCommitted()) {
                    return;
                }

                // Validate and write response headers
                try {
                    prepareResponse();
                } catch (final IOException e) {
                    // Set error flag
                    error = true;
                }
            }
            break;
        case CLIENT_FLUSH:
            {
                if (!response.isCommitted()) {
                    // Validate and write response headers
                    try {
                        prepareResponse();
                    } catch (final IOException e) {
                        // Set error flag
                        error = true;
                        return;
                    }
                }
                softLock.lock();
                try {
                    /*
                     * Write empty SEND-BODY-CHUNK package
                     */
                    output.write(flushMessageArray);
                    lastWriteAccess = System.currentTimeMillis();
                    // output.flush();
                } catch (final IOException e) {
                    // Set error flag
                    error = true;
                } finally {
                    softLock.unlock();
                }
            }
            break;
        case CLIENT_PING:
            {
                final Lock hardLock = mainLock.writeLock();
                hardLock.lock();
                try {
                    if (response.isCommitted()) {
                        /*
                         * Write empty SEND-BODY-CHUNK package
                         */
                        output.write(flushMessageArray);
                        lastWriteAccess = System.currentTimeMillis();
                        // output.flush();
                        if (DEBUG) {
                            LOG.debug("Performed keep-alive through a flush package.");
                        }
                    } else {
                        /*
                         * Not committed, yet. Write an empty GET-BODY-CHUNK package.
                         */
                        output.write(AJPv13Response.getGetBodyChunkBytes(0));
                        output.flush();
                        lastWriteAccess = System.currentTimeMillis();
                        /*
                         * Receive probably empty body chunk
                         */
                        final ByteChunk byteChunk = new ByteChunk(8192);
                        final int read = inputBuffer.doRead(byteChunk, request);
                        if (read > 0) {
                            // Received a non-empty data chunk...
                            // Dump that chunk to ServletInputStream
                            int len = byteChunk.getLength();
                            if (len > read) {
                                len = read;
                            }
                            final byte[] chunk = new byte[len];
                            System.arraycopy(byteChunk.getBuffer(), byteChunk.getStart(), chunk, 0, len);
                            request.appendToBuffer(chunk);
                        }
                        if (DEBUG) {
                            LOG.debug("Performed keep-alive through an empty get-body-chunk package (and received requested chunk).");
                        }
                    }
                } catch (final IOException e) {
                    // Set error flag
                    error = true;
                } catch (final AJPv13Exception e) {
                    // Cannot occur
                } finally {
                    hardLock.unlock();
                }
            }
            break;
        case CLOSE:
            {
                // Close

                // End the processing of the current request, and stop any further
                // transactions with the client
                try {
                    /*
                     * Write END-RESPONSE package
                     */
                    final Boolean reuseFlag = (param instanceof Boolean) ? ((Boolean) param) : Boolean.TRUE;
                    finish(reuseFlag.booleanValue());
                } catch (final IOException e) {
                    // Set error flag
                    error = true;
                }
            }
            break;
        case START:
            started = true;
            break;
        case STOP:
            started = false;
            break;
        case REQ_HOST_ATTRIBUTE:
            {
                // Get remote host name using a DNS resolution
                if (request.getRemoteHost() == null) {
                    try {
                        request.setRemoteHost(InetAddress.getByName(request.getRemoteAddr().toString()).getHostName());
                    } catch (final IOException iex) {
                        // Ignore
                    }
                }
            }
            break;
        case REQ_LOCAL_ADDR_ATTRIBUTE:
            {
                // Copy from local name for now, which should simply be an address
                request.setLocalAddr(request.getLocalName().toString());
            }
            break;
        case REQ_SET_BODY_REPLAY:
            {
                // Set the given bytes as the content
                final ByteChunk bc = (ByteChunk) param;
                final int length = bc.getLength();
                bodyBytes.setBytes(bc.getBytes(), bc.getStart(), length);
                request.setContentLength(length);
                first = false;
                empty = false;
                replay = true;
            }
            break;
        default:
            break;
        }
    }

    /**
     * Get the associated servlet.
     *
     * @return the associated servlet
     */
    public HttpServlet getServlet() {
        return servlet;
    }

    private static final String JSESSIONID_URI = AJPv13RequestHandler.JSESSIONID_URI;

    private static final String EAS_URI = "/Microsoft-Server-ActiveSync";

    private static final String EAS_CMD = "Cmd";

    private static final String EAS_PING = "Ping";

    /**
     * Checks for long-running EAS ping command, that is URI is equal to <code>"/Microsoft-Server-ActiveSync"</code> and request's
     * <code>"Cmd"</code> parameter equals <code>"Ping"</code>.
     *
     * @return <code>true</code> if EAS ping command is detected; otherwise <code>false</code>
     */
    private boolean isEASPingCommand() {
        return EAS_URI.equals(request.getRequestURI()) && EAS_PING.equals(request.getParameter(EAS_CMD));
    }

    /**
     * After reading the request headers, we have to setup the request filters.
     *
     * @throws IndexOutOfBoundsException If parsing of forward-request fails; usually because servlet missed to read request-body chunk(s)
     */
    protected void prepareRequest() {
        // Translate the HTTP method code to a String.
        final byte methodCode = requestHeaderMessage.getByte();
        if (methodCode != Constants.SC_M_JK_STORED) {
            final String methodName = Constants.methodTransArray[methodCode - 1];
            request.setMethod(methodName);
        }
        final StringBuilder temp = new StringBuilder(16);
        request.setProtocol(requestHeaderMessage.getString(temp));
        String jsessionID = null;
        {
            final String requestURI = requestHeaderMessage.getString(temp);
            final int pos = requestURI.toLowerCase(Locale.ENGLISH).indexOf(JSESSIONID_URI);
            if (pos > -1) {
                jsessionID = requestURI.substring(pos + JSESSIONID_URI.length());
                request.setRequestedSessionIdFromURL(true);
                request.setRequestedSessionIdFromCookie(false);
            }
            request.setRequestURI(requestURI);
        }
        request.setRemoteAddr(requestHeaderMessage.getString(temp));
        request.setRemoteHost(requestHeaderMessage.getString(temp));
        {
            final String serverName = requestHeaderMessage.getString(temp);
            request.setLocalName(serverName);
            request.setServerName(serverName);
        }
        {
            final int serverPort = requestHeaderMessage.getInt();
            request.setLocalPort(serverPort);
            request.setServerPort(serverPort);
        }

        if (requestHeaderMessage.getByte() != 0) {
            request.setSecure(true);
            request.setScheme(HTTPS);
        }

        // Decode headers
        {
            final int hCount = requestHeaderMessage.getInt();
            for (int i = 0; i < hCount; i++) {
                final String hName;
                // Header names are encoded as either an integer code starting
                // with 0xA0, or as a normal string (in which case the first
                // two bytes are the length).
                int isc = requestHeaderMessage.peekInt();
                int hId = isc & 0xFF;
                isc &= 0xFF00;
                if (0xA000 == isc) {
                    requestHeaderMessage.getInt(); // To advance the read position
                    hName = Constants.headerTransArray[hId - 1];
                } else {
                    // reset hId -- if the header currently being read
                    // happens to be 7 or 8 bytes long, the code below
                    // will think it's the content-type header or the
                    // content-length header - SC_REQ_CONTENT_TYPE=7,
                    // SC_REQ_CONTENT_LENGTH=8 - leading to unexpected
                    // behaviour. see bug 5861 for more information.
                    hId = -1;
                    hName = requestHeaderMessage.getString(temp);
                }
                final String hValue = requestHeaderMessage.getString(temp);
                /*
                 * Check for "Content-Length" and "Content-Type" headers
                 */
                if (hId == Constants.SC_REQ_CONTENT_LENGTH || (hId == -1 && hName.equalsIgnoreCase("Content-Length"))) {
                    /*
                     * Read the content-length header, so set it
                     */
                    final long cl = Long.parseLong(hValue);
                    if (cl < Integer.MAX_VALUE) {
                        request.setContentLength(cl);
                    }
                } else if (hId == Constants.SC_REQ_CONTENT_TYPE || (hId == -1 && hName.equalsIgnoreCase("Content-Type"))) {
                    /*
                     * Read the content-type header, so set it
                     */
                    try {
                        request.setContentType(hValue);
                    } catch (final AJPv13Exception e) {
                        response.setStatus(403);
                        error = true;
                    }
                } else if (hId == Constants.SC_REQ_COOKIE || (hId == -1 && hName.equalsIgnoreCase("Cookie"))) {
                    /*
                     * Read a cookie, so set it
                     */
                    try {
                        request.setCookies(CookieParser.parseCookieHeader(hValue));
                    } catch (final AJPv13Exception e) {
                        response.setStatus(403);
                        error = true;
                    }
                } else {
                    try {
                        request.setHeader(hName, hValue, false);
                    } catch (final AJPv13Exception e) {
                        // Cannot occur
                    }
                }
            }
        }
        if (LogProperties.isEnabled()) {
            /*
             * Gather logging info
             */
            final String echoHeaderName = AJPv13Response.getEchoHeaderName();
            if (null != echoHeaderName) {
                final String echoValue = request.getHeader(echoHeaderName);
                if (null != echoValue) {
                    LogProperties.putLogProperty(LogProperties.Name.AJP_REQUEST_ID, echoValue);
                }
            }
        }
        /*
         * Decode extra attributes
         */
        boolean secret = false;
        for (byte attributeCode = requestHeaderMessage.getByte(); attributeCode != Constants.SC_A_ARE_DONE; attributeCode =
            requestHeaderMessage.getByte()) {
            switch (attributeCode) {
            case Constants.SC_A_REQ_ATTRIBUTE: {
                final String n = requestHeaderMessage.getString(temp);
                final String v = requestHeaderMessage.getString(temp);
                /*
                 * AJP13 misses to forward the remotePort. Allow the AJP connector to add this info via a private request attribute. We will
                 * accept the forwarded data as the remote port, and remove it from the public list of request attributes.
                 */
                if (n.equals(Constants.SC_A_REQ_REMOTE_PORT)) {
                    try {
                        request.setRemotePort(Integer.parseInt(v));
                    } catch (final NumberFormatException nfe) {
                        // Ignore
                    }
                } else {
                    request.setAttribute(n, v);
                }
            }
                break;
            case Constants.SC_A_CONTEXT: {
                final String context = requestHeaderMessage.getString(temp);
                request.setAttribute(Constants.attributeNameArray[attributeCode - 1], context);
                request.setContextPath(context);
            }
                break;
            case Constants.SC_A_SERVLET_PATH: {
                final String servletPath = requestHeaderMessage.getString(temp);
                request.setAttribute(Constants.attributeNameArray[attributeCode - 1], servletPath);
                request.setServletPath(servletPath);
            }
                break;
            case Constants.SC_A_REMOTE_USER:
                if (tomcatAuthentication) {
                    // ignore server
                    requestHeaderMessage.getString(temp);
                } else {
                    request.setRemoteUser(requestHeaderMessage.getString(temp));
                }
                break;
            case Constants.SC_A_AUTH_TYPE:
                if (tomcatAuthentication) {
                    // ignore server
                    requestHeaderMessage.getString(temp);
                } else {
                    request.setAuthType(requestHeaderMessage.getString(temp));
                }
                break;
            case Constants.SC_A_QUERY_STRING: {
                final String queryString = requestHeaderMessage.getString(temp);
                request.setQueryString(queryString);
                {
                    parseQueryString(queryString);
                    if (DEBUG && isEASPingCommand()) {
                        LOG.debug("Incoming long-running EAS ping request.");
                    }
                }
            }
                break;
            case Constants.SC_A_JVM_ROUTE: {
                final String jvmRoute = requestHeaderMessage.getString(temp);
                if (DEBUG && !AJPv13Config.getJvmRoute().equals(jvmRoute)) {
                    LOG.debug("JVM route mismatch. Expected \"" + AJPv13Config.getJvmRoute() + "\", but is \"" + jvmRoute + "\".");
                }
                request.setInstanceId(jvmRoute);
            }
                break;
            case Constants.SC_A_SSL_CERT:
                request.setScheme(HTTPS);
                // SSL certificate extraction is lazy, moved to JkCoyoteHandler
                requestHeaderMessage.getBytes(certificates);
                break;
            case Constants.SC_A_SSL_CIPHER:
                request.setScheme(HTTPS);
                request.setAttribute("javax.servlet.request.cipher_suite", requestHeaderMessage.getString(temp));
                break;
            case Constants.SC_A_SSL_SESSION:
                request.setScheme(HTTPS);
                request.setAttribute("javax.servlet.request.ssl_session", requestHeaderMessage.getString(temp));
                break;
            case Constants.SC_A_SSL_KEY_SIZE:
                request.setAttribute("javax.servlet.request.key_size", new Integer(requestHeaderMessage.getInt()));
                break;
            case Constants.SC_A_STORED_METHOD:
                request.setMethod(requestHeaderMessage.getString(temp));
                break;
            case Constants.SC_A_SECRET: {
                final String value = requestHeaderMessage.getString(temp);
                if (requiredSecret != null) {
                    secret = true;
                    if (!value.equals(requiredSecret)) {
                        response.setStatus(403);
                        error = true;
                    }
                }
            }
                break;
            default:
                // Nothing to do
                break;
            }
        }
        /*
         * Check if secret was submitted if required
         */
        if ((requiredSecret != null) && !secret) {
            response.setStatus(403);
            error = true;
        }
        /*
         * Check for a full URI (including protocol://host:port/)
         */
        {
            final String requestURI = request.getRequestURI();
            if (requestURI.regionMatches(true, 0, "http", 0, 4)) {
                final int pos = requestURI.indexOf("://", 4);
                int slashPos = -1;
                if (pos != -1) {
                    slashPos = requestURI.indexOf('/', pos + 3);
                    if (slashPos == -1) {
                        slashPos = requestURI.length();
                        /*
                         * Set URI as "/"
                         */
                        request.setRequestURI("/");
                    } else {
                        request.setRequestURI(requestURI.substring(slashPos));
                    }
                    final String host = request.getHeader("host");
                    if (null != host) {
                        try {
                            request.setHeader("host", host.substring(pos + 3, slashPos), false);
                        } catch (final AJPv13Exception e) {
                            // Cannot occur
                        }
                    }
                }
            }
        }
        /*
         * Parse host header
         */
        {
            final String host = request.getHeader("host");
            if (null != host) {
                parseHost(host);
            }
        }
        /*-
         * Parsing done
         *
         * Determine addressed servlet instance
         */
        setServletInstance(request.getRequestURI());
        final String serverName = request.getServerName();
        {
            final Props properties = LogProperties.getLogProperties();
            if (LogProperties.isEnabled()) {
                properties.put(LogProperties.Name.AJP_REQUEST_URI, request.getRequestURI());
                properties.put(LogProperties.Name.AJP_SERVLET_PATH, request.getServletPath());
                properties.put(LogProperties.Name.AJP_PATH_INFO, request.getPathInfo());
                final String action = request.getParameter("action");
                if (null != action) {
                    properties.put(LogProperties.Name.AJAX_ACTION, action);
                }
            }
            properties.put(LogProperties.Name.AJP_REQUEST_IP, request.getRemoteAddr());
            properties.put(LogProperties.Name.AJP_SERVER_NAME, serverName);
        }
        /*
         * Set proper JSESSIONID cookie and pre-create associated HTTP session
         */
        if (jsessionID == null) {
            /*
             * Look for JSESSIONID cookie, if request URI does not contain session id
             */
            checkJSessionIDCookie(serverName);
        } else {
            String thisJVMRoute = request.getInstanceId();
            if (null == thisJVMRoute) {
                final int dot = jsessionID.lastIndexOf('.');
                thisJVMRoute = -1 == dot ? null : jsessionID.substring(dot + 1);
            }
            if ((null == thisJVMRoute) || (AJPv13Config.getJvmRoute().equals(thisJVMRoute))) {
                addJSessionIDCookie(jsessionID, serverName);
            } else {
                /*
                 * JVM route does not match
                 */
                createJSessionIDCookie(serverName);
            }
        }
    }

    /**
     * Parse host.
     */
    public void parseHost(final String host) {
        if (null == host || 0 == host.length()) {
            /*
             * HTTP/1.0; server port and server name are equal to local ones.
             */
            request.setServerPort(request.getLocalPort());
            request.setServerName(request.getLocalName());
            return;
        }
        /*
         * Build host string
         */
        final boolean ipv6 = '[' == host.charAt(0);
        final int length = host.length();
        final StringBuilder hostBuilder = new StringBuilder(length);
        int colonPos = -1;
        boolean bracketClosed = false;
        for (int i = 0; i < length; i++) {
            final char c = host.charAt(i);
            hostBuilder.append(c);
            if (']' == c) {
                bracketClosed = true;
            } else if (':' == c) {
                if (!ipv6 || bracketClosed) {
                    colonPos = i;
                    break;
                }
            }
        }
        /*
         * Colon detected?
         */
        if (colonPos < 0) {
            if (request.getScheme().equalsIgnoreCase(HTTPS)) {
                /*
                 * 443 - Default HTTPS port
                 */
                request.setServerPort(443);
            } else {
                // 80 - Default HTTTP port
                request.setServerPort(80);
            }
            request.setServerName(hostBuilder.toString());
        } else {
            request.setServerName(hostBuilder.substring(0, colonPos));
            int port = 0;
            int mult = 1;
            for (int i = length - 1; i > colonPos; i--) {
                final int charValue = host.charAt(i);
                /*-
                 *
                if (charValue == -1) {
                    // Invalid character
                    error = true;
                    // 400 - Bad request
                    LOG.warn("400 - Bad Request: Invalid character in server name: \"" + host + '"');
                    response.setStatus(400);
                    break;
                }
                 *
                 */
                port = port + (charValue * mult);
                mult = 10 * mult;
            }
            request.setServerPort(port);
        }
    }

    private static final java.util.regex.Pattern PATTERN_SPLIT = java.util.regex.Pattern.compile("&");

    /**
     * Parses a query string and puts resulting parameters into given servlet request.
     *
     * @param queryStr The query string to be parsed
     */
    private void parseQueryString(final String queryStr) {
        final String[] paramsNVPs = PATTERN_SPLIT.split(queryStr, 0);
        String charEnc = request.getCharacterEncoding();
        if (null == charEnc) {
            charEnc = AJPv13Config.getServerProperty(Property.DefaultEncoding);
        }
        for (String paramsNVP : paramsNVPs) {
            paramsNVP = paramsNVP.trim();
            if (paramsNVP.length() > 0) {
                // Look-up character '='
                final int pos = paramsNVP.indexOf('=');
                if (pos >= 0) {
                    request.setParameter(paramsNVP.substring(0, pos), decodeUrl(paramsNVP.substring(pos + 1), charEnc));
                } else {
                    request.setParameter(paramsNVP, "");
                }
            }
        }
    }

    /**
     * Sets this request hander's servlet reference to the one bound to given path argument
     *
     * @param requestURI The request URI
     */
    private void setServletInstance(final String requestURI) {
        /*
         * Remove leading slash character
         */
        final String path = requestURI.length() > 1 ? removeFromPath(requestURI, '/') : requestURI;
        /*
         * Lookup path in available servlet paths
         */
        if (servletId.length() > 0) {
            servletId.setLength(0);
        }
        HttpServlet servlet = HttpServletManager.getServlet(path, servletId);
        if (servlet == null) {
            servlet = new HttpErrorServlet("No servlet bound to path/alias: " + AJPv13Utility.urlEncode(requestURI));
        }
        this.servlet = servlet;
        // servletId = pathStorage.length() > 0 ? pathStorage.toString() : null;
        if (servletId.length() > 0) {
            servletPath = removeFromPath(servletId.toString(), '*');
        }
        request.setServletInstance(servlet);
        request.setServletPath(servletPath);
        if (null != servletPath) {
            /*
             * Apply the servlet path with leading "/" character
             */
            final int servletPathLen = servletPath.length();
            if ((1 == servletPathLen) && ('*' == servletPath.charAt(0))) {
                /*
                 * Set an empty string ("") if the servlet used to process this request was matched using the "/*" pattern.
                 */
                request.setServletPath("");
                /*
                 * Set complete request URI as path info
                 */
                request.setPathInfo(requestURI);
            } else {
                /*
                 * The path starts with a "/" character and includes either the servlet name or a path to the servlet, but does not include
                 * any extra path information or a query string.
                 */
                request.setServletPath(servletPath);
                /*
                 * Set path info: The extra path information follows the servlet path but precedes the query string and will start with a
                 * "/" character.
                 */
                if ((requestURI.length() > servletPathLen) /* && requestURI.startsWith(servletPath) */) {
                    request.setPathInfo(requestURI.substring(servletPathLen));
                } else {
                    request.setPathInfo(null);
                }
            }
        }
    }

    /**
     * Removes specified character if given path ends with such a character.
     *
     * @param path The path to prepare
     * @param c The (trailing) character to remove
     * @return The path possibly with ending character removed
     */
    private static String removeFromPath(final String path, final char c) {
        final int len = path.length();
        if (c == path.charAt(len - 1)) {
            // Ends with "/"
            return path.substring(0, len - 1);
        }
        return path;
    }

    private static final String JSESSIONID_COOKIE = AJPv13RequestHandler.JSESSIONID_COOKIE;

    private void checkJSessionIDCookie(final String serverName) {
        final Cookie[] cookies = request.getCookies();
        Cookie jsessionIDCookie = null;
        boolean deleteAttempt = false;
        if (cookies != null) {
            NextCookie: for (int i = 0; (i < cookies.length) && (jsessionIDCookie == null); i++) {
                final Cookie current = cookies[i];
                if (JSESSIONID_COOKIE.equals(current.getName())) {
                    /*
                     * Check JVM route
                     */
                    final String id = current.getValue();
                    final int pos = id.lastIndexOf('.');
                    final String jvmRoute = AJPv13Config.getJvmRoute();
                    if (pos > -1) {
                        if ((jvmRoute != null) && !jvmRoute.equals(id.substring(pos + 1))) {
                            /*
                             * Different JVM route detected -> Discard
                             */
                            if (DEBUG) {
                                LOG.debug(new com.openexchange.java.StringAllocator("\n\tDifferent JVM route detected. Removing JSESSIONID cookie: ").append(id));
                            }
                            current.setPath("/");
                            final String domain = extractDomainValue(id);
                            if (null != domain) {
                                current.setDomain(domain);
                                // Once again without domain parameter
                                final Cookie respCookie2 = new Cookie(JSESSIONID_COOKIE, id);
                                respCookie2.setPath("/");
                                respCookie2.setMaxAge(0); // delete
                                response.addCookie(respCookie2);
                            }
                            current.setMaxAge(0); // delete
                            current.setSecure(request.isSecure() || (forceHttps && !Cookies.isLocalLan(request)));
                            response.addCookie(current);
                            deleteAttempt = true;
                            continue NextCookie;
                        }
                        /*
                         * Check known JSESSIONIDs and corresponding HTTP session
                         */
                        if (!HttpSessionManagement.isHttpSessionValid(id)) {
                            /*
                             * Invalid cookie
                             */
                            if (DEBUG) {
                                LOG.debug(new com.openexchange.java.StringAllocator("\n\tExpired or invalid cookie -> Removing JSESSIONID cookie: ").append(current.getValue()));
                            }
                            current.setPath("/");
                            final String domain = extractDomainValue(id);
                            if (null != domain) {
                                current.setDomain(domain);
                                // Once again without domain parameter
                                final Cookie respCookie2 = new Cookie(JSESSIONID_COOKIE, id);
                                respCookie2.setPath("/");
                                respCookie2.setMaxAge(0); // delete
                                response.addCookie(respCookie2);
                            }
                            current.setMaxAge(0); // delete
                            current.setSecure(request.isSecure() || (forceHttps && !Cookies.isLocalLan(request)));
                            response.addCookie(current);
                            deleteAttempt = true;
                            continue NextCookie;
                        }
                        jsessionIDCookie = current;
                        LogProperties.putLogProperty(LogProperties.Name.AJP_HTTP_SESSION, id);
                        jsessionIDCookie.setSecure(request.isSecure() || (forceHttps && !Cookies.isLocalLan(request)));
                        httpSessionCookie = jsessionIDCookie;
                        httpSessionJoined = true;
                    } else {
                        /*
                         * Value does not apply to pattern [UID].[JVM-ROUTE], hence only UID is given through special cookie JSESSIONID.
                         */
                        if (jvmRoute != null) {
                            /*
                             * But this host defines a JVM route
                             */
                            if (DEBUG) {
                                LOG.debug(new com.openexchange.java.StringAllocator("\n\tMissing JVM route in JESSIONID cookie").append(current.getValue()));
                            }
                            current.setPath("/");
                            final String domain = extractDomainValue(id);
                            if (null != domain) {
                                current.setDomain(domain);
                                // Once again without domain parameter
                                final Cookie respCookie2 = new Cookie(JSESSIONID_COOKIE, id);
                                respCookie2.setPath("/");
                                respCookie2.setMaxAge(0); // delete
                                response.addCookie(respCookie2);
                            }
                            current.setMaxAge(0); // delete
                            current.setSecure(request.isSecure() || (forceHttps && !Cookies.isLocalLan(request)));
                            response.addCookie(current);
                            deleteAttempt = true;
                            continue NextCookie;
                        }
                        /*
                         * Check known JSESSIONIDs and corresponding HTTP session
                         */
                        if (!HttpSessionManagement.isHttpSessionValid(id)) {
                            /*
                             * Invalid cookie
                             */
                            if (DEBUG) {
                                LOG.debug(new com.openexchange.java.StringAllocator("\n\tExpired or invalid cookie -> Removing JSESSIONID cookie: ").append(current.getValue()));
                            }
                            current.setPath("/");
                            final String domain = extractDomainValue(id);
                            if (null != domain) {
                                current.setDomain(domain);
                                // Once again without domain parameter
                                final Cookie respCookie2 = new Cookie(JSESSIONID_COOKIE, id);
                                respCookie2.setPath("/");
                                respCookie2.setMaxAge(0); // delete
                                response.addCookie(respCookie2);
                            }
                            current.setMaxAge(0); // delete
                            current.setSecure(request.isSecure() || (forceHttps && !Cookies.isLocalLan(request)));
                            response.addCookie(current);
                            deleteAttempt = true;
                            continue NextCookie;
                        }
                        jsessionIDCookie = current;
                        LogProperties.putLogProperty(LogProperties.Name.AJP_HTTP_SESSION, id);
                        jsessionIDCookie.setSecure(request.isSecure() || (forceHttps && !Cookies.isLocalLan(request)));
                        httpSessionCookie = jsessionIDCookie;
                        httpSessionJoined = true;
                    }
                }
            }
        }
        if (jsessionIDCookie == null) {
            createJSessionIDCookie(serverName);
        } else if (deleteAttempt) {
            final Cookie reApply = new Cookie(JSESSIONID_COOKIE, jsessionIDCookie.getValue());
            reApply.setPath("/");
            final String domain = getDomainValue(serverName);
            if (null != domain) {
                reApply.setDomain(domain);
            }
            HttpServletRequestImpl.configureCookie(reApply);
            response.addCookie(reApply);
        }
    }

    private void createJSessionIDCookie(final String serverName) {
        /*
         * Create a new unique id
         */
        final com.openexchange.java.StringAllocator jsessionIDVal = new com.openexchange.java.StringAllocator(HttpSessionManagement.getNewUniqueId());
        final String jvmRoute = AJPv13Config.getJvmRoute();
        final String domain = getDomainValue(serverName);
        if ((jvmRoute != null) && (jvmRoute.length() > 0)) {
            if (null != domain) {
                jsessionIDVal.append('-').append(urlEncode(domain));
            }
            jsessionIDVal.append('.').append(jvmRoute);
        }
        final String id = jsessionIDVal.toString();
        final Cookie jsessionIDCookie = newJsessionIdCookie(id, domain);
        LogProperties.putLogProperty(LogProperties.Name.AJP_HTTP_SESSION, id);
        jsessionIDCookie.setSecure(request.isSecure() || (forceHttps && !Cookies.isLocalLan(request)));
        httpSessionCookie = jsessionIDCookie;
        httpSessionJoined = false;
        /*
         * HttpServletRequestWrapper.getSession() adds the JSESSIONID cookie
         */
        request.getSession(true);
    }

    private void addJSessionIDCookie(final String id, final String serverName) {
        final String domain = getDomainValue(serverName);
        final String jsessionIdVal;
        final boolean join;
        /*
         * Check known JSESSIONIDs and corresponding HTTP session
         */
        if (HttpSessionManagement.isHttpSessionValid(id)) {
            jsessionIdVal = id;
            join = true;
        } else {
            /*
             * Invalid cookie. Create a new unique id
             */
            final StringBuilder jsessionIDVal = new StringBuilder(HttpSessionManagement.getNewUniqueId());
            final String jvmRoute = AJPv13Config.getJvmRoute();
            if ((jvmRoute != null) && (jvmRoute.length() > 0)) {
                if (null != domain) {
                    jsessionIDVal.append('-').append(urlEncode(domain));
                }
                jsessionIDVal.append('.').append(jvmRoute);
            }
            jsessionIdVal = jsessionIDVal.toString();
            join = false;
        }
        final Cookie jsessionIDCookie = newJsessionIdCookie(jsessionIdVal, domain);
        LogProperties.putLogProperty(LogProperties.Name.AJP_HTTP_SESSION, jsessionIdVal);
        jsessionIDCookie.setSecure(request.isSecure() || (forceHttps && !Cookies.isLocalLan(request)));
        httpSessionCookie = jsessionIDCookie;
        httpSessionJoined = join;
        /*
         * HttpServletRequestWrapper.getSession() adds the JSESSIONID cookie
         */
        request.getSession(true);
    }

    private static final String DEFAULT_PATH = "/";

    private Cookie newJsessionIdCookie(final String jsessionId, final String domain) {
        final Cookie jsessionIDCookie = new Cookie(JSESSIONID_COOKIE, jsessionId);
        jsessionIDCookie.setPath(DEFAULT_PATH);
        if (null != domain) {
            jsessionIDCookie.setDomain(domain);
        }
        return jsessionIDCookie;
    }

    private static final String STR_SET_COOKIE = "Set-Cookie";

    /**
     * Data length of SEND_BODY_CHUNK:
     *
     * <pre>
     * prefix(1) + http_status_code(2) + http_status_msg(3) + num_headers(2)
     * </pre>
     */
    private static final int SEND_HEADERS_LENGTH = 8;

    /**
     * Starting first 4 bytes:
     *
     * <pre>
     * 'A' + 'B' + [data length as 2 byte integer]
     * </pre>
     */
    private static final int RESPONSE_PREFIX_LENGTH = 4;

    /**
     * When committing the response, we have to validate the set of headers, as well as setup the response filters.
     */
    protected void prepareResponse() throws IOException {
        response.setCommitted(true);
        /*
         * prefix + http_status_code + http_status_msg (empty string) + num_headers (integer)
         */
        String statusMsg = response.getStatusMsg();
        if (null == statusMsg) {
            statusMsg = "";
        }
        /*-
         * Check for echo header presence
         */
        {
            final String echoHeaderName = AJPv13Response.getEchoHeaderName();
            if (null != echoHeaderName) {
                final String echoValue = request.getHeader(echoHeaderName);
                if (null != echoValue) {
                    response.setHeader(echoHeaderName, urlEncode(echoValue));
                }
            }
        }
        /*
         * Write headers&cookies to JK_AJP13_SEND_HEADERS message
         */
        int numHeaders = 0;
        final byte[] headers;
        {
            sink.reset();
            for (final Entry<String, List<String>> entry : response.getHeaderEntrySet()) {
                for (final String value : entry.getValue()) {
                    writeHeaderSafe(entry.getKey(), value, sink);
                    numHeaders++;
                }
            }
            headers = sink.toByteArray();
        }
        final byte[] cookies;
        final int numCookies;
        {
            sink.reset();
            final List<List<String>> formattedCookies = response.getFormatedCookies();
            final int length = formattedCookies.size();
            if (length > 0) {
                List<String> list = formattedCookies.get(0);
                for (final String sCookie : list) {
                    writeHeaderSafe(STR_SET_COOKIE, sCookie, sink);
                }
                if (length > 1) {
                    final StringBuilder sb = new StringBuilder(STR_SET_COOKIE.length() + 1);
                    for (int i = 1; i < length; i++) {
                        sb.setLength(0);
                        final String hdrName = sb.append(STR_SET_COOKIE).append(i + 1).toString();
                        list = formattedCookies.get(i);
                        for (final String sCookie : list) {
                            writeHeaderSafe(hdrName, sCookie, sink);
                        }
                    }
                }
                cookies = sink.toByteArray();
                numCookies = getNumOfCookieHeader(formattedCookies);
            } else {
                cookies = new byte[0];
                numCookies =  0;
            }
        }
        /*
         * Calculate data length
         */
        final int dataLength = SEND_HEADERS_LENGTH + headers.length + cookies.length + statusMsg.length();
        try {
            if (dataLength + RESPONSE_PREFIX_LENGTH > Constants.MAX_PACKET_SIZE) {
                throw new AJPv13MaxPackgeSizeException((dataLength + RESPONSE_PREFIX_LENGTH));
            }
            sink.reset();
            AJPv13Response.fillStartBytes(Constants.JK_AJP13_SEND_HEADERS, dataLength, sink);
            AJPv13Response.writeInt(response.getStatus(), sink);
            AJPv13Response.writeString(statusMsg, sink);
            AJPv13Response.writeInt(numHeaders + numCookies, sink);
            AJPv13Response.writeByteArray(headers, sink);
            AJPv13Response.writeByteArray(cookies, sink);
        } catch (final AJPv13Exception e) {
            throw new IOException(e.getMessage(), e);
        }
        softLock.lock();
        try {
            sink.writeTo(output);
            // output.flush();
        } finally {
            softLock.unlock();
        }
        lastWriteAccess = System.currentTimeMillis();
    }

    private static final int getNumOfCookieHeader(final List<List<String>> formattedCookies) {
        int retval = 0;
        for (final List<String> formattedCookie : formattedCookies) {
            retval += formattedCookie.size();
        }
        return retval;
    }

    private static String toValue(final List<String> values) {
        if (null == values || values.isEmpty()) {
            return "";
        }
        final StringBuilder retval = new StringBuilder(128);
        retval.append(values.get(0));
        final int len = values.size();
        for (int i = 1; i < len; i++) {
            retval.append(',').append(values.get(i));
        }
        return retval.toString();
    }

    /**
     * Finish AJP response.
     */
    protected void finish() throws IOException {
        finish(true);
    }

    /**
     * Finish AJP response.
     */
    protected void finish(final boolean reuse) throws IOException {

        if (!response.isCommitted()) {
            // Validate and write response headers
            try {
                prepareResponse();
            } catch (final IOException e) {
                // Set error flag
                error = true;
            }
        }

        if (finished) {
            return;
        }

        finished = true;

        // Add the end message
        softLock.lock();
        try {
            output.write(reuse ? endMessageArray : endMessageArrayNoReuse);
            // output.flush();
        } finally {
            softLock.unlock();
        }
        lastWriteAccess = System.currentTimeMillis();
    }

    /**
     * Read at least the specified amount of bytes, and place them in the input buffer.
     */
    protected boolean read(final byte[] buf, final int pos, final int n) throws IOException {
        int read = 0;
        int res = 0;
        while (read < n) {
            res = input.read(buf, read + pos, n - read);
            if (res <= 0) {
                throw new IOException("Socket read failed");
            }
            read += res;
        }
        return true;
    }

    /**
     * Receive a chunk of data. Called to implement the 'special' packet in ajp13 and to receive the data after we send a GET_BODY packet
     */
    public boolean receive() throws IOException {
        first = false;
        bodyMessage.reset();
        readMessage(bodyMessage);
        // No data received.
        if (bodyMessage.getLen() == 0) {
            // just the header
            // Don't mark 'end of stream' for the first chunk.
            return false;
        }
        final int blen = bodyMessage.peekInt();
        if (blen == 0) {
            return false;
        }
        bodyMessage.getBytes(bodyBytes);
        empty = false;
        return true;
    }

    /**
     * Get more request body data from the web server and store it in the internal buffer.
     *
     * @return true if there is more data, false if not.
     */
    protected boolean refillReadBuffer() throws IOException {
        // If the server returns an empty packet, assume that that end of
        // the stream has been reached (yuck -- fix protocol??).
        // FORM support
        if (replay) {
            endOfStream = true; // we've read everything there is
        }
        if (endOfStream) {
            return false;
        }

        // Request more data immediately
        softLock.lock();
        try {
            output.write(getBodyMessageArray);
            // output.flush();
        } finally {
            softLock.unlock();
        }
        lastWriteAccess = System.currentTimeMillis();

        final boolean moreData = receive();
        if (!moreData) {
            endOfStream = true;
        }
        return moreData;
    }

    /**
     * Read an AJP message.
     *
     * @return true if the message has been read, false if the short read didn't return anything
     * @throws IOException any other failure, including incomplete reads
     */
    protected boolean readMessage(final AjpMessage message) throws IOException {
        final byte[] buf = message.getBuffer();
        // Read initial bytes: 0x12 0x34 <data-length>
        read(buf, 0, message.getHeaderLength());
        message.processHeader();
        // Read AJP payload
        read(buf, message.getHeaderLength(), message.getLen());
        return true;
    }

    /**
     * Recycle the processor.
     */
    public void recycle() {
        // Recycle Request object
        first = true;
        endOfStream = false;
        empty = true;
        replay = false;
        finished = false;
        pingCount = 0;
        httpSessionCookie = null;
        httpSessionJoined = false;
        servlet = null;
        servletPath = null;
        servletId.setLength(0);
        lastWriteAccess = 0L;
        request.recycle();
        response.recycle();
        certificates.recycle();
    }

    // ------------------------------------- InputStreamInputBuffer Inner Class

    /**
     * This class is an input buffer which will read its data from an input stream.
     */
    protected class SocketInputBuffer implements InputBuffer {

        /**
         * Read bytes into the specified chunk.
         */
        @Override
        public int doRead(final ByteChunk chunk, final HttpServletRequestImpl req) throws IOException {
            if (endOfStream) {
                return -1;
            }
            if (first && req.getContentLengthLong() > 0) {
                /*
                 * Handle special first-body-chunk
                 */
                if (!receive()) {
                    return 0;
                }
            } else if (empty) {
                if (!refillReadBuffer()) {
                    return -1;
                }
            }
            final ByteChunk bc = bodyBytes.getByteChunk();
            chunk.setBytes(bc.getBuffer(), bc.getStart(), bc.getLength());
            empty = true;
            return chunk.getLength();
        }
    }

    // ----------------------------------- OutputStreamOutputBuffer Inner Class

    /**
     * Data length of SEND_BODY_CHUNK:
     *
     * <pre>
     * prefix (1) + chunk_length (2) + terminating zero byte (1)
     * </pre>
     */
    private static final int SEND_BODY_CHUNK_LENGTH = 4;

    /**
     * This class is an output buffer which will write data to an output stream.
     */
    protected class SocketOutputBuffer implements OutputBuffer {

        private final int chunkSize;

        protected SocketOutputBuffer() {
            super();
            chunkSize = Constants.MAX_SEND_SIZE + (packetSize - Constants.MAX_PACKET_SIZE);
        }

        @Override
        public int getPacketSize() {
            return chunkSize;
        }

        /**
         * Write chunk.
         */
        @Override
        public int doWrite(final ByteChunk chunk) throws IOException {
            if (!response.isCommitted()) {
                // Validate and write response headers
                try {
                    prepareResponse();
                } catch (final IOException e) {
                    // Set error flag
                    error = true;
                }
            }
            int len = chunk.getLength();
            if (len <= 0) {
                return len;
            }
            // 4 - hardcoded, byte[] marshalling overhead
            // Adjust allowed size if packetSize != default (Constants.MAX_PACKET_SIZE)
            final byte[] b = chunk.getBuffer();
            final int start = chunk.getOffset();
            int off = 0;
            do {
                int thisTime = len;
                if (thisTime > chunkSize) {
                    thisTime = chunkSize;
                }
                len -= thisTime;
                softLock.lock();
                try {
                    output.write(getSendBodyChunkBytes(b, start + off, thisTime));
                    // output.flush();
                } finally {
                    softLock.unlock();
                }
                off += thisTime;
            } while (len > 0);
            lastWriteAccess = System.currentTimeMillis();
            return chunk.getLength();
        }

        private byte[] getSendBodyChunkBytes(final byte[] b, final int off, final int len) {
            /*
             * prefix + chunk_length (2 bytes) + chunk bytes + terminating zero byte
             */
            final int dataLength = SEND_BODY_CHUNK_LENGTH + len;
            /*
             * Start bytes
             */
            sink.reset();
            sink.write(0x41);
            sink.write(0x42);
            sink.write((dataLength >>> 8) & 0xFF);
            sink.write(dataLength & 0xFF);
            sink.write(Constants.JK_AJP13_SEND_BODY_CHUNK);
            sink.write((len >>> 8) & 0xFF);
            sink.write(len & 0xFF);
            sink.write(b, off, len);
            sink.write(0);
            return sink.toByteArray();
        }

    }

    private static final class KeepAliveRunnable implements Runnable {

        private final AjpProcessor ajpProcessor;
        private final int max;

        /**
         * Initializes a new {@link KeepAliveRunnable} to only perform keep-alive on given AJP task.
         *
         * @param task The AJP task
         * @param max The max. processing time when a AJP task is considered as exceeded an keep-alive takes place
         */
        public KeepAliveRunnable(final AjpProcessor ajpProcessor, final int max) {
            super();
            this.ajpProcessor = ajpProcessor;
            this.max = max;
        }

        @Override
        public void run() {
            try {
                if (ajpProcessor.isProcessing() && ((System.currentTimeMillis() - ajpProcessor.getLastWriteAccess()) > max)) {
                    /*
                     * Send "keep-alive" package
                     */
                    ajpProcessor.action(ActionCode.CLIENT_PING, null);
                }
            } catch (final Exception e) {
                if (DEBUG) {
                    LOG.error("AJP KEEP-ALIVE failed.", e);
                }
            }
        }

    } // End of class

    private static void appendStackTrace(final StackTraceElement[] trace, final com.openexchange.java.StringAllocator sb, final String lineSeparator) {
        if (null == trace) {
            return;
        }
        for (final StackTraceElement ste : trace) {
            final String className = ste.getClassName();
            if (null != className) {
                sb.append("    at ").append(className).append('.').append(ste.getMethodName());
                if (ste.isNativeMethod()) {
                    sb.append("(Native Method)");
                } else {
                    final String fileName = ste.getFileName();
                    if (null == fileName) {
                        sb.append("(Unknown Source)");
                    } else {
                        final int lineNumber = ste.getLineNumber();
                        sb.append('(').append(fileName);
                        if (lineNumber >= 0) {
                            sb.append(':').append(lineNumber);
                        }
                        sb.append(')');
                    }
                }
                sb.append(lineSeparator);
            }
        }
    }

    /**
     * Generates URL-encoding of specified text.
     *
     * @param text The text
     * @return The URL-encoded text
     */
    private static String urlEncode(final String text) {
        try {
            return URLEncoder.encode(text, "iso-8859-1");
        } catch (final UnsupportedEncodingException e) {
            return text;
        }
    }

}
