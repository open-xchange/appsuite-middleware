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

package com.openexchange.realtime.json.impl.stanza;

import static org.junit.Assert.assertEquals;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.java.Streams;
import com.openexchange.realtime.json.impl.stanza.builder.StanzaBuilderSelector;
import com.openexchange.realtime.json.stanza.StanzaBuilder;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Stanza;


/**
 * {@link PresenceBuilderTest}
 *
 * @author <a href="mailto:marc.arens@open-xchange.com">Marc Arens</a>
 */
public class PresenceBuilderTest {

    private static final String presenceRequest = "{\n\t\"element\":\"presence\",\n\t\"type\":\"subscribe\",\n\t\"to\":\"ox://marc.arens@premium\",\n\t\"session\":\"1234\",\n\t\"payloads\":\n\t\t[\n\t            {\"element\":\"show\", \"data\":\"Hello marens, please let me subscribe to your presence, WBR., Mr. X\"},\n\t            {\"element\":\"priority\", \"data\":\"7\"},\n\t            \n\t            {\"element\":\"flatObject\", \"data\":\n\t            \t{\n\t            \t\t\t\"string\":\"hello0\",\n\t            \t\t\t\"number\":\"0\",\n\t            \t\t\t\"boolean\":\"false\",\n\t            \t\t\t\"null\":\"null\"\n\t            \t}\n\t            },\n\t            \n\t            {\"element\":\"arrayOfFlatObjects\", \"data\":\n            \t\t[\n\t\t\t\t\t\t{\"element\":\"flatObject1\", \"data\":\n\t\t\t\t\t\t\t{\n\t\t\t\t\t\t\t\t\"string\":\"hello1\",\n\t\t\t\t\t\t\t\t\"number\":\"1\",\n\t\t\t\t\t\t\t\t\"boolean\":\"false\",\n\t\t\t\t\t\t\t\t\"null\":\"null\"\n\t\t\t\t\t\t\t}\n\t\t\t\t\t\t},\n\t\t\t\t\t\t{\"element\":\"flatObject2\", \"data\":\n\t\t\t\t\t\t\t{\n\t\t\t\t\t\t\t\t\"string\":\"hello2\",\n\t\t\t\t\t\t\t\t\"number\":\"2\",\n\t\t\t\t\t\t\t\t\"boolean\":\"false\",\n\t\t\t\t\t\t\t\t\"null\":\"null\"\n\t\t\t\t\t\t\t}\n\t\t\t\t\t\t}\n\t\t\t\t\t]\n\t\t\t\t}\n\t            \n\t    ]\n}\n";
        
    private JSONObject presenceJSON = null;

    private final static String readFile(String file) throws IOException, URISyntaxException {
        FileChannel channel = null;
        try {
            URL path = PresenceBuilderTest.class.getResource(file);
            channel = new FileInputStream(new File(path.toURI())).getChannel();
            ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
            channel.read(buffer);
            channel.close();
            channel = null;
            return new String(buffer.array());
        } finally {
            Streams.close(channel);
        }
     }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        presenceJSON = new JSONObject(presenceRequest);
    }

    /**
     * Test method for {@link com.openexchange.realtime.atmosphere.impl.parser.PresenceParser#parseStanza(org.json.JSONObject)}.
     * @throws OXException
     */
    @Test
    public void testBuildPresence() throws OXException {
        StanzaBuilder<? extends Stanza> builder = StanzaBuilderSelector.getBuilder(new ID("ox://thorben.betten@premium"), null, presenceJSON);
        Stanza stanza = builder.build();
        assertEquals(6, stanza.getPayloadTrees().size());
    }

}
