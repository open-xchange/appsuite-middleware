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

package com.openexchange.ajax;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.parser.InfostoreParser;
import com.openexchange.ajax.parser.InfostoreParser.UnknownMetadataException;
import com.openexchange.ajax.request.InfostoreRequest;
import com.openexchange.ajax.request.ServletRequestAdapter;
import com.openexchange.api.OXPermissionException;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.UserConfiguration;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.infostore.DocumentMetadata;
import com.openexchange.groupware.infostore.InfostoreFacade;
import com.openexchange.groupware.infostore.SearchEngine;
import com.openexchange.groupware.infostore.database.impl.DocumentMetadataImpl;
import com.openexchange.groupware.infostore.database.impl.GetSwitch;
import com.openexchange.groupware.infostore.database.impl.SetSwitch;
import com.openexchange.groupware.infostore.facade.impl.InfostoreFacadeImpl;
import com.openexchange.groupware.infostore.facade.impl.VirtualFolderInfostoreFacade;
import com.openexchange.groupware.infostore.search.impl.SearchEngineImpl;
import com.openexchange.groupware.infostore.utils.InfostoreConfigUtils;
import com.openexchange.groupware.infostore.utils.Metadata;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.tx.DBPoolProvider;
import com.openexchange.groupware.tx.TransactionException;
import com.openexchange.groupware.upload.UploadEvent;
import com.openexchange.groupware.upload.UploadException;
import com.openexchange.groupware.upload.UploadFile;
import com.openexchange.groupware.upload.UploadException.UploadCode;
import com.openexchange.json.OXJSONWriter;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.encoding.Helper;
import com.openexchange.tools.exceptions.LoggingLogic;
import com.openexchange.tools.servlet.UploadServletException;
import com.openexchange.tools.servlet.http.Tools;

public class Infostore extends PermissionServlet {
	
	private static final String STR_JSON = "json";

	private static final String STR_ERROR = "error";

	private static final String STR_ACTION = "action";

	private static final String MIME_TEXT_HTML = "text/html";

	private static final long serialVersionUID = 2674990072903834660L;

	private static  final InfostoreParser PARSER = new InfostoreParser();

	public static final InfostoreFacade VIRTUAL_FACADE = new VirtualFolderInfostoreFacade();
	
	public static  final InfostoreFacade FACADE = new InfostoreFacadeImpl(new DBPoolProvider());
	static{
		FACADE.setTransactional(true);
	}
	
	public static  final SearchEngine SEARCH_ENGINE = new SearchEngineImpl(new DBPoolProvider());
	static {
		SEARCH_ENGINE.setTransactional(true);
	}
	
	
//	public static final Exception2Message OXEXCEPTION_HANDLER = new InfostoreException2Message();
	
	private static final Log LOG = LogFactory.getLog(Infostore.class);
	private static final LoggingLogic LL = LoggingLogic.getLoggingLogic(Infostore.class, LOG);

	private long maxUploadSize = -1;
	
