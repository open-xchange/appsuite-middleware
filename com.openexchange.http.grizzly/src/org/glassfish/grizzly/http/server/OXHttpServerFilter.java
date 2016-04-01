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

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2010-2012 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 *
 * Portions Copyright 2012 OPEN-XCHANGE, licensed under GPL Version 2.
 */
package org.glassfish.grizzly.http.server;

import static org.glassfish.grizzly.http.util.HttpCodecUtils.put;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.ConnectionProbe;
import org.glassfish.grizzly.Grizzly;
import org.glassfish.grizzly.IOEvent;
import org.glassfish.grizzly.ReadHandler;
import org.glassfish.grizzly.attributes.Attribute;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.FilterChainContext.CompletionListener;
import org.glassfish.grizzly.filterchain.NextAction;
import org.glassfish.grizzly.http.HttpContent;
import org.glassfish.grizzly.http.HttpPacket;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.http.HttpResponsePacket;
import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.server.io.OutputBuffer;
import org.glassfish.grizzly.http.server.util.HtmlHelper;
import org.glassfish.grizzly.http.util.Header;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.glassfish.grizzly.memory.Buffers;
import org.glassfish.grizzly.memory.MemoryManager;
import org.glassfish.grizzly.monitoring.jmx.JmxMonitoringAware;
import org.glassfish.grizzly.monitoring.jmx.JmxObject;
import org.glassfish.grizzly.utils.DelayedExecutor;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.ExceptionUtils;
import com.openexchange.http.grizzly.osgi.Services;
import com.openexchange.http.grizzly.util.RequestTools;
import com.openexchange.java.Charsets;
import com.openexchange.marker.OXThreadMarker;
import com.openexchange.timer.ScheduledTimerTask;
import com.openexchange.timer.TimerService;

/**
 * Filter implementation to provide high-level HTTP request/response processing.
 */
public class OXHttpServerFilter extends HttpServerFilter implements JmxMonitoringAware<HttpServerProbe> {

    private static enum Ping {
        /**
         * No ping at all.
         */
        NONE,
        /**
         * The client SHOULD continue with its request. This interim response is used to inform the client that the initial part of the
         * request has been received and has not yet been rejected by the server. The client SHOULD continue by sending the remainder of the
         * request or, if the request has already been completed, ignore this response. The server MUST send a final response after the
         * request has been completed. See section 8.2.3 for detailed discussion of the use and handling of this status code.
         */
        CONTINUE,
        /**
         * The 102 (Processing) status code is an interim response used to inform the client that the server has accepted the complete
         * request, but has not yet completed it. This status code SHOULD only be sent when the server has a reasonable expectation that the
         * request will take significant time to complete. As guidance, if a method is taking longer than 20 seconds (a reasonable, but
         * arbitrary value) to process the server SHOULD return a 102 (Processing) response. The server MUST send a final response after the
         * request has been completed.
         * <p>
         * Methods can potentially take a long period of time to process, especially methods that support the Depth header. In such cases
         * the client may time-out the connection while waiting for a response. To prevent this the server may return a 102 (Processing)
         * status code to indicate to the client that the server is still processing the method.
         */
        PROCESSING,
        /**
         * Tries to avoid a timeout through sending a whitespace.
         */
        WHITESPACE;

        /**
         * Gets the ping constant for specified identifier.
         *
         * @param identifier The identifier
         * @return The ping constant or <code>null</code>
         */
        public static Ping pingFor(final String identifier) {
            if (null == identifier) {
                return null;
            }
            for (final Ping p : Ping.values()) {
                if (identifier.equalsIgnoreCase(p.name())) {
                    return p;
                }
            }
            return null;
        }
    }

    private static final class WatchInfo implements CompletionListener, ConnectionProbe {

        final ScheduledTimerTask timerTask;
        final Response handlerResponse;
        private final AtomicInteger pingCount;
        private final Object sync;
        private long lastReadTime = -1;

        WatchInfo(ScheduledTimerTask timerTask, Response handlerResponse, AtomicInteger pingCount, Object sync) {
            super();
            this.timerTask = timerTask;
            this.handlerResponse = handlerResponse;
            this.pingCount = pingCount;
            this.sync = sync;
        }

        @Override
        public void onComplete(final FilterChainContext context) {
            stopPing();
        }

