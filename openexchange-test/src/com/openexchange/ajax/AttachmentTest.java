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

package com.openexchange.ajax;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.tools.JSONCoercion;
import com.openexchange.groupware.attach.AttachmentMetadata;
import com.openexchange.test.TestInit;

public class AttachmentTest extends AbstractAJAXTest {

	public AttachmentTest(final String name) {
		super(name);
	}

	protected String sessionId = null;
	protected File testFile = null;
	protected File testFile2 = null;

	protected List<AttachmentMetadata> clean = new ArrayList<AttachmentMetadata>();

	@Override
	public void setUp() throws Exception {
		sessionId = getSessionId();
		testFile = new File(TestInit.getTestProperty("ajaxPropertiesFile"));
		testFile2 = new File(TestInit.getTestProperty("webdavPropertiesFile"));
	}

	@Override
	public void tearDown() throws Exception {
		removeAttachments();
		super.tearDown();
	}

	public void removeAttachments() throws Exception{
		for(final AttachmentMetadata attachment : clean) {
			detach(getWebConversation(), sessionId, attachment.getFolderId(), attachment.getAttachedId(), attachment.getModuleId(), new int[]{attachment.getId()});
		}
		clean.clear();
	}

	public Response attach(final WebConversation webConv, final String sessionId, final int folderId, final int attachedId, final int moduleId, final List<File> uploads) throws JSONException, IOException {
		return attach(webConv, sessionId, folderId, attachedId, moduleId, uploads, new HashMap<File,String>(), new HashMap<File,String>());
	}

	public Response attach(final WebConversation webConv, final String sessionId, final int folderId, final int attachedId, final int moduleId, final List<File> uploads, final Map<File, String> filenames, final Map<File, String> mimetypes) throws JSONException, IOException {
		final StringBuffer url = getUrl(sessionId,"attach");

		final PostMethodWebRequest req = new PostMethodWebRequest(url.toString(), true);

		int index = 0;

		for(final File upload : uploads) {

			final JSONObject object = new JSONObject();

			final String filename = filenames.get(upload);
			final String mimeType = mimetypes.get(upload);

			object.put("folder", folderId);
			object.put("attached", attachedId);
			object.put("module",moduleId);
			if(filename != null) {
				object.put("filename", filename);
			}
			if(mimeType != null) {
				object.put("file_mimetype",mimeType);
			}

			req.setParameter("json_"+index,object.toString());
			if(upload != null) {
				req.selectFile("file_"+index,upload);
			}

			index++;
		}

		final WebResponse resp = webConv.getResource(req);

		final String html = resp.getText();
		final JSONObject response = extractFromCallback(html);
//		if(!"".equals(response.optString("error"))) {
//			throw new IOException(response.getString("error"));
//		}

		return Response.parse(response.toString());
	}

	public Response attach(final WebConversation webConv, final String sessionId, final int folderId, final int attachedId, final int moduleId,final File upload) throws JSONException, IOException {
		return attach(webConv,sessionId,folderId,attachedId,moduleId,upload,null, null);
	}

	public Response attach(final WebConversation webConv, final String sessionId, final int folderId, final int attachedId, final int moduleId, final File upload, final String filename, final String mimeType) throws JSONException, IOException {
		final Map<File, String> filenames = new HashMap<File,String>();
		if(null != filename) {
			filenames.put(upload,filename);
		}

		final Map<File, String> mimeTypes = new HashMap<File,String>();
		if(null != mimeType) {
			filenames.put(upload,mimeType);
		}

		return attach(webConv,sessionId,folderId,attachedId,moduleId,Arrays.asList(upload), filenames, mimeTypes);
	}

	public Response attach (final WebConversation webConv, final String sessionId, final int folderId, final int attachedId, final int moduleId, Map<String, Object> dataSourceParams) throws JSONException, MalformedURLException, IOException, SAXException {
	    final StringBuffer url = getUrl(sessionId,"attach");

        JSONObject object = new JSONObject();
        object.put("folder", folderId);
        object.put("attached", attachedId);
        object.put("module",moduleId);

        object.put("datasource", JSONCoercion.coerceToJSON(dataSourceParams));

        JSONObject resp = put(webConv, url.toString(), object.toString());
        return Response.parse(resp.toString());

	    //return Response.parse(response.toString());
	}

	public Response detach(final WebConversation webConv, final String sessionId, final int folderId, final int attachedId, final int moduleId, final int[] ids) throws MalformedURLException, JSONException, IOException, SAXException {
		final StringBuffer url = getUrl(sessionId,"detach");
		addCommon(url, folderId, attachedId, moduleId);

		final StringBuffer data = new StringBuffer("[");
		for(final int id : ids){
			data.append(id);
			data.append(',');
		}
		data.setLength(data.length()-1);
		data.append(']');

		return putT(webConv,url.toString(), data.toString());
	}

