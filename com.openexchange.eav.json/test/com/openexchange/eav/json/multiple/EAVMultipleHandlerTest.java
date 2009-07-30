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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

package com.openexchange.eav.json.multiple;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.TimeZone;
import org.json.JSONException;
import com.openexchange.eav.EAVNode;
import com.openexchange.eav.EAVPath;
import com.openexchange.eav.EAVType;
import com.openexchange.eav.EAVTypeMetadataNode;
import com.openexchange.eav.json.exception.EAVJsonExceptionMessage;
import com.openexchange.groupware.AbstractOXException;
import com.openexchange.json.JSONAssertion;


/**
 * {@link EAVMultipleHandlerTest}
 *
 * @author <a href="mailto:francisco.laguna@open-xchange.com">Francisco Laguna</a>
 *
 */
public class EAVMultipleHandlerTest extends DeclarativeEAVMultipleHandlerTest {

    /*
     * Happy paths
     */
    
    public void testNewWithoutMetadata()  throws Exception {
        
        duringRequest("new", PARAMS("path" , "/contacts/12/13/com.openexchange.test"), BODY("{number : 12, string : \"Hello\", bool : true, multiples : { number :  [12,13,14], string : [\"a\", \"b\", \"c\"], bool : [true, true, false] }}"));
        
        EAVNode stored = N("com.openexchange.test", 
                             N("number", 12),
                             N("string", "Hello"),
                             N("bool", true),
                             N("multiples", 
                                 N("number", 12,13,14),
                                 N("string", "a", "b", "c"),
                                 N("bool", true, true, false)
                             )
                         );
        
        expectStorageCall("insert", ctx, new EAVPath("contacts", "12", "13"), stored);

        expectResponseData(1);
        
        runRequest();
        
    }
    
    public void testNewWithMetadata() throws Exception {
        TimeZone tz = TimeZone.getTimeZone("Pacific/Rarotonga");
        long nowUTC = System.currentTimeMillis();
        long nowRarotonga = nowUTC + tz.getOffset(nowUTC);
        
        duringRequest("new", PARAMS("path", "/contacts/12/13/com.openexchange.test"), BODY("{data : {strings : [\"a\",\"b\",\"c\", \"c\"], time : "+nowRarotonga+"}, types : { strings : {containerType : \"set\"}, time : {t : \"Time\", timezone : \""+tz.getID()+"\"} }}"));
        
        EAVNode stored = 
        N("com.openexchange.test",
            SET("strings", "a", "b", "c"),
            N("time", EAVType.TIME, nowUTC)
        );
        
        expectStorageCall("insert", ctx, new EAVPath("contacts", "12", "13"), stored);

        expectResponseData(1);
        
        runRequest();
    }
    
    public void testNewAttribute() throws Exception {
        duringRequest("new", PARAMS("path", "/contacts/12/13/com.openexchange.test/string"), BODY("\"Hello World\""));
        
        expectStorageCall("insert", ctx, new EAVPath("contacts", "12", "13", "com.openexchange.test"), N("string", "Hello World"));
        
        expectResponseData(1);
        
        runRequest();
    }
    
    public void testNewAttributeWithMetadata() throws Exception {
        
        TimeZone tz = TimeZone.getTimeZone("Pacific/Rarotonga");
        long nowUTC = System.currentTimeMillis();
        long nowRarotonga = nowUTC + tz.getOffset(nowUTC);
        
        
        duringRequest("new", PARAMS("path", "/contacts/12/13/com.openexchange.test/time"), BODY("{data : "+nowRarotonga+", types : {t : \"Time\", timezone : \""+tz.getID()+"\"}}"));
        
        expectStorageCall("insert", ctx, new EAVPath("contacts", "12", "13", "com.openexchange.test"), N("time", EAVType.TIME, nowUTC));
        
        expectResponseData(1);
        
        runRequest();
    }
    
