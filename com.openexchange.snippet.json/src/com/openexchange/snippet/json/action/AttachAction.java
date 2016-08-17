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

package com.openexchange.snippet.json.action;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.documentation.RequestMethod;
import com.openexchange.documentation.annotations.Action;
import com.openexchange.documentation.annotations.Parameter;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.upload.UploadFile;
import com.openexchange.groupware.upload.impl.UploadEvent;
import com.openexchange.groupware.upload.impl.UploadException;
import com.openexchange.mail.mime.ContentDisposition;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.osgi.ServiceListing;
import com.openexchange.server.ServiceLookup;
import com.openexchange.snippet.Attachment;
import com.openexchange.snippet.DefaultAttachment;
import com.openexchange.snippet.DefaultAttachment.InputStreamProvider;
import com.openexchange.snippet.DefaultSnippet;
import com.openexchange.snippet.Property;
import com.openexchange.snippet.SnippetService;
import com.openexchange.snippet.json.SnippetRequest;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link AttachAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
@Action(
    name = "attach"
    , description = "Attaches a file attachment to an existing snippet associated with the current user and context."
    , method = RequestMethod.POST
    , parameters = {
        @Parameter(name = "id", description = "The snippet's identifier", optional=false)
    }
)
public final class AttachAction extends SnippetAction {

    private static final class InputStreamProviderImpl implements InputStreamProvider {

        private final UploadFile fileItem;

        protected InputStreamProviderImpl(final UploadFile fileItem) {
            this.fileItem = fileItem;
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new FileInputStream(fileItem.getTmpFile());
        }
    }

    /**
     * Initializes a new {@link AttachAction}.
     */
    public AttachAction(final ServiceLookup services, final ServiceListing<SnippetService> snippetServices, final Map<String, SnippetAction> actions) {
        super(services, snippetServices, actions);
    }

    @Override
    protected AJAXRequestResult perform(final SnippetRequest snippetRequest) throws OXException, JSONException {
        final String id = snippetRequest.checkParameter("id");
        final SnippetService snippetService = getSnippetService(snippetRequest.getSession());

        final DefaultSnippet snippet = new DefaultSnippet().setId(id);
        final AJAXRequestData requestData = snippetRequest.getRequestData();
        long maxSize = sysconfMaxUpload();
        if (!requestData.hasUploads(-1L, maxSize > 0 ? maxSize : -1L)) {
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create("Not an upload request.");
        }
        final UploadEvent upload = requestData.getUploadEvent();
        final String fileTypeFilter = requestData.getParameter(AJAXServlet.PARAMETER_TYPE);
        if (fileTypeFilter == null) {
            throw UploadException.UploadCode.MISSING_PARAM.create(AJAXServlet.PARAMETER_TYPE);
        }
        /*
         * Iterate uploaded files
         */
        final List<UploadFile> uploadFiles = upload.getUploadFiles();
        final List<Attachment> attachments = new ArrayList<Attachment>(uploadFiles.size());
        for (final UploadFile uploadFile : uploadFiles) {
            /*
             * Check file item's content type
             */
            final String sContentType = uploadFile.getContentType();
            final ContentType ct = new ContentType(sContentType);
            if (!checkFileType(fileTypeFilter, ct)) {
                throw UploadException.UploadCode.INVALID_FILE_TYPE.create(sContentType, fileTypeFilter);
            }
            attachments.add(processFileItem(uploadFile));
        }
        /*
         * Update
         */
        final String newId = snippetService.getManagement(snippetRequest.getSession()).updateSnippet(
            id,
            snippet,
            Collections.<Property> emptySet(),
            attachments,
            Collections.<Attachment> emptyList());
        return new AJAXRequestResult(newId, "string");
    }

    private static Attachment processFileItem(final UploadFile fileItem) {
        final DefaultAttachment attachment = new DefaultAttachment();
        attachment.setStreamProvider(new InputStreamProviderImpl(fileItem));
        {
            final String fileName = fileItem.getPreparedFileName();
            if (null != fileName) {
                final ContentDisposition cd = new ContentDisposition();
                cd.setAttachment();
                cd.setFilenameParameter(fileName);
                attachment.setContentDisposition(cd.toString());
            }
        }
        {
            final String sct = fileItem.getContentType();
            if (null != sct) {
                attachment.setContentType(sct);
            }
        }
        final long size = fileItem.getSize();
        if (size > 0) {
            attachment.setSize(size);
        }
        attachment.setId(UUID.randomUUID().toString());
        return attachment;
    }

    private static long sysconfMaxUpload() {
        final String sizeS = ServerConfig.getProperty(com.openexchange.configuration.ServerConfig.Property.MAX_UPLOAD_SIZE);
        if(null == sizeS) {
            return 0;
        }
        return Long.parseLong(sizeS);
    }

    private static final String FILE_TYPE_ALL = "file";

    private static final String FILE_TYPE_TEXT = "text";

    private static final String FILE_TYPE_MEDIA = "media";

    private static final String FILE_TYPE_IMAGE = "image";

    private static final String FILE_TYPE_AUDIO = "audio";

    private static final String FILE_TYPE_VIDEO = "video";

    private static final String FILE_TYPE_APPLICATION = "application";

    private static boolean checkFileType(final String filter, final ContentType fileContentType) {
        if (FILE_TYPE_ALL.equalsIgnoreCase(filter)) {
            return true;
        } else if (FILE_TYPE_TEXT.equalsIgnoreCase(filter)) {
            return fileContentType.startsWith("text/");
        } else if (FILE_TYPE_MEDIA.equalsIgnoreCase(filter)) {
            return fileContentType.startsWith("image/") || fileContentType.startsWith("audio/") || fileContentType.startsWith("video/");
        } else if (FILE_TYPE_IMAGE.equalsIgnoreCase(filter)) {
            return fileContentType.startsWith("image/");
        } else if (FILE_TYPE_AUDIO.equalsIgnoreCase(filter)) {
            return fileContentType.startsWith("audio/");
        } else if (FILE_TYPE_VIDEO.equalsIgnoreCase(filter)) {
            return fileContentType.startsWith("video/");
        } else if (FILE_TYPE_APPLICATION.equalsIgnoreCase(filter)) {
            return fileContentType.startsWith("application/");
        }
        return false;
    }

    @Override
    public String getAction() {
        return "attach";
    }

}