	//TODO: Better error handling
	
	
	@Override
	protected boolean hasModulePermission(final SessionObject sessionObj) {
		return InfostoreRequest.hasPermission(sessionObj.getUserConfiguration());
	}

	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse res)
		throws ServletException, IOException {
		
		final SessionObject sessionObj = getSessionObject(req);
		
		final Context ctx = sessionObj.getContext();
		final User user = sessionObj.getUserObject();
		final UserConfiguration userConfig = sessionObj.getUserConfiguration();
		
		final String action = req.getParameter(PARAMETER_ACTION);
		if (action == null) {
			missingParameter(PARAMETER_ACTION,res, false, null);
			return;
		}

		if(action.equals(ACTION_DOCUMENT)){
			if(req.getParameter(PARAMETER_ID) == null) {
				final Response resp = new Response();
				resp.setException(new AbstractOXException("You must provide a value for "+PARAMETER_ID));
				final StringWriter w = new StringWriter();
				try {
					Response.write(resp, w);
				} catch (JSONException e) {
					//shouldn't happen
					throw new ServletException(e);
				}
				res.setContentType(MIME_TEXT_HTML);
				res.getWriter().write(substitute(JS_FRAGMENT, STR_ACTION, STR_ERROR, STR_JSON, w.toString()));
			}
			int id;
			try {
				id = Integer.valueOf(req.getParameter(PARAMETER_ID));
			} catch (NumberFormatException x) {
				handleOXException(res, new AbstractOXException("Invalid number"), STR_ERROR, true, JS_FRAGMENT);
				return;
			}
			
			final String versionS = req.getParameter(PARAMETER_VERSION);
			final int version = (versionS == null) ? InfostoreFacade.CURRENT_VERSION : Integer.valueOf(versionS);
			
			final String contentType = req.getParameter(PARAMETER_CONTENT_TYPE);
			
			document(res, isIE(req),isIE7(req), id, version, contentType, ctx, user, userConfig);
			
			return;
		}
		final OXJSONWriter writer = new OXJSONWriter();
		final InfostoreRequest request = new InfostoreRequest(sessionObj,writer);
		try {
			if(!request.action(action,new ServletRequestAdapter(req,res))) {
				unknownAction("GET", action, res, false);
				return;
			}
			Response.write(new Response((JSONObject) writer.getObject()), res.getWriter());
		} catch (final JSONException e) {
			LOG.error(e.getLocalizedMessage(), e);
		} catch (OXPermissionException e) {
			LOG.error("Not possible, obviously: "+e.getLocalizedMessage(), e);
		}
	}

	

	@Override
	protected void doPut(final HttpServletRequest req, final HttpServletResponse res)
			throws ServletException, IOException {
		final SessionObject sessionObj = getSessionObject(req);

		final String action = req.getParameter(PARAMETER_ACTION);
		if (action == null) {
			missingParameter(PARAMETER_ACTION,res, false, null);
			return;
		}
		final OXJSONWriter writer = new OXJSONWriter();
		final InfostoreRequest request = new InfostoreRequest(sessionObj,writer);
		try {
			if(!request.action(action,new ServletRequestAdapter(req,res))) {
				unknownAction("PUT", action, res, false);
				return;
			}
			if(writer.isJSONObject()) {
				Response.write(new Response((JSONObject) writer.getObject()), res.getWriter());
			} else if(writer.isJSONArray()) {
				res.getWriter().print(writer.getObject().toString());
			}
		} catch (final JSONException e) {
			LOG.error(e.getLocalizedMessage(), e);
		} catch (OXPermissionException e) {
			LOG.error("Not possible, obviously: "+e.getLocalizedMessage(), e);
		}
	}
	
	@Override
	protected void doPost(final HttpServletRequest req, final HttpServletResponse res)
	throws ServletException, IOException {
		
		final SessionObject sessionObj = getSessionObject(req);

		final Context ctx = sessionObj.getContext();
		final User user = sessionObj.getUserObject();
		final UserConfiguration userConfig = sessionObj.getUserConfiguration();

		final String action = req.getParameter(PARAMETER_ACTION);
		if (action == null) {
			missingParameter(PARAMETER_ACTION,res, true, "new");
			return;
		}
		
		try {
			checkSize(req.getContentLength(), sessionObj.getUserConfiguration());
			if(action.equals(ACTION_NEW) || action.equals(ACTION_UPDATE) || action.equals(ACTION_COPY)) {
				UploadEvent upload = null;
				try {
					upload = processUpload(req);
					final UploadFile uploadFile = upload.getUploadFileByFieldName("file");
					if(null != uploadFile) {
						checkSize(uploadFile.getSize(), sessionObj.getUserConfiguration());
					}
					final String obj = upload.getFormField(STR_JSON);
					if(obj == null) {
						missingParameter(STR_JSON,res, true, action);
						return;
					}
					
					
					final DocumentMetadata metadata = PARSER.getDocumentMetadata(obj);
					if(action.equals(ACTION_NEW)){
						newDocument(metadata,res, uploadFile,ctx, user, userConfig, sessionObj);
					} else {
						if(!checkRequired(req,res,true, action, PARAMETER_ID, PARAMETER_TIMESTAMP)){
							return;
						}
						final int id = Integer.parseInt(req.getParameter(PARAMETER_ID));
						final long timestamp = Long.parseLong(req.getParameter(PARAMETER_TIMESTAMP));
						
						metadata.setId(id);
						Metadata[] presentFields = null;
						
						try {
							presentFields = PARSER.findPresentFields(obj);
						} catch (UnknownMetadataException x) {
							unknownColumn(res,"BODY",x.getColumnId(), true, action);
							return;
						}
						
						if(action.equals(ACTION_UPDATE)) {
							update(res, id,metadata,timestamp,presentFields,uploadFile,ctx, user, userConfig, sessionObj);
						} else {
							copy(res,id,metadata,timestamp,presentFields,uploadFile,ctx, user, userConfig, sessionObj);
						}
					}
				} finally {
					if (upload != null) {
						upload.cleanUp();
					}
				}
			} 
		} catch (OXException x) {
			handleOXException(res, x, action, true, null);
		} catch (UploadException x) {
			final Response resp = new Response();
			resp.setException(new AbstractOXException(x.getMessage())); // FIXME
			try {
				res.setContentType("text/html; charset=UTF-8");
				
				throw new UploadServletException(res, substitute(JS_FRAGMENT, STR_JSON, resp.getJSON().toString(), STR_ACTION,action),x.getMessage(),x);
			} catch (JSONException e) {
				LOG.error("Giving up",e);
			}
		} catch (JSONException e) {
			handleOXException(res,e,action, true, null);
		}
	}

	private void checkSize(final long size, final UserConfiguration userConfig) throws UploadException {
		long maxSize = InfostoreConfigUtils.determineRelevantUploadSize(userConfig);
		if(maxSize == 0) {
			return;
		}
		
		if(size > maxSize) {
			throw new UploadException(UploadCode.MAX_UPLOAD_SIZE_EXCEEDED, null, size, maxSize);
		}
	}

	// Response Methods

	/*private void notImplemented(final String action, final HttpServletResponse res) throws IOException, ServletException {
		sendErrorAsJS(res,"The action "+action+" isn't implemented yet");
	}*/

	// Handlers

	protected void newDocument(final DocumentMetadata newDocument, final HttpServletResponse res, final UploadFile upload, final Context ctx, final User user, final UserConfiguration userConfig, final SessionObject sessionObj) {
		//System.out.println("------> "+newDocument.getFolderId());
		res.setContentType(MIME_TEXT_HTML);
		
		final InfostoreFacade infostore = getInfostore(newDocument.getFolderId());
		final SearchEngine searchEngine = getSearchEngine();
		FileInputStream in = null;
		try {
			
			infostore.startTransaction();
			searchEngine.startTransaction();
			
			if(upload==null || upload.getTmpFile().length() == 0) {
				infostore.saveDocumentMetadata(newDocument,System.currentTimeMillis(),sessionObj);
			} else {
				initMetadata(newDocument,upload);
				infostore.saveDocument(newDocument,in = new FileInputStream(upload.getTmpFile()),System.currentTimeMillis(),sessionObj);
			}
			//System.out.println("DONE SAVING: "+System.currentTimeMillis());
			searchEngine.index(newDocument,ctx, user, userConfig);
			
			infostore.commit();
			searchEngine.commit();
	
		} catch (OXException t){
			rollback(infostore, searchEngine, res, t, ACTION_NEW, true);
			return;
		} catch (FileNotFoundException e) {
			rollback(infostore, searchEngine, res, e, ACTION_NEW, true);
			return;
		} finally {
			try {
				infostore.finish();
				searchEngine.finish();
			} catch (TransactionException e) {
				LOG.debug("",e);
			}
			if(in != null) {
				try {
					in.close();
				} catch (IOException e) {
					LOG.debug("",e);
				}
			}
			
		}
		PrintWriter w = null;
		try {
			w = res.getWriter();
			final JSONObject obj = new JSONObject();
			obj.put("data",newDocument.getId());
			w.print(substitute(JS_FRAGMENT, STR_JSON, obj.toString(),STR_ACTION,ACTION_NEW));
			
			w.flush();
		} catch (IOException e) {
			LOG.debug("", e);
		} catch (JSONException e) {
			LOG.debug("", e);
		} finally {
			close(w);
		}
	}
	
	protected void update(final HttpServletResponse res, final int id, DocumentMetadata updated, final long timestamp, final Metadata[] presentFields, final UploadFile upload, final Context ctx, final User user, final UserConfiguration userConfig, final SessionObject sessionObj) {
		
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
		
		res.setContentType(MIME_TEXT_HTML);
		
		final InfostoreFacade infostore = getInfostore(updated.getFolderId());
		final SearchEngine searchEngine = getSearchEngine();
		
		try {
			
			infostore.startTransaction();
			searchEngine.startTransaction();
			
			if(upload == null || upload.getTmpFile().length() == 0){
				infostore.saveDocumentMetadata(updated,timestamp,presentFields,sessionObj);
			} else {
				initMetadata(updated,upload);
				infostore.saveDocument(updated,new FileInputStream(upload.getTmpFile()),timestamp,presentFields,sessionObj);
			}
			infostore.commit();
			searchEngine.commit();
			
		} catch (OXException t){
			rollback(infostore, null, res, t, ACTION_UPDATE, true);
			return;
		} catch (FileNotFoundException e) {
			rollback(infostore, null, res, e, ACTION_UPDATE,true);
			return;
		} finally {
			try {
				infostore.finish();
				searchEngine.finish();
			} catch (TransactionException e) {
				LOG.debug("", e);
			}
			
		}
				
		PrintWriter w = null;
		try {
			w = res.getWriter();
			w.write(substitute(JS_FRAGMENT,STR_JSON,"{}",STR_ACTION,ACTION_UPDATE));
			close(w);
		} catch (IOException e) {
			LOG.warn(e);
		}
	}
	
	protected void copy(final HttpServletResponse res, final int id, final DocumentMetadata updated, final long timestamp, final Metadata[] presentFields, final UploadFile upload, final Context ctx, final User user, final UserConfiguration userConfig, final SessionObject sessionObj) {

		res.setContentType(MIME_TEXT_HTML);
		
		
		final InfostoreFacade infostore = getInfostore();
		final SearchEngine searchEngine = getSearchEngine();
		DocumentMetadata metadata = null;
		
		try {
			
			infostore.startTransaction();
			searchEngine.startTransaction();
			
			
			metadata = new DocumentMetadataImpl(infostore.getDocumentMetadata(id,InfostoreFacade.CURRENT_VERSION,ctx, user, userConfig));
			
			
			final SetSwitch set = new SetSwitch(metadata);
			final GetSwitch get = new GetSwitch(updated);
			for(Metadata field : presentFields) {
				final Object value = field.doSwitch(get);
				set.setValue(value);
				field.doSwitch(set);
			}
			metadata.setVersion(0);
			metadata.setId(InfostoreFacade.NEW);
			
			if(upload == null){
				if(metadata.getFileName() != null && !"".equals(metadata.getFileName())) {
					infostore.saveDocument(metadata,infostore.getDocument(id,InfostoreFacade.CURRENT_VERSION,ctx, user, userConfig),metadata.getSequenceNumber(), sessionObj);
				} else {
					infostore.saveDocumentMetadata(metadata,timestamp,sessionObj);
				}
			} else {
				initMetadata(metadata,upload);
				infostore.saveDocument(metadata,new FileInputStream(upload.getTmpFile()),timestamp, sessionObj);
			}
			searchEngine.index(metadata,ctx, user, userConfig);
			
			infostore.commit();
			searchEngine.commit();
		} catch (OXException t){
			rollback(infostore, searchEngine, res, t, ACTION_COPY, true);
			return;
		} catch (FileNotFoundException e) {
			rollback(infostore, searchEngine, res, e, ACTION_COPY, true);
			return;
		} finally {
			try {
				infostore.finish();
				searchEngine.finish();
			} catch (TransactionException e) {
				LOG.debug("", e);
			}
			
		}
				
		PrintWriter w = null;
		try {
			w = res.getWriter();
			final JSONObject obj = new JSONObject();
			obj.put("data",metadata.getId());
			w.print(substitute(JS_FRAGMENT, STR_JSON, obj.toString(),STR_ACTION,ACTION_NEW));
			w.flush();
		} catch (IOException e) {
			LOG.debug("", e);
		} catch (JSONException e) {
			LOG.debug("", e);
		} finally {
			close(w);
		}
	}
	
	protected void document(final HttpServletResponse res, final boolean ie,final boolean ie7, final int id, final int version, String contentType, final Context ctx, final User user, final UserConfiguration userConfig) throws IOException {
		final InfostoreFacade infostore = getInfostore();
		OutputStream os = null;
		InputStream documentData = null;
		try {
			final DocumentMetadata metadata = infostore.getDocumentMetadata(id,version,ctx, user, userConfig);
			
			documentData = infostore.getDocument(id,version,ctx, user, userConfig);
			os = res.getOutputStream();
			
			res.setContentLength((int) metadata.getFileSize());
			res.setContentType(contentType == null ? metadata.getFileMIMEType() : contentType);
			if(contentType != null && contentType.equals(SAVE_AS_TYPE)) {
				res.setHeader("Content-Disposition", "attachment; filename=\""+Helper.encodeFilename(metadata.getFileName(), "UTF-8" , ie)+"\"");
			} else {
				res.setHeader("Content-Disposition", "filename=\""+Helper.encodeFilename(metadata.getFileName(), "UTF-8" , ie)+"\"");	
			}
			
			// Browsers doesn't like the Pragma header the way we usually set
            // this. Especially if files are sent to the browser. So removing
            // pragma header
			Tools.removeCachingHeader(res);
			
			final byte[] buffer = new byte[200];
			int bytesRead = 0;
			
			while((bytesRead = documentData.read(buffer))!=-1){
				os.write(buffer,0,bytesRead);
				os.flush();
			}
			os=null;
			
		} catch (OXException x) {
			LOG.debug(x.getMessage(),x);
			handleOXException(res, x, STR_ERROR, true, JS_FRAGMENT);
			return;
		} finally {
			
			if(os!=null){
				try {
					os.flush();
				} catch (IOException e) {
					LOG.debug("",e);
				}
				try {
					os.close();
				} catch (IOException e) {
					LOG.debug("",e);
				}
			}
			if(documentData!=null){
				try {
					documentData.close();
				} catch (IOException e) {
					LOG.debug("",e);
				}
			}
		}
	}
	
	private final boolean handleOXException(final HttpServletResponse res, final Throwable t, final String action, final boolean post, final String fragmentOverride) {
		res.setContentType("text/html; charset=UTF-8");
		if(t instanceof AbstractOXException ) {
			final Response resp = new Response();
			resp.setException((AbstractOXException) t);
			Writer writer = null;
			
			try {
				if(post) {
					writer = new StringWriter();
				} else {
					writer = res.getWriter();
				}
				Response.write(resp, writer);
				if(post) {
					res.getWriter().write(substitute(fragmentOverride != null ? fragmentOverride : JS_FRAGMENT,STR_JSON,writer.toString(),STR_ACTION,action));
				}
			} catch (JSONException e) {
				LOG.error("",t);
			} catch (IOException e) {
				LOG.error("",e);
			}
			
			LL.log((AbstractOXException) t);
			
			return true;
		}
		return false;
	}

	
	protected void sendErrorAsJS(final PrintWriter w, final String error, final String...errorParams) {
		final StringBuilder commaSeperatedErrorParams = new StringBuilder();
		for(String param : errorParams) {
			commaSeperatedErrorParams.append('"');
			commaSeperatedErrorParams.append(param);
			commaSeperatedErrorParams.append('"');
			commaSeperatedErrorParams.append(',');
		}
		commaSeperatedErrorParams.setLength(commaSeperatedErrorParams.length()-1);
		w.print("{ \"error\" : \"");
		w.print(error);
		w.print("\", \"error_params\" : [");
		w.print(commaSeperatedErrorParams.toString());
		w.print("}");
		w.flush();
	}
	
	// Helpers
	
	protected int[] parseIDList(final JSONArray array) throws JSONException {
		int[] ids = new int[array.length()];
		
		for(int i = 0; i < array.length(); i++) {
			final JSONObject tuple = array.getJSONObject(i);
			try {
				ids[i] = tuple.getInt(PARAMETER_ID);
			} catch (JSONException x) {
				ids[i] = Integer.parseInt(tuple.getString(PARAMETER_ID));
			}
		}
		return ids;
	} 

	
	protected void initMetadata(final DocumentMetadata metadata, final UploadFile upload) {
		if(metadata.getFileName() == null || "".equals(metadata.getFileName())){
			String s = upload.getFileName();
			// Try guessing the filename separator
			if(s.contains("\\")){
				s = s.substring(s.lastIndexOf('\\')+1);
			} else if (s.contains("/")){
				s = s.substring(s.lastIndexOf('/')+1);
			}
			metadata.setFileName(s);
		}
		if(metadata.getFileSize() <= 0){
			metadata.setFileSize(upload.getSize());
		}
		if(metadata.getFileMIMEType() == null || "".equals(metadata.getFileMIMEType())){
			metadata.setFileMIMEType(upload.getContentType());
		}
	}
	
	protected void rollback(final InfostoreFacade infostore, final SearchEngine searchEngine, final HttpServletResponse res, final Throwable t, final String action, final boolean post){
		if(infostore != null) {
			try {
				infostore.rollback();
			} catch (TransactionException e) {
				LOG.error("", e);
			}
		}
		if(searchEngine != null) {
			try {
				searchEngine.rollback();
			} catch (TransactionException e) {
				LOG.error("", e);
			}
		}
		if(!handleOXException(res, t, action, post, null)){
			try {
				sendErrorAsJSHTML(res,t.toString(),action);
				LOG.error("Got non OXException", t);
			} catch (IOException e) {
				LOG.error(e);
			}
		}
	}
	
	// Errors
	
	
	protected InfostoreFacade getInfostore(){
		return FACADE;
	}
	
	public static InfostoreFacade getInfostore(final long folderId){
		if(folderId == FolderObject.VIRTUAL_LIST_INFOSTORE_FOLDER_ID 
				|| folderId == FolderObject.VIRTUAL_USER_INFOSTORE_FOLDER_ID
				|| folderId == FolderObject.SYSTEM_INFOSTORE_FOLDER_ID) {
			return VIRTUAL_FACADE;
		}
		return FACADE;
	}
	
	
	protected SearchEngine getSearchEngine(){
		return SEARCH_ENGINE;
	}
	
	
}