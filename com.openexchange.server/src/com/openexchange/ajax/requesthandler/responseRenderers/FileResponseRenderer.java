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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.ajax.requesthandler.responseRenderers;

import static com.openexchange.java.Streams.close;
import static com.openexchange.java.Strings.isEmpty;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.ajax.AJAXUtility;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.ajax.fileholder.Readable;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.ResponseRenderer;
import com.openexchange.ajax.requesthandler.responseRenderers.Actions.CheckParametersAction;
import com.openexchange.ajax.requesthandler.responseRenderers.Actions.IDataWrapper;
import com.openexchange.ajax.requesthandler.responseRenderers.Actions.IFileResponseRendererAction;
import com.openexchange.ajax.requesthandler.responseRenderers.Actions.OutputBinaryContentAction;
import com.openexchange.ajax.requesthandler.responseRenderers.Actions.PrepareResponseHeaderAction;
import com.openexchange.ajax.requesthandler.responseRenderers.Actions.RemovePragmaHeaderAction;
import com.openexchange.ajax.requesthandler.responseRenderers.Actions.SetBinaryInputStreamAction;
import com.openexchange.ajax.requesthandler.responseRenderers.Actions.TransformImageAction;
import com.openexchange.ajax.requesthandler.responseRenderers.Actions.UpdateETagHeaderAction;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.PropertyEvent;
import com.openexchange.config.PropertyListener;
import com.openexchange.exception.OXException;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.images.ImageTransformationService;
import com.openexchange.tools.servlet.AjaxExceptionCodes;

