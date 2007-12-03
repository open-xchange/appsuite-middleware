package com.openexchange.groupware.attach;

import com.openexchange.groupware.Init;

import junit.framework.TestCase;

public abstract class AbstractAttachmentTest extends TestCase {
	private Mode mode;

	@Override
    public void setUp() throws Exception {
		  super.setUp();
		  mode().setUp();
	}
	
    @Override
    protected void tearDown() throws Exception {
        mode().tearDown();
        super.tearDown();
    }

    protected Mode mode(){
		if(mode==null)
			mode = getMode();
		return mode;
	}

	public abstract Mode getMode();
	
	public static interface Mode {
		public void setUp() throws Exception;
        public void tearDown() throws Exception;
	}
	
	
	public static class INTEGRATION implements Mode {
		public void setUp() throws Exception {
	        Init.startServer();
		}
        public void tearDown() throws Exception {
            Init.stopServer();
        }
	}
	
	public static class ISOLATION implements Mode {
		public void setUp() throws Exception {
	    }
        public void tearDown() throws Exception {
        }
	}
}