        public void stopPing() {
            synchronized (sync) {
                pingCount.set(-1);
            }
        }

        @Override
        public void onBindEvent(Connection connection) {}

        @Override
        public void onAcceptEvent(Connection serverConnection, Connection clientConnection) {}

        @Override
        public void onConnectEvent(Connection connection) {}

        @Override
        public void onReadEvent(Connection connection, Buffer data, int size) {
            lastReadTime = System.currentTimeMillis();
        }

        @Override
        public void onWriteEvent(Connection connection, Buffer data, long size) {
            // Sending a ping will touch the lastWriteTime
            //lastWriteTime = System.currentTimeMillis();
        }

        @Override
        public void onErrorEvent(Connection connection, Throwable error) {}

        @Override
        public void onCloseEvent(Connection connection) {}

        @Override
        public void onIOEventReadyEvent(Connection connection, IOEvent ioEvent) {}

        @Override
        public void onIOEventEnableEvent(Connection connection, IOEvent ioEvent) {}

        @Override
        public void onIOEventDisableEvent(Connection connection, IOEvent ioEvent) {}

    }

    private static final Logger LOGGER = Grizzly.logger(OXHttpServerFilter.class);

    private static final byte[] CRLF = {(byte) '\r', (byte) '\n'};

    private final Attribute<Request> httpRequestInProcessAttr;
    private final Attribute<Boolean> reregisterForReadAttr;
    protected final DelayedExecutor.DelayQueue<Response> suspendedResponseQueue;
    private volatile HttpHandler httpHandler;
    private final ConcurrentMap<FilterChainContext, WatchInfo> pingMap;
    private final int pingDelay;
    private final int maxPingCount;
    private final Ping ping;

    // ------------------------------------------------------------ Constructors

    public OXHttpServerFilter(final ServerFilterConfiguration config, final DelayedExecutor delayedExecutor) {
        super(config, delayedExecutor);
        // Ping stuff
        pingMap = new ConcurrentHashMap<FilterChainContext, WatchInfo>(512, 0.75f, 32);
        {
            final ConfigurationService service = Services.optService(ConfigurationService.class);
            pingDelay = null == service ? 90000 : service.getIntProperty("com.openexchange.http.grizzly.pingDelay", 90000);
            maxPingCount = null == service ? 9 : service.getIntProperty("com.openexchange.http.grizzly.maxPingCount", 9);
            ping = null == service ? Ping.PROCESSING : Ping.pingFor(service.getProperty("com.openexchange.http.grizzly.ping", "PROCESSING").trim());
        }
        // Rest
        suspendedResponseQueue = Response.createDelayQueue(delayedExecutor);
        httpRequestInProcessAttr = Grizzly.DEFAULT_ATTRIBUTE_BUILDER.createAttribute("HttpServerFilter.Request");
        reregisterForReadAttr = Grizzly.DEFAULT_ATTRIBUTE_BUILDER.createAttribute("HttpServerFilter.reregisterForReadAttr");
    }

    @Override
    @SuppressWarnings({"UnusedDeclaration"})
    public HttpHandler getHttpHandler() {
        return httpHandler;
    }

    @Override
    public void setHttpHandler(final HttpHandler httpHandler) {
        this.httpHandler = httpHandler;
    }

    // ----------------------------------------------------- Methods from Filter

    private static final OXThreadMarker DUMMY = new OXThreadMarker() {

        @Override
        public void setHttpRequestProcessing(boolean httpProcessing) {
            // Nothing
        }

        @Override
        public boolean isHttpRequestProcessing() {
            return false;
        }
    };

