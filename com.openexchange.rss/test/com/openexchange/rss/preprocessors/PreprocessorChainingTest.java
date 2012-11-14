package com.openexchange.rss.preprocessors;

import junit.framework.TestCase;

public class PreprocessorChainingTest extends TestCase {
	
	class Pre1 extends AbstractPreprocessor {
		@Override
		public String process2(String payload) {
			return payload + "A";
		}		
	}
	
	class Pre2 extends AbstractPreprocessor {
		@Override
		public String process2(String payload) {
			return payload + "B";
		}		
	}
	
	class Pre3 extends AbstractPreprocessor {
		@Override
		public String process2(String payload) {
			return payload + "C";
		}		
	}
	
	public void testChaining(){
		String actual = new Pre1().chain(new Pre2().chain(new Pre3())).process("");
		assertEquals("ABC", actual);
	}
	
	public void testNoChain(){
		String actual = new Pre1().process("");
		assertEquals("A", actual);
	}
	
}
