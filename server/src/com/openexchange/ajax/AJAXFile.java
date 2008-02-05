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

import static com.openexchange.ajax.Mail.getSaveAsFileName;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.helper.ParamContainer;
import com.openexchange.api.OXConflictException;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.configuration.ServerConfig.Property;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.groupware.Component;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.groupware.contexts.impl.ContextException;
import com.openexchange.groupware.contexts.impl.ContextStorage;
import com.openexchange.groupware.upload.ManagedUploadFile;
import com.openexchange.groupware.upload.impl.AJAXUploadFile;
import com.openexchange.groupware.upload.impl.UploadException;
import com.openexchange.groupware.upload.impl.UploadQuotaChecker;
import com.openexchange.groupware.upload.impl.UploadException.UploadCode;
import com.openexchange.mail.MailException;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MIMEType2ExtMap;
import com.openexchange.session.Session;
import com.openexchange.tools.servlet.OXJSONException;
import com.openexchange.tools.servlet.UploadServletException;
import com.openexchange.tools.servlet.http.Tools;

/**
 * AJAXFile
 * 
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * 
 */
public final class AJAXFile extends PermissionServlet {

	/**
	 * Serial Version UID
	 */
	private static final long serialVersionUID = 1L;

	private static final transient org.apache.commons.logging.Log LOG = org.apache.commons.logging.LogFactory
			.getLog(AJAXFile.class);

	private static final String MIME_TEXT_HTML_CHARSET_UTF_8 = "text/html; charset=UTF-8";

	private static final String MIME_APPLICATION_OCTET_STREAM = "application/octet-stream";

	private static final String STR_NULL = "null";

