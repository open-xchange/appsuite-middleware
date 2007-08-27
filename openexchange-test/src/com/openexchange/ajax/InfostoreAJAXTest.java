package com.openexchange.ajax;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.httpclient.HttpException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.container.Response;

public class InfostoreAJAXTest extends AbstractAJAXTest {

	public static final String INFOSTORE_FOLDER = "infostore.folder";
	
	protected int folderId;
	
	protected String sessionId;
	
	protected List<Integer> clean = new ArrayList<Integer>();
	
	protected String hostName = null;
	
	public InfostoreAJAXTest(String name){
		super(name);
	}	
	
	public void setUp() throws Exception {
		this.sessionId = getSessionId();
		final int userId = FolderTest.getUserId(getWebConversation(), getHostName(), getLogin(), getPassword());
		this.folderId = FolderTest.getMyInfostoreFolder(getWebConversation(),getHostName(),sessionId,userId).getObjectID();
		
		Map<String,String> create = m(
			"folder_id" 		,	((Integer)folderId).toString(),
			"title"  		,  	"test knowledge",
			"description" 	, 	"test knowledge description"
		);
		
		int c = this.createNew(getWebConversation(),getHostName(), sessionId, create);
		
		clean.add(c);
		
		create = m(
				"folder_id" 		, 	((Integer)folderId).toString(),
				"title"  		,  	"test url",
				"description" 	, 	"test url description",
				"url" 			, 	"http://www.open-xchange.com"
			);
			
		c = this.createNew(getWebConversation(),getHostName(), sessionId, create);
		
		clean.add(c);
	}
	
	public void tearDown() throws Exception {
		int[][] toDelete = new int[clean.size()][2];
		
		for(int i = 0; i < toDelete.length; i++) {
			toDelete[i][0] = folderId; // FIXME: Put a correct folderId here
			toDelete[i][1] = clean.get(i);
		}
		
		int[] notDeleted = delete(getWebConversation(),getHostName(),sessionId, System.currentTimeMillis(), toDelete);
		
		//assertEquals("Couldn't delete "+j(notDeleted),0,notDeleted.length);
	}
	
	
	private String j(int[] ids) {
		StringBuffer b = new StringBuffer("[ ");
		for(int i : ids) {
			b.append(i);
			b.append(" ");
		}
		b.append("]");
		return b.toString();
	}
	
	// Methods from the specification
	
	
	public Response all(WebConversation webConv, String hostname, String sessionId, int folderId, int[] columns) throws MalformedURLException, JSONException, IOException, SAXException  {
		return all(webConv,hostname,sessionId,folderId,columns, -1, null);
	}
	
	public Response all(WebConversation webConv, String hostname, String sessionId, int folderId, int[] columns, int sort, String order) throws MalformedURLException, JSONException, IOException, SAXException {
		StringBuffer url = getUrl(sessionId,"all", hostname);
		url.append("&folder=");
		url.append(folderId);
		url.append("&columns=");
		for(int col : columns) {
			url.append(col);
			url.append(",");
		}
		url.deleteCharAt(url.length()-1);
		
		if(sort != -1) {
			url.append("&sort=");
			url.append(sort);
		}
		
		if(order != null){
			url.append("&order=");
			url.append(order);
		}
		
		return gT(webConv, url.toString());
	}
	
	public Response list(WebConversation webConv, String hostname, String sessionId, int[] columns, int[][] ids) throws MalformedURLException, JSONException, IOException, SAXException {
		StringBuffer url = getUrl(sessionId,"list", hostname);
		url.append("&columns=");
		for(int col : columns) {
			url.append(col);
			url.append(",");
		}
		url.deleteCharAt(url.length()-1);
		
		StringBuffer data = new StringBuffer("[");
		if(ids.length > 0) {
			for(int[] tuple : ids) {
				data.append("{folder : ");
				data.append(tuple[0]);
				data.append(", id : ");
				data.append(tuple[1]);
				data.append("},");
			}
			data.deleteCharAt(data.length()-1);
		}
		data.append("]");
		
		return putT(webConv,url.toString(), data.toString()); 
	}

