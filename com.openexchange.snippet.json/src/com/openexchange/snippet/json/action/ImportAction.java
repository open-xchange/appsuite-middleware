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

import static com.openexchange.java.Strings.toLowerCase;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.configuration.ServerConfig;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.upload.UploadFile;
import com.openexchange.groupware.upload.impl.UploadEvent;
import com.openexchange.html.HtmlService;
import com.openexchange.java.CharsetDetector;
import com.openexchange.java.HTMLDetector;
import com.openexchange.java.Streams;
import com.openexchange.java.Strings;
import com.openexchange.mail.mime.ContentType;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.mail.mime.MimeTypes;
import com.openexchange.mail.usersetting.UserSettingMail;
import com.openexchange.osgi.ServiceListing;
import com.openexchange.server.ServiceExceptionCode;
import com.openexchange.server.ServiceLookup;
import com.openexchange.snippet.DefaultSnippet;
import com.openexchange.snippet.SnippetProcessor;
import com.openexchange.snippet.SnippetService;
import com.openexchange.snippet.SnippetUtils;
import com.openexchange.snippet.json.SnippetRequest;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link ImportAction}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class ImportAction extends SnippetAction {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(ImportAction.class);

    private static final String MIME_APPL_OCTET = MimeTypes.MIME_APPL_OCTET;

    private final List<Method> restMethods;

    /**
     * Initializes a new {@link ImportAction}.
     *
     * @param services The service look-up
     */
    public ImportAction(final ServiceLookup services, final ServiceListing<SnippetService> snippetServices, final Map<String, SnippetAction> actions) {
        super(services, snippetServices, actions);
        restMethods = Collections.singletonList(Method.POST);
    }

    @Override
    protected AJAXRequestResult perform(SnippetRequest snippetRequest) throws OXException, JSONException, IOException {
        // Mandatory parameter
        String displayName = snippetRequest.getRequestData().checkParameter("displayName");

        // Parse & get upload
        UploadEvent uploadEvent;
        {
            long maxSize;
            long maxFileSize;
            UserSettingMail usm = snippetRequest.getSession().getUserSettingMail();
            maxFileSize = usm.getUploadQuotaPerFile();
            if (maxFileSize <= 0) {
                maxFileSize = -1L;
            }
            maxSize = usm.getUploadQuota();
            if (maxSize <= 0) {
                if (maxSize == 0) {
                    maxSize = -1L;
                } else {
                    LOG.debug("Upload quota is less than zero. Using global server property \"MAX_UPLOAD_SIZE\" instead.");
                    long globalQuota;
                    try {
                        globalQuota = ServerConfig.getLong(ServerConfig.Property.MAX_UPLOAD_SIZE).longValue();
                    } catch (OXException e) {
                        LOG.error("", e);
                        globalQuota = 0L;
                    }
                    maxSize = globalQuota <= 0 ? -1L : globalQuota;
                }
            }

            if (false == snippetRequest.getRequestData().hasUploads(maxFileSize, maxSize)) {
                throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
            }

            uploadEvent = snippetRequest.getRequestData().getUploadEvent();
            if (null == uploadEvent) {
                throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
            }
        }

        List<UploadFile> uploadFiles = uploadEvent.getUploadFiles();
        if (null == uploadFiles || uploadFiles.isEmpty()) {
            throw AjaxExceptionCodes.MISSING_REQUEST_BODY.create();
        } else if (uploadFiles.size() != 1) {
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create("Only one file may be uploaded");
        }

        // Check service
        HtmlService htmlService = services.getOptionalService(HtmlService.class);
        if (null == htmlService) {
            throw ServiceExceptionCode.absentService(HtmlService.class);
        }

        // Get the HTML file
        UploadFile htmlFile = uploadFiles.get(0);

        ContentType contentType = new ContentType(htmlFile.getContentType());
        String fileName = htmlFile.getFileName();
        if ((null != fileName) && contentType.startsWith(MIME_APPL_OCTET)) {
            /*
             * Try to determine MIME type
             */
            String ct = MimeType2ExtMap.getContentType(fileName);
            int pos = ct.indexOf('/');
            contentType.setPrimaryType(ct.substring(0, pos));
            contentType.setSubType(ct.substring(pos + 1));
        }

        if (false == Strings.startsWithAny(toLowerCase(contentType.getSubType()), "htm", "xhtm")) {
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create("Only HTML files are supported");
        }

        String cs = contentType.getCharsetParameter();
        if (!CharsetDetector.isValid(cs)) {
            cs = CharsetDetector.detectCharset(new FileInputStream(htmlFile.getTmpFile()));
            if ("US-ASCII".equalsIgnoreCase(cs)) {
                cs = "ISO-8859-1";
            }
        }

        String content = Streams.stream2string(new FileInputStream(htmlFile.getTmpFile()), cs);
        if (com.openexchange.java.Strings.isEmpty(content) || !HTMLDetector.containsHTMLTags(content, true)) {
            throw AjaxExceptionCodes.UNEXPECTED_ERROR.create("No valid HTML content");
        }
        content = SnippetUtils.sanitizeHtmlContent(content, htmlService);

        DefaultSnippet snippet = new DefaultSnippet();
        snippet.setContent(content);
        snippet.setType("signature");
        snippet.setModule("io.ox/mail");
        snippet.setDisplayName(Strings.isEmpty(displayName) ? "My signature" : displayName);
        snippet.setMisc(new JSONObject(3).put("insertion", "below").put("content-type", "text/html"));

        // Process images in an <img> HTML tag and add it as an attachment
        ServerSession session = snippetRequest.getSession();
        new SnippetProcessor(session).processExternalImages(snippet);

        // Create via management
        String id = getSnippetService(session).getManagement(session).createSnippet(snippet);
        return new AJAXRequestResult(new JSONObject(2).put("id", id), "json");
    }

    @Override
    protected AJAXRequestResult performREST(final SnippetRequest snippetRequest, final Method method) throws OXException, JSONException, IOException {
        if (!Method.POST.equals(method)) {
            throw AjaxExceptionCodes.BAD_REQUEST.create();
        }
        /*
         * REST style access
         */
        final AJAXRequestData requestData = snippetRequest.getRequestData();
        final String pathInfo = requestData.getPathInfo();
        if (isEmpty(pathInfo)) {
            requestData.setAction("import");
        } else {
            final String[] pathElements = SPLIT_PATH.split(pathInfo);
            final int length = pathElements.length;
            if (0 < length) {
                throw AjaxExceptionCodes.UNKNOWN_ACTION.create(pathInfo);
            }
            requestData.setAction("import");
        }
        return actions.get(requestData.getAction()).perform(snippetRequest);
    }

    @Override
    public String getAction() {
        return "import";
    }

    @Override
    public List<Method> getRESTMethods() {
        return restMethods;
    }

}
