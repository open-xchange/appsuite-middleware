package com.openexchange.groupware.attach;

import com.openexchange.groupware.Init;

import junit.framework.TestCase;

public abstract class AbstractAttachmentTest extends TestCase {
	private Mode mode;

	public void setUp() throws Exception {
		  super.setUp();
		  mode().setUp();
	}
	
	protected Mode mode(){
		if(mode==null)
			mode = getMode();
		return mode;
	}

	public abstract Mode getMode();
	
	public static interface Mode {
		public void setUp() throws Exception;
	}
	
	
	public static class INTEGRATION implements Mode {

		public void setUp() throws Exception {
	        Init.initDB();
		}
		
	}
	
	public static class ISOLATION implements Mode {
		public void setUp() throws Exception {
	    }
	}
}