    public void testUpdateWithoutSuppliedMetadataApplyingStoredMetadata() throws Exception {
        long someDay = 1245715200000l;
        duringRequest("update", PARAMS("path", "/contacts/12/13/com.openexchange.test"), BODY("{date : "+someDay+"}"));
        
        EAVPath parent = new EAVPath("contacts", "12", "13");
        EAVNode parsed = N("com.openexchange.test", N("date", EAVType.NUMBER, someDay));
        
        EAVTypeMetadataNode metadata = TYPE("com.openexchange.test",
                                            TYPE("date", EAVType.DATE)
                                        );
        
        
        expectStorageCall("getTypes", ctx, parent, parsed);
        andReturn(metadata);
        
        EAVNode update = N("com.openexchange.test", N("date", EAVType.DATE, someDay));
        expectStorageCall("update", ctx, parent, update);
        
        expectResponseData(1);
        
        runRequest();
    
    }
    
    public void testUpdateWithSuppliedMetadata() throws Exception {
        TimeZone tz = TimeZone.getTimeZone("Pacific/Rarotonga");
        long someDay = 1245715200000l;
        long nowUTC = System.currentTimeMillis();
        long nowRarotonga = nowUTC + tz.getOffset(nowUTC);
        
        duringRequest("update", PARAMS("path", "/contacts/12/13/com.openexchange.test"), BODY("{data : {date : "+someDay+", time : "+nowRarotonga+"}, types : {time : {timezone : \""+tz.getID()+"\"}}}"));
        
        EAVPath parent = new EAVPath("contacts", "12", "13");
        EAVNode parsed = N("com.openexchange.test", N("date", EAVType.NUMBER, someDay), N("time", EAVType.NUMBER, nowRarotonga));
        
        EAVTypeMetadataNode metadata = TYPE("com.openexchange.test",
                                            TYPE("date", EAVType.DATE),
                                            TYPE("time", EAVType.TIME)
                                        );
        
        
        expectStorageCall("getTypes", ctx, parent, parsed);
        andReturn(metadata);
        
        EAVNode update = N("com.openexchange.test", N("date", EAVType.DATE, someDay), N("time", EAVType.TIME, nowUTC));
        expectStorageCall("update", ctx, parent, update);
        
        expectResponseData(1);
        
        runRequest();
    
    }
    
    public void testUpdateAttribute() throws Exception {
        duringRequest("update", PARAMS("path", "/contacts/12/13/com.openexchange.test/attribute"), BODY("12"));
        
        EAVNode parsed = N("attribute", 12);
        EAVTypeMetadataNode metadata = TYPE("attribute", EAVType.NUMBER);
        EAVPath parent = new EAVPath("contacts", "12", "13", "com.openexchange.test");
        
        expectStorageCall("getTypes", ctx, parent, parsed);
        andReturn(metadata);
        
        expectStorageCall("update", ctx, parent, parsed);
        
        expectResponseData(1);
   
        runRequest();
    }
    
    public void testUpdateAttributeWithMetadata() throws Exception {
        duringRequest("update", PARAMS("path", "/contacts/12/13/com.openexchange.test/attribute"), BODY("{data : 1245715200000, types : {t : \"Date\"}}"));
        
        EAVNode parsed = N("attribute", 1245715200000l);
        EAVNode update = N("attribute", EAVType.DATE, 1245715200000l);
        
        EAVTypeMetadataNode metadata = TYPE("attribute", EAVType.DATE);
        EAVPath parent = new EAVPath("contacts", "12", "13", "com.openexchange.test");
        
        expectStorageCall("getTypes", ctx, parent, parsed);
        andReturn(metadata);
        
        expectStorageCall("update", ctx, parent, update);
        
        expectResponseData(1);
   
        runRequest();
    }

    
    //TODO: updateArray
    
    public void testDelete() throws Exception {
        duringRequest("delete", PARAMS("path", "/contacts/12/13/com.openexchange.test/subtree"));
        
        expectStorageCall("delete", ctx, new EAVPath("contacts", "12", "13", "com.openexchange.test", "subtree"));
        
        expectResponseData(1);
        
        runRequest();
    }
    
