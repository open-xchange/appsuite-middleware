
package com.openexchange.tools.io;

import static org.junit.Assert.assertEquals;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.junit.Test;

// Bug 6104
public class SizeAwareInputStreamTest {

    @Test
    public void testCountSimpleRead() throws IOException {
        final ByteArrayInputStream b = new ByteArrayInputStream(new byte[23]);
        final TestSizeAwareInputStream testStream = new TestSizeAwareInputStream(b);
        while (testStream.read() != -1) {
        }

        assertEquals(23, testStream.getSize());
    }

    @Test
    public void testCountReadBuffer() throws IOException {
        final ByteArrayInputStream b = new ByteArrayInputStream(new byte[23]);
        final TestSizeAwareInputStream testStream = new TestSizeAwareInputStream(b);
        while (testStream.read(new byte[5]) != -1) {
        }
        assertEquals(23, testStream.getSize());
    }

    @Test
    public void testCountReadBufferComplicated() throws IOException {
        final ByteArrayInputStream b = new ByteArrayInputStream(new byte[23]);
        final TestSizeAwareInputStream testStream = new TestSizeAwareInputStream(b);
        while (testStream.read(new byte[5], 2, 2) != -1) {
        }
        assertEquals(23, testStream.getSize());

    }

    private static class TestSizeAwareInputStream extends SizeAwareInputStream {

        private long size;

        public TestSizeAwareInputStream(final InputStream delegate) {
            super(delegate);
        }

        @Override
        public void size(final long size) {
            this.size = size;
        }

        public long getSize() {
            return size;
        }
    }
}
