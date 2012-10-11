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

package com.openexchange.file.storage.json.actions.files;

import java.util.Date;
import java.util.List;
import org.apache.commons.logging.Log;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.json.FileMetadataWriter;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.log.LogFactory;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractFileAction}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public abstract class AbstractFileAction implements AJAXActionService {
    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(AbstractFileAction.class));


    private static final FileMetadataWriter fileWriter = new FileMetadataWriter();

    public static enum Param {
        ID("id"),
        FOLDER_ID("folder"),
        VERSION("version"),
        COLUMNS("columns"),
        SORT("sort"),
        ORDER("order"),
        TIMEZONE("timezone"),
        TIMESTAMP("timestamp"),
        IGNORE("ignore"),
        DIFF("diff"),
        ATTACHED_ID("attached"),
        MODULE("module"),
        ATTACHMENT("attachment");

        String name;

        private Param(final String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    protected FileMetadataWriter getWriter() {
        return fileWriter;
    }

    public abstract AJAXRequestResult handle(InfostoreRequest request) throws OXException;

    public AJAXRequestResult result(final TimedResult<File> documents, final InfostoreRequest request) throws OXException {
        final SearchIterator<File> results = documents.results();
        return results(results, documents.sequenceNumber() , request);
    }

    protected AJAXRequestResult results(final SearchIterator<File> results, final long timestamp, final InfostoreRequest request) throws OXException {
        return new AJAXRequestResult(results, new Date(timestamp), "infostore");
    }

    public AJAXRequestResult result(final Delta<File> delta, final InfostoreRequest request) throws OXException {
        final SearchIterator<File> results = delta.results();
        JSONArray array = null;
        try {
            array = getWriter().write(results, request.getColumns(), request.getTimezone());
        } finally {
            results.close();
        }
        final SearchIterator<File> deleted = delta.getDeleted();
        try {
            while (deleted.hasNext()) {
                array.put(deleted.next().getId());
            }
        } finally {
            deleted.close();
        }

        return new AJAXRequestResult(array, new Date(delta.sequenceNumber()));
    }

    public AJAXRequestResult result(final File file, final InfostoreRequest request) throws OXException {
        return new AJAXRequestResult(file, new Date(file.getSequenceNumber()), "infostore");
    }


    public AJAXRequestResult result(final List<String> ids, final InfostoreRequest request) throws OXException {
        final JSONArray array = new JSONArray();
        try {
            for (final String id : ids) {
                final JSONObject object = new JSONObject();
                object.put("id", id);
                object.put("folder", request.getFolderForID(id));
                array.put(object);
            }
        } catch (final JSONException x) {
            throw AjaxExceptionCodes.JSON_ERROR.create( x.getMessage());
        }

        return new AJAXRequestResult(array);
    }


    public AJAXRequestResult result(final int[] versions, final long sequenceNumber, final InfostoreRequest request) throws OXException {
        final JSONArray array = new JSONArray();
        for (final int i : versions) {
            array.put(i);
        }

        return new AJAXRequestResult(array, new Date(sequenceNumber));
    }

    public AJAXRequestResult success(final long sequenceNumber) {
        return new AJAXRequestResult(true, new Date(sequenceNumber));
    }

    @Override
    public AJAXRequestResult perform(final AJAXRequestData requestData, final ServerSession session) throws OXException {
        final AJAXInfostoreRequest req = new AJAXInfostoreRequest(requestData, session);
        try {
            before(req);
            final AJAXRequestResult result = handle(req);
            success(req, result);
            return result;
        } catch (final OXException x) {
            failure(req,x);
            throw x;
        } catch (final NullPointerException e) {
            failure(req,e);
            LOG.error(e.getMessage(), e);
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, "Null dereference.");
        } catch (final RuntimeException e) {
            failure(req,e);
            LOG.error(e.getMessage(), e);
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create(e, e.getMessage());
        } finally {
            after(req);

            // Delete tmp files
            if (requestData.hasUploads()) {
                requestData.getUploadEvent().cleanUp();
            }
        }
    }

    protected void after(final AJAXInfostoreRequest req) throws OXException{
        req.getFileAccess().finish();
    }


    protected void failure(final AJAXInfostoreRequest req, final Throwable throwable) throws OXException{

    }


    protected void success(final AJAXInfostoreRequest req, final AJAXRequestResult result) throws OXException{

    }


    protected void before(final AJAXInfostoreRequest req) throws OXException {

    }

}
