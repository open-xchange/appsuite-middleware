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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import static org.junit.Assert.*;

import java.util.List;

import org.junit.Test;

import com.openexchange.groupware.importexport.csv.CSVParser;
import com.openexchange.groupware.importexport.exceptions.ImportExportException;

import junit.framework.JUnit4TestAdapter;

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
	private static final String UNEVEN_TEST = "title1, title 2\ncontent 1\ncontent1, content2,content3";
	private List< List<String> > result;
	private CSVParser parser;
	
	@Test public void getLines(){
		parser = new CSVParser(UNESCAPED_TEST );
		assertEquals("title 1, title 2, title3",parser.getLine(0));
		assertEquals("content 1, content2, content3",parser.getLine(1));
		
		parser = new CSVParser(UNEVEN_TEST );
		assertEquals("title1, title 2",parser.getLine(0));
		assertEquals("content 1",parser.getLine(1));
		assertEquals("content1, content2,content3",parser.getLine(2));
	}
	
	@Test public void parseUnescaped() throws ImportExportException{
		doAsserts(UNESCAPED_TEST + '\n' , "Un-escaped with final linebreak",2,3);
		doAsserts(UNESCAPED_TEST, "Un-escaped without final linebreak",2,3);
	}
	
	@Test public void parseEscaped() throws ImportExportException{
		doAsserts(ESCAPED_TEST + '\n' , "Escaped with final linebreak",2,3);
		doAsserts(ESCAPED_TEST, "Escaped without final linebreak",2,3);
	}
	
	@Test public void parseMixed() throws ImportExportException{
		doAsserts(UNESCAPED_TEST + '\n' + ESCAPED_TEST , "Un-escaped with final linebreak",4,3);
		doAsserts(UNESCAPED_TEST + '\n' + ESCAPED_TEST + '\n', "Un-escaped without final linebreak",4,3);
	}
	
	@Test public void parseBugged() throws ImportExportException{
		parser = new CSVParser(UNEVEN_TEST );
		parser.parse();
		List<Integer> unparsableLines = parser.getUnparsableLineNumbers();
		assertEquals("Should have two unparsable lines", unparsableLines.size(), 2);
		assertEquals("First wrong line" , "content 1", parser.getLine(unparsableLines.get(0)));
		assertEquals("Second wrong line" , "content1, content2,content3", parser.getLine(unparsableLines.get(1)));
		
	}
	
	protected void doAsserts(String line, String comment, int lines, int cells) throws ImportExportException{
		parser = new CSVParser(line);
		result = parser.parse();
		assertEquals(comment + ":: Number of lines",result.size(), lines);
		for(List<String> currList : result){
			assertEquals(comment + ":: Number of cells", currList.size() , cells);
		}
	}
}
