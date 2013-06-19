
package com.openexchange.realtime.atmosphere.payload.converter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.openexchange.conversion.DataExceptionCodes;
import com.openexchange.conversion.simple.SimpleConverter;
import com.openexchange.exception.OXException;
import com.openexchange.realtime.exception.RealtimeException;
import com.openexchange.realtime.exception.RealtimeExceptionCodes;
import com.openexchange.tools.session.ServerSession;

public class RealtimeExceptionToJSONConverter extends AbstractPOJOConverter {

    @Override
    public String getInputFormat() {
        return RealtimeException.class.getSimpleName();
    }

    @Override
    public Object convert(Object data, ServerSession session, SimpleConverter converter) throws OXException {
        RealtimeException incoming = (RealtimeException) data;
        int code = incoming.getCode();
        String plainLogMessage = incoming.getPlainLogMessage();
        String localizedMessage = incoming.getLocalizedMessage();
        Object[] logArgs = incoming.getLogArgs();
        StackTraceElement[] stackTraceElements = incoming.getStackTrace();
        Throwable cause = incoming.getCause();

        JSONObject jsonException = new JSONObject();
        try {
            jsonException.put("code", code);
            jsonException.put("plainLogMessage", plainLogMessage);
            JSONArray logArgArray = new JSONArray();
            for (Object arg : logArgs) {
                logArgArray.put(arg.toString());
            }
            jsonException.put("logArgs", logArgArray);
            jsonException.put("localizedMessage", localizedMessage);
            jsonException.put("stackTrace", stackTraceToJSON(stackTraceElements));
            if(cause != null) {
                jsonException.put("cause", converter.convert(Throwable.class.getSimpleName(), "json", cause, null));
            }
        } catch (Exception e) {
            throw DataExceptionCodes.UNABLE_TO_CHANGE_DATA.create(data.toString(), e);
        }
        return jsonException;
    }

    private JSONArray stackTraceToJSON(StackTraceElement[] stackTrace) throws OXException {
        JSONArray stackTraceArray = new JSONArray();
        try {
            for (StackTraceElement stackTraceElement : stackTrace) {
                stackTraceArray.put(stackTraceElementToJSON(stackTraceElement));
            }
        } catch (JSONException e) {
            throw RealtimeExceptionCodes.STANZA_INTERNAL_SERVER_ERROR.create(e);
        }
        return stackTraceArray;
    }

    private JSONObject stackTraceElementToJSON(StackTraceElement stackTraceElement) throws JSONException {
        JSONObject stackTraceObject = new JSONObject();
        stackTraceObject.put("fileName", stackTraceElement.getFileName());
        stackTraceObject.put("className", stackTraceElement.getClassName());
        stackTraceObject.put("methodName", stackTraceElement.getMethodName());
        stackTraceObject.put("lineNumber", stackTraceElement.getLineNumber());
        return stackTraceObject;
    }

}
