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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.logging.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.fields.ResponseFields;
import com.openexchange.ajax.helper.DownloadUtility;
import com.openexchange.ajax.helper.DownloadUtility.CheckedDownload;
import com.openexchange.ajax.parser.InfostoreParser;
import com.openexchange.ajax.parser.InfostoreParser.UnknownMetadataException;
import com.openexchange.ajax.request.InfostoreRequest;
import com.openexchange.ajax.request.ServletRequestAdapter;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.database.provider.DBPoolProvider;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.InfostoreSearchEngine;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.groupware.infostore.facade.impl.InfostoreFacadeImpl;
import com.openexchange.groupware.infostore.facade.impl.VirtualFolderInfostoreFacade;
import com.openexchange.groupware.infostore.search.impl.SearchEngineImpl;
import com.openexchange.groupware.infostore.utils.GetSwitch;
import com.openexchange.groupware.infostore.utils.InfostoreConfigUtils;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.infostore.utils.SetSwitch;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.upload.UploadFile;
import com.openexchange.groupware.upload.impl.UploadEvent;
import com.openexchange.groupware.upload.impl.UploadException;
import com.openexchange.groupware.upload.impl.UploadSizeExceededException;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.java.AllocatingStringWriter;
import com.openexchange.java.Streams;
import com.openexchange.json.OXJSONWriter;
import com.openexchange.log.LogFactory;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.mail.usersetting.UserSettingMailStorage;
import com.openexchange.session.Session;
import com.openexchange.sessiond.impl.ThreadLocalSessionHolder;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.UploadServletException;
import com.openexchange.tools.servlet.http.Tools;
import com.openexchange.tools.session.ServerSession;

public class Infostore extends PermissionServlet {

    private static final String STR_JSON = "json";

    private static final String STR_ERROR = "error";

    private static final String STR_ACTION = "action";

    private static final String MIME_TEXT_HTML = "text/html";

    private static final long serialVersionUID = 2674990072903834660L;

    private static final InfostoreParser PARSER = new InfostoreParser();

    public static final InfostoreFacade VIRTUAL_FACADE = new VirtualFolderInfostoreFacade();

    public static final InfostoreFacade FACADE = new InfostoreFacadeImpl(new DBPoolProvider());
    static {
        FACADE.setTransactional(true);
        FACADE.setSessionHolder(ThreadLocalSessionHolder.getInstance());
    }

    public static final InfostoreSearchEngine SEARCH_ENGINE = new SearchEngineImpl(new DBPoolProvider());
    static {
        SEARCH_ENGINE.setTransactional(true);
    }

