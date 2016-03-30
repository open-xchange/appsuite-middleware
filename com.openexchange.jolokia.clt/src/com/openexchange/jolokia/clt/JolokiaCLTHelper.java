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

package com.openexchange.jolokia.clt;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.management.MalformedObjectNameException;
import org.apache.commons.cli.Option;
import org.jolokia.client.J4pClient;
import org.jolokia.client.exception.J4pException;
import org.jolokia.client.exception.J4pRemoteException;
import org.jolokia.client.request.J4pExecRequest;
import org.jolokia.client.request.J4pExecResponse;
import org.jolokia.client.request.J4pReadRequest;
import org.jolokia.client.request.J4pReadResponse;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * {@link JolokiaCLTHelper}
 * 
 * @author <a href="mailto:felix.marx@open-xchange.com">Felix Marx</a>
 */
public class JolokiaCLTHelper {

    protected static final String LINE_SEPARATOR = System.getProperty("line.separator");

    protected static String writeObject(Object o) {
        if (o instanceof JSONArray) {
            return writeJsonArray((JSONArray) o);
        } else if (o instanceof JSONObject) {
            return writeJsonObject((JSONObject) o);
        } else if (o instanceof String[]) {
            final String[] c = (String[]) o;
            return Arrays.toString(c);
        } else if (o instanceof long[]) {
            final long[] l = (long[]) o;
            return Arrays.toString(l);
        } else {
            return o.toString();
        }
    }

