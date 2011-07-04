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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.json.FileMetadataWriter;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.servlet.AjaxException;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link AbstractFileAction}
 * 
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public abstract class AbstractFileAction implements AJAXActionService {
    private static final Log LOG = LogFactory.getLog(AbstractFileAction.class);
    
    
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

        private Param(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    protected FileMetadataWriter getWriter() {
        return fileWriter;
    }

    public abstract AJAXRequestResult handle(InfostoreRequest request) throws AbstractOXException;

    public AJAXRequestResult result(TimedResult<File> documents, InfostoreRequest request) throws AbstractOXException {
        SearchIterator<File> results = documents.results();
        return results(results, documents.sequenceNumber() , request);
    }

    protected AJAXRequestResult results(SearchIterator<File> results, long timestamp, InfostoreRequest request) throws AbstractOXException {
        try {
            return new AJAXRequestResult(results, new Date(timestamp), "infostore");
        } finally {
            results.close();
        }
    
    }

    public AJAXRequestResult result(Delta<File> delta, InfostoreRequest request) throws AbstractOXException {
        SearchIterator<File> results = delta.results();
        JSONArray array = null;
        try {
            array = getWriter().write(results, request.getColumns(), request.getTimezone());
        } finally {
            results.close();
        }
        SearchIterator<File> deleted = delta.getDeleted();
        try {
            while (deleted.hasNext()) {
                array.put(deleted.next().getId());
            }
        } finally {
            deleted.close();
        }

        return new AJAXRequestResult(array, new Date(delta.sequenceNumber()));
    }

    public AJAXRequestResult result(File file, InfostoreRequest request) throws AbstractOXException {
        return new AJAXRequestResult(file, new Date(file.getSequenceNumber()), "infostore");
    }


    public AJAXRequestResult result(List<String> ids, InfostoreRequest request) throws AbstractOXException {
        JSONArray array = new JSONArray();
        try {
            for (String id : ids) {
                JSONObject object = new JSONObject();
                object.put("id", id);
                object.put("folder", request.getFolderForID(id));
                array.put(object);
            }
        } catch (JSONException x) {
            throw AjaxExceptionCodes.JSONError.create( x.getMessage());
        }

        return new AJAXRequestResult(array);
    }
    
    
    public AJAXRequestResult result(int[] versions, long sequenceNumber, InfostoreRequest request) throws AbstractOXException {
        JSONArray array = new JSONArray();
        for (int i : versions) {
            array.put(i);
        }
        
        return new AJAXRequestResult(array, new Date(sequenceNumber));
    }
    
    public AJAXRequestResult success(long sequenceNumber) {
        return new AJAXRequestResult(true, new Date(sequenceNumber));
    }
    
    public AJAXRequestResult perform(AJAXRequestData request, ServerSession session) throws OXException {
        AJAXInfostoreRequest req = new AJAXInfostoreRequest(request, session);
        try {
            before(req);
            AJAXRequestResult result = handle(req);
            success(req, result);
            return result;
        } catch (AbstractOXException x) {
            failure(req,x);
            throw x;
        } catch (Throwable t) {
            failure(req,t);
            LOG.error(t.getMessage(), t);
            throw AjaxExceptionCodes.UnexpectedError.create( t.getMessage());
        } finally {
            after(req);
            
            // Delete tmp files
            if (request.hasUploads()) {
                request.getUploadEvent().cleanUp();
            }
        }
    }

    protected void after(AJAXInfostoreRequest req) throws AbstractOXException{
        req.getFileAccess().finish();
    }

   
    protected void failure(AJAXInfostoreRequest req, Throwable throwable) throws AbstractOXException{
        
    }

    
    protected void success(AJAXInfostoreRequest req, AJAXRequestResult result) throws AbstractOXException{
        
    }

   
    protected void before(AJAXInfostoreRequest req) throws AbstractOXException {
        
    }


}
