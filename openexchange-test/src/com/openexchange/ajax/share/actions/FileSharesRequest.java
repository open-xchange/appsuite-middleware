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

package com.openexchange.ajax.share.actions;

import java.io.IOException;
import org.json.JSONException;
import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.infostore.actions.AbstractInfostoreRequest;
import com.openexchange.file.storage.File.Field;

/**
 * {@link FileSharesRequest}
 *
 * @author <a href="mailto:tobias.friedrich@open-xchange.com">Tobias Friedrich</a>
 */
public class FileSharesRequest extends AbstractInfostoreRequest<FileSharesResponse> {

    /**
     * The default columns for the <code>shares</code> action
     */
    public static final int[] DEFAULT_COLUMNS = {
        Field.FOLDER_ID.getNumber(), Field.ID.getNumber(), Field.FILENAME.getNumber(), Field.TITLE.getNumber(),
        Field.VERSION.getNumber(), Field.CREATED_BY.getNumber(), Field.MODIFIED_BY.getNumber(),
        Field.OBJECT_PERMISSIONS.getNumber(), 7010
    };

    private final int[] columns;
    private final boolean failOnError;

    /**
     * Initializes a new {@link FileSharesRequest}.
     *
     * @param columns The columns which shall be available in returned folder objects
     * @param failOnError <code>true</code> to fail on errors, <code>false</code>, otherwise
     */
    public FileSharesRequest(int[] columns, boolean failOnError) {
        super();
        this.columns = columns;
        this.failOnError = failOnError;
    }

    /**
     * Initializes a new {@link FileSharesRequest}.
     *
     * @param columns The columns which shall be available in returned folder objects
     */
    public FileSharesRequest(int[] columns) {
        this(columns, true);
    }

    /**
     * Initializes a new {@link FileSharesRequest} with default columns.
     */
    public FileSharesRequest() {
        this(DEFAULT_COLUMNS);
    }

    @Override
    public Object getBody() {
        return null;
    }

    @Override
    public Method getMethod() {
        return Method.GET;
    }

    @Override
    public Parameter[] getParameters() throws IOException, JSONException {
        return new Parameter[] {
            new URLParameter(AJAXServlet.PARAMETER_COLUMNS, columns),
            new URLParameter(AJAXServlet.PARAMETER_ACTION, "shares")
        };
    }

    @Override
    public FileSharesParser getParser() {
        return new FileSharesParser(columns, failOnError);
    }

}
