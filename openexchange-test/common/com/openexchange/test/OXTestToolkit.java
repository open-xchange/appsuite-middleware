package com.openexchange.test;

import junit.framework.Assert;

import java.util.Date;
import java.io.InputStream;
import java.io.IOException;
import java.io.ByteArrayOutputStream;


public class OXTestToolkit {
    public static void assertEqualsAndNotNull(String message, Date expect, Date value) throws Exception {
        if (expect != null) {
            Assert.assertNotNull(message + " is null", value);
            Assert.assertEquals(message, expect.getTime(), value.getTime());
        }
    }

    public static void assertEqualsAndNotNull(String message, byte[] expect, byte[] value) throws Exception {
        if (expect != null) {
            Assert.assertNotNull(message + " is null", value);
            Assert.assertEquals(message + " byte array size is not equals", expect.length, value.length);
            for (int a = 0; a < expect.length; a++) {
                Assert.assertEquals(message + " byte in pos (" + a + ") is not equals",  expect[a], value[a]);
            }
        }
    }

    public static void assertEqualsAndNotNull(String message, Object expect, Object value) throws Exception {
        if (expect != null) {
            Assert.assertNotNull(message + " is null", value);
            Assert.assertEquals(message, expect, value);
        }
    }

    public static void assertSameContent(InputStream is1, InputStream is2) throws IOException {
        int i = 0;
        while((i = is1.read()) != -1){
            Assert.assertEquals(i, is2.read());
        }
        Assert.assertEquals(-1,is2.read());
    }

    public static String readStreamAsString(InputStream is) throws IOException {
        int len;
        byte[] buffer = new byte[0xFFFF];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while( (len = is.read(buffer)) != -1 ){
            baos.write(buffer, 0, len);
        }
        is.close();
        buffer = baos.toByteArray();
        baos.close();
        return new String(buffer, "UTF-8");
    }
}
