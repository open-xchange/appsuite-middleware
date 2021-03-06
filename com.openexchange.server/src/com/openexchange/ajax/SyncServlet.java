/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.ajax;

import static com.openexchange.java.Autoboxing.I;
import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONWriter;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.helper.ParamContainer;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.api2.sync.FolderSyncInterface;
import com.openexchange.api2.sync.RdbFolderSyncInterface;
import com.openexchange.exception.Category;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.userconfiguration.UserConfigurationStorage;
import com.openexchange.java.AllocatingStringWriter;
import com.openexchange.mail.MailServletInterface;
import com.openexchange.monitoring.MonitoringInfo;
import com.openexchange.session.Session;
import com.openexchange.tools.oxfolder.OXFolderAccess;
import com.openexchange.tools.oxfolder.OXFolderExceptionCode;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link SyncServlet} - The AJAX servlet to serve SyncML requests
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public class SyncServlet extends PermissionServlet {

    private static final long serialVersionUID = 8749478304854849616L;

    private static final transient org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(SyncServlet.class);

    public static final String ACTION_REFRESH_SERVER = "refresh_server";

    /**
     * Default constructor
     */
    public SyncServlet() {
        super();
    }

    @Override
    protected void incrementRequests() {
        MonitoringInfo.incrementNumberOfConnections(MonitoringInfo.SYNCML);
    }

    @Override
    protected void decrementRequests() {
        MonitoringInfo.decrementNumberOfConnections(MonitoringInfo.SYNCML);
    }

    @Override
    protected boolean hasModulePermission(final ServerSession session) {
        return session.getUserPermissionBits().hasSyncML();
    }

    @Override
    protected void doPut(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        setDefaultContentType(resp);
        Tools.disableCaching(resp);
        try {
            actionPut(req, resp);
        } catch (OXException e) {
            switch (e.getCategories().get(0).getLogLevel()) {
                case TRACE:
                    LOG.trace("", e);
                    break;
                case DEBUG:
                    LOG.debug("", e);
                    break;
                case INFO:
                    LOG.info("", e);
                    break;
                case WARNING:
                    LOG.warn("", e);
                    break;
                case ERROR:
                    LOG.error("", e);
                    break;
                default:
                    break;
            }
            final Writer writer = resp.getWriter();
            final Response response = new Response();
            response.setException(e);
            try {
                ResponseWriter.write(response, writer, localeFrom(getSessionObject(req)));
            } catch (JSONException e1) {
                LOG.error("", e1);
            }
        } catch (Exception e) {
            final OXException wrapper = getWrappingOXException(e);
            LOG.error("", wrapper);
            final Writer writer = resp.getWriter();
            final Response response = new Response();
            response.setException(wrapper);
            try {
                ResponseWriter.write(response, writer, localeFrom(getSessionObject(req)));
            } catch (JSONException e1) {
                LOG.error("", e1);
            }
        }
    }

    /**
     * Assigns incoming PUT request to corresponding method
     */
    private final void actionPut(final HttpServletRequest req, final HttpServletResponse resp) throws IOException, OXException {
        final String actionStr = checkStringParam(req, PARAMETER_ACTION);
        if (actionStr.equalsIgnoreCase(ACTION_REFRESH_SERVER)) {
            actionPutClearFolderContent(req, resp);
        } else {
            throw getWrappingOXException(new Exception("Action \"" + actionStr + "\" NOT supported via PUT on module sync"));
        }
    }

    private final void actionPutClearFolderContent(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        try {
            actionPutClearFolderContent(getSessionObject(req), resp.getWriter(), getBody(req), ParamContainer.getInstance(req, resp));
        } catch (JSONException e) {
            writeErrorResponse((HttpServletResponseWrapper) resp, e, getSessionObject(req));
        }
    }

    private final void actionPutClearFolderContent(final Session sessionObj, final Writer writer, final String body, final ParamContainer paramContainer) throws JSONException, IOException {
        /*
         * Some variables
         */
        final Response response;
        try {
            response = new Response(sessionObj);
        } catch (OXException e1) {
            ResponseWriter.write(new Response().setException(e1), writer, localeFrom(sessionObj));
            return;
        }
        final AllocatingStringWriter strWriter = new AllocatingStringWriter();
        final JSONWriter jsonWriter = new JSONWriter(strWriter);
        Date lastModifiedDate = null;
        /*
         * Start response
         */
        jsonWriter.array();
        try {
            final Context ctx = ContextStorage.getStorageContext(sessionObj.getContextId());
            Date timestamp = null;
            final JSONArray jsonArr = new JSONArray(body);
            final int length = jsonArr.length();
            FolderSyncInterface folderSyncInterface = null;
            MailServletInterface mailInterface = null;
            try {
                long lastModified = 0;
                final OXFolderAccess access = new OXFolderAccess(ctx);
                NextId: for (int i = 0; i < length; i++) {
                    final String deleteIdentifier = jsonArr.getString(i);
                    int delFolderId = -1;
                    if ((delFolderId = com.openexchange.java.util.Tools.getUnsignedInteger(deleteIdentifier)) >= 0) {
                        if (timestamp == null) {
                            timestamp = paramContainer.checkDateParam(PARAMETER_TIMESTAMP);
                        }
                        if (folderSyncInterface == null) {
                            folderSyncInterface = new RdbFolderSyncInterface(sessionObj, ctx, access);
                        }
                        FolderObject delFolderObj;
                        try {
                            delFolderObj = access.getFolderObject(delFolderId);
                        } catch (OXException exc) {
                            LOG.warn("", exc);
                            continue NextId;
                        }
                        if (delFolderObj.getLastModified().getTime() > timestamp.getTime()) {
                            jsonWriter.value(delFolderObj.getObjectID());
                            continue NextId;
                        }
                        folderSyncInterface.clearFolder(delFolderObj, timestamp);
                        lastModified = Math.max(lastModified, delFolderObj.getLastModified().getTime());
                    } else if (deleteIdentifier.startsWith(FolderObject.SHARED_PREFIX)) {
                        throw OXFolderExceptionCode.NO_ADMIN_ACCESS.create(I(sessionObj.getUserId()), deleteIdentifier, Integer.valueOf(ctx.getContextId()));
                    } else {
                        if (UserConfigurationStorage.getInstance().getUserConfigurationSafe(sessionObj.getUserId(), ctx).hasWebMail()) {
                            if (mailInterface == null) {
                                mailInterface = MailServletInterface.getInstance(sessionObj);
                            }
                            mailInterface.clearFolder(deleteIdentifier);
                        } else {
                            jsonWriter.value(deleteIdentifier);
                        }
                    }
                }
                if (lastModified != 0) {
                    lastModifiedDate = new Date(lastModified);
                }
            } finally {
                if (mailInterface != null) {
                    mailInterface.close(true);
                    mailInterface = null;
                }
            }
        } catch (OXException e) {
            switch (e.getCategories().get(0).getLogLevel()) {
                case TRACE:
                    LOG.trace("", e);
                    break;
                case DEBUG:
                    LOG.debug("", e);
                    break;
                case INFO:
                    LOG.info("", e);
                    break;
                case WARNING:
                    LOG.warn("", e);
                    break;
                case ERROR:
                    LOG.error("", e);
                    break;
                default:
                    break;
            }

            if (!e.getCategory().equals(Category.CATEGORY_PERMISSION_DENIED)) {
                response.setException(e);
            }
        } catch (Exception e) {
            final OXException wrapper = getWrappingOXException(e);
            LOG.error("", wrapper);
            response.setException(wrapper);
        }
        /*
         * Close response and flush print writer
         */
        jsonWriter.endArray();
        response.setData(new JSONArray(strWriter.toString()));
        response.setTimestamp(lastModifiedDate);
        ResponseWriter.write(response, writer, localeFrom(sessionObj));
    }

    /*-
     * ++++++++++++++++++++++ Helper methods +++++++++++++++++++++++
     */

    private static final void writeErrorResponse(final HttpServletResponseWrapper resp, final Throwable e, final Session session) throws IOException {
        final OXException wrapper = getWrappingOXException(e);
        LOG.error("", wrapper);
        writeErrorResponse(resp, wrapper, session);
    }

    private static final void writeErrorResponse(final HttpServletResponseWrapper resp, final OXException e, final Session session) throws IOException {
        final Writer writer = resp.getWriter();
        final Response response = new Response();
        response.setException(e);
        try {
            ResponseWriter.write(response, writer, localeFrom(session));
        } catch (JSONException e1) {
            LOG.error("", e1);
        }
    }

    private static final OXException getWrappingOXException(final Throwable cause) {
        return AjaxExceptionCodes.UNEXPECTED_ERROR.create(cause, cause.getMessage());
    }

    private static final String checkStringParam(final HttpServletRequest req, final String paramName) throws OXException {
        final String paramVal = req.getParameter(paramName);
        if (paramVal == null) {
            throw OXFolderExceptionCode.MISSING_PARAMETER.create(paramName);
        }
        return paramVal;
    }
}
