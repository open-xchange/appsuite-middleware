package com.openexchange.ajax;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.xml.sax.SAXException;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebConversation;
import com.openexchange.ajax.container.Response;

public class AttachmentClient {
	private static AttachmentTest delegate;

	public static Response all(WebConversation webConv, String sessionId, int folderId, int attachedId, int moduleId, int[] columns, int sort, String order) throws MalformedURLException, JSONException, IOException, SAXException {
		return delegate.all(webConv, sessionId, folderId, attachedId, moduleId, columns, sort, order);
	}

	public static Response attach(WebConversation webConv, String sessionId, int folderId, int attachedId, int moduleId, File upload, String filename, String mimeType) throws JSONException, IOException {
		return delegate.attach(webConv, sessionId, folderId, attachedId, moduleId, upload, filename, mimeType);
	}

	public static Response attach(WebConversation webConv, String sessionId, int folderId, int attachedId, int moduleId, File upload) throws JSONException, IOException {
		return delegate.attach(webConv, sessionId, folderId, attachedId, moduleId, upload);
	}

	public static Response attach(WebConversation webConv, String sessionId, int folderId, int attachedId, int moduleId, List<File> uploads, Map<File, String> filenames, Map<File, String> mimetypes) throws JSONException, IOException {
		return delegate.attach(webConv, sessionId, folderId, attachedId, moduleId, uploads, filenames, mimetypes);
	}

	public static Response attach(WebConversation webConv, String sessionId, int folderId, int attachedId, int moduleId, List<File> uploads) throws JSONException, IOException {
		return delegate.attach(webConv, sessionId, folderId, attachedId, moduleId, uploads);
	}

	public static Response detach(WebConversation webConv, String sessionId, int folderId, int attachedId, int moduleId, int[] ids) throws MalformedURLException, JSONException, IOException, SAXException {
		return delegate.detach(webConv, sessionId, folderId, attachedId, moduleId, ids);
	}

	public static InputStream document(WebConversation webConv, String sessionId, int folderId, int attachedId, int moduleId, int id, String contentType) throws IOException {
		return delegate.document(webConv, sessionId, folderId, attachedId, moduleId, id, contentType);
	}

	public static InputStream document(WebConversation webConv, String sessionId, int folderId, int attachedId, int moduleId, int id) throws IOException {
		return delegate.document(webConv, sessionId, folderId, attachedId, moduleId, id);
	}

	public static GetMethodWebRequest documentRequest(String sessionId, int folderId, int attachedId, int moduleId, int id, String contentType) {
		return delegate.documentRequest(sessionId, folderId, attachedId, moduleId, id, contentType);
	}

	public static Response get(WebConversation webConv, String sessionId, int folderId, int attachedId, int moduleId, int id) throws MalformedURLException, JSONException, IOException, SAXException {
		return delegate.get(webConv, sessionId, folderId, attachedId, moduleId, id);
	}
	
	public Response list(WebConversation webConv, String sessionId, int folderId, int attachedId, int moduleId, int[] ids, int[] columns) throws JSONException, MalformedURLException, IOException, SAXException {
		return delegate.list(webConv, sessionId, folderId, attachedId, moduleId, ids, columns);
	}

	public Response quota(WebConversation webConv, String sessionId) throws MalformedURLException, JSONException, IOException, SAXException {
		return delegate.quota(webConv, sessionId);
	}

	public Response updates(WebConversation webConv, String sessionId, int folderId, int attachedId, int moduleId, long timestamp, int[] columns, int sort, String order) throws MalformedURLException, JSONException, IOException, SAXException {
		return delegate.updates(webConv, sessionId, folderId, attachedId, moduleId, timestamp, columns, sort, order);
	}
}
