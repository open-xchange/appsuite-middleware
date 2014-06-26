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

package com.openexchange.solr.internal;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.WeakHashMap;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.util.ContentStreamBase;
import org.apache.solr.common.util.FastWriter;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.common.util.SimpleOrderedMap;
import org.apache.solr.core.Config;
import org.apache.solr.core.CoreContainer;
import org.apache.solr.core.SolrConfig;
import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.component.SearchHandler;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.request.SolrRequestHandler;
import org.apache.solr.request.SolrRequestInfo;
import org.apache.solr.response.BinaryQueryResponseWriter;
import org.apache.solr.response.QueryResponseWriter;
import org.apache.solr.response.SolrQueryResponse;
import org.apache.solr.servlet.SolrRequestParsers;
import org.apache.solr.servlet.cache.HttpCacheHeaderUtil;
import org.apache.solr.servlet.cache.Method;
import org.xml.sax.InputSource;
import com.openexchange.exception.OXException;

/**
 * {@link SolrServlet}
 *
 * @author <a href="mailto:steffen.templin@open-xchange.com">Steffen Templin</a>
 */
public class SolrServlet extends HttpServlet {

    private static final long serialVersionUID = -7395630600887358848L;

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SolrServlet.class);

    private static final Charset UTF8 = Charset.forName("UTF-8");

    private final Map<SolrConfig, SolrRequestParsers> parsers = new WeakHashMap<SolrConfig, SolrRequestParsers>();

    private final SolrRequestParsers adminRequestParser;

    private final CoreContainer cores;

    private final String pathPrefix;

    private ServletConfig config;

    public SolrServlet(final CoreContainer cores, final String pathPrefix) throws OXException {
        super();
        this.cores = cores;
        this.pathPrefix = pathPrefix;
        try {
            adminRequestParser = new SolrRequestParsers(new Config(null,"solr",new InputSource(new ByteArrayInputStream("<root/>".getBytes("UTF-8"))),"") );
        } catch (Exception e) {
            throw new OXException(e);
        }
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        this.config = config;
    }

    @Override
    public ServletConfig getServletConfig() {
        return config;
    }

    @Override
    public void service(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        if (request instanceof HttpServletRequest) {
            HttpServletRequest req = (HttpServletRequest) request;
            HttpServletResponse resp = (HttpServletResponse) response;
            SolrRequestHandler handler = null;
            SolrQueryRequest solrReq = null;
            SolrCore core = null;
            String corename = "";
            try {
                // put the core container in request attribute
                req.setAttribute("org.apache.solr.CoreContainer", cores);
                String path = req.getServletPath();
                if (req.getPathInfo() != null) {
                    // this lets you handle /update/commit when /update is a servlet
                    path += req.getPathInfo();
                }
                if (pathPrefix != null && path.startsWith(pathPrefix)) {
                    path = path.substring(pathPrefix.length());
                }
                // check for management path
                String alternate = cores.getManagementPath();
                if (alternate != null && path.startsWith(alternate)) {
                    path = path.substring(0, alternate.length());
                }
                // unused feature ?
                int idx = path.indexOf(':');
                if (idx > 0) {
                    // save the portion after the ':' for a 'handler' path parameter
                    path = path.substring(0, idx);
                }

                // Check for the core admin page
                if (path.equals(cores.getAdminPath())) {
                    handler = cores.getMultiCoreHandler();
                    solrReq = adminRequestParser.parse(null, path, req);
                    handleAdminRequest(req, response, handler, solrReq);
                    return;
                } else {
                    // otherwise, we should find a core from the path
                    idx = path.indexOf("/", 1);
                    if (idx > 1) {
                        // try to get the corename as a request parameter first
                        corename = path.substring(1, idx);
                        core = cores.getCore(corename);
                        if (core != null) {
                            path = path.substring(idx);
                        }
                    }
                    if (core == null) {
                        corename = "";
                        core = cores.getCore("");
                    }
                }

                // With a valid core...
                if (core != null) {
                    final SolrConfig config = core.getSolrConfig();
                    // get or create/cache the parser for the core
                    SolrRequestParsers parser = null;
                    parser = parsers.get(config);
                    if (parser == null) {
                        parser = new SolrRequestParsers(config);
                        parsers.put(config, parser);
                    }

                    // Determine the handler from the url path if not set
                    // (we might already have selected the cores handler)
                    if (handler == null && path.length() > 1) { // don't match "" or "/" as valid path
                        handler = core.getRequestHandler(path);
                        // no handler yet but allowed to handle select; let's check
                        if (handler == null && parser.isHandleSelect()) {
                            if ("/select".equals(path) || "/select/".equals(path)) {
                                solrReq = parser.parse(core, path, req);
                                String qt = solrReq.getParams().get(CommonParams.QT);
                                handler = core.getRequestHandler(qt);
                                if (handler == null) {
                                    throw new SolrException(SolrException.ErrorCode.BAD_REQUEST, "unknown handler: " + qt);
                                }
                                if (qt != null && qt.startsWith("/") && !(handler instanceof SearchHandler)) {
                                    // For security reasons it's a bad idea to allow a leading '/', ex: /select?qt=/update see SOLR-3161
                                    // There was no restriction from Solr 1.4 thru 3.5 and it's now only supported for SearchHandlers.
                                    throw new SolrException(
                                        SolrException.ErrorCode.BAD_REQUEST,
                                        "Invalid query type.  Do not use /select to access: " + qt);
                                }
                            }
                        }
                    }

                    // With a valid handler and a valid core...
                    if (handler != null) {
                        // if not a /select, create the request
                        if (solrReq == null) {
                            solrReq = parser.parse(core, path, req);
                        }

                        final Method reqMethod = Method.getMethod(req.getMethod());
                        HttpCacheHeaderUtil.setCacheControlHeader(config, resp, reqMethod);
                        // unless we have been explicitly told not to, do cache validation
                        // if we fail cache validation, execute the query
                        if (config.getHttpCachingConfig().isNever304() || !HttpCacheHeaderUtil.doCacheHeaderValidation(
                            solrReq,
                            req,
                            reqMethod,
                            resp)) {
                            SolrQueryResponse solrRsp = new SolrQueryResponse();
                            /*
                             * even for HEAD requests, we need to execute the handler to ensure we don't get an error (and to make sure the
                             * correct QueryResponseWriter is selected and we get the correct Content-Type)
                             */
                            SolrRequestInfo.setRequestInfo(new SolrRequestInfo(solrReq, solrRsp));
                            this.execute(req, handler, solrReq, solrRsp);
                            HttpCacheHeaderUtil.checkHttpCachingVeto(solrRsp, resp, reqMethod);
                            // add info to http headers
                            // TODO: See SOLR-232 and SOLR-267.
                            /*
                             * try { NamedList solrRspHeader = solrRsp.getResponseHeader(); for (int i=0; i<solrRspHeader.size(); i++) {
                             * ((javax.servlet.http.HttpServletResponse) response).addHeader(("Solr-" + solrRspHeader.getName(i)),
                             * String.valueOf(solrRspHeader.getVal(i))); } } catch (ClassCastException cce) { log.log(Level.WARNING,
                             * "exception adding response header log information", cce); }
                             */
                            QueryResponseWriter responseWriter = core.getQueryResponseWriter(solrReq);
                            writeResponse(solrRsp, response, responseWriter, solrReq, reqMethod);
                        }
                        return; // we are done with a valid handler
                    }
                    // otherwise (we have a core), let's ensure the core is in the SolrCore request attribute so
                    // a servlet/jsp can retrieve it
                    else {
                        req.setAttribute("org.apache.solr.SolrCore", core);
                        // Modify the request so each core gets its own /admin
                        if (path.startsWith("/admin")) {
                            req.getRequestDispatcher(pathPrefix == null ? path : pathPrefix + path).forward(request, response);
                            return;
                        }
                    }
                }
                log.debug("no handler or core retrieved for {}, follow through...", path);
            } catch (Throwable ex) {
                sendError((HttpServletResponse) response, ex);
                return;
            } finally {
                if (solrReq != null) {
                    solrReq.close();
                }
                if (core != null) {
                    core.close();
                }
                SolrRequestInfo.clearRequestInfo();
            }
        }
    }

    private void handleAdminRequest(HttpServletRequest req, ServletResponse response, SolrRequestHandler handler, SolrQueryRequest solrReq) throws IOException {
        SolrQueryResponse solrResp = new SolrQueryResponse();
        final NamedList<Object> responseHeader = new SimpleOrderedMap<Object>();
        solrResp.add("responseHeader", responseHeader);
        NamedList toLog = solrResp.getToLog();
        toLog.add("webapp", req.getContextPath());
        toLog.add("path", solrReq.getContext().get("path"));
        toLog.add("params", "{" + solrReq.getParamString() + "}");
        handler.handleRequest(solrReq, solrResp);
        SolrCore.setResponseHeaderValues(handler, solrReq, solrResp);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < toLog.size(); i++) {
            String name = toLog.getName(i);
            Object val = toLog.getVal(i);
            sb.append(name).append("=").append(val).append(" ");
        }
        QueryResponseWriter respWriter = SolrCore.DEFAULT_RESPONSE_WRITERS.get(solrReq.getParams().get(CommonParams.WT));
        if (respWriter == null) {
            respWriter = SolrCore.DEFAULT_RESPONSE_WRITERS.get("standard");
        }
        writeResponse(solrResp, response, respWriter, solrReq, Method.getMethod(req.getMethod()));
    }

    private void writeResponse(SolrQueryResponse solrRsp, ServletResponse response, QueryResponseWriter responseWriter, SolrQueryRequest solrReq, Method reqMethod) throws IOException {
        if (solrRsp.getException() != null) {
            sendError((HttpServletResponse) response, solrRsp.getException());
        } else {
            // Now write it out
            final String ct = responseWriter.getContentType(solrReq, solrRsp);
            // don't call setContentType on null
            if (null != ct) {
                response.setContentType(ct);
            }

            if (Method.HEAD != reqMethod) {
                if (responseWriter instanceof BinaryQueryResponseWriter) {
                    BinaryQueryResponseWriter binWriter = (BinaryQueryResponseWriter) responseWriter;
                    binWriter.write(response.getOutputStream(), solrReq, solrRsp);
                } else {
                    String charset = ContentStreamBase.getCharsetFromContentType(ct);
                    Writer out = (charset == null || charset.equalsIgnoreCase("UTF-8")) ? new OutputStreamWriter(
                        response.getOutputStream(),
                        UTF8) : new OutputStreamWriter(response.getOutputStream(), charset);
                    out = new FastWriter(out);
                    responseWriter.write(out, solrReq, solrRsp);
                    out.flush();
                }
            }
            // else http HEAD request, nothing to write out, waited this long just to get ContentType
        }
    }

    protected void execute(HttpServletRequest req, SolrRequestHandler handler, SolrQueryRequest sreq, SolrQueryResponse rsp) {
        // a custom filter could add more stuff to the request before passing it on.
        // for example: sreq.getContext().put( "HttpServletRequest", req );
        // used for logging query stats in SolrCore.execute()
        sreq.getContext().put("webapp", req.getContextPath());
        sreq.getCore().execute(handler, sreq, rsp);
    }

    protected void sendError(HttpServletResponse res, Throwable ex) throws IOException {
        int code = 500;
        String trace = "";
        if (ex instanceof SolrException) {
            code = ((SolrException) ex).code();
        }

        // For any regular code, don't include the stack trace
        if (code == 500 || code < 100) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            trace = "\n\n" + sw.toString();

//            FIXME:
//            SolrException.logOnce(log, null, ex);

            // non standard codes have undefined results with various servers
            if (code < 100) {
                log.warn("invalid return code: {}", code);
                code = 500;
            }
        }
        res.sendError(code, ex.getMessage() + trace);
    }

    @Override
    public String getServletInfo() {
        return "This is the solr servlet";
    }

    @Override
    public void destroy() {

    }

}
