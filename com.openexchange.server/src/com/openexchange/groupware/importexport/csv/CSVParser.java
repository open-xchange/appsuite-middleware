/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 * 
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
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
