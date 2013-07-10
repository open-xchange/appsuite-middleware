package com.openexchange.realtime.packet;

import static org.junit.Assert.*;
import org.junit.Test;
import com.openexchange.realtime.packet.IDComponentsParser.IDComponents;


public class IDComponentsParserTest {

    @Test
    public void testUser() {
        IDComponents idComponents = IDComponentsParser.parse("marc.arens");
        assertNull(idComponents.protocol);
        assertNull(idComponents.component);
        assertEquals("marc.arens", idComponents.user);
        assertNull(idComponents.context);
        assertNull(idComponents.resource);
    }
    
    @Test
    public void testUserAndContext() {
        IDComponents idComponents = IDComponentsParser.parse("marc.arens@premium");
        assertNull(idComponents.protocol);
        assertNull(idComponents.component);
        assertEquals("marc.arens", idComponents.user);
        assertEquals("premium", idComponents.context);
        assertNull(idComponents.resource);
    }
    
    @Test
    public void testUserContextAndResource() {
        IDComponents idComponents = IDComponentsParser.parse("marc.arens@premium/20d39asd9da93249f009d");
        assertNull(idComponents.protocol);
        assertNull(idComponents.component);
        assertEquals("marc.arens", idComponents.user);
        assertEquals("premium", idComponents.context);
        assertEquals("20d39asd9da93249f009d", idComponents.resource);
    }
    
    @Test
    public void testProtocolUserContextAndResource() {
        IDComponents idComponents = IDComponentsParser.parse("ox://marc.arens@premium/20d39asd9da93249f009d");
        assertEquals("ox", idComponents.protocol);
        assertNull(idComponents.component);
        assertEquals("marc.arens", idComponents.user);
        assertEquals("premium", idComponents.context);
        assertEquals("20d39asd9da93249f009d", idComponents.resource);
    }
    
    @Test
    public void testProtocolComponentUserContextAndResource() {
        IDComponents idComponents = IDComponentsParser.parse("ox.some.component://marc.arens@premium/20d39asd9da93249f009d");
        assertEquals("ox", idComponents.protocol);
        assertEquals("some.component", idComponents.component);
        assertEquals("marc.arens", idComponents.user);
        assertEquals("premium", idComponents.context);
        assertEquals("20d39asd9da93249f009d", idComponents.resource);
    }
}