    @SuppressWarnings({ "unchecked", "ReturnInsideFinallyBlock" })
    @Override
    public NextAction handleRead(final FilterChainContext ctx) throws IOException {
        final Object message = ctx.getMessage();
        final Connection connection = ctx.getConnection();

        if (HttpPacket.isHttp(message)) {
            OXThreadMarker threadMarker;
            {
                Thread t = Thread.currentThread();
                threadMarker = t instanceof OXThreadMarker ? (OXThreadMarker) t : DUMMY;
            }
            threadMarker.setHttpRequestProcessing(true);
            try {
                // Otherwise cast message to a HttpContent
                final HttpContent httpContent = (HttpContent) message;

                Request handlerRequest = httpRequestInProcessAttr.get(connection);

                if (handlerRequest == null) {
                    // It's a new HTTP request
                    final HttpRequestPacket request = (HttpRequestPacket) httpContent.getHttpHeader();
                    final HttpResponsePacket response = request.getResponse();
                    handlerRequest = OXRequest.create();
                    handlerRequest.parameters.setLimit(getConfiguration().getMaxRequestParameters());
                    httpRequestInProcessAttr.set(connection, handlerRequest);
                    final Response handlerResponse = handlerRequest.getResponse();

                    handlerRequest.initialize(/* handlerResponse, */request, ctx, this);
                    final SuspendStatus suspendStatus = handlerResponse.initialize(handlerRequest, response, ctx, suspendedResponseQueue, this);

                    HttpServerProbeNotifier.notifyRequestReceive(this, connection, handlerRequest);

                    boolean wasSuspended = false;
                    boolean pingInitiated = false;

                    try {
                        ctx.setMessage(handlerResponse);

                        if (!getConfiguration().isPassTraceRequest() && request.getMethod() == Method.TRACE) {
                            onTraceRequest(handlerRequest, handlerResponse);
                        } else {
                            final HttpHandler httpHandlerLocal = httpHandler;
                            if (httpHandlerLocal != null) {
                                // Initiate ping (if required)
                                if (ping != Ping.NONE && allowsPing(handlerRequest) && !isLongRunning(handlerRequest)) {
                                    pingInitiated = initiatePing(handlerResponse, ctx, ping);
                                }

                                // Handle HTTP message
                                httpHandlerLocal.doHandle(handlerRequest, handlerResponse);
                            }
                        }
                    } catch (Exception t) {
                        handlerRequest.getRequest().getProcessingState().setError(true);

                        if (!response.isCommitted()) {
                            final ByteBuffer b = HtmlHelper.getExceptionErrorPage("Internal Server Error", "Grizzly/2.0", t);
                            handlerResponse.reset();
                            handlerResponse.setStatus(HttpStatus.INTERNAL_SERVER_ERROR_500);
                            handlerResponse.setContentType("text/html");
                            handlerResponse.setCharacterEncoding("UTF-8");
                            final MemoryManager mm = ctx.getMemoryManager();
                            final Buffer buf = Buffers.wrap(mm, b);
                            handlerResponse.getOutputBuffer().writeBuffer(buf);
                        }
                    } catch (Throwable t) {
                        ExceptionUtils.handleThrowable(t);
                        LOGGER.log(Level.WARNING, "Unexpected error", t);
                        throw new IllegalStateException(t);
                    } finally {
                        // don't forget to invalidate the suspendStatus
                        wasSuspended = suspendStatus.getAndInvalidate();

                        if (pingInitiated) {
                            final WatchInfo watchInfo = pingMap.remove(ctx);
                            if (null != watchInfo) {
                                watchInfo.stopPing();
                                watchInfo.timerTask.cancel(false);
                                ctx.removeCompletionListener(watchInfo);
                                connection.getMonitoringConfig().removeProbes(watchInfo);
                                // Canceled timer task gets purged by CustomThreadPoolExecutorTimerService.PurgeRunnable
                            }
                        }
                    }

                    if (!wasSuspended) {
                        return afterService(ctx, connection, handlerRequest, handlerResponse);
                    } else {
                        return ctx.getSuspendAction();
                    }
                } else {
                    // We're working with suspended HTTP request
                    try {
                        if (!handlerRequest.getInputBuffer().append(httpContent)) {
                            // we don't want this thread/context to reset
                            // OP_READ on Connection

                            // we have enough data? - terminate filter chain execution
                            final NextAction action = ctx.getSuspendAction();
                            ctx.completeAndRecycle();
                            return action;
                        }
                    } finally {
                        httpContent.recycle();
                    }
                }
            } finally {
                threadMarker.setHttpRequestProcessing(false);
            }
        } else { // this code will be run, when we resume the context
            if (Boolean.TRUE.equals(reregisterForReadAttr.remove(ctx))) {
                // Do we want to reregister OP_READ to get more data async?
                ctx.suspend();
                return ctx.getForkAction();
            } else {
                // We're finishing the request processing
                final Response response = (Response) message;
                final Request request = response.getRequest();
                return afterService(ctx, connection, request, response);
            }
        }

        return ctx.getStopAction();
    }

