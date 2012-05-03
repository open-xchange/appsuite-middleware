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

package com.openexchange.calendar.itip.analyzers;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import com.openexchange.calendar.itip.ITipAnalysis;
import com.openexchange.calendar.itip.ITipAnalyzer;
import com.openexchange.calendar.itip.ITipAnalyzerService;
import com.openexchange.calendar.itip.ITipIntegrationUtility;
import com.openexchange.data.conversion.ical.itip.ITipMessage;
import com.openexchange.data.conversion.ical.itip.ITipMethod;
import com.openexchange.data.conversion.ical.itip.ITipSpecialHandling;
import com.openexchange.exception.OXException;
import com.openexchange.server.ServiceLookup;
import com.openexchange.session.Session;


/**
 * {@link DefaultITipAnalyzerService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class DefaultITipAnalyzerService implements ITipAnalyzerService {
    
    private final Map<ITipMethod, ITipAnalyzer> analyzers = new EnumMap<ITipMethod, ITipAnalyzer>(ITipMethod.class);
    
    private final ITipAnalyzer internalAnalyzer;
    
    public DefaultITipAnalyzerService(ITipIntegrationUtility util, ServiceLookup services) {
        add(new AddITipAnalyzer(util, services));
        add(new CancelITipAnalyzer(util, services));
        add(new DeclineCounterITipAnalyzer(util, services));
        add(new RefreshITipAnalyzer(util, services));
        add(new ReplyITipAnalyzer(util, services));
        add(new UpdateITipAnalyzer(util, services));
        
        internalAnalyzer = new InternalITipAnalyzer(util, services);
    }
    
    private void add(ITipAnalyzer analyzer) {
        List<ITipMethod> methods = analyzer.getMethods();
        for (ITipMethod method : methods) {
            analyzers.put(method, analyzer);
        }
    }

    @Override
    public List<ITipAnalysis> analyze(List<ITipMessage> messages, String format, Session session, Map<String, String> mailHeader) throws OXException {
        List<ITipAnalysis> result = new ArrayList<ITipAnalysis>(messages.size());
        for (ITipMessage message : messages) {
            ITipMethod method = message.getMethod();
            if (mailHeader.containsKey("X-Mailer") && mailHeader.get("X-Mailer").toLowerCase().contains("outlook")) {
            	message.addFeature(ITipSpecialHandling.MICROSOFT);
            }
            if (method == ITipMethod.COUNTER && message.hasFeature(ITipSpecialHandling.MICROSOFT)) {
                method = ITipMethod.REPLY;
            }
            
            ITipAnalyzer analyzer;
            if (mailHeader.containsKey("X-Open-Xchange-Object")) {
                analyzer = internalAnalyzer;
            } else {
                analyzer = analyzers.get(method);
            }
            
            if (analyzer == null) {
                // TODO: Error
            } else {
            	result.add(analyzer.analyze(message, mailHeader, format, session));
            }
        }
        
        return result;
    }

}
