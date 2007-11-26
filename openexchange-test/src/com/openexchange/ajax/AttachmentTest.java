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
import com.openexchange.groupware.Init;
import com.openexchange.groupware.attach.AttachmentMetadata;

public class AttachmentTest extends AbstractAJAXTest {
	
	public AttachmentTest(String name) {
		super(name);
	}

	protected String sessionId = null;
	protected File testFile = null;
	protected File testFile2 = null;
	
	protected List<AttachmentMetadata> clean = new ArrayList<AttachmentMetadata>();
	
	public void setUp() throws Exception {
		sessionId = getSessionId();
		testFile = new File(Init.getTestProperty("ajaxPropertiesFile"));
		testFile2 = new File(Init.getTestProperty("webdavPropertiesFile"));
	}
	
	public void tearDown() throws Exception {
		for(AttachmentMetadata attachment : clean) {
			detach(getWebConversation(), sessionId, attachment.getFolderId(), attachment.getAttachedId(), attachment.getModuleId(), new int[]{attachment.getId()});
		}
		clean.clear();
	}
	
	public Response attach(WebConversation webConv, String sessionId, int folderId, int attachedId, int moduleId, List<File> uploads) throws JSONException, IOException {
		return attach(webConv, sessionId, folderId, attachedId, moduleId, uploads, new HashMap<File,String>(), new HashMap<File,String>());
	}
	
	public Response attach(WebConversation webConv, String sessionId, int folderId, int attachedId, int moduleId, List<File> uploads, Map<File, String> filenames, Map<File, String> mimetypes) throws JSONException, IOException {
		StringBuffer url = getUrl(sessionId,"attach");
		
		PostMethodWebRequest req = new PostMethodWebRequest(url.toString());
		req.setMimeEncoded(true);
		
		int index = 0;
		
		for(File upload : uploads) {
		
			JSONObject object = new JSONObject();
			
			String filename = filenames.get(upload);
			String mimeType = mimetypes.get(upload);
			
			object.put("folder", folderId);
			object.put("attached", attachedId);
			object.put("module",moduleId);
			if(filename != null)
				object.put("filename", filename);
			if(mimeType != null)
				object.put("file_mimetype",mimeType);
			
			req.setParameter("json_"+index,object.toString());
			if(upload != null) {
				req.selectFile("file_"+index,upload);
			}
			
			index++;
		}
		
		WebResponse resp = webConv.getResource(req);
		
		String html = resp.getText();
		JSONObject response = extractFromCallback(html);
//		if(!"".equals(response.optString("error"))) {
//			throw new IOException(response.getString("error"));
//		}
		
		return Response.parse(response.toString());
	}
	
	
	public Response attach(WebConversation webConv, String sessionId, int folderId, int attachedId, int moduleId,File upload) throws JSONException, IOException {
		return attach(webConv,sessionId,folderId,attachedId,moduleId,upload,null, null);
	}
	
	public Response attach(WebConversation webConv, String sessionId, int folderId, int attachedId, int moduleId, File upload, String filename, String mimeType) throws JSONException, IOException {
		Map<File, String> filenames = new HashMap<File,String>();
		if(null != filename)
			filenames.put(upload,filename);
		
		Map<File, String> mimeTypes = new HashMap<File,String>();
		if(null != mimeType)
			filenames.put(upload,mimeType);
		
		return attach(webConv,sessionId,folderId,attachedId,moduleId,Arrays.asList(upload), filenames, mimeTypes);
	}

	public Response detach(WebConversation webConv, String sessionId, int folderId, int attachedId, int moduleId, int[] ids) throws MalformedURLException, JSONException, IOException, SAXException {
		StringBuffer url = getUrl(sessionId,"detach");
		addCommon(url, folderId, attachedId, moduleId);
		
		StringBuffer data = new StringBuffer("[");
		for(int id : ids){
			data.append(id);
			data.append(",");
		}
		data.setLength(data.length()-1);
		data.append("]");
		
		return putT(webConv,url.toString(), data.toString());
	}
	
