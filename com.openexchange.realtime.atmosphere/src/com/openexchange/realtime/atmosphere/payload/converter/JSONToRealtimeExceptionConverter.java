
package com.openexchange.realtime.atmosphere.payload.converter;

import java.util.ArrayList;
import java.util.Iterator;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.conversion.simple.SimpleConverter;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.exception.RealtimeException;
import com.openexchange.realtime.exception.RealtimeExceptionCodes;
import com.openexchange.realtime.exception.RealtimeExceptionFactory;
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
            String plainLogMessage = incoming.getString("plainLogMessage");
            Object[] logArgs = getLogArgs(incoming.getJSONArray("logArgs"));
            StackTraceElement[] stackTrace = getStackTrace(incoming.getJSONArray("stackTrace"));
//            Throwable throwable = new Throwable();
//            throwable.setStackTrace(stackTrace);
            RealtimeException outgoing = RealtimeExceptionCodes.create(codeNumber, null, logArgs);
            outgoing.setStackTrace(stackTrace);
            return outgoing;
        } catch (JSONException e) {
            throw RealtimeExceptionCodes.STANZA_INTERNAL_SERVER_ERROR.create(e);
        }
    }

    private Object[] getLogArgs(JSONArray jsonArray) {
        // TODO Auto-generated method stub
        return null;
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
