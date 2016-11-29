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

package org.glassfish.grizzly.http.server;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.glassfish.grizzly.CompletionHandler;
import org.glassfish.grizzly.Connection;
import org.glassfish.grizzly.EmptyCompletionHandler;
import org.glassfish.grizzly.Grizzly;
import org.glassfish.grizzly.ReadHandler;
import org.glassfish.grizzly.attributes.Attribute;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.FilterChainEvent;
import org.glassfish.grizzly.filterchain.NextAction;
import org.glassfish.grizzly.filterchain.TransportFilter;
import org.glassfish.grizzly.http.HttpContent;
import org.glassfish.grizzly.http.HttpContext;
import org.glassfish.grizzly.http.HttpPacket;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.http.HttpResponsePacket;
import org.glassfish.grizzly.http.Method;
import org.glassfish.grizzly.http.server.util.HtmlHelper;
import org.glassfish.grizzly.http.util.HttpStatus;
import org.glassfish.grizzly.localization.LogMessages;
import org.glassfish.grizzly.monitoring.DefaultMonitoringConfig;
import org.glassfish.grizzly.monitoring.MonitoringConfig;
import org.glassfish.grizzly.utils.DelayedExecutor;
import com.openexchange.exception.ExceptionUtils;
import com.openexchange.http.grizzly.GrizzlyConfig;
import com.openexchange.marker.OXThreadMarker;


/**
 * {@link OXHttpServerFilter}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.3
 */
public class OXHttpServerFilter extends HttpServerFilter {

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

    private final static Logger LOGGER = Grizzly.logger(HttpHandler.class);
    /**
     * The {@link CompletionHandler} to be used to make sure the response data
     * have been flushed
     */
    private final FlushResponseHandler flushResponseHandler = new FlushResponseHandler();

    /**
     * Attribute, which holds the current HTTP Request in progress associated
     * with an HttpContext
     */
    private final Attribute<Request> httpRequestInProgress;

    /**
     * Delay queue to control suspended request/response processing timeouts
     */
    private final DelayedExecutor.DelayQueue<Response.SuspendTimeout> suspendedResponseQueue;

    /**
     * Root {@link HttpHandler}
     */
    private volatile HttpHandler httpHandler;

    /**
     * The flag, which indicates if the server is currently in the shutdown phase
     */
    private volatile boolean isShuttingDown;
    /**
     * CompletionHandler to be notified, when shutdown could be gracefully completed
     */
    private AtomicReference<CompletionHandler<HttpServerFilter>> shutdownCompletionHandlerRef;

    /**
     * The number of requests, which are currently in process.
     */
    private final AtomicInteger activeRequestsCounter = new AtomicInteger();

    /**
     * Web server probes
     */
    protected final DefaultMonitoringConfig<HttpServerProbe> monitoringConfig =
            new DefaultMonitoringConfig<HttpServerProbe>(HttpServerProbe.class) {

                @Override
                public Object createManagementObject() {
                    return createJmxManagementObject();
                }

            };

    private final GrizzlyConfig grizzlyConfig;


    // ------------------------------------------------------------ Constructors

