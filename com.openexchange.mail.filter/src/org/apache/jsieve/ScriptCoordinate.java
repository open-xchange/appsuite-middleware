/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/

package org.apache.jsieve;

import org.apache.commons.logging.Log;
import com.openexchange.log.LogFactory;

/**
 * Specifies the positional extent of an element
 * within the script being executed.
 * In other words, this gives the line and column at which
 * the elment starts and at which it ends.
 */
public final class ScriptCoordinate {

    private static Log LOG = com.openexchange.log.Log.valueOf(LogFactory.getLog(ScriptCoordinate.class));

    private final int startLineNumber;
    private final int startColumnNumber;
    private final int endLineNumber;
    private final int endColumnNumber;

    public ScriptCoordinate(final int startLineNumber, final int startColumnNumber,
            final int endLineNumber, final int endColumnNumber) {
        super();
        this.startLineNumber = startLineNumber;
        this.startColumnNumber = startColumnNumber;
        this.endLineNumber = endLineNumber;
        this.endColumnNumber = endColumnNumber;
    }

    /**
     * Gets the number of the column where the elements ends.
     * @return column number
     */
    public int getEndColumnNumber() {
        return endColumnNumber;
    }

    /**
     * Gets the number of the line where the element ends.
     * @return line number
     */
    public int getEndLineNumber() {
        return endLineNumber;
    }

    /**
     * Gets the number of the column where the element start.
     * @return column number
     */
    public int getStartColumnNumber() {
        return startColumnNumber;
    }

    /**
     * Gets the number of the line where the element starts.
     * @return line number
     */
    public int getStartLineNumber() {
        return startLineNumber;
    }

    /**
     * Creates a syntax exception based on the given message
     * containing details of the script position.
     * The message should end with a full stop.
     * @param message <code>CharSequence</code> containing the base message,
     * not null
     * @return <code>SyntaxException</code> with details of the script position
     * appended to the message, not null
     */
    public SyntaxException syntaxException(CharSequence message) {
        if (LOG.isWarnEnabled()) {
            LOG.warn(message);
        }
        logDiagnosticsInfo(LOG);
        final String fullMessage = addStartLineAndColumn(message);
        final SyntaxException result = new SyntaxException(fullMessage);
        return result;
    }


//    /**
//     * Creates a command exception based on the given message
//     * containing details of the script position.
//     * The message should end with a full stop.
//     * @param message <code>CharSequence</code> containing the base message,
//     * not null
//     * @return <code>CommandException</code> with details of the script position
//     * appended to the message, not null
//     */
//    public CommandException commandException(CharSequence message) {
//        if (LOG.isWarnEnabled()) {
//            LOG.warn(message);
//        }
//        logDiagnosticsInfo(LOG);
//        final String fullMessage = addStartLineAndColumn(message);
//        final CommandException result = new CommandException(fullMessage);
//        return result;
//    }

    /**
     * Appends a standard position phrase to the given message.
     * This message should end with a full stop.
     * @param message <code>CharSequence</code> message, not null
     * @return <code>String</code> containing the original message
     * with positional phrase appended, not null
     */
    public String addStartLineAndColumn(CharSequence message) {
        final StringBuffer buffer;
        if (message instanceof StringBuffer) {
            buffer = (StringBuffer) message;
        } else {
            buffer = new StringBuffer(message);
        }
        buffer.append(" Line ");
        buffer.append(startLineNumber);
        buffer.append(" column ");
        buffer.append(startColumnNumber);
        buffer.append('.');
        return buffer.toString();
    }

    /**
     * Logs diagnotic information about the script coordinate.
     * @param logger <code>Log</code>, not null
     */
    public void logDiagnosticsInfo(Log logger) {
        if (logger.isInfoEnabled()) {
            logger.info("Expression starts line " + startLineNumber + " column " + startColumnNumber);
            logger.info("Expression ends line " + endLineNumber + " column " + endColumnNumber);
        }
    }

    /**
     * Logs diagnotic information about the script coordinate.
     * @param logger <code>Log</code>, not null
     */
    public void debugDiagnostics(Log logger) {
        if (logger.isDebugEnabled()) {
            logger.debug("Expression starts line " + startLineNumber + " column " + startColumnNumber);
            logger.debug("Expression ends line " + endLineNumber + " column " + endColumnNumber);
        }
    }
}
