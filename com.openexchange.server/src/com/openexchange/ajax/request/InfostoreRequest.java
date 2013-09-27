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

package com.openexchange.ajax.request;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Attachment;
import com.openexchange.ajax.Infostore;
import com.openexchange.ajax.fields.ResponseFields;
import com.openexchange.ajax.fields.SearchFields;
import com.openexchange.ajax.parser.InfostoreParser;
import com.openexchange.ajax.parser.InfostoreParser.UnknownMetadataException;
import com.openexchange.ajax.writer.InfostoreWriter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.attach.AttachmentField;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.CreatedByComparator;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.InfostoreSearchEngine;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.groupware.infostore.utils.GetSwitch;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.infostore.utils.SetSwitch;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.groupware.userconfiguration.UserPermissionBits;
import com.openexchange.java.Streams;
import com.openexchange.log.LogFactory;
import com.openexchange.sessiond.impl.ThreadLocalSessionHolder;
import com.openexchange.tools.TimeZoneUtils;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

public class InfostoreRequest extends CommonRequest {

    private static final InfostoreParser PARSER = new InfostoreParser();

    private final ServerSession session;

    private final Context ctx;

    private final User user;

    private final UserPermissionBits userPermissionBits;

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(InfostoreRequest.class));

    public InfostoreRequest(final ServerSession session, final JSONWriter w) {
        super(w);
        this.ctx = session.getContext();
        this.session = session;
        userPermissionBits = session.getUserPermissionBits();
        user = session.getUser();
    }

    public static boolean hasPermission(final UserPermissionBits userConfig) {
        return userConfig.hasInfostore();
    }

    public boolean action(final String action, final SimpleRequest req) throws OXException {
        if (!hasPermission(userPermissionBits)) {
            throw OXException.noPermissionForModule("infostore");
        }
        try {
            ThreadLocalSessionHolder.getInstance().setSession(session);
            if (action.equals(AJAXServlet.ACTION_ALL)) {

                if (!checkRequired(req, AJAXServlet.PARAMETER_FOLDERID, AJAXServlet.PARAMETER_COLUMNS)) {
                    return true;
                }

                doSortedSearch(req);
                return true;
            } else if (action.equals(AJAXServlet.ACTION_UPDATES)) {
                if (!checkRequired(req, AJAXServlet.PARAMETER_FOLDERID, AJAXServlet.PARAMETER_COLUMNS,
                        AJAXServlet.PARAMETER_TIMESTAMP)) {
                    return true;
                }

                doSortedSearch(req);

                return true;
            } else if (action.equals(AJAXServlet.ACTION_GET)) {
                if (!checkRequired(req, AJAXServlet.PARAMETER_ID)) {
                    return true;
                }
                final int id = Integer.parseInt(req.getParameter(AJAXServlet.PARAMETER_ID));

                int version = InfostoreFacade.CURRENT_VERSION;

                if (req.getParameter(AJAXServlet.PARAMETER_VERSION) != null) {
                    version = Integer.parseInt(req.getParameter(AJAXServlet.PARAMETER_VERSION));
                }

                final String timeZoneId = req.getParameter(AJAXServlet.PARAMETER_TIMEZONE);

                get(id, version, timeZoneId);

                return true;
            } else if (action.equals(AJAXServlet.ACTION_VERSIONS)) {
                if (!checkRequired(req, AJAXServlet.PARAMETER_ID, AJAXServlet.PARAMETER_COLUMNS)) {
                    return true;
                }

                doSortedSearch(req);

                return true;
            } else if (action.equals(AJAXServlet.ACTION_REVERT)) {
                if (!checkRequired(req, AJAXServlet.PARAMETER_ID, AJAXServlet.PARAMETER_TIMESTAMP)) {
                    return true;
                }
                final int id = Integer.parseInt(req.getParameter(AJAXServlet.PARAMETER_ID));
                final long ts = Long.parseLong(req.getParameter(AJAXServlet.PARAMETER_TIMESTAMP));

                revert(id, ts);

                return true;
            } else if (action.equals(AJAXServlet.ACTION_LIST)) {
                if (!checkRequired(req, AJAXServlet.PARAMETER_COLUMNS)) {
                    return true;
                }
                final JSONArray array = (JSONArray) req.getBody();
                final int[] ids = parseIDList(array, null);

                Metadata[] cols = null;

                try {
                    cols = PARSER.getColumns(req.getParameterValues(AJAXServlet.PARAMETER_COLUMNS));
                } catch (final InfostoreParser.UnknownMetadataException x) {
                    unknownColumn(x.getColumnId());
                    return true;
                }

                final String timeZoneId = req.getParameter(AJAXServlet.PARAMETER_TIMEZONE);

                list(ids, cols, timeZoneId);

                return true;
            } else if (action.equals(AJAXServlet.ACTION_DELETE)) {
                if (!checkRequired(req, AJAXServlet.PARAMETER_TIMESTAMP)) {
                    return true;
                }
                final Object toDelete = req.getBody();
                final TIntIntMap folderMapping = new TIntIntHashMap();
                final int[] ids = parseIDList(toDelete, folderMapping);
                final long timestamp = Long.parseLong(req.getParameter(AJAXServlet.PARAMETER_TIMESTAMP));
                delete(ids, folderMapping, timestamp);
                return true;
            } else if (action.equals(AJAXServlet.ACTION_DETACH)) {
                if (!checkRequired(req, AJAXServlet.PARAMETER_TIMESTAMP, AJAXServlet.PARAMETER_ID)) {
                    return true;
                }
                final JSONArray array = (JSONArray) req.getBody();
                final long timestamp = Long.parseLong(req.getParameter(AJAXServlet.PARAMETER_TIMESTAMP));
                final int id = Integer.parseInt(req.getParameter(AJAXServlet.PARAMETER_ID));

                final int[] versions = new int[array.length()];
                for (int i = 0; i < array.length(); i++) {
                    versions[i] = array.getInt(i);
                }

                detach(id, versions, timestamp);
                return true;
            } else if (action.equals(AJAXServlet.ACTION_UPDATE) || action.equals(AJAXServlet.ACTION_COPY)) {

                if (!checkRequired(req, AJAXServlet.PARAMETER_ID, AJAXServlet.PARAMETER_TIMESTAMP)) {
                    return true;
                }

                final int id = Integer.parseInt(req.getParameter(AJAXServlet.PARAMETER_ID));
                final long timestamp = Long.parseLong(req.getParameter(AJAXServlet.PARAMETER_TIMESTAMP));

                final String updateBody = req.getBody().toString();
                final DocumentMetadata updated = PARSER.getDocumentMetadata(updateBody);
                updated.setId(id);
                Metadata[] presentFields = null;

                try {
                    presentFields = PARSER.findPresentFields(updateBody);
                } catch (final UnknownMetadataException x) {
                    unknownColumn(x.getColumnId());
                    return true;
                }

                if (action.equals(AJAXServlet.ACTION_UPDATE)) {
                    update(id, updated, timestamp, presentFields);
                } else {
                    copy(id, updated, timestamp, presentFields);
                }
                return true;
            } else if (action.equals(AJAXServlet.ACTION_NEW)) {

                final DocumentMetadata newDocument = PARSER.getDocumentMetadata(req.getBody().toString());
                // newDocument.setFolderId(new
                // Long(req.getParameter(PARAMETER_FOLDERID)));
                newDocument(newDocument);
                return true;
            } else if (action.equals(AJAXServlet.ACTION_LOCK)) {
                if (!checkRequired(req, action, AJAXServlet.PARAMETER_ID)) {
                    return true;
                }
                long diff = -1;
                if (null != req.getParameter("diff")) {
                    diff = Long.parseLong(req.getParameter("diff"));
                }
                final int id = Integer.parseInt(req.getParameter(AJAXServlet.PARAMETER_ID));
                lock(id, diff);

                return true;

            } else if (action.equals(AJAXServlet.ACTION_UNLOCK)) {
                if (!checkRequired(req, action, AJAXServlet.PARAMETER_ID)) {
                    return true;
                }
                unlock(Integer.parseInt(req.getParameter(AJAXServlet.PARAMETER_ID)));

                return true;
            } else if (action.equals(AJAXServlet.ACTION_SEARCH)) {
                if (!checkRequired(req, action, AJAXServlet.PARAMETER_COLUMNS)) {
                    return true;
                }

                doSortedSearch(req);
                return true;
            } else if (action.equals(AJAXServlet.ACTION_SAVE_AS)) {
                if (!checkRequired(req, action, AJAXServlet.PARAMETER_FOLDERID, AJAXServlet.PARAMETER_ATTACHEDID,
                        AJAXServlet.PARAMETER_MODULE, AJAXServlet.PARAMETER_ATTACHMENT)) {
                    return true;
                }
                final int folderId = Integer.parseInt(req.getParameter(AJAXServlet.PARAMETER_FOLDERID));
                final int attachedId = Integer.parseInt(req.getParameter(AJAXServlet.PARAMETER_ATTACHEDID));
                final int moduleId = Integer.parseInt(req.getParameter(AJAXServlet.PARAMETER_MODULE));
                final int attachment = Integer.parseInt(req.getParameter(AJAXServlet.PARAMETER_ATTACHMENT));

                final String body = req.getBody().toString();
                final DocumentMetadata newDocument = PARSER.getDocumentMetadata(body);
                final Metadata[] fields = PARSER.findPresentFields(body);
                saveAs(newDocument, fields, folderId, attachedId, moduleId, attachment);
                return true;
            }
            return false;
        } catch (final JSONException x) {
            handle(x, session);
            return true;
        } catch (final UnknownMetadataException x) {
            handle(x, session);
            return true;
        } catch (final OXException x) {
            handle(x, session);
            return true;
        } catch (final NumberFormatException x) {
            handle(x, session);
            return true;
        } catch (final Throwable t) {
            handle(t, session);
            return true;
        } finally {
            ThreadLocalSessionHolder.getInstance().clear();
        }
    }

    protected int[] parseIDList(final Object toDelete, final TIntIntMap folderMapping) throws JSONException {
        if(JSONArray.class.isAssignableFrom(toDelete.getClass())) {
            final JSONArray array = (JSONArray) toDelete;
            final int[] ids = new int[array.length()];

            for (int i = 0; i < array.length(); i++) {
                final JSONObject tuple = array.getJSONObject(i);
                try {
                    ids[i] = tuple.getInt(AJAXServlet.PARAMETER_ID);
                } catch (final JSONException x) {
                    ids[i] = Integer.parseInt(tuple.getString(AJAXServlet.PARAMETER_ID));
                }
                if(folderMapping != null) {

                    int folder = -1;
                    try {
                        folder = tuple.getInt("folder");
                    } catch (final JSONException x) {
                        folder = Integer.parseInt("folder");
                    }
                    folderMapping.put(ids[i], folder);
                }
            }

            return ids;
        }
        final int[] ids = new int[1];
        ids[0] = ((JSONObject)toDelete).getInt(AJAXServlet.PARAMETER_ID);
        return ids;
    }

    protected void doSortedSearch(final SimpleRequest req) throws JSONException, OXException {
        Metadata[] cols = null;

        try {
            cols = PARSER.getColumns(req.getParameterValues(AJAXServlet.PARAMETER_COLUMNS));
        } catch (final InfostoreParser.UnknownMetadataException x) {
            unknownColumn(x.getColumnId());
            return;
        }

        final String sort = req.getParameter(AJAXServlet.PARAMETER_SORT);
        final String order = req.getParameter(AJAXServlet.PARAMETER_ORDER);

        if (order != null && !checkRequired(req, AJAXServlet.PARAMETER_SORT)) {
            return;
        }

        Metadata sortedBy = null;
        int dir = -23;

        if (sort != null) {

            dir = InfostoreFacade.ASC;
            if (order != null && order.equalsIgnoreCase("DESC")) {
                // if(order.equalsIgnoreCase("DESC")) {
                dir = InfostoreFacade.DESC;
                // }
            }
            sortedBy = Metadata.get(Integer.parseInt(sort));
            if (sortedBy == null) {
                invalidParameter(AJAXServlet.PARAMETER_SORT, sort);
                return;
            }
        }

        final String action = req.getParameter(AJAXServlet.PARAMETER_ACTION);

        if (action.equals(AJAXServlet.ACTION_ALL)) {
            if (!checkRequired(req, action, AJAXServlet.PARAMETER_FOLDERID)) {
                return;
            }
            final int folderId = Integer.parseInt(req.getParameter(AJAXServlet.PARAMETER_FOLDERID));

            final String timeZoneId = req.getParameter(AJAXServlet.PARAMETER_TIMEZONE);

            final String stringLeftHandLimit = req.getParameter(AJAXServlet.LEFT_HAND_LIMIT);
            final String stringRightHandLimit = req.getParameter(AJAXServlet.RIGHT_HAND_LIMIT);

            final int leftHandLimit;
            final int rightHandLimit;

            if (stringLeftHandLimit == null) {
                leftHandLimit = 0;
            } else {
                leftHandLimit = Integer.parseInt(AJAXServlet.LEFT_HAND_LIMIT);
            }

            if (stringRightHandLimit == null) {
                rightHandLimit = 0;
            } else {
                rightHandLimit = Integer.parseInt(AJAXServlet.RIGHT_HAND_LIMIT);
            }

            all(folderId, cols, sortedBy, dir, timeZoneId, leftHandLimit, rightHandLimit);
        } else if (action.equals(AJAXServlet.ACTION_VERSIONS)) {
            if (!checkRequired(req, action, AJAXServlet.PARAMETER_ID)) {
                return;
            }
            final int id = Integer.parseInt(req.getParameter(AJAXServlet.PARAMETER_ID));
            final String timeZoneId = req.getParameter(AJAXServlet.PARAMETER_TIMEZONE);
            versions(id, cols, sortedBy, dir, timeZoneId);
        } else if (action.equals(AJAXServlet.ACTION_UPDATES)) {
            if (!checkRequired(req, action, AJAXServlet.PARAMETER_FOLDERID, AJAXServlet.PARAMETER_TIMESTAMP)) {
                return;
            }
            final int folderId = Integer.parseInt(req.getParameter(AJAXServlet.PARAMETER_FOLDERID));
            final long timestamp = Long.parseLong(req.getParameter(AJAXServlet.PARAMETER_TIMESTAMP));
            final String delete = req.getParameter(AJAXServlet.PARAMETER_IGNORE);
            final String timeZoneId = req.getParameter(AJAXServlet.PARAMETER_TIMEZONE);
            updates(folderId, cols, sortedBy, dir, timestamp, delete != null && delete.equals("deleted"), timeZoneId);

        } else if (action.equals(AJAXServlet.ACTION_SEARCH)) {
            final JSONObject queryObject = (JSONObject) req.getBody();
            final String query = queryObject.getString(SearchFields.PATTERN);

            int folderId = InfostoreSearchEngine.NO_FOLDER;
            final String folderS = req.getParameter(AJAXServlet.PARAMETER_FOLDERID);
            if (null != folderS) {
                folderId = Integer.parseInt(folderS);
            }

            int start = InfostoreSearchEngine.NOT_SET;
            final String startS = req.getParameter(AJAXServlet.PARAMETER_START);
            if (null != startS) {
                start = Integer.parseInt(startS);
            }

            int end = InfostoreSearchEngine.NOT_SET;
            final String endS = req.getParameter(AJAXServlet.PARAMETER_END);
            if (null != endS) {
                end = Integer.parseInt(endS);
            }

            if (start == InfostoreSearchEngine.NOT_SET && end == InfostoreSearchEngine.NOT_SET) {
                final String limitS = req.getParameter(AJAXServlet.PARAMETER_LIMIT);
                if (limitS != null) {
                    final int limit = Integer.parseInt(limitS);
                    start = 0;
                    end = limit - 1;
                }
            }

            final String timeZoneId = req.getParameter(AJAXServlet.PARAMETER_TIMEZONE);

            search(query, cols, folderId, sortedBy, dir, start, end, timeZoneId);
        }
    }

    // Actions

    protected void list(final int[] ids, final Metadata[] cols, final String timeZoneId) throws OXException {
        final InfostoreFacade infostore = getInfostore();
        TimedResult<DocumentMetadata> result = null;
        SearchIterator<DocumentMetadata> iter = null;
        try {

            result = infostore.getDocuments(ids, cols, ctx, user, userPermissionBits);

            iter = result.results();

            final InfostoreWriter iWriter = new InfostoreWriter(w);
            iWriter.timedResult(result.sequenceNumber());
            iWriter.writeMetadata(iter, cols, TimeZoneUtils.getTimeZone(null == timeZoneId ? user.getTimeZone() : timeZoneId));
            iWriter.endTimedResult();

        } catch (final Throwable t) {
            handle(t, session);

        } finally {
            if (iter != null) {
                try {
                    iter.close();
                } catch (final OXException e) {
                    LOG.error(e.getMessage(), e);
                }
            }
        }
    }

    protected void get(final int id, final int version, final String timeZoneId) {
        final InfostoreFacade infostore = getInfostore();
        DocumentMetadata dm = null;
        try {

            dm = infostore.getDocumentMetadata(id, version, ctx, user, userPermissionBits);
            if (dm == null) {
                sendErrorAsJS("Cannot find document: %s ", Integer.toString(id));
            }
        } catch (final Throwable t) {
            handle(t, session);
            return;
        }

        try {
            final InfostoreWriter iWriter = new InfostoreWriter(w);
            iWriter.timedResult(dm.getSequenceNumber());
            iWriter.write(dm, TimeZoneUtils.getTimeZone(null == timeZoneId ? user.getTimeZone() : timeZoneId));
            iWriter.endTimedResult();
        } catch (final JSONException e) {
            LOG.error("", e);
        }
    }

    protected void revert(final int id, final long ts) {
        final InfostoreFacade infostore = getInfostore();
        SearchIterator<DocumentMetadata> iter = null;
        long timestamp = -1;
        try {
            // SearchENgine?
            infostore.startTransaction();
            infostore.getDocumentMetadata(id, InfostoreFacade.CURRENT_VERSION, ctx, user,
                    userPermissionBits).getSequenceNumber();
            final TimedResult<DocumentMetadata> result = infostore.getVersions(id, new Metadata[] { Metadata.VERSION_LITERAL },
                    ctx, user, userPermissionBits);
            if (timestamp > ts) {
                throw AjaxExceptionCodes.CONFLICT.create();
            }
            iter = result.results();
            final TIntList versions;
            try {
                versions = new TIntArrayList();
                while (iter.hasNext()) {
                    final int version = (iter.next()).getVersion();
                    if (version == 0) {
                        continue;
                    }
                    versions.add(version);
                }
            } finally {
                iter.close();
            }
            infostore.removeVersion(id, versions.toArray(), session);
            timestamp = infostore.getDocumentMetadata(id, InfostoreFacade.CURRENT_VERSION, ctx,
                    user, userPermissionBits).getSequenceNumber();
            infostore.commit();
            w.object();
            w.key(ResponseFields.DATA).value(new JSONObject()).key(ResponseFields.TIMESTAMP).value(timestamp);
            w.endObject();
        } catch (final Throwable t) {
            try {
                infostore.rollback();
            } catch (final OXException e) {
                LOG.debug("", e);
            }
            handle(t, session);
            return;
        } finally {
            if (iter != null) {
                try {
                    iter.close();
                } catch (final OXException e) {
                    LOG.error("", e);
                }
            }
            try {
                infostore.finish();
            } catch (final OXException e1) {
                LOG.error("", e1);
            }
        }
    }

    protected void all(final int folderId, final Metadata[] cols, final Metadata sortedBy, final int dir, final String timeZoneId, final int leftHandLimit, final int rightHandLimit)
            throws OXException {
        /**
         * System.out.println("ALL: "+System.currentTimeMillis());
         * System.out.println("---------all-------------");
         * System.out.println(folderId); System.out.println(cols.length);
         * for(Metadata m : cols) { System.out.println(m.getName()); }
         * System.out.println(sortedBy); System.out.println(dir);
         * System.out.println("----------all------------");
         */
        final InfostoreFacade infostore = getInfostore(folderId);
        TimedResult<DocumentMetadata> result = null;
        SearchIterator<DocumentMetadata> iter = null;
        try {

            if (sortedBy == null) {
                result = infostore.getDocuments(folderId, cols, ctx, user, userPermissionBits);
            } else {
                result = infostore.getDocuments(folderId, cols, sortedBy, dir, ctx, user,
                        userPermissionBits);
            }

            iter = result.results();
            if (Metadata.CREATED_BY_LITERAL.equals(sortedBy)) {
                iter = CreatedByComparator.resort(iter, new CreatedByComparator(user.getLocale(), ctx).setDescending(dir < 0));
            }

            final InfostoreWriter iWriter = new InfostoreWriter(w);
            iWriter.timedResult(result.sequenceNumber());
            iWriter.writeMetadata(iter, cols, TimeZoneUtils.getTimeZone(null == timeZoneId ? user.getTimeZone() : timeZoneId));
            iWriter.endTimedResult();

        } catch (final Throwable t) {
            handle(t, session);
            return;
        } finally {
            if (iter != null) {
                iter.close();
            }
        }
    }

    protected void versions(final int id, final Metadata[] cols, final Metadata sortedBy, final int dir, final String timeZoneId)
            throws OXException {
        final InfostoreFacade infostore = getInfostore();
        TimedResult<DocumentMetadata> result = null;
        SearchIterator<DocumentMetadata> iter = null;
        final Metadata[] loadCols = addIfNeeded(cols, Metadata.VERSION_LITERAL);
        try {

            if (sortedBy == null) {
                result = infostore.getVersions(id, loadCols, ctx, user, userPermissionBits);
            } else {
                result = infostore.getVersions(id, loadCols, sortedBy, dir, ctx, user,
                        userPermissionBits);
            }
            iter = result.results();
            if (Metadata.CREATED_BY_LITERAL.equals(sortedBy)) {
                iter = CreatedByComparator.resort(iter, new CreatedByComparator(user.getLocale(), ctx).setDescending(dir < 0));
            }
            final InfostoreWriter iWriter = new InfostoreWriter(w);
            iWriter.timedResult(result.sequenceNumber());
            iWriter.writeMetadata(skipVersion0(iter), cols, TimeZoneUtils.getTimeZone(null == timeZoneId ? user.getTimeZone() : timeZoneId));
            iWriter.endTimedResult();

        } catch (final Throwable t) {
            handle(t, session);
            return;
        } finally {
            if (iter != null) {
                iter.close();
            }
        }
    }

    private Metadata[] addIfNeeded(final Metadata[] cols, final Metadata column) {
        final List<Metadata> newCols = new ArrayList<Metadata>(cols.length+1);
        for (final Metadata metadata : cols) {
            if(metadata == column) {
                return cols;
            }
            newCols.add(metadata);
        }
        newCols.add(column);
        return newCols.toArray(new Metadata[cols.length+1]);
    }

    private SearchIterator<DocumentMetadata> skipVersion0(final SearchIterator<DocumentMetadata> iter) {
        return new SearchIterator<DocumentMetadata>() {

            private DocumentMetadata next;
            private SearchIteratorException se;
            private OXException oxe;

            @Override
            public void addWarning(final OXException warning) {
                iter.addWarning(warning);
            }

            @Override
            public void close() throws OXException {
                iter.close();
            }

            @Override
            public OXException[] getWarnings() {
                return iter.getWarnings();
            }

            @Override
            public boolean hasNext() throws OXException{
                try {
                    scrollToNext();
                } catch (final OXException e) {
                    oxe = e;
                }
                return next != null;
            }


            @Override
            public boolean hasWarnings() {
                return iter.hasWarnings();
            }

            @Override
            public DocumentMetadata next() throws OXException {
                if(se != null) {
                    throw se;
                }
                if(oxe != null) {
                    throw oxe;
                }
                if(next == null) {
                    scrollToNext();
                }
                final DocumentMetadata nextResult = next;
                next = null;
                return nextResult;
            }

            private void scrollToNext() throws OXException {
                while(iter.hasNext()) {
                    next = iter.next();
                    if(next.getVersion() != 0) {
                        return;
                    } else {
                        next = null;
                    }
                }
            }

            @Override
            public int size() {
                return -1;
            }

        };
    }

    protected void updates(final int folderId, final Metadata[] cols, final Metadata sortedBy, final int dir,
            final long timestamp, final boolean ignoreDelete, final String timeZoneId) throws OXException {
        final InfostoreFacade infostore = getInfostore(folderId);
        Delta<DocumentMetadata> delta = null;

        SearchIterator<DocumentMetadata> iter = null;
        SearchIterator<DocumentMetadata> iter2 = null;

        try {

            if (sortedBy == null) {
                delta = infostore.getDelta(folderId, timestamp, cols, ignoreDelete, ctx, user,
                        userPermissionBits);
            } else {
                delta = infostore.getDelta(folderId, timestamp, cols, sortedBy, dir, ignoreDelete, ctx,
                        user, userPermissionBits);
            }

            iter = delta.results();
            iter2 = delta.getDeleted();
            if (Metadata.CREATED_BY_LITERAL.equals(sortedBy)) {
                final CreatedByComparator comparator = new CreatedByComparator(user.getLocale(), ctx).setDescending(dir < 0);
                iter = CreatedByComparator.resort(iter, comparator);
                iter2 = CreatedByComparator.resort(iter2, comparator);
            }

            final InfostoreWriter iWriter = new InfostoreWriter(w);
            iWriter.timedResult(delta.sequenceNumber());
            iWriter.writeDelta(iter, iter2, cols, ignoreDelete, TimeZoneUtils.getTimeZone(null == timeZoneId ? user.getTimeZone() : timeZoneId));
            iWriter.endTimedResult();

        } catch (final Throwable t) {
            handle(t, session);
            return;
        } finally {
            if (iter != null) {
                iter.close();
            }
            if (iter2 != null) {
                iter2.close();
            }
        }
    }

    protected void delete(final int[] ids, final TIntIntMap folderMapping, final long timestamp) {
        final InfostoreFacade infostore = getInfostore();
        final InfostoreSearchEngine searchEngine = getSearchEngine();

        int[] notDeleted = new int[0];
        if (ids.length != 0) {
            try {

                infostore.startTransaction();
                searchEngine.startTransaction();

                notDeleted = infostore.removeDocument(ids, timestamp, session);

                final TIntSet notDeletedSet = new TIntHashSet();
                for (final int nd : notDeleted) {
                    notDeletedSet.add(nd);
                }

                for (final int id : ids) {
                    if (!notDeletedSet.contains(id)) {
                        searchEngine.unIndex0r(id, ctx, user, userPermissionBits);
                    }
                }

                infostore.commit();
                searchEngine.commit();

            } catch (final Throwable t) {
                try {
                    infostore.rollback();
                    searchEngine.rollback();
                    handle(t, session);
                    return;
                } catch (final OXException e) {
                    LOG.error("", e);
                }
            } finally {
                try {
                    infostore.finish();
                    searchEngine.finish();
                } catch (final OXException e) {
                    LOG.error("", e);
                }

            }
        }

        try {
            w.object();
            w.key("data");

            w.array();
            for (int i = 0; i < notDeleted.length; i++) {
                w.object();
                w.key(AJAXServlet.PARAMETER_ID);
                final int nd = notDeleted[i];
                w.value(nd);
                w.key("folder");
                w.value(folderMapping.get(nd));
                w.endObject();
            }
            w.endArray();
            w.endObject();

        } catch (final JSONException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    protected void detach(final int objectId, final int[] ids, final long timestamp) {
        final InfostoreFacade infostore = getInfostore();
        final InfostoreSearchEngine searchEngine = getSearchEngine();

        int[] notDetached = new int[0];
        long newTimestamp = 0;
        if (ids.length != 0) {
            try {

                infostore.startTransaction();
                searchEngine.startTransaction();

                notDetached = infostore.removeVersion(objectId, ids, session);

                final DocumentMetadata currentVersion = infostore.getDocumentMetadata(objectId, InfostoreFacade.CURRENT_VERSION, ctx,
                        user, userPermissionBits);
                searchEngine.index(currentVersion, ctx, user, userPermissionBits);
                newTimestamp = currentVersion.getLastModified().getTime();
                infostore.commit();
                searchEngine.commit();
            } catch (final Throwable t) {
                try {
                    infostore.rollback();
                    searchEngine.rollback();
                } catch (final OXException e) {
                    LOG.error("", e);
                }
                handle(t, session);
                return;
            } finally {
                try {
                    infostore.finish();
                    searchEngine.finish();
                } catch (final OXException e) {
                    LOG.error("", e);
                }

            }
        }

        try {
            w.object();
            w.key("data");
            w.array();
            for (int i = 0; i < notDetached.length; i++) {
                final int nd = notDetached[i];
                w.value(nd);
            }
            w.endArray();
            w.key(ResponseFields.TIMESTAMP);
            w.value(newTimestamp);
            w.endObject();
        } catch (final JSONException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    protected void newDocument(final DocumentMetadata newDocument) throws JSONException {
        final InfostoreFacade infostore = getInfostore(newDocument.getFolderId());
        final InfostoreSearchEngine searchEngine = getSearchEngine();
        try {

            infostore.startTransaction();
            searchEngine.startTransaction();

            infostore.saveDocumentMetadata(newDocument, System.currentTimeMillis(), session);
            infostore.commit();
            // System.out.println("DONE SAVING: "+System.currentTimeMillis());
            searchEngine.index(newDocument, ctx, user, userPermissionBits);
            searchEngine.commit();
        } catch (final Throwable t) {
            try {
                infostore.rollback();
                searchEngine.rollback();

            } catch (final OXException e) {
                LOG.error("", e);
            }
            handle(t, session);
            return;
        } finally {
            try {
                infostore.finish();
                searchEngine.finish();
            } catch (final OXException e) {
                LOG.error("", e);
            }
        }
        w.object();
        w.key(ResponseFields.DATA).value(newDocument.getId());
        w.endObject();
    }

    protected void saveAs(final DocumentMetadata newDocument, final Metadata[] fields, final int folderId,
            final int attachedId, final int moduleId, final int attachment) throws JSONException {
        final Set<Metadata> alreadySet = new HashSet<Metadata>(Arrays.asList(fields));
        if (!alreadySet.contains(Metadata.FOLDER_ID_LITERAL)) {
            missingParameter("folder_id in object", AJAXServlet.ACTION_SAVE_AS);
            // try {
            // missingParameter("folder_id in
            // object",AJAXServlet.ACTION_SAVE_AS);
            // } catch (final IOException e1) {
            // LOG.debug("", e1);
            // }
        }

        final AttachmentBase attachmentBase = Attachment.ATTACHMENT_BASE;
        final InfostoreFacade infostore = getInfostore(newDocument.getFolderId());
        final InfostoreSearchEngine searchEngine = getSearchEngine();
        InputStream in = null;
        try {
            attachmentBase.startTransaction();
            infostore.startTransaction();
            searchEngine.startTransaction();

            final AttachmentMetadata att = attachmentBase.getAttachment(session, folderId, attachedId, moduleId, attachment,
                    ctx, user, session.getUserConfiguration());
            final com.openexchange.groupware.attach.util.GetSwitch get = new com.openexchange.groupware.attach.util.GetSwitch(
                    att);
            final SetSwitch set = new SetSwitch(newDocument);

            for (final Metadata attachmentCompatible : Metadata.VALUES) {
                if (alreadySet.contains(attachmentCompatible)) {
                    continue;
                }
                final AttachmentField attField = Metadata.getAttachmentField(attachmentCompatible);
                if (null == attField) {
                    continue;
                }
                final Object value = attField.doSwitch(get);
                set.setValue(value);
                attachmentCompatible.doSwitch(set);
            }
            newDocument.setId(InfostoreFacade.NEW);
            in = attachmentBase.getAttachedFile(session, folderId, attachedId, moduleId, attachment, ctx,
                    user, session.getUserConfiguration());
            infostore.saveDocument(newDocument, in, System.currentTimeMillis(), session); // FIXME
                                                                                                // violates
                                                                                                // encapsulation

            // System.out.println("DONE SAVING: "+System.currentTimeMillis());
            searchEngine.index(newDocument, ctx, user, userPermissionBits);

            infostore.commit();
            searchEngine.commit();
            attachmentBase.commit();
        } catch (final Throwable t) {
            try {
                infostore.rollback();
                searchEngine.rollback();
                attachmentBase.rollback();
            } catch (final OXException e) {
                LOG.error("", e);
            }
            handle(t, session);
            return;
        } finally {
            try {
                infostore.finish();
                searchEngine.finish();
                attachmentBase.finish();
            } catch (final OXException e) {
                LOG.error("", e);
            }
            Streams.close(in);
        }

        w.object();
        w.key(ResponseFields.DATA).value(newDocument.getId());
        w.endObject();
    }

    protected void update(final int id, final DocumentMetadata updated, final long timestamp, final Metadata[] presentFields) {
        final InfostoreFacade infostore = getInfostore(updated.getFolderId());
        final InfostoreSearchEngine searchEngine = getSearchEngine();

        try {

            infostore.startTransaction();
            searchEngine.startTransaction();

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

            infostore.saveDocumentMetadata(updated, timestamp, presentFields, session);

            infostore.commit();
            searchEngine.commit();
        } catch (final Throwable t) {
            try {
                infostore.rollback();
                searchEngine.rollback();
            } catch (final OXException e) {
                LOG.error("", e);
            }
            handle(t, session);
            return;
        } finally {
            try {
                infostore.finish();
                searchEngine.finish();
            } catch (final OXException e) {
                LOG.error("", e);
            }

        }
        try {
            w.object();
            w.key(ResponseFields.TIMESTAMP);
            w.value(updated.getLastModified().getTime());
            w.endObject();
        } catch (final JSONException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    protected void copy(final int id, final DocumentMetadata updated, final long timestamp,
            final Metadata[] presentFields) {

        final InfostoreFacade infostore = getInfostore(updated.getFolderId());
        final InfostoreSearchEngine searchEngine = getSearchEngine();
        DocumentMetadata metadata = null;

        try {

            infostore.startTransaction();
            searchEngine.startTransaction();

            metadata = new DocumentMetadataImpl(infostore.getDocumentMetadata(id, InfostoreFacade.CURRENT_VERSION,
                    ctx, user, userPermissionBits));

            final SetSwitch set = new SetSwitch(metadata);
            final GetSwitch get = new GetSwitch(updated);
            for (final Metadata field : presentFields) {
                final Object value = field.doSwitch(get);
                set.setValue(value);
                field.doSwitch(set);
                // System.out.println(field+" : "+value);
            }
            metadata.setVersion(0);
            metadata.setId(InfostoreFacade.NEW);

            if (metadata.getFileName() != null && !"".equals(metadata.getFileName())) {
                infostore.saveDocument(metadata, infostore.getDocument(id, InfostoreFacade.CURRENT_VERSION, ctx,
                        user, userPermissionBits), metadata.getSequenceNumber(), session);
            } else {
                infostore.saveDocumentMetadata(metadata, timestamp, session);
            }
            searchEngine.index(metadata, ctx, user, userPermissionBits);

            infostore.commit();
            searchEngine.commit();
        } catch (final Throwable t) {
            try {
                infostore.rollback();
                searchEngine.rollback();
            } catch (final OXException e) {
                LOG.error("", e);
            }
            handle(t, session);
            return;
        } finally {
            try {
                infostore.finish();
                searchEngine.finish();
            } catch (final OXException e) {
                LOG.debug("", e);
            }

        }
        try {
            w.object();
            w.key(ResponseFields.DATA).value(metadata.getId());
            w.endObject();
        } catch (final JSONException e) {
            LOG.error(e.getMessage(), e);
        }

    }

    protected void lock(final int id, final long diff) {
        final InfostoreFacade infostore = getInfostore();

        try {
            infostore.startTransaction();

            infostore.lock(id, diff, session);

            infostore.commit();

            final DocumentMetadata currentVersion = infostore.getDocumentMetadata(id, InfostoreFacade.CURRENT_VERSION, ctx,
                    user, userPermissionBits);

            try {
                w.object();
                w.key(ResponseFields.TIMESTAMP);
                w.value(currentVersion.getLastModified().getTime());
                w.endObject();
            } catch (final JSONException e) {
                LOG.error(e.getMessage(), e);
            }

        } catch (final Throwable t) {
            try {
                infostore.rollback();
            } catch (final OXException e) {
                LOG.error("", e);
            }
            handle(t, session);
        } finally {
            try {
                infostore.finish();
            } catch (final OXException e) {
                LOG.error("", e);
            }
        }
    }

    protected void unlock(final int id) {
        final InfostoreFacade infostore = getInfostore();

        try {
            infostore.startTransaction();

            /* DocumentMetadata m = */new DocumentMetadataImpl();

            infostore.unlock(id, session);

            infostore.commit();

            final DocumentMetadata currentVersion = infostore.getDocumentMetadata(id, InfostoreFacade.CURRENT_VERSION, ctx,
                                user, userPermissionBits);

            try {
                w.object().key(ResponseFields.TIMESTAMP).value(currentVersion.getLastModified().getTime()).endObject();
            } catch (final JSONException e) {
                LOG.error(e.getMessage(), e);
            }

        } catch (final Throwable t) {
            try {
                infostore.rollback();
            } catch (final OXException e) {
                LOG.error("", e);
            }
            handle(t, session);
        } finally {
            try {
                infostore.finish();
            } catch (final OXException e) {
                LOG.debug("", e);
            }
        }

    }

    protected void search(final String query, final Metadata[] cols, final int folderId, final Metadata sortedBy,
            final int dir, final int start, final int end, final String timeZoneId) {
        final InfostoreSearchEngine searchEngine = getSearchEngine();

        try {
            searchEngine.startTransaction();

            SearchIterator<DocumentMetadata> results = searchEngine.search(query, cols, folderId, sortedBy, dir, start, end,
                    ctx, user, userPermissionBits);

            if (Metadata.CREATED_BY_LITERAL.equals(sortedBy)) {
                results = CreatedByComparator.resort(results, new CreatedByComparator(user.getLocale(), ctx).setDescending(dir < 0));
            }

            final InfostoreWriter iWriter = new InfostoreWriter(w);
            iWriter.timedResult(System.currentTimeMillis());
            iWriter.writeMetadata(results, cols, TimeZoneUtils.getTimeZone(null == timeZoneId ? user.getTimeZone() : timeZoneId));
            iWriter.endTimedResult();

            searchEngine.commit();
        } catch (final Throwable t) {
            try {
                searchEngine.rollback();
            } catch (final OXException x) {
                LOG.debug("", x);
            }
            handle(t, session);
        } finally {
            try {
                searchEngine.finish();
            } catch (final OXException x) {
                LOG.error("", x);
            }
        }
    }

    protected InfostoreFacade getInfostore() {
        return Infostore.FACADE;
    }

    protected InfostoreFacade getInfostore(final long folderId) {
        return Infostore.getInfostore(folderId);
    }

    protected InfostoreSearchEngine getSearchEngine() {
        return Infostore.SEARCH_ENGINE;
    }

}