    public void testGetWithoutMetadata() throws Exception {
        EAVNode tree =
        N("com.openexchange.test", 
            N("exampleString", "Hallo"),
            N("exampleBoolean", true),
            N("exampleNumber", 12),
            N("exampleFloat", 12.1),
            N("multiples", 
                N("strings", "Hello", "World","what's", "up"),
                N("bools", true, true, false, true, false, false, false, true),
                N("numbers", 12,13,14,15)
            )
        );
        
        JSONAssertion jsonAssertion = new JSONAssertion()
        .isObject()
            .hasKey("exampleString").withValue("Hallo")
            .hasKey("exampleBoolean").withValue(true)
            .hasKey("exampleNumber").withValue(12)
            .hasKey("exampleFloat").withValue(12.1)
            .hasKey("multiples").withValueObject()
                .hasKey("strings").withValueArray().withValues("Hello", "World", "what's", "up").inAnyOrder()
                .hasKey("bools").withValueArray().withValues(true, true, false, true, false, false, false, true).inAnyOrder()
                .hasKey("numbers").withValueArray().withValues(12,13,14,15).inAnyOrder()
            .objectEnds()
        .objectEnds();
        
        
        duringRequest("get", PARAMS("path", "/contacts/12/13/com.openexchange.test"));
        expectStorageCall("get", ctx, new EAVPath("contacts","12","13","com.openexchange.test"), false);
        andReturn(tree);
        
        expectResponseData(jsonAssertion);
        
        runRequest();
        
        
    }
    
    public void testGetAllBinaries() throws Exception {
       
        byte[] bytes = "Hello World".getBytes("UTF-8");
        String encoded = com.openexchange.tools.encoding.Base64.encode(bytes);
        
        EAVNode tree = 
        N("myBinaries", 
            N("binary1", bytes),
            N("binary2", bytes),
            N("binary3", bytes)
        );
        
        JSONAssertion jsonAssertion = new JSONAssertion()
        .isObject()
            .hasKey("binary1").withValue(encoded)
            .hasKey("binary2").withValue(encoded)
            .hasKey("binary3").withValue(encoded)
        .objectEnds();
        
        duringRequest("get", PARAMS("path", "/contacts/12/13/com.openexchange.test/myBinaries", "allBinaries", true));
        expectStorageCall("get", ctx, new EAVPath("contacts","12","13","com.openexchange.test", "myBinaries"), true);
        andReturn(tree);
        
        expectResponseData(jsonAssertion);
        
        runRequest();
    }
    
    public void testGetCertainBinaries() throws Exception {
        byte[] bytes = "Hello World".getBytes("UTF-8");
        String encoded = com.openexchange.tools.encoding.Base64.encode(bytes);
        
        EAVNode tree = 
        N("myBinaries", 
            N("binary1", bytes),
            N("binary2", bytes),
            N("binary3", bytes)
        );
        
        JSONAssertion jsonAssertion = new JSONAssertion()
        .isObject()
            .hasKey("binary1").withValue(encoded)
            .hasKey("binary2").withValue(encoded)
            .hasKey("binary3").withValue(encoded)
        .objectEnds();
        
        duringRequest("get", PARAMS("path", "/contacts/12/13/com.openexchange.test/myBinaries"), BODY("{loadBinaries : [\"binary1\", \"binary2\", \"binary3\"] }"));
        expectStorageCall("get", ctx, new EAVPath("contacts","12","13","com.openexchange.test", "myBinaries"), new HashSet<EAVPath>(Arrays.asList(new EAVPath("binary1"), new EAVPath("binary2"), new EAVPath("binary3"))));
        andReturn(tree);
        
        expectResponseData(jsonAssertion);
        
        runRequest();
    }
    
    public void testGetAttribute() throws Exception {
        duringRequest("get", PARAMS("path", "/contacts/12/13/com.openexchange.test/myString"));
        
        expectStorageCall("get", ctx, new EAVPath("contacts", "12", "13", "com.openexchange.test", "myString"), false);
        andReturn(N("myString", "Hello World"));
        
        expectResponseData("Hello World");
        
        runRequest();
    }
    