    // public static final Exception2Message OXEXCEPTION_HANDLER = new
    // InfostoreException2Message();

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(Infostore.class));

    private final long maxUploadSize = -1;

    // TODO: Better error handling

    @Override
    protected boolean hasModulePermission(final ServerSession session) {
        return InfostoreRequest.hasPermission(session.getUserPermissionBits());
    }

    @Override
    protected void doGet(final HttpServletRequest req, final HttpServletResponse res) throws ServletException, IOException {

        final ServerSession session = getSessionObject(req);
        ThreadLocalSessionHolder.getInstance().setSession(session);

        final Context ctx = session.getContext();
        final User user = session.getUser();
        final UserPermissionBits userConfig = session.getUserPermissionBits();

        final String action = req.getParameter(PARAMETER_ACTION);
        if (action == null) {
            missingParameter(PARAMETER_ACTION, res, false, null);
            return;
        }

        if (action.equals(ACTION_DOCUMENT)) {
            if (req.getParameter(PARAMETER_ID) == null) {
                final Response resp = new Response(session);
                resp.setException(AjaxExceptionCodes.UNEXPECTED_ERROR.create("You must provide a value for " + PARAMETER_ID));
                final AllocatingStringWriter w = new AllocatingStringWriter();
                try {
                    ResponseWriter.write(resp, w, localeFrom(session));
                } catch (final JSONException e) {
                    // shouldn't happen
                    final ServletException se = new ServletException(e);
                    se.initCause(e);
                    throw se;
                }
                res.setContentType(MIME_TEXT_HTML);
                res.getWriter().write(substituteJS(w.toString(), STR_ERROR));
            }
            int id;
            try {
                id = Integer.parseInt(req.getParameter(PARAMETER_ID));
            } catch (final NumberFormatException x) {
                handleOXException(res, AjaxExceptionCodes.IMVALID_PARAMETER.create(PARAMETER_ID), STR_ERROR, true, session);
                return;
            }

            final String versionS = req.getParameter(PARAMETER_VERSION);
            final int version = (versionS == null) ? InfostoreFacade.CURRENT_VERSION : Integer.parseInt(versionS);

            final String contentType = req.getParameter(PARAMETER_CONTENT_TYPE);

            document(res, req.getHeader("user-agent"), id, version, contentType, ctx, user, userConfig, session);

            return;
        }
        final OXJSONWriter writer = new OXJSONWriter();
        final InfostoreRequest request = new InfostoreRequest(session, writer);
        try {
            if (!request.action(action, new ServletRequestAdapter(req, res))) {
                unknownAction("GET", action, res, false);
                return;
            }
            ((JSONObject) writer.getObject()).write(res.getWriter());
        } catch (final JSONException e) {
            if (e.getCause() instanceof IOException) {
                /*
                 * Throw proper I/O error since a serious socket error could been occurred which prevents further communication. Just
                 * throwing a JSON error possibly hides this fact by trying to write to/read from a broken socket connection.
                 */
                throw (IOException) e.getCause();
            }
            LOG.error(e.getMessage(), e);
        } catch (final OXException e) {
            LOG.error("Not possible, obviously: " + e.getMessage(), e);
        } finally {
            ThreadLocalSessionHolder.getInstance().clear();
        }
    }

    @Override
    protected void doPut(final HttpServletRequest req, final HttpServletResponse res) throws ServletException, IOException {
        final ServerSession session = getSessionObject(req);
        ThreadLocalSessionHolder.getInstance().setSession(session);

        final String action = req.getParameter(PARAMETER_ACTION);
        if (action == null) {
            missingParameter(PARAMETER_ACTION, res, false, null);
            return;
        }
        final OXJSONWriter writer = new OXJSONWriter();
        final InfostoreRequest request = new InfostoreRequest(session, writer);
        try {
            if (!request.action(action, new ServletRequestAdapter(req, res))) {
                unknownAction("PUT", action, res, false);
                return;
            }
            if (writer.isJSONObject()) {
                ((JSONObject) writer.getObject()).write(res.getWriter());
            } else if (writer.isJSONArray()) {
                res.getWriter().print(writer.getObject().toString());
            }
        } catch (final JSONException e) {
            if (e.getCause() instanceof IOException) {
                /*
                 * Throw proper I/O error since a serious socket error could been occurred which prevents further communication. Just
                 * throwing a JSON error possibly hides this fact by trying to write to/read from a broken socket connection.
                 */
                throw (IOException) e.getCause();
            }
            LOG.error(e.getMessage(), e);
        } catch (final OXException e) {
            LOG.error("Not possible, obviously: " + e.getMessage(), e);
        } catch (final Throwable t) {
            LOG.error(t.getMessage(), t);
        } finally {
            ThreadLocalSessionHolder.getInstance().clear();
        }
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse res) throws ServletException, IOException {

        final ServerSession session = getSessionObject(req);
        ThreadLocalSessionHolder.getInstance().setSession(session);

        final Context ctx = session.getContext();
        final User user = session.getUser();
        final UserPermissionBits userConfig = session.getUserPermissionBits();

        final String action = req.getParameter(PARAMETER_ACTION);
        if (action == null) {
            missingParameter(PARAMETER_ACTION, res, true, "new");
            return;
        }

        try {
            checkSize(req.getContentLength(), UserSettingMailStorage.getInstance().getUserSettingMail(
                session.getUserId(),
                session.getContext()));
            if (action.equals(ACTION_NEW) || action.equals(ACTION_UPDATE) || action.equals(ACTION_COPY)) {
                UploadEvent upload = null;
                try {
                    upload = processUpload(req);
                    final UploadFile uploadFile;
                    {
                        final List<UploadFile> list = upload.getUploadFilesByFieldName("file");
                        uploadFile = null == list || list.isEmpty() ? null : list.get(0);
                    }

                    if (null != uploadFile) {
                        checkSize(uploadFile.getSize(), UserSettingMailStorage.getInstance().getUserSettingMail(
                            session.getUserId(),
                            session.getContext()));
                    }
                    final String obj = upload.getFormField(STR_JSON);
                    if (obj == null) {
                        missingParameter(STR_JSON, res, true, action);
                        return;
                    }

                    final DocumentMetadata metadata = PARSER.getDocumentMetadata(obj);
                    if (action.equals(ACTION_NEW)) {
                        newDocument(metadata, res, uploadFile, ctx, user, userConfig, session);
                    } else {
                        if (!checkRequired(req, res, true, action, PARAMETER_ID, PARAMETER_TIMESTAMP)) {
                            return;
                        }
                        final int id = Integer.parseInt(req.getParameter(PARAMETER_ID));
                        final long timestamp = Long.parseLong(req.getParameter(PARAMETER_TIMESTAMP));

                        metadata.setId(id);
                        Metadata[] presentFields = null;

                        try {
                            presentFields = PARSER.findPresentFields(obj);
                        } catch (final UnknownMetadataException x) {
                            unknownColumn(res, "BODY", x.getColumnId(), true, action);
                            return;
                        }

                        if (action.equals(ACTION_UPDATE)) {
                            update(res, id, metadata, timestamp, presentFields, uploadFile, ctx, user, userConfig, session);
                        } else {
                            copy(res, id, metadata, timestamp, presentFields, uploadFile, ctx, user, userConfig, session);
                        }
                    }
                } finally {
                    if (upload != null) {
                        upload.cleanUp();
                    }
                }
            }
        } catch (final UploadException x) {
            final Response resp = new Response(session);
            resp.setException(x);
            try {
                res.setContentType("text/html; charset=UTF-8");
                throw new UploadServletException(res, substituteJS(
                        ResponseWriter.getJSON(resp).toString(), action),
                        x.getMessage(), x);
            } catch (final JSONException e) {
                LOG.error("Giving up", e);
            }
        } catch (final OXException x) {
            handleOXException(res, x, action, true, session);
        } catch (final Throwable t) {
            final Response resp = new Response(session);
            resp.setException(AjaxExceptionCodes.UNEXPECTED_ERROR.create(t, t.getMessage()));
            try {
                res.setContentType("text/html; charset=UTF-8");
                throw new UploadServletException(res, substituteJS(
                        ResponseWriter.getJSON(resp).toString(), action),
                        t.getMessage(), t);
            } catch (final JSONException e) {
                LOG.error("Giving up", e);
            }
            LOG.error(t.getMessage(), t);
        } finally {
            ThreadLocalSessionHolder.getInstance().clear();
        }
    }

    private void checkSize(final long size, final UserSettingMail userSettingMail) throws OXException {
        final long maxSize = InfostoreConfigUtils.determineRelevantUploadSize();
        if (maxSize == 0) {
            return;
        }

        if (size > maxSize) {
            throw UploadSizeExceededException.create(size, maxSize, true);
        }
    }

    // Response Methods

    /*
     * private void notImplemented(final String action, final HttpServletResponse res) throws IOException, ServletException {
     * sendErrorAsJS(res,"The action "+action+" isn't implemented yet"); }
     */

    // Handlers
    protected void newDocument(final DocumentMetadata newDocument, final HttpServletResponse res, final UploadFile upload, final Context ctx, final User user, final UserPermissionBits userConfig, final ServerSession session) {
        // System.out.println("------> "+newDocument.getFolderId());
        res.setContentType(MIME_TEXT_HTML);

        final InfostoreFacade infostore = getInfostore(newDocument.getFolderId());
        final InfostoreSearchEngine searchEngine = getSearchEngine();
        FileInputStream in = null;
        try {

            infostore.startTransaction();
            searchEngine.startTransaction();

            if (!looksLikeFileUpload(upload, newDocument)) {
                infostore.saveDocumentMetadata(newDocument, System.currentTimeMillis(), session);
            } else {
                initMetadata(newDocument, upload);
                infostore.saveDocument(newDocument, in = new FileInputStream(upload.getTmpFile()), System.currentTimeMillis(), session);
            }
            // System.out.println("DONE SAVING: "+System.currentTimeMillis());
            searchEngine.index(newDocument, ctx, user, userConfig);

            infostore.commit();
            searchEngine.commit();

        } catch (final OXException t) {
            rollback(infostore, searchEngine, res, t, ACTION_NEW, true, session);
            return;
        } catch (final FileNotFoundException e) {
            rollback(infostore, searchEngine, res, e, ACTION_NEW, true, session);
            return;
        } catch (final RuntimeException e) {
            rollback(infostore, searchEngine, res, e, ACTION_NEW, true, session);
            return;
        } finally {
            try {
                infostore.finish();
                searchEngine.finish();
            } catch (final OXException e) {
                LOG.debug("", e);
            }
            Streams.close(in);
        }
        PrintWriter w = null;
        try {
            w = res.getWriter();
            final JSONObject obj = new JSONObject();
            obj.put(ResponseFields.DATA, newDocument.getId());
            w.print(substituteJS(obj.toString(), ACTION_NEW));

            w.flush();
        } catch (final IOException e) {
            LOG.debug("", e);
        } catch (final JSONException e) {
            LOG.debug("", e);
        } finally {
            close(w);
        }
    }

    private boolean looksLikeFileUpload(final UploadFile upload, final DocumentMetadata newDocument) {
        return upload != null;
    }

    protected void update(final HttpServletResponse res, final int id, final DocumentMetadata updated, final long timestamp, final Metadata[] presentFields, final UploadFile upload, final Context ctx, final User user, final UserPermissionBits userConfig, final ServerSession session) {

        boolean version = false;
        for (final Metadata m : presentFields) {
            if (m.equals(Metadata.VERSION_LITERAL)) {
                version = true;
                break;
            }
        }
        if (!version) {
            updated.setVersion(InfostoreFacade.CURRENT_VERSION);
        }

        res.setContentType(MIME_TEXT_HTML);

        final InfostoreFacade infostore = getInfostore(updated.getFolderId());
        final InfostoreSearchEngine searchEngine = getSearchEngine();

        try {

            infostore.startTransaction();
            searchEngine.startTransaction();

            if (!looksLikeFileUpload(upload, updated)) {
                infostore.saveDocumentMetadata(updated, timestamp, presentFields, session);
            } else {
                initMetadata(updated, upload);
                infostore.saveDocument(updated, new FileInputStream(upload.getTmpFile()), timestamp, presentFields, session);
            }
            infostore.commit();
            searchEngine.commit();

        } catch (final OXException t) {
            rollback(infostore, null, res, t, ACTION_UPDATE, true, session);
            return;
        } catch (final FileNotFoundException e) {
            rollback(infostore, null, res, e, ACTION_UPDATE, true, session);
            return;
        } catch (final RuntimeException e) {
            rollback(infostore, null, res, e, ACTION_UPDATE, true, session);
            return;
        } finally {
            try {
                infostore.finish();
                searchEngine.finish();
            } catch (final OXException e) {
                LOG.debug("", e);
            }

        }

        PrintWriter w = null;
        try {
            w = res.getWriter();
            w.write(substituteJS("{}", ACTION_UPDATE));
            close(w);
        } catch (final IOException e) {
            LOG.warn(e);
        }
    }

    protected void copy(final HttpServletResponse res, final int id, final DocumentMetadata updated, final long timestamp, final Metadata[] presentFields, final UploadFile upload, final Context ctx, final User user, final UserPermissionBits userConfig, final ServerSession session) {

        res.setContentType(MIME_TEXT_HTML);

        final InfostoreFacade infostore = getInfostore();
        final InfostoreSearchEngine searchEngine = getSearchEngine();
        DocumentMetadata metadata = null;

        try {

            infostore.startTransaction();
            searchEngine.startTransaction();

            metadata = new DocumentMetadataImpl(infostore.getDocumentMetadata(id, InfostoreFacade.CURRENT_VERSION, ctx, user, userConfig));

            final SetSwitch set = new SetSwitch(metadata);
            final GetSwitch get = new GetSwitch(updated);
            for (final Metadata field : presentFields) {
                final Object value = field.doSwitch(get);
                set.setValue(value);
                field.doSwitch(set);
            }
            metadata.setVersion(0);
            metadata.setId(InfostoreFacade.NEW);

            if (upload == null) {
                if (metadata.getFileName() != null && !"".equals(metadata.getFileName())) {
                    infostore.saveDocument(
                        metadata,
                        infostore.getDocument(id, InfostoreFacade.CURRENT_VERSION, ctx, user, userConfig),
                        metadata.getSequenceNumber(),
                        session);
                } else {
                    infostore.saveDocumentMetadata(metadata, timestamp, session);
                }
            } else {
                initMetadata(metadata, upload);
                infostore.saveDocument(metadata, new FileInputStream(upload.getTmpFile()), timestamp, session);
            }
            searchEngine.index(metadata, ctx, user, userConfig);

            infostore.commit();
            searchEngine.commit();
        } catch (final OXException t) {
            rollback(infostore, searchEngine, res, t, ACTION_COPY, true, session);
            return;
        } catch (final FileNotFoundException e) {
            rollback(infostore, searchEngine, res, e, ACTION_COPY, true, session);
            return;
        } catch (final RuntimeException e) {
            rollback(infostore, searchEngine, res, e, ACTION_COPY, true, session);
            return;
        } finally {
            try {
                infostore.finish();
                searchEngine.finish();
            } catch (final OXException e) {
                LOG.debug("", e);
            }

        }

        PrintWriter w = null;
        try {
            w = res.getWriter();
            final JSONObject obj = new JSONObject();
            obj.put(ResponseFields.DATA, metadata.getId());
            w.print(substituteJS(obj.toString(), ACTION_NEW));
            w.flush();
        } catch (final IOException e) {
            LOG.debug("", e);
        } catch (final JSONException e) {
            LOG.debug("", e);
        } finally {
            close(w);
        }
    }

    protected void document(final HttpServletResponse res, final String userAgent, final int id, final int version, final String contentType, final Context ctx, final User user, final UserPermissionBits userConfig, final Session session) throws IOException {
        final InfostoreFacade infostore = getInfostore();
        OutputStream os = null;
        InputStream documentData = null;
        try {
            final DocumentMetadata metadata = infostore.getDocumentMetadata(id, version, ctx, user, userConfig);

            documentData = infostore.getDocument(id, version, ctx, user, userConfig);
            os = res.getOutputStream();

            res.setContentLength((int) metadata.getFileSize());
            if (SAVE_AS_TYPE.equals(contentType)) {
                String contentDisposition = null;
                if (null == contentDisposition) {
                    final StringBuilder sb = new StringBuilder(32).append("attachment");
                    DownloadUtility.appendFilenameParameter(metadata.getFileName(), null, userAgent, sb);
                    res.setHeader("Content-Disposition", sb.toString());
                } else {
                    final StringBuilder sb = new StringBuilder(32).append(contentDisposition);
                    DownloadUtility.appendFilenameParameter(metadata.getFileName(), null, userAgent, sb);
                    res.setHeader("Content-Disposition", sb.toString());
                    // Tools.setHeaderForFileDownload(userAgent, res, metadata.getFileName());
                }
                res.setContentType(contentType);
            } else {
                final CheckedDownload checkedDownload = DownloadUtility.checkInlineDownload(
                    documentData,
                    metadata.getFileName(),
                    metadata.getFileMIMEType(),
                    userAgent);
                res.setHeader("Content-Disposition", checkedDownload.getContentDisposition());
                res.setContentType(checkedDownload.getContentType());
                documentData = checkedDownload.getInputStream();
            }
            // Browsers doesn't like the Pragma header the way we usually set
            // this. Especially if files are sent to the browser. So removing
            // pragma header
            Tools.removeCachingHeader(res);

            final byte[] buffer = new byte[0xFFFF];
            int bytesRead = 0;

            while ((bytesRead = documentData.read(buffer)) > 0) {
                os.write(buffer, 0, bytesRead);
            }
            os.flush();
            os = null;

        } catch (final OXException x) {
            LOG.debug(x.getMessage(), x);
            handleOXException(res, x, STR_ERROR, true, session);
            return;
        } finally {
            Streams.flush(os);
            Streams.close(os);
            Streams.close(documentData);
        }
    }

    private final boolean handleOXException(final HttpServletResponse res, final Throwable t, final String action, final boolean post, final Session session) {
        res.setContentType("text/html; charset=UTF-8");
        final OXException e;
        if (t instanceof OXException) {
            e = (OXException) t;
        } else {
            e = new OXException(t);
        }
        final Response resp = new Response();
        resp.setException(e);
        Writer writer = null;
        try {
            if (post) {
                writer = new AllocatingStringWriter();
            } else {
                writer = res.getWriter();
            }
            ResponseWriter.write(resp, writer, localeFrom(session));
            if (post) {
                res.getWriter().write(substituteJS(writer.toString(), action));
            }
        } catch (final JSONException e1) {
            LOG.error("", t);
        } catch (final IOException e1) {
            LOG.error("", e);
        }
        ((OXException) t).log(LOG);
        return true;
    }

    protected void sendErrorAsJS(final PrintWriter w, final String error, final String... errorParams) {
        final StringBuilder commaSeperatedErrorParams = new StringBuilder();
        for (final String param : errorParams) {
            commaSeperatedErrorParams.append('"');
            commaSeperatedErrorParams.append(param);
            commaSeperatedErrorParams.append('"');
            commaSeperatedErrorParams.append(',');
        }
        commaSeperatedErrorParams.setLength(commaSeperatedErrorParams.length() - 1);
        w.print("{ \"");
        w.print(ResponseFields.ERROR);
        w.print("\" : \"");
        w.print(error);
        w.print("\", \"");
        w.print(ResponseFields.ERROR_PARAMS);
        w.print("\" : [");
        w.print(commaSeperatedErrorParams.toString());
        w.print('}');
        w.flush();
    }

    // Helpers

    protected int[] parseIDList(final JSONArray array) throws JSONException {
        final int[] ids = new int[array.length()];

        for (int i = 0; i < array.length(); i++) {
            final JSONObject tuple = array.getJSONObject(i);
            try {
                ids[i] = tuple.getInt(PARAMETER_ID);
            } catch (final JSONException x) {
                ids[i] = Integer.parseInt(tuple.getString(PARAMETER_ID));
            }
        }
        return ids;
    }

    protected void initMetadata(final DocumentMetadata metadata, final UploadFile upload) {
        if (metadata.getFileName() == null || "".equals(metadata.getFileName())) {
            metadata.setFileName(upload.getPreparedFileName());
        }
        if (metadata.getFileSize() <= 0) {
            metadata.setFileSize(upload.getSize());
        }
        if (metadata.getFileMIMEType() == null || "application/octet-stream".equals(metadata.getFileMIMEType())) {
            String contentType = MimeType2ExtMap.getContentType(metadata.getFileName());
            if("application/octet-stream".equals(contentType)) {
                contentType = upload.getContentType();
            }
            metadata.setFileMIMEType(contentType);
        }
    }

    protected void rollback(final InfostoreFacade infostore, final InfostoreSearchEngine searchEngine, final HttpServletResponse res, final Throwable t, final String action, final boolean post, final Session session) {
        if (infostore != null) {
            try {
                infostore.rollback();
            } catch (final OXException e) {
                LOG.error("", e);
            }
        }
        if (searchEngine != null) {
            try {
                searchEngine.rollback();
            } catch (final OXException e) {
                LOG.error("", e);
            }
        }
        if (!handleOXException(res, t, action, post, session)) {
            try {
                sendErrorAsJSHTML(res, t.toString(), action);
                LOG.error("Got non OXException", t);
            } catch (final IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }

    // Errors

    protected InfostoreFacade getInfostore() {
        return FACADE;
    }

    // TODO: Ask Cisco

    private static final Set<Long> VIRTUAL_FOLDERS = new HashSet<Long>() {

        {
            add((long) FolderObject.VIRTUAL_LIST_INFOSTORE_FOLDER_ID);
            add((long) FolderObject.SYSTEM_INFOSTORE_FOLDER_ID);
            add((long) FolderObject.SYSTEM_PUBLIC_INFOSTORE_FOLDER_ID);
            add((long) FolderObject.SYSTEM_USER_INFOSTORE_FOLDER_ID);
        }
    };

    public static InfostoreFacade getInfostore(final long folderId) {
        // if (folderId == FolderObject.VIRTUAL_LIST_INFOSTORE_FOLDER_ID
        // || folderId == FolderObject.VIRTUAL_USER_INFOSTORE_FOLDER_ID
        // || folderId == FolderObject.SYSTEM_INFOSTORE_FOLDER_ID) {
        // return VIRTUAL_FACADE;
        // }
        if (VIRTUAL_FOLDERS.contains(folderId)) {
            return VIRTUAL_FACADE;
        }
        return FACADE;
    }

    protected InfostoreSearchEngine getSearchEngine() {
        return SEARCH_ENGINE;
    }

}
