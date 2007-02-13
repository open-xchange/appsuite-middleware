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
import java.io.PrintWriter;
import java.io.Writer;
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
import com.openexchange.groupware.container.FolderObject;
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
import com.openexchange.tools.iterator.SearchIterator;
import com.openexchange.tools.iterator.SearchIteratorException;

public class InfostoreRequest extends CommonRequest{
	
	private static final InfostoreParser PARSER = new InfostoreParser();
	
	private SessionObject sessionObj;

	private static final Log LOG = LogFactory.getLog(InfostoreRequest.class);
	
	public InfostoreRequest(SessionObject sessionObj, Writer w) {
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
				int id = Integer.valueOf(req.getParameter(AJAXServlet.PARAMETER_ID));
				
				int version = InfostoreFacade.CURRENT_VERSION;
				
				if(req.getParameter(AJAXServlet.PARAMETER_VERSION) != null)
					version = Integer.valueOf(req.getParameter(AJAXServlet.PARAMETER_VERSION));
				
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
				int id = Integer.valueOf(req.getParameter(AJAXServlet.PARAMETER_ID));
				long ts = new Long(req.getParameter(AJAXServlet.PARAMETER_TIMESTAMP));
				
				revert(id,ts);
				return true;
			} else if(action.equals(AJAXServlet.ACTION_LIST)) {
				if(!checkRequired(req, AJAXServlet.PARAMETER_COLUMNS)){
					return true;
				}
				JSONArray array = (JSONArray) req.getBody();
				int[] ids = parseIDList(array);
				
				Metadata[] cols = null;
				
				try {
					cols = PARSER.getColumns(req.getParameterValues(AJAXServlet.PARAMETER_COLUMNS));
				} catch (InfostoreParser.UnknownMetadataException x) {
					unknownColumn(x.getColumnId());
					return true;
				}
				
				list(ids, cols);
				
				return true;
			} else if (action.equals(AJAXServlet.ACTION_DELETE)) {
				if (!checkRequired(req, AJAXServlet.PARAMETER_TIMESTAMP)) {
					return true;
				}
				JSONArray array = (JSONArray) req.getBody();
				int[] ids = parseIDList(array);
				long timestamp = new Long(req.getParameter(AJAXServlet.PARAMETER_TIMESTAMP));
				delete(ids,timestamp);
				return true;
			} else if (action.equals(AJAXServlet.ACTION_DETACH)) {
				if(!checkRequired(req, AJAXServlet.PARAMETER_TIMESTAMP, AJAXServlet.PARAMETER_ID)) {
					return true;
				}
				JSONArray array = (JSONArray) req.getBody();
				long timestamp = new Long(req.getParameter(AJAXServlet.PARAMETER_TIMESTAMP));
				int id = Integer.valueOf(req.getParameter(AJAXServlet.PARAMETER_ID));
				
				int[] versions = new int[array.length()];
				for(int i = 0; i < array.length(); i++) {
					versions[i] = array.getInt(i);
				}
				
				detach(id,versions,timestamp);
				return true;
			} else if (action.equals(AJAXServlet.ACTION_UPDATE) || action.equals(AJAXServlet.ACTION_COPY)){
				
				if(!checkRequired(req, AJAXServlet.PARAMETER_ID, AJAXServlet.PARAMETER_TIMESTAMP)) {
					return true;
				}
				
				int id = Integer.valueOf(req.getParameter(AJAXServlet.PARAMETER_ID));
				long timestamp = new Long(req.getParameter(AJAXServlet.PARAMETER_TIMESTAMP));
				
				String updateBody = req.getBody().toString();
				DocumentMetadata updated = PARSER.getDocumentMetadata(updateBody);
				updated.setId(id);
				Metadata[] presentFields = null;
				
				try {
					presentFields = PARSER.findPresentFields(updateBody);
				} catch (UnknownMetadataException x) {
					unknownColumn(x.getColumnId());
					return true;
				}
				
				if(action.equals(AJAXServlet.ACTION_UPDATE))
					update(id, updated,timestamp, presentFields);
				else
					copy(id,updated,timestamp,presentFields);
				return true;
			} else if (action.equals(AJAXServlet.ACTION_NEW)){
				
				DocumentMetadata newDocument = PARSER.getDocumentMetadata(req.getBody().toString());
				//newDocument.setFolderId(new Long(req.getParameter(PARAMETER_FOLDERID)));
				newDocument(newDocument);
				return true;
			} else if (action.equals(AJAXServlet.ACTION_LOCK)) {
				if(!checkRequired(req,action,AJAXServlet.PARAMETER_ID)) {
					return true;
				}
				long diff = -1;
				if(null != req.getParameter("diff")) {
					diff = new Long(req.getParameter("diff"));
				}
				int id = Integer.valueOf(req.getParameter(AJAXServlet.PARAMETER_ID));
				lock(id,diff);
				
				return true;
				
			} else if (action.equals(AJAXServlet.ACTION_UNLOCK)) {
				if(!checkRequired(req,action,AJAXServlet.PARAMETER_ID)) {
					return true;
				}
				unlock(Integer.valueOf(req.getParameter(AJAXServlet.PARAMETER_ID)));
				
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
				int folderId = Integer.valueOf(req.getParameter(AJAXServlet.PARAMETER_FOLDERID));
				int attachedId = Integer.valueOf(req.getParameter(AJAXServlet.PARAMETER_ATTACHEDID));
				int moduleId = Integer.valueOf(req.getParameter(AJAXServlet.PARAMETER_MODULE));
				int attachment = Integer.valueOf(req.getParameter(AJAXServlet.PARAMETER_ATTACHMENT));
				
				String body = req.getBody().toString();
				DocumentMetadata newDocument = PARSER.getDocumentMetadata(body);
				Metadata[] fields = PARSER.findPresentFields(body);
				saveAs(newDocument,fields,folderId,attachedId,moduleId,attachment);
				return true;

			}
			return false;
		} catch (IOException x) {
			LOG.info("Lost contact to client", x);
			return true;
		} catch (JSONException x) {
			handle(x);
			return true;
		} catch (UnknownMetadataException x) {
			handle(x);
			return true;
		} catch (OXException x) {
			handle(x);
			return true;
		} catch (SearchIteratorException x) {
			handle(x);
			return true;
		} catch (NumberFormatException x) {
			handle(x);
			return true;
		} catch (Throwable t) {
			handle(t);
			return true;
		}
	}

	protected int[] parseIDList(JSONArray array) throws JSONException {
		int[] ids = new int[array.length()];
		
		for(int i = 0; i < array.length(); i++) {
			JSONObject tuple = array.getJSONObject(i);
			try {
				ids[i] = tuple.getInt(AJAXServlet.PARAMETER_ID);
			} catch (JSONException x) {
				ids[i] = Integer.valueOf(tuple.getString(AJAXServlet.PARAMETER_ID));
			}
		}
		return ids;
	}
	
	protected void doSortedSearch(SimpleRequest req) throws JSONException, IOException, SearchIteratorException, OXException {
		Metadata[] cols = null;
		
		try {
			cols = PARSER.getColumns(req.getParameterValues(AJAXServlet.PARAMETER_COLUMNS));
		} catch (InfostoreParser.UnknownMetadataException x) {
			unknownColumn(x.getColumnId());
			return;
		}
		
		String sort = req.getParameter(AJAXServlet.PARAMETER_SORT);
		String order = req.getParameter(AJAXServlet.PARAMETER_ORDER);
		
		
		if(order != null && !checkRequired(req, AJAXServlet.PARAMETER_SORT)){
			return;
		}
		
		Metadata sortedBy = null;
		int dir = -23;
		
		if(sort != null) {
			
			dir = InfostoreFacade.ASC;
			if(order != null) {
				if(order.equalsIgnoreCase("DESC"))
					dir = InfostoreFacade.DESC;
			}
			sortedBy = Metadata.get(Integer.valueOf(sort));
			if(sortedBy == null) {
				invalidParameter(AJAXServlet.PARAMETER_SORT, sort);
				return;
			}
		}
		
		String action = req.getParameter(AJAXServlet.PARAMETER_ACTION);
			
		if(action.equals(AJAXServlet.ACTION_ALL)) {
			if(!checkRequired(req,action,AJAXServlet.PARAMETER_FOLDERID)) {
				return;
			}
			int folderId = Integer.valueOf(req.getParameter(AJAXServlet.PARAMETER_FOLDERID));
			all(folderId,cols,sortedBy,dir);
		} else if (action.equals(AJAXServlet.ACTION_VERSIONS)) {
			if(!checkRequired(req,action,AJAXServlet.PARAMETER_ID)) {
				return;
			}
			int id = Integer.valueOf(req.getParameter(AJAXServlet.PARAMETER_ID));
			versions(id,cols,sortedBy,dir);
		} else if (action.equals(AJAXServlet.ACTION_UPDATES)) {
			if(!checkRequired(req,action,AJAXServlet.PARAMETER_FOLDERID, AJAXServlet.PARAMETER_TIMESTAMP)) {
				return;
			}
			int folderId = Integer.valueOf(req.getParameter(AJAXServlet.PARAMETER_FOLDERID));
			long timestamp = new Long(req.getParameter(AJAXServlet.PARAMETER_TIMESTAMP));
			String delete = req.getParameter(AJAXServlet.PARAMETER_IGNORE);
			updates(folderId,cols,sortedBy,dir,timestamp,delete != null && delete.equals("deleted"));
			
		} else if (action.equals(AJAXServlet.ACTION_SEARCH)){
			JSONObject queryObject = (JSONObject) req.getBody();
			String query = queryObject.getString("pattern");
			
			int folderId = SearchEngine.NO_FOLDER;
			String folderS = req.getParameter(AJAXServlet.PARAMETER_FOLDERID);
			if(null != folderS) {
				folderId = Integer.valueOf(folderS);
			}
			
			int start = SearchEngine.NOT_SET;
			String startS = req.getParameter(AJAXServlet.PARAMETER_START);
			if(null != startS)
				start = Integer.valueOf(startS);
			
			
			int end = SearchEngine.NOT_SET;
			String endS = req.getParameter(AJAXServlet.PARAMETER_END);
			if(null != endS)
				end = Integer.valueOf(endS);
			
			if(start == SearchEngine.NOT_SET && end == SearchEngine.NOT_SET) {
				String limitS = req.getParameter(AJAXServlet.PARAMETER_LIMIT);
				if(limitS != null) {
					int limit = Integer.valueOf(limitS);
					start = 0;
					end = limit-1;
				}
			}
			
			search(query,cols,folderId,sortedBy,dir,start,end);
		}
	}
	
	// Actions

	protected void list(int[] ids, Metadata[] cols) throws JSONException, SearchIteratorException, OXException {
		InfostoreFacade infostore = getInfostore();
		TimedResult result = null;
		SearchIterator iter = null;
		try {
			
			result = infostore.getDocuments(ids,cols,sessionObj.getContext(), sessionObj.getUserObject(), sessionObj.getUserConfiguration());
			
			iter = result.results();
			
			InfostoreWriter iWriter = new InfostoreWriter(w);
			iWriter.timedResult(result.sequenceNumber());
			iWriter.writeMetadata(iter ,cols,TimeZone.getTimeZone(sessionObj.getUserObject().getTimeZone()));
			iWriter.endTimedResult();
			w.flush();
			
		
		} catch (Throwable t){
			handle(t);
			
		} finally {
			if(iter!=null)
				iter.close();
		}
	} 
	
	protected void get(int id , int version ){
		InfostoreFacade infostore = getInfostore();
		DocumentMetadata dm = null;
		try {
			
			dm = infostore.getDocumentMetadata(id,version,sessionObj.getContext(), sessionObj.getUserObject(), sessionObj.getUserConfiguration());
			if(dm==null) {
				sendErrorAsJS("Cannot find document: %s ", ((Integer)id).toString());
			}
		} catch (Throwable t){
			handle(t);
			return;
		} finally {
			
		}
				
		try {
			InfostoreWriter iWriter = new InfostoreWriter(w);
			iWriter.timedResult(dm.getSequenceNumber());
			iWriter.write(dm,TimeZone.getTimeZone(sessionObj.getUserObject().getTimeZone()));
			iWriter.endTimedResult();
		} catch (JSONException e) {
			LOG.debug("", e);
		}
	}
	
	protected void revert(int id, long ts) throws IOException {
		InfostoreFacade infostore = getInfostore();
		SearchIterator iter = null;
		long timestamp = -1;
		try {
			//SearchENgine?
			infostore.startTransaction();
			infostore.getDocumentMetadata(id,InfostoreFacade.CURRENT_VERSION,sessionObj.getContext(),sessionObj.getUserObject(),sessionObj.getUserConfiguration()).getSequenceNumber();
			TimedResult result = infostore.getVersions(id,new Metadata[]{Metadata.VERSION_LITERAL}, sessionObj.getContext(), sessionObj.getUserObject(), sessionObj.getUserConfiguration());
			if(timestamp > ts) {
				throw new OXConflictException();
			}
			iter = result.results();
			List<Integer> versions = new ArrayList<Integer>();
			while(iter.hasNext()) {
				int version =((DocumentMetadata)iter.next()).getVersion();
				if(version == 0)
					continue;
				versions.add(version);
			}
			iter.close();
			int[] versionsArray = new int[versions.size()];
			int index = 0;
			for(int version : versions) 
				versionsArray[index++] = version;
			infostore.removeVersion(id, versionsArray, sessionObj);
			timestamp = infostore.getDocumentMetadata(id,InfostoreFacade.CURRENT_VERSION,sessionObj.getContext(),sessionObj.getUserObject(),sessionObj.getUserConfiguration()).getSequenceNumber();
			infostore.commit();
			JSONObject object = new JSONObject();
			object.put("data", new JSONObject());
			object.put("timestamp", timestamp);
			w.write(object.toString());
			w.flush();
		} catch (Throwable t){
			try {
				infostore.rollback();
			} catch (TransactionException e) {
				LOG.debug("", e);
			}
			handle(t);
			return;
		} finally {
			if(iter!=null)
				try {
					iter.close();
				} catch (SearchIteratorException e) {
					LOG.debug("", e);
				}
			try {
				infostore.finish();
			} catch (TransactionException e1) {
				LOG.debug("", e1);
			}
		}
	}
	
	protected void all(int folderId, Metadata[] cols, Metadata sortedBy, int dir) throws SearchIteratorException, JSONException, OXException {
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
		InfostoreFacade infostore = getInfostore(folderId);
		TimedResult result = null;
		SearchIterator iter = null;
		try {
			
			if(sortedBy == null) {
				result = infostore.getDocuments(folderId,cols,sessionObj.getContext(), sessionObj.getUserObject(), sessionObj.getUserConfiguration());
			} else {
				result = infostore.getDocuments(folderId,cols,sortedBy,dir,sessionObj.getContext(), sessionObj.getUserObject(), sessionObj.getUserConfiguration());
			}
			
			iter = result.results();
			
			InfostoreWriter iWriter = new InfostoreWriter(w);
			iWriter.timedResult(result.sequenceNumber());
			iWriter.writeMetadata(iter,cols,TimeZone.getTimeZone(sessionObj.getUserObject().getTimeZone()));
			iWriter.endTimedResult();
		
		} catch (Throwable t){
			handle(t);
			return;
		} finally {
			if(iter!=null)
				iter.close();
		}
	}
	
	protected void versions(int id, Metadata[] cols, Metadata sortedBy, int dir) throws SearchIteratorException, JSONException, OXException {
		InfostoreFacade infostore = getInfostore();
		TimedResult result = null;
		SearchIterator iter = null;
		try {
			
			if(sortedBy == null)
				result = infostore.getVersions(id,cols,sessionObj.getContext(), sessionObj.getUserObject(), sessionObj.getUserConfiguration());
			else {
				result = infostore.getVersions(id,cols,sortedBy,dir,sessionObj.getContext(), sessionObj.getUserObject(), sessionObj.getUserConfiguration());
			}
			iter = result.results();
			iter.next(); // Skip version zero
			InfostoreWriter iWriter = new InfostoreWriter(w);
			iWriter.timedResult(result.sequenceNumber());
			iWriter.writeMetadata(iter,cols,TimeZone.getTimeZone(sessionObj.getUserObject().getTimeZone()));
			iWriter.endTimedResult();
		
		} catch (Throwable t){
			handle(t);
			return;
		} finally {
			if(iter!=null)
				iter.close();
		}
	}
	
	protected void updates(int folderId, Metadata[] cols, Metadata sortedBy, int dir, long timestamp, boolean ignoreDelete) throws SearchIteratorException, JSONException, OXException, IOException {
		InfostoreFacade infostore = getInfostore(folderId);
		Delta delta = null;
		
		
		SearchIterator iter = null;
		SearchIterator iter2 = null;
		
		try {
			
			
			if(sortedBy == null)
				delta = infostore.getDelta(folderId,timestamp,cols,ignoreDelete,sessionObj.getContext(), sessionObj.getUserObject(), sessionObj.getUserConfiguration());
			else {
				delta = infostore.getDelta(folderId,timestamp,cols,sortedBy,dir,ignoreDelete,sessionObj.getContext(), sessionObj.getUserObject(), sessionObj.getUserConfiguration());
			}
			
			iter = delta.results();
			iter2 = delta.getDeleted();
			
			InfostoreWriter iWriter = new InfostoreWriter(w);
			iWriter.timedResult(delta.sequenceNumber());
			iWriter.writeDelta(iter, iter2, cols,ignoreDelete,TimeZone.getTimeZone(sessionObj.getUserObject().getTimeZone()));
			iWriter.endTimedResult();
			w.flush();
		
		} catch (Throwable t){
			handle(t);
			return;
		} finally {
			if(iter!=null)
				iter.close();
			if(iter2!=null)
				iter2.close();
		}
	}
	
	protected void delete(int[] ids, long timestamp) throws IOException {
		InfostoreFacade infostore = getInfostore();
		SearchEngine searchEngine = getSearchEngine();
		
		int[] notDeleted = new int[0];
		if(ids.length!=0) {
			try {
				
				infostore.startTransaction();
				searchEngine.startTransaction();
				
				notDeleted = infostore.removeDocument(ids,timestamp, sessionObj);
				
				Set<Integer> notDeletedSet = new HashSet<Integer>();
				for(int nd : notDeleted) { notDeletedSet.add(nd); }
				
				for(int id : ids) {
					if(!notDeletedSet.contains(id)){
						searchEngine.unIndex0r(id,sessionObj.getContext(), sessionObj.getUserObject(), sessionObj.getUserConfiguration());
					}
				}
				
				infostore.commit();
				searchEngine.commit();
				
			} catch (Throwable t){
				LOG.debug("",t);
				try {
					infostore.rollback();
					searchEngine.rollback();
					return;
				} catch (TransactionException e) {
					LOG.debug("", e);
				}
				handle(t);
				return;
			} finally {
				try {
					infostore.finish();
					searchEngine.finish();
				} catch (TransactionException e) {
					LOG.debug("", e);
				}
				
			}
		}
		
		StringBuilder b = new StringBuilder();
		b.append("[");
		for (int i = 0; i < notDeleted.length; i++) {
			int nd = notDeleted[i];
			b.append(nd);
			if(i != notDeleted.length-1)
				b.append(",");
		}
		b.append("]");
		
		w.write(b.toString());
	}
	
	protected void detach(int objectId, int[] ids, long timestamp) throws IOException {
		InfostoreFacade infostore = getInfostore();
		SearchEngine searchEngine = getSearchEngine();
		
		int[] notDetached = new int[0];
		if(ids.length!=0) {
			try {
				
				infostore.startTransaction();
				searchEngine.startTransaction();
				
				notDetached = infostore.removeVersion(objectId,ids,sessionObj);
				
				searchEngine.index(infostore.getDocumentMetadata(objectId, InfostoreFacade.CURRENT_VERSION,sessionObj.getContext(), sessionObj.getUserObject(), sessionObj.getUserConfiguration()), sessionObj.getContext(), sessionObj.getUserObject(), sessionObj.getUserConfiguration());
				
				infostore.commit();
				searchEngine.commit();
			} catch (Throwable t){
				LOG.debug(t);
				try {
					infostore.rollback();
					searchEngine.rollback();
				} catch (TransactionException e) {
					LOG.debug("",e);
				}
				handle(t);
				return;
			} finally {
				try {
					infostore.finish();
					searchEngine.finish();
				} catch (TransactionException e) {
					LOG.debug("", e);
				}
				
			}
		}
		
		StringBuilder b = new StringBuilder();
		b.append("[");
		for (int i = 0; i < notDetached.length; i++) {
			int nd = notDetached[i];
			b.append(nd);
			if(i != notDetached.length-1)
				b.append(",");
		}
		b.append("]");
		
		w.write(b.toString());
	}
	
	protected void newDocument(DocumentMetadata newDocument) throws JSONException, IOException {
		InfostoreFacade infostore = getInfostore(newDocument.getFolderId());
		SearchEngine searchEngine = getSearchEngine();
		try {
			
			infostore.startTransaction();
			searchEngine.startTransaction();
			
			infostore.saveDocumentMetadata(newDocument,System.currentTimeMillis(),sessionObj);
			infostore.commit();
			//System.out.println("DONE SAVING: "+System.currentTimeMillis());
			searchEngine.index(newDocument,sessionObj.getContext(), sessionObj.getUserObject(), sessionObj.getUserConfiguration());
			searchEngine.commit();
		} catch (Throwable t){
			LOG.debug(t);
			try {
				infostore.rollback();
				searchEngine.rollback();
				
			} catch (TransactionException e) {
				LOG.debug("", e);
			}
			handle(t);
			return;
		} finally {
			try {
				infostore.finish();
				searchEngine.finish();
			} catch (TransactionException e) {
				LOG.debug("",e);
			}	
		}
		JSONObject obj = new JSONObject();
		obj.put("data", newDocument.getId());
		w.write(obj.toString());
	}
	
	protected void saveAs(DocumentMetadata newDocument, Metadata[] fields, int folderId, int attachedId, int moduleId, int attachment) throws JSONException, IOException {
		Set<Metadata> alreadySet = new HashSet<Metadata>(Arrays.asList(fields));
		if(!alreadySet.contains(Metadata.FOLDER_ID_LITERAL))
			try {
				missingParameter("folder_id in object",AJAXServlet.ACTION_SAVE_AS);
			} catch (IOException e1) {
				LOG.debug("", e1);
			}
		
		AttachmentBase attachmentBase = Attachment.ATTACHMENT_BASE;
		InfostoreFacade infostore = getInfostore(newDocument.getFolderId());
		SearchEngine searchEngine = getSearchEngine();
		InputStream in = null;
		try {
			attachmentBase.startTransaction();
			infostore.startTransaction();
			searchEngine.startTransaction();
			
			AttachmentMetadata att = attachmentBase.getAttachment(folderId,attachedId,moduleId,attachment,sessionObj.getContext(),sessionObj.getUserObject(), sessionObj.getUserConfiguration());
			com.openexchange.groupware.attach.util.GetSwitch get = new com.openexchange.groupware.attach.util.GetSwitch(att);
			SetSwitch set = new SetSwitch(newDocument);
			
			for(Metadata attachmentCompatible : Metadata.VALUES) {
				if(alreadySet.contains(attachmentCompatible))
					continue;
				AttachmentField attField = Metadata.getAttachmentField(attachmentCompatible);
				if(null == attField)
					continue;
				Object value = attField.doSwitch(get);
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
		} catch (Throwable t){
			LOG.debug(t);
			try {
				infostore.rollback();
				searchEngine.rollback();
				attachmentBase.rollback();
			} catch (TransactionException e) {
				LOG.debug("", e);
			}
			handle(t);
			return;
		} finally {
			try {
				infostore.finish();
				searchEngine.finish();
				attachmentBase.finish();
			} catch (TransactionException e) {
				LOG.debug("", e);
			}
			if(in != null)
				try {
					in.close();
				} catch (IOException e) {
					LOG.debug("", e);
				}
		}
		
		JSONObject obj = new JSONObject();
		obj.put("data", newDocument.getId());
		w.write(obj.toString());
	}
	
	protected void update(int id, DocumentMetadata updated, long timestamp, Metadata[] presentFields) throws IOException {
		InfostoreFacade infostore = getInfostore(updated.getFolderId());
		SearchEngine searchEngine = getSearchEngine();
		
		try {
			
			infostore.startTransaction();
			searchEngine.startTransaction();
			
			boolean version = false;
			for(Metadata m : presentFields) {
				if(m.equals(Metadata.VERSION_LITERAL)){
					version = true;
					break;
				}
			}
			if(!version){
				updated.setVersion(InfostoreFacade.CURRENT_VERSION);
			}
			
			infostore.saveDocumentMetadata(updated,timestamp,presentFields,sessionObj);
			
			updated = infostore.getDocumentMetadata(id,InfostoreFacade.CURRENT_VERSION,sessionObj.getContext(), sessionObj.getUserObject(), sessionObj.getUserConfiguration());
	
			searchEngine.index(updated,sessionObj.getContext(), sessionObj.getUserObject(), sessionObj.getUserConfiguration());
			
			
			infostore.commit();
			searchEngine.commit();
		} catch (Throwable t){
			LOG.debug(t);
			try {
				infostore.rollback();
				searchEngine.rollback();
			} catch (TransactionException e) {
				LOG.debug("", e);
			}
			handle(t);
			return;
		} finally {
			try {
				infostore.finish();
				searchEngine.finish();
			} catch (TransactionException e) {
				LOG.debug("", e);
			}
			
		}
				
		w.write("{}");
	}
	
	protected void copy(int id, DocumentMetadata updated, long timestamp, Metadata[] presentFields) throws IOException {

		InfostoreFacade infostore = getInfostore(updated.getFolderId());
		SearchEngine searchEngine = getSearchEngine();
		DocumentMetadata metadata = null;
		
		try {
			
			infostore.startTransaction();
			searchEngine.startTransaction();
			
			
			metadata = new DocumentMetadataImpl(infostore.getDocumentMetadata(id,InfostoreFacade.CURRENT_VERSION,sessionObj.getContext(), sessionObj.getUserObject(), sessionObj.getUserConfiguration()));
			
			
			SetSwitch set = new SetSwitch(metadata);
			GetSwitch get = new GetSwitch(updated);
			for(Metadata field : presentFields) {
				Object value = field.doSwitch(get);
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
		} catch (Throwable t){
			LOG.debug(t);
			try {
				infostore.rollback();
				searchEngine.rollback();
			} catch (TransactionException e) {
				LOG.debug("", e);
			}
			handle(t);
			return;
		} finally {
			try {
				infostore.finish();
				searchEngine.finish();
			} catch (TransactionException e) {
				LOG.debug("", e);
			}
			
		}
				
		w.write("{ \"data\" :"+metadata.getId()+"}");
		
	}

	protected void lock(int id, long diff) {
		InfostoreFacade infostore = getInfostore();
		
		try {
			infostore.startTransaction();
			
			infostore.lock(id,diff,sessionObj);
			
			infostore.commit();
			
			w.write("{}");
			w.flush();
			
		} catch (Throwable t) {
			try {
				infostore.rollback();
			} catch (TransactionException e) {
				LOG.debug("", e);
			}
			handle(t);
		} finally {
			try {
				infostore.finish();
			} catch (TransactionException e) {
				LOG.debug("", e);
			}
		}
	}
	
	protected void unlock(int id) {
		InfostoreFacade infostore = getInfostore();
		
		try {
			infostore.startTransaction();
			
			DocumentMetadata m = new DocumentMetadataImpl();
			
			infostore.unlock(id, sessionObj);
			
			infostore.commit();
			
			w.write("{}");
			w.flush();
			
		} catch (Throwable t) {
			try {
				infostore.rollback();
			} catch (TransactionException e) {
				LOG.debug("", e);
			}
			handle(t);
		} finally {
			try {
				infostore.finish();
			} catch (TransactionException e) {
				LOG.debug("", e);
			}
		}
		
		
	}
	
	protected void search(String query, Metadata[] cols, int folderId, Metadata sortedBy, int dir, int start, int end) {
		SearchEngine searchEngine = getSearchEngine();
		
		try {
			searchEngine.startTransaction();
			
			SearchIterator results = searchEngine.search(query,cols,folderId,sortedBy,dir,start,end, sessionObj.getContext(), sessionObj.getUserObject(), sessionObj.getUserConfiguration());
			
			InfostoreWriter iWriter = new InfostoreWriter(w);
			iWriter.timedResult(System.currentTimeMillis());
			iWriter.writeMetadata(results,cols,TimeZone.getTimeZone(sessionObj.getUserObject().getTimeZone()));
			iWriter.endTimedResult();
			w.flush();
			
			searchEngine.commit();
		} catch (Throwable t) {
			try {
				searchEngine.rollback();
			} catch (TransactionException x) {
				LOG.debug("", x);
			}
			handle(t);
		} finally {
			try {
				searchEngine.finish();
			} catch (TransactionException x){
				LOG.debug("", x);
			}
		}
	}
	
	
	protected InfostoreFacade getInfostore(){
		return Infostore.FACADE;
	}
	
	protected InfostoreFacade getInfostore(long folderId){
		if(folderId == FolderObject.VIRTUAL_LIST_INFOSTORE_FOLDER_ID)
			return Infostore.VIRTUAL_FACADE;
		return Infostore.FACADE;
	}
	
	protected SearchEngine getSearchEngine(){
		return Infostore.SEARCH_ENGINE;
	}
	
}
