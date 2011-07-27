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
 *     Copyright (C) 2004-2011 Open-Xchange, Inc.
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

package com.openexchange.ajax.requesthandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.json.JSONObject;
import com.openexchange.ajax.fields.RequestConstants;
import com.openexchange.ajax.parser.DataParser;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.upload.UploadFile;
import com.openexchange.groupware.upload.impl.UploadEvent;
import com.openexchange.server.services.ServerServiceRegistry;
import com.openexchange.tools.servlet.AjaxExceptionCodes;
import com.openexchange.tools.strings.StringParser;

/**
 * {@link AJAXRequestData} contains the parameters and the payload of the request.
 * 
 * @author <a href="mailto:marcus.klein@open-xchange.com">Marcus Klein</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public class AJAXRequestData {

    public static interface InputStreamProvider {

        /**
         * Gets this provider's input stream.
         * 
         * @return The input stream
         * @throws IOException If an I/O error occurs
         */
        InputStream getInputStream() throws IOException;
    }

    private final Map<String, String> params;

    private final Map<String, String> headers;

    private boolean secure;

    private Object data;

    private String module;
    
    private String action;
    
    private InputStreamProvider uploadStreamProvider;

    private final List<UploadFile> files = new ArrayList<UploadFile>(5);

    private String hostname;

    private String route;

    private UploadEvent uploadEvent;
    
    private String format;

    private AJAXState state;

    /**
     * Initializes a new {@link AJAXRequestData}.
     * 
     * @param json The JSON data
     * @throws OXException If an AJAX error occurs
     */
    public AJAXRequestData(final JSONObject json) throws OXException {
        this();
        data = DataParser.checkJSONObject(json, RequestConstants.DATA);
    }
    
    /**
     * Initializes a new {@link AJAXRequestData}.
     * 
     * @param data The payload to use data
     * @throws OXException If an AJAX error occurs
     */
    public AJAXRequestData(final Object data) throws OXException {
        this();
        this.data = data;
    }

    /**
     * Initializes a new {@link AJAXRequestData}.
     */
    public AJAXRequestData() {
        super();
        params = new LinkedHashMap<String, String>();
        headers = new LinkedHashMap<String, String>();
    }

    /**
     * Puts given name-value-pair into this data's parameters.
     * <p>
     * A <code>null</code> value removes the mapping.
     * 
     * @param name The parameter name
     * @param value The parameter value
     * @throws NullPointerException If name is <code>null</code>
     */
    public void putParameter(final String name, final String value) {
        if (null == name) {
            throw new NullPointerException("name is null");
        }
        if (null == value) {
            params.remove(name);
        } else {
            params.put(name, value);
        }
    }

    /**
     * Gets this request's parameters as a {@link Map map}
     * 
     * @return The parameters as a {@link Map map}
     */
    public Map<String, String> getParameters() {
        return new HashMap<String, String>(params);
    }

    /**
     * Gets the value mapped to given parameter name.
     * 
     * @param name The parameter name
     * @return The value mapped to given parameter name or <code>null</code> if not present
     * @throws NullPointerException If name is <code>null</code>
     */
    public String getParameter(final String name) {
        if (null == name) {
            throw new NullPointerException("name is null");
        }
        return params.get(name);
    }
    
    /**
     * Tries to get a parameter value as parsed as a certain type
     * @param name The parameter name
     * @param coerceTo The type the parameter should be interpreted as
     * @return
     */
    public <T> T getParameter(final String name, final Class<T> coerceTo) {
        return ServerServiceRegistry.getInstance().getService(StringParser.class).parse(getParameter(name), coerceTo);
    }

    /**
     * Gets all available parameter names wrapped by an {@link Iterator iterator}.
     * 
     * @return The {@link Iterator iterator} for available parameter names
     */
    public Iterator<String> getParameterNames() {
        return params.keySet().iterator();
    }

    /**
     * Gets an {@link Iterator iterator} for those parameters not matching given parameter names.
     * 
     * @param nonMatchingParameterNames The non-matching parameter names
     * @return An {@link Iterator iterator} for non-matching parameters
     */
    public Iterator<Entry<String, String>> getNonMatchingParameters(final Collection<String> nonMatchingParameterNames) {
        final Map<String, String> clone = new HashMap<String, String>(params);
        clone.keySet().removeAll(nonMatchingParameterNames);
        return clone.entrySet().iterator();
    }

    /**
     * Gets an {@link Iterator iterator} for those parameters matching given parameter names.
     * 
     * @param matchingParameterNames The matching parameter names
     * @return An {@link Iterator iterator} for matching parameters
     */
    public Iterator<Entry<String, String>> getMatchingParameters(final Collection<String> matchingParameterNames) {
        final Map<String, String> clone = new HashMap<String, String>(params);
        clone.keySet().retainAll(matchingParameterNames);
        return clone.entrySet().iterator();
    }

    /**
     * Gets the data object.
     * 
     * @return The data object or <code>null</code> if not available
     */
    public Object getData() {
        return data;
    }

    /**
     * Sets the data object.
     * 
     * @param data The data object to set
     */
    public void setData(final Object data) {
        this.data = data;
    }

    
    /**
     * Gets the format
     *
     * @return The format
     */
    public String getFormat() {
        return format;
    }
    
    
    /**
     * Sets the format
     *
     * @param format The format to set
     */
    public void setFormat(final String format) {
        this.format = format;
    }
    
    
    /**
     * Whether this request has a secure connection.
     * 
     * @return <code>true</code> if this request has a secure connection; otherwise <code>false</code>
     */
    public boolean isSecure() {
        return secure;
    }

    /**
     * Sets whether this request has a secure connection.
     * 
     * @param secure <code>true</code> if this request has a secure connection; otherwise <code>false</code>
     */
    public void setSecure(final boolean secure) {
        this.secure = secure;
    }

    /**
     * Gets the upload stream. Retrieves the body of the request as binary data as an {@link InputStream}.
     * 
     * @return The upload stream or <code>null</code> if not available
     * @throws IOException If an I/O error occurs
     */
    public InputStream getUploadStream() throws IOException {
        return uploadStreamProvider.getInputStream();
    }

    /**
     * Sets the upload stream provider
     * 
     * @param uploadStream The upload stream provider to set
     */
    public void setUploadStreamProvider(final InputStreamProvider uploadStreamProvider) {
        this.uploadStreamProvider = uploadStreamProvider;
    }

    /**
     * Computes a list of missing parameters from a list of mandatory parameters. Or use {@link #require(String...)} to check for the presence of certain parameters
     * 
     * @param mandatoryParameters The mandatory parameters expected.
     * @return A list of missing parameter names
     */
    public List<String> getMissingParameters(final String... mandatoryParameters) {
        final List<String> missing = new ArrayList<String>(mandatoryParameters.length);
        for (final String paramName : mandatoryParameters) {
            if (!params.containsKey(paramName)) {
                missing.add(paramName);
            }
        }
        return missing;
    }
    
    /**
     * Require a set of mandatory parameters, throw an exception, if a parameter is missing otherwise.
     */
    public void require(final String...mandatoryParameters) throws OXException {
        final List<String> missingParameters = getMissingParameters(mandatoryParameters);
        if(!missingParameters.isEmpty()) {
            throw AjaxExceptionCodes.MISSING_PARAMETER.create( missingParameters.toString());
        }
    }
    
    /**
     * Finds out if a parameter is set
     */
    public boolean isSet(final String paramName) {
        return params.containsKey(paramName);
    }
    
    /**
     * Sets a header value
     */
    public void setHeader(final String header, final String value) {
        headers.put(header, value);
    }

    /**
     * Gets a header value
     */
    public String getHeader(final String header) {
        return headers.get(header);
    }
    
    /**
     * Gets a header value
     */
    public <T> T getHeader(final String header, final Class<T> coerceTo) {
        return ServerServiceRegistry.getInstance().getService(StringParser.class).parse(getHeader(header), coerceTo);
    }
    
    /**
     * Gets the headers
     *
     * @return The headers
     */
    public Map<String, String> getHeaders() {
        return new HashMap<String,String>(headers);
    }
    
    /**
     * Find out whether this request contains an uploaded file. Note that this is only possible via a servlet interface and not via the
     * multiple module.
     * 
     * @return true if one or more files were uploaded, false otherwise.
     */
    public boolean hasUploads() {
        return !files.isEmpty();
    }

    /**
     * Retrieve file uploads.
     * 
     * @return A list of file uploads.
     */
    public List<UploadFile> getFiles() {
        return Collections.unmodifiableList(files);
    }

    /**
     * Retrieve a file with a given form name.
     * 
     * @param name The name of the form field that include the file
     * @return The file, or null if no file field of this name was found
     */
    public UploadFile getFile(final String name) {
        for (final UploadFile file : files) {
            if (file.getFieldName().equals(name)) {
                return file;
            }
        }
        return null;
    }

    public void addFile(final UploadFile file) {
        files.add(file);
    }

    /**
     * Constructs a URL to this server, injecting the hostname and optionally the jvm route.
     * 
     * @param protocol The protocol to use (http or https). If <code>null</code>, defaults to the protocol used for this request.
     * @param path The path on the server. If <code>null</code> no path is inserted
     * @param withRoute Whether to include the jvm route in the server URL or not
     * @param query The query string. If <code>null</code> no query is included
     * @return A string builder with the URL so far, ready for meddling.
     */
    public StringBuilder constructURL(final String protocol, final String path, final boolean withRoute, final String query) {
        final StringBuilder url = new StringBuilder();
        String prot = protocol;
        if (prot == null) {
            prot = isSecure() ? "https://" : "http://";
        }
        url.append(prot);
        if (!prot.endsWith("://")) {
            url.append("://");
        }

        url.append(hostname);
        if (path != null) {
            if (!path.startsWith("/")) {
                url.append('/');
            }
            url.append(path);
        }
        if (withRoute) {
            url.append(";jsessionid=12345.").append(route);
        }
        if (query != null) {
            if (!query.startsWith("?")) {
                url.append('?');
            }
            url.append(query);
        }
        return url;
    }

    /**
     * Gets the host name either fetched from HTTP request or from host name service
     * 
     * @return The host name
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Sets the host name either fetched from HTTP request or from host name service
     * 
     * @param hostname The host name
     */
    public void setHostname(final String hostname) {
        this.hostname = hostname;
    }

    /**
     * Gets the AJP route.
     * 
     * @return The AJP route
     */
    public String getRoute() {
        return route;
    }

    /**
     * Sets the AJP route.
     * 
     * @param route The AJP route
     */
    public void setRoute(final String route) {
        this.route = route;
    }

    /**
     * Sets the associated upload event.
     * 
     * @param upload The upload event
     */
    public void setUploadEvent(final UploadEvent upload) {
        this.uploadEvent = upload;
    }

    /**
     * Gets the associated upload event.
     * 
     * @return The upload event
     */
    public UploadEvent getUploadEvent() {
        return uploadEvent;
    }

    
    public String getModule() {
        return module;
    }

    
    public void setModule(final String module) {
        this.module = module;
    }

    
    public String getAction() {
        return action;
    }

    
    public void setAction(final String action) {
        this.action = action;
    }

    public void setData(final Object object, final String format) {
        setData(object);
        setFormat(format);
    }

    public void setState(final AJAXState state) {
        this.state = state;
    }
    
    public AJAXState getState() {
        return state;
    }
}
