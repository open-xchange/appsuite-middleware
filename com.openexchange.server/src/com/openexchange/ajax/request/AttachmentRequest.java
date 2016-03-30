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

package com.openexchange.ajax.request;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Attachment;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.parser.AttachmentParser;
import com.openexchange.ajax.parser.AttachmentParser.UnknownColumnException;
import com.openexchange.ajax.writer.AttachmentWriter;
import com.openexchange.ajax.writer.ResponseWriter;
import com.openexchange.conversion.ConversionService;
import com.openexchange.conversion.Data;
import com.openexchange.conversion.DataArguments;
import com.openexchange.conversion.DataProperties;
import com.openexchange.conversion.DataSource;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.attach.AttachmentExceptionCodes;
import com.openexchange.groupware.attach.AttachmentField;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.ldap.UserStorage;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.groupware.userconfiguration.UserConfiguration;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.session.Session;
import com.openexchange.tools.TimeZoneUtils;
import com.openexchange.tools.exceptions.OXAborted;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIterators;
import com.openexchange.tools.session.ServerSession;
import com.openexchange.tools.session.ServerSessionAdapter;

public class AttachmentRequest extends CommonRequest {

    private static final AttachmentParser PARSER = new AttachmentParser();

    private static final AttachmentBase ATTACHMENT_BASE = Attachment.ATTACHMENT_BASE;

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(AttachmentRequest.class);

    private static final String DATASOURCE = "datasource";

    private static final String IDENTIFIER = "identifier";

    private final UserConfiguration userConfig;

    private final User user;

    private final Context ctx;

    private final Session session;

    public AttachmentRequest(final Session session, final Context ctx, final JSONWriter w) {
        this(ServerSessionAdapter.valueOf(session, ctx), w);
    }

    public AttachmentRequest(final ServerSession session, final JSONWriter w) {
        super(w);
        this.ctx = session.getContext();
        this.user = session.getUser();
        this.userConfig = session.getUserConfiguration();
        this.session = session;
    }

    private static Locale localeFrom(final ServerSession session) {
        if (null == session) {
            return Locale.US;
        }
        return session.getUser().getLocale();
    }

    private static Locale localeFrom(final Session session) throws OXException {
        if (null == session) {
            return Locale.US;
        }
        if (session instanceof ServerSession) {
            return ((ServerSession) session).getUser().getLocale();
        }
        return UserStorage.getInstance().getUser(session.getUserId(), session.getContextId()).getLocale();
    }

    public static boolean hasPermission(final UserConfiguration userConfig) {
        return userConfig.hasCalendar() || userConfig.hasContact() || userConfig.hasTask();
    }

