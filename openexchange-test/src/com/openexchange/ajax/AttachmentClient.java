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
	private static AttachmentTest delegate = new AttachmentTest("");

	public static Response all(final WebConversation webConv, final String sessionId, final int folderId, final int attachedId, final int moduleId, final int[] columns, final int sort, final String order) throws MalformedURLException, JSONException, IOException, SAXException {
		return delegate.all(webConv, sessionId, folderId, attachedId, moduleId, columns, sort, order);
	}

	public static Response attach(final WebConversation webConv, final String sessionId, final int folderId, final int attachedId, final int moduleId, final File upload, final String filename, final String mimeType) throws JSONException, IOException {
		return delegate.attach(webConv, sessionId, folderId, attachedId, moduleId, upload, filename, mimeType);
	}

	public static Response attach(final WebConversation webConv, final String sessionId, final int folderId, final int attachedId, final int moduleId, final File upload) throws JSONException, IOException {
		return delegate.attach(webConv, sessionId, folderId, attachedId, moduleId, upload);
	}

	public static Response attach(final WebConversation webConv, final String sessionId, final int folderId, final int attachedId, final int moduleId, final List<File> uploads, final Map<File, String> filenames, final Map<File, String> mimetypes) throws JSONException, IOException {
		return delegate.attach(webConv, sessionId, folderId, attachedId, moduleId, uploads, filenames, mimetypes);
	}

	public static Response attach(final WebConversation webConv, final String sessionId, final int folderId, final int attachedId, final int moduleId, final List<File> uploads) throws JSONException, IOException {
		return delegate.attach(webConv, sessionId, folderId, attachedId, moduleId, uploads);
	}

	public static Response detach(final WebConversation webConv, final String sessionId, final int folderId, final int attachedId, final int moduleId, final int[] ids) throws MalformedURLException, JSONException, IOException, SAXException {
		return delegate.detach(webConv, sessionId, folderId, attachedId, moduleId, ids);
	}

	public static InputStream document(final WebConversation webConv, final String sessionId, final int folderId, final int attachedId, final int moduleId, final int id, final String contentType) throws IOException {
		return delegate.document(webConv, sessionId, folderId, attachedId, moduleId, id, contentType);
	}

	public static InputStream document(final WebConversation webConv, final String sessionId, final int folderId, final int attachedId, final int moduleId, final int id) throws IOException {
		return delegate.document(webConv, sessionId, folderId, attachedId, moduleId, id);
	}

	public static GetMethodWebRequest documentRequest(final String sessionId, final int folderId, final int attachedId, final int moduleId, final int id, final String contentType) {
		return delegate.documentRequest(sessionId, folderId, attachedId, moduleId, id, contentType);
	}

	public static Response get(final WebConversation webConv, final String sessionId, final int folderId, final int attachedId, final int moduleId, final int id) throws MalformedURLException, JSONException, IOException, SAXException {
		return delegate.get(webConv, sessionId, folderId, attachedId, moduleId, id);
	}

	public Response list(final WebConversation webConv, final String sessionId, final int folderId, final int attachedId, final int moduleId, final int[] ids, final int[] columns) throws JSONException, MalformedURLException, IOException, SAXException {
		return delegate.list(webConv, sessionId, folderId, attachedId, moduleId, ids, columns);
	}

	public Response quota(final WebConversation webConv, final String sessionId) throws MalformedURLException, JSONException, IOException, SAXException {
		return delegate.quota(webConv, sessionId);
	}

	public Response updates(final WebConversation webConv, final String sessionId, final int folderId, final int attachedId, final int moduleId, final long timestamp, final int[] columns, final int sort, final String order) throws MalformedURLException, JSONException, IOException, SAXException {
		return delegate.updates(webConv, sessionId, folderId, attachedId, moduleId, timestamp, columns, sort, order);
	}
}
