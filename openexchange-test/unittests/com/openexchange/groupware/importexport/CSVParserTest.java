/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
 *
 */

package com.openexchange.groupware.importexport;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.util.List;
import junit.framework.JUnit4TestAdapter;
import org.junit.Test;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.importexport.csv.CSVParser;

/**
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 *
 */
public class CSVParserTest {
	public static junit.framework.Test suite() {
		return new JUnit4TestAdapter(CSVParserTest.class);
	}

	private static final String UNESCAPED_TEST = "title 1, title 2, title3\ncontent 1, content2, content3";
	private static final String ESCAPED_TEST = "\"title 1\", \"title 2\", \"title3\"\r\n\"content \"\"1\"\"\", \"content,2\", \"content\n3\"";
	private static final String UNEVEN_TEST = "title1, title 2\ncontent 11\ncontent21, content22,content23";
	private List< List<String> > result;
	private CSVParser parser;

	@Test public void getLines(){
		parser = new CSVParser(UNESCAPED_TEST );
		assertEquals("title 1, title 2, title3",parser.getLine(0));
		assertEquals("content 1, content2, content3",parser.getLine(1));

		parser = new CSVParser(UNEVEN_TEST );
		assertEquals("title1, title 2",parser.getLine(0));
		assertEquals("content 11",parser.getLine(1));
		assertEquals("content21, content22,content23",parser.getLine(2));
	}

	 @Test public void parseUnescaped() throws OXException{
		doAsserts(UNESCAPED_TEST + '\n' , "Un-escaped with final linebreak",2,3, false);
		doAsserts(UNESCAPED_TEST, "Un-escaped without final linebreak",2,3, false);
	}

	 @Test public void parseEscaped() throws OXException{
		doAsserts(ESCAPED_TEST + '\n' , "Escaped with final linebreak",2,3, false);
		doAsserts(ESCAPED_TEST, "Escaped without final linebreak",2,3, false);
	}

	 @Test public void parseMixed() throws OXException{
		doAsserts(UNESCAPED_TEST + '\n' + ESCAPED_TEST , "Un-escaped with final linebreak",4,3, false);
		doAsserts(UNESCAPED_TEST + '\n' + ESCAPED_TEST + '\n', "Un-escaped without final linebreak",4,3, false);
	}

	 @Test public void parseBuggedIntolerant() {
		parser = new CSVParser(UNEVEN_TEST );
		parser.setTolerant(false);
		try {
			parser.parse();
		} catch (final OXException e){
			assertTrue("Exception caught" , true);
			assertEquals("Correct exception thrown" , "CSV-1000" , e.getErrorCode());
			return;
		}
		fail("Unparsable CSV given, but no exception thrown!");
	}

	 @Test public void parseBuggedIntolerant2() {
		final String bla = "1\n2,3";
		parser = new CSVParser(bla );
		parser.setTolerant(false);
		try {
			parser.parse();
		} catch (final OXException e){
			assertTrue("Exception caught" , true);
			assertEquals("Correct exception thrown" , "CSV-1000" , e.getErrorCode());
			return;
		}
		fail("Unparsable CSV given, but no exception thrown!");
	}

	@Test public void parseBuggedTolerant() throws OXException {
		final List<List<String>> result = doAsserts(UNEVEN_TEST , "Bugged lines with tolerant parser", 3, 2, true);
		assertEquals("checking last element" , "content22" , result.get(2).get(1));
	}

	@Test public void umlauts() throws OXException {
		final String umlaut = "\u00dcmlaut title\nSonder\u00dfeichen cell";
		final List<List<String>> result = doAsserts(umlaut, "Checking umlauts", 2, 1, false);
		assertEquals("\u00dc in title", "\u00dcmlaut title" , result.get(0).get(0));
		assertEquals("\u00df in cell", "Sonder\u00dfeichen cell" , result.get(1).get(0));
	}

	protected List<List<String>> doAsserts(final String line, final String comment, final int lines, final int cells, final boolean isTolerant) throws OXException{
		parser = new CSVParser(line);
		parser.setTolerant(isTolerant);
		result = parser.parse();
		assertEquals(comment + ":: Number of lines", lines, result.size());
		for(final List<String> currList : result){
			assertEquals(comment + ":: Number of cells", cells, currList.size());
		}
		return result;
	}
}
