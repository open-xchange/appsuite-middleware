/**
 *
 */
package com.openexchange.tools;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.openexchange.tools.encoding.Charsets;
import com.openexchange.tools.encoding.URLCoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class provides methods to generate URL parameters.
 * @author <a href="mailto:marcus@open-xchange.org">Marcus Klein</a>
 */
public class URLParameter {
	
	/**
	 * enables multivalues for the url parameter
	 **/
	private boolean multivalue = false;
	
	/**
	 * Container for the parameters.
	 */
	private final transient Map parameters;
	
	/**
	 * Container for the parameters with multi values
	 */
	private final transient Map<String, List<String>> multivalueParameters;
	
	/**
	 * Default constructor.
	 */
	public URLParameter() {
		super();
		parameters = new HashMap();
		multivalueParameters = new HashMap<String, List<String>>();
	}
	
	public URLParameter(boolean multivalue) {
		super();
		this.multivalue = multivalue;
		parameters = new HashMap();
		multivalueParameters = new HashMap<String, List<String>>();
	}
	
	public void setParameter(final String name, final String value) {
		setParameter2Map(name, value);
	}
	
	public void setParameter(final String name, final int value) {
		setParameter2Map(name, String.valueOf(value));
	}
	
	public void setParameter(final String name, final long value) {
		setParameter2Map(name, String.valueOf(value));
	}
	
	public void setParameter(final String name, final boolean value) {
		setParameter2Map(name, String.valueOf(value));
	}
	
	public void setParameter(final String name, final Date value) {
		setParameter2Map(name, String.valueOf(value.getTime()));
	}
	
	private void setParameter2Map(final String name, final String value) {
		if (multivalue) {
			if (multivalueParameters.containsKey(name)) {
				List l = multivalueParameters.get(name);
				l.add(value);
			} else {
				List<String> l = new ArrayList<String>();
				l.add(value);
				
				multivalueParameters.put(name, l);
			}
		} else {
			parameters.put(name, value);
		}
	}
	
	public String getURLParameters() throws UnsupportedEncodingException {
		final StringBuffer sb = new StringBuffer();
		
		if (multivalue) {
			if (multivalueParameters.size() > 0) {
				sb.append('?');
			}
			
			final Iterator iter = multivalueParameters.keySet().iterator();
			while (iter.hasNext()) {
				final String name = (String) iter.next();
				final List l = multivalueParameters.get(name);
				
				for (int a = 0; a < l.size(); a++) {
					sb.append(URLCoder.encode(name, Charsets.UTF_8));
					sb.append('=');
					sb.append(URLCoder.encode((String) l.get(a), Charsets.UTF_8));
					if (iter.hasNext()) {
						sb.append('&');
					}
				}
			}
		} else {
			if (parameters.size() > 0) {
				sb.append('?');
			}
			
			final Iterator iter = parameters.keySet().iterator();
			while (iter.hasNext()) {
				final String name = (String) iter.next();
				sb.append(URLCoder.encode(name, Charsets.UTF_8));
				sb.append('=');
				sb.append(URLCoder.encode((String) parameters.get(name), Charsets.UTF_8));
				if (iter.hasNext()) {
					sb.append('&');
				}
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
				sb.append(',');
				sb.append(cols[a]);
			}
		}
		
		return sb.toString();
	}
}
