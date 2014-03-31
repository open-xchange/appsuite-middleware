package org.json;

/*
Copyright (c) 2002 JSON.org

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

The Software shall be used for Good, not Evil.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

import java.util.Set;

/**
 * Convert an HTTP header to a JSONObject and back.
 * @author JSON.org
 * @version 2
 */
public class HTTP {

    /** Carriage return/line feed. */
    public static final String CRLF = "\r\n";

    private static final String HEADER_HTTP_VERSION = "HTTP-Version";

    private static final String HEADER_STATUS_CODE = "Status-Code";

    private static final String HEADER_REASON_PHRASE = "Reason-Phrase";

    private static final String HEADER_METHOD = "Method";

    private static final String HEADER_REQ_URI = "Request-URI";

    /**
     * Convert an HTTP header string into a JSONObject. It can be a request
     * header or a response header. A request header will contain
     * <pre>{
     *    Method: "POST" (for example),
     *    "Request-URI": "/" (for example),
     *    "HTTP-Version": "HTTP/1.1" (for example)
     * }</pre>
     * A response header will contain
     * <pre>{
     *    "HTTP-Version": "HTTP/1.1" (for example),
     *    "Status-Code": "200" (for example),
     *    "Reason-Phrase": "OK" (for example)
     * }</pre>
     * In addition, the other parameters in the header will be captured, using
     * the HTTP field names as JSON names, so that <pre>
     *    Date: Sun, 26 May 2002 18:06:04 GMT
     *    Cookie: Q=q2=PPEAsg--; B=677gi6ouf29bn&b=2&f=s
     *    Cache-Control: no-cache</pre>
     * become
     * <pre>{...
     *    Date: "Sun, 26 May 2002 18:06:04 GMT",
     *    Cookie: "Q=q2=PPEAsg--; B=677gi6ouf29bn&b=2&f=s",
     *    "Cache-Control": "no-cache",
     * ...}</pre>
     * It does no further checking or conversion. It does not parse dates.
     * It does not do '%' transforms on URLs.
     * @param string An HTTP header string.
     * @return A JSONObject containing the elements and attributes
     * of the XML string.
     * @throws JSONException
     */
    public static JSONObject toJSONObject(final String string) throws JSONException {
    	final JSONObject     o = new JSONObject();
    	final HTTPTokener    x = new HTTPTokener(string);
        String         t;

        t = x.nextToken();
        if (t.toUpperCase().startsWith("HTTP")) {

// Response

            o.put(HEADER_HTTP_VERSION, t);
            o.put(HEADER_STATUS_CODE, x.nextToken());
            o.put(HEADER_REASON_PHRASE, x.nextTo('\0'));
            x.next();

        } else {

// Request

            o.put(HEADER_METHOD, t);
            o.put(HEADER_REQ_URI, x.nextToken());
            o.put(HEADER_HTTP_VERSION, x.nextToken());
        }

// Fields

        while (x.more()) {
        	final String name = x.nextTo(':');
            x.next(':');
            o.put(name, x.nextTo('\0'));
            x.next();
        }
        return o;
    }


    /**
     * Convert a JSONObject into an HTTP header. A request header must contain
     * <pre>{
     *    Method: "POST" (for example),
     *    "Request-URI": "/" (for example),
     *    "HTTP-Version": "HTTP/1.1" (for example)
     * }</pre>
     * A response header must contain
     * <pre>{
     *    "HTTP-Version": "HTTP/1.1" (for example),
     *    "Status-Code": "200" (for example),
     *    "Reason-Phrase": "OK" (for example)
     * }</pre>
     * Any other members of the JSONObject will be output as HTTP fields.
     * The result will end with two CRLF pairs.
     * @param o A JSONObject
     * @return An HTTP header string.
     * @throws JSONException if the object does not contain enough
     *  information.
     */
    public static String toString(final JSONObject o) throws JSONException {
    	final Set<String>     keys = o.keySet();
    	final StringBuilder sb = new StringBuilder();
        if (o.has(HEADER_STATUS_CODE) && o.has(HEADER_REASON_PHRASE)) {
            sb.append(o.getString(HEADER_HTTP_VERSION));
            sb.append(' ');
            sb.append(o.getString(HEADER_STATUS_CODE));
            sb.append(' ');
            sb.append(o.getString(HEADER_REASON_PHRASE));
        } else if (o.has(HEADER_METHOD) && o.has(HEADER_REQ_URI)) {
            sb.append(o.getString(HEADER_METHOD));
            sb.append(' ');
            sb.append('"');
            sb.append(o.getString(HEADER_REQ_URI));
            sb.append('"');
            sb.append(' ');
            sb.append(o.getString(HEADER_HTTP_VERSION));
        } else {
            throw new JSONException("Not enough material for an HTTP header.");
        }
        sb.append(CRLF);
        for (String s : keys) {
            if (!s.equals(HEADER_HTTP_VERSION)      && !s.equals(HEADER_STATUS_CODE) &&
                    !s.equals(HEADER_REASON_PHRASE) && !s.equals(HEADER_METHOD) &&
                    !s.equals(HEADER_REQ_URI)   && !o.isNull(s)) {
                sb.append(s);
                sb.append(": ");
                sb.append(o.getString(s));
                sb.append(CRLF);
            }
        }
        sb.append(CRLF);
        return sb.toString();
    }
}