	public Response updates(WebConversation webConv, String hostname, String sessionId, int folderId, int[] columns, long timestamp) throws MalformedURLException, JSONException, IOException, SAXException {
		return updates(webConv,hostname,sessionId,folderId,columns,timestamp,-1, null, null);
	}
	
	public Response updates(WebConversation webConv, String hostname, String sessionId, int folderId, int[] columns, long timestamp, String ignore) throws MalformedURLException, JSONException, IOException, SAXException {
		return updates(webConv,hostname,sessionId,folderId,columns,timestamp,-1, null, ignore);
	}
	
	public Response updates(WebConversation webConv, String hostname, String sessionId, int folderId, int[] columns, long timestamp, int sort, String order, String ignore) throws MalformedURLException, JSONException, IOException, SAXException {
		StringBuffer url = getUrl(sessionId,"updates", hostname);
		url.append("&folder=");
		url.append(folderId);
		url.append("&columns=");
		for(int col : columns) {
			url.append(col);
			url.append(",");
		}
		url.deleteCharAt(url.length()-1);
		
		url.append("&timestamp=");
		url.append(timestamp);
		
		if(sort != -1) {
			url.append("&sort=");
			url.append(sort);
		}
		
		if(order != null){
			url.append("&order=");
			url.append(order);
		}
		
		if(ignore != null){
			url.append("&ignore=");
			url.append(ignore);	
		}
		
		return gT(webConv, url.toString());
	}
	
	public Response get(WebConversation webConv, String hostname, String sessionId, int objectId) throws MalformedURLException, JSONException, IOException, SAXException  {
		return get(webConv,hostname,sessionId, objectId, -1);
	}
	
	public Response get(WebConversation webConv, String hostname, String sessionId, int objectId, int version) throws MalformedURLException, JSONException, IOException, SAXException {
		StringBuffer url = getUrl(sessionId,"get", hostname);
		url.append("&id=");
		url.append(objectId);
		if(version != -1) {
			url.append("&version=");
			url.append(version);
		}
		
		return gT(webConv, url.toString());
	}
	
	public Response versions(WebConversation webConv, String hostname, String sessionId, int objectId, int[] columns) throws MalformedURLException, JSONException, IOException, SAXException {
		return versions(webConv,hostname,sessionId,objectId,columns, -1, null);
	}
	
	public Response versions(WebConversation webConv, String hostname, String sessionId, int objectId, int[] columns, int sort, String order) throws MalformedURLException, JSONException, IOException, SAXException {
		StringBuffer url = getUrl(sessionId,"versions", hostname);
		url.append("&id=");
		url.append(objectId);
		url.append("&columns=");
		for(int col : columns) {
			url.append(col);
			url.append(",");
		}
		url.deleteCharAt(url.length()-1);
		
		if(sort != -1) {
			url.append("&sort=");
			url.append(sort);
		}
		
		if(order != null){
			url.append("&order=");
			url.append(order);
		}
		
		return gT(webConv, url.toString());
	}
	
	private JSONObject toJSONArgs(Map<String, String> modified) throws JSONException {
		JSONObject obj = new JSONObject();
		for(String attr : modified.keySet()) {
			if(attr.equals("categories")) {
				obj.put(attr, new JSONArray(modified.get(attr)));
			} else {
				obj.put(attr, modified.get(attr));
			}
		}
		return obj;
	}

	
	public Response update(WebConversation webConv, String hostname, String sessionId, int id, long timestamp, Map<String,String> modified) throws MalformedURLException, IOException, SAXException, JSONException {
		StringBuffer url = getUrl(sessionId,"update", hostname);
		url.append("&id=");
		url.append(id);
		
		url.append("&timestamp=");
		url.append(timestamp);
		JSONObject obj = toJSONArgs(modified);
		
		return putT(webConv,url.toString(), obj.toString());
	}
	
