package com.openexchange.ajax;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.Map;

import org.apache.commons.httpclient.HttpException;
import org.json.JSONException;
import org.xml.sax.SAXException;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.openexchange.ajax.container.Response;

public class InfostoreClient {
	private InfostoreClient(){}
	
	private static InfostoreAJAXTest test = new InfostoreAJAXTest("InfostoreClient");

	public static Response all(WebConversation webConv, String hostname, String sessionId, int folderId, int[] columns, int sort, String order) throws MalformedURLException, JSONException, IOException, SAXException {
		return test.all(webConv, hostname, sessionId, folderId, columns, sort, order);
	}

	public static Response all(WebConversation webConv, String hostname, String sessionId, int folderId, int[] columns) throws MalformedURLException, JSONException, IOException, SAXException {
		return test.all(webConv, hostname, sessionId, folderId, columns);
	}

	public static int copy(WebConversation webConv, String hostname, String sessionId, int id, long timestamp, Map<String, String> modified, File upload, String contentType) throws JSONException, IOException {
		return test.copy(webConv, hostname, sessionId, id, timestamp, modified, upload, contentType);
	}

	public static int copy(WebConversation webConv, String hostname, String sessionId, int id, long timestamp, Map<String, String> modified) throws MalformedURLException, JSONException, IOException, SAXException {
		return test.copy(webConv, hostname, sessionId, id, timestamp, modified);
	}

	public static int createNew(WebConversation webConv, String hostname, String sessionId, Map<String, String> fields, File upload, String contentType) throws MalformedURLException, IOException, SAXException, JSONException {
		return test.createNew(webConv, hostname, sessionId, fields, upload, contentType);
	}

	public static int createNew(WebConversation webConv, String hostname, String sessionId, Map<String, String> fields) throws MalformedURLException, IOException, SAXException, JSONException {
		return test.createNew(webConv, hostname, sessionId, fields);
	}

	public static int[] delete(WebConversation webConv, String hostname, String sessionId, long timestamp, int[][] ids) throws MalformedURLException, JSONException, IOException, SAXException {
		return test.delete(webConv, hostname, sessionId, timestamp, ids);
	}

	public static int[] detach(WebConversation webConv, String hostname, String sessionId, long timestamp, int objectId, int[] versions) throws MalformedURLException, JSONException, IOException, SAXException {
		return test.detach(webConv, hostname, sessionId, timestamp, objectId, versions);
	}

	public static InputStream document(WebConversation webConv, String hostname, String sessionId, int id, int version, String contentType) throws HttpException, IOException {
		return test.document(webConv, hostname, sessionId, id, version, contentType);
	}

	public static InputStream document(WebConversation webConv, String hostname, String sessionId, int id, int version) throws HttpException, IOException {
		return test.document(webConv, hostname, sessionId, id, version);
	}

	public static InputStream document(WebConversation webConv, String hostname, String sessionId, int id, String contentType) throws HttpException, IOException {
		return test.document(webConv, hostname, sessionId, id, contentType);
	}

	public static InputStream document(WebConversation webConv, String hostname, String sessionId, int id) throws HttpException, IOException {
		return test.document(webConv, hostname, sessionId, id);
	}

	public static GetMethodWebRequest documentRequest(String sessionId, String hostname, int id, int version, String contentType) {
		return test.documentRequest(sessionId, hostname, id, version, contentType);
	}

	public static Response get(WebConversation webConv, String hostname, String sessionId, int objectId, int version) throws MalformedURLException, JSONException, IOException, SAXException {
		return test.get(webConv, hostname, sessionId, objectId, version);
	}

	public static Response get(WebConversation webConv, String hostname, String sessionId, int objectId) throws MalformedURLException, JSONException, IOException, SAXException {
		return test.get(webConv, hostname, sessionId, objectId);
	}

