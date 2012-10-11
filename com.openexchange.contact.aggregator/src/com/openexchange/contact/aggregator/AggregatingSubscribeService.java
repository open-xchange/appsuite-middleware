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
 *     Copyright (C) 2004-2012 Open-Xchange, Inc.
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

package com.openexchange.contact.aggregator;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;
import com.openexchange.api2.RdbContactSQLImpl;
import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.container.Contact;
import com.openexchange.groupware.container.FolderObject;
import com.openexchange.subscribe.AbstractSubscribeService;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.SubscriptionErrorMessage;
import com.openexchange.subscribe.SubscriptionSource;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link AggregatingSubscribeService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class AggregatingSubscribeService extends AbstractSubscribeService {

    private static final Log LOG = com.openexchange.log.Log.loggerFor(AggregatingSubscribeService.class);
    
    private ContactAggregator aggregator = new ContactAggregator();
    
    private final SubscriptionSource source;
    
    public AggregatingSubscribeService(final ContactAggregator aggregator) {
        super();
        this.aggregator = aggregator;
        source = new SubscriptionSource();
        source.setId("com.openexchange.contact.aggregator");
        source.setDisplayName("OX Contact Aggregator");
        source.setFolderModule(FolderObject.CONTACT);
        
        final DynamicFormDescription form = new DynamicFormDescription();
        
        form.add(FormElement.input("displayName", "Display Name", true, "Contact Aggregator"));
        
        source.setFormDescription(form);
        source.setSubscribeService(this);
    }

    public Collection<?> getContent(final Subscription subscription) throws OXException {
        try {
            return aggregator.aggregate(subscription.getSession(), fastOnly(subscription), loadContacts(subscription.getFolderIdAsInt(), subscription.getSession()));
        } catch (final Throwable t) {
            LOG.error(t.getMessage(), t);
            throw SubscriptionErrorMessage.COMMUNICATION_PROBLEM.create(t);
        }
    }

    private List<Contact> loadContacts(final int folderIdAsInt, final ServerSession session) {
        
        try {
            return new ContactFolderContactSource(folderIdAsInt,new RdbContactSQLImpl(session),null).getContacts(session);
        } catch (final OXException e) {
            return Collections.emptyList();
        } catch (final Exception e) {
            return Collections.emptyList();
        }
    }

    private boolean fastOnly(final Subscription subscription) {
        final Object object = subscription.getConfiguration().get("fast");
        return object != null && (Boolean) object;
    }

    public SubscriptionSource getSubscriptionSource() {
        return source;
    }

    public boolean handles(final int folderModule) {
        return folderModule == FolderObject.CONTACT;
    }
    
    @Override
    public void modifyOutgoing(final Subscription subscription) throws OXException {
        super.modifyOutgoing(subscription);
        subscription.setSource(source);
        subscription.setDisplayName(subscription.getConfiguration().get("displayName").toString());
    }
}
