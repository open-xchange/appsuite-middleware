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


package org.apache.jsieve.parser;

import org.apache.jsieve.ScriptCoordinate;
import org.apache.jsieve.parser.generated.Token;


/**
 * Class SieveNode defines aspects all jjTree parse nodes may require.
 *
 * Creation Date: 27-Jan-04
 */
public class SieveNode
{

    /**
     * Constructor for SieveNode.
     */
    public SieveNode()
    {
        super();
    }

    private Token firstToken;
    private Token lastToken;

    /**
     * The name associated to this node or null
     */
    private String fieldName;

    /**
     * The value associated to this node or null
     */
    private Object fieldValue;
    /**
     * Returns the name.
     * @return String
     */
    public String getName()
    {
        return fieldName;
    }

    /**
     * Returns the value.
     * @return Object
     */
    public Object getValue()
    {
        return fieldValue;
    }

    /**
     * Sets the name.
     * @param name The name to set
     */
    public void setName(String name)
    {
        fieldName = name;
    }

    /**
     * Sets the value.
     * @param value The value to set
     */
    public void setValue(Object value)
    {
        fieldValue = value;
    }

    /**
     * Gets the first token comprising this node.
     * @return <code>Token</code>, not null
     */
    public Token getFirstToken() {
        return firstToken;
    }

    /**
     * Sets the first token comprising this node.
     * @param firstToken <code>Token</code>, not null
     */
    public void setFirstToken(Token firstToken) {
        this.firstToken = firstToken;
    }

    /**
     * Gets the last token comprising this node.
     * @return <code>Token</code>, not null
     */
    public Token getLastToken() {
        return lastToken;
    }

    /**
     * Sets the last token comprising this node.
     * @param lastToken <code>Token</code>, not null
     */
    public void setLastToken(Token lastToken) {
        this.lastToken = lastToken;
    }

    /**
     * Gets the position of this node in the script.
     * @return <code>ScriptCoordinate</code> containing the position of this node,
     * not null
     */
    public ScriptCoordinate getCoordinate() {
        final int lastColumn = lastToken.endColumn;
        final int lastList = lastToken.endLine;
        final int firstColumn = firstToken.beginColumn;
        final int firstLine = firstToken.beginLine;
        final ScriptCoordinate scriptCoordinate = new ScriptCoordinate(firstLine, firstColumn, lastList, lastColumn);
        return scriptCoordinate;
    }
}