	public Response updates(final WebConversation webConv, final String sessionId, final int folderId, final int attachedId, final int moduleId, final long timestamp, final int[] columns, final int sort, final String order) throws MalformedURLException, JSONException, IOException, SAXException {
		final StringBuffer url = getUrl(sessionId, "updates");
		addCommon(url, folderId, attachedId, moduleId);
		addSort(url,columns,sort,order);
		url.append("&timestamp="+timestamp);
		return gT(webConv, url.toString());
	}

	public Response all(final WebConversation webConv, final String sessionId, final int folderId, final int attachedId, final int moduleId, final int[] columns, final int sort, final String order) throws MalformedURLException, JSONException, IOException, SAXException {
		final StringBuffer url = getUrl(sessionId, "all");
		addCommon(url, folderId, attachedId, moduleId);
		addSort(url, columns,sort,order);


		return gT(webConv, url.toString());
	}

	private void addSort(final StringBuffer url, final int[] columns, final int sort, final String order) {
		final StringBuffer cols = new StringBuffer();
		for(final int id : columns) {
			cols.append(id);
			cols.append(',');
		}
		cols.setLength(cols.length()-1);

		url.append("&columns=");
		url.append(cols.toString());

        url.append("&sort=");
		url.append(sort);

		url.append("&order=");
		url.append(order);
	}

	public Response list(final WebConversation webConv, final String sessionId, final int folderId, final int attachedId, final int moduleId, final int[] ids, final int[] columns) throws JSONException, MalformedURLException, IOException, SAXException {
		final StringBuffer url = getUrl(sessionId, "list");
		addCommon(url,folderId,attachedId,moduleId);
		final StringBuffer data = new StringBuffer("[");
		for(final int id : ids) {
			data.append(id);
			data.append(',');
		}
		data.setLength(data.length()-1);
		data.append(']');

		final StringBuffer cols = new StringBuffer();
		for(final int col : columns) {
			cols.append(col);
			cols.append(',');
		}
		cols.setLength(cols.length()-1);

		url.append("&columns=");
		url.append(cols);


		return putT(webConv, url.toString(), data.toString());
	}

	public Response get(final WebConversation webConv, final String sessionId, final int folderId, final int attachedId, final int moduleId, final int id) throws MalformedURLException, JSONException, IOException, SAXException{
		final StringBuffer url = getUrl(sessionId,"get");
		addCommon(url, folderId, attachedId, moduleId);
		url.append("&id="+id);
		return gT(webConv, url.toString());
	}

	public InputStream document(final WebConversation webConv, final String sessionId, final int folderId, final int attachedId, final int moduleId, final int id) throws IOException {
		return document(webConv, sessionId, folderId, attachedId, moduleId, id, null);
	}

	public InputStream document(final WebConversation webConv, final String sessionId, final int folderId, final int attachedId, final int moduleId, final int id, final String contentType) throws IOException{
		final GetMethodWebRequest m = documentRequest(sessionId, folderId, attachedId, moduleId, id, contentType);
		final WebResponse resp = getWebConversation().getResource(m);

		return resp.getInputStream();
	}

	public GetMethodWebRequest documentRequest(final String sessionId, final int folderId, final int attachedId, final int moduleId, final int id, String contentType) {
		final StringBuffer url = getUrl(sessionId,"document");
		addCommon(url, folderId, attachedId, moduleId);
		url.append("&id="+id);
		if(null != contentType) {
			contentType = contentType.replaceAll("/", "%2F");
			url.append("&content_type=");
			url.append(contentType);
		}

		return new GetMethodWebRequest(url.toString());
	}

	public Response quota(final WebConversation webConv, final String sessionId) throws MalformedURLException, JSONException, IOException, SAXException {
		final StringBuffer url = new StringBuffer("http://");
		url.append(getHostName());
		url.append("/ajax/quota?session=");
		url.append(sessionId);
		url.append("&action=filestore");
		return gT(webConv, url.toString());
	}

	private void addCommon(final StringBuffer url, final int folderId, final int attachedId, final int moduleId) {
		url.append("&folder="+folderId);
		url.append("&attached="+attachedId);
		url.append("&module="+moduleId);
	}

	protected StringBuffer getUrl(final String sessionId, final String action) {
		final StringBuffer url = new StringBuffer("http://");
		url.append(getHostName());
		url.append("/ajax/attachment?session=");
		url.append(sessionId);
		url.append("&action=");
		url.append(action);
		return url;
	}
}
