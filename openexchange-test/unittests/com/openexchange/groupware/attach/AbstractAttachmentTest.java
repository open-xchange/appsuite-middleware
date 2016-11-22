
package com.openexchange.groupware.attach;

import org.junit.After;
import org.junit.Before;
import com.openexchange.groupware.Init;

public abstract class AbstractAttachmentTest {

    private Mode mode;

    @Before
    public void setUp() throws Exception {
        mode().setUp();
    }

    @After
    public void tearDown() throws Exception {
        mode().tearDown();
    }

    protected Mode mode() {
        if (mode == null) {
            mode = getMode();
        }
        return mode;
    }

    public abstract Mode getMode();

    public static interface Mode {

        public void setUp() throws Exception;

        @After
        public void tearDown() throws Exception;
    }

    public static class INTEGRATION implements Mode {

        @Override
        public void setUp() throws Exception {
            Init.startServer();
        }

        @After
        public void tearDown() throws Exception {
            Init.stopServer();
        }
    }

    public static class ISOLATION implements Mode {

        @Override
        public void setUp() throws Exception {}

        @After
        public void tearDown() throws Exception {}
    }
}
