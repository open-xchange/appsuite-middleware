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

package com.openexchange.http.grizzly.filters.backendroute;

import java.io.IOException;
import java.util.Iterator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.grizzly.filterchain.BaseFilter;
import org.glassfish.grizzly.filterchain.FilterChainContext;
import org.glassfish.grizzly.filterchain.NextAction;
import org.glassfish.grizzly.http.HttpContent;
import org.glassfish.grizzly.http.HttpHeader;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.http.HttpResponsePacket;
import org.glassfish.grizzly.http.util.Header;
import org.glassfish.grizzly.http.util.MimeHeaders;
import org.glassfish.grizzly.memory.ByteBufferWrapper;

/**
 * {@link AppendBackendRouteFilter} to append the backend route to the JSessionId cookie if the backend has a route configured.
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class AppendBackendRouteFilter extends BaseFilter {

    private static final Log LOG = LogFactory.getLog(AppendBackendRouteFilter.class);
    private String backendRoute;
    
    /**
     * Gets the backendRoute
     *
     * @return The backendRoute
     */
    private String getBackendRoute() {
        return backendRoute;
    }

    /**
     * Sets the backendRoute.
     *
     * @param backendRoute The backendRoute to set
     */
    private void setBackendRoute(String backendRoute) {
        this.backendRoute = backendRoute == null ? "" : backendRoute;
    }

    /**
     * Initializes a new {@link AppendBackendRouteFilter}.
     * @param backendRoute the currently configured backend route.
     */
    public AppendBackendRouteFilter(String backendRoute) {
        if(backendRoute == null) {
            throw new IllegalArgumentException();
        }
        setBackendRoute(backendRoute);
    }
    
    
    
    /* (non-Javadoc)
     * @see org.glassfish.grizzly.filterchain.BaseFilter#handleAccept(org.glassfish.grizzly.filterchain.FilterChainContext)
     */
    @Override
    public NextAction handleAccept(FilterChainContext ctx) throws IOException {
        LOG.info(ctx.getMessage());
        return super.handleAccept(ctx);
    }

    /* (non-Javadoc)
     * @see org.glassfish.grizzly.filterchain.BaseFilter#handleClose(org.glassfish.grizzly.filterchain.FilterChainContext)
     */
    @Override
    public NextAction handleClose(FilterChainContext ctx) throws IOException {
        LOG.info(ctx.getMessage());
        return super.handleClose(ctx);
    }

    /** 
     * While handling reads we have to handle several different cases of JSessionId in the incoming request:
     * <ol>
     *   <li>
     *     <h4>No JSessionId</h4>
     *     <p>The JSessionId will get generated further up the fitler chain, recheck in handleWrite.</p>
     *   </li>
     *   <li>
     *     <h4>JSessionId without backend route</h4>
     *     <p>Add the backend route to the JSessionId if the backend has a route configured.</p>
     *   </li>
     *   <li>
     *     <h4>JSessionId with wrong backend route</h4>
     *     <p>Adjust the JSessionId to match the backend route of this backend if it has a route configured.</p>
     *   </li>
     *   <li>
     *     <h4>JSessionId with correct backend route</h4>
     *     <p>Nothing to do here.</p>
     *   </li>
     * </ol>
     * @see org.glassfish.grizzly.filterchain.BaseFilter#handleRead(org.glassfish.grizzly.filterchain.FilterChainContext)
     */
    @Override
    public NextAction handleRead(FilterChainContext ctx) throws IOException {
        if(ctx.getMessage() instanceof HttpContent) {
            HttpContent content = (HttpContent) ctx.getMessage();
            HttpHeader httpHeader = content.getHttpHeader();
            if(httpHeader instanceof HttpRequestPacket) {
                HttpRequestPacket httpRequestPacket = (HttpRequestPacket) httpHeader;
                checkCookieHeaders(httpRequestPacket, getBackendRoute());
            } else {
                throw new IllegalStateException();
            }
        }
        return super.handleRead(ctx);
    }

    /**
     * Request might not have had a JSessionID but response received one during Servlet.service().
     * Check Headers for new JSessionId and append the backend route if it has a route configured.
     * @see org.glassfish.grizzly.filterchain.BaseFilter#handleRead(org.glassfish.grizzly.filterchain.FilterChainContext) 
     */
    @Override
    public NextAction handleWrite(FilterChainContext ctx) throws IOException {
        if(!getBackendRoute().isEmpty()) { //only check if route is configured
            if(ctx.getMessage() instanceof HttpContent) {
                HttpContent content = (HttpContent) ctx.getMessage();
                HttpHeader httpHeader = content.getHttpHeader();
                if(httpHeader instanceof HttpResponsePacket) {
                    HttpResponsePacket httpResponsePacket = (HttpResponsePacket) httpHeader;
                    checkSetCookieHeaders(httpResponsePacket, getBackendRoute());
                } else {
                    throw new IllegalStateException();
                }
            }
        }
        return super.handleWrite(ctx);
    }

    /**
     * Check if one of the Cookie headers contains a JSessionId and modify request/response if needed.
     * @param httpRequestPacket the incoming http request, must not be null
     * @param backendRoute the currently configured backend route, must not be null
     * @throws IllegalArgumentException for null arguments
     */
    private void checkCookieHeaders(HttpRequestPacket httpRequestPacket, String backendRoute) {
        if(httpRequestPacket == null || backendRoute == null) {
            throw new IllegalArgumentException();
        }
        MimeHeaders requestMimeHeaders = httpRequestPacket.getHeaders();
        if (requestMimeHeaders.contains(Header.Cookie)) {
            ClientCookieInspector clientCookieInspector = new ClientCookieInspector(httpRequestPacket, backendRoute);
            if (clientCookieInspector.isJSessionIdExistant() && !clientCookieInspector.isJSessionIdValid()) {
                writeToDebugLog(new StringBuilder("JSessionId is invalid: ").append(clientCookieInspector.getJSessionIdValue()).toString());
                clientCookieInspector.fixJSessionId();
                writeToDebugLog(new StringBuilder("Fixed JSessionId is: ").append(clientCookieInspector.getJSessionIdValue()).toString());
                
                //fix response
                HttpResponsePacket httpResponsePacket = httpRequestPacket.getResponse();
                ServerCookieInspector serverCookieInspector = new ServerCookieInspector(httpResponsePacket, backendRoute);
                String fixedJSessionId = clientCookieInspector.getJSessionIdValue();
                serverCookieInspector.setJSessionIDCookieValue(fixedJSessionId);
                ByteBufferWrapper setJSessionIdCookieHeader = serverCookieInspector.getSetJSessionIdCookieHeader();
                MimeHeaders responseMimeHeaders = httpResponsePacket.getHeaders();
                responseMimeHeaders.removeHeaderMatches(Header.SetCookie, "^JSESSIONID=.*");
                responseMimeHeaders.addValue(Header.SetCookie).setBuffer(setJSessionIdCookieHeader);
            }
        }
    }

    /**
     * Check if one of the Set-Cookie headers contains a JSessionId and modify response if needed.
     * @param httpResponsePacket the outgoing http respone, must not be null
     * @param backendRoute the currently configured backend route, must not be null
     * @throws 
     */
    private void checkSetCookieHeaders(HttpResponsePacket httpResponsePacket, String backendRoute) {
        if(httpResponsePacket == null || backendRoute == null) {
            throw new IllegalArgumentException();
        }
        MimeHeaders responseMimeHeaders = httpResponsePacket.getHeaders();
        if(responseMimeHeaders.contains(Header.SetCookie)) {
            Iterator<String> setCookieIterator = responseMimeHeaders.values(Header.SetCookie).iterator();
            while(setCookieIterator.hasNext()) {
                String setCookieHeader = setCookieIterator.next();
                if(setCookieHeader.startsWith("JSESSIONID=")) {
                    ServerCookieInspector serverCookieInspector = new ServerCookieInspector(httpResponsePacket, backendRoute);
                    if(!serverCookieInspector.isJSessionIdValid()) {
                        serverCookieInspector.fixJSessionId();
                        setCookieIterator.remove();
//                        at org.glassfish.grizzly.http.util.ValuesIterator.findNext(MimeHeaders.java:642)
//                        at org.glassfish.grizzly.http.util.ValuesIterator.remove(MimeHeaders.java:669)
//                        at com.openexchange.http.grizzly.filters.backendroute.AppendBackendRouteFilter.checkSetCookieHeaders(AppendBackendRouteFilter.java:235)
//                        at com.openexchange.http.grizzly.filters.backendroute.AppendBackendRouteFilter.handleWrite(AppendBackendRouteFilter.java:176)
//                        at org.glassfish.grizzly.filterchain.ExecutorResolver$8.execute(ExecutorResolver.java:111)
//                        at org.glassfish.grizzly.filterchain.DefaultFilterChain.executeFilter(DefaultFilterChain.java:265)
//                        at org.glassfish.grizzly.filterchain.DefaultFilterChain.executeChainPart(DefaultFilterChain.java:200)
//                        at org.glassfish.grizzly.filterchain.DefaultFilterChain.execute(DefaultFilterChain.java:134)
//                        at org.glassfish.grizzly.filterchain.DefaultFilterChain.process(DefaultFilterChain.java:112)
//                        at org.glassfish.grizzly.ProcessorExecutor.execute(ProcessorExecutor.java:78)
//                        at org.glassfish.grizzly.filterchain.FilterChainContext.write(FilterChainContext.java:652)
//                        at org.glassfish.grizzly.http.server.io.OutputBuffer.writeContentBuffer0(OutputBuffer.java:839)
//                        at org.glassfish.grizzly.http.server.io.OutputBuffer.writeContentChunk(OutputBuffer.java:824)
//                        at org.glassfish.grizzly.http.server.io.OutputBuffer.flush(OutputBuffer.java:616)
//                        at org.glassfish.grizzly.http.server.NIOWriterImpl.flush(NIOWriterImpl.java:104)
//                        at org.glassfish.grizzly.servlet.ServletHandler.customizeErrorPage(ServletHandler.java:333)
//                        at org.glassfish.grizzly.servlet.ServletHandler.doServletService(ServletHandler.java:255)
//                        at org.glassfish.grizzly.servlet.ServletHandler.service(ServletHandler.java:190)
//                        at com.openexchange.http.grizzly.services.http.OSGiMainHandler.service(OSGiMainHandler.java:142)
//                        at org.glassfish.grizzly.http.server.HttpHandler.doHandle(HttpHandler.java:164)
//                        at org.glassfish.grizzly.http.server.HttpHandlerChain.service(HttpHandlerChain.java:207)
//                        at org.glassfish.grizzly.http.server.HttpHandler.doHandle(HttpHandler.java:164)
//                        at org.glassfish.grizzly.http.server.HttpServerFilter.handleRead(HttpServerFilter.java:164)
//                        at org.glassfish.grizzly.filterchain.ExecutorResolver$9.execute(ExecutorResolver.java:119)
//                        at org.glassfish.grizzly.filterchain.DefaultFilterChain.executeFilter(DefaultFilterChain.java:265)
//                        at org.glassfish.grizzly.filterchain.DefaultFilterChain.executeChainPart(DefaultFilterChain.java:200)
//                        at org.glassfish.grizzly.filterchain.DefaultFilterChain.execute(DefaultFilterChain.java:134)
//                        at org.glassfish.grizzly.filterchain.DefaultFilterChain.process(DefaultFilterChain.java:112)
//                        at org.glassfish.grizzly.ProcessorExecutor.execute(ProcessorExecutor.java:78)
//                        at org.glassfish.grizzly.nio.transport.TCPNIOTransport.fireIOEvent(TCPNIOTransport.java:815)
//                        at org.glassfish.grizzly.strategies.AbstractIOStrategy.fireIOEvent(AbstractIOStrategy.java:112)
//                        at org.glassfish.grizzly.strategies.WorkerThreadIOStrategy.run0(WorkerThreadIOStrategy.java:115)
//                        at org.glassfish.grizzly.strategies.WorkerThreadIOStrategy.access$0(WorkerThreadIOStrategy.java:111)
//                        at org.glassfish.grizzly.strategies.WorkerThreadIOStrategy$WorkerThreadRunnable.run(WorkerThreadIOStrategy.java:135)
//                        at org.glassfish.grizzly.threadpool.AbstractThreadPool$Worker.doWork(AbstractThreadPool.java:567)
//                        at org.glassfish.grizzly.threadpool.AbstractThreadPool$Worker.run(AbstractThreadPool.java:547)
//                        at java.lang.Thread.run(Thread.java:662)
                        responseMimeHeaders.addValue(Header.SetCookie).setBuffer(serverCookieInspector.getSetJSessionIdCookieHeader());
                    }
                }
                break;
            }
        }
    }
    
    /**
     * Write a String to the info log.
     * @param logValue the String that should be logged
     */
    private static void writeToInfoLog(String logValue) {
        if(LOG.isInfoEnabled()) {
            LOG.info(logValue);
        }
    }
    
    /**
     * Write a String to the debug log.
     * @param logValue the String that should be logged
     */
    private static void writeToDebugLog(String logValue) {
        if(LOG.isDebugEnabled()) {
            LOG.debug(logValue);
        }
    }
}
