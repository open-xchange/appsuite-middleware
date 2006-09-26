/**
 * 
 */
package com.openexchange.tools;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.openexchange.tools.encoding.URLCoder;
import java.util.Date;

/**
 * This class provides methods to generate URL parameters.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class URLParameter {

    /**
     * Container for the parameters.
     */
    private final transient Map parameters;
    
    /**
     * Default constructor.
     */
    public URLParameter() {
        super();
        parameters = new HashMap();
    }

    public void setParameter(final String name, final String value) {
        parameters.put(name, value);
    }
	
    public void setParameter(final String name, final int value) {
        parameters.put(name, String.valueOf(value));
    }
	
    public void setParameter(final String name, final long value) {
        parameters.put(name, String.valueOf(value));
    }
	
    public void setParameter(final String name, final boolean value) {
        parameters.put(name, String.valueOf(value));
    }
	
    public void setParameter(final String name, final Date value) {
        parameters.put(name, String.valueOf(value.getTime()));
    }


    public String getURLParameters() throws UnsupportedEncodingException {
        final StringBuffer sb = new StringBuffer();
        if (parameters.size() > 0) {
            sb.append('?');
        }
        final Iterator iter = parameters.keySet().iterator();
        while (iter.hasNext()) {
            final String name = (String) iter.next();
            sb.append(URLCoder.encode(name, "UTF-8"));
            sb.append('=');
            sb.append(URLCoder.encode((String) parameters.get(name), "UTF-8"));
            if (iter.hasNext()) {
                sb.append('&');
            }
        }
        return sb.toString();
    }
	
	public static String colsArray2String(int cols[]) {
		StringBuffer sb = new StringBuffer();
		for (int a = 0; a  < cols.length; a++) {
			if (a == 0) {
				sb.append(cols[a]);
			} else {
				// sb.append("%2C" + cols[a]);
				sb.append("," + cols[a]);
			}
		}
		
		return sb.toString();
	}
}
