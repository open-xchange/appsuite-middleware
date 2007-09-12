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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.ajax.importexport.actions;

import java.io.InputStream;

import org.json.JSONException;

import com.openexchange.ajax.AJAXServlet;
import com.openexchange.ajax.framework.AJAXRequest;
import com.openexchange.groupware.importexport.Format;

/**
 * 
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public abstract class AbstractImportRequest implements AJAXRequest {

    /**
     * URL of the tasks AJAX interface.
     */
    public static final String IMPORT_URL = "/ajax/import";

    private final Action action;

    private final int folderId;

    private final InputStream upload;

    /**
     * Default constructor.
     */
    public AbstractImportRequest(final Action action, final int folderId,
        final InputStream upload) {
        super();
        this.action = action;
        this.folderId = folderId;
        this.upload = upload;
    }

    /**
     * {@inheritDoc}
     */
    public Object getBody() throws JSONException {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public Method getMethod() {
        return Method.UPLOAD;
    }

    /**
     * {@inheritDoc}
     */
    public Parameter[] getParameters() {
        return new Parameter[] {
            new Parameter(AJAXServlet.PARAMETER_ACTION, action.name),
            new Parameter(AJAXServlet.PARAMETER_FOLDERID, folderId),
            new FileParameter("file", action.fileName, upload,
                action.format.getMimeType())
        };
    }

    protected enum Action {
        CSV("CSV", "vcard.vcf", Format.CSV);
        private String name;
        private String fileName;
        private Format format;
        private Action(final String name, final String fileName,
            final Format format) {
            this.name = name;
            this.fileName = fileName;
            this.format = format;
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getServletPath() {
        return IMPORT_URL;
    }
}