    protected static String writeJsonArray(JSONArray o) {
        final StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < o.size(); i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(o.get(i));
        }
        sb.append("]");
        return sb.toString();
    }

    protected static String writeJsonObject(JSONObject o) {
        final StringBuilder sb = new StringBuilder();
        Set<Map.Entry<String, ?>> entrySet = o.entrySet();
        sb.append("[");
        for (Iterator<Entry<String, ?>> iterator = entrySet.iterator(); iterator.hasNext();) {
            if (sb.length() > 1) {
                sb.append(", ");
            }
            Entry<String, ?> innerE = iterator.next();
            sb.append(innerE.getKey());
            sb.append("=");
            sb.append(writeObject(innerE.getValue()));
        }
        sb.append("]");
        return sb.toString();
    }

    protected static J4pReadResponse getReadResponse(J4pClient j4pClient, String pObjectName, String... pAttribute) throws MalformedObjectNameException, J4pException, J4pRemoteException {
        J4pReadRequest req = new J4pReadRequest(pObjectName, pAttribute);
        J4pReadResponse resp = null;
        try {
            resp = j4pClient.execute(req);
        } catch (J4pRemoteException j4pRemE) {
            if ("javax.management.InstanceNotFoundException".equalsIgnoreCase(j4pRemE.getErrorType())) {
                // consume not found instances and throw the rest
            } else {
                throw j4pRemE;
            }
        }
        return resp;
    }

    protected static J4pExecResponse getExecResponse(J4pClient j4pClient, String pObjectName, String pOperation, Object... pAttribute) throws MalformedObjectNameException, J4pException, J4pRemoteException {
        J4pExecRequest req = new J4pExecRequest(pObjectName, pOperation, pAttribute);
        J4pExecResponse resp = null;
        try {
            resp = j4pClient.execute(req);
        } catch (J4pRemoteException j4pRemE) {
            if ("javax.management.InstanceNotFoundException".equalsIgnoreCase(j4pRemE.getErrorType())) {
                // consume not found instances and throw the rest
            } else {
                throw j4pRemE;
            }
        }
        return resp;
    }

    protected static String[] parseStacktracJsonArrayToString(JSONArray jsonArray) {
        String[] returnString = new String[0];

        if (null != jsonArray) {
            returnString = new String[jsonArray.size()];
            for (int i = 0; i < jsonArray.size(); i++) {
                StringBuilder sb = new StringBuilder();
                JSONObject jo = (JSONObject) jsonArray.get(i);
                sb.append(jo.get("className")).append(jo.get("methodName"));
                sb.append("(");
                if ((Boolean) jo.get("nativeMethod")) {
                    sb.append("Native Method");
                } else {
                    sb.append(jo.get("fileName")).append(":").append(jo.get("lineNumber"));
                }
                sb.append(")");
                returnString[i] = sb.toString();
            }
        }
        return returnString;
    }

    protected static long[] extractLongFromJsonArray(final J4pExecResponse execResponse, int size) {
        long[] longArray = null;
        if (execResponse.getValue() instanceof JSONArray) {
            JSONArray jsonArray = execResponse.getValue();
            longArray = new long[jsonArray.size()];
            for (int i = 0; i < jsonArray.size(); i++) {
                longArray[i] = (Long) jsonArray.get(i);
            }
        }
        if (null == longArray) {
            longArray = new long[size];
            Arrays.fill(longArray, 0);
        }
        return longArray;
    }

    protected static String writeOperation(String className, String name, final Entry<String, ?> operationE, final String operationName) {
        StringBuilder sb = new StringBuilder();
        if (operationE.getValue() instanceof JSONObject) {
            final JSONObject operationValues = (JSONObject) operationE.getValue();
            sb.append(writeOperationDetails(className, name, operationName, operationValues));
        } else if (operationE.getValue() instanceof JSONArray) {
            final JSONArray operationArray = (JSONArray) operationE.getValue();
            for (int i = 0; i < operationArray.size(); i++) {
                final JSONObject operationJsonObject = (JSONObject) operationArray.get(i);
                sb.append(writeOperationDetails(className, name, operationName, operationJsonObject));
            }
        }
        return sb.toString();
    }

    protected static String writeOperationDetails(String className, String name, final String operationName, final JSONObject operationValues) {
        StringBuilder sb = new StringBuilder();
        sb.append(className).append(":").append(name);
        sb.append(", operationname: ");
        sb.append(operationName);
        sb.append(writeOperationArguments(operationValues));
        sb.append(", desciption: ");
        sb.append(operationValues.get("desc"));
        sb.append(LINE_SEPARATOR);
        return sb.toString();
    }

    protected static String writeOperationArguments(final JSONObject operationJsonObject) {
        StringBuilder sb = new StringBuilder();
        if (null != operationJsonObject.get("args")) {
            if (operationJsonObject.get("args") instanceof JSONObject) {
                final JSONObject argsObject = (JSONObject) operationJsonObject.get("args");
                sb.append("(");
                sb.append(argsObject.get("type"));
                sb.append(")");
            } else if (operationJsonObject.get("args") instanceof JSONArray) {
                final JSONArray argsObjects = (JSONArray) operationJsonObject.get("args");
                sb.append("(");
                for (int i = 0; i < argsObjects.size(); i++) {
                    final JSONObject argsObject = (JSONObject) argsObjects.get(i);
                    if (i > 0) {
                        sb.append(", ");
                    }
                    sb.append(argsObject.get("type"));
                }
                sb.append(")");
            }

        }
        return sb.toString();
    }


    protected static Option createShortLongOption(char shortArg, String longArg, String description, boolean required) {
        Option option = new Option(String.valueOf(shortArg), longArg, false, description);
        option.setRequired(required);
        return option;
    }

    protected static Option createShortOption(char shortArg, String description, boolean required) {
        Option option = new Option(String.valueOf(shortArg), null, false, description);
        option.setRequired(required);
        return option;
    }

    protected static Option createLongOption(String longArg, String description, boolean required) {
        Option option = new Option(null, longArg, false, description);
        option.setRequired(required);
        return option;
    }

    protected static Option createShortLongArgumentOption(char shortArg, String longArg, String description, String argName, boolean required) {
        Option option = new Option(String.valueOf(shortArg), longArg, true, description);
        option.setArgs(1);
        option.setArgName(argName);
        option.setRequired(required);
        return option;
    }

    protected static Option createShortArgumentOption(char shortArg, String description, String argName, boolean required) {
        Option option = new Option(String.valueOf(shortArg), null, true, description);
        option.setArgs(1);
        option.setArgName(argName);
        option.setRequired(required);
        return option;
    }

    protected static Option createLongArgumentOption(String longArg, String description, String argName, boolean required) {
        Option option = new Option(null, longArg, true, description);
        option.setArgs(1);
        option.setArgName(argName);
        option.setRequired(required);
        return option;
    }

    protected static String getUsableOptionRepresentation(Option option) {
        if (null != option.getLongOpt()) {
            return option.getLongOpt();
        }
        return option.getOpt();
    }
}
