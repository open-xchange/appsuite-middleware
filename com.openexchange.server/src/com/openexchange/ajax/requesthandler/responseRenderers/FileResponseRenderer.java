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

package com.openexchange.ajax.requesthandler.responseRenderers;

import static com.openexchange.java.Streams.close;
import static com.openexchange.java.Strings.isEmpty;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.httpclient.HttpStatus;
import com.openexchange.ajax.AJAXUtility;
import com.openexchange.ajax.fileholder.IFileHolder;
import com.openexchange.ajax.fileholder.Readable;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.DispatcherListener;
import com.openexchange.ajax.requesthandler.ResponseRenderer;
import com.openexchange.ajax.requesthandler.responseRenderers.actions.CheckParametersAction;
import com.openexchange.ajax.requesthandler.responseRenderers.actions.IDataWrapper;
import com.openexchange.ajax.requesthandler.responseRenderers.actions.IFileResponseRendererAction;
import com.openexchange.ajax.requesthandler.responseRenderers.actions.OutputBinaryContentAction;
import com.openexchange.ajax.requesthandler.responseRenderers.actions.PrepareResponseHeaderAction;
import com.openexchange.ajax.requesthandler.responseRenderers.actions.RemovePragmaHeaderAction;
import com.openexchange.ajax.requesthandler.responseRenderers.actions.SetBinaryInputStreamAction;
import com.openexchange.ajax.requesthandler.responseRenderers.actions.TransformImageAction;
import com.openexchange.ajax.requesthandler.responseRenderers.actions.UpdateETagHeaderAction;
import com.openexchange.config.ConfigurationService;
import com.openexchange.config.PropertyEvent;
import com.openexchange.config.PropertyListener;
import com.openexchange.exception.OXException;
import com.openexchange.imagetransformation.ImageTransformationDeniedIOException;
import com.openexchange.imagetransformation.ImageTransformationService;
import com.openexchange.mail.mime.MimeType2ExtMap;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.servlet.http.Tools;

