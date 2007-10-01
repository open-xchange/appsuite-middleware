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

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.exceptions.Exception2Message;
import com.openexchange.ajax.exceptions.InfostoreException2Message;
import com.openexchange.ajax.parser.AttachmentParser;
import com.openexchange.ajax.request.AttachmentRequest;
import com.openexchange.ajax.request.ServletRequestAdapter;
import com.openexchange.api2.OXException;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.UserConfiguration;
import com.openexchange.groupware.attach.AttachmentBase;
import com.openexchange.groupware.attach.AttachmentConfig;
import com.openexchange.groupware.attach.AttachmentField;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.groupware.attach.Attachments;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.ldap.User;
import com.openexchange.groupware.tx.TransactionException;
import com.openexchange.groupware.upload.UploadEvent;
import com.openexchange.groupware.upload.UploadException;
import com.openexchange.groupware.upload.UploadFile;
import com.openexchange.groupware.upload.UploadException.UploadCode;
import com.openexchange.json.OXJSONWriter;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.sessiond.SessionObject;
import com.openexchange.tools.encoding.Helper;
import com.openexchange.tools.servlet.UploadServletException;
import com.openexchange.tools.servlet.http.Tools;

/**
 * Attachment
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class Attachment extends PermissionServlet {
	
	private static final String MIME_TEXT_HTML_CHARSET_UTF8 = "text/html; charset=UTF-8";

	private static final String MIME_TEXT_HTML = "text/html";

	private static final String PREFIX_JSON = "json_";

	private static final long serialVersionUID = -5819944675070929520L;

	private static transient final AttachmentParser PARSER = new AttachmentParser();
	
	private static transient final AttachmentField[] REQUIRED = new AttachmentField[]{
		AttachmentField.FOLDER_ID_LITERAL,
		AttachmentField.ATTACHED_ID_LITERAL,
		AttachmentField.MODULE_ID_LITERAL
	};

	public static transient final AttachmentBase ATTACHMENT_BASE = Attachments.getInstance();
	static {
		ATTACHMENT_BASE.setTransactional(true);
	}
	
	public static transient final Exception2Message OXEXCEPTION_HANDLER = new InfostoreException2Message();
	
	private static transient final Log LOG = LogFactory.getLog(Attachment.class);

	private long maxUploadSize = -2;
	
	@Override
	protected boolean hasModulePermission(final SessionObject sessionObj) {
		return AttachmentRequest.hasPermission(sessionObj.getUserConfiguration());
	}
	
	
	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse res)
	throws ServletException, IOException {
		final SessionObject session = getSessionObject(req);
		
		final String action = req.getParameter(PARAMETER_ACTION);
		if(action == null) {
			missingParameter(PARAMETER_ACTION,res, false, null);
			return ;
		}
		
		final User user = session.getUserObject();
		final Context ctx = session.getContext();
		final UserConfiguration userConfig = session.getUserConfiguration();
		
		if (ACTION_DOCUMENT.equals(action)) {
			try {
				require(req, res,
						PARAMETER_FOLDERID, PARAMETER_ATTACHEDID, PARAMETER_MODULE, PARAMETER_ID);
			} catch (OXException e) {
				handle(res, e, action, JS_FRAGMENT_POPUP);
				return;
			}
			int folderId, attachedId, moduleId, id;
			final String contentType = req.getParameter(PARAMETER_CONTENT_TYPE);
			try {
				folderId = Integer.parseInt(req.getParameter(PARAMETER_FOLDERID));
				attachedId = Integer.parseInt(req.getParameter(PARAMETER_ATTACHEDID));
				moduleId = Integer.parseInt(req.getParameter(PARAMETER_MODULE));
				id = Integer.parseInt(req.getParameter(PARAMETER_ID));
					
			} catch (NumberFormatException x) {
				handle(res, new AbstractOXException("Invalid Number"), action, contentType == null ? JS_FRAGMENT_POPUP : null );
				return;
			}
			
			document(res,isIE(req),isIE7(req), folderId,attachedId,moduleId,id,contentType,ctx,user,userConfig);
		} else {
			final OXJSONWriter writer = new OXJSONWriter();
			final AttachmentRequest attRequest = new AttachmentRequest(session,writer);
			if(!attRequest.action(action,new ServletRequestAdapter(req,res))){
				unknownAction("GET",action,res,false);
			}
			try {
				Response.write(new Response((JSONObject) writer.getObject()), res.getWriter());
			} catch (final JSONException e) {
				LOG.error(e.getLocalizedMessage(), e);
			}
		}
	}
	
	@Override
	protected void doPut(final HttpServletRequest req, final HttpServletResponse res)
	throws ServletException, IOException {
		final SessionObject session = getSessionObject(req);
		
		final String action = req.getParameter(PARAMETER_ACTION);
		if(action == null) {
			missingParameter(PARAMETER_ACTION,res, false, null);
			return ;
		}
		final OXJSONWriter writer = new OXJSONWriter();
		final AttachmentRequest attRequest = new AttachmentRequest(session,writer);
		if(!attRequest.action(action,new ServletRequestAdapter(req,res))){
			unknownAction("PUT",action,res,false);
		}
		try {
			Response.write(new Response((JSONObject) writer.getObject()), res.getWriter());
		} catch (final JSONException e) {
			LOG.error(e.getLocalizedMessage(), e);
		}
	}

	@Override
	protected void doPost(final HttpServletRequest req, final HttpServletResponse res)
	throws ServletException, IOException {
		
		res.setContentType(MIME_TEXT_HTML);
		
		final SessionObject session = getSessionObject(req);
		
		final String action = req.getParameter(PARAMETER_ACTION);
		if(action == null) {
			missingParameter(PARAMETER_ACTION,res, true, "attach");
			return ;
		}
		
		final User user = session.getUserObject();
		final Context ctx = session.getContext();
		final UserConfiguration userConfig = session.getUserConfiguration();
		
		try {
			checkSize(req.getContentLength(), session.getUserSettingMail());
			if (ACTION_ATTACH.equals(action)) {
				UploadEvent upload = null;
				try {
					upload = processUpload(req);
					final List<AttachmentMetadata> attachments = new ArrayList<AttachmentMetadata>();
					final List<UploadFile> uploadFiles = new ArrayList<UploadFile>();
					
					long sum = 0;
					final JSONObject json = new JSONObject();
					final List<UploadFile> l = upload.getUploadFiles();
					final int size = l.size();
					final Iterator<UploadFile> iter = l.iterator();
					for (int a = 0; a < size; a++) {
						final UploadFile uploadFile = iter.next();
						final String fileField = uploadFile.getFieldName();
						final int index = Integer.parseInt(fileField.substring(5));
						final String obj = upload.getFormField(PREFIX_JSON+index);
						if (obj == null || obj.length() == 0) {
							continue;
						}
						json.reset();
						json.parseJSONString(obj);
						for(AttachmentField required : REQUIRED){
							if(!json.has(required.getName())) {
								missingParameter(required.getName(),res, true, action);
							}
						}
						
						final AttachmentMetadata attachment = PARSER.getAttachmentMetadata(json.toString());
						assureSize(index, attachments, uploadFiles);
						
						attachments.set(index, attachment);
						uploadFiles.set(index, uploadFile);
						sum += uploadFile.getSize();
						checkSingleSize(uploadFile.getSize(), session.getUserSettingMail());
						checkSize(sum, session.getUserSettingMail());
					}
					attach(res,attachments,uploadFiles,ctx,user,userConfig);
				} finally {
					if (upload != null) {
						upload.cleanUp();
					}
				}
			}
		}catch (UploadException x) {
			final Response resp = new Response();
			resp.setException(new AbstractOXException(x.getMessage())); // FIXME
			try {
				res.setContentType(MIME_TEXT_HTML_CHARSET_UTF8);
				
				throw new UploadServletException(res, substitute(JS_FRAGMENT, "json", resp.getJSON().toString(), "action","error"),x.getMessage(),x);
			} catch (JSONException e) {
				LOG.error("Giving up",e);
			}
			
		} catch (JSONException e) {
			if (LOG.isErrorEnabled()) {
				LOG.error(e.getMessage(), e);
			}
		} 
	}


	private void assureSize(final int index, final List<AttachmentMetadata> attachments, final List<UploadFile> uploadFiles) {
		int enlarge = index - (attachments.size()-1);
		for(int i = 0; i < enlarge; i++) { attachments.add(null); }
		
		enlarge = index - (uploadFiles.size()-1);
		for(int i = 0; i < enlarge; i++) { uploadFiles.add(null); }
		
	}


	private void document(final HttpServletResponse res, final boolean ie,final boolean ie7, final int folderId, final int attachedId, final int moduleId, final int id, final String contentType, final Context ctx, final User user, final UserConfiguration userConfig) {
		InputStream documentData = null;
		OutputStream os = null;
		
		try {
			ATTACHMENT_BASE.startTransaction();
			final AttachmentMetadata attachment = ATTACHMENT_BASE.getAttachment(folderId,attachedId,moduleId,id,ctx,user,userConfig);
			res.setContentType(contentType == null ? attachment.getFileMIMEType() : contentType);
			res.setContentLength((int) attachment.getFilesize());
			
			documentData = ATTACHMENT_BASE.getAttachedFile(folderId,attachedId,moduleId,id,ctx,user,userConfig);
			
			if(contentType != null && contentType.equals(SAVE_AS_TYPE)) {
				res.setHeader("Content-Disposition", "attachment; filename=\""+Helper.encodeFilename(attachment.getFilename(),"UTF-8",ie)+"\"");
			} else {
				res.setHeader("Content-Disposition", "filename=\""+Helper.encodeFilename(attachment.getFilename(),"UTF-8",ie)+"\"");	
			}

            // Browsers doesn't like the Pragma header the way we usually set
            // this. Especially if files are sent to the browser. So removing
            // pragma header
			Tools.removeCachingHeader(res);

			os = res.getOutputStream();
			
			final byte[] buffer = new byte[200];
			int bytesRead = 0;
			
			while((bytesRead = documentData.read(buffer))!=-1){
				os.write(buffer,0,bytesRead);
				os.flush();
			}
			os=null; // No need to close the IS anymore
			
			ATTACHMENT_BASE.commit();
		} catch (Throwable t) {
			// This is a bit convoluted: In case the contentType is not
			// overridden the returned file will be opened
			// in a new window. To call the JS callback routine from a popup we
			// can use parent.callback_error() but
			// must use window.opener.callback_error()
			rollback(t, res, Response.ERROR, contentType == null ? JS_FRAGMENT_POPUP : null);
			return;
		} finally {
			if(documentData != null) {
				try {
					documentData.close();
				} catch (IOException e) {
					LOG.debug("", e);
				}
			}
			if(os!=null){
				try {
					os.flush();
				} catch (IOException e) {
					LOG.debug("", e);
				}
				/*try {
					os.close();
				} catch (IOException e) {
				}*/
			}
			try {
				ATTACHMENT_BASE.finish();
			} catch (TransactionException e) {
				LOG.debug("", e);
			}
		}
	}

	
	private void rollback(Throwable t, HttpServletResponse res, String action, String fragmentOverride) {
		try {
			ATTACHMENT_BASE.rollback();
		} catch (TransactionException e) {
			LOG.debug("", e);
		}
		if(t instanceof AbstractOXException) {
			handle(res,(AbstractOXException) t,action, fragmentOverride);
		} else {
			handle(res, new OXException(t), action, fragmentOverride);
		}
	}


	private void attach(final HttpServletResponse res, final List<AttachmentMetadata> attachments, final List<UploadFile> uploadFiles, final Context ctx, final User user, final UserConfiguration userConfig) {
		initAttachments(attachments, uploadFiles);
		PrintWriter w = null;
		try {
			ATTACHMENT_BASE.startTransaction();
			//final Iterator<AttachmentMetadata> attIter = attachments.iterator();
			final Iterator<UploadFile> ufIter = uploadFiles.iterator();
			
			final JSONObject result = new JSONObject();
			final JSONArray arr = new JSONArray();
			
			long timestamp = 0;
			
			for (AttachmentMetadata attachment : attachments) {
			//while(attIter.hasNext()) {
				//final AttachmentMetadata attachment = attIter.next();
				final UploadFile uploadFile = ufIter.next();
			
				attachment.setId(AttachmentBase.NEW);
	
				final long modified = ATTACHMENT_BASE.attachToObject(attachment,new BufferedInputStream(new FileInputStream(uploadFile.getTmpFile())),ctx,user,userConfig);
				if(modified  >  timestamp) {
					timestamp = modified;
				}
				arr.put(attachment.getId());
				
			}
			result.put(Response.DATA,arr);
			result.put(Response.TIMESTAMP, timestamp);
			w = res.getWriter();
			w.print(substitute(JS_FRAGMENT, "json", result.toString(),"action",ACTION_ATTACH));
			ATTACHMENT_BASE.commit();
		} catch (OXException t) {
			try {
				ATTACHMENT_BASE.rollback();
			} catch (TransactionException e) {
				LOG.error(e);
			}
			handle(res,t,Response.ERROR, null);
			return;
		} catch (JSONException e) {
			try {
				ATTACHMENT_BASE.rollback();
			} catch (TransactionException x) {
				LOG.error(e);
			}
			handle(res,new OXException(e),Response.ERROR, null);
			return;
		} catch (IOException e) {
			try {
				ATTACHMENT_BASE.rollback();
			} catch (TransactionException x) {
				LOG.error(e);
			}
			handle(res,new OXException(e),Response.ERROR, null);
			return;
		} finally {
			try {
				ATTACHMENT_BASE.finish();
			} catch (TransactionException e) {
				LOG.debug("", e);
			}
		}
	}


	private void initAttachments(final List<AttachmentMetadata> attachments, final List<UploadFile> uploads) {
		final List<AttachmentMetadata> attList = new ArrayList<AttachmentMetadata>(attachments);
		// final Iterator<AttachmentMetadata> attIter = new ArrayList<AttachmentMetadata>(attachments).iterator();
		final Iterator<UploadFile> ufIter = new ArrayList<UploadFile>(uploads).iterator();
		
		int index = 0;
		for (AttachmentMetadata attachment : attList) {
		// while(attIter.hasNext()) {
			// final AttachmentMetadata attachment = attIter.next();
			if(attachment == null) {
				attachments.remove(index);
				ufIter.next();
				uploads.remove(index);
				continue;
			}
			final UploadFile upload = ufIter.next();
			if(upload == null) {
				attachments.remove(index);
				uploads.remove(index);
				continue;
			}
			if(attachment.getFilename() == null || "".equals(attachment.getFilename())){
				String s = upload.getFileName();
				// Try guessing the filename separator
				if(s.contains("\\")){
					s = s.substring(s.lastIndexOf('\\')+1);
				} else if (s.contains("/")){
					s = s.substring(s.lastIndexOf('/')+1);
				}
				attachment.setFilename(s);
			}
			if(attachment.getFilesize() <= 0){
				attachment.setFilesize(upload.getSize());
			}
			if(attachment.getFileMIMEType() == null || "".equals(attachment.getFileMIMEType())){
				attachment.setFileMIMEType(upload.getContentType());
			}
			index++;
		}
	}
	
	private void handle(final HttpServletResponse res, final AbstractOXException t, final String action, final String fragmentOverride) {
		LOG.debug("",t);
		
		res.setContentType(MIME_TEXT_HTML_CHARSET_UTF8);

		final Response resp = new Response();
		resp.setException(t);
		Writer writer = null;
		
		try {
			writer = new StringWriter();
			Response.write(resp, writer);
			res.getWriter().write(substitute((fragmentOverride != null) ? fragmentOverride : JS_FRAGMENT,"json",writer.toString(),"action",action));
		} catch (JSONException e) {
			LOG.error("",t);
		} catch (IOException e) {
			LOG.error("",e);
		}
	}
	
	private void checkSize(final long size, final UserSettingMail userSettingMail) throws UploadException {
		if(maxUploadSize == -2) {
			maxUploadSize = AttachmentConfig.getMaxUploadSize();
		}
		long maxSize = 0;
		maxSize = userSettingMail.getUploadQuota();
		maxSize = maxSize < 0 ? maxUploadSize : maxSize;
		if(maxSize == 0) {
			return;
		}
		
		if(size > maxSize) {
			throw new UploadException(UploadCode.MAX_UPLOAD_SIZE_EXCEEDED, null, Long.valueOf(size), Long.valueOf(maxSize));
		}
	}
	
	private void checkSingleSize(final long size, final UserSettingMail userSettingMail) throws UploadException {
		final long maxSize = userSettingMail.getUploadQuotaPerFile();
		if(maxSize < 1) {
			return;
		}
		if(size > maxSize) {
			throw new UploadException(UploadCode.MAX_UPLOAD_SIZE_EXCEEDED, null, Long.valueOf(size), Long.valueOf(maxSize));
		}
	}
	
	protected void require(final HttpServletRequest req, final HttpServletResponse res, final String... parameters) throws OXException {
		for (String param : parameters) {
			if (req.getParameter(param) == null) {
				throw new OXException("Missing Parameter "+param);
			}
		}
	}
	

}
