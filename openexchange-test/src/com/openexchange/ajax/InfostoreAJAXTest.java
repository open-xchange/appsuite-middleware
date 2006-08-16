package com.openexchange.ajax;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.container.Response;



public abstract class InfostoreAJAXTest extends AbstractAJAXTest {

	public static final String INFOSTORE_FOLDER = "infostore.folder";
	
	protected int folderId;
	
	protected String sessionId;
	
	protected List<Integer> clean = new ArrayList<Integer>();
	
	
	private static Pattern CALLBACK_ARG_PATTERN = Pattern.compile("callback\\s*\\((.*?)\\);");
	
	
	public InfostoreAJAXTest(){
		super();
	}	
	
	public void setUp() throws Exception {
		this.sessionId = getSessionId();
		this.folderId = FolderTest.getMyInfostoreFolder(getWebConversation(),getHostName(),sessionId).getObjectID();
		
		Map<String,String> create = m(
			"folder_id" 		,	((Integer)folderId).toString(),
			"title"  		,  	"test knowledge",
			"description" 	, 	"test knowledge description"
		);
		
		int c = this.createNew(sessionId,create);
		
		clean.add(c);
		
		create = m(
				"folder_id" 		, 	((Integer)folderId).toString(),
				"title"  		,  	"test url",
				"description" 	, 	"test url description",
				"url" 			, 	"http://www.open-xchange.com"
			);
			
		c = this.createNew(sessionId,create);
		
		clean.add(c);
	}
	
