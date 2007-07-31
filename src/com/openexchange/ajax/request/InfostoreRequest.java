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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONWriter;

import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Attachment;
import com.openexchange.ajax.Infostore;
import com.openexchange.ajax.parser.InfostoreParser;
import com.openexchange.ajax.parser.InfostoreParser.UnknownMetadataException;
import com.openexchange.ajax.writer.InfostoreWriter;
import com.openexchange.api.OXConflictException;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.attach.AttachmentField;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.SearchEngine;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.groupware.infostore.database.impl.GetSwitch;
import com.openexchange.groupware.infostore.database.impl.SetSwitch;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.results.Delta;
import com.openexchange.groupware.results.TimedResult;
import com.openexchange.groupware.tx.TransactionException;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.exceptions.LoggingLogic;
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;

public class InfostoreRequest extends CommonRequest{
	
	private static final InfostoreParser PARSER = new InfostoreParser();
	
	private SessionObject sessionObj;

	private static final Log LOG = LogFactory.getLog(InfostoreRequest.class);
	private static final LoggingLogic LL = LoggingLogic.getLoggingLogic(InfostoreRequest.class);
	
	public InfostoreRequest(final SessionObject sessionObj, final JSONWriter w) {
		super(w);
		this.sessionObj = sessionObj;
	}
		
