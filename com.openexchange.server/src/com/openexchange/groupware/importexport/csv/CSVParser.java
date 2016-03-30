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

package com.openexchange.groupware.importexport.csv;

import static com.openexchange.java.Autoboxing.I;
import java.util.LinkedList;
import java.util.List;
import com.openexchange.exception.OXException;

/**
 * This class represents a combined parser and lexer for CSV files. It is designed rather simple with speed in mind. Note: Proper CSV files
 * should have the dimensions M x N. If this parser encounters a line that has not as many columns as the others, it would not be right, but
 * the behaviour can be switched to be strict or not. Note: See also RFC 4180
 *
 * @author <a href="mailto:tobias.prinz@open-xchange.com">Tobias 'Tierlieb' Prinz</a>
 */
public class CSVParser {

    public static final char LINE_DELIMITER = '\n';
    public static final char CELL_DELIMITER = ',';
    public static final char ESCAPER = '"';
    public static final int STARTING_LENGTH = -1;


    private boolean isEscaping;
    protected boolean isTolerant;
    private String file;
    private int numberOfCells, currentLineNumber;
    private StringBuilder currentCell = new StringBuilder();
    private List<String> currentLine = new LinkedList<String>();
    private List<List<String>> structure = new LinkedList<List<String>>();
    private int pointer;
    private char[] fileAsArray;
    private char cellDelimiter;

    public CSVParser(final String file) {
        this();
        this.file = file;
    }

    private void reset() {
    	numberOfCells = STARTING_LENGTH;
        currentLineNumber = 0;
        currentCell = new StringBuilder();
        currentLine = new LinkedList<String>();
        structure = new LinkedList<List<String>>();
    }

    public CSVParser() {
        cellDelimiter = CELL_DELIMITER;
        isTolerant = false;
        reset();
    }

    public boolean isTolerant() {
        return isTolerant;
    }

    /**
     * Sets the parser to behave tolerant to broken CSV formats
     *
     * @param isTolerant
     */
    public void setTolerant(final boolean isTolerant) {
        this.isTolerant = isTolerant;
    }

    /**
     * Sets the cell delimiter character to use.
     *
     * @param delimiter The delimiter
     */
    public void setCellDelimiter(char delimiter) {
        this.cellDelimiter = delimiter;
    }

    /**
     * Convenience method, combines setContent() and parse().
     *
     * @param str - CSV to be parsed
     * @return
     * @throws OXException
     */
    public List<List<String>> parse(final String str) throws OXException {
    	reset();
        this.file = str;
        return parse();
    }

    public List<List<String>> parse() throws OXException {
        if (file == null) {
            return null;
        }
        file = wellform(file);
        // converting to char array to make it iterable
        fileAsArray = file.toCharArray();
        // preparations

        pointer = 0;
        for (; pointer < fileAsArray.length; pointer++) {
            char c = fileAsArray[pointer];
            if (cellDelimiter == c) {
                handleCellDelimiter();
            } else if (LINE_DELIMITER == c) {
                handleLineDelimiter();
            } else if (ESCAPER == c) {
                handleEscaping();
            } else {
                handleDefault();
            }
        }

        if (!(currentCell.length() == 0 && currentLine.isEmpty())) {
            throw CsvExceptionCodes.DATA_AFTER_LAST_LINE.create();
        }

        return structure;
    }

    protected void handleDefault() {
        currentCell.append(fileAsArray[pointer]);
    }

    protected void handleCellDelimiter() throws OXException {
        if (isEscaping) {
            currentCell.append(cellDelimiter);
        } else {
            if ((numberOfCells == STARTING_LENGTH) || (numberOfCells != STARTING_LENGTH && currentLine.size() <= numberOfCells)) {
                currentLine.add(currentCell.toString().trim());
                currentCell = new StringBuilder();
            } else {
                if (!isTolerant()) {
                    throw CsvExceptionCodes.BROKEN_CSV.create(I(numberOfCells), I(currentLineNumber), I(currentLine.size()));
                }
            }
        }
    }

    protected void handleEscaping() {
        if (isEscaping) {
            if ((pointer + 1) < fileAsArray.length && fileAsArray[pointer + 1] == ESCAPER) {
                currentCell.append(ESCAPER);
                pointer++;
            } else {
                isEscaping = false;
            }
        } else {
            isEscaping = true;
        }
    }

    protected void handleLineDelimiter() throws OXException {
        if (isEscaping) {
            currentCell.append(LINE_DELIMITER);
        } else {
            currentLineNumber++;
            if (currentLine.size() < numberOfCells || numberOfCells == STARTING_LENGTH) {
                currentLine.add(currentCell.toString().trim());
            } else {
                if (!isTolerant()) {
                    throw CsvExceptionCodes.BROKEN_CSV.create(I(numberOfCells), I(currentLineNumber), I(currentLine.size()));
                }
            }
            currentCell = new StringBuilder();
            if (numberOfCells == STARTING_LENGTH) {
                numberOfCells = currentLine.size();
                structure.add(currentLine);
            } else if (numberOfCells == currentLine.size() || isTolerant()) {
                for (int j = currentLine.size(); j < numberOfCells; j++) {
                    currentLine.add("");
                }
                structure.add(currentLine);
            } else {
                throw CsvExceptionCodes.BROKEN_CSV.create(I(numberOfCells), I(currentLineNumber), I(currentLine.size()));
                // unparsableLines.add(currentLineNumber-1);
            }
            currentLine = new LinkedList<String>();
        }
    }

    public void setFileContent(final String content) {
        this.file = content;
    }

    /**
     * Returns a line from the CSV file given. Starts counting at 0.
     */
    public String getLine(final int lineNumber) {
        file = wellform(file);
        return file.split("\n")[lineNumber];
    }

    protected static String wellform(final String str) {
        // changing all possible formats (Mac, DOS) to Unix
        String retval = str.replace("\r\n", "\n").replace("\r", "\n");
        //removing excess whitespaces
        retval = retval.trim();
        // adding ending to create well-formed file
        if (!retval.endsWith("\n")) {
            retval = retval + "\n";
        }
        return retval;
    }

}
