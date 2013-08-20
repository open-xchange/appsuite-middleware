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

package com.openexchange.ajax;

import gnu.trove.ConcurrentTIntObjectHashMap;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.ajax.fields.ResponseFields;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.ajax.request.AttachmentRequest;
import com.openexchange.ajax.request.FolderRequest;
import com.openexchange.ajax.request.JSONSimpleRequest;
import com.openexchange.ajax.request.MailRequest;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.AJAXState;
import com.openexchange.ajax.requesthandler.Dispatcher;
import com.openexchange.ajax.requesthandler.MultipleAdapter;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.java.Streams;
import com.openexchange.json.OXJSONWriter;
import com.openexchange.log.LogProperties;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.multiple.MultipleHandler;
import com.openexchange.multiple.MultipleHandlerFactoryService;
import com.openexchange.multiple.PathAware;
import com.openexchange.multiple.internal.MultipleHandlerRegistry;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.threadpool.BoundedCompletionService;
import com.openexchange.threadpool.ThreadPoolCompletionService;
import com.openexchange.threadpool.ThreadPools;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSession;

/**
 * The <tt>Multiple</tt> Servlet processes <a href="http://oxpedia.org/wiki/index.php?title=HTTP_API#Module_.22multiple.22">multiple incoming JSON</a> requests.
 */
public class Multiple extends SessionServlet {

    private static final long serialVersionUID = 3029074251138469122L;
    private static final transient Log LOG = com.openexchange.log.Log.loggerFor(Multiple.class);

    private static final String ACTION = PARAMETER_ACTION;

    protected static final String MODULE = "module";

    // protected static final String MODULE_INFOSTORE = "infostore";

    protected static final String MODULE_FOLDER = "folder";

    protected static final String MODULE_FOLDERS = "folders";

    private static final String ATTRIBUTE_MAIL_INTERFACE = "mi";

    private static final String ATTRIBUTE_MAIL_REQUEST = "mr";

    private static volatile Dispatcher dispatcher;

    /**
     * Sets the dispatcher instance.
     *
     * @param dispatcher The dispatcher to set
     */
    public static void setDispatcher(final Dispatcher dispatcher) {
        Multiple.dispatcher = dispatcher;
    }

    /**
     * Gets the dispatcher instance
     *
     * @return The dispatcher instance or <code>null</code>
     */
    private static Dispatcher getDispatcher() {
        return dispatcher;
    }

