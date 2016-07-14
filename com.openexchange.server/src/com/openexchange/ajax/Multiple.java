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

package com.openexchange.ajax;

import gnu.trove.ConcurrentTIntObjectHashMap;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.ajax.fields.RequestConstants;
import com.openexchange.ajax.fields.ResponseFields;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.ajax.request.AttachmentRequest;
import com.openexchange.ajax.request.FolderRequest;
import com.openexchange.ajax.request.JSONSimpleRequest;
import com.openexchange.ajax.request.MailRequest;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestDataTools;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.AJAXState;
import com.openexchange.ajax.requesthandler.Dispatcher;
import com.openexchange.ajax.requesthandler.Dispatchers;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.exception.OXException;
import com.openexchange.exception.OXExceptionStrings;
import com.openexchange.folderstorage.mail.MailFolderType;
import com.openexchange.groupware.notify.hostname.HostnameService;
import com.openexchange.java.Strings;
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
    private static final transient org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(Multiple.class);

    private static final String ACTION = PARAMETER_ACTION;
    private static final String PARENT = "parent";

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
        // Acquire session
        ServerSession session = getSessionObject(req);
        if (session == null) {
            OXException e = AjaxExceptionCodes.MISSING_PARAMETER.create(PARAMETER_SESSION);
            e.setDisplayMessage(OXExceptionStrings.BAD_REQUEST, new Object[0]);
            LOG.error("Missing '{}' parameter.", PARAMETER_SESSION, e);
            Tools.sendErrorPage(resp, HttpServletResponse.SC_BAD_REQUEST, e.getDisplayMessage(Locale.US));
            return;
        }

        // Parse request body into a JSON array
        JSONArray dataArray;
        {
            Reader reader = AJAXServlet.getReaderFor(req);
            try {
                dataArray = new JSONArray(reader);
            } catch (JSONException e) {
                OXException exc = OXJSONExceptionCodes.JSON_READ_ERROR.create(e, e.getMessage());
                exc.setDisplayMessage(OXExceptionStrings.BAD_REQUEST, new Object[0]);
                LOG.error("Received invalid JSON body in multiple request for user {} in context {} (exceptionId: {})", Integer.valueOf(session.getUserId()), Integer.valueOf(session.getContextId()), exc.getExceptionId(), e);
                Tools.sendErrorPage(resp, HttpServletResponse.SC_BAD_REQUEST, exc.getDisplayMessage(localeFrom(session)));
                return;
            }
        }

        // Handle parsed JSON array
        try {
            JSONArray respArr = perform(dataArray, req, session);
            resp.setStatus(HttpServletResponse.SC_OK);
            resp.setContentType(CONTENTTYPE_JAVASCRIPT);
            final Writer writer = resp.getWriter();
            writeTo(null == respArr ? new JSONArray(0) : respArr, writer);
            writer.flush();
        } catch (final JSONException e) {
            logError(RESPONSE_ERROR, session, e);
            sendError(resp);
        } catch (final OXException e) {
            logError(RESPONSE_ERROR, session, e);
            sendError(resp);
        } catch (final RuntimeException e) {
            logError(RESPONSE_ERROR, session, e);
            sendError(resp);
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
                CompletionService<Object> completionService = null;
                int concurrentTasksCount = 0;
                // Build-up mapping & schedule for either serial or concurrent execution
                final ConcurrentTIntObjectHashMap<JsonInOut> mapping = new ConcurrentTIntObjectHashMap<JsonInOut>(length);
                if (length > 1) {
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
                            if (null == completionService) {
                                final int concurrencyLevel = CONCURRENCY_LEVEL;
                                if (concurrencyLevel <= 0 || length <= concurrencyLevel) {
                                    completionService = new ThreadPoolCompletionService<Object>(ThreadPools.getThreadPool()).setTrackable(true);
                                } else {
                                    completionService = new BoundedCompletionService<Object>(ThreadPools.getThreadPool(), concurrencyLevel).setTrackable(true);
                                }
                            }
                            completionService.submit(new CallableImpl(jsonInOut, session, module, req));
                            concurrentTasksCount++;
                        }
                    }
                } else {
                    final int pos = 0;
                    final JSONObject dataObject = dataArray.getJSONObject(pos);
                    final JsonInOut jsonInOut = new JsonInOut(pos, dataObject);
                    mapping.put(pos, jsonInOut);
                    if (!dataObject.hasAndNotNull(MODULE)) {
                        throw AjaxExceptionCodes.MISSING_PARAMETER.create(MODULE);
                    }
                    serialTasks = new ArrayList<JsonInOut>(1);
                    serialTasks.add(jsonInOut);
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
                if (null != completionService) {
                    // Await completion service
                    awaitCompletionOfConcurrentTasks(completionService, concurrentTasksCount);
                }
                // Add single responses to JSON array
                for (int pos = 0; pos < length; pos++) {
                    final JsonInOut jsonInOut = mapping.get(pos);
                    if (null != jsonInOut) {
                        JSONValue outputObj = jsonInOut.getOutputObject();
                        if (null == outputObj) {
                            OXJSONWriter jsonWriter = new OXJSONWriter();
                            jsonWriter.object();
                            try {
                                ResponseWriter.writeException(OXException.general("Failed to handle JSON request: " + jsonInOut.getInputObject().toString()), jsonWriter, localeFrom(session), false);
                            } finally {
                                jsonWriter.endObject();
                            }
                            respArr.put(jsonWriter.getObject());
                        } else {
                            respArr.put(outputObj);
                        }
                    }
                }
            } finally {
                close((MailServletInterface) req.getAttribute(ATTRIBUTE_MAIL_INTERFACE));
                if (state != null) {
                    final Dispatcher dispatcher = getDispatcher();
                    if (null != dispatcher) {
                        dispatcher.end(state);
                    }
                }
            }
        }
        return respArr;
    }

    private static void awaitCompletionOfConcurrentTasks(final CompletionService<Object> completionService, final int concurrentTasksCount) throws OXException {
        for (int i = 0; i < concurrentTasksCount; i++) {
            try {
                completionService.take();
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
            }
        }
    }

    /** If a module identifier is contained in this set, serial execution is mandatory */
    private static final Set<String> SERIAL_MODULES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(MODULE_MAIL, "templating")));

    /** If a module identifier is contained in this set, serial execution is mandatory in case action hints to a {@link #MODIFYING_ACTIONS modifying operation} */
    private static final Set<String> SERIAL_ON_MODIFICATION_MODULES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(MODULE_CALENDAR, MODULE_TASK, MODULE_FOLDER, MODULE_FOLDERS, MODULE_CONTACT)));

    /** A set containing those actions that are considered as modifying */
    private static final Set<String> MODIFYING_ACTIONS = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList(ACTION_DELETE, ACTION_NEW, ACTION_UPDATE)));

    private static boolean indicatesSerial(final JSONObject dataObject) throws JSONException {
        // Retrieve module identifier
        final String module = Strings.toLowerCase(dataObject.getString(MODULE));

        if (null != module) {
            // Check for serial module; mail, templating, ...
            if (SERIAL_MODULES.contains(module)) {
                return true;
            }

            // Check for a folder action
            if (MODULE_FOLDERS.equals(module)) {
                // Check for either modifying operation or a mail folder list request
                final String action = Strings.toLowerCase(dataObject.optString(ACTION, null));
                if (MODIFYING_ACTIONS.contains(action) || isMailFolderList(action, dataObject.optString(PARENT, null))) {
                    return true;
                }
            } else {
                // Check for a modifying operation
                if (SERIAL_ON_MODIFICATION_MODULES.contains(module)) {
                    final String action = Strings.toLowerCase(dataObject.optString(ACTION, null));
                    return ((null != action) && MODIFYING_ACTIONS.contains(action));
                }
            }
        }

        // Does not require serial execution
        return false;
    }

    private static boolean isMailFolderList(final String action, final String parentId) {
        return ACTION_LIST.equals(action) && MailFolderType.getInstance().servesParentId(parentId);
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
                    final String hn;
                    if (session.getUser().isGuest()) {
                        hn = hostnameService.getGuestHostname(session.getUserId(), session.getContextId());
                    } else {
                        hn = hostnameService.getHostname(session.getUserId(), session.getContextId());
                    }
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
                } else if (action.equalsIgnoreCase(AJAXServlet.ACTION_GET) && MailRequest.isCollectableGet(jsonObj)) {
                    handles = false;
                }
            }
            if (handles) {
                final AJAXRequestData request = parse(req, moduleCandidate.toString(), module.substring(moduleCandidate.length()), action, jsonObj, session, Tools.considerSecure(req));
                jsonWriter.object();
                AJAXRequestResult requestResult = null;
                Exception exc = null;
                try {
                    if (action == null || action.length() == 0) {
                    	request.setAction("GET"); // Backwards Compatibility
                    }
                    if (state == null) {
                        state = dispatcher.begin();
                    }
                    requestResult = dispatcher.perform(request, state, session);

                    Date timestamp = requestResult.getTimestamp();
                    if (timestamp != null) {
                        jsonWriter.key(ResponseFields.TIMESTAMP);
                        jsonWriter.value(timestamp.getTime());
                    }

                    jsonWriter.key(ResponseFields.DATA);
                    jsonWriter.value(requestResult.getResultObject());

                    if (null != requestResult.getException()) {
                        boolean includeStackTraceOnError = AJAXRequestDataTools.parseBoolParameter("includeStackTraceOnError", request);
                        ResponseWriter.writeException(requestResult.getException(), jsonWriter, localeFrom(session), includeStackTraceOnError);
                    }

                    if (false == requestResult.getWarnings().isEmpty()) {
                        ResponseWriter.writeWarnings(new ArrayList<OXException>(requestResult.getWarnings()), jsonWriter, localeFrom(session));
                    }
                } catch (OXException e) {
                    exc = e;
                    logError(e.getMessage(), session, e);
                    ResponseWriter.writeException(e, jsonWriter, localeFrom(session), AJAXRequestDataTools.parseBoolParameter("includeStackTraceOnError", request));
                    return state;
                } catch (RuntimeException rte) {
                    exc = rte;
                    logError(rte.getMessage(), session, rte);
                    final OXException e = AjaxExceptionCodes.UNEXPECTED_ERROR.create(rte, rte.getMessage());
                    ResponseWriter.writeException(e, jsonWriter, localeFrom(session), AJAXRequestDataTools.parseBoolParameter("includeStackTraceOnError", request));
                    return state;
                } finally {
                	jsonWriter.endObject();
                	Dispatchers.signalDone(requestResult, exc);
                }
                return state;
            }
            boolean includeStackTraceOnError = jsonObj.optBoolean("includeStackTraceOnError", false);
            final MultipleHandler multipleHandler = lookUpMultipleHandler(module);
            if (null != multipleHandler) {
                try {
                    writeMailRequest(req);
                } catch (final OXException e) {
                    jsonWriter.object();
                    ResponseWriter.writeException(e, jsonWriter, localeFrom(session), includeStackTraceOnError);
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
                        ResponseWriter.writeException(warnings.iterator().next(), jsonWriter, localeFrom(session), includeStackTraceOnError);
                    }
                } catch (final OXException e) {
                    if (jsonWriter.isExpectingValue()) {
                        jsonWriter.value("");
                    }
                    ResponseWriter.writeException(e, jsonWriter, localeFrom(session), includeStackTraceOnError);
                } catch (final JSONException e) {
                    final OXException oje = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
                    logError(oje.getMessage(), session, oje);
                    if (jsonWriter.isExpectingValue()) {
                        jsonWriter.value("");
                    }
                    ResponseWriter.writeException(oje, jsonWriter, localeFrom(session), includeStackTraceOnError);
                } catch (final RuntimeException rte) {
                    logError(rte.getMessage(), session, rte);
                    final OXException e = AjaxExceptionCodes.UNEXPECTED_ERROR.create(rte, rte.getMessage());
                    if (jsonWriter.isExpectingValue()) {
                        jsonWriter.value("");
                    }
                    ResponseWriter.writeException(e, jsonWriter, localeFrom(session), includeStackTraceOnError);
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
                    ResponseWriter.writeException(e, jsonWriter, localeFrom(session), includeStackTraceOnError);
                    jsonWriter.endObject();
                }
                final FolderRequest folderequest = new FolderRequest(session, jsonWriter);
                try {
                    folderequest.action(action, jsonObj);
                } catch (final OXException e) {
                    logError(e.getMessage(), session, e);
                    jsonWriter.object();
                    ResponseWriter.writeException(e, jsonWriter, localeFrom(session), includeStackTraceOnError);
                    jsonWriter.endObject();
                } catch (final JSONException e) {
                    final OXException oje = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
                    logError(oje.getMessage(), session, oje);
                    jsonWriter.object();
                    ResponseWriter.writeException(oje, jsonWriter, localeFrom(session), includeStackTraceOnError);
                    jsonWriter.endObject();
                } catch (final RuntimeException rte) {
                    logError(rte.getMessage(), session, rte);
                    final OXException e = AjaxExceptionCodes.UNEXPECTED_ERROR.create(rte, rte.getMessage());
                    jsonWriter.object();
                    ResponseWriter.writeException(e, jsonWriter, localeFrom(session), includeStackTraceOnError);
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
                    ResponseWriter.writeException(e, jsonWriter, localeFrom(session), includeStackTraceOnError);
                    jsonWriter.endObject();
                } catch (final JSONException e) {
                    final OXException oje = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
                    logError(oje.getMessage(), session, oje);
                    jsonWriter.object();
                    ResponseWriter.writeException(oje, jsonWriter, localeFrom(session), includeStackTraceOnError);
                    jsonWriter.endObject();
                }
            } else if (MODULE_ATTACHMENTS.equals(module)) {
                    final AttachmentRequest request = new AttachmentRequest(session, jsonWriter);
                    request.action(action, new JSONSimpleRequest(jsonObj));
            } else {
                final OXException oxe = AjaxExceptionCodes.UNKNOWN_MODULE.create( module);
                logError(oxe.getMessage(), session, oxe);
                jsonWriter.object();
                ResponseWriter.writeException(oxe, jsonWriter, localeFrom(session), includeStackTraceOnError);
                jsonWriter.endObject();
            }
        } catch (final JSONException e) {
            /*
             * Cannot occur
             */
            LOG.error("", e);
        }
        return state;
    }

    /**
     * The pattern to split by commas.
     */
    private static final Pattern SPLIT_CSV = Pattern.compile("\\s*,\\s*");

    private static AJAXRequestData parse(HttpServletRequest servletRequest, String module, String path, String action, final JSONObject jsonObject, final ServerSession session, final boolean secure) throws JSONException {
        final AJAXRequestData request = new AJAXRequestData();
        request.setSecure(secure);

        /*
         * Check for decorators
         */
        if (jsonObject.hasAndNotNull("decorators")) {
            final String parameter = jsonObject.getString("decorators");
            if (null != parameter) {
                for (final String id : SPLIT_CSV.split(parameter, 0)) {
                    request.addDecoratorId(id.trim());
                }
            }
        }

        request.setHttpServletRequest(servletRequest);
        request.setHostname(jsonObject.getString(HOSTNAME));
        request.setRoute(jsonObject.getString(ROUTE));
        request.setRemoteAddress(jsonObject.getString(REMOTE_ADDRESS));
        request.setPrefix(Dispatchers.getPrefix());

        for (final Entry<String, Object> entry : jsonObject.entrySet()) {
            final String name = entry.getKey();
            if (RequestConstants.DATA.equals(name)) {
                request.setData(entry.getValue());
            } else {
                final Object value = entry.getValue();
                if (value != null && value != JSONObject.NULL) {
                    request.putParameter(name, entry.getValue().toString());
                }
            }
        }
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        request.setModule(module);
        request.setFormat("json");
        request.setAction(action);
        request.setServletRequestURI(path);
        request.setPathInfo(path);
        request.setSession(session);

        return request;
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

    /** Writes JSON value to given writer. */
    private static void writeTo(final JSONValue jValue, final Writer writer) throws IOException {
        try {
            jValue.write(writer);
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
                LOG.error("", e);
            }
        }
    }

    private static void logError(final Object message, final Session session, final Exception e) {
        LogProperties.putSessionProperties(session);
        LOG.error(message.toString(), e);
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