    public boolean action(final String action, final SimpleRequest req) {
        LOG.debug("Attachments: {} {}", action, req);
        try {
            if (AJAXServlet.ACTION_ATTACH.equals(action)) {
                final JSONObject object = (JSONObject) req.getBody();

                for (final AttachmentField required : Attachment.REQUIRED) {
                    if (!object.has(required.getName())) {
                        missingParameter(required.getName(), action);
                        return true;
                    }
                }
                if(!object.has(DATASOURCE)) {
                    missingParameter(DATASOURCE, action);
                    return true;
                }

                final AttachmentMetadata attachment = PARSER.getAttachmentMetadata(object);
                final ConversionService conversionService = ServerServiceRegistry.getInstance().getService(ConversionService.class);

                if (conversionService == null) {
                    throw
                        ServiceExceptionCode.SERVICE_UNAVAILABLE.create(
                        ConversionService.class.getName());
                }

                final JSONObject datasourceDef = object.getJSONObject(DATASOURCE);
                final String datasourceIdentifier = datasourceDef.getString(IDENTIFIER);

                final DataSource source = conversionService.getDataSource(datasourceIdentifier);
                if(source == null) {
                    invalidParameter("datasource", datasourceIdentifier);
                    return true;
                }

                final List<Class<?>> types = Arrays.asList(source.getTypes());

                final Map<String, String> arguments = new HashMap<String, String>();

                for(final String key : datasourceDef.keySet()) {
                    arguments.put(key, datasourceDef.getString(key));
                }

                InputStream is;
                if(types.contains(InputStream.class)) {
                    final Data<InputStream> data = source.getData(InputStream.class, new DataArguments(arguments), session);
                    final String sizeS = data.getDataProperties().get(DataProperties.PROPERTY_SIZE);
                    final String contentTypeS = data.getDataProperties().get(DataProperties.PROPERTY_CONTENT_TYPE);

                    if(sizeS != null) {
                        attachment.setFilesize(Long.parseLong(sizeS));
                    }

                    if(contentTypeS != null) {
                        attachment.setFileMIMEType(contentTypeS);
                    }

                    final String name = data.getDataProperties().get(DataProperties.PROPERTY_NAME);
                    if(name != null && null == attachment.getFilename()) {
                        attachment.setFilename(name);
                    }

                    is = data.getData();

                } else if (types.contains(byte[].class)) {
                    final Data<byte[]> data = source.getData(byte[].class, new DataArguments(arguments), session);
                    final byte[] bytes = data.getData();
                    is = new ByteArrayInputStream(bytes);
                    attachment.setFilesize(bytes.length);

                    final String contentTypeS = data.getDataProperties().get(DataProperties.PROPERTY_CONTENT_TYPE);
                    if(contentTypeS != null) {
                        attachment.setFileMIMEType(contentTypeS);
                    }

                    final String name = data.getDataProperties().get(DataProperties.PROPERTY_NAME);
                    if(name != null && null == attachment.getFilename()) {
                        attachment.setFilename(name);
                    }

                } else {
                    invalidParameter("datasource", datasourceIdentifier);
                    return true; // Maybe add better error message here.
                }

                if(attachment.getFilename() == null) {
                    attachment.setFilename("unknown"+System.currentTimeMillis());
                }

                attachment.setId(AttachmentBase.NEW);

                ATTACHMENT_BASE.startTransaction();
                long ts;
                try {
                    ts = ATTACHMENT_BASE.attachToObject(attachment, is, session, ctx, user, userConfig);
                    ATTACHMENT_BASE.commit();
                } catch (final OXException x) {
                    ATTACHMENT_BASE.rollback();
                    throw x;
                } finally {
                    ATTACHMENT_BASE.finish();
                }

                final Response resp = new Response(session);
                resp.setData(attachment.getId());
                resp.setTimestamp(new Date(ts));

                ResponseWriter.write(resp, w, localeFrom(session));
                return true;

            } else if (AJAXServlet.ACTION_GET.equals(action)) {
                if (!checkRequired(
                    req,
                    AJAXServlet.PARAMETER_FOLDERID,
                    AJAXServlet.PARAMETER_ATTACHEDID,
                    AJAXServlet.PARAMETER_MODULE,
                    AJAXServlet.PARAMETER_ID)) {
                    return true;
                }
                final int folderId = requireNumber(req, AJAXServlet.PARAMETER_FOLDERID);
                final int attachedId = requireNumber(req, AJAXServlet.PARAMETER_ATTACHEDID);
                final int moduleId = requireNumber(req, AJAXServlet.PARAMETER_MODULE);
                final int id = requireNumber(req, AJAXServlet.PARAMETER_ID);

                get(folderId, attachedId, moduleId, id);
                return true;
            } else if (AJAXServlet.ACTION_UPDATES.equals(action)) {
                if (!checkRequired(
                    req,
                    AJAXServlet.PARAMETER_FOLDERID,
                    AJAXServlet.PARAMETER_MODULE,
                    AJAXServlet.PARAMETER_ATTACHEDID,
                    AJAXServlet.PARAMETER_TIMESTAMP)) {
                    return true;
                }
                final int folderId = requireNumber(req, AJAXServlet.PARAMETER_FOLDERID);
                final int attachedId = requireNumber(req, AJAXServlet.PARAMETER_ATTACHEDID);
                final int moduleId = requireNumber(req, AJAXServlet.PARAMETER_MODULE);

                long timestamp = -1;
                try {
                    timestamp = Long.parseLong(req.getParameter(AJAXServlet.PARAMETER_TIMESTAMP));
                } catch (final NumberFormatException nfe) {
                    numberError(AJAXServlet.PARAMETER_TIMESTAMP, req.getParameter(AJAXServlet.PARAMETER_TIMESTAMP));
                }

                final AttachmentField[] columns = PARSER.getColumns(req.getParameterValues(AJAXServlet.PARAMETER_COLUMNS));

                AttachmentField sort = null;
                if (null != req.getParameter(AJAXServlet.PARAMETER_SORT)) {
                    sort = AttachmentField.get(Integer.parseInt(req.getParameter(AJAXServlet.PARAMETER_SORT)));
                }

                int order = AttachmentBase.ASC;
                if ("DESC".equalsIgnoreCase(req.getParameter(AJAXServlet.PARAMETER_ORDER))) {
                    order = AttachmentBase.DESC;
                }

                final String delete = req.getParameter(AJAXServlet.PARAMETER_IGNORE);

                final String timeZoneId = req.getParameter(AJAXServlet.PARAMETER_TIMEZONE);

                updates(folderId, attachedId, moduleId, timestamp, "deleted".equals(delete), columns, sort, order, timeZoneId);
                return true;
            } else if (AJAXServlet.ACTION_ALL.equals(action)) {
                if (!checkRequired(req, AJAXServlet.PARAMETER_FOLDERID, AJAXServlet.PARAMETER_MODULE, AJAXServlet.PARAMETER_ATTACHEDID)) {
                    return true;
                }
                final int folderId = requireNumber(req, AJAXServlet.PARAMETER_FOLDERID);
                final int attachedId = requireNumber(req, AJAXServlet.PARAMETER_ATTACHEDID);
                final int moduleId = requireNumber(req, AJAXServlet.PARAMETER_MODULE);

                final AttachmentField[] columns = PARSER.getColumns(req.getParameterValues(AJAXServlet.PARAMETER_COLUMNS));

                AttachmentField sort = null;
                if (null != req.getParameter(AJAXServlet.PARAMETER_SORT)) {
                    sort = AttachmentField.get(Integer.parseInt(req.getParameter(AJAXServlet.PARAMETER_SORT)));
                }

                int order = AttachmentBase.ASC;
                if ("DESC".equalsIgnoreCase(req.getParameter(AJAXServlet.PARAMETER_ORDER))) {
                    order = AttachmentBase.DESC;
                }
                all(folderId, attachedId, moduleId, columns, sort, order);
                return true;
            } else if (AJAXServlet.ACTION_DETACH.equals(action) || AJAXServlet.ACTION_LIST.equals(action)) {
                if (!checkRequired(req, AJAXServlet.PARAMETER_FOLDERID, AJAXServlet.PARAMETER_MODULE, AJAXServlet.PARAMETER_ATTACHEDID)) {
                    return true;
                }
                final int folderId = requireNumber(req, AJAXServlet.PARAMETER_FOLDERID);
                final int attachedId = requireNumber(req, AJAXServlet.PARAMETER_ATTACHEDID);
                final int moduleId = requireNumber(req, AJAXServlet.PARAMETER_MODULE);

                final JSONArray idsArray = (JSONArray) req.getBody();

                final int[] ids = new int[idsArray.length()];
                for (int i = 0; i < idsArray.length(); i++) {
                    try {
                        ids[i] = idsArray.getInt(i);
                    } catch (final JSONException e) {
                        try {
                            ids[i] = Integer.parseInt(idsArray.getString(i));
                        } catch (final NumberFormatException e1) {
                            handle(e1, session);
                        } catch (final JSONException e1) {
                            handle(e1, session);
                        }
                    }
                }

                if (AJAXServlet.ACTION_DETACH.equals(action)) {
                    detach(folderId, attachedId, moduleId, ids);
                } else {
                    String timeZoneId = null; //req.getParameter(AJAXServlet.PARAMETER_TIMEZONE);
                    final AttachmentField[] columns = PARSER.getColumns(req.getParameterValues(AJAXServlet.PARAMETER_COLUMNS));
                    list(folderId, attachedId, moduleId, ids, columns, timeZoneId);
                }
                return true;
            }
        }
        /*
         * catch (IOException x) { LOG.info("Lost contact to client: ",x); }
         */
        catch (final UnknownColumnException e) {
            handle(e, session);
        } catch (final OXAborted x) {
            return true;
        } catch (final JSONException e) {
            handle(e, session);
        } catch (final OXException e) {
            handle(e, session);
        }

        return false;
    }

