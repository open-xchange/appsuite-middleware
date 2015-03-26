package com.openexchange.rss.preprocessors;

import com.openexchange.rss.RssResult;
import junit.framework.TestCase;

public class PreprocessorChainingTest extends TestCase {

	class Pre1 extends AbstractPreprocessor {
		@Override
		public String innerProcess(String payload, RssResult rssResult) {
			return payload + "A";
		}
	}

	class Pre2 extends AbstractPreprocessor {
		@Override
		public String innerProcess(String payload, RssResult rssResult) {
			return payload + "B";
		}
	}

	class Pre3 extends AbstractPreprocessor {
		@Override
		public String innerProcess(String payload, RssResult rssResult) {
			return payload + "C";
		}
	}

	public void testChaining(){
		String actual = new Pre1().chain(new Pre2().chain(new Pre3())).process("", null);
		assertEquals("ABC", actual);
	}

	public void testNoChain(){
		String actual = new Pre1().process("", null);
		assertEquals("A", actual);
	}

}