	public Response update(WebConversation webConv, String hostname, String sessionId, int id, long timestamp, Map<String, String> modified, File upload, String contentType) throws MalformedURLException, IOException, SAXException, JSONException {
		StringBuffer url = getUrl(sessionId,"update", hostname);
		url.append("&id=");
		url.append(id);
		
		url.append("&timestamp=");
		url.append(timestamp);
		
		PostMethodWebRequest req = new PostMethodWebRequest(url.toString());
		req.setMimeEncoded(true);
		
		JSONObject obj = toJSONArgs(modified);
		
		req.setParameter("json",obj.toString());
		
		if(upload!=null) {
			req.selectFile("file",upload,contentType);
		}
		WebResponse resp = webConv.getResource(req);
		JSONObject res = extractFromCallback(resp.getText());
		return Response.parse(res.toString());
	}
	
	public int createNew(WebConversation webConv, String hostname, String sessionId, Map<String,String> fields) throws MalformedURLException, IOException, SAXException, JSONException  {
		StringBuffer url = getUrl(sessionId,"new", hostname);
		JSONObject obj = toJSONArgs(fields);
		
		PutMethodWebRequest m = new PutMethodWebRequest(url.toString(), new ByteArrayInputStream(obj.toString().getBytes("UTF-8")),"text/javascript");
		
		WebResponse resp = webConv.getResponse(m);
		try {
			return new Integer(new JSONObject(resp.getText()).getInt("data"));
		} catch (JSONException x) {
			throw new JSONException("Got unexpected answer: "+resp.getText());
		}
	}
	
	public int createNew(WebConversation webConv, String hostname, String sessionId, Map<String, String> fields, File upload, String contentType) throws MalformedURLException, IOException, SAXException, JSONException {
		StringBuffer url = getUrl(sessionId,"new", hostname);
		PostMethodWebRequest req = new PostMethodWebRequest(url.toString());
		req.setMimeEncoded(true);
		
		JSONObject obj = toJSONArgs(fields);
		
		req.setParameter("json",obj.toString());
		
		if(upload != null) {
			req.selectFile("file",upload,contentType);
		}
		
		WebResponse resp = webConv.getResource(req);
		
		String html = resp.getText();
		JSONObject response = extractFromCallback(html);
		if(response == null) {
			throw new IOException("Didn't receive response");
		}
		if(!"".equals(response.optString("error"))) {
			throw new IOException(response.getString("error"));
		}
		try {
			return response.getInt("data");
		} catch (JSONException x) {
			throw new JSONException("Got unexpected answer: "+response);
		}
	}
	
	public int saveAs(WebConversation webConv, String hostname, String sessionId, int folderId, int attached, int module, int attachment, Map<String,String> fields) throws MalformedURLException, IOException, SAXException, JSONException  {
		StringBuffer url = getUrl(sessionId,"saveAs", hostname);
		url.append("&folder=");
		url.append(folderId);
		url.append("&attached=");
		url.append(attached);
		url.append("&module=");
		url.append(module);
		url.append("&attachment=");
		url.append(attachment);
		JSONObject obj = toJSONArgs(fields);
		
		PutMethodWebRequest m = new PutMethodWebRequest(url.toString(), new ByteArrayInputStream(obj.toString().getBytes()),"text/javascript");
		
		WebResponse resp = webConv.getResponse(m);
		Response res = Response.parse(resp.getText());
		if(res.hasError())
			throw new JSONException(res.getErrorMessage());
		return (Integer) res.getData();
	}

	public int[] delete(WebConversation webConv, String hostname, String sessionId, long timestamp, int[][] ids) throws MalformedURLException, JSONException, IOException, SAXException {
		StringBuffer url = getUrl(sessionId,"delete", hostname);
		url.append("&timestamp=");
		url.append(timestamp);
		
		
		StringBuffer data = new StringBuffer("[");
		
		if(ids.length > 0) {
			for(int[] tuple : ids) {
				data.append("{folder : ");
				data.append(tuple[0]);
				data.append(", id : ");
				data.append(tuple[1]);
				data.append("},");
			}
			data.deleteCharAt(data.length()-1);
		}
		
		data.append("]");
		
		JSONArray arr = putA(webConv, url.toString(), data.toString());
		int[] notDeleted = new int[arr.length()];
		
		for(int i = 0; i < arr.length(); i++) {
			notDeleted[i] = arr.getInt(i);
		}
		
		return notDeleted;
	}
	
