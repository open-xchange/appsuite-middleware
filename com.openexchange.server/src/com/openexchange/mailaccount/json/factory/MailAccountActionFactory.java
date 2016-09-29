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
 *    trademarks of the OX Software GmbH. group of companies.
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

package com.openexchange.mailaccount.json.factory;

import java.util.Collection;
import java.util.Map;
import com.google.common.collect.ImmutableMap;
import com.openexchange.ajax.requesthandler.AJAXActionService;
import com.openexchange.ajax.requesthandler.AJAXActionServiceFactory;
import com.openexchange.exception.OXException;
import com.openexchange.mailaccount.Constants;
import com.openexchange.mailaccount.json.ActiveProviderDetector;
import com.openexchange.mailaccount.json.actions.AllAction;
import com.openexchange.mailaccount.json.actions.DeleteAction;
import com.openexchange.mailaccount.json.actions.GetAction;
import com.openexchange.mailaccount.json.actions.GetTreeAction;
import com.openexchange.mailaccount.json.actions.ListAction;
import com.openexchange.mailaccount.json.actions.NewAction;
import com.openexchange.mailaccount.json.actions.StatusAction;
import com.openexchange.mailaccount.json.actions.UpdateAction;
import com.openexchange.mailaccount.json.actions.ValidateAction;
import com.openexchange.oauth.provider.resourceserver.annotations.OAuthModule;
import com.openexchange.tools.servlet.AjaxExceptionCodes;


/**
 * {@link MailAccountActionFactory}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 * @since v7.8.2
 */
@OAuthModule
public class MailAccountActionFactory implements AJAXActionServiceFactory {

    private final Map<String, AJAXActionService> actions;

    /**
     * Initializes a new {@link MailAccountActionFactory}.
     */
    public MailAccountActionFactory(ActiveProviderDetector activeProviderDetector) {
        super();
        ImmutableMap.Builder<String, AJAXActionService> tmp = ImmutableMap.builder();
        tmp.put(AllAction.ACTION, new AllAction(activeProviderDetector));
        tmp.put(ListAction.ACTION, new ListAction(activeProviderDetector));
        tmp.put(GetAction.ACTION, new GetAction(activeProviderDetector));
        tmp.put(ValidateAction.ACTION, new ValidateAction(activeProviderDetector));
        tmp.put(DeleteAction.ACTION, new DeleteAction(activeProviderDetector));
        tmp.put(UpdateAction.ACTION, new UpdateAction(activeProviderDetector));
        tmp.put(GetTreeAction.ACTION, new GetTreeAction(activeProviderDetector));
        tmp.put(NewAction.ACTION, new NewAction(activeProviderDetector));
        tmp.put(StatusAction.ACTION, new StatusAction(activeProviderDetector));
        actions = tmp.build();
    }

    @Override
    public AJAXActionService createActionService(String action) throws OXException {
        AJAXActionService actionService = actions.get(action);
        if (null == actionService) {
            throw AjaxExceptionCodes.UNKNOWN_ACTION_IN_MODULE.create(action, Constants.getModule());
        }
        return actionService;
    }

    @Override
    public Collection<?> getSupportedServices() {
        return actions.values();
    }

}