	public void tearDown() throws Exception {
		int[][] toDelete = new int[clean.size()][2];
		
		for(int i = 0; i < toDelete.length; i++) {
			toDelete[i][0] = folderId; // FIXME: Put a correct folderId here
			toDelete[i][1] = clean.get(i);
		}
		
		int[] notDeleted = delete(sessionId,System.currentTimeMillis(),toDelete);
		assertEquals("Couldn't delete "+j(notDeleted),0,notDeleted.length);
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
	
	
	public Response all(String sessionId, int folderId, int[] columns) throws MalformedURLException, JSONException, IOException, SAXException  {
		return all(sessionId,folderId,columns,-1,null);
	}
	
	public Response all(String sessionId, int folderId, int[] columns, int sort, String order) throws MalformedURLException, JSONException, IOException, SAXException {
		StringBuffer url = getUrl(sessionId,"all");
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
		
		return gT(url.toString());
	}
	
	public Response list(String sessionId, int[] columns, int[][] ids) throws MalformedURLException, JSONException, IOException, SAXException {
		StringBuffer url = getUrl(sessionId,"list");
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
		
		return putT(url.toString(),data.toString()); 
	}

	public Response updates(String sessionId, int folderId, int[] columns, long timestamp) throws MalformedURLException, JSONException, IOException, SAXException {
		return updates(sessionId,folderId,columns,timestamp,-1,null,null);
	}
	
	public Response updates(String sessionId, int folderId, int[] columns, long timestamp, String ignore) throws MalformedURLException, JSONException, IOException, SAXException {
		return updates(sessionId,folderId,columns,timestamp,-1,null,ignore);
	}
	
	public Response updates(String sessionId, int folderId, int[] columns, long timestamp, int sort, String order, String ignore) throws MalformedURLException, JSONException, IOException, SAXException {
		StringBuffer url = getUrl(sessionId,"updates");
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
		
		return gT(url.toString());
	}
	
	public Response get(String sessionId, int objectId) throws MalformedURLException, JSONException, IOException, SAXException  {
		return get(sessionId,objectId,-1);
	}
	
	public Response get(String sessionId, int objectId, int version) throws MalformedURLException, JSONException, IOException, SAXException {
		StringBuffer url = getUrl(sessionId,"get");
		url.append("&id=");
		url.append(objectId);
		if(version != -1) {
			url.append("&version=");
			url.append(version);
		}
		
		return gT(url.toString());
	}
	
	public Response versions(String sessionId, int folderId, int[] columns) throws MalformedURLException, JSONException, IOException, SAXException {
		return versions(sessionId,folderId,columns,-1,null);
	}
	
	public Response versions(String sessionId, int folderId, int[] columns, int sort, String order) throws MalformedURLException, JSONException, IOException, SAXException {
		StringBuffer url = getUrl(sessionId,"versions");
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
		
		return gT(url.toString());
	}
	
	public Response update(String sessionId, int id, long timestamp, Map<String,String> modified) throws MalformedURLException, IOException, SAXException, JSONException {
		StringBuffer url = getUrl(sessionId,"update");
		url.append("&id=");
		url.append(id);
		
		url.append("&timestamp=");
		url.append(timestamp);
		JSONObject obj = new JSONObject();
		for(String attr : modified.keySet()) {
			obj.put(attr, modified.get(attr));
		}
		
		return putT(url.toString(),obj.toString());
	}
	
	public Response update(String sessionId, int id, long timestamp, Map<String, String> modified, File upload, String contentType) throws MalformedURLException, IOException, SAXException, JSONException {
		StringBuffer url = getUrl(sessionId,"update");
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
		WebResponse resp = getWebConversation().getResource(req);
		JSONObject res = extractFromCallback(resp.getText());
		return Response.parse(res.toString());
	}
	
	public int createNew(String sessionId, Map<String,String> fields) throws MalformedURLException, IOException, SAXException, JSONException  {
		StringBuffer url = getUrl(sessionId,"new");
		JSONObject obj = new JSONObject();
		for(String attr : fields.keySet()) {
			obj.put(attr, fields.get(attr));
		}
		
		PutMethodWebRequest m = new PutMethodWebRequest(url.toString(), new ByteArrayInputStream(obj.toString().getBytes()),"text/javascript");
		
		WebResponse resp = getWebConversation().getResponse(m);
		
		return new Integer(resp.getText());
	}
	
	public int createNew(String sessionId, Map<String, String> fields, File upload, String contentType) throws MalformedURLException, IOException, SAXException, JSONException {
		StringBuffer url = getUrl(sessionId,"new");
		PostMethodWebRequest req = new PostMethodWebRequest(url.toString());
		req.setMimeEncoded(true);
		
		JSONObject obj = new JSONObject();
		for(String attr : fields.keySet()) {
			obj.put(attr, fields.get(attr));
		}
		
		req.setParameter("json",obj.toString());
		
		if(upload != null) {
			req.selectFile("file",upload,contentType);
		}
		
		WebResponse resp = getWebConversation().getResource(req);
		
		String html = resp.getText();
		JSONObject response = extractFromCallback(html);
		if(!"".equals(response.optString("error"))) {
			throw new IOException(response.getString("error"));
		}
		return response.getInt("data");
	}

	public int[] delete(String sessionId, long timestamp, int[][] ids) throws MalformedURLException, JSONException, IOException, SAXException {
		StringBuffer url = getUrl(sessionId,"delete");
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
		
		JSONArray arr = putA(url.toString(), data.toString());
		int[] notDeleted = new int[arr.length()];
		
		for(int i = 0; i < arr.length(); i++) {
			notDeleted[i] = arr.getInt(i);
		}
		
		return notDeleted;
	}
	
	public InputStream document(String sessionId, int id) throws HttpException, IOException {
		return document(sessionId,id,-1);
	}
	
	public InputStream document(String sessionId, int id, int version) throws HttpException, IOException{
		StringBuffer url = getUrl(sessionId,"document");
		url.append("&id="+id);
		if(version!=-1)
			url.append("&version="+version);
		
		HttpClient client = new HttpClient();
		GetMethod get = new GetMethod(url.toString());
		client.executeMethod(get);
		return get.getResponseBodyAsStream();
	}
	
	protected StringBuffer getUrl(String sessionId, String action) {
		StringBuffer url = new StringBuffer("http://");
		url.append(getHostName());
		url.append("/ajax/infostore?session=");
		url.append(sessionId);
		url.append("&action=");
		url.append(action);
		return url;
	}
	
	protected Map<String,String> m(String ...pairs){
		if(pairs.length % 2 != 0)
			throw new IllegalArgumentException("Must contain matching pairs");
		
		Map<String,String> m = new HashMap<String,String>();
		
		for(int i = 0; i < pairs.length; i++) {
			m.put(pairs[i], pairs[++i]);
		}
		
		return m;
		
	}
	
	/*public void m() throws HttpException, IOException{
		HttpClient client = new HttpClient();
		
		PostMethod m = new PostMethod(new URL("http://www.google.com").toString());
		m.addParameter(new NameValuePair("query","openexchange"));
		
		client.executeMethod(m);
		
		m.getResponseBodyAsString();
		
	}*/

	
	
	// Helper
	
	private JSONObject extractFromCallback(String html) throws JSONException {
		Matcher matcher = CALLBACK_ARG_PATTERN.matcher(html);
		if(matcher.find()){
			return new JSONObject(matcher.group(1));
		}
		return null;
	}
	

}