/**
 * {@link FileResponseRenderer}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class FileResponseRenderer implements ResponseRenderer {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FileResponseRenderer.class);

    private static final String SAVE_AS_TYPE = "application/octet-stream";

    private final AtomicReference<File> tmpDirReference;

    private final IFileResponseRendererAction REGISTERED_ACTIONS[] = { new CheckParametersAction(), new TransformImageAction(), new SetBinaryInputStreamAction(), new PrepareResponseHeaderAction(), new RemovePragmaHeaderAction(), new UpdateETagHeaderAction(), new OutputBinaryContentAction() };

    private final TransformImageAction TRANSFORM_IMAGE_ACTION = (TransformImageAction) REGISTERED_ACTIONS[1];

    /**
     * Initializes a new {@link FileResponseRenderer}.
     */
    public FileResponseRenderer() {
        super();
        final AtomicReference<File> tmpDirReference = new AtomicReference<File>();
        this.tmpDirReference = tmpDirReference;
        final ServerServiceRegistry registry = ServerServiceRegistry.getInstance();
        // Get configuration service
        final ConfigurationService cs = registry.getService(ConfigurationService.class);
        if (null == cs) {
            throw new IllegalStateException("Missing configuration service");
        }
        final String path = cs.getProperty("UPLOAD_DIRECTORY", new PropertyListener() {

            @Override
            public void onPropertyChange(final PropertyEvent event) {
                if (PropertyEvent.Type.CHANGED.equals(event.getType())) {
                    tmpDirReference.set(getTmpDirByPath(event.getValue()));
                }
            }
        });
        tmpDirReference.set(getTmpDirByPath(path));
    }

    @Override
    public int getRanking() {
        return 0;
    }

    /**
     * Sets the image scaler.
     *
     * @param scaler The image scaler
     */
    public void setScaler(ImageTransformationService scaler) {
        TRANSFORM_IMAGE_ACTION.setScaler(scaler);
    }

    @Override
    public boolean handles(AJAXRequestData request, AJAXRequestResult result) {
        return (result.getResultObject() instanceof IFileHolder);
    }

    @Override
    public void write(AJAXRequestData request, AJAXRequestResult result, HttpServletRequest req, HttpServletResponse resp) {
        IFileHolder file = (IFileHolder) result.getResultObject();
        // Check if file is actually supplied by the request URL.
        if (file == null || hasNoFileItem(file)) {
            try {
                // Do your thing if the file is not supplied to the request URL or if there is no file item associated with specified file
                // Throw an exception, or send 404, or show default/warning page, or just ignore it.
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            } catch (final IOException e) {
                LOG.error("Exception while trying to write HTTP response.", e);
            }
            return;
        }
        try {
            writeFileHolder(file, request, result, req, resp);
        } finally {
            postProcessingTasks(file);
        }
    }

    private void postProcessingTasks(IFileHolder file) {
        List<Runnable> tasks = file.getPostProcessingTasks();
        if (null != tasks && !tasks.isEmpty()) {
            for (Runnable task : tasks) {
                task.run();
            }
        }
    }

    /**
     * Writes specified file holder.
     *
     * @param fileHolder The file holder
     * @param requestData The AJAX request data
     * @param result The AJAX response
     * @param req The HTTP request
     * @param resp The HTTP response
     */
    public void writeFileHolder(IFileHolder fileHolder, AJAXRequestData requestData, AJAXRequestResult result, HttpServletRequest req, HttpServletResponse resp) {
        final String fileName = fileHolder.getName();
        final long length = fileHolder.getLength();
        final List<Closeable> closeables = new LinkedList<Closeable>();
        final String fileContentType = fileHolder.getContentType();
        DataWrapper data = new DataWrapper(null, null, null, false, null, length, fileHolder, req, fileContentType, fileName, requestData, resp, null, closeables, result, tmpDirReference);

        try {
            data.setUserAgent(AJAXUtility.sanitizeParam(req.getHeader("user-agent")));
            for (IFileResponseRendererAction action : REGISTERED_ACTIONS) {
                action.call(data);
            }
        } catch (FileResponseRendererActionException ex) {
            //Respond with an error
            return;
        } catch (final OXException e) {
            String message = isEmpty(fileName) ? "Exception while trying to output file" : new StringBuilder("Exception while trying to output file ").append(fileName).toString();
            LOG.error(message, e);
            if (AjaxExceptionCodes.BAD_REQUEST.equals(e)) {
                Throwable cause = e;
                while (cause.getCause() != null) {
                    cause = cause.getCause();
                }
                final String causeMsg = cause.getMessage();
                sendErrorSafe(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null == causeMsg ? message : causeMsg, resp);
            } else {
                sendErrorSafe(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message, resp);
            }
        } catch (final Exception e) {
            String message = isEmpty(fileName) ? "Exception while trying to output file" : new StringBuilder("Exception while trying to output file ").append(fileName).toString();
            LOG.error(message, e);
            sendErrorSafe(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message, resp);
        } finally {
            close(data.getDocumentData(), data.getFile());
            close(closeables);
        }
    }

    public static String getContentTypeByFileName(final String fileName) {
        return null == fileName ? null : MimeType2ExtMap.getContentType(fileName, null);
    }

    private void sendErrorSafe(int sc, String msg, final HttpServletResponse resp) {
        try {
            resp.sendError(sc, msg);
        } catch (final Exception e) {
            // Ignore
        }
    }

    private boolean hasNoFileItem(final IFileHolder file) {
        final String fileMIMEType = file.getContentType();
        return ((isEmpty(fileMIMEType) || SAVE_AS_TYPE.equals(fileMIMEType)) && isEmpty(file.getName()) && (file.getLength() <= 0L));
    }

    /**
     * Gets the appropriate directory to save to.
     *
     * @param path The path as indicated by configuration
     * @return The directory
     */
    static File getTmpDirByPath(final String path) {
        if (null == path) {
            throw new IllegalArgumentException("Path is null. Probably property \"UPLOAD_DIRECTORY\" is not set.");
        }
        final File tmpDir = new File(path);
        if (!tmpDir.exists()) {
            throw new IllegalArgumentException("Directory " + path + " does not exist.");
        }
        if (!tmpDir.isDirectory()) {
            throw new IllegalArgumentException(path + " is not a directory.");
        }
        return tmpDir;
    }

    private class DataWrapper implements IDataWrapper {

        private String delivery = null;
        private String contentType = null;
        private String contentDisposition = null;
        private String userAgent;
        private String fileContentType;
        private String fileName;
        private long length = -1;
        private Boolean contentTypeByParameter = false;
        private Readable documentData = null;
        private IFileHolder file;
        private HttpServletRequest request;
        private HttpServletResponse response;
        private AJAXRequestData requestData;
        private AJAXRequestResult result;
        private List<Closeable> closeables;
        private AtomicReference<File> tmpDirReference;

        public DataWrapper(String delivery, String contentType, String contentDisposition, Boolean contentTypeByParameter, Readable documentData, long length, IFileHolder file, HttpServletRequest req, String fileContentType, String fileName, AJAXRequestData requestData, HttpServletResponse response, String userAgent, List<Closeable> closeables, AJAXRequestResult result, AtomicReference<File> tmpDirReference) {
            this.delivery = delivery;
            this.contentType = contentType;
            this.contentDisposition = contentDisposition;
            this.contentTypeByParameter = contentTypeByParameter;
            this.documentData = documentData;
            this.length = length;
            this.file = file;
            this.request = req;
            this.fileContentType = fileContentType;
            this.fileName = fileName;
            this.requestData = requestData;
            this.response = response;
            this.userAgent = userAgent;
            this.closeables = closeables;
            this.result = result;
            this.tmpDirReference = tmpDirReference;
        }

        @Override
        public String getDelivery() {
            return delivery;
        }

        @Override
        public void setDelivery(String delivery) {
            this.delivery = delivery;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public void setContentType(String contentType) {
            this.contentType = contentType;
        }

        @Override
        public String getContentDisposition() {
            return contentDisposition;
        }

        @Override
        public void setContentDisposition(String contentDisposition) {
            this.contentDisposition = contentDisposition;
        }

        @Override
        public Boolean getContentTypeByParameter() {
            return contentTypeByParameter;
        }

        @Override
        public void setContentTypeByParameter(Boolean contentTypeByParameter) {
            this.contentTypeByParameter = contentTypeByParameter;
        }

        @Override
        public Readable getDocumentData() {
            return documentData;
        }

        @Override
        public void setDocumentData(Readable documentData) {
            this.documentData = documentData;
        }

        @Override
        public long getLength() {
            return length;
        }

        @Override
        public void setLength(long length) {
            this.length = length;
        }

        @Override
        public IFileHolder getFile() {
            return file;
        }

        @Override
        public void setFile(IFileHolder file) {
            this.file = file;
        }

        @Override
        public HttpServletRequest getRequest() {
            return request;
        }

        @Override
        public void setRequest(HttpServletRequest req) {
            this.request = req;
        }

        @Override
        public String getFileContentType() {
            return fileContentType;
        }

        @Override
        public void setFileContentType(String fileContentType) {
            this.fileContentType = fileContentType;
        }

        @Override
        public String getFileName() {
            return fileName;
        }

        @Override
        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        @Override
        public AJAXRequestData getRequestData() {
            return requestData;
        }

        @Override
        public void setRequestData(AJAXRequestData requestData) {
            this.requestData = requestData;
        }

        @Override
        public HttpServletResponse getResponse() {
            return response;
        }

        @Override
        public void setResponse(HttpServletResponse response) {
            this.response = response;
        }

        @Override
        public String getUserAgent() {
            return userAgent;
        }

        @Override
        public void setUserAgent(String userAgent) {
            this.userAgent = userAgent;
        }

        @Override
        public void addCloseable(Closeable closeable) {
            if (closeables == null) {
                closeables = new ArrayList<Closeable>();
            }

            this.closeables.add(closeable);
        }

        @Override
        public List<Closeable> getCloseables() {
            return closeables;
        }

        @Override
        public AJAXRequestResult getResult() {
            return result;
        }

        @Override
        public void setResult(AJAXRequestResult result) {
            this.result = result;
        }

        @Override
        public AtomicReference<File> getTmpDirReference() {
            return tmpDirReference;
        }

        @Override
        public void setTmpDirReference(AtomicReference<File> tmpDirReference) {
            this.tmpDirReference = tmpDirReference;
        }

    }

    public static class FileResponseRendererActionException extends Exception {

        private static final long serialVersionUID = 1654135178706909163L;
    }
}
