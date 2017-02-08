package com.openexchange.rss.preprocessors;

import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.rss.RssResult;
import static org.junit.Assert.*;

public class PreprocessorChainingTest {
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

	     @Test
     public void testChaining() throws OXException{
		String actual = new Pre1().chain(new Pre2().chain(new Pre3())).process("", null);
		assertEquals("ABC", actual);
	}

	     @Test
     public void testNoChain() throws OXException{
		String actual = new Pre1().process("", null);
		assertEquals("A", actual);
	}

}
