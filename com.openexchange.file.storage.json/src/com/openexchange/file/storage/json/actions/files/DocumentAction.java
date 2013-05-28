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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.file.storage.json.actions.files;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import com.openexchange.ajax.container.FileHolder;
import com.openexchange.ajax.container.IFileHolder;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.DispatcherNotes;
import com.openexchange.ajax.requesthandler.ETagAwareAJAXActionService;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.file.storage.File;
import com.openexchange.file.storage.FileStorageUtility;
import com.openexchange.file.storage.composition.IDBasedFileAccess;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link DocumentAction}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
@Action(method = RequestMethod.GET, defaultFormat = "file", name = "[filename]?action=document", description = "Get an infoitem document", parameters = {
    @Parameter(name = "session", description = "A session ID previously obtained from the login module."),
    @Parameter(name = "id", description = "Object ID of the requested infoitem."),
    @Parameter(name = "folder", description = "Object ID of the infoitem's folder."),
    @Parameter(name = "version", optional=true, description = "If present the infoitem data describes the given version. Otherwise the current version is returned"),
    @Parameter(name = "content_type", optional=true, description = "If present the response declares the given content_type in the Content-Type header.")
}, responseDescription = "The raw byte data of the document. The response type for the HTTP Request is set accordingly to the defined mimetype for this infoitem or the content_type given.")
@DispatcherNotes(defaultFormat = "file", allowPublicSession = true)
public class DocumentAction extends AbstractFileAction implements ETagAwareAJAXActionService{
    @Override
    public AJAXRequestResult handle(final InfostoreRequest request) throws OXException {
        request.require(Param.ID);

        final IDBasedFileAccess fileAccess = request.getFileAccess();
        final String id = request.getId();
        final String version = request.getVersion();
        final File fileMetadata = fileAccess.getFileMetadata(id, version);

        final IFileHolder.InputStreamClosure isClosure = new IFileHolder.InputStreamClosure() {

            @Override
            public InputStream newStream() throws OXException, IOException {
                final InputStream inputStream = fileAccess.getDocument(id, version);
                if ((inputStream instanceof BufferedInputStream) || (inputStream instanceof ByteArrayInputStream)) {
                    return inputStream;
                }
                return new BufferedInputStream(inputStream);
            }
        };

        final FileHolder fileHolder = new FileHolder(isClosure, fileMetadata.getFileSize(), fileMetadata.getFileMIMEType(), fileMetadata.getFileName());

        AJAXRequestResult result = new AJAXRequestResult(fileHolder, "file");
        createAndSetETag(fileMetadata, request, result);

        return result;
    }

	private void createAndSetETag(File fileMetadata, InfostoreRequest request, AJAXRequestResult result) throws OXException {
		setETag(FileStorageUtility.getETagFor(fileMetadata), 0, result);
	}

	@Override
	public boolean checkETag(String clientETag, AJAXRequestData requestData,
			ServerSession session) throws OXException {
		final AJAXInfostoreRequest request = new AJAXInfostoreRequest(requestData, session);
		final IDBasedFileAccess fileAccess = request.getFileAccess();
	    final File fileMetadata = fileAccess.getFileMetadata(request.getId(), request.getVersion());
		return FileStorageUtility.getETagFor(fileMetadata).equals(clientETag);
	}

	@Override
	public void setETag(String eTag, long expires, AJAXRequestResult result)
			throws OXException {
		 result.setExpires(expires);
		 if (eTag != null) {
		     result.setHeader("ETag", eTag);
		 }
	}
}