    private int requireNumber(final SimpleRequest req, final String parameter) {
        final String value = req.getParameter(parameter);
        try {
            return Integer.parseInt(value);
        } catch (final NumberFormatException nfe) {
            numberError(parameter, value);
            throw new OXAborted();
        }
    }

    public void numberError(final String parameter, final String value) {
        handle(AttachmentExceptionCodes.INVALID_REQUEST_PARAMETER.create(parameter, value), session);
    }

    // Actions

    private void get(final int folderId, final int attachedId, final int moduleId, final int id) {
        try {
            ATTACHMENT_BASE.startTransaction();

            final AttachmentMetadata attachment = ATTACHMENT_BASE.getAttachment(session, folderId, attachedId, moduleId, id, ctx, user, userConfig);

            final AttachmentWriter aWriter = new AttachmentWriter(w);
            aWriter.timedResult(attachment.getCreationDate().getTime());
            aWriter.write(attachment, TimeZoneUtils.getTimeZone(user.getTimeZone()));
            aWriter.endTimedResult();

            ATTACHMENT_BASE.commit();
        } catch (final Throwable t) {
            try {
                ATTACHMENT_BASE.rollback();
            } catch (final OXException e) {
                LOG.debug("", e);
            }
            handle(t, session);
        } finally {
            try {
                ATTACHMENT_BASE.finish();
            } catch (final OXException e) {
                LOG.error("", e);
            }
        }
    }

