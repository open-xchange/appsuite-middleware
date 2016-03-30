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

package com.openexchange.ajax.infostore.actions;

import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.container.Response;
import com.openexchange.ajax.framework.AbstractAJAXParser;

/**
 * {@link ZipDocumentsRequest}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 *
 */
public final class ZipDocumentsRequest extends AbstractInfostoreRequest {

	class ZipDocumentsParser extends AbstractAJAXParser<ZipDocumentsResponse> {

		/**
		 * Default constructor.
		 */
		ZipDocumentsParser(final boolean failOnError) {
			super(failOnError);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected ZipDocumentsResponse createResponse(final Response response) throws JSONException {
			return new ZipDocumentsResponse(response);
		}
	}

	private final List<IdVersionPair> pairs;
	private final String folderId;
	private final boolean failOnError;

	/**
     * Initializes a new {@link ZipDocumentsRequest}.
     */
    public ZipDocumentsRequest(List<IdVersionPair> pairs, String folderId) {
        this(pairs, folderId, true);
    }

	/**
     * Initializes a new {@link ZipDocumentsRequest}.
     */
    public ZipDocumentsRequest(List<IdVersionPair> pairs, String folderId, boolean failOnError) {
        super();
        this.pairs = pairs;
        this.folderId = folderId;
        this.failOnError = failOnError;
    }

	@Override
    public Object getBody() throws JSONException {
	    final JSONArray ja = new JSONArray(pairs.size());
	    for (final IdVersionPair pair : pairs) {
            final JSONObject jo = new JSONObject(3);
            jo.put(AJAXServlet.PARAMETER_FOLDERID, folderId);
            jo.put(AJAXServlet.PARAMETER_ID, pair.getIdentifier());
            final String version = pair.getVersion();
            if (null != version) {
                jo.put("version", version);
            }
            ja.put(jo);
        }
		return ja;
	}

	@Override
    public Method getMethod() {
		return Method.PUT;
	}

	@Override
    public Parameter[] getParameters() {
		return new Parameter[] { new Parameter(AJAXServlet.PARAMETER_ACTION, "zipdocuments"), new Parameter(AJAXServlet.PARAMETER_FOLDERID, folderId)};
	}

	@Override
    public AbstractAJAXParser<?> getParser() {
		return new ZipDocumentsParser(failOnError);
	}

	/**
	 * {@link IdVersionPair} - A pair of an identifier and a version.
	 *
	 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
	 */
	public static final class IdVersionPair {

	    private final String identifier;
	    private final String version;

	    /**
	     * Initializes a new {@link IdVersionPair}.
	     *
	     * @param identifier
	     * @param version
	     */
	    public IdVersionPair(String identifier, String version) {
	        super();
	        this.identifier = identifier;
	        this.version = version;
	    }

	    /**
	     * Gets the identifier
	     *
	     * @return The identifier
	     */
	    public String getIdentifier() {
	        return identifier;
	    }

	    /**
	     * Gets the version
	     *
	     * @return The version or {@link FileStorageFileAccess#CURRENT_VERSION}
	     */
	    public String getVersion() {
	        return version;
	    }
	}

}
