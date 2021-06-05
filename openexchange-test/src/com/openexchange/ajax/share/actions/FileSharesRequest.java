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
    public static final int[] DEFAULT_COLUMNS = { Field.FOLDER_ID.getNumber(), Field.ID.getNumber(), Field.FILENAME.getNumber(), Field.TITLE.getNumber(), Field.VERSION.getNumber(), Field.CREATED_BY.getNumber(), Field.MODIFIED_BY.getNumber(), Field.OBJECT_PERMISSIONS.getNumber(), 7010
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
        return new Parameter[] { new URLParameter(AJAXServlet.PARAMETER_COLUMNS, columns), new URLParameter(AJAXServlet.PARAMETER_ACTION, "shares")
        };
    }

    @Override
    public FileSharesParser getParser() {
        return new FileSharesParser(columns, failOnError);
    }

}
