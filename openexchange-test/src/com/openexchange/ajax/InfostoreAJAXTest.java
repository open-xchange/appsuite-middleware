package com.openexchange.ajax;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import com.meterware.httpunit.PutMethodWebRequest;
import com.meterware.httpunit.WebResponse;
import com.openexchange.ajax.types.Response;



public abstract class InfostoreAJAXTest extends AbstractAJAXTest {

	public static final String INFOSTORE_FOLDER = "infostore.folder";
	
	protected int folderId;
	
	protected String sessionId;
	
	protected List<Integer> clean = new ArrayList<Integer>();
	
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
			toDelete[i][0] = -1; // FIXME: Put a correct folderId here
			toDelete[i][1] = clean.get(i);
		}
		
		
		if(delete(sessionId,System.currentTimeMillis(),(int[][])toDelete).length != 0)
			throw new IllegalStateException("Someone modified the objects!");
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
	
	public void update(String sessionId, int id, long timestamp, Map<String,String> modified) throws MalformedURLException, IOException, SAXException, JSONException {
		StringBuffer url = getUrl(sessionId,"update");
		url.append("&id=");
		url.append(id);
		
		url.append("&timestamp=");
		url.append(timestamp);
		
		JSONObject obj = new JSONObject();
		for(String attr : modified.keySet()) {
			obj.put(attr, modified.get(attr));
		}
		
		putN(url.toString(),obj.toString());
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

}