/**
 * {@link FileResponseRenderer}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class FileResponseRenderer implements ResponseRenderer {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(FileResponseRenderer.class);

    private static final String SAVE_AS_TYPE = "application/octet-stream";

    // -------------------------------------------------------------------------------------------------------------------------------

    private final AtomicReference<File> tmpDirReference;
    private final TransformImageAction imageAction;
    private final List<IFileResponseRendererAction> registeredActions;
    private final List<DispatcherListener> listenerRegistry;

    /**
     * Initializes a new {@link FileResponseRenderer}.
     */
    public FileResponseRenderer() {
        super();

        // Initialize renderer actions
        imageAction = new TransformImageAction();
        registeredActions = new ArrayList<IFileResponseRendererAction>(8);
        registeredActions.add(new CheckParametersAction());
        registeredActions.add(imageAction);
        registeredActions.add(new SetBinaryInputStreamAction());
        registeredActions.add(new PrepareResponseHeaderAction());
        registeredActions.add(new RemovePragmaHeaderAction());
        registeredActions.add(new UpdateETagHeaderAction());
        registeredActions.add(new OutputBinaryContentAction());

        // Initialize rest
        final AtomicReference<File> tmpDirReference = new AtomicReference<File>();
        this.tmpDirReference = tmpDirReference;
        final ServerServiceRegistry registry = ServerServiceRegistry.getInstance();
        // Get configuration service
        final ConfigurationService cs = registry.getService(ConfigurationService.class);
        if (null == cs) {
            throw new IllegalStateException("Missing configuration service");
        }
        String path = cs.getProperty("UPLOAD_DIRECTORY", new PropertyListener() {

            @Override
            public void onPropertyChange(final PropertyEvent event) {
                if (PropertyEvent.Type.CHANGED.equals(event.getType())) {
                    tmpDirReference.set(getTmpDirByPath(event.getValue()));
                }
            }
        });
        tmpDirReference.set(getTmpDirByPath(path));
        this.listenerRegistry = Collections.synchronizedList(new ArrayList<DispatcherListener>());
    }

    public void addDispatcherListener(DispatcherListener listener) {
        this.listenerRegistry.add(listener);
    }

    public void removeDispatcherListener(DispatcherListener listener) {
        this.listenerRegistry.remove(listener);
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
        imageAction.setScaler(scaler);
    }

    @Override
    public boolean handles(AJAXRequestData request, AJAXRequestResult result) {
        return (result.getResultObject() instanceof IFileHolder);
    }

    @Override
    public void write(AJAXRequestData request, AJAXRequestResult result, HttpServletRequest req, HttpServletResponse resp) {
        List<DispatcherListener> registry = this.listenerRegistry;
        try {
            if (!registry.isEmpty()) {
                preProcessListeners(request);
            }
        } catch (OXException e1) {
            LOG.warn("Pre processing of DispatcherListener aborted due to the following exception {}. Skip further processing", e1.getMessage());
            return;
        }

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
        try {
            if (!registry.isEmpty()) {
                postProcessListeners(request, result);
            }
        } catch (OXException e1) {
            LOG.warn("Post processing of DispatcherListener throws an exception {}.", e1.getMessage());
            return;
        }
    }

    private void postProcessListeners(AJAXRequestData request, AJAXRequestResult result) throws OXException {
        for (DispatcherListener dispatcherListener : this.listenerRegistry) {
            if (dispatcherListener.applicable(request)) {
                dispatcherListener.onRequestPerformed(request, result, null);
            }
        }
    }

    private void preProcessListeners(AJAXRequestData request) throws OXException {
        for (DispatcherListener dispatcherListener : this.listenerRegistry) {
            if (dispatcherListener.applicable(request)) {
                dispatcherListener.onRequestInitialized(request);
            }
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
        IDataWrapper data = new DataWrapper().setContentTypeByParameter(false).setLength(length).setFile(fileHolder).setRequest(req).setFileContentType(fileContentType).setFileName(fileName).setRequestData(requestData).setResponse(resp).setCloseAbles(closeables).setResult(result).setTmpDirReference(tmpDirReference);

        try {
            data.setUserAgent(AJAXUtility.sanitizeParam(req.getHeader("user-agent")));
            for (IFileResponseRendererAction action : registeredActions) {
                action.call(data);
            }
        } catch (FileResponseRendererActionException ex) {
            // Respond with an error
            try {
                resp.sendError(ex.statusCode, ex.message == null ? HttpStatus.getStatusText(ex.statusCode) : ex.message);
            } catch (IOException e) {
                LOG.error("", e);
            }
            return;
        } catch (OXException e) {
            String message = isEmpty(fileName) ? "Exception while trying to output file" : new StringBuilder("Exception while trying to output file ").append(fileName).toString();
            LOG.error(message, e);
            if (AjaxExceptionCodes.BAD_REQUEST.equals(e)) {
                Throwable cause = e;
                while (cause.getCause() != null) {
                    cause = cause.getCause();
                }
                final String causeMsg = cause.getMessage();
                sendErrorSafe(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null == causeMsg ? message : causeMsg, resp);
            } else if (AjaxExceptionCodes.HTTP_ERROR.equals(e)) {
                Object[] logArgs = e.getLogArgs();
                Object statusMsg = logArgs.length > 1 ? logArgs[1] : null;
                int sc = ((Integer) logArgs[0]).intValue();
                sendErrorSafe(sc, null == statusMsg ? null : statusMsg.toString(), resp);
                return;
            } else {
                sendErrorSafe(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, message, resp);
            }
        } catch (ImageTransformationDeniedIOException e) {
            // Quit with 406
            String message = isEmpty(fileName) ? "Exception while trying to output image" : new StringBuilder("Exception while trying to output image ").append(fileName).toString();
            LOG.error(message, e);
            sendErrorSafe(HttpServletResponse.SC_NOT_ACCEPTABLE, e.getMessage(), resp);
        } catch (Exception e) {
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
            Tools.sendErrorPage(resp, sc, msg);
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
            if (!tmpDir.mkdirs()) {
                throw new IllegalArgumentException("Directory " + path + " does not exist and cannot be created.");
            }
            LOG.info("Directory " + path + " did not exist, but could be created.");
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
        private Boolean contentTypeByParameter = Boolean.FALSE;
        private Readable documentData = null;
        private IFileHolder file;
        private HttpServletRequest request;
        private HttpServletResponse response;
        private AJAXRequestData requestData;
        private AJAXRequestResult result;
        private List<Closeable> closeables;
        private AtomicReference<File> tmpDirReference;

        /**
         * Initializes a new {@link DataWrapper}.
         */
        DataWrapper() {
            super();
        }

        @Override
        public String getDelivery() {
            return delivery;
        }

        @Override
        public IDataWrapper setDelivery(String delivery) {
            this.delivery = delivery;
            return this;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public IDataWrapper setContentType(String contentType) {
            this.contentType = contentType;
            return this;
        }

        @Override
        public String getContentDisposition() {
            return contentDisposition;
        }

        @Override
        public IDataWrapper setContentDisposition(String contentDisposition) {
            this.contentDisposition = contentDisposition;
            return this;
        }

        @Override
        public Boolean getContentTypeByParameter() {
            return contentTypeByParameter;
        }

        @Override
        public IDataWrapper setContentTypeByParameter(Boolean contentTypeByParameter) {
            this.contentTypeByParameter = contentTypeByParameter;
            return this;
        }

        @Override
        public Readable getDocumentData() {
            return documentData;
        }

        @Override
        public IDataWrapper setDocumentData(Readable documentData) {
            this.documentData = documentData;
            return this;
        }

        @Override
        public long getLength() {
            return length;
        }

        @Override
        public IDataWrapper setLength(long length) {
            this.length = length;
            return this;
        }

        @Override
        public IFileHolder getFile() {
            return file;
        }

        @Override
        public IDataWrapper setFile(IFileHolder file) {
            this.file = file;
            return this;
        }

        @Override
        public HttpServletRequest getRequest() {
            return request;
        }

        @Override
        public IDataWrapper setRequest(HttpServletRequest req) {
            this.request = req;
            return this;
        }

        @Override
        public String getFileContentType() {
            return fileContentType;
        }

        @Override
        public IDataWrapper setFileContentType(String fileContentType) {
            this.fileContentType = fileContentType;
            return this;
        }

        @Override
        public String getFileName() {
            return fileName;
        }

        @Override
        public IDataWrapper setFileName(String fileName) {
            this.fileName = fileName;
            return this;
        }

        @Override
        public AJAXRequestData getRequestData() {
            return requestData;
        }

        @Override
        public IDataWrapper setRequestData(AJAXRequestData requestData) {
            this.requestData = requestData;
            return this;
        }

        @Override
        public HttpServletResponse getResponse() {
            return response;
        }

        @Override
        public IDataWrapper setResponse(HttpServletResponse response) {
            this.response = response;
            return this;
        }

        @Override
        public String getUserAgent() {
            return userAgent;
        }

        @Override
        public IDataWrapper setUserAgent(String userAgent) {
            this.userAgent = userAgent;
            return this;
        }

        @Override
        public void addCloseable(Closeable closeable) {
            if (this.closeables == null) {
                this.closeables = new ArrayList<Closeable>();
            }

            this.closeables.add(closeable);
        }

        @Override
        public IDataWrapper setCloseAbles(java.util.List<Closeable> closeables) {
            if (closeables != null) {
                this.closeables = closeables;
            }
            return this;
        };

        @Override
        public List<Closeable> getCloseables() {
            return closeables;
        }

        @Override
        public AJAXRequestResult getResult() {
            return result;
        }

        @Override
        public IDataWrapper setResult(AJAXRequestResult result) {
            this.result = result;
            return this;
        }

        @Override
        public AtomicReference<File> getTmpDirReference() {
            return tmpDirReference;
        }

        @Override
        public IDataWrapper setTmpDirReference(AtomicReference<File> tmpDirReference) {
            this.tmpDirReference = tmpDirReference;
            return this;
        }
    } // End of class DataWrapper

    /**
     * {@link FileResponseRendererActionException} - The special exception to signal that an appropriate HTTP status has already been
     * applied to {@link HttpServletResponse} instance and control flow is supposed to return.
     */
    public static class FileResponseRendererActionException extends Exception {

        private static final long serialVersionUID = 1654135178706909163L;

        /** The status code to respond with */
        public final int statusCode;

        /** The optional accompanying message */
        public final String message;

        /**
         * Initializes a new {@link FileResponseRendererActionException}.
         *
         * @param statusCode The HTTP status code
         * @param message The optional accompanying message
         */
        public FileResponseRendererActionException(int statusCode, String message) {
            super();
            this.statusCode = statusCode;
            this.message = message;
        }
    } // End of class FileResponseRendererActionException

}