	public static Response list(WebConversation webConv, String hostname, String sessionId, int[] columns, int[][] ids) throws MalformedURLException, JSONException, IOException, SAXException {
		return test.list(webConv, hostname, sessionId, columns, ids);
	}

	public static Response lock(WebConversation webConv, String hostname, String sessionId, int id, long timeDiff) throws MalformedURLException, JSONException, IOException, SAXException {
		return test.lock(webConv, hostname, sessionId, id, timeDiff);
	}

	public static Response lock(WebConversation webConv, String hostname, String sessionId, int id) throws MalformedURLException, JSONException, IOException, SAXException {
		return test.lock(webConv, hostname, sessionId, id);
	}

	public static Response revert(WebConversation webConv, String hostname, String sessionId, long timestamp, int objectId) throws MalformedURLException, JSONException, IOException, SAXException {
		return test.revert(webConv, hostname, sessionId, timestamp, objectId);
	}

	public static int saveAs(WebConversation webConv, String hostname, String sessionId, int folderId, int attached, int module, int attachment, Map<String, String> fields) throws MalformedURLException, IOException, SAXException, JSONException {
		return test.saveAs(webConv, hostname, sessionId, folderId, attached, module, attachment, fields);
	}

	public static Response search(WebConversation webConv, String hostname, String sessionId, String query, int[] columns, int folderId, int sort, String order, int start, int end) throws MalformedURLException, JSONException, IOException, SAXException {
		return test.search(webConv, hostname, sessionId, query, columns, folderId, sort, order, start, end);
	}

	public static Response search(WebConversation webConv, String hostname, String sessionId, String query, int[] columns) throws MalformedURLException, JSONException, IOException, SAXException {
		return test.search(webConv, hostname, sessionId, query, columns);
	}

	public static Response unlock(WebConversation webConv, String hostname, String sessionId, int id) throws MalformedURLException, JSONException, IOException, SAXException {
		return test.unlock(webConv, hostname, sessionId, id);
	}

	public static Response update(WebConversation webConv, String hostname, String sessionId, int id, long timestamp, Map<String, String> modified, File upload, String contentType) throws MalformedURLException, IOException, SAXException, JSONException {
		return test.update(webConv, hostname, sessionId, id, timestamp, modified, upload, contentType);
	}

	public static Response update(WebConversation webConv, String hostname, String sessionId, int id, long timestamp, Map<String, String> modified) throws MalformedURLException, IOException, SAXException, JSONException {
		return test.update(webConv, hostname, sessionId, id, timestamp, modified);
	}

	public static Response updates(WebConversation webConv, String hostname, String sessionId, int folderId, int[] columns, long timestamp, int sort, String order, String ignore) throws MalformedURLException, JSONException, IOException, SAXException {
		return test.updates(webConv, hostname, sessionId, folderId, columns, timestamp, sort, order, ignore);
	}

	public static Response updates(WebConversation webConv, String hostname, String sessionId, int folderId, int[] columns, long timestamp, String ignore) throws MalformedURLException, JSONException, IOException, SAXException {
		return test.updates(webConv, hostname, sessionId, folderId, columns, timestamp, ignore);
	}

	public static Response updates(WebConversation webConv, String hostname, String sessionId, int folderId, int[] columns, long timestamp) throws MalformedURLException, JSONException, IOException, SAXException {
		return test.updates(webConv, hostname, sessionId, folderId, columns, timestamp);
	}

	public static Response versions(WebConversation webConv, String hostname, String sessionId, int objectId, int[] columns, int sort, String order) throws MalformedURLException, JSONException, IOException, SAXException {
		return test.versions(webConv, hostname, sessionId, objectId, columns, sort, order);
	}

	public static Response versions(WebConversation webConv, String hostname, String sessionId, int objectId, int[] columns) throws MalformedURLException, JSONException, IOException, SAXException {
		return test.versions(webConv, hostname, sessionId, objectId, columns);
	}
	
	
}