    private void updates(final int folderId, final int attachedId, final int moduleId, final long ts, final boolean ignoreDeleted, final AttachmentField[] fields, final AttachmentField sort, final int order, final String timeZoneId) {

        SearchIterator<AttachmentMetadata> iter = null;
        SearchIterator<AttachmentMetadata> iter2 = null;

        try {
            ATTACHMENT_BASE.startTransaction();
            Delta<AttachmentMetadata> delta;
            if (sort != null) {
                delta = ATTACHMENT_BASE.getDelta(
                    session, folderId,
                    attachedId,
                    moduleId,
                    ts,
                    ignoreDeleted,
                    fields,
                    sort,
                    order,
                    ctx,
                    user,
                    userConfig);
            } else {
                delta = ATTACHMENT_BASE.getDelta(session, folderId, attachedId, moduleId, ts, ignoreDeleted, ctx, user, userConfig);
            }
            iter = delta.results();
            iter2 = delta.getDeleted();

            final AttachmentWriter aWriter = new AttachmentWriter(w);
            aWriter.timedResult(delta.sequenceNumber());
            aWriter.writeDelta(
                iter,
                iter2,
                fields,
                ignoreDeleted,
                null == timeZoneId ? TimeZoneUtils.getTimeZone(user.getTimeZone()) : TimeZoneUtils.getTimeZone(timeZoneId));
            aWriter.endTimedResult();
            // w.flush();
            ATTACHMENT_BASE.commit();
        } catch (final Throwable t) {
            try {
                ATTACHMENT_BASE.rollback();
            } catch (final OXException e) {
                LOG.debug("", e);
            }
            handle(t, session);
        } finally {
            try {
                ATTACHMENT_BASE.finish();
            } catch (final OXException e) {
                LOG.error("", e);
            }
            SearchIterators.close(iter);
            SearchIterators.close(iter2);
        }
    }