	public boolean action(final String action, final SimpleRequest req){
		try {
			if (action.equals(AJAXServlet.ACTION_ALL)) {

				if (!checkRequired(req, AJAXServlet.PARAMETER_FOLDERID, AJAXServlet.PARAMETER_COLUMNS)) {
					return true;
				}

				doSortedSearch(req);
				return true;
			} else if (action.equals(AJAXServlet.ACTION_UPDATES)) {
				if (!checkRequired(req, AJAXServlet.PARAMETER_FOLDERID, AJAXServlet.PARAMETER_COLUMNS, AJAXServlet.PARAMETER_TIMESTAMP)) {
					return true;
				}
				
				doSortedSearch(req);

				return true;
			} else if (action.equals(AJAXServlet.ACTION_GET)) {
				if(!checkRequired(req, AJAXServlet.PARAMETER_ID)) {
					return true;
				}
				final int id = Integer.parseInt(req.getParameter(AJAXServlet.PARAMETER_ID));
				
				int version = InfostoreFacade.CURRENT_VERSION;
				
				if(req.getParameter(AJAXServlet.PARAMETER_VERSION) != null) {
					version = Integer.parseInt(req.getParameter(AJAXServlet.PARAMETER_VERSION));
				}
				
				get(id, version);
				
				return true;
			} else if (action.equals(AJAXServlet.ACTION_VERSIONS)) {
				if(!checkRequired(req,AJAXServlet.PARAMETER_ID, AJAXServlet.PARAMETER_COLUMNS)) {
					return true;
				}
				
				doSortedSearch(req);
				
				return true;
			} else if (action.equals(AJAXServlet.ACTION_REVERT)) {
				if(!checkRequired(req, AJAXServlet.PARAMETER_ID, AJAXServlet.PARAMETER_TIMESTAMP)) {
					return true;
				}
				final int id = Integer.parseInt(req.getParameter(AJAXServlet.PARAMETER_ID));
				final long ts = Long.parseLong(req.getParameter(AJAXServlet.PARAMETER_TIMESTAMP));
				
				revert(id,ts);
				
				return true;
			} else if(action.equals(AJAXServlet.ACTION_LIST)) {
				if(!checkRequired(req, AJAXServlet.PARAMETER_COLUMNS)){
					return true;
				}
				final JSONArray array = (JSONArray) req.getBody();
				final int[] ids = parseIDList(array);
				
				Metadata[] cols = null;
				
				try {
					cols = PARSER.getColumns(req.getParameterValues(AJAXServlet.PARAMETER_COLUMNS));
				} catch (final InfostoreParser.UnknownMetadataException x) {
					unknownColumn(x.getColumnId());
					return true;
				}
				
				list(ids, cols);
				
				return true;
			} else if (action.equals(AJAXServlet.ACTION_DELETE)) {
				if (!checkRequired(req, AJAXServlet.PARAMETER_TIMESTAMP)) {
					return true;
				}
				final JSONArray array = (JSONArray) req.getBody();
				final int[] ids = parseIDList(array);
				final long timestamp = Long.parseLong(req.getParameter(AJAXServlet.PARAMETER_TIMESTAMP));
				delete(ids,timestamp);
				return true;
			} else if (action.equals(AJAXServlet.ACTION_DETACH)) {
				if(!checkRequired(req, AJAXServlet.PARAMETER_TIMESTAMP, AJAXServlet.PARAMETER_ID)) {
					return true;
				}
				final JSONArray array = (JSONArray) req.getBody();
				final long timestamp = Long.parseLong(req.getParameter(AJAXServlet.PARAMETER_TIMESTAMP));
				final int id = Integer.parseInt(req.getParameter(AJAXServlet.PARAMETER_ID));
				
				final int[] versions = new int[array.length()];
				for(int i = 0; i < array.length(); i++) {
					versions[i] = array.getInt(i);
				}
				
				detach(id,versions,timestamp);
				return true;
			} else if (action.equals(AJAXServlet.ACTION_UPDATE) || action.equals(AJAXServlet.ACTION_COPY)){
				
				if(!checkRequired(req, AJAXServlet.PARAMETER_ID, AJAXServlet.PARAMETER_TIMESTAMP)) {
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
				
				if(action.equals(AJAXServlet.ACTION_UPDATE)) {
					update(id, updated,timestamp, presentFields);
				} else {
					copy(id,updated,timestamp,presentFields);
				}
				return true;
			} else if (action.equals(AJAXServlet.ACTION_NEW)){
				
				final DocumentMetadata newDocument = PARSER.getDocumentMetadata(req.getBody().toString());
				//newDocument.setFolderId(new Long(req.getParameter(PARAMETER_FOLDERID)));
				newDocument(newDocument);
				return true;
			} else if (action.equals(AJAXServlet.ACTION_LOCK)) {
				if(!checkRequired(req,action,AJAXServlet.PARAMETER_ID)) {
					return true;
				}
				long diff = -1;
				if(null != req.getParameter("diff")) {
					diff = Long.parseLong(req.getParameter("diff"));
				}
				final int id = Integer.parseInt(req.getParameter(AJAXServlet.PARAMETER_ID));
				lock(id,diff);
				
				return true;
				
			} else if (action.equals(AJAXServlet.ACTION_UNLOCK)) {
				if(!checkRequired(req,action,AJAXServlet.PARAMETER_ID)) {
					return true;
				}
				unlock(Integer.parseInt(req.getParameter(AJAXServlet.PARAMETER_ID)));
				
				return true;
			} else if (action.equals(AJAXServlet.ACTION_SEARCH)) {
				if(! checkRequired(req,action,AJAXServlet.PARAMETER_COLUMNS)) {
					return true;
				}
				
				doSortedSearch(req);
				return true;
			} else if (action.equals(AJAXServlet.ACTION_SAVE_AS)) {
				if(!checkRequired(req,action,AJAXServlet.PARAMETER_FOLDERID, AJAXServlet.PARAMETER_ATTACHEDID, AJAXServlet.PARAMETER_MODULE, AJAXServlet.PARAMETER_ATTACHMENT)) {
					return true;
				}
				final int folderId = Integer.parseInt(req.getParameter(AJAXServlet.PARAMETER_FOLDERID));
				final int attachedId = Integer.parseInt(req.getParameter(AJAXServlet.PARAMETER_ATTACHEDID));
				final int moduleId = Integer.parseInt(req.getParameter(AJAXServlet.PARAMETER_MODULE));
				final int attachment = Integer.parseInt(req.getParameter(AJAXServlet.PARAMETER_ATTACHMENT));
				
				final String body = req.getBody().toString();
				final DocumentMetadata newDocument = PARSER.getDocumentMetadata(body);
				final Metadata[] fields = PARSER.findPresentFields(body);
				saveAs(newDocument,fields,folderId,attachedId,moduleId,attachment);
				return true;

			}
			return false;
		} catch (final JSONException x) {
			handle(x);
			return true;
		} catch (final UnknownMetadataException x) {
			handle(x);
			return true;
		} catch (final OXException x) {
			handle(x);
			return true;
		} catch (final SearchIteratorException x) {
			handle(x);
			return true;
		} catch (final NumberFormatException x) {
			handle(x);
			return true;
		} catch (final Throwable t) {
			handle(t);
			return true;
		}
	}

	protected int[] parseIDList(final JSONArray array) throws JSONException {
		final int[] ids = new int[array.length()];
		
		for(int i = 0; i < array.length(); i++) {
			final JSONObject tuple = array.getJSONObject(i);
			try {
				ids[i] = tuple.getInt(AJAXServlet.PARAMETER_ID);
			} catch (final JSONException x) {
				ids[i] = Integer.parseInt(tuple.getString(AJAXServlet.PARAMETER_ID));
			}
		}
		return ids;
	}
	
	protected void doSortedSearch(final SimpleRequest req) throws JSONException, SearchIteratorException {
		Metadata[] cols = null;
		
		try {
			cols = PARSER.getColumns(req.getParameterValues(AJAXServlet.PARAMETER_COLUMNS));
		} catch (final InfostoreParser.UnknownMetadataException x) {
			unknownColumn(x.getColumnId());
			return;
		}
		
		final String sort = req.getParameter(AJAXServlet.PARAMETER_SORT);
		final String order = req.getParameter(AJAXServlet.PARAMETER_ORDER);
		
		
		if(order != null && !checkRequired(req, AJAXServlet.PARAMETER_SORT)){
			return;
		}
		
		Metadata sortedBy = null;
		int dir = -23;
		
		if(sort != null) {
			
			dir = InfostoreFacade.ASC;
			if(order != null && order.equalsIgnoreCase("DESC")) {
				//if(order.equalsIgnoreCase("DESC")) {
					dir = InfostoreFacade.DESC;
				//}
			}
			sortedBy = Metadata.get(Integer.parseInt(sort));
			if(sortedBy == null) {
				invalidParameter(AJAXServlet.PARAMETER_SORT, sort);
				return;
			}
		}
		
		final String action = req.getParameter(AJAXServlet.PARAMETER_ACTION);
			
		if(action.equals(AJAXServlet.ACTION_ALL)) {
			if(!checkRequired(req,action,AJAXServlet.PARAMETER_FOLDERID)) {
				return;
			}
			final int folderId = Integer.parseInt(req.getParameter(AJAXServlet.PARAMETER_FOLDERID));
			all(folderId,cols,sortedBy,dir);
		} else if (action.equals(AJAXServlet.ACTION_VERSIONS)) {
			if(!checkRequired(req,action,AJAXServlet.PARAMETER_ID)) {
				return;
			}
			final int id = Integer.parseInt(req.getParameter(AJAXServlet.PARAMETER_ID));
			versions(id,cols,sortedBy,dir);
		} else if (action.equals(AJAXServlet.ACTION_UPDATES)) {
			if(!checkRequired(req,action,AJAXServlet.PARAMETER_FOLDERID, AJAXServlet.PARAMETER_TIMESTAMP)) {
				return;
			}
			final int folderId = Integer.parseInt(req.getParameter(AJAXServlet.PARAMETER_FOLDERID));
			final long timestamp = Long.parseLong(req.getParameter(AJAXServlet.PARAMETER_TIMESTAMP));
			final String delete = req.getParameter(AJAXServlet.PARAMETER_IGNORE);
			updates(folderId,cols,sortedBy,dir,timestamp,delete != null && delete.equals("deleted"));
			
		} else if (action.equals(AJAXServlet.ACTION_SEARCH)){
			final JSONObject queryObject = (JSONObject) req.getBody();
			final String query = queryObject.getString("pattern");
			
			int folderId = SearchEngine.NO_FOLDER;
			final String folderS = req.getParameter(AJAXServlet.PARAMETER_FOLDERID);
			if(null != folderS) {
				folderId = Integer.parseInt(folderS);
			}
			
			int start = SearchEngine.NOT_SET;
			final String startS = req.getParameter(AJAXServlet.PARAMETER_START);
			if(null != startS) {
				start = Integer.parseInt(startS);
			}
			
			
			int end = SearchEngine.NOT_SET;
			final String endS = req.getParameter(AJAXServlet.PARAMETER_END);
			if(null != endS) {
				end = Integer.parseInt(endS);
			}
			
			if(start == SearchEngine.NOT_SET && end == SearchEngine.NOT_SET) {
				final String limitS = req.getParameter(AJAXServlet.PARAMETER_LIMIT);
				if(limitS != null) {
					final int limit = Integer.parseInt(limitS);
					start = 0;
					end = limit-1;
				}
			}
			
			search(query,cols,folderId,sortedBy,dir,start,end);
		}
	}
	
	// Actions

	protected void list(final int[] ids, final Metadata[] cols) throws SearchIteratorException {
		final InfostoreFacade infostore = getInfostore();
		TimedResult result = null;
		SearchIterator iter = null;
		try {
			
			result = infostore.getDocuments(ids,cols,sessionObj.getContext(), sessionObj.getUserObject(), sessionObj.getUserConfiguration());
			
			iter = result.results();
			
			final InfostoreWriter iWriter = new InfostoreWriter(w);
			iWriter.timedResult(result.sequenceNumber());
			iWriter.writeMetadata(iter ,cols,TimeZone.getTimeZone(sessionObj.getUserObject().getTimeZone()));
			iWriter.endTimedResult();
			
		
		} catch (final Throwable t){
			handle(t);
			
		} finally {
			if(iter!=null) {
				iter.close();
			}
		}
	} 
	
	protected void get(final int id , final int version ){
		final InfostoreFacade infostore = getInfostore();
		DocumentMetadata dm = null;
		try {
			
			dm = infostore.getDocumentMetadata(id,version,sessionObj.getContext(), sessionObj.getUserObject(), sessionObj.getUserConfiguration());
			if(dm==null) {
				sendErrorAsJS("Cannot find document: %s ", String.valueOf(id));
			}
		} catch (final Throwable t){
			handle(t);
			return;
		}
				
		try {
			final InfostoreWriter iWriter = new InfostoreWriter(w);
			iWriter.timedResult(dm.getSequenceNumber());
			iWriter.write(dm,TimeZone.getTimeZone(sessionObj.getUserObject().getTimeZone()));
			iWriter.endTimedResult();
		} catch (final JSONException e) {
			LOG.error("", e);
		}
	}
	
	protected void revert(final int id, final long ts) {
		final InfostoreFacade infostore = getInfostore();
		SearchIterator iter = null;
		long timestamp = -1;
		try {
			//SearchENgine?
			infostore.startTransaction();
			infostore.getDocumentMetadata(id,InfostoreFacade.CURRENT_VERSION,sessionObj.getContext(),sessionObj.getUserObject(),sessionObj.getUserConfiguration()).getSequenceNumber();
			final TimedResult result = infostore.getVersions(id,new Metadata[]{Metadata.VERSION_LITERAL}, sessionObj.getContext(), sessionObj.getUserObject(), sessionObj.getUserConfiguration());
			if(timestamp > ts) {
				throw new OXConflictException();
			}
			iter = result.results();
			final List<Integer> versions = new ArrayList<Integer>();
			while(iter.hasNext()) {
				final int version =((DocumentMetadata)iter.next()).getVersion();
				if(version == 0) {
					continue;
				}
				versions.add(Integer.valueOf(version));
			}
			iter.close();
			final int[] versionsArray = new int[versions.size()];
			int index = 0;
			for(final int version : versions) {
				versionsArray[index++] = version;
			}
			infostore.removeVersion(id, versionsArray, sessionObj);
			timestamp = infostore.getDocumentMetadata(id,InfostoreFacade.CURRENT_VERSION,sessionObj.getContext(),sessionObj.getUserObject(),sessionObj.getUserConfiguration()).getSequenceNumber();
			infostore.commit();
			final JSONObject object = new JSONObject();
			object.put("data", new JSONObject());
			object.put("timestamp", timestamp);
			w.value(object);
		} catch (final Throwable t){
			try {
				infostore.rollback();
			} catch (final TransactionException e) {
				LOG.debug("", e);
			}
			handle(t);
			return;
		} finally {
			if(iter!=null) {
				try {
					iter.close();
				} catch (final SearchIteratorException e) {
					LOG.error("", e);
				}
			}
			try {
				infostore.finish();
			} catch (final TransactionException e1) {
				LOG.error("", e1);
			}
		}
	}
	
	protected void all(final int folderId, final Metadata[] cols, final Metadata sortedBy, final int dir) throws SearchIteratorException {
		/**System.out.println("ALL: "+System.currentTimeMillis());
		System.out.println("---------all-------------");
		System.out.println(folderId);
		System.out.println(cols.length);
		for(Metadata m : cols) {
			System.out.println(m.getName());
		}
		System.out.println(sortedBy);
		System.out.println(dir);
		System.out.println("----------all------------");
		*/
		final InfostoreFacade infostore = getInfostore(folderId);
		TimedResult result = null;
		SearchIterator iter = null;
		try {
			
			if(sortedBy == null) {
				result = infostore.getDocuments(folderId,cols,sessionObj.getContext(), sessionObj.getUserObject(), sessionObj.getUserConfiguration());
			} else {
				result = infostore.getDocuments(folderId,cols,sortedBy,dir,sessionObj.getContext(), sessionObj.getUserObject(), sessionObj.getUserConfiguration());
			}
			
			iter = result.results();
			
			final InfostoreWriter iWriter = new InfostoreWriter(w);
			iWriter.timedResult(result.sequenceNumber());
			iWriter.writeMetadata(iter,cols,TimeZone.getTimeZone(sessionObj.getUserObject().getTimeZone()));
			iWriter.endTimedResult();
		
		} catch (final Throwable t){
			handle(t);
			return;
		} finally {
			if(iter!=null) {
				iter.close();
			}
		}
	}
	
	protected void versions(final int id, final Metadata[] cols, final Metadata sortedBy, final int dir) throws SearchIteratorException {
		final InfostoreFacade infostore = getInfostore();
		TimedResult result = null;
		SearchIterator iter = null;
		try {
			
			if(sortedBy == null) {
				result = infostore.getVersions(id,cols,sessionObj.getContext(), sessionObj.getUserObject(), sessionObj.getUserConfiguration());
			} else {
				result = infostore.getVersions(id,cols,sortedBy,dir,sessionObj.getContext(), sessionObj.getUserObject(), sessionObj.getUserConfiguration());
			}
			iter = result.results();
			iter.next(); // Skip version zero
			final InfostoreWriter iWriter = new InfostoreWriter(w);
			iWriter.timedResult(result.sequenceNumber());
			iWriter.writeMetadata(iter,cols,TimeZone.getTimeZone(sessionObj.getUserObject().getTimeZone()));
			iWriter.endTimedResult();
		
		} catch (final Throwable t){
			handle(t);
			return;
		} finally {
			if(iter!=null) {
				iter.close();
			}
		}
	}
	
	protected void updates(final int folderId, final Metadata[] cols, final Metadata sortedBy, final int dir, final long timestamp, final boolean ignoreDelete) throws SearchIteratorException {
		final InfostoreFacade infostore = getInfostore(folderId);
		Delta delta = null;
		
		
		SearchIterator iter = null;
		SearchIterator iter2 = null;
		
		try {
			
			
			if(sortedBy == null) {
				delta = infostore.getDelta(folderId,timestamp,cols,ignoreDelete,sessionObj.getContext(), sessionObj.getUserObject(), sessionObj.getUserConfiguration());
			} else {
				delta = infostore.getDelta(folderId,timestamp,cols,sortedBy,dir,ignoreDelete,sessionObj.getContext(), sessionObj.getUserObject(), sessionObj.getUserConfiguration());
			}
			
			iter = delta.results();
			iter2 = delta.getDeleted();
			
			final InfostoreWriter iWriter = new InfostoreWriter(w);
			iWriter.timedResult(delta.sequenceNumber());
			iWriter.writeDelta(iter, iter2, cols,ignoreDelete,TimeZone.getTimeZone(sessionObj.getUserObject().getTimeZone()));
			iWriter.endTimedResult();
		
		} catch (final Throwable t){
			handle(t);
			return;
		} finally {
			if(iter!=null) {
				iter.close();
			}
			if(iter2!=null) {
				iter2.close();
			}
		}
	}
	
	protected void delete(final int[] ids, final long timestamp) {
		final InfostoreFacade infostore = getInfostore();
		final SearchEngine searchEngine = getSearchEngine();
		
		int[] notDeleted = new int[0];
		if(ids.length!=0) {
			try {
				
				infostore.startTransaction();
				searchEngine.startTransaction();
				
				notDeleted = infostore.removeDocument(ids,timestamp, sessionObj);
				
				final Set<Integer> notDeletedSet = new HashSet<Integer>();
				for(final int nd : notDeleted) { notDeletedSet.add(nd); }
				
				for(final int id : ids) {
					if(!notDeletedSet.contains(Integer.valueOf(id))){
						searchEngine.unIndex0r(id,sessionObj.getContext(), sessionObj.getUserObject(), sessionObj.getUserConfiguration());
					}
				}
				
				infostore.commit();
				searchEngine.commit();
				
			} catch (final Throwable t){
				try {
					infostore.rollback();
					searchEngine.rollback();
					return;
				} catch (final TransactionException e) {
					LOG.error("", e);
				}
				handle(t);
				return;
			} finally {
				try {
					infostore.finish();
					searchEngine.finish();
				} catch (final TransactionException e) {
					LOG.error("", e);
				}
				
			}
		}
		
		final StringBuilder b = new StringBuilder();
		b.append('[');
		for (int i = 0; i < notDeleted.length; i++) {
			final int nd = notDeleted[i];
			b.append(nd);
			if(i != notDeleted.length-1) {
				b.append(',');
			}
		}
		b.append(']');
		// TODO: Use JSONArray instaed of StringBuilder
		try {
			w.value(new JSONArray(b.toString()));
		} catch (final JSONException e) {
			LOG.error(e.getLocalizedMessage(), e);
		}
	}
	
	protected void detach(final int objectId, final int[] ids, final long timestamp) {
		final InfostoreFacade infostore = getInfostore();
		final SearchEngine searchEngine = getSearchEngine();
		
		int[] notDetached = new int[0];
		if(ids.length!=0) {
			try {
				
				infostore.startTransaction();
				searchEngine.startTransaction();
				
				notDetached = infostore.removeVersion(objectId,ids,sessionObj);
				
				searchEngine.index(infostore.getDocumentMetadata(objectId, InfostoreFacade.CURRENT_VERSION,sessionObj.getContext(), sessionObj.getUserObject(), sessionObj.getUserConfiguration()), sessionObj.getContext(), sessionObj.getUserObject(), sessionObj.getUserConfiguration());
				
				infostore.commit();
				searchEngine.commit();
			} catch (final Throwable t){
				try {
					infostore.rollback();
					searchEngine.rollback();
				} catch (final TransactionException e) {
					LOG.error("",e);
				}
				handle(t);
				return;
			} finally {
				try {
					infostore.finish();
					searchEngine.finish();
				} catch (final TransactionException e) {
					LOG.error("", e);
				}
				
			}
		}
		
		final StringBuilder b = new StringBuilder();
		b.append('[');
		for (int i = 0; i < notDetached.length; i++) {
			final int nd = notDetached[i];
			b.append(nd);
			if(i != notDetached.length-1) {
				b.append(',');
			}
		}
		b.append(']');
		// TODO: Use JSONArray instaed of StringBuilder
		try {
			w.value(new JSONArray(b.toString()));
		} catch (final JSONException e) {
			LOG.error(e.getLocalizedMessage(), e);
		}
	}
	
	protected void newDocument(final DocumentMetadata newDocument) throws JSONException {
		final InfostoreFacade infostore = getInfostore(newDocument.getFolderId());
		final SearchEngine searchEngine = getSearchEngine();
		try {
			
			infostore.startTransaction();
			searchEngine.startTransaction();
			
			infostore.saveDocumentMetadata(newDocument,System.currentTimeMillis(),sessionObj);
			infostore.commit();
			//System.out.println("DONE SAVING: "+System.currentTimeMillis());
			searchEngine.index(newDocument,sessionObj.getContext(), sessionObj.getUserObject(), sessionObj.getUserConfiguration());
			searchEngine.commit();
		} catch (final Throwable t){
			try {
				infostore.rollback();
				searchEngine.rollback();
				
			} catch (final TransactionException e) {
				LOG.error("", e);
			}
			handle(t);
			return;
		} finally {
			try {
				infostore.finish();
				searchEngine.finish();
			} catch (final TransactionException e) {
				LOG.error("",e);
			}	
		}
		final JSONObject obj = new JSONObject();
		obj.put("data", newDocument.getId());
		w.value(obj);
	}
	
	protected void saveAs(final DocumentMetadata newDocument, final Metadata[] fields, final int folderId, final int attachedId, final int moduleId, final int attachment) throws JSONException {
		final Set<Metadata> alreadySet = new HashSet<Metadata>(Arrays.asList(fields));
		if(!alreadySet.contains(Metadata.FOLDER_ID_LITERAL)) {
			missingParameter("folder_id in object",AJAXServlet.ACTION_SAVE_AS);
//			try {
//				missingParameter("folder_id in object",AJAXServlet.ACTION_SAVE_AS);
//			} catch (final IOException e1) {
//				LOG.debug("", e1);
//			}
		}
		
		final AttachmentBase attachmentBase = Attachment.ATTACHMENT_BASE;
		final InfostoreFacade infostore = getInfostore(newDocument.getFolderId());
		final SearchEngine searchEngine = getSearchEngine();
		InputStream in = null;
		try {
			attachmentBase.startTransaction();
			infostore.startTransaction();
			searchEngine.startTransaction();
			
			final AttachmentMetadata att = attachmentBase.getAttachment(folderId,attachedId,moduleId,attachment,sessionObj.getContext(),sessionObj.getUserObject(), sessionObj.getUserConfiguration());
			final com.openexchange.groupware.attach.util.GetSwitch get = new com.openexchange.groupware.attach.util.GetSwitch(att);
			final SetSwitch set = new SetSwitch(newDocument);
			
			for(final Metadata attachmentCompatible : Metadata.VALUES) {
				if(alreadySet.contains(attachmentCompatible)) {
					continue;
				}
				final AttachmentField attField = Metadata.getAttachmentField(attachmentCompatible);
				if(null == attField) {
					continue;
				}
				final Object value = attField.doSwitch(get);
				set.setValue(value);
				attachmentCompatible.doSwitch(set);
			}
			newDocument.setId(InfostoreFacade.NEW);
			in = attachmentBase.getAttachedFile(folderId,attachedId,moduleId,attachment,sessionObj.getContext(), sessionObj.getUserObject(), sessionObj.getUserConfiguration());
			infostore.saveDocument(newDocument,in, System.currentTimeMillis(),sessionObj); // FIXME violates encapsulation
			
			//System.out.println("DONE SAVING: "+System.currentTimeMillis());
			searchEngine.index(newDocument,sessionObj.getContext(), sessionObj.getUserObject(), sessionObj.getUserConfiguration());
			
			infostore.commit();
			searchEngine.commit();
			attachmentBase.commit();
		} catch (final Throwable t){
			try {
				infostore.rollback();
				searchEngine.rollback();
				attachmentBase.rollback();
			} catch (final TransactionException e) {
				LOG.error("", e);
			}
			handle(t);
			return;
		} finally {
			try {
				infostore.finish();
				searchEngine.finish();
				attachmentBase.finish();
			} catch (final TransactionException e) {
				LOG.error("", e);
			}
			if(in != null) {
				try {
					in.close();
				} catch (final IOException e) {
					LOG.debug("", e);
				}
			}
		}
		
		final JSONObject obj = new JSONObject();
		obj.put("data", newDocument.getId());
		w.value(obj);
	}
	
	protected void update(final int id, DocumentMetadata updated, final long timestamp, final Metadata[] presentFields) {
		final InfostoreFacade infostore = getInfostore(updated.getFolderId());
		final SearchEngine searchEngine = getSearchEngine();
		
		try {
			
			infostore.startTransaction();
			searchEngine.startTransaction();
			
			boolean version = false;
			for(final Metadata m : presentFields) {
				if(m.equals(Metadata.VERSION_LITERAL)){
					version = true;
					break;
				}
			}
			if(!version){
				updated.setVersion(InfostoreFacade.CURRENT_VERSION);
			}
			
			infostore.saveDocumentMetadata(updated,timestamp,presentFields,sessionObj);
						
			infostore.commit();
			searchEngine.commit();
		} catch (final Throwable t){
			try {
				infostore.rollback();
				searchEngine.rollback();
			} catch (final TransactionException e) {
				LOG.error("", e);
			}
			handle(t);
			return;
		} finally {
			try {
				infostore.finish();
				searchEngine.finish();
			} catch (final TransactionException e) {
				LOG.error("", e);
			}
			
		}
		try {
			w.value(new JSONObject());
		} catch (final JSONException e) {
			LOG.error(e.getLocalizedMessage(), e);
		}
	}
	
	protected void copy(final int id, final DocumentMetadata updated, final long timestamp, final Metadata[] presentFields) {

		final InfostoreFacade infostore = getInfostore(updated.getFolderId());
		final SearchEngine searchEngine = getSearchEngine();
		DocumentMetadata metadata = null;
		
		try {
			
			infostore.startTransaction();
			searchEngine.startTransaction();
			
			
			metadata = new DocumentMetadataImpl(infostore.getDocumentMetadata(id,InfostoreFacade.CURRENT_VERSION,sessionObj.getContext(), sessionObj.getUserObject(), sessionObj.getUserConfiguration()));
			
			
			final SetSwitch set = new SetSwitch(metadata);
			final GetSwitch get = new GetSwitch(updated);
			for(final Metadata field : presentFields) {
				final Object value = field.doSwitch(get);
				set.setValue(value);
				field.doSwitch(set);
				//System.out.println(field+" : "+value);
			}
			metadata.setVersion(0);
			metadata.setId(InfostoreFacade.NEW);
			
			if(metadata.getFileName() != null && !"".equals(metadata.getFileName())) {
				infostore.saveDocument(metadata,infostore.getDocument(id,InfostoreFacade.CURRENT_VERSION,sessionObj.getContext(), sessionObj.getUserObject(), sessionObj.getUserConfiguration()),metadata.getSequenceNumber(),sessionObj);
			} else {
				infostore.saveDocumentMetadata(metadata,timestamp,sessionObj);
			}
			searchEngine.index(metadata,sessionObj.getContext(), sessionObj.getUserObject(), sessionObj.getUserConfiguration());
			
			infostore.commit();
			searchEngine.commit();
		} catch (final Throwable t){
			try {
				infostore.rollback();
				searchEngine.rollback();
			} catch (final TransactionException e) {
				LOG.error("", e);
			}
			handle(t);
			return;
		} finally {
			try {
				infostore.finish();
				searchEngine.finish();
			} catch (final TransactionException e) {
				LOG.debug("", e);
			}
			
		}
		try {
			final JSONObject obj = new JSONObject();
			obj.put("data", metadata.getId());
			w.value(obj);
		} catch (final JSONException e) {
			LOG.error(e.getLocalizedMessage(), e);
		}
		
	}

	protected void lock(final int id, final long diff) {
		final InfostoreFacade infostore = getInfostore();
		
		try {
			infostore.startTransaction();
			
			infostore.lock(id,diff,sessionObj);
			
			infostore.commit();
			
			try {
				w.value(new JSONObject());
			} catch (final JSONException e) {
				LOG.error(e.getLocalizedMessage(), e);
			}
			
		} catch (final Throwable t) {
			try {
				infostore.rollback();
			} catch (final TransactionException e) {
				LOG.error("", e);
			}
			handle(t);
		} finally {
			try {
				infostore.finish();
			} catch (final TransactionException e) {
				LOG.error("", e);
			}
		}
	}
	
	protected void unlock(final int id) {
		final InfostoreFacade infostore = getInfostore();
		
		try {
			infostore.startTransaction();
			
			/*DocumentMetadata m = */new DocumentMetadataImpl();
			
			infostore.unlock(id, sessionObj);
			
			infostore.commit();
			try {
				w.value(new JSONObject());
			} catch (final JSONException e) {
				LOG.error(e.getLocalizedMessage(), e);
			}
			
		} catch (final Throwable t) {
			try {
				infostore.rollback();
			} catch (final TransactionException e) {
				LOG.error("", e);
			}
			handle(t);
		} finally {
			try {
				infostore.finish();
			} catch (final TransactionException e) {
				LOG.debug("", e);
			}
		}
		
		
	}
	
	protected void search(final String query, final Metadata[] cols, final int folderId, final Metadata sortedBy, final int dir, final int start, final int end) {
		final SearchEngine searchEngine = getSearchEngine();
		
		try {
			searchEngine.startTransaction();
			
			final SearchIterator results = searchEngine.search(query,cols,folderId,sortedBy,dir,start,end, sessionObj.getContext(), sessionObj.getUserObject(), sessionObj.getUserConfiguration());
			
			final InfostoreWriter iWriter = new InfostoreWriter(w);
			iWriter.timedResult(System.currentTimeMillis());
			iWriter.writeMetadata(results,cols,TimeZone.getTimeZone(sessionObj.getUserObject().getTimeZone()));
			iWriter.endTimedResult();
			
			searchEngine.commit();
		} catch (final Throwable t) {
			try {
				searchEngine.rollback();
			} catch (final TransactionException x) {
				LOG.debug("", x);
			}
			handle(t);
		} finally {
			try {
				searchEngine.finish();
			} catch (final TransactionException x){
				LOG.error("", x);
			}
		}
	}
	
	
	protected InfostoreFacade getInfostore(){
		return Infostore.FACADE;
	}
	
	protected InfostoreFacade getInfostore(final long folderId) {
		return Infostore.getInfostore(folderId);
	}
	
	protected SearchEngine getSearchEngine(){
		return Infostore.SEARCH_ENGINE;
	}
	
}