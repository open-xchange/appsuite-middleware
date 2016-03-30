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

package com.openexchange.subscribe.json.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.json.JSONArray;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.server.ServiceLookup;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.subscribe.json.SubscriptionSourceJSONWriter;

/**
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class ListSourcesAction extends AbstractSubscribeSourcesAction {

    /**
     * Initializes a new {@link ListSourcesAction}.
     */
    public ListSourcesAction(ServiceLookup services) {
        super(services);
    }

    private static final String[] FIELDS = new String[] { "id", "displayName", "icon", "module", "formDescription" };

    private static final Set<String> IGNOREES = Collections.unmodifiableSet(new HashSet<String>(Arrays.asList("com.openexchange.subscribe.crawler.gmx")));

    @Override
    public AJAXRequestResult perform(SubscribeRequest subscribeRequest) throws OXException {
        final int module = getModule(subscribeRequest.getRequestData().getModule());
        // Retrieve subscription sources
        final List<SubscriptionSource> sources = new ArrayList<SubscriptionSource>(getAvailableSources(subscribeRequest.getServerSession()).getSources(module));
        for (final Iterator<SubscriptionSource> iterator = sources.iterator(); iterator.hasNext();) {
            final SubscriptionSource subscriptionSource = iterator.next();
            if (IGNOREES.contains(subscriptionSource.getId()) || false == subscriptionSource.getSubscribeService().isCreateModifyEnabled()) {
                iterator.remove();
            }
        }
        // Generate appropriate JSON
        final JSONArray json = new SubscriptionSourceJSONWriter(createTranslator(subscribeRequest.getServerSession())).writeJSONArray(sources, FIELDS);
        return new AJAXRequestResult(json, "json");
    }

    protected int getModule(String moduleAsString) {
        if (moduleAsString == null) {
            return -1;
        }
        if (moduleAsString.equals("contacts")) {
            return FolderObject.CONTACT;
        } else if (moduleAsString.equals("calendar")) {
            return FolderObject.CALENDAR;
        } else if (moduleAsString.equals("tasks")) {
            return FolderObject.TASK;
        } else if (moduleAsString.equals("infostore")) {
            return FolderObject.INFOSTORE;
        }
        return -1;
    }
}
