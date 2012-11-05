package com.openexchange.realtime.atmosphere.impl.stanza;

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
import com.openexchange.conversion.simple.SimplePayloadConverter;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.atmosphere.impl.StanzaTransformer;
import com.openexchange.realtime.atmosphere.impl.stanza.builder.StanzaBuilderSelector;
import com.openexchange.realtime.atmosphere.osgi.ExtensionRegistry;
import com.openexchange.realtime.atmosphere.presence.converter.ByteToJSONConverter;
import com.openexchange.realtime.atmosphere.stanza.StanzaBuilder;
import com.openexchange.realtime.packet.ID;
import com.openexchange.realtime.packet.Presence;
import com.openexchange.realtime.packet.Stanza;
import com.openexchange.realtime.payload.converter.ByteTransformer;
import com.openexchange.realtime.payload.converter.StringTransformer;

public class PresenceTransformerTest {
    private String presenceRequest = null;
    private JSONObject presenceJSON = null;
    private Stanza jsonStanza = null;
    
    private final static String readFile(String file) throws IOException, URISyntaxException {
        URL path = PresenceTransformerTest.class.getResource(file);
        FileChannel channel = new FileInputStream(new File(path.toURI())).getChannel();
        ByteBuffer buffer = ByteBuffer.allocate((int) channel.size());
        channel.read(buffer);
        channel.close();
        return new String(buffer.array());
     }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        presenceRequest = readFile("presenceTransformer.json");
        presenceJSON = new JSONObject(presenceRequest);
        StanzaBuilder<? extends Stanza> builder = StanzaBuilderSelector.getBuilder(new ID("ox://thorben.betten@premium"), presenceJSON);
        jsonStanza = builder.build();
        
        ExtensionRegistry registry = ExtensionRegistry.getInstance();
        //registry.addPayloadElementTransFormer(new PresenceStateTransformer());
        //registry.addElementPathMapping(Presence.PRESENCE_STATE_PATH, PresenceState.class);
        registry.addPayloadElementTransFormer(new StringTransformer());
        registry.addElementPathMapping(Presence.MESSAGE_PATH, String.class);
        registry.addPayloadElementTransFormer(new ByteTransformer());
        registry.addElementPathMapping(Presence.PRIORITY_PATH, Byte.class);
        
        //---
    }

    /**
     * Test method for {@link com.openexchange.realtime.atmosphere.impl.parser.PresenceParser#parseStanza(org.json.JSONObject)}.
     * @throws OXException 
     */
    @Test
    public void testTransformPresence() throws OXException {
        StanzaTransformer transformer = new StanzaTransformer();
        transformer.incoming(jsonStanza, null);
        
        
        
    }
}
