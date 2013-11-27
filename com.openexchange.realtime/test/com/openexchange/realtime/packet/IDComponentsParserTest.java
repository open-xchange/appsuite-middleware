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
        IDComponents idComponents = IDComponentsParser.parse("ox://marc.arens@premium/20d39asd9da9/3249f009d");
        assertEquals("ox", idComponents.protocol);
        assertNull(idComponents.component);
        assertEquals("marc.arens", idComponents.user);
        assertEquals("premium", idComponents.context);
        assertEquals("20d39asd9da9/3249f009d", idComponents.resource);
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
    
    @Test
    public void testComponentContextAndResource(){
        IDComponents idComponents = IDComponentsParser.parse("synthetic.office://operations/folderId.fileId~fileVersion_fileName");
        assertEquals("folderId.fileId~fileVersion_fileName", idComponents.resource);
        
        assertEquals("folderId.fileId~fileVersion_fileName", new ID("synthetic.office://operations/folderId.fileId~fileVersion_fileName").getResource());
        
    }
    
    // Bug 30006
    @Test
    public void testAtSignInContextName() {
        ID id = new ID(new ID("protocol", "user", "context_with_@_sign", "resource").toString());
        assertEquals("context_with_@_sign", id.getContext());
    }
    
    @Test
    public void testAtSignInUserName() {
        ID id = new ID(new ID("protocol", "user_with_@_sign", "context", "resource").toString());
        assertEquals("user_with_@_sign", id.getUser());
    }
    
    @Test
    public void testForwardSlashInContextName() {
        ID id = new ID(new ID("protocol", "user", "context_with_/_sign", "resource").toString());
        assertEquals("context_with_/_sign", id.getContext());
    }
    
    public void testBackslashInContextName() {
        ID id = new ID(new ID("protocol", "user", "context_with_\\_sign", "resource").toString());
        assertEquals("context_with_\\_sign", id.getContext());
    }
    
    @Test
    public void testBackslashInUserName() {
        ID id = new ID(new ID("protocol", "user_with_\\_sign", "context", "resource").toString());
        assertEquals("user_with_\\_sign", id.getUser());
    }
}
