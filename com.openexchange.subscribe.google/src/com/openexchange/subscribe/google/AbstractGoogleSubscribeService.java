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
 *     Copyright (C) 2004-2014 Open-Xchange, Inc.
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

package com.openexchange.subscribe.google;

import com.openexchange.datatypes.genericonf.DynamicFormDescription;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.oauth.OAuthServiceMetaData;
import com.openexchange.server.ServiceLookup;
import com.openexchange.subscribe.AbstractSubscribeService;
import com.openexchange.subscribe.SubscriptionSource;

/**
 * {@link AbstractGoogleSubscribeService}
 *
 * @author <a href="mailto:ioannis.chouklis@open-xchange.com">Ioannis Chouklis</a>
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public abstract class AbstractGoogleSubscribeService extends AbstractSubscribeService {

    /** The meta data of associated Google OAuth account */
    protected final OAuthServiceMetaData googleMetaData;

    /** The service look-up */
    protected final ServiceLookup services;

    /**
     * Initializes a new {@link AbstractGoogleSubscribeService}.
     *
     * @param googleMetaData The OAuth account's meta data
     * @param services The service look-up
     */
    protected AbstractGoogleSubscribeService(OAuthServiceMetaData googleMetaData, ServiceLookup services) {
        super();
        this.services = services;
        this.googleMetaData = googleMetaData;
    }

    /**
     * Initializes the appropriate subscription source.
     *
     * @param module The associated module
     * @param appendix The identifier appendix
     * @return The subscription source
     */
    protected final SubscriptionSource initSS(int module, String appendix) {
        SubscriptionSource source = new SubscriptionSource();
        source.setDisplayName("Google");
        source.setFolderModule(module);
        source.setId("com.openexchange.subscribe.google." + appendix);
        source.setSubscribeService(this);
        
        final DynamicFormDescription form = new DynamicFormDescription();
        final FormElement oauthAccount = FormElement.custom("oauthAccount", "account", FormStrings.ACCOUNT_LABEL);
        oauthAccount.setOption("type", googleMetaData.getId());
        form.add(oauthAccount);
        
        source.setFormDescription(form);
        return source;
    }

}
