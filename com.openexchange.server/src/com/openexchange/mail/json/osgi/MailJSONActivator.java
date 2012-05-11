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

package com.openexchange.mail.json.osgi;

import javax.mail.internet.InternetAddress;
import org.apache.commons.logging.Log;
import org.json.JSONObject;
import com.openexchange.ajax.requesthandler.AJAXRequestData;
import com.openexchange.ajax.requesthandler.AJAXRequestResult;
import com.openexchange.ajax.requesthandler.AJAXResultDecorator;
import com.openexchange.ajax.requesthandler.ResultConverter;
import com.openexchange.ajax.requesthandler.osgiservice.AJAXModuleActivator;
import com.openexchange.exception.OXException;
import com.openexchange.mail.dataobjects.MailMessage;
import com.openexchange.mail.json.MailActionFactory;
import com.openexchange.mail.json.converters.MailConverter;
import com.openexchange.mail.json.converters.MailJSONConverter;
import com.openexchange.server.ExceptionOnAbsenceServiceLookup;
import com.openexchange.tools.session.ServerSession;


/**
 * {@link MailJSONActivator}
 *
 * @author <a href="mailto:thorben.betten@open-xchange.com">Thorben Betten</a>
 */
public final class MailJSONActivator extends AJAXModuleActivator {

    protected static final Log LOG = com.openexchange.log.Log.loggerFor(MailJSONActivator.class);

    /**
     * Initializes a new {@link MailJSONActivator}.
     */
    public MailJSONActivator() {
        super();
    }

    @Override
    protected Class<?>[] getNeededServices() {
        return EMPTY_CLASSES;
    }

    @Override
    protected void startBundle() throws Exception {
        registerModule(new MailActionFactory(new ExceptionOnAbsenceServiceLookup(this)), "mail");
        final MailConverter converter = new MailConverter();
        registerService(ResultConverter.class, converter);
        registerService(ResultConverter.class, new MailJSONConverter(converter));

        registerService(AJAXResultDecorator.class, new AJAXResultDecorator() {
            
            @Override
            public String getIdentifier() {
                return "mail.senderImageUrl";
            }
            
            @Override
            public String getFormat() {
                return "mail";
            }
            
            @Override
            public void decorate(final AJAXRequestData requestData, final AJAXRequestResult result, final ServerSession session) throws OXException {
                final Object resultObject = result.getResultObject();
                if (null == resultObject) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("Result object is null.");
                    }
                    result.setResultObject(JSONObject.NULL, "json");
                    return;
                }
                final String action = requestData.getParameter("action");
                if ("get".equals(action) && resultObject instanceof MailMessage) {
                    final MailMessage mailMessage = (MailMessage) resultObject;
                    final InternetAddress[] from = mailMessage.getFrom();
                    if (null == from || 0 == from.length) {
                        return;
                    }
                    // TODO: Lookup contact by address
                }
            }
        });
    }

}
