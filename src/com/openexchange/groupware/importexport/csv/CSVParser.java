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

package com.openexchange.groupware.importexport.csv;

import java.util.LinkedList;
import java.util.List;

import com.openexchange.groupware.Component;
import com.openexchange.groupware.OXExceptionSource;
import com.openexchange.groupware.OXThrowsMultiple;
import com.openexchange.groupware.AbstractOXException.Category;
import com.openexchange.groupware.importexport.exceptions.ImportExportException;
import com.openexchange.groupware.importexport.exceptions.ImportExportExceptionClasses;
import com.openexchange.groupware.importexport.exceptions.ImportExportExceptionFactory;

/**
 * This class represents a combined parser and lexer for CSV files.
 * It is designed rather simple with speed in mind. 
 * 
 * Note: Proper CSV files should have the dimensions M x N. If this
 * parser encounters a line that has not as many columns as the others,
 * it will not parse it but add it to a list you can access via
 * getUnparsableLineNumbers() 
 * 
 * Note: See also RFC 4180
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 *
 */

@OXExceptionSource(
	classId=ImportExportExceptionClasses.CSVPARSER, 
	component=Component.IMPORT_EXPORT)
@OXThrowsMultiple(
	category={Category.USER_INPUT, Category.PROGRAMMING_ERROR}, 
	desc={"",""}, 
	exceptionId={0,1}, 
	msg={
		"Broken CSV file: Lines have different number of cells, line #1 has %d, line #%d has %d",
		"Illegal state: Found data after presumed last line."})
public class CSVParser {
	private static final ImportExportExceptionFactory EXCEPTIONS = new ImportExportExceptionFactory(CSVParser.class);
	private static boolean isEscaping;
	public static final char LINE_DELIMITER = '\n';
	public static final char CELL_DELIMITER = ',';
	public static final char ESCAPER = '"';
	private List<Integer> unparsableLines;
	private String file;
	
	public CSVParser(String file){
		this();
		this.file = file;
	}
	
	public CSVParser(){
		this.unparsableLines = new LinkedList<Integer>();
	}

	public List< List<String> > parse(String str) throws ImportExportException{
		this.file = str;
		this.unparsableLines = new LinkedList<Integer>();
		return parse();
	}
	
	public List< List<String> > parse() throws ImportExportException{
		if(file == null){
			return null;
		}
		//changing all possible formats (Mac, DOS) to Unix
		file = file.replace("\r\n", "\n").replace("\r", "\n");
		//adding ending to create well-formed file
		if(! file.endsWith("\n")){
			file = file + "\n";
		}
		//converting to char array to make it iterable
		char[] arr = file.toCharArray();
		//preparations
		int numberOfCells = -1, currentLineNumber = 0;
		StringBuilder currentCell = new StringBuilder();
		List<String> currentLine = new LinkedList<String>();
		List< List<String> > structure = new LinkedList<List <String> >();
		
		
		for(int i = 0; i < arr.length; i++){
			switch(arr[i]){
				case LINE_DELIMITER: 
					if(isEscaping){
						currentCell.append(LINE_DELIMITER);
					} else {
						currentLineNumber++;
						currentLine.add( currentCell.toString().trim() );
						currentCell = new StringBuilder();
						if(numberOfCells == -1 ){
							numberOfCells = currentLine.size();
							structure.add(currentLine);
						} else if(numberOfCells != currentLine.size() ){
							//throw EXCEPTIONS.create(0, numberOfCells, currentLineNumber, currentLine.size());
							unparsableLines.add(currentLineNumber);
						} else {
							structure.add(currentLine);
						}
						currentLine = new LinkedList<String>();
					}
				break;
				
				case ESCAPER: 
					if(isEscaping){
						if( (i+1) < arr.length && arr[i+1] == ESCAPER){
							currentCell.append(ESCAPER);
							i++;
						} else {
							isEscaping = false;
						}
					} else {
						isEscaping = true;
					}
				break;
				
				case CELL_DELIMITER:
					if(isEscaping){
						currentCell.append(CELL_DELIMITER);
					} else {
						currentLine.add( currentCell.toString().trim() );
						currentCell = new StringBuilder();
					}
				break;
				
				default:
					currentCell.append(arr[i]);
				break;
			}
		}
		
		if( ! (currentCell.length() == 0 && currentLine.size() == 0) ){
			throw EXCEPTIONS.create(1);
		}
		
		return structure;
	}
	
	public List<Integer> getUnparsableLineNumbers(){
		return this.unparsableLines;
	}
	
	public void setFileContent(String content){
		this.file = content;
	}
}