    private boolean isLongRunning(final Request request) {
        return RequestTools.isUsmJsonOrEasRequest(request);
    }

    private static final String USM_USER_AGENT = "Open-Xchange USM HTTP Client";
    private static final String _NET_USER_AGENT = "Open-Xchange .NET HTTP Client";
    private static final String OXSTOR_USER_AGENT = "oxstor.dll";
    private static final int MAX_UA_LEN = _NET_USER_AGENT.length();

    private static final Set<String> NO_PING = Collections.<String> unmodifiableSet(new HashSet<String>(Arrays.asList(USM_USER_AGENT, _NET_USER_AGENT, OXSTOR_USER_AGENT)));

    private boolean allowsPing(Request request) {
        String ua = request.getHeader("User-Agent");
        return null != ua && (ua.length() > MAX_UA_LEN || !NO_PING.contains(ua));
    }

    private boolean initiatePing(final Response handlerResponse, final FilterChainContext ctx, final Ping ping) {
        if (ping == Ping.NONE) {
            return false;
        }

        final TimerService timerService = Services.optService(TimerService.class);
        if (null == timerService) {
            return false;
        }

        final ConcurrentMap<FilterChainContext, WatchInfo> cm = pingMap;
        final Logger logger = LOGGER;
        final boolean debugEnabled = logger.isLoggable(Level.FINE);
        final byte[] crlfBytes = CRLF;

        final int maxPingCount = this.maxPingCount;
        final AtomicInteger pingCount = new AtomicInteger(maxPingCount <= 0 ? Integer.MAX_VALUE : maxPingCount);
        final AtomicReference<ScheduledTimerTask> ref = new AtomicReference<ScheduledTimerTask>();
        final int pingDelay = this.pingDelay;

        final Runnable r = new Runnable() {

            @Override
            public synchronized void run() {
                try {
                    final WatchInfo watchInfo = cm.get(ctx);
                    boolean pingIssued = false;
                    if (null != watchInfo && (watchInfo.handlerResponse instanceof OXResponse)) {

                        //We are still reading from the client?
                        long readDiffMillis = System.currentTimeMillis() - watchInfo.lastReadTime;
                        if(readDiffMillis < pingDelay) {
                            return;
                        }

                        final StampingNIOOutputStreamImpl stamped = (StampingNIOOutputStreamImpl) ((OXResponse) watchInfo.handlerResponse).createOutputStream();

                        // Issue a ping as long as stream not closed AND nothing has been written to upstream
                        if (stamped.closed || !stamped.doPing) {
                            if (debugEnabled) {
                                final Request request = ((OXResponse) watchInfo.handlerResponse).getRequest();
                                if (null != request) {
                                    final String action = request.getParameter("action");
                                    logger.fine("OXHttpServerFilter: Aborted 102 Processing interim responses for " + request.getRequestURI() + (null == action ? "" : "?action=" + action));
                                }
                            }
                            // Not allowed/possible to issue a further ping as data was already transferred
                            final ScheduledTimerTask timerTask = ref.get();
                            if (null != timerTask) {
                                timerTask.cancel(false);
                            }
                            return;
                        }

                        // Check whether to issue a further ping
                        if (pingCount.decrementAndGet() < 0) {
                            // Not allowed to issue a further ping
                            final ScheduledTimerTask timerTask = ref.get();
                            if (null != timerTask) {
                                timerTask.cancel(false);
                            }
                            return;
                        }

                        // Issue a ping
                        final MemoryManager memoryManager = ctx.getMemoryManager();
                        if (Ping.PROCESSING == ping) {
                            final Buffer encodedBuffer = memoryManager.allocate(128);
                            put(memoryManager, encodedBuffer, Charsets.toAsciiBytes("HTTP/1.1 102 Processing"));
                            put(memoryManager, encodedBuffer, crlfBytes);
                            put(memoryManager, encodedBuffer, crlfBytes);
                            encodedBuffer.trim();
                            encodedBuffer.allowBufferDispose(true);
                            ctx.write(encodedBuffer, true);
                            if (debugEnabled) {
                                final Request request = ((OXResponse) watchInfo.handlerResponse).getRequest();
                                if (null != request) {
                                    final String action = request.getParameter("action");
                                    logger.fine("OXHttpServerFilter: Issued a 102 Processing interim response for " + request.getRequestURI() + (null == action ? "" : "?action=" + action));
                                }
                            }
                        } else if (Ping.CONTINUE == ping) {
                            final Buffer encodedBuffer = memoryManager.allocate(128);
                            put(memoryManager, encodedBuffer, Charsets.toAsciiBytes("HTTP/1.1 100 Continue"));
                            put(memoryManager, encodedBuffer, crlfBytes);
                            put(memoryManager, encodedBuffer, crlfBytes);
                            encodedBuffer.trim();
                            encodedBuffer.allowBufferDispose(true);
                            ctx.write(encodedBuffer, true);
                        } else {
                            final Buffer buffer = memoryManager.allocate(128);
                            put(memoryManager, buffer, Charsets.toAsciiBytes(" "));
                            buffer.trim();
                            buffer.allowBufferDispose(true);
                            final OutputBuffer outputBuffer = handlerResponse.getOutputBuffer();
                            outputBuffer.writeBuffer(buffer);
                            outputBuffer.flush();
                        }
                        pingIssued = true;
                    }
                    if (false == pingIssued) {
                        pingCount.set(maxPingCount);
                    }
                } catch (final Exception e) {
                    logger.log(Level.WARNING, "Timer run failed: " + e.getMessage(), e);
                }
            }
        };

        final ScheduledTimerTask timerTask = timerService.scheduleWithFixedDelay(r, pingDelay, pingDelay);
        ref.set(timerTask);
        final WatchInfo watchInfo = new WatchInfo(timerTask, handlerResponse, pingCount, r);
        cm.put(ctx, watchInfo);
        ctx.getConnection().getMonitoringConfig().addProbes(watchInfo);
        ctx.addCompletionListener(watchInfo);

        return true;
    }