    private void all(final int folderId, final int attachedId, final int moduleId, final AttachmentField[] fields, final AttachmentField sort, final int order) {

        SearchIterator<AttachmentMetadata> iter = null;

        try {
            ATTACHMENT_BASE.startTransaction();
            TimedResult<AttachmentMetadata> result;
            if (sort != null) {
                result = ATTACHMENT_BASE.getAttachments(session, folderId, attachedId, moduleId, fields, sort, order, ctx, user, userConfig);
            } else {
                result = ATTACHMENT_BASE.getAttachments(session, folderId, attachedId, moduleId, ctx, user, userConfig);
            }
            iter = result.results();
            final AttachmentWriter aWriter = new AttachmentWriter(w);
            aWriter.timedResult(result.sequenceNumber());
            aWriter.writeAttachments(iter, fields, TimeZoneUtils.getTimeZone(user.getTimeZone()));
            aWriter.endTimedResult();
            // w.flush();
            ATTACHMENT_BASE.commit();
        } catch (final Throwable t) {
            try {
                ATTACHMENT_BASE.rollback();
            } catch (final OXException e) {
                LOG.debug("", e);
            }
            handle(t, session);
        } finally {
            try {
                ATTACHMENT_BASE.finish();
            } catch (final OXException e) {
                LOG.error("", e);
            }
            SearchIterators.close(iter);
        }
    }

    private void detach(final int folderId, final int attachedId, final int moduleId, final int[] ids) {
        long timestamp = 0;
        try {
            ATTACHMENT_BASE.startTransaction();

            timestamp = ATTACHMENT_BASE.detachFromObject(folderId, attachedId, moduleId, ids, session, ctx, user, userConfig);

            ATTACHMENT_BASE.commit();
        } catch (final Throwable t) {
            try {
                ATTACHMENT_BASE.rollback();
            } catch (final OXException e) {
                LOG.debug("", e);
            }
            handle(t, session);
            return;
        } finally {
            try {
                ATTACHMENT_BASE.finish();
            } catch (final OXException e) {
                LOG.error("", e);
            }
        }

        Response resp;
        try {
            resp = new Response(session);
        } catch (final OXException e) {
            resp = new Response();
        }
        resp.setData("");
        resp.setTimestamp(new Date(timestamp));
        try {
            ResponseWriter.write(resp, w, localeFrom(session));
        } catch (final JSONException e) {
            LOG.debug("Cannot contact client", e);
        } catch (OXException e) {
            LOG.debug("", e);
        }
    }

    private void list(final int folderId, final int attachedId, final int moduleId, final int[] ids, final AttachmentField[] fields, String timeZoneId) {

        SearchIterator<AttachmentMetadata> iter = null;
        final TimeZone tz;
        if (null == timeZoneId) {
            tz = TimeZoneUtils.getTimeZone(user.getTimeZone());
        } else {
            tz = TimeZoneUtils.getTimeZone(timeZoneId);
        }
        try {
            ATTACHMENT_BASE.startTransaction();

            final TimedResult<AttachmentMetadata> result = ATTACHMENT_BASE.getAttachments(session, folderId, attachedId, moduleId, ids, fields, ctx, user, userConfig);

            iter = result.results();

            final AttachmentWriter aWriter = new AttachmentWriter(w);
            aWriter.timedResult(result.sequenceNumber());
            aWriter.writeAttachments(iter, fields, tz);
            aWriter.endTimedResult();
            // w.flush();

            ATTACHMENT_BASE.commit();
        } catch (final Throwable t) {
            try {
                ATTACHMENT_BASE.rollback();
            } catch (final OXException e) {
                LOG.error("", e);
            }
            handle(t, session);
        } finally {
            try {
                ATTACHMENT_BASE.finish();
            } catch (final OXException e) {
                LOG.error("", e);
            }

            SearchIterators.close(iter);
        }
    }

}