    @Override
    protected void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        JSONArray dataArray;
        {
            final Reader reader = AJAXServlet.getReaderFor(req);
            try {
                dataArray = new JSONArray(reader);
            } catch (final JSONException e) {
                final OXException exc = OXJSONExceptionCodes.JSON_READ_ERROR.create(e, e.getMessage());
                LOG.warn(exc.getMessage() + Tools.logHeaderForError(req), exc);
                dataArray = new JSONArray();
            } finally {
                Streams.close(reader);
            }
        }
        // Aquire session
        final ServerSession session = getSessionObject(req);
        if (session == null) {
            final OXException e = AjaxExceptionCodes.MISSING_PARAMETER.create(PARAMETER_SESSION);
            log(RESPONSE_ERROR, e);
            sendError(resp);
        }
        try {
            // Process multiple request
            JSONArray respArr = null;
            try {
                respArr = perform(dataArray, req, session);
            } catch (final JSONException e) {
                logError(RESPONSE_ERROR, session, e);
                sendError(resp);
            } catch (final OXException e) {
                logError(RESPONSE_ERROR, session, e);
                sendError(resp);
            } catch (final RuntimeException e) {
                logError(RESPONSE_ERROR, session, e);
                sendError(resp);
            }
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType(CONTENTTYPE_JAVASCRIPT);
            final Writer writer = resp.getWriter();
            writeTo(null == respArr ? new JSONArray(0) : respArr, writer);
            writer.flush();
        } finally {
            LogProperties.removeLogProperties();
        }
    }

    /** The concurrency level for processing multiple requests */
    private static final int CONCURRENCY_LEVEL = 5;

    public static JSONArray perform(JSONArray dataArray, HttpServletRequest req, ServerSession session) throws OXException, JSONException {
    	final int length = dataArray.length();
        final JSONArray respArr = new JSONArray(length);
        if (length > 0) {
            AJAXState state = null;
            try {
                // Distinguish between serially and concurrently executable requests
                List<JsonInOut> serialTasks = null;
                CompletionService<Object> concurrentTasks = null;
                int concurrentTasksCount = 0;
                // Build-up mapping & schedule for either serial or concurrent execution
                final ConcurrentTIntObjectHashMap<JsonInOut> mapping = new ConcurrentTIntObjectHashMap<JsonInOut>(length);
                for (int pos = 0; pos < length; pos++) {
                    final JSONObject dataObject = dataArray.getJSONObject(pos);
                    final JsonInOut jsonInOut = new JsonInOut(pos, dataObject);
                    mapping.put(pos, jsonInOut);
                    if (!dataObject.hasAndNotNull(MODULE)) {
                        throw AjaxExceptionCodes.MISSING_PARAMETER.create(MODULE);
                    }
                    // Check if module indicates serial or concurrent execution
                    final String module = dataObject.getString(MODULE);
                    if (indicatesSerial(dataObject)) {
                        if (null == serialTasks) {
                            serialTasks = new ArrayList<JsonInOut>(length);
                        }
                        serialTasks.add(jsonInOut);
                    } else {
                        if (null == concurrentTasks) {
                            final int concurrencyLevel = CONCURRENCY_LEVEL;
                            if (concurrencyLevel <= 0 || length <= concurrencyLevel) {
                                concurrentTasks = new ThreadPoolCompletionService<Object>(ThreadPools.getThreadPool()).setTrackable(true);
                            } else {
                                concurrentTasks = new BoundedCompletionService<Object>(ThreadPools.getThreadPool(), concurrencyLevel).setTrackable(true);
                            }
                        }
                        concurrentTasks.submit(new CallableImpl(jsonInOut, session, module, req));
                        concurrentTasksCount++;
                    }
                }
                if (null != serialTasks) {
                    final int size = serialTasks.size();
                    final JSONArray serialResponses = new JSONArray(size);
                    // Execute serial tasks with current thread
                    for (final JsonInOut jsonInOut : serialTasks) {
                        state = parseActionElement(jsonInOut.getInputObject(), serialResponses, session, req, state);
                    }
                    // Don't forget to write mail request
                    writeMailRequest(req);
                    // Fill responses
                    for (int i = 0; i < size; i++) {
                        serialTasks.get(i).setOutputObject((JSONValue) serialResponses.get(i));
                    }
                }
                if (null != concurrentTasks) {
                    // Await completion service
                    for (int i = 0; i < concurrentTasksCount; i++) {
                        try {
                            concurrentTasks.take();
                        } catch (final InterruptedException e) {
                            Thread.currentThread().interrupt();
                            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
                        }
                    }
                }
                // Add single responses to JSON array
                for (int pos = 0; pos < length; pos++) {
                    final JsonInOut jsonInOut = mapping.get(pos);
                    if (null != jsonInOut) {
                        respArr.put(jsonInOut.getOutputObject());
                    }
                }
            } finally {
                close((MailServletInterface) req.getAttribute(ATTRIBUTE_MAIL_INTERFACE));
                if (state != null) {
                    getDispatcher().end(state);
                }
            }
        }
        return respArr;
    }

    private static boolean indicatesSerial(JSONObject dataObject) throws JSONException {
        String module = dataObject.getString(MODULE);
        if (module.equals(MODULE_MAIL)) {
            return true;
        }

        if (module.equals(MODULE_CALENDAR) && dataObject.hasAndNotNull(ACTION) && dataObject.getString(ACTION).equals(ACTION_DELETE)) {
            return true;
        }

        return false;
    }

    protected static final void performActionElement(final JsonInOut jsonInOut, final String module, final ServerSession session, final HttpServletRequest req) {
        AJAXState ajaxState = null;
        try {
            final OXJSONWriter jWriter = new OXJSONWriter();
            final JSONObject inObject = jsonInOut.getInputObject();
            ajaxState = doAction(module, inObject.optString(ACTION), inObject, session, req, jWriter, null);
            jsonInOut.setOutputObject(jWriter.getObject());
        } finally {
            if (null != ajaxState) {
                ajaxState.close();
            }
        }
    }

    protected static final AJAXState parseActionElement(final JSONObject inObject, final JSONArray serialResponses, final ServerSession session, final HttpServletRequest req, final AJAXState state) throws OXException {
        try {
            return doAction(DataParser.checkString(inObject, MODULE), inObject.optString(ACTION), inObject, session, req, new OXJSONWriter(serialResponses), state);
        } catch (final JSONException e) {
            throw AjaxExceptionCodes.JSON_ERROR.create(e, e.getMessage());
        }
    }

    private static final void writeMailRequest(final HttpServletRequest req) throws OXException {
        final MailRequest mailReq = (MailRequest) req.getAttribute(ATTRIBUTE_MAIL_REQUEST);
        if (mailReq != null) {
            try {
                /*
                 * Write withheld mail response first
                 */
                mailReq.performMultiple((MailServletInterface) req.getAttribute(ATTRIBUTE_MAIL_INTERFACE));
            } finally {
                /*
                 * Remove mail request object
                 */
                req.setAttribute(ATTRIBUTE_MAIL_REQUEST, null);
            }
        }
    }

    private static final Pattern SPLIT = Pattern.compile("/");

    private static final String HOSTNAME = MultipleHandler.HOSTNAME;
    private static final String ROUTE = MultipleHandler.ROUTE;
    private static final String REMOTE_ADDRESS = MultipleHandler.REMOTE_ADDRESS;

    protected static final AJAXState doAction(final String module, final String action, final JSONObject jsonObj, final ServerSession session, final HttpServletRequest req, final OXJSONWriter jsonWriter, final AJAXState ajaxState) {
        AJAXState state = ajaxState;
        try {
            /*
             * Look up appropriate multiple handler first, then step through if-else-statement
             */
            {
                final HostnameService hostnameService = ServerServiceRegistry.getInstance().getService(HostnameService.class);
                if (null == hostnameService) {
                    jsonObj.put(HOSTNAME, req.getServerName());
                } else {
                    final String hn = hostnameService.getHostname(session.getUserId(), session.getContextId());
                    jsonObj.put(HOSTNAME, null == hn ? req.getServerName() : hn);
                }
            }
            jsonObj.put(ROUTE, Tools.getRoute(req.getSession(true).getId()));
            jsonObj.put(REMOTE_ADDRESS, req.getRemoteAddr());
            final Dispatcher dispatcher = getDispatcher();
            final StringBuilder moduleCandidate = new StringBuilder(32);
            boolean handles = false;
            for (final String component : SPLIT.split(module, 0)) {
                moduleCandidate.append(component);
                handles = dispatcher.handles(moduleCandidate.toString());
                if (handles) {
                    break;
                }
                moduleCandidate.append('/');
            }
            if (MODULE_MAIL.equals(module)) {
                if (action.equalsIgnoreCase(AJAXServlet.ACTION_UPDATE)) {
                    if (MailRequest.isMove(jsonObj)) {
                        handles = false;
                    } else if (MailRequest.isStoreFlags(jsonObj)) {
                        handles = false;
                    } else if (MailRequest.isColorLabel(jsonObj)) {
                        handles = false;
                    }
                } else if (action.equalsIgnoreCase(AJAXServlet.ACTION_COPY)) {
                    handles = false;
                }
            }
            if (handles) {
                final AJAXRequestData request = MultipleAdapter.parse(moduleCandidate.toString(), module.substring(moduleCandidate.length()), action, jsonObj, session, Tools.considerSecure(req));
                jsonWriter.object();
                final AJAXRequestResult result;
                try {
                    if (action == null || action.length() == 0) {
                    	request.setAction("GET"); // Backwards Compatibility
                    }
                    if (state == null) {
                        state = dispatcher.begin();
                    }
                    result = dispatcher.perform(request, state, session);

                    if (result.getTimestamp() != null) {
                        jsonWriter.key(ResponseFields.TIMESTAMP);
                        jsonWriter.value(result.getTimestamp().getTime());
                    }
                    jsonWriter.key(ResponseFields.DATA);
                    jsonWriter.value(result.getResultObject());
                } catch (final OXException e) {
                    logError(e.getMessage(), session, e);
                    ResponseWriter.writeException(e, jsonWriter, localeFrom(session));
                    return state;
                } catch (final RuntimeException rte) {
                    logError(rte.getMessage(), session, rte);
                    final OXException e = AjaxExceptionCodes.UNEXPECTED_ERROR.create(rte, rte.getMessage());
                    ResponseWriter.writeException(e, jsonWriter, localeFrom(session));
                    return state;
                } finally {
                	jsonWriter.endObject();
                }
                return state;
            }
            final MultipleHandler multipleHandler = lookUpMultipleHandler(module);
            if (null != multipleHandler) {
                try {
                    writeMailRequest(req);
                } catch (final OXException e) {
                    jsonWriter.object();
                    ResponseWriter.writeException(e, jsonWriter, localeFrom(session));
                    jsonWriter.endObject();
                }
                jsonWriter.object();
                try {
                    final Object tmp = multipleHandler.performRequest(action, jsonObj, session, Tools.considerSecure(req));
                    jsonWriter.key(ResponseFields.DATA);
                    jsonWriter.value(tmp);
                    final Date timestamp = multipleHandler.getTimestamp();
                    if (null != timestamp) {
                        jsonWriter.key(ResponseFields.TIMESTAMP).value(timestamp.getTime());
                    }
                    final Collection<OXException> warnings = multipleHandler.getWarnings();
                    if (null != warnings && !warnings.isEmpty()) {
                        ResponseWriter.writeException(warnings.iterator().next(), jsonWriter, localeFrom(session));
                    }
                } catch (final OXException e) {
                    if (jsonWriter.isExpectingValue()) {
                        jsonWriter.value("");
                    }
                    ResponseWriter.writeException(e, jsonWriter, localeFrom(session));
                } catch (final JSONException e) {
                    final OXException oje = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
                    logError(oje.getMessage(), session, oje);
                    if (jsonWriter.isExpectingValue()) {
                        jsonWriter.value("");
                    }
                    ResponseWriter.writeException(oje, jsonWriter, localeFrom(session));
                } catch (final RuntimeException rte) {
                    logError(rte.getMessage(), session, rte);
                    final OXException e = AjaxExceptionCodes.UNEXPECTED_ERROR.create(rte, rte.getMessage());
                    if (jsonWriter.isExpectingValue()) {
                        jsonWriter.value("");
                    }
                    ResponseWriter.writeException(e, jsonWriter, localeFrom(session));
                } finally {
                    multipleHandler.close();
                    jsonWriter.endObject();
                }
            /*} else if (MODULE_INFOSTORE.equals(module)) {
                writeMailRequest(req);
                final InfostoreRequest infoRequest = new InfostoreRequest(session, jsonWriter);
                try {
                    infoRequest.action(action, new JSONSimpleRequest(jsonObj));
                } catch (final OXPermissionException e) {
                    jsonWriter.object();
                    ResponseWriter.writeException(e, jsonWriter);
                    jsonWriter.endObject();
                } */
            } else if (MODULE_FOLDER.equals(module) || MODULE_FOLDERS.equals(module)) {
                try {
                    writeMailRequest(req);
                } catch (final OXException e) {
                    jsonWriter.object();
                    ResponseWriter.writeException(e, jsonWriter, localeFrom(session));
                    jsonWriter.endObject();
                }
                final FolderRequest folderequest = new FolderRequest(session, jsonWriter);
                try {
                    folderequest.action(action, jsonObj);
                } catch (final OXException e) {
                    logError(e.getMessage(), session, e);
                    jsonWriter.object();
                    ResponseWriter.writeException(e, jsonWriter, localeFrom(session));
                    jsonWriter.endObject();
                } catch (final JSONException e) {
                    final OXException oje = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
                    logError(oje.getMessage(), session, oje);
                    jsonWriter.object();
                    ResponseWriter.writeException(oje, jsonWriter, localeFrom(session));
                    jsonWriter.endObject();
                } catch (final RuntimeException rte) {
                    logError(rte.getMessage(), session, rte);
                    final OXException e = AjaxExceptionCodes.UNEXPECTED_ERROR.create(rte, rte.getMessage());
                    jsonWriter.object();
                    ResponseWriter.writeException(e, jsonWriter, localeFrom(session));
                    jsonWriter.endObject();
                }
            } else if (MODULE_MAIL.equals(module)) {
                try {
                    /*
                     * Fetch or create mail request object
                     */
                    final boolean storeMailRequest;
                    final MailRequest mailrequest;
                    Object tmp = req.getAttribute(ATTRIBUTE_MAIL_REQUEST);
                    if (tmp == null) {
                        mailrequest = new MailRequest(session, jsonWriter);
                        storeMailRequest = true;
                    } else {
                        mailrequest = (MailRequest) tmp;
                        storeMailRequest = false;
                    }
                    /*
                     * Fetch or create mail interface object
                     */
                    final MailServletInterface mi;
                    tmp = req.getAttribute(ATTRIBUTE_MAIL_INTERFACE);
                    if (tmp == null) {
                        mi = MailServletInterface.getInstance(session);
                        req.setAttribute(ATTRIBUTE_MAIL_INTERFACE, mi);
                    } else {
                        mi = ((MailServletInterface) tmp);
                    }
                    mailrequest.action(action, jsonObj, mi);
                    if (mailrequest.isContiguousCollect()) {
                        /*
                         * Put into attributes to further collect move/copy requests and return a null reference to avoid writing response
                         * object
                         */
                        if (storeMailRequest) {
                            req.setAttribute(ATTRIBUTE_MAIL_REQUEST, mailrequest);
                        }
                        return state;
                    }
                } catch (final OXException e) {
                    logError(e.getMessage(), session, e);
                    jsonWriter.object();
                    ResponseWriter.writeException(e, jsonWriter, localeFrom(session));
                    jsonWriter.endObject();
                } catch (final JSONException e) {
                    final OXException oje = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
                    logError(oje.getMessage(), session, oje);
                    jsonWriter.object();
                    ResponseWriter.writeException(oje, jsonWriter, localeFrom(session));
                    jsonWriter.endObject();
                }
            } else if (MODULE_ATTACHMENTS.equals(module)) {
                    final AttachmentRequest request = new AttachmentRequest(session, jsonWriter);
                    request.action(action, new JSONSimpleRequest(jsonObj));
            } else {
                final OXException oxe = AjaxExceptionCodes.UNKNOWN_MODULE.create( module);
                logError(oxe.getMessage(), session, oxe);
                jsonWriter.object();
                ResponseWriter.writeException(oxe, jsonWriter, localeFrom(session));
                jsonWriter.endObject();
            }
        } catch (final JSONException e) {
            /*
             * Cannot occur
             */
            LOG.error(e.getMessage(), e);
        }
        return state;
    }

    private static MultipleHandler lookUpMultipleHandler(final String module) {
        final MultipleHandlerRegistry registry = ServerServiceRegistry.getInstance().getService(MultipleHandlerRegistry.class);
        if (null != registry) {
            final MultipleHandlerFactoryService factoryService = registry.getFactoryService(module);
            if (null != factoryService) {
                final MultipleHandler multipleHandler = factoryService.createMultipleHandler();
                if(PathAware.class.isInstance(multipleHandler)) {
                    final PathAware pa = (PathAware) multipleHandler;
                    pa.setPath(module.substring(factoryService.getSupportedModule().length()));
                }
                return multipleHandler;
            }
        }
        return null;
    }

    /** Writes JSON array to given writer. */
    private static void writeTo(final JSONArray respArr, final Writer writer) throws IOException {
        try {
            respArr.write(writer);
        } catch (final JSONException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    /** Close passed {@link MailServletInterface} instance. */
    private static void close(final MailServletInterface mi) {
        if (mi != null) {
            try {
                mi.close(true);
            } catch (final Exception e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    private static void logError(final Object message, final Session session, final Exception e) {
        LogProperties.putSessionProperties(session);
        LOG.error(message, e);
    }

    private static final class CallableImpl implements Callable<Object> {

        private final JsonInOut jsonDataResponse;
        private final ServerSession session;
        private final String module;
        private final HttpServletRequest req;

        protected CallableImpl(final JsonInOut jsonDataResponse, final ServerSession session, final String module, final HttpServletRequest req) {
            super();
            this.jsonDataResponse = jsonDataResponse;
            this.session = session;
            this.module = module;
            this.req = req;
        }

        @Override
        public Object call() throws OXException {
            try {
                performActionElement(jsonDataResponse, module, session, req);
                return null;
            } catch (final RuntimeException e) {
                throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
    } // End of class

    private static final class JsonInOut {

        private final int pos;
        private final JSONObject inObject;
        private volatile JSONValue outObject;

        protected JsonInOut(final int pos, final JSONObject inObject) {
            super();
            this.pos = pos;
            this.inObject = inObject;
        }

        /**
         * Gets the (zero-based) position.
         *
         * @return The (zero-based) position
         */
        public int getPos() {
            return pos;
        }

        /**
         * Gets the input object
         *
         * @return The input object
         */
        public JSONObject getInputObject() {
            return inObject;
        }

        /**
         * Gets the output object
         *
         * @return The output object
         */
        public JSONValue getOutputObject() {
            return outObject;
        }

        /**
         * Sets the output object
         *
         * @param outObject The output object to set
         */
        public void setOutputObject(final JSONValue outObject) {
            this.outObject = outObject;
        }

    } // End of class JsonDataResponse

}
