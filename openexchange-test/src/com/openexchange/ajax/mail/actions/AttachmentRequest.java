/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.ajax.mail.actions;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.ParseException;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.Mail;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;
import com.openexchange.java.Strings;

/**
 * {@link AttachmentRequest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class AttachmentRequest extends AbstractMailRequest<AttachmentResponse> {

    class AttachmentParser extends AbstractAJAXParser<AttachmentResponse> {

        private String strBody;
        private byte[] bytesBody;

        /**
         * Default constructor.
         */
        AttachmentParser(final boolean failOnError) {
            super(failOnError);
        }

        @Override
        public String checkResponse(HttpResponse resp, HttpRequest request) throws ParseException, IOException {
            assertEquals("Response code is not okay. (" + resp.getStatusLine().getReasonPhrase() + ")", HttpStatus.SC_OK, resp.getStatusLine().getStatusCode());
            Header contentType = resp.getFirstHeader("Content-Type");
            if (Strings.asciiLowerCase(contentType.getValue()).startsWith("text/")) {
                strBody = EntityUtils.toString(resp.getEntity());
            } else {
                bytesBody = EntityUtils.toByteArray(resp.getEntity());
            }
            return "{}";
        }

        @Override
        public AttachmentResponse parse(String body) throws JSONException {
            if (body.length() == 0) {
                return new AttachmentResponse(new Response());
            }
            return null == strBody ? new AttachmentResponse(bytesBody) : new AttachmentResponse(strBody);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected AttachmentResponse createResponse(final Response response) throws JSONException {
            return new AttachmentResponse(response);
        }
    }

    /**
     * Unique identifier
     */
    private final String[] folderAndIDAndSequenceID;

    private final boolean failOnError;
    private boolean saveToDisk;
    private boolean filter;
    private boolean fromStructure;
    private boolean decrypt;

    public AttachmentRequest(final String folder, final String ID, final String sequenceId) {
        this(new String[] { folder, ID, sequenceId }, true);
    }

    /**
     * Initializes a new {@link AttachmentRequest}
     *
     * @param mailPath
     */
    public AttachmentRequest(final String[] folderAndIDAndSequenceID) {
        this(folderAndIDAndSequenceID, true);
    }

    /**
     * Initializes a new {@link AttachmentRequest}
     *
     * @param mailPath
     * @param failOnError
     */
    public AttachmentRequest(final String[] folderAndIDAndSequenceID, final boolean failOnError) {
        super();
        this.folderAndIDAndSequenceID = folderAndIDAndSequenceID;
        this.failOnError = failOnError;
    }

    /**
     * Sets the <code>fromStructure</code> flag
     *
     * @param fromStructure The <code>fromStructure</code> flag to set
     */
    public AttachmentRequest setFromStructure(boolean fromStructure) {
        this.fromStructure = fromStructure;
        return this;
    }

    /**
     * Sets the <code>decrypt</code> flag in order to decrypt the attachment.
     *
     * @param doDecryption True, to obtain the decrypted attachment, false to obtain the raw attachment.
     */
    public AttachmentRequest setDecrypt(boolean decrypt) {
        this.decrypt= decrypt;
        return this;
    }

    @Override
    public Object getBody() throws JSONException {
        return null;
    }

    @Override
    public Method getMethod() {
        return Method.GET;
    }

    @Override
    public Parameter[] getParameters() {
        List<Parameter> l = new LinkedList<Parameter>();
        l.add(new Parameter(AJAXServlet.PARAMETER_ACTION, Mail.ACTION_MATTACH));
        l.add(new Parameter(AJAXServlet.PARAMETER_FOLDERID, folderAndIDAndSequenceID[0]));
        l.add(new Parameter(AJAXServlet.PARAMETER_ID, folderAndIDAndSequenceID[1]));
        l.add(new Parameter(Mail.PARAMETER_MAILATTCHMENT, folderAndIDAndSequenceID[2]));
        l.add(new Parameter(Mail.PARAMETER_SAVE, String.valueOf(saveToDisk ? 1 : 0)));
        l.add(new Parameter(Mail.PARAMETER_FILTER, filter ? "true" : "false"));
        l.add(new Parameter("from_structure", fromStructure ? "true" : "false"));
        l.add(new Parameter("decrypt", decrypt ? "true" : "false"));
        return l.toArray(new Parameter[0]);
    }

    @Override
    public AttachmentParser getParser() {
        return new AttachmentParser(failOnError);
    }

    /**
     * Gets the saveToDisk
     *
     * @return the saveToDisk
     */
    public boolean isSaveToDisk() {
        return saveToDisk;
    }

    /**
     * Sets the saveToDisk
     *
     * @param saveToDisk the saveToDisk to set
     */
    public void setSaveToDisk(final boolean saveToDisk) {
        this.saveToDisk = saveToDisk;
    }

    /**
     * Gets the filter
     *
     * @return the filter
     */
    public boolean isFilter() {
        return filter;
    }

    /**
     * Sets the filter
     *
     * @param filter the filter to set
     */
    public void setFilter(final boolean filter) {
        this.filter = filter;
    }

}