	public int[] detach(WebConversation webConv, String hostname, String sessionId, long timestamp, int objectId, int[] versions) throws MalformedURLException, JSONException, IOException, SAXException {
		StringBuffer url = getUrl(sessionId,"detach", hostname);
		url.append("&timestamp=");
		url.append(timestamp);
		url.append("&id=");
		url.append(objectId);
		
		
		StringBuffer data = new StringBuffer("[");
		
		if(versions.length > 0) {
			for(int id : versions) {
				data.append(id);
				data.append(",");
			}
			data.deleteCharAt(data.length()-1);
		}
		
		data.append("]");
		
		String content = putS(webConv, url.toString(), data.toString());
		JSONArray arr = null;
		try{
			arr = new JSONArray(content);
		} catch (JSONException x) {
			Response res = Response.parse(content);
			if(res.hasError())
				throw new IOException(res.getErrorMessage());
		}
		int[] notDeleted = new int[arr.length()];
		
		for(int i = 0; i < arr.length(); i++) {
			notDeleted[i] = arr.getInt(i);
		}
		
		return notDeleted;
	}
	
	public Response revert(WebConversation webConv, String hostname, String sessionId, long timestamp, int objectId) throws MalformedURLException, JSONException, IOException, SAXException {
		StringBuffer url = getUrl(sessionId,"revert", hostname);
		url.append("&timestamp=");
		url.append(timestamp);
		url.append("&id=");
		url.append(objectId);
		
		return gT(webConv, url.toString());
	}
	
	public InputStream document(WebConversation webConv, String hostname, String sessionId, int id) throws HttpException, IOException {
		return document(webConv,hostname,sessionId, id, -1, null);
	}
	
	public InputStream document(WebConversation webConv, String hostname, String sessionId, int id, String contentType) throws HttpException, IOException {
		return document(webConv, hostname, sessionId, id, -1, contentType);
	}
	
	public InputStream document(WebConversation webConv, String hostname, String sessionId, int id, int version) throws HttpException, IOException{
		return document(webConv,hostname,sessionId,id,version, null);
	}
	
	public InputStream document(WebConversation webConv, String hostname, String sessionId, int id, int version, String contentType) throws HttpException, IOException{
		
		GetMethodWebRequest m = documentRequest(sessionId, hostname, id, version, contentType);
		WebResponse resp = webConv.getResource(m);
		
		return resp.getInputStream();
	}
	
	public GetMethodWebRequest documentRequest(String sessionId, String hostname, int id, int version, String contentType) {
		StringBuffer url = getUrl(sessionId,"document", hostname);
		url.append("&id="+id);
		if(version!=-1)
			url.append("&version="+version);
		
		if(null != contentType) {
			contentType = contentType.replaceAll("/", "%2F");
			url.append("&content_type=");
			url.append(contentType);
		}
		return new GetMethodWebRequest(url.toString());
	}
	
	public int copy(WebConversation webConv, String hostname, String sessionId, int id, long timestamp, Map<String, String> modified, File upload, String contentType) throws JSONException, IOException {
		StringBuffer url = getUrl(sessionId,"copy", hostname);
		url.append("&id=");
		url.append(id);
		
		url.append("&timestamp=");
		url.append(timestamp);
		
		PostMethodWebRequest req = new PostMethodWebRequest(url.toString());
		req.setMimeEncoded(true);
		
		JSONObject obj = new JSONObject();
		for(String attr : modified.keySet()) {
			obj.put(attr, modified.get(attr));
		}
		
		req.setParameter("json",obj.toString());
		
		if(upload!=null) {
			req.selectFile("file",upload,contentType);
		}
		WebResponse resp = webConv.getResource(req);
		JSONObject res = extractFromCallback(resp.getText());
		return (Integer) Response.parse(res.toString()).getData();
	}
	
