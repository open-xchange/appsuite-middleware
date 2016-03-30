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

package com.openexchange.publish.json;

import static com.openexchange.publish.json.MultipleHandlerTools.wrapThrowable;
import static com.openexchange.publish.json.PublicationJSONErrorMessage.MISSING_PARAMETER;
import static com.openexchange.publish.json.PublicationJSONErrorMessage.UNKNOWN_ACTION;
import static com.openexchange.publish.json.PublicationJSONErrorMessage.UNKNOWN_TARGET;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONValue;
import com.openexchange.exception.OXException;
import com.openexchange.i18n.I18nService;
import com.openexchange.i18n.I18nTranslator;
import com.openexchange.i18n.Translator;
import com.openexchange.multiple.MultipleHandler;
import com.openexchange.publish.PublicationTarget;
import com.openexchange.publish.PublicationTargetDiscoveryService;
import com.openexchange.tools.session.ServerSession;

/**
 * {@link PublicationTargetMultipleHandler}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class PublicationTargetMultipleHandler implements MultipleHandler {

    static final Set<String> ACTIONS_REQUIRING_BODY = Collections.emptySet();

    private PublicationTargetDiscoveryService discoverer = null;

    public PublicationTargetMultipleHandler(final PublicationTargetDiscoveryService discoverer) {
        super();
        this.discoverer = discoverer;
    }

    @Override
    public void close() {
    }

    @Override
    public Date getTimestamp() {
        return null;
    }

    @Override
    public JSONValue performRequest(final String action, final JSONObject request, final ServerSession session, final boolean secure) throws JSONException, OXException {
        try {
            if (null == action) {
                throw MISSING_PARAMETER.create("action");
            } else if (action.equals("listTargets") || action.equals("all")) {
                return listTargets(request, session);
            } else if (action.equals("getTarget") || action.equals("get")) {
                return getTarget(request, session);
            } else {
                throw UNKNOWN_ACTION.create(action);
            }
        } catch (final OXException x) {
            throw x;
        } catch (final Throwable t) {
            throw wrapThrowable(t);
        }
    }

    @Override
    public Collection<OXException> getWarnings() {
        return Collections.<OXException> emptySet();
    }

    private JSONValue getTarget(final JSONObject request, final ServerSession session) throws OXException, OXException, JSONException {
        final String identifier = request.optString("id");
        if (identifier == null) {
            throw MISSING_PARAMETER.create("id");
        }
        final PublicationTarget target = discoverer.getTarget(identifier);
        if(target == null) {
            throw UNKNOWN_TARGET.create(identifier);
        }
        final JSONObject data = new PublicationTargetWriter(createTranslator(session)).write(target, session.getUser(), session.getUserPermissionBits());
        return data;
    }

    private Translator createTranslator(final ServerSession session) {
        final Locale locale = session.getUser().getLocale();
        final I18nService service = I18n.getInstance().get(locale);
        return null == service ? Translator.EMPTY : new I18nTranslator(service);
    }

    private JSONValue listTargets(final JSONObject request, final ServerSession session) throws JSONException, OXException, OXException {
        final Collection<PublicationTarget> targets = discoverer.listTargets();
        if (null != targets && 0 < targets.size()) {
            for (Iterator<PublicationTarget> iterator = targets.iterator(); iterator.hasNext();) {
                if (false == iterator.next().getPublicationService().isCreateModifyEnabled()) {
                    iterator.remove();
                }
            }
        }
        final String[] columns = getColumns(request);
        final JSONArray json = new PublicationTargetWriter(createTranslator(session)).writeJSONArray(targets, columns, session.getUser(), session.getUserPermissionBits());
        return json;
    }

    private String[] getColumns(final JSONObject req) {
        final String columns = req.optString("columns");
        if (columns == null) {
            return new String[] { "id", "displayName", "module", "icon", "formDescription" };
        }
        return columns.split("\\s*,\\s*");
    }
}