    public void testGetSpecificBinary() throws Exception {
        duringRequest("get", PARAMS("path", "/contacts/12/13/com.openexchange.test/myFile"));
        
        expectStorageCall("get", ctx, new EAVPath("contacts", "12", "13", "com.openexchange.test", "myFile"), false);
        byte[] bytes = "Hello World".getBytes("UTF-8");
        andReturn(N("myFile", bytes));
        
        ignoreResponseData();
        
        runRequest();
        
        InputStream is = (InputStream) response;
        
        int i = 0;
        int read = -1;
        while((read = is.read()) != -1) {
            assertEquals(bytes[i++], read);
        }
        assertEquals(i, bytes.length);
        
    }
    
    public void testGetSpecificBinaryInBase64() throws Exception {  
        byte[] bytes = "Hello World".getBytes("UTF-8");
        String encoded = com.openexchange.tools.encoding.Base64.encode(bytes);
        
        duringRequest("get", PARAMS("path", "/contacts/12/13/com.openexchange.test/myFile", "binaryEncoding", "base64"));
        
        expectStorageCall("get", ctx, new EAVPath("contacts", "12", "13", "com.openexchange.test", "myFile"), false);
        
        andReturn(N("myFile", bytes));
        
        expectResponseData(encoded);
        
        runRequest();
        
    }
    
    public void testGetAttributeWithMetadata() throws Exception {
        TimeZone tz = TimeZone.getTimeZone("Pacific/Rarotonga");
        long nowUTC = System.currentTimeMillis();
        long nowRarotonga = nowUTC + tz.getOffset(nowUTC);
        
        duringRequest("get", PARAMS("path", "/contacts/12/13/com.openexchange.test/myTime"), BODY("{types : {timezone : \"Pacific/Rarotonga\"}}"));
        
        expectStorageCall("get", ctx, new EAVPath("contacts", "12", "13", "com.openexchange.test", "myTime"), false);
        andReturn(N("myTime", EAVType.TIME, nowUTC));
        
        expectResponseData(nowRarotonga);
        
        runRequest();
    }
    
    
    /*
     * Error conditions
     */
    
   
    public void testNewWithoutPath() throws JSONException, AbstractOXException {
        duringRequest("new", PARAMS());
        
        expectException(EAVJsonExceptionMessage.MissingParameter.getDetailNumber());
        
        runRequest();
        
    }
    
    public void testNewWithoutBody() throws AbstractOXException, JSONException {
        duringRequest("new", PARAMS("path" , "/contacts/12/13/com.openexchange.test"));
        
        expectException(EAVJsonExceptionMessage.MissingParameter.getDetailNumber());
        
        runRequest();
    }
    
    public void testUpdateWithoutPath() throws AbstractOXException, JSONException {
        duringRequest("update", PARAMS());
        
        expectException(EAVJsonExceptionMessage.MissingParameter.getDetailNumber());
        
        runRequest();
    }
    
    public void testUpdateWithoutBody() throws JSONException, AbstractOXException {
        duringRequest("update", PARAMS("path" , "/contacts/12/13/com.openexchange.test"));
        
        expectException(EAVJsonExceptionMessage.MissingParameter.getDetailNumber());
        
        runRequest();
    }
    
    public void testDeleteWithoutPath() throws JSONException, AbstractOXException {
        duringRequest("delete", PARAMS());
     
        expectException(EAVJsonExceptionMessage.MissingParameter.getDetailNumber());
        
        runRequest();
    }
    
    public void testGetWithoutPath() throws AbstractOXException, JSONException {
        duringRequest("get", PARAMS());
        
        expectException(EAVJsonExceptionMessage.MissingParameter.getDetailNumber());
        
        runRequest();
    }
    
    public void testGetCertainBinariesWithInvalidLoadBinariesMetadata() throws JSONException, AbstractOXException {
        duringRequest("get", PARAMS("path", "/contacts/12/13/com.openexchange.test/myBinaries"), BODY("{loadBinaries : {someKey : 12} }"));
        
        expectException(EAVJsonExceptionMessage.InvalidLoadBinaries.getDetailNumber());
        
        runRequest();
        
    }
    
    public void testGetWithConflictingBinaryOptions() {
        
    }
    
    public void testGetWithInvalidBinaryEncodingSwitch() {
        
    }

    public void testUnknownAction() {
        
    }
}
