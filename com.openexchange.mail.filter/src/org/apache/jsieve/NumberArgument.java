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

import org.apache.jsieve.parser.generated.Token;

/**
 * <p>A parsed representation of the RFC3028 BNF...</p>
 *
 * <code>1*DIGIT [QUANTIFIER]</code>
 *
 * <p>Note that the stored value is the absolute value after applying the quantifier.
 * </p>
 *
 */
public class NumberArgument implements Argument
{

    /**
     * The absolute value of the number after applying the quentifier.
     */
    private Integer fieldValue;

    /**
     * Constructor for NumberArgument.
     */
    private NumberArgument()
    {
        super();
    }

    /**
     * Constructor for NumberArgument.
     * @param token
     */
    public NumberArgument(Token token)
    {
        this();
        setValue(token);
    }

    /**
     * Sets the value of the reciver to an Integer.
     * @param number The value to set
     */
    protected void setValue(Integer number)
    {
        fieldValue = number;
    }

    /**
     * @see org.apache.jsieve.Argument#getValue()
     */
    @Override
    public Object getValue()
    {
        return fieldValue;
    }

    /**
     * Method getInteger answers the value of the receiver as an Integer.
     * @return Integer
     */
    public Integer getInteger()
    {
        return fieldValue;
    }

    /**
     * Sets the value of the receiver from a Token.
     * @param aToken The Token from which to extract the value to set
     */
    protected void setValue(Token aToken)
    {
        int endIndex = aToken.image.length();
        int magnitude = 1;
        if (aToken.image.endsWith("K"))
        {
            magnitude = 1024;
            endIndex--;
        }
        else if (aToken.image.endsWith("M"))
        {
            magnitude = 1048576;
            endIndex--;
        }
        else if (aToken.image.endsWith("G"))
        {
            magnitude = 1073741824;
            endIndex--;
        }

        setValue(
            new Integer(
                Integer.parseInt(aToken.image.substring(0, endIndex))
                    * magnitude));
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return (getValue() == null) ? "null" : getValue().toString();
    }

}