	public AJAXFile() {
		super();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@Override
	protected void doGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException,
			IOException {
		resp.setContentType(CONTENTTYPE_JAVASCRIPT);
		/*
		 * The magic spell to disable caching
		 */
		Tools.disableCaching(resp);
		final String action = req.getParameter(PARAMETER_ACTION);
		if (ACTION_KEEPALIVE.equalsIgnoreCase(action)) {
			actionKeepAlive(req, resp);
		} else if (ACTION_GET.equalsIgnoreCase(action)) {
			actionGet(req, resp);
		} else {
			final Response response = new Response();
			response.setException(new UploadException(UploadException.UploadCode.UNKNOWN_ACTION_VALUE, null,
					action == null ? STR_NULL : action));
			try {
				Response.write(response, resp.getWriter());
			} catch (final JSONException e) {
				LOG.error(e.getMessage(), e);
				throw new ServletException(e.getMessage(), e);
			}
		}
	}

	private void actionKeepAlive(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException,
			IOException {
		try {
			Response.write(actionKeepAlive(getSessionObject(req), ParamContainer.getInstance(req, Component.UPLOAD,
					resp)), resp.getWriter());
		} catch (final JSONException e) {
			final Response response = new Response();
			response.setException(new OXJSONException(OXJSONException.Code.JSON_WRITE_ERROR, e, new Object[0]));
			try {
				Response.write(response, resp.getWriter());
			} catch (final JSONException e1) {
				LOG.error(e1.getMessage(), e1);
				throw new ServletException(e1.getMessage(), e1);
			}
		}
	}

	private Response actionKeepAlive(final Session sessionObj, final ParamContainer paramContainer) {
		/*
		 * Some variables
		 */
		final Response response = new Response();
		/*
		 * Start response
		 */
		try {
			final String id = paramContainer.checkStringParam(PARAMETER_ID);
			if (!sessionObj.touchUploadedFile(id)) {
				throw new UploadException(UploadCode.UPLOAD_FILE_NOT_FOUND, ACTION_KEEPALIVE, id);
			}
		} catch (final AbstractOXException e) {
			response.setException(e);
		}
		/*
		 * Close response and flush print writer
		 */
		response.setData(JSONObject.NULL);
		response.setTimestamp(null);
		return response;
	}

	private void actionGet(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException,
			IOException {
		try {
			final String id = req.getParameter(PARAMETER_ID);
			if (id == null || id.length() == 0) {
				throw new UploadException(UploadException.UploadCode.MISSING_PARAM, ACTION_GET, PARAMETER_ID);
			}
			/*
			 * Check if user agent is internet explorer
			 */
			final String userAgent = req.getHeader("user-agent") == null ? null : req.getHeader("user-agent")
					.toLowerCase(Locale.ENGLISH);
			final boolean internetExplorer = (userAgent != null && userAgent.indexOf("msie") > -1 && userAgent
					.indexOf("windows") > -1);
			/*
			 * Fetch file from session
			 */
			final Session session = getSessionObject(req);
			final ManagedUploadFile uploadFile = session.getUploadedFile(id);
			if (uploadFile == null) {
				throw new UploadException(UploadException.UploadCode.FILE_NOT_FOUND, ACTION_GET, id);
			}
			/*
			 * Set proper headers
			 */
			final String fileName = getSaveAsFileName(uploadFile.getFileName(), internetExplorer, uploadFile
					.getContentType());
			final ContentType contentType = new ContentType(uploadFile.getContentType());
			if (contentType.getBaseType().equalsIgnoreCase(MIME_APPLICATION_OCTET_STREAM)) {
				/*
				 * Try to determine MIME type
				 */
				final String ct = MIMEType2ExtMap.getContentType(fileName);
				final int pos = ct.indexOf('/');
				contentType.setPrimaryType(ct.substring(0, pos));
				contentType.setSubType(ct.substring(pos + 1));
			}
			contentType.setParameter("name", fileName);
			resp.setContentType(contentType.toString());
			resp.setHeader("Content-disposition", new StringBuilder(50).append("inline; filename=\"").append(fileName)
					.append('"').toString());
			/*
			 * Write from content's input stream to response output stream
			 */
			InputStream contentInputStream = null;
			/*
			 * Reset response header values since we are going to directly write
			 * into servlet's output stream and then some browsers do not allow
			 * header "Pragma"
			 */
			Tools.removeCachingHeader(resp);
			final OutputStream out = resp.getOutputStream();
			try {
				contentInputStream = new FileInputStream(uploadFile.getFile());
				final byte[] buffer = new byte[0xFFFF];
				for (int len; (len = contentInputStream.read(buffer)) != -1;) {
					out.write(buffer, 0, len);
				}
				out.flush();
			} finally {
				if (contentInputStream != null) {
					contentInputStream.close();
					contentInputStream = null;
				}
			}
		} catch (final UploadException e) {
			LOG.error(e.getMessage(), e);
			resp.setContentType(MIME_TEXT_HTML_CHARSET_UTF_8);
			Tools.disableCaching(resp);
			JSONObject responseObj = null;
			try {
				final Response response = new Response();
				response.setException(e);
				responseObj = response.getJSON();
			} catch (final JSONException e1) {
				LOG.error(e1.getMessage(), e1);
			}
			throw new UploadServletException(resp, JS_FRAGMENT.replaceFirst(JS_FRAGMENT_JSON,
					responseObj == null ? STR_NULL : Matcher.quoteReplacement(responseObj.toString())).replaceFirst(
					JS_FRAGMENT_ACTION, e.getAction() == null ? STR_NULL : e.getAction()), e.getMessage(), e);
		} catch (final AbstractOXException e) {
			LOG.error(e.getMessage(), e);
			resp.setContentType(MIME_TEXT_HTML_CHARSET_UTF_8);
			Tools.disableCaching(resp);
			JSONObject responseObj = null;
			try {
				final Response response = new Response();
				response.setException(e);
				responseObj = response.getJSON();
			} catch (final JSONException e1) {
				LOG.error(e1.getMessage(), e1);
			}
			throw new UploadServletException(resp, JS_FRAGMENT.replaceFirst(JS_FRAGMENT_JSON,
					responseObj == null ? STR_NULL : Matcher.quoteReplacement(responseObj.toString())).replaceFirst(
					JS_FRAGMENT_ACTION, ACTION_GET), e.getMessage(), e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException,
			IOException {
		/*
		 * The magic spell to disable caching
		 */
		Tools.disableCaching(resp);
		resp.setContentType(MIME_TEXT_HTML_CHARSET_UTF_8);
		String action = null;
		try {
			if (ServletFileUpload.isMultipartContent(new ServletRequestContext(req))) {
				final DiskFileItemFactory factory = new DiskFileItemFactory();
				/*
				 * Set factory constraints
				 */
				factory.setSizeThreshold(0);
				factory.setRepository(new File(ServerConfig.getProperty(Property.UploadDirectory)));
				/*
				 * Create a new file upload handler
				 */
				final ServletFileUpload upload = new ServletFileUpload(factory);
				/*
				 * Set overall request size constraint
				 */
				final String moduleParam = req.getParameter(PARAMETER_MODULE);
				if (moduleParam == null) {
					throw new UploadException(UploadException.UploadCode.MISSING_PARAM, null, PARAMETER_MODULE);
				}
				final String fileTypeFilter = req.getParameter(PARAMETER_TYPE);
				if (fileTypeFilter == null) {
					throw new UploadException(UploadException.UploadCode.MISSING_PARAM, null, PARAMETER_TYPE);
				}
				final Session sessionObj = getSessionObject(req);
				final UploadQuotaChecker checker = UploadQuotaChecker.getUploadQuotaChecker(
						getModuleInteger(moduleParam), sessionObj, ContextStorage.getStorageContext(sessionObj
								.getContextId()));
				upload.setSizeMax(checker.getQuotaMax());
				upload.setFileSizeMax(checker.getFileQuotaMax());
				/*
				 * Check action parameter
				 */
				try {
					action = getAction(req);
				} catch (final OXConflictException e) {
					throw new UploadException(e, null);
				}
				if (!ACTION_NEW.equalsIgnoreCase(action)) {
					throw new UploadException(UploadException.UploadCode.INVALID_ACTION_VALUE, action, action);
				}
				/*
				 * Process upload
				 */
				final List<FileItem> items;
				try {
					items = upload.parseRequest(req);
				} catch (final FileUploadException e) {
					throw new UploadException(UploadCode.UPLOAD_FAILED, action, e);
				}
				final int size = items.size();
				final Iterator<FileItem> iter = items.iterator();
				final JSONArray jArray = new JSONArray();
				try {
					for (int i = 0; i < size; i++) {
						final FileItem fileItem = iter.next();
						if (!fileItem.isFormField() && fileItem.getSize() > 0 && fileItem.getName() != null
								&& fileItem.getName().length() > 0) {
							if (!checkFileType(fileTypeFilter, fileItem.getContentType())) {
								throw new UploadException(UploadException.UploadCode.INVALID_FILE_TYPE,
										action == null ? STR_NULL : action, fileItem.getContentType(), fileTypeFilter);
							}
							jArray.put(processFileItem(fileItem, sessionObj));
						}
					}
				} catch (final UploadException e) {
					throw e;
				} catch (final Exception e) {
					throw new UploadException(UploadCode.UPLOAD_FAILED, action, e);
				}
				/*
				 * Return IDs of upload files in response
				 */
				final Response response = new Response();
				response.setData(jArray);
				final String jsResponse = JS_FRAGMENT.replaceFirst(JS_FRAGMENT_JSON,
						Matcher.quoteReplacement(response.getJSON().toString())).replaceFirst(JS_FRAGMENT_ACTION,
						action);
				final Writer writer = resp.getWriter();
				writer.write(jsResponse);
				writer.flush();

			}
		} catch (final UploadException e) {
			LOG.error(e.getMessage(), e);
			JSONObject responseObj = null;
			try {
				final Response response = new Response();
				response.setException(e);
				responseObj = response.getJSON();
			} catch (final JSONException e1) {
				LOG.error(e1.getMessage(), e1);
			}
			throw new UploadServletException(resp, JS_FRAGMENT.replaceFirst(JS_FRAGMENT_JSON,
					responseObj == null ? STR_NULL : Matcher.quoteReplacement(responseObj.toString())).replaceFirst(
					JS_FRAGMENT_ACTION, e.getAction() == null ? STR_NULL : e.getAction()), e.getMessage(), e);
		} catch (final JSONException e) {
			final OXJSONException oje = new OXJSONException(OXJSONException.Code.JSON_WRITE_ERROR, e, new Object[0]);
			JSONObject responseObj = null;
			try {
				final Response response = new Response();
				response.setException(oje);
				responseObj = response.getJSON();
			} catch (final JSONException e1) {
				LOG.error(e1.getMessage(), e1);
			}
			throw new UploadServletException(resp, JS_FRAGMENT.replaceFirst(JS_FRAGMENT_JSON,
					responseObj == null ? STR_NULL : Matcher.quoteReplacement(responseObj.toString())).replaceFirst(
					JS_FRAGMENT_ACTION, action == null ? STR_NULL : action), e.getMessage(), e);
		} catch (final ContextException e) {
			final OXJSONException oje = new OXJSONException(OXJSONException.Code.JSON_WRITE_ERROR, e, new Object[0]);
			JSONObject responseObj = null;
			try {
				final Response response = new Response();
				response.setException(oje);
				responseObj = response.getJSON();
			} catch (final JSONException e1) {
				LOG.error(e1.getMessage(), e1);
			}
			throw new UploadServletException(resp, JS_FRAGMENT.replaceFirst(JS_FRAGMENT_JSON,
					responseObj == null ? STR_NULL : Matcher.quoteReplacement(responseObj.toString())).replaceFirst(
					JS_FRAGMENT_ACTION, action == null ? STR_NULL : action), e.getMessage(), e);
		}
	}

	private static final String FILE_TYPE_ALL = "file";

	private static final String FILE_TYPE_TEXT = "text";

	private static final String FILE_TYPE_MEDIA = "media";

	private static final String FILE_TYPE_IMAGE = "image";

	private static final String FILE_TYPE_AUDIO = "audio";

	private static final String FILE_TYPE_VIDEO = "video";

	private static final String FILE_TYPE_APPLICATION = "application";

	private static boolean checkFileType(final String filter, final String fileContentType) throws MailException {
		if (FILE_TYPE_ALL.equalsIgnoreCase(filter)) {
			return true;
		} else if (FILE_TYPE_TEXT.equalsIgnoreCase(filter)) {
			return ContentType.isMimeType(fileContentType, "text/*");
		} else if (FILE_TYPE_MEDIA.equalsIgnoreCase(filter)) {
			final ContentType tmp = new ContentType(fileContentType);
			return tmp.isMimeType("image/*") || tmp.isMimeType("audio/*") || tmp.isMimeType("video/*");
		} else if (FILE_TYPE_IMAGE.equalsIgnoreCase(filter)) {
			return ContentType.isMimeType(fileContentType, "image/*");
		} else if (FILE_TYPE_AUDIO.equalsIgnoreCase(filter)) {
			return ContentType.isMimeType(fileContentType, "audio/*");
		} else if (FILE_TYPE_VIDEO.equalsIgnoreCase(filter)) {
			return ContentType.isMimeType(fileContentType, "video/*");
		} else if (FILE_TYPE_APPLICATION.equalsIgnoreCase(filter)) {
			return ContentType.isMimeType(fileContentType, "application/*");
		}
		return false;
	}

	private static final File UPLOAD_DIR = new File(ServerConfig.getProperty(Property.UploadDirectory));

	private static final String FILE_PREFIX = "openexchange";

	private static String processFileItem(final FileItem fileItem, final Session session) throws Exception {
		final File tmpFile = File.createTempFile(FILE_PREFIX, null, UPLOAD_DIR);
		tmpFile.deleteOnExit();
		fileItem.write(tmpFile);
		final AJAXUploadFile uploadFile = new AJAXUploadFile(tmpFile, System.currentTimeMillis());
		uploadFile.setFileName(fileItem.getName());
		uploadFile.setContentType(fileItem.getContentType());
		uploadFile.setSize(fileItem.getSize());
		final String id = plainStringToMD5(tmpFile.getName());
		session.putUploadedFile(id, uploadFile);
		return id;
	}

	private static final String ALG_MD5 = "MD5";

	private static String plainStringToMD5(final String input) {
		final MessageDigest md;
		try {
			/*
			 * Choose MD5 (SHA1 is also possible)
			 */
			md = MessageDigest.getInstance(ALG_MD5);
		} catch (final NoSuchAlgorithmException e) {
			LOG.error("Unable to generate file ID", e);
			return input;
		}
		/*
		 * Reset
		 */
		md.reset();
		/*
		 * Update the digest
		 */
		try {
			md.update(input.getBytes("UTF-8"));
		} catch (final UnsupportedEncodingException e) {
			/*
			 * Should not occur since utf-8 is a known encoding in jsdk
			 */
			LOG.error("Unable to generate file ID", e);
			return input;
		}
		/*
		 * Here comes the hash
		 */
		final byte[] byteHash = md.digest();
		final StringBuilder resultString = new StringBuilder();
		for (int i = 0; i < byteHash.length; i++) {
			resultString.append(Integer.toHexString(0xF0 & byteHash[i]).charAt(0));
		}
		return resultString.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.openexchange.ajax.PermissionServlet#hasModulePermission(com.openexchange.sessiond.Session)
	 */
	@Override
	protected boolean hasModulePermission(final Session sessionObj, final Context ctx) {
		return true;
	}

}
