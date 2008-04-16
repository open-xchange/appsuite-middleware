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

import com.openexchange.groupware.EnumComponent;
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
 * it would not be right, but the behaviour can be switched to be 
 * strict or not.
 * 
 * Note: See also RFC 4180
 * 
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 *
 */

@OXExceptionSource(
	classId=ImportExportExceptionClasses.CSVPARSER, 
	component=EnumComponent.IMPORT_EXPORT)
@OXThrowsMultiple(
	category={Category.USER_INPUT, Category.CODE_ERROR}, 
	desc={"",""}, 
	exceptionId={0,1}, 
	msg={
		"Broken CSV file: Lines have different number of cells, line #1 has %d, line #%d has %d. Is this really a CSV file?",
		"Illegal state: Found data after presumed last line."})
public class CSVParser {
	private static final ImportExportExceptionFactory EXCEPTIONS = new ImportExportExceptionFactory(CSVParser.class);
	private boolean isEscaping;
	protected boolean isTolerant;
	public static final char LINE_DELIMITER = '\n';
	public static final char CELL_DELIMITER = ',';
	public static final char ESCAPER = '"';
	public static final int STARTING_LENGTH = -1;
	private String file;
	private int numberOfCells, currentLineNumber;
	private StringBuilder currentCell = new StringBuilder();
	private List<String> currentLine = new LinkedList<String>();
	private List< List<String> > structure = new LinkedList<List <String> >();
	private int pointer;
	private char[] fileAsArray;
	
	public CSVParser(final String file){
		this();
		this.file = file;
	}
	
	public CSVParser(){
		isTolerant = false;
		numberOfCells = STARTING_LENGTH;
		currentLineNumber = 0;
		currentCell = new StringBuilder();
		currentLine = new LinkedList<String>();
		structure = new LinkedList<List <String> >();
		
	}
	
	public boolean isTolerant() {
		return isTolerant;
	}

	/**
	 * Sets the parser to behave tolerant to broken CSV formats
	 * @param isTolerant
	 */
	public void setTolerant(final boolean isTolerant) {
		this.isTolerant = isTolerant;
	}

	/**
	 * Convenience method, combines setContent() and parse().
	 * 
	 * @param str - CSV to be parsed
	 * @return
	 * @throws ImportExportException
	 */
	public List< List<String> > parse(final String str) throws ImportExportException{
		this.file = str;
		return parse();
	}
	
	public List< List<String> > parse() throws ImportExportException{
		if(file == null){
			return null;
		}
		file = wellform(file);
		//converting to char array to make it iterable
		fileAsArray = file.toCharArray();
		//preparations
		
		
		pointer = 0;
		for(; pointer < fileAsArray.length; pointer++){
			switch(fileAsArray[pointer]){
				case LINE_DELIMITER: 
					handleLineDelimiter();
				break;
				
				case ESCAPER: 
					handleEscaping();
				break;
				
				case CELL_DELIMITER:
					handleCellDelimiter();
				break;
				
				default:
					handleDefault();
				break;
			}
		}
		
		if( ! (currentCell.length() == 0 && currentLine.size() == 0) ){
			throw EXCEPTIONS.create(1);
		}
		
		return structure;
	}
	
	protected void handleDefault() {
		currentCell.append(fileAsArray[pointer]);
	}

	protected void handleCellDelimiter() throws ImportExportException {
		if(isEscaping){
			currentCell.append(CELL_DELIMITER);
		} else {
			if( (numberOfCells == STARTING_LENGTH) ||
				(numberOfCells != STARTING_LENGTH && currentLine.size() < numberOfCells)){
				currentLine.add( currentCell.toString().trim() );
				currentCell = new StringBuilder();
			} else {
				if(! isTolerant()){
					throw EXCEPTIONS.create(0);
				}
			}
		}
	}

	protected void handleEscaping() {
		if(isEscaping){
			if( (pointer+1) < fileAsArray.length && fileAsArray[pointer+1] == ESCAPER){
				currentCell.append(ESCAPER);
				pointer++;
			} else {
				isEscaping = false;
			}
		} else {
			isEscaping = true;
		}
	}

	protected void handleLineDelimiter() throws ImportExportException {
		if(isEscaping){
			currentCell.append(LINE_DELIMITER);
		} else {
			currentLineNumber++;
			if(currentLine.size() < numberOfCells || numberOfCells == STARTING_LENGTH){
				currentLine.add( currentCell.toString().trim() );
			} else {
				if(! isTolerant() ) {
					throw EXCEPTIONS.create(0, Integer.valueOf(numberOfCells), Integer.valueOf(currentLineNumber), Integer.valueOf(currentLine.size()));
				}
			}
			currentCell = new StringBuilder();
			if(numberOfCells == STARTING_LENGTH ){
				numberOfCells = currentLine.size();
				structure.add(currentLine);
			} else if(numberOfCells != currentLine.size() && !isTolerant() ){
				throw EXCEPTIONS.create(0, Integer.valueOf(numberOfCells), Integer.valueOf(currentLineNumber), Integer.valueOf(currentLine.size()));
				//unparsableLines.add(currentLineNumber-1);
			} else {
				for(int j = currentLine.size(); j < numberOfCells; j++){
					currentLine.add("");
				}
				structure.add(currentLine);
			}
			currentLine = new LinkedList<String>();
		}
	}

	public void setFileContent(final String content){
		this.file = content;
	}
	
	/**
	 * Returns a line from the CSV file given.
	 * Starts counting at 0.
	 */
	public String getLine(final int lineNumber){
		file = wellform(file);
		return file.split("\n")[lineNumber];
	}
	
	protected String wellform(String str){
		//changing all possible formats (Mac, DOS) to Unix
		str = str.replace("\r\n", "\n").replace("\r", "\n");
		//adding ending to create well-formed file
		if(! str.endsWith("\n")){
			str = str + "\n";
		}
		return str;
	}
	
}
