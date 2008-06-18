package com.openexchange.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import junit.framework.Assert;


public class OXTestToolkit {
    public static void assertEqualsAndNotNull(final String message, final Date expect, final Date value) throws Exception {
        if (expect != null) {
            Assert.assertNotNull(message + " is null", value);
            Assert.assertEquals(message, expect.getTime(), value.getTime());
        }
    }

    public static void assertEqualsAndNotNull(final String message, final byte[] expect, final byte[] value) throws Exception {
        if (expect != null) {
            Assert.assertNotNull(message + " is null", value);
            Assert.assertEquals(message + " byte array size is not equals", expect.length, value.length);
            for (int a = 0; a < expect.length; a++) {
                Assert.assertEquals(message + " byte in pos (" + a + ") is not equals",  expect[a], value[a]);
            }
        }
    }

    public static void assertEqualsAndNotNull(final String message, final Object expect, final Object value) throws Exception {
        if (expect != null) {
            Assert.assertNotNull(message + " is null", value);
            Assert.assertEquals(message, expect, value);
        }
    }

    public static void assertSameContent(final InputStream is1, final InputStream is2) throws IOException {
        int i = 0;
        while((i = is1.read()) != -1){
            Assert.assertEquals(i, is2.read());
        }
        Assert.assertEquals(-1,is2.read());
    }

    public static String readStreamAsString(final InputStream is) throws IOException {
        int len;
        byte[] buffer = new byte[0xFFFF];
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while( (len = is.read(buffer)) != -1 ){
            baos.write(buffer, 0, len);
        }
        is.close();
        buffer = baos.toByteArray();
        baos.close();
        return new String(buffer, "UTF-8");
    }
}
