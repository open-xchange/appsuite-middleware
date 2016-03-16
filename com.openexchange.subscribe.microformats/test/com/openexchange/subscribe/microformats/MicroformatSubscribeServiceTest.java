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

package com.openexchange.subscribe.microformats;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import junit.framework.TestCase;
import com.openexchange.exception.OXException;
import com.openexchange.subscribe.Subscription;
import com.openexchange.subscribe.microformats.datasources.SimMicroformatSource;
import com.openexchange.subscribe.microformats.parser.SimOXMFParser;
import com.openexchange.subscribe.microformats.parser.SimOXMFParserFactory;
import com.openexchange.subscribe.microformats.transformers.SimMapTransformer;


/**
 * {@link MicroformatSubscribeServiceTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class MicroformatSubscribeServiceTest extends TestCase {

    // here we'll test our transformation workflow
    // The data from the microformat source is passed on to the OXMF Parser which turns the OXMF into a List<Map<String, String>> structure
    // which in turn is passed to the transformer that spits out ox objects understood by the rest of the system
    public void testGetContent() throws OXException {
        MicroformatSubscribeService subscribeService = new MicroformatSubscribeService();

        SimMicroformatSource mfSource = new SimMicroformatSource("I would normally be microformatted HTML");


        List<Map<String, String>> parsed = new ArrayList<Map<String, String>>();

        Map<String, String> entry = new HashMap<String, String>();
        entry.put("ox_some_attribute", "some value");
        parsed.add(entry);

        SimOXMFParser parser = new SimOXMFParser(parsed);
        SimOXMFParserFactory parserFactory = new SimOXMFParserFactory(parser);

        List<Object> transformed = new ArrayList<Object>();
        Object value = new Object();
        transformed.add(value);
        SimMapTransformer transformer = new SimMapTransformer(transformed);

        subscribeService.setOXMFSource(mfSource);
        subscribeService.setOXMFParserFactory(parserFactory);
        subscribeService.setTransformer(transformer);

        Subscription subscription = new Subscription();
        subscription.setId(1337);
        Collection content = subscribeService.getContent(subscription);

        assertNotNull("Content was null!", content);
        assertTrue("Expected transformed is returned", content.equals(transformed));
        assertTrue("Expected output from oxmf parser as input for transformation", parsed == transformer.getInput());
        assertEquals("Expected output from mfSource to be input for oxmf parser", "I would normally be microformatted HTML", parser.getHtml());
        assertTrue("Expected subscription as input to the mfSource", subscription == mfSource.getSubscription());


    }

    public void testConfigureParser() {
        MicroformatSubscribeService subscribeService = new MicroformatSubscribeService();

        SimOXMFParser parser = new SimOXMFParser(null);

        subscribeService.addContainerElement("ox_contact");
        subscribeService.addContainerElement("ox_task");
        subscribeService.addPrefix("ox_");
        subscribeService.addPrefix("google_");

        subscribeService.configureParser(parser);

        assertTrue("Expected ox_contact as container", parser.knowsContainer("ox_contact"));
        assertTrue("Expected ox_task as container", parser.knowsContainer("ox_task"));
        assertTrue("Expected ox_ as prefix", parser.knowsPrefix("ox_"));
        assertTrue("Expected google_ as prefix", parser.knowsPrefix("google_"));


    }
}
