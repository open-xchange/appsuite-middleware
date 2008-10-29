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

package com.openexchange.ajax.group;

import java.io.IOException;

import org.json.JSONException;
import org.xml.sax.SAXException;

import com.openexchange.ajax.framework.AJAXClient;
import com.openexchange.ajax.framework.Executor;
import com.openexchange.ajax.group.actions.ChangeRequest;
import com.openexchange.ajax.group.actions.ChangeResponse;
import com.openexchange.ajax.group.actions.CreateRequest;
import com.openexchange.ajax.group.actions.CreateResponse;
import com.openexchange.ajax.group.actions.DeleteRequest;
import com.openexchange.ajax.group.actions.DeleteResponse;
import com.openexchange.ajax.group.actions.GetRequest;
import com.openexchange.ajax.group.actions.GetResponse;
import com.openexchange.ajax.group.actions.ListRequest;
import com.openexchange.ajax.group.actions.ListResponse;
import com.openexchange.ajax.group.actions.SearchRequest;
import com.openexchange.ajax.group.actions.SearchResponse;
import com.openexchange.tools.servlet.AjaxException;

/**
 *
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public final class GroupTools {

    /**
     * Prevent instantiation.
     */
    private GroupTools() {
        super();
    }

    public static final GetResponse get(final AJAXClient client,
        final GetRequest request) throws AjaxException, IOException,
        SAXException, JSONException {
        return (GetResponse) Executor.execute(client, request);
    }

    public static SearchResponse search(final AJAXClient client,
        final SearchRequest request) throws AjaxException, IOException,
        SAXException, JSONException {
        return (SearchResponse) Executor.execute(client, request);
    }

    public static ListResponse list(final AJAXClient client,
        final ListRequest request) throws AjaxException, IOException,
        SAXException, JSONException {
        return Executor.execute(client, request);
    }

    public static CreateResponse create(final AJAXClient client,
        final CreateRequest request) throws AjaxException, IOException,
        SAXException, JSONException {
        return (CreateResponse) Executor.execute(client, request);
    }

    public static DeleteResponse delete(final AJAXClient client,
        final DeleteRequest request) throws AjaxException, IOException,
        SAXException, JSONException {
        return (DeleteResponse) Executor.execute(client, request);
    }

    public static ChangeResponse change(final AJAXClient client,
        final ChangeRequest request) throws AjaxException, IOException,
        SAXException, JSONException {
        return (ChangeResponse) Executor.execute(client, request);
    }
}
