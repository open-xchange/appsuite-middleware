/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */

package com.openexchange.chronos.itip.analyzers;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openexchange.chronos.ical.ImportedCalendar;
import com.openexchange.chronos.itip.ITipAnalysis;
import com.openexchange.chronos.itip.ITipAnalyzer;
import com.openexchange.chronos.itip.ITipAnalyzerService;
import com.openexchange.chronos.itip.ITipIntegrationUtility;
import com.openexchange.chronos.itip.ITipMessage;
import com.openexchange.chronos.itip.ITipMethod;
import com.openexchange.chronos.itip.ITipSpecialHandling;
import com.openexchange.chronos.itip.LegacyAnalyzing;
import com.openexchange.chronos.itip.ical.ICal4JITipParser;
import com.openexchange.chronos.itip.osgi.Services;
import com.openexchange.chronos.service.CalendarSession;
import com.openexchange.config.ConfigurationService;
import com.openexchange.exception.OXException;

/**
 * {@link DefaultITipAnalyzerService}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 */
public class DefaultITipAnalyzerService implements ITipAnalyzerService {

    public static final Integer RANKING = new Integer(0);
    
    private final static Logger LOGGER = LoggerFactory.getLogger(DeclineCounterITipAnalyzer.class);

    private final ITipAnalyzer internalAnalyzer;
    private final List<ITipAnalyzer> analyzers;

    public DefaultITipAnalyzerService(ITipIntegrationUtility util) {
        super();
        this.analyzers = Collections.unmodifiableList(Arrays.<ITipAnalyzer> asList(
            new CancelAnalyzer(util),
            new RequestAnalyzer(util),
            new ReplyAnalyzer(util),
            new AddITipAnalyzer(util),
            new CancelITipAnalyzer(util),
            new DeclineCounterITipAnalyzer(util),
            new RefreshITipAnalyzer(util),
            new ReplyITipAnalyzer(util),
            new UpdateITipAnalyzer(util)
        ));
        internalAnalyzer = new InternalITipAnalyzer(util);
    }

    @Override
    public List<ITipAnalysis> analyze(InputStream ical, String format, CalendarSession session, Map<String, String> mailHeader) throws OXException {
        return analyze(ICal4JITipParser.importCalendar(ical), format, session, mailHeader);
    }

    public List<ITipAnalysis> analyze(ImportedCalendar calendar, String format, CalendarSession session, Map<String, String> mailHeader) throws OXException {
        int owner = 0;
        if (mailHeader.containsKey("com.openexchange.conversion.owner")) {
            owner = Integer.parseInt(mailHeader.get("com.openexchange.conversion.owner"));
        }

        List<ITipMessage> messages = ICal4JITipParser.parseMessage(calendar, owner, session);

        List<ITipAnalysis> result = new ArrayList<ITipAnalysis>(messages.size());
        for (ITipMessage message : messages) {
            ITipMethod method = message.getMethod();
            if (mailHeader.containsKey("X-Mailer") && mailHeader.get("X-Mailer").toLowerCase().contains("outlook")) {
                message.addFeature(ITipSpecialHandling.MICROSOFT);
            }

            /*
             * Get analyzer to use
             */
            boolean isLegacyScheduling = isLegacyScheduling();
            ITipAnalyzer analyzer = null;
            if (mailHeader.containsKey("X-Open-Xchange-Object")) {
                analyzer = internalAnalyzer;
            } else {
                for (ITipAnalyzer iTipAnalyzer : analyzers) {
                    if (iTipAnalyzer.getMethods().contains(method)) {
                        if (isLegacyScheduling) {
                            if (LegacyAnalyzing.class.isAssignableFrom(iTipAnalyzer.getClass())) {
                                analyzer = iTipAnalyzer;
                                break;
                            }
                        } else {
                            analyzer = iTipAnalyzer;
                            break;
                        }
                    }
                }
            }

            if (analyzer == null) {
                LOGGER.error("Could not find itip analyzer for method {}", method.toString());
            } else {
                result.add(analyzer.analyze(message, mailHeader, format, session));
            }
        }

        return result;
    }

    private boolean isLegacyScheduling() {
        ConfigurationService configurationService = Services.getService(ConfigurationService.class);
        return null == configurationService || configurationService.getBoolProperty("com.openexchange.calendar.useLegacyScheduling", false);
    }

}