    public OXHttpServerFilter(GrizzlyConfig grizzlyConfig, final ServerFilterConfiguration config, final DelayedExecutor delayedExecutor) {
        super(config, delayedExecutor);
        this.grizzlyConfig = grizzlyConfig;
        suspendedResponseQueue = Response.createDelayQueue(delayedExecutor);
        httpRequestInProgress = Grizzly.DEFAULT_ATTRIBUTE_BUILDER.
                        createAttribute("HttpServerFilter.Request");
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

    @SuppressWarnings({"unchecked", "ReturnInsideFinallyBlock"})
    @Override
    public NextAction handleRead(final FilterChainContext ctx)
          throws IOException {

        // every message coming to HttpServerFilter#handleRead has to have
        // HttpContext associated with the FilterChainContext
        assert HttpContext.get(ctx) != null;

        final Object message = ctx.getMessage();
        final Connection connection = ctx.getConnection();

        if (HttpPacket.isHttp(message)) {
            OXThreadMarker threadMarker = threadMarker();
            threadMarker.setHttpRequestProcessing(true);
            try {
                // Otherwise cast message to a HttpContent
                final HttpContent httpContent = (HttpContent) message;
                final HttpContext context = httpContent.getHttpHeader()
                        .getProcessingState().getHttpContext();
                Request handlerRequest = httpRequestInProgress.get(context);

                if (handlerRequest == null) {
                    // It's a new HTTP request
                    final HttpRequestPacket request = (HttpRequestPacket) httpContent.getHttpHeader();
                    final HttpResponsePacket response = request.getResponse();
                    ServerFilterConfiguration config = getConfiguration();

                    handlerRequest = OXRequest.create(grizzlyConfig);
                    handlerRequest.parameters.setLimit(config.getMaxRequestParameters());
                    httpRequestInProgress.set(context, handlerRequest);
                    final Response handlerResponse = handlerRequest.getResponse();

                    handlerRequest.initialize(request, ctx, this);
                    handlerResponse.initialize(handlerRequest, response,
                            ctx, suspendedResponseQueue, this);

                    if (config.isGracefulShutdownSupported()) {
                        activeRequestsCounter.incrementAndGet();
                        handlerRequest.addAfterServiceListener(flushResponseHandler);
                    }

                    HttpServerProbeNotifier.notifyRequestReceive(this, connection,
                            handlerRequest);

                    boolean wasSuspended = false;

                    try {
                        ctx.setMessage(handlerResponse);

                        if (isShuttingDown) { // if we're in the shutting down phase - serve shutdown page and exit
                            handlerResponse.getResponse().getProcessingState().setError(true);
                            HtmlHelper.setErrorAndSendErrorPage(
                                    handlerRequest, handlerResponse,
                                    config.getDefaultErrorPageGenerator(),
                                    503, HttpStatus.SERVICE_UNAVAILABLE_503.getReasonPhrase(),
                                    "The server is being shutting down...", null);
                        } else if (!config.isPassTraceRequest()
                                && request.getMethod() == Method.TRACE) {
                            onTraceRequest(handlerRequest, handlerResponse);
                        } else if (!checkMaxPostSize(request.getContentLength())) {
                            handlerResponse.getResponse().getProcessingState().setError(true);
                            HtmlHelper.setErrorAndSendErrorPage(
                                    handlerRequest, handlerResponse,
                                    config.getDefaultErrorPageGenerator(),
                                    400, HttpStatus.BAD_REQUEST_400.getReasonPhrase(),
                                    "The request payload size exceeds the max post size limitation", null);
                        } else {
                            final HttpHandler httpHandlerLocal = httpHandler;
                            if (httpHandlerLocal != null) {
                                wasSuspended = !httpHandlerLocal.doHandle(
                                        handlerRequest, handlerResponse);
                            }
                        }
                    } catch (Exception t) {
                        LOGGER.log(Level.WARNING,
                                LogMessages.WARNING_GRIZZLY_HTTP_SERVER_FILTER_HTTPHANDLER_INVOCATION_ERROR(), t);

                        request.getProcessingState().setError(true);

                        if (!response.isCommitted()) {
                                HtmlHelper.setErrorAndSendErrorPage(
                                        handlerRequest, handlerResponse,
                                        config.getDefaultErrorPageGenerator(),
                                        500, HttpStatus.INTERNAL_SERVER_ERROR_500.getReasonPhrase(),
                                        HttpStatus.INTERNAL_SERVER_ERROR_500.getReasonPhrase(),
                                        t);
                        }
                    } catch (Throwable t) {
                        ExceptionUtils.handleThrowable(t);
                        LOGGER.log(Level.WARNING,
                                LogMessages.WARNING_GRIZZLY_HTTP_SERVER_FILTER_UNEXPECTED(), t);
                        throw new IllegalStateException(t);
                    }

                    if (!wasSuspended) {
                        return afterService(ctx, connection,
                                handlerRequest, handlerResponse);
                    } else {
                        return ctx.getSuspendAction();
                    }
                } else {
                    // We're working with suspended HTTP request
                    try {
                        ctx.suspend();
                        final NextAction action = ctx.getSuspendAction();

                        if (!handlerRequest.getInputBuffer().append(httpContent)) {
                            // we don't want this thread/context to reset
                            // OP_READ on Connection

                            // we have enough data? - terminate filter chain execution
                            ctx.completeAndRecycle();
                        } else {
                            ctx.resume(ctx.getStopAction());
                        }

                        return action;
                    } finally {
                        httpContent.recycle();
                    }
                }
            } finally {
                threadMarker.setHttpRequestProcessing(false);
            }
        } else { // this code will be run, when we resume the context
            // We're finishing the request processing
            final Response response = (Response) message;
            final Request request = response.getRequest();
            return afterService(ctx, connection, request, response);
        }
    }

    /**
     * Override the default implementation to notify the {@link ReadHandler},
     * if available, of any read error that has occurred during processing.
     *
     * @param ctx event processing {@link FilterChainContext}
     * @param error error, which occurred during <tt>FilterChain</tt> execution
     */
    @Override
    public void exceptionOccurred(final FilterChainContext ctx,
            final Throwable error) {
        final HttpContext context = HttpContext.get(ctx);
        if (context != null) {
            final Request request = httpRequestInProgress.get(context);

            if (request != null) {
                final ReadHandler handler = request.getInputBuffer().getReadHandler();
                if (handler != null) {
                    handler.onError(error);
                }
            }
        }
    }


    // ---------------------------------------------------------- Public Methods


    /**
     * {@inheritDoc}
     */
    @Override
    public MonitoringConfig<HttpServerProbe> getMonitoringConfig() {
        return monitoringConfig;
    }

    /**
     * Method, which might be optionally called to prepare the filter for
     * shutdown.
     * @param shutdownCompletionHandler {@link CompletionHandler} to be notified,
     *        when shutdown could be gracefully completed
     */
    @Override
    public void prepareForShutdown(
            final CompletionHandler<HttpServerFilter> shutdownCompletionHandler) {
        this.shutdownCompletionHandlerRef =
                new AtomicReference<CompletionHandler<HttpServerFilter>>(shutdownCompletionHandler);
        isShuttingDown = true;

        if (activeRequestsCounter.get() == 0 &&
                shutdownCompletionHandlerRef.getAndSet(null) != null) {
            shutdownCompletionHandler.completed(this);
        }
    }

    // --------------------------------------------------------- Private Methods

    private NextAction afterService(
            final FilterChainContext ctx,
            final Connection connection,
            final Request request,
            final Response response)
            throws IOException {

        final HttpContext context = request.getRequest()
                .getProcessingState().getHttpContext();

        httpRequestInProgress.remove(context);
        response.finish();
        request.onAfterService();

        HttpServerProbeNotifier.notifyRequestComplete(this, connection, response);

        final HttpRequestPacket httpRequest = request.getRequest();
        final boolean isBroken = httpRequest.isContentBroken();

        // Suspend state is cancelled - it means normal processing might have
        // been broken. We don't want to reuse Request and Response in this state,
        // cause there still might be threads referencing them.
        if (response.suspendState != Response.SuspendState.CANCELLED) {
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

    /**
     * Will be called, once HTTP request processing is complete and response is
     * flushed.
     */
    private void onRequestCompleteAndResponseFlushed() {
        final int count = activeRequestsCounter.decrementAndGet();
        if (count == 0 && isShuttingDown) {
            final CompletionHandler<HttpServerFilter> shutdownHandler =
                    shutdownCompletionHandlerRef != null
                    ? shutdownCompletionHandlerRef.getAndSet(null)
                    : null;

            if (shutdownHandler != null) {
                shutdownHandler.completed(this);
            }
        }
    }

    /**
     * @param requestContentLength
     * @return <tt>true</tt> if request content-length doesn't exceed
     *      the max post size limit, or <tt>false</tt> otherwise
     */
    private boolean checkMaxPostSize(final long requestContentLength) {
        final long maxPostSize = getConfiguration().getMaxPostSize();
        return requestContentLength <= 0 || maxPostSize < 0 ||
                maxPostSize >= requestContentLength;
    }

    /**
     * The {@link CompletionHandler} to be used to make sure the response data
     * have been flushed.
     */
    private final class FlushResponseHandler
            extends EmptyCompletionHandler<Object>
            implements AfterServiceListener{

        private final FilterChainEvent event = TransportFilter.createFlushEvent(this);

        @Override
        public void cancelled() {
            onRequestCompleteAndResponseFlushed();
        }

        @Override
        public void failed(final Throwable throwable) {
            onRequestCompleteAndResponseFlushed();
        }

        @Override
        public void completed(final Object result) {
            onRequestCompleteAndResponseFlushed();
        }

        @Override
        public void onAfterService(final Request request) {
            // same as request.getContext().flush(this), but less garbage
            request.getContext().notifyDownstream(event);
        }
    }

    private OXThreadMarker threadMarker() {
        Thread t = Thread.currentThread();
        return t instanceof OXThreadMarker ? (OXThreadMarker) t : DUMMY;
    }

}
