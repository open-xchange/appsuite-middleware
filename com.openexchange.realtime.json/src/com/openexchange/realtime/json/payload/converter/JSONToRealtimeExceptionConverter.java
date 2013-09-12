
package com.openexchange.realtime.json.payload.converter;

import java.util.ArrayList;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.conversion.DataExceptionCodes;
import com.openexchange.conversion.simple.SimpleConverter;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.exception.RealtimeException;
import com.openexchange.realtime.exception.RealtimeExceptionCodes;
import com.openexchange.tools.session.ServerSession;

public class JSONToRealtimeExceptionConverter extends AbstractJSONConverter {

    @Override
    public String getOutputFormat() {
        return RealtimeException.class.getSimpleName();
    }

    @Override
    public Object convert(Object data, ServerSession session, SimpleConverter converter) throws OXException {
        JSONObject incoming = (JSONObject) data;

        try {
            int codeNumber = incoming.getInt("code");
            Object[] logArgs = getLogArgs(incoming.optJSONArray("logArgs"));
            StackTraceElement[] stackTrace = getStackTrace(incoming.getJSONArray("stackTrace"));
            Object causeObject = incoming.opt("cause");
            Throwable cause = null;
            if(causeObject != null) {
                cause = (Throwable) converter.convert("json", Throwable.class.getSimpleName(), causeObject, session);
            }
            RealtimeException outgoing = RealtimeExceptionCodes.create(codeNumber, cause, logArgs);
            outgoing.setStackTrace(stackTrace);
            return outgoing;
        } catch (Exception e) {
            throw DataExceptionCodes.UNABLE_TO_CHANGE_DATA.create(data.toString(), e);
        }
    }

    private Object[] getLogArgs(JSONArray jsonArray) {
        ArrayList<Object> logArgs = new ArrayList<Object>();
        if (jsonArray != null) {
            Iterator<Object> iterator = jsonArray.iterator();
            while (iterator.hasNext()) {
                logArgs.add(iterator.next());
            }
        }
        return logArgs.toArray(new Object[logArgs.size()]);
    }

    private StackTraceElement[] getStackTrace(JSONArray stackTraceArray) throws OXException {
        ArrayList<StackTraceElement> stackTraceList = new ArrayList<StackTraceElement>();
        try {
            Iterator<Object> arrayIterator = stackTraceArray.iterator();
            while(arrayIterator.hasNext()) {
                stackTraceList.add(stackTraceElementToJSON((JSONObject)arrayIterator.next()));
            }
        } catch (JSONException e) {
            throw RealtimeExceptionCodes.STANZA_INTERNAL_SERVER_ERROR.create(e);
        }
        return stackTraceList.toArray(new StackTraceElement[stackTraceList.size()]);
    }

    private StackTraceElement stackTraceElementToJSON(JSONObject stackTraceObject) throws JSONException {
        String fileName = stackTraceObject.getString("fileName");
        String className = stackTraceObject.getString("className");
        String methodName = stackTraceObject.getString("methodName");
        int lineNumber = stackTraceObject.getInt("lineNumber");
        StackTraceElement stackTraceElement = new StackTraceElement(className, methodName, fileName, lineNumber);
        return stackTraceElement;
    }

}