	public Response updates(WebConversation webConv, String sessionId, int folderId, int attachedId, int moduleId, long timestamp, int[] columns, int sort, String order) throws MalformedURLException, JSONException, IOException, SAXException {
		StringBuffer url = getUrl(sessionId, "updates");
		addCommon(url, folderId, attachedId, moduleId);
		addSort(url,columns,sort,order);
		url.append("&timestamp="+timestamp);
		return gT(webConv, url.toString());
	}
	
	public Response all(WebConversation webConv, String sessionId, int folderId, int attachedId, int moduleId, int[] columns, int sort, String order) throws MalformedURLException, JSONException, IOException, SAXException {
		StringBuffer url = getUrl(sessionId, "all");
		addCommon(url, folderId, attachedId, moduleId);
		addSort(url, columns,sort,order);
		
		
		return gT(webConv, url.toString());
	}
	
	private void addSort(StringBuffer url, int[] columns, int sort, String order) {
		StringBuffer cols = new StringBuffer();
		for(int id : columns) {
			cols.append(id);
			cols.append(",");
		}
		cols.setLength(cols.length()-1);
		
		url.append("&columns=");
		url.append(cols.toString());
		
		url.append("&sort=");
		url.append(sort);
		
		url.append("&order=");
		url.append(order);
	}

	public Response list(WebConversation webConv, String sessionId, int folderId, int attachedId, int moduleId, int[] ids, int[] columns) throws JSONException, MalformedURLException, IOException, SAXException {
		StringBuffer url = getUrl(sessionId, "list");
		addCommon(url,folderId,attachedId,moduleId);
		StringBuffer data = new StringBuffer("[");
		for(int id : ids) {
			data.append(id);
			data.append(",");
		}
		data.setLength(data.length()-1);
		data.append("]");
		
		StringBuffer cols = new StringBuffer();
		for(int col : columns) {
			cols.append(col);
			cols.append(",");
		}
		cols.setLength(cols.length()-1);
		
		url.append("&columns=");
		url.append(cols);
		
		
		return putT(webConv, url.toString(), data.toString());
	}
	
	public Response get(WebConversation webConv, String sessionId, int folderId, int attachedId, int moduleId, int id) throws MalformedURLException, JSONException, IOException, SAXException{
		StringBuffer url = getUrl(sessionId,"get");
		addCommon(url, folderId, attachedId, moduleId);
		url.append("&id="+id);
		return gT(webConv, url.toString());
	}
	
	public InputStream document(WebConversation webConv, String sessionId, int folderId, int attachedId, int moduleId, int id) throws IOException {
		return document(webConv, sessionId, folderId, attachedId, moduleId, id, null);
	}
	
	public InputStream document(WebConversation webConv, String sessionId, int folderId, int attachedId, int moduleId, int id, String contentType) throws IOException{
		GetMethodWebRequest m = documentRequest(sessionId, folderId, attachedId, moduleId, id, contentType);
		WebResponse resp = getWebConversation().getResource(m);
		
		return resp.getInputStream();
	}
	
	public GetMethodWebRequest documentRequest(String sessionId, int folderId, int attachedId, int moduleId, int id, String contentType) {
		StringBuffer url = getUrl(sessionId,"document");
		addCommon(url, folderId, attachedId, moduleId);
		url.append("&id="+id);
		if(null != contentType) {
			contentType = contentType.replaceAll("/", "%2F");
			url.append("&content_type=");
			url.append(contentType);
		}
		
		return new GetMethodWebRequest(url.toString());
	}
	
	public Response quota(WebConversation webConv, String sessionId) throws MalformedURLException, JSONException, IOException, SAXException {
		StringBuffer url = new StringBuffer("http://");
		url.append(getHostName());
		url.append("/ajax/quota?session=");
		url.append(sessionId);
		url.append("&action=filestore");
		return gT(webConv, url.toString());
	}
	
	private void addCommon(StringBuffer url, int folderId, int attachedId, int moduleId) {
		url.append("&folder="+folderId);
		url.append("&attached="+attachedId);
		url.append("&module="+moduleId);
	}

	protected StringBuffer getUrl(String sessionId, String action) {
		StringBuffer url = new StringBuffer("http://");
		url.append(getHostName());
		url.append("/ajax/attachment?session=");
		url.append(sessionId);
		url.append("&action=");
		url.append(action);
		return url;
	}
}