    /**
     * Override the default implementation to notify the {@link ReadHandler}, if available, of any read error that has occurred during
     * processing.
     *
     * @param ctx event processing {@link FilterChainContext}
     * @param error error, which occurred during <tt>FilterChain</tt> execution
     */
    @Override
    public void exceptionOccurred(FilterChainContext ctx, Throwable error) {
        final Connection c = ctx.getConnection();

        final Request request = httpRequestInProcessAttr.get(c);

        if (request != null) {
            final ReadHandler handler = request.getInputBuffer().getReadHandler();
            if (handler != null) {
                handler.onError(error);
            }
        }
    }


    // ------------------------------------------------------- Protected Methods


    @Override
    protected JmxObject createJmxManagementObject() {
        return new org.glassfish.grizzly.http.server.jmx.HttpServerFilter(this);
    }

    @Override
    protected void onTraceRequest(final Request request, final Response response) throws IOException {
        if (getConfiguration().isTraceEnabled()) {
            HtmlHelper.writeTraceMessage(request, response);
        } else {
            response.setStatus(HttpStatus.METHOD_NOT_ALLOWED_405);
            response.setHeader(Header.Allow, "POST, GET, DELETE, OPTIONS, PUT, HEAD");
        }
    }

    // --------------------------------------------------------- Private Methods


    private NextAction afterService(final FilterChainContext ctx, final Connection connection, final Request request, final Response response) throws IOException {

        httpRequestInProcessAttr.remove(connection);

        response.finish();
        request.onAfterService();

        HttpServerProbeNotifier.notifyRequestComplete(this, connection, response);

        final HttpRequestPacket httpRequest = request.getRequest();
        final boolean isBroken = httpRequest.isContentBroken();

        // Suspend state is cancelled - it means normal processing might have
        // been broken. We don't want to reuse Request and Response in this state,
        // cause there still might be threads referencing them.
        if (response.suspendState.get() != Response.SuspendState.CANCELLED) {
            response.recycle();
            request.recycle();
        }

        if (isBroken) {
            // if content is broken - we're not able to distinguish
            // the end of the message - so stop processing any input data on
            // this connection (connection is being closed by
            // {@link org.glassfish.grizzly.http.HttpServerFilter#handleEvent(...)}
            final NextAction suspendNextAction = ctx.getSuspendAction();
            ctx.completeAndRecycle();
            return suspendNextAction;
        }

        return ctx.getStopAction();
    }
}
