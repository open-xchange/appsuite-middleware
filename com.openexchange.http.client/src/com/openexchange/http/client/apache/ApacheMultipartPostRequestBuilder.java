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

package com.openexchange.http.client.apache;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
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
import org.apache.commons.httpclient.methods.multipart.StringPart;
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

	@Override
    public HTTPMultipartPostRequestBuilder part(String fieldName, File file) throws OXException {
		try {
			parts.add(new FilePart(fieldName, file));
		} catch (FileNotFoundException e) {
		}
		return this;
	}

	@Override
    public HTTPMultipartPostRequestBuilder part(String fieldName,
			InputStream is, String contentType, String filename) throws OXException {
		parts.add(new FilePart(fieldName, partSource(filename, is), contentType, "UTF-8"));
		return this;
	}


	@Override
    public HTTPMultipartPostRequestBuilder part(String fieldName,
			InputStream is, String contentType) throws OXException {
		parts.add(new FilePart(fieldName, partSource("data.bin", is), contentType, "UTF-8"));
		return this;
	}

	@Override
    public HTTPMultipartPostRequestBuilder part(String fieldName, String s,
			String contentType, String filename) throws OXException {
		try {
			parts.add(new FilePart(fieldName, new ByteArrayPartSource(filename, s.getBytes("UTF-8")), contentType, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
		}
		return this;
	}

	@Override
    public HTTPMultipartPostRequestBuilder part(String fieldName, String s,
			String contentType) throws OXException {
		try {
			parts.add(new FilePart(fieldName, new ByteArrayPartSource("data.txt", s.getBytes("UTF-8")), contentType, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
		}
		return this;
	}

	@Override
    public HTTPMultipartPostRequestBuilder part(String fieldName, InputStream is, String contentType, long length, String filename) throws OXException {
            parts.add(new FilePart(fieldName, partSource(length, filename, is), contentType, "UTF-8"));
        return this;
    }

	@Override
    public HTTPMultipartPostRequestBuilder stringPart(String fieldName, String fieldValue) {
        parts.add(new StringPart(fieldName, fieldValue, "UTF-8"));
        return this;
    }

	@Override
	protected HttpMethodBase createMethod(String encodedSite) {
		PostMethod m = new PostMethod(encodedSite);

		MultipartRequestEntity multipart = new MultipartRequestEntity(parts.toArray(new Part[parts.size()]), m.getParams());
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

	private PartSource partSource(final long size, final String filename, final InputStream is) throws OXException {
	    PartSource source = new PartSource() {

            @Override
            public long getLength() {
                return size;
            }

            @Override
            public String getFileName() {
                return filename;
            }

            @Override
            public InputStream createInputStream() throws IOException {
                return is;
            }
        };
        return source;
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
