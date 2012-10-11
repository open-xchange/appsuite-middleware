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

import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Date;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
import com.openexchange.json.OXJSONWriter;
import com.openexchange.log.LogFactory;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.multiple.MultipleHandler;
import com.openexchange.multiple.MultipleHandlerFactoryService;
import com.openexchange.multiple.PathAware;
import com.openexchange.multiple.internal.MultipleHandlerRegistry;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.OXJSONExceptionCodes;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSession;

public class Multiple extends SessionServlet {

    private static final long serialVersionUID = 3029074251138469122L;

    protected static final String MODULE = "module";

    protected static final String MODULE_INFOSTORE = "infostore";

    protected static final String MODULE_FOLDER = "folder";

    protected static final String MODULE_FOLDERS = "folders";

    private static final String ATTRIBUTE_MAIL_INTERFACE = "mi";

    private static final String ATTRIBUTE_MAIL_REQUEST = "mr";

    private static final transient Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(Multiple.class));

    private static volatile Dispatcher dispatcher;

    public static void setDispatcher(final Dispatcher dispatcher) {
        Multiple.dispatcher = dispatcher;
    }

    private static Dispatcher getDispatcher() {
        return dispatcher;
    }


    @Override
    protected void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        JSONArray dataArray;
        {
            final String data = getBody(req);
            try {
                dataArray = new JSONArray(data);
            } catch (final JSONException e) {
                final OXException exc = OXJSONExceptionCodes.JSON_READ_ERROR.create(e, data);
                LOG.warn(exc.getMessage() + Tools.logHeaderForError(req), exc);
                dataArray = new JSONArray();
            }
        }
        final JSONArray respArr = new JSONArray();
        final int length = dataArray.length();
        if (length > 0) {
            AJAXState state = null;
            try {
                final ServerSession session = getSessionObject(req);
                for (int a = 0; a < length; a++) {
                    state = parseActionElement(respArr, dataArray, a, session, req, state);
                }
                /*
                 * Don't forget to write mail request
                 */
                writeMailRequest(req);
            } catch (final JSONException e) {
                log(RESPONSE_ERROR, e);
                sendError(resp);
            } catch (final OXException e) {
                log(RESPONSE_ERROR, e);
                sendError(resp);
            } catch (final RuntimeException e) {
                log(RESPONSE_ERROR, e);
                sendError(resp);
            } finally {
                final MailServletInterface mi = (MailServletInterface) req.getAttribute(ATTRIBUTE_MAIL_INTERFACE);
                if (mi != null) {
                    try {
                        mi.close(true);
                    } catch (final OXException e) {
                        LOG.error(e.getMessage(), e);
                    }
                }
                if (state != null) {
                    getDispatcher().end(state);
                }
            }
        }
        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType(CONTENTTYPE_JAVASCRIPT);
        final Writer writer = resp.getWriter();
        writer.write(respArr.toString());
        writer.flush();
    }


    protected static final AJAXState parseActionElement(final JSONArray respArr, final JSONArray dataArray, final int pos, final ServerSession session, final HttpServletRequest req, final AJAXState state) throws JSONException, OXException {
        final JSONObject jsonObj = dataArray.getJSONObject(pos);

        final String module;
        final String action;

        if (jsonObj.has(MODULE)) {
            module = DataParser.checkString(jsonObj, MODULE);
        } else {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create( MODULE);
        }

        action = jsonObj.optString(PARAMETER_ACTION);

        final OXJSONWriter jWriter = new OXJSONWriter(respArr);

        return doAction(module, action, jsonObj, session, req, jWriter, state);
    }

    private static final void writeMailRequest(final HttpServletRequest req) throws JSONException {
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

    protected static final AJAXState doAction(final String module, final String action, final JSONObject jsonObj, final ServerSession session, final HttpServletRequest req, final OXJSONWriter jsonWriter, final AJAXState ajaxState) {
        AJAXState state = ajaxState;
        try {
            /*
             * Look up appropriate multiple handler first, then step through if-else-statement
             */
            {
                final HostnameService hostnameService = ServerServiceRegistry.getInstance().getService(HostnameService.class);
                if (null == hostnameService) {
                    jsonObj.put(MultipleHandler.HOSTNAME, req.getServerName());
                } else {
                    final String hn = hostnameService.getHostname(session.getUserId(), session.getContextId());
                    jsonObj.put(MultipleHandler.HOSTNAME, null == hn ? req.getServerName() : hn);
                }
            }
            jsonObj.put(MultipleHandler.ROUTE, Tools.getRoute(req.getSession(true).getId()));
            jsonObj.put(MultipleHandler.REMOTE_ADDRESS, req.getRemoteAddr());
            final Dispatcher dispatcher = getDispatcher();
            StringBuilder moduleCandidate = new StringBuilder();
            boolean handles = false;
            for (final String component : SPLIT.split(module, 0)) {
                moduleCandidate.append(component);
                handles = dispatcher.handles(moduleCandidate.toString());
                if (handles) {
                    break;
                }
                moduleCandidate.append('/');
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
                    LOG.error(e.getMessage(), e);
                    ResponseWriter.writeException(e, jsonWriter);
                    return state;
                } finally {
                	jsonWriter.endObject();
                }
                return state;
            }
            final MultipleHandler multipleHandler = lookUpMultipleHandler(module);
            if (null != multipleHandler) {
                writeMailRequest(req);
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
                        ResponseWriter.writeException(warnings.iterator().next(), jsonWriter);
                    }
                } catch (final OXException e) {
                    if (jsonWriter.isExpectingValue()) {
                        jsonWriter.value("");
                    }
                    ResponseWriter.writeException(e, jsonWriter);
                } catch (final JSONException e) {
                    final OXException oje = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
                    LOG.error(oje.getMessage(), oje);
                    if (jsonWriter.isExpectingValue()) {
                        jsonWriter.value("");
                    }
                    ResponseWriter.writeException(oje, jsonWriter);
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
                writeMailRequest(req);
                final FolderRequest folderequest = new FolderRequest(session, jsonWriter);
                try {
                    folderequest.action(action, jsonObj);
                } catch (final OXException e) {
                    LOG.error(e.getMessage(), e);
                    jsonWriter.object();
                    ResponseWriter.writeException(e, jsonWriter);
                    jsonWriter.endObject();
                } catch (final JSONException e) {
                    final OXException oje = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
                    LOG.error(oje.getMessage(), oje);
                    jsonWriter.object();
                    ResponseWriter.writeException(oje, jsonWriter);
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
                    LOG.error(e.getMessage(), e);
                    jsonWriter.object();
                    ResponseWriter.writeException(e, jsonWriter);
                    jsonWriter.endObject();
                } catch (final JSONException e) {
                    final OXException oje = OXJSONExceptionCodes.JSON_WRITE_ERROR.create(e);
                    LOG.error(oje.getMessage(), oje);
                    jsonWriter.object();
                    ResponseWriter.writeException(oje, jsonWriter);
                    jsonWriter.endObject();
                }
            } else if (MODULE_ATTACHMENTS.equals(module)) {
                    final AttachmentRequest request = new AttachmentRequest(session, jsonWriter);
                    request.action(action, new JSONSimpleRequest(jsonObj));
            } else {
                final OXException OXException = AjaxExceptionCodes.UNKNOWN_MODULE.create( module);
                LOG.error(OXException.getMessage(), OXException);
                jsonWriter.object();
                ResponseWriter.writeException(OXException, jsonWriter);
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

}
