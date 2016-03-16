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

package com.openexchange.subscribe.microformats.datasources;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.httpclient.HttpException;
import com.openexchange.datatypes.genericonf.FormElement;
import com.openexchange.datatypes.genericonf.WidgetSwitcher;
import com.openexchange.exception.OXException;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.external.ExternalSubscriptionSource;
import com.openexchange.subscribe.microformats.OXMFSubscriptionErrorMessage;
import com.openexchange.subscribe.microformats.parser.OXMFForm;


/**
 * {@link HTTPFormSubmittingOXMFDataSource}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class HTTPFormSubmittingOXMFDataSource implements OXMFDataSource {

    private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(HTTPFormSubmittingOXMFDataSource.class);

    @Override
    public Reader getData(Subscription subscription) throws OXException {
        if(!ExternalSubscriptionSource.class.isInstance(subscription.getSource())) {
            throw OXMFSubscriptionErrorMessage.CAN_ONLY_POST_TO_EXTERNAL_SUBSCRIPTION_SOURCES.create();
        }

        Map<String, String> formValues = new HashMap<String, String>(subscription.getSource().getFormDescription().getFormElements().size());

        Map<String, Object> values = subscription.getConfiguration();

        WidgetSwitcher formSwitcher = new FormSwitcher();
        for(FormElement element : subscription.getSource().getFormDescription().getFormElements()) {
            String name = element.getName();
            String stringValue = (String) element.doSwitch(formSwitcher, values.get(name));
            if(stringValue != null) {
                formValues.put(name, stringValue);
            }
        }

        ExternalSubscriptionSource source = (ExternalSubscriptionSource) subscription.getSource();
        String address = ((OXMFForm) source.getFormDescription()).getAction();
        if(address == null) {
            address = source.getExternalAddress();
        }


        try {
            return HTTPToolkit.post(address, formValues);
        } catch (HttpException e) {
            LOG.error("", e);
            throw OXMFSubscriptionErrorMessage.HttpException.create(e.getMessage(), e);
        } catch (IOException e) {
            LOG.error("", e);
            throw OXMFSubscriptionErrorMessage.IOException.create(e.getMessage(), e);
        }

    }


    private static final class FormSwitcher implements WidgetSwitcher {

        @Override
        public Object checkbox(Object... args) {
            if(args[0] != null && args[0] == Boolean.TRUE) {
                return "on";
            }
            return null;
        }

        @Override
        public Object input(Object... args) {
            return args[0];
        }

        @Override
        public Object link(Object... args) {
            return args[0];
        }

        @Override
        public Object password(Object... args) {
            return args[0];
        }

        @Override
        public Object text(Object... args) {
            return args[0];
        }

        @Override
        public Object custom(Object... args) {
            return null;
        }

    }

}
