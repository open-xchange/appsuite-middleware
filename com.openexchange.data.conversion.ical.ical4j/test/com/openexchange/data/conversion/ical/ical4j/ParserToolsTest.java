package com.openexchange.data.conversion.ical.ical4j;
import java.util.TimeZone;

import junit.framework.TestCase;

import com.openexchange.data.conversion.ical.ical4j.internal.ParserTools;


public class ParserToolsTest extends TestCase {
	

	public void testFindingOutlookTimezone(){
		String outlookTZID = "Amsterdam, Berlin, Bern, Rom, Stockholm, Wien";
		TimeZone actual = ParserTools.findTzidBySimilarity(outlookTZID);
		String id = actual.getID();
		assertTrue(id.contains("Europe/"));
		assertEquals(60*60*1000, actual.getRawOffset());
	}
	
	public void testAmbivalentTerm(){
		String outlookTZID = "Europe";
		TimeZone actual = ParserTools.findTzidBySimilarity(outlookTZID);
		assertTrue(actual.getID().startsWith("Europe/"));
	}
	
	public void testEuropeBerlin(){
		assertEquals("Europe/Berlin", ParserTools.findTzidBySimilarity("Europe/Berlin").getID());
	}
	
	public void testStandards(){
		assertEquals("GMT", ParserTools.findTzidBySimilarity("GMT").getID());
		assertEquals("Zulu", ParserTools.findTzidBySimilarity("Zulu").getID());
		assertEquals("Zulu", ParserTools.findTzidBySimilarity("Z").getID());
		assertEquals("UTC", ParserTools.findTzidBySimilarity("UTC").getID());
	}
	
	
	public void testNotMatchingOne(){
		String outlookTZID = "doesntexistatall";
		TimeZone actual = ParserTools.findTzidBySimilarity(outlookTZID);
		assertNull(actual);
	}

}
