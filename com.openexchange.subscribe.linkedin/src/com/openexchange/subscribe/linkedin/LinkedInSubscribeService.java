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
 *     Copyright (C) 2004-2010 Open-Xchange, Inc.
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

package com.openexchange.subscribe.linkedin;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.subscribe.AbstractSubscribeService;
import com.openexchange.subscribe.Subscription;
import com.openexchange.exception.OXException;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.subscribe.linkedin.osgi.Activator;


/**
 * {@link LinkedInSubscribeService}
 *
 * @author <a href="mailto:karsten.will@open-xchange.com">Karsten Will</a>
 */
public class LinkedInSubscribeService  extends AbstractSubscribeService {

    private final Activator activator;

    private static final Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(LinkedInSubscribeService.class));

    private final SubscriptionSource source = new SubscriptionSource();

    public LinkedInSubscribeService(Activator activator){
        this.activator = activator;

        source.setDisplayName("LinkedIn");
        source.setFolderModule(FolderObject.CONTACT);
        source.setId("com.openexchange.subscribe.socialplugin.linkedin");
        source.setSubscribeService(this);

        DynamicFormDescription form = new DynamicFormDescription();

        FormElement oauthAccount = FormElement.custom("oauthAccount", "account", FormStrings.ACCOUNT_LABEL);
        oauthAccount.setOption("type", activator.getOAuthServiceMetadata().getId());
        form.add(oauthAccount);

        source.setFormDescription(form);
    }

    public Collection<?> getContent(Subscription subscription) throws OXException {
        return activator.getLinkedInService().getContacts(subscription.getSecret(), subscription.getUserId(), subscription.getContext().getContextId(), (Integer)subscription.getConfiguration().get("account"));
    }

    public SubscriptionSource getSubscriptionSource() {
        return source;
    }

    public boolean handles(int folderModule) {
        return FolderObject.CONTACT == folderModule;
    }

    @Override
    public void modifyIncoming(Subscription subscription) throws OXException {
        super.modifyIncoming(subscription);
        Integer accountId = (Integer) subscription.getConfiguration().get("account");
        if(accountId != null) {
            subscription.getConfiguration().put("account", accountId.toString());
        }
    }

    @Override
    public void modifyOutgoing(Subscription subscription) throws OXException {
        String accountId = (String) subscription.getConfiguration().get("account");
        if (null != accountId){
            Integer accountIdInt = Integer.parseInt(accountId);
            if (null != accountIdInt) {
                subscription.getConfiguration().put("account",accountIdInt);
            }
            String displayName = null;
            if(subscription.getSecret() != null) {
                displayName = activator.getLinkedInService().getAccountDisplayName(subscription.getSecret(), subscription.getUserId(), subscription.getContext().getContextId(), (Integer)subscription.getConfiguration().get("account"));
            }
            if (null != displayName && !"".equals(displayName)){
                subscription.setDisplayName(displayName);
            } else {
                subscription.setDisplayName("LinkedIn");
            }

        }
        super.modifyOutgoing(subscription);
    }

    public void deleteAllUsingOAuthAccount(Context context, int id) throws OXException {
        Map<String, Object> query = new HashMap<String, Object>();
        query.put("account", String.valueOf(id));
        removeWhereConfigMatches(context, query);
    }
}