	public int copy(WebConversation webConv, String hostname, String sessionId, int id, long timestamp, Map<String, String> modified) throws MalformedURLException, JSONException, IOException, SAXException {
		StringBuffer url = getUrl(sessionId,"copy", hostname);
		url.append("&id=");
		url.append(id);
		
		url.append("&timestamp=");
		url.append(timestamp);
		JSONObject obj = new JSONObject();
		for(String attr : modified.keySet()) {
			obj.put(attr, modified.get(attr));
		}
		
		Response res = putT(webConv,url.toString(), obj.toString());
		if(res.hasError())
			throw new JSONException(res.getErrorMessage());
		return (Integer) res.getData();
	}
	
	public Response lock(WebConversation webConv, String hostname, String sessionId, int id, long timeDiff) throws MalformedURLException, JSONException, IOException, SAXException {
		StringBuffer url = getUrl(sessionId,"lock", hostname);
		url.append("&id=");
		url.append(id);
		if(timeDiff > 0) {
			url.append("&diff=");
			url.append(timeDiff);
		}
		
		return gT(webConv, url.toString());
	}
	
	public Response lock(WebConversation webConv, String hostname, String sessionId, int id) throws MalformedURLException, JSONException, IOException, SAXException {
		return lock(webConv,hostname,sessionId, id, -1);
	}
	
	public Response unlock(WebConversation webConv, String hostname, String sessionId, int id ) throws MalformedURLException, JSONException, IOException, SAXException {
		StringBuffer url = getUrl(sessionId,"unlock", hostname);
		url.append("&id=");
		url.append(id);
		
		return gT(webConv, url.toString());
	}
	
	public Response search(WebConversation webConv, String hostname, String sessionId, String query, int[] columns) throws MalformedURLException, JSONException, IOException, SAXException {
		return search(webConv,hostname,sessionId,query,columns,-1,-1,null, -1, -1);
	}
	
	public Response search(WebConversation webConv, String hostname, String sessionId, String query, int[] columns, int folderId, int sort, String order, int start, int end) throws MalformedURLException, JSONException, IOException, SAXException {
		StringBuffer url = getUrl(sessionId,"search", hostname);
		url.append("&columns=");
		for(int c : columns) {
			url.append(c);
			url.append(",");
		}
		url.setLength(url.length()-1);
		if(folderId != -1) {
			url.append("&folder=");
			url.append(folderId);
		}
		
		if(sort != -1) {
			url.append("&sort=");
			url.append(sort);
			
			url.append("&order=");
			url.append(order);
			
			if(start != -1) {
				url.append("&start=");
				url.append(start);
			}
			
			if(end != -1) {
				url.append("&end=");
				url.append(end);
			}
		}
		JSONObject queryObject = new JSONObject();
		queryObject.put("pattern",query);
		return putT(webConv,url.toString(), queryObject.toString());
	}
	
	public Response search(WebConversation webConv, String hostname, String sessionId, String query, int[] columns, int folderId, int sort, String order, int limit) throws MalformedURLException, JSONException, IOException, SAXException {
		StringBuffer url = getUrl(sessionId,"search", hostname);
		url.append("&columns=");
		for(int c : columns) {
			url.append(c);
			url.append(",");
		}
		url.setLength(url.length()-1);
		if(folderId != -1) {
			url.append("&folder=");
			url.append(folderId);
		}
		
		if(sort != -1) {
			url.append("&sort=");
			url.append(sort);
			
			url.append("&order=");
			url.append(order);
			
			url.append("&limit=");
			url.append(limit);
		}
		JSONObject queryObject = new JSONObject();
		queryObject.put("pattern",query);
		return putT(webConv,url.toString(), queryObject.toString());
	}
	
	protected StringBuffer getUrl(String sessionId, String action, String hostname) {
		StringBuffer url = new StringBuffer("http://");
		url.append((hostname == null) ? getHostName() : hostname );
		url.append("/ajax/infostore?session=");
		url.append(sessionId);
		url.append("&action=");
		url.append(action);
		return url;
	}

	@Override
	public String getHostName() {
		if(null == hostName)
			return super.getHostName();
		return hostName;
	}
	
	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

}
