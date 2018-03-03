package com.openexchange.data.conversion.ical.ical4j;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import java.util.TimeZone;
import org.junit.Test;
import com.openexchange.data.conversion.ical.ical4j.internal.ParserTools;


public class ParserToolsTest {	

	     @Test
     public void testFindingOutlookTimezone(){
		String outlookTZID = "Amsterdam, Berlin, Bern, Rom, Stockholm, Wien";
		TimeZone actual = ParserTools.findTzidBySimilarity(outlookTZID);
		String id = actual.getID();
		assertTrue(id.contains("Europe/"));
		assertEquals(60*60*1000, actual.getRawOffset());
	}
	
	     @Test
     public void testAmbivalentTerm(){
		String outlookTZID = "Europe";
		TimeZone actual = ParserTools.findTzidBySimilarity(outlookTZID);
		assertTrue(actual.getID().startsWith("Europe/"));
	}
	
	     @Test
     public void testEuropeBerlin(){
		assertEquals("Europe/Berlin", ParserTools.findTzidBySimilarity("Europe/Berlin").getID());
	}
	
	     @Test
     public void testStandards(){
		assertEquals("GMT", ParserTools.findTzidBySimilarity("GMT").getID());
		assertEquals("Zulu", ParserTools.findTzidBySimilarity("Zulu").getID());
		assertEquals("Zulu", ParserTools.findTzidBySimilarity("Z").getID());
		assertEquals("UTC", ParserTools.findTzidBySimilarity("UTC").getID());
	}
	
	
	     @Test
     public void testNotMatchingOne(){
		String outlookTZID = "doesntexistatall";
		TimeZone actual = ParserTools.findTzidBySimilarity(outlookTZID);
		assertNull(actual);
	}

}
