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

package com.openexchange.subscribe.external.parser;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.modules.Module;
import com.openexchange.subscribe.external.ExternalSubscriptionSource;
import com.openexchange.subscribe.microformats.OXMFParser;
import com.openexchange.subscribe.microformats.OXMFParserFactoryService;


/**
 * {@link ListingParser}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class ListingParser {

    private static final String SOURCE_ID = "ox_sourceId";
    private static final String ICON = "ox_icon";
    private static final String MODULE = "ox_module";
    private static final String DISPLAY_NAME = "ox_displayName";
    private static final String LINK = "ox_link";
    private static final String PRIORITY = "ox_priority";


    private final OXMFParserFactoryService parserFactory;

    /**
     * Initializes a new {@link ListingParser}.
     * @param microformatParserFactory
     */
    public ListingParser(final OXMFParserFactoryService parserFactory) {
        super();
        this.parserFactory = parserFactory;
    }

    public List<ExternalSubscriptionSource> parse(final String html) throws OXException {
        return parse(new StringReader(html));
    }

    public List<ExternalSubscriptionSource> parse(final Reader html) throws OXException {
        final OXMFParser parser = parserFactory.getParser();
        parser.addAttributePrefix("ox_");
        parser.addContainerElement("ox_subscriptionSource");
        final List<Map<String, String>> parsed = parser.parse(html);

        final List<ExternalSubscriptionSource> sources = new ArrayList<ExternalSubscriptionSource>(parsed.size());
        for(final Map<String, String> attributes : parsed) {
            sources.add(transform(attributes));
        }

        return sources;
    }

    private ExternalSubscriptionSource transform(final Map<String, String> attributes) {
        final ExternalSubscriptionSource source = new ExternalSubscriptionSource();
        apply(source, attributes);
        return source;
    }

    public static void apply(final ExternalSubscriptionSource source, final Map<String, String> attributes) {
        if(attributes.containsKey(SOURCE_ID)) {
            source.setId(attributes.get(SOURCE_ID));
        }
        if(attributes.containsKey(ICON)) {
            source.setIcon(attributes.get(ICON));
        }
        if(attributes.containsKey(MODULE)) {
            source.setFolderModule(Module.getModuleInteger(attributes.get(MODULE)));
        }
        if(attributes.containsKey(DISPLAY_NAME)) {
            source.setDisplayName(attributes.get(DISPLAY_NAME));
        }
        if(attributes.containsKey(LINK)) {
            source.setExternalAddress(attributes.get(LINK));
        }
        if(attributes.containsKey(PRIORITY)) {
            source.setPriority(Integer.valueOf(attributes.get(PRIORITY)));
        }
    }

}
