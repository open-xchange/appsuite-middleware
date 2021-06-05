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

package com.openexchange.mail.filter.json.v2.json.mapper.parser.test;

import java.util.ArrayList;
import java.util.List;
import com.openexchange.jsieve.commands.MatchType;
import com.openexchange.mail.filter.json.v2.mapper.ArgumentUtil;

/**
 * {@link StartsOrEndsWithMatcherUtil}
 *
 * @author <a href="mailto:kevin.ruthmann@open-xchange.com">Kevin Ruthmann</a>
 * @since v7.8.4
 */
public class StartsOrEndsWithMatcherUtil {

    private static final String WILDCARD="*";

    /**
     * Tests if the given matchType is a simplified matcher
     * @param matchType The name of the {@link MatchType}
     * @return true if it is a simplified {@link MatchType}, false otherwise
     */
    public static boolean isSimplifiedMatcher(String matchType){
        return matchType.equals(MatchType.startswith.name()) || matchType.equals(MatchType.endswith.name());
    }

    /**
     * Inserts the matches {@link MatchType} into the given argument list
     * @param argList The argument List
     */
    public static void insertMatchesMatcher(List<Object> argList) {
        argList.add(ArgumentUtil.createTagArgument(MatchType.matches.name()));
    }

    /**
     * Inserts a values list into the argument list with wildcards depending on the given {@link MatchType} name
     * @param list The values to insert
     * @param normalizedMatchType The normalized {@link MatchType} name
     * @param argList The argument list
     */
    public static void insertValuesArgumentWithWildcards(List<String> list, String normalizedMatchType, List<Object> argList) {
        argList.add(replaceValueList(list, normalizedMatchType.equals(MatchType.endswith.name())));
    }

    /**
     * Add wildcards to the given values
     *
     * @param values The values
     * @param endsWith Defines whether the wildcards should be added at the beginning or the end of the string
     * @return The changed values
     */
    private static List<String> replaceValueList(List<String> values, boolean endsWith){

        List<String> result = new ArrayList<>(values.size());
        if (endsWith) {
            for (String str : values) {
                result.add(WILDCARD + str);
            }
        } else {
            for (String str : values) {
                result.add(str + WILDCARD);
            }
        }

        return result;
    }

    /**
     * Checks the given {@link MatchType} and values if they can be handles as a simplified {@link MatchType}
     * @param matchType The {@link MatchType}
     * @param values The values
     * @return The proper {@link MatchType} for the given input. Either a simplified {@link MatchType} or the given {@link MatchType}.
     */
    public static MatchType checkMatchType(MatchType matchType, List<String> values){

        if (matchType == null || !matchType.equals(MatchType.matches)){
            return matchType;
        }

       boolean startswith = false;
       boolean endswith = false;

        for(String value: values){
            if (value.startsWith(WILDCARD) && startswith==false){
                endswith=true;
            } else if (value.endsWith(WILDCARD) && endswith==false) {
                startswith=true;
            } else {
                return matchType;
            }
        }

        return endswith ? MatchType.endswith : MatchType.startswith;
    }

    /**
     * Checks if the given {@link MatchType} is a simplified {@link MatchType} and manipulates the values accordingly. E.g. removes leading wildcards for the startswith {@link MatchType}.
     *
     * @param values The values
     * @param matchType The {@link MatchType}
     * @return The adjusted values
     */
    public static List<String> retrieveListForMatchType(List<String> values, MatchType matchType){
        if (matchType==null){
            return values;
        }
        List<String> result;
        switch (matchType){
            case startswith:
                result = new ArrayList<>(values.size());
                for (String str : values) {
                    result.add(str.substring(0,str.length()-1));

                }
                return result;
            case endswith:
                result = new ArrayList<>(values.size());
                for (String str : values) {
                    result.add(str.substring(1));
                }
                return result;
            default:
                return values;
        }
    }

}
