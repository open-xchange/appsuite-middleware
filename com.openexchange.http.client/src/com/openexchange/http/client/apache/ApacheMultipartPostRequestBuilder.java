package com.openexchange.http.client.apache;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.ByteArrayPartSource;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.FilePartSource;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.commons.httpclient.methods.multipart.PartSource;

import com.openexchange.exception.OXException;
import com.openexchange.filemanagement.ManagedFile;
import com.openexchange.filemanagement.ManagedFileManagement;
import com.openexchange.http.client.builder.HTTPMultipartPostRequestBuilder;
import com.openexchange.http.client.exceptions.OxHttpClientExceptionCodes;

public class ApacheMultipartPostRequestBuilder extends CommonApacheHTTPRequest<HTTPMultipartPostRequestBuilder> implements
		HTTPMultipartPostRequestBuilder {

	private ManagedFileManagement fileManager;
	private List<Part> parts = new ArrayList<Part>();
	
	private List<ManagedFile> managedFiles = new ArrayList<ManagedFile>();
	
	public ApacheMultipartPostRequestBuilder(
			ApacheClientRequestBuilder coreBuilder, ManagedFileManagement fileManager) {
		super(coreBuilder);
		this.fileManager = fileManager;
		
	}

	public HTTPMultipartPostRequestBuilder part(String fieldName, File file) throws OXException {
		try {
			parts.add(new FilePart(fieldName, file));
		} catch (FileNotFoundException e) {
		}
		return this;
	}

	public HTTPMultipartPostRequestBuilder part(String fieldName,
			InputStream is, String contentType, String filename) throws OXException {
		parts.add(new FilePart(fieldName, partSource(filename, is), contentType, "UTF-8"));
		return this;
	}


	public HTTPMultipartPostRequestBuilder part(String fieldName,
			InputStream is, String contentType) throws OXException {
		parts.add(new FilePart(fieldName, partSource("data.bin", is), contentType, "UTF-8"));
		return this;
	}

	public HTTPMultipartPostRequestBuilder part(String fieldName, String s,
			String contentType, String filename) throws OXException {
		try {
			parts.add(new FilePart(fieldName, new ByteArrayPartSource(filename, s.getBytes("UTF-8")), contentType, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
		}
		return this;
	}

	public HTTPMultipartPostRequestBuilder part(String fieldName, String s,
			String contentType) throws OXException {
		try {
			parts.add(new FilePart(fieldName, new ByteArrayPartSource("data.txt", s.getBytes("UTF-8")), contentType, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
		}
		return this;
	}

	@Override
	protected HttpMethodBase createMethod(String encodedSite) {
		PostMethod m = new PostMethod(encodedSite);
		
		MultipartRequestEntity multipart = new MultipartRequestEntity((Part[]) parts.toArray(new Part[parts.size()]), m.getParams());
		m.setRequestEntity( multipart );
		
		return m;
	}
	
	private PartSource partSource(String filename, InputStream is) throws OXException {
		try {
			ManagedFile managedFile = fileManager.createManagedFile(is);
			managedFiles.add(managedFile);
			return new FilePartSource(filename, managedFile.getFile());
		} catch (FileNotFoundException e) {
			throw OxHttpClientExceptionCodes.APACHE_CLIENT_ERROR.create(e.getMessage(), e);
		}
	}
	
	@Override
	public void done() {
		for (ManagedFile managedFile : managedFiles) {
			managedFile.delete();
		}
		managedFiles.clear();
		super.done();
	}


}
