package org.json;

/**
 * The JSONException is thrown by the JSON.org classes then things are amiss.
 * @author JSON.org
 * @version 2
 */
public class JSONException extends Exception {

	private static final long serialVersionUID = 5894276831604379907L;

	/**
	 * Checks whether given <code>Exception</code> instance is caused by a JSON parse error.
	 *
	 * @param e The <code>Exception</code> instance to examine
	 * @return <code>true</code> if a JSON parse error is the cause; otherwise <code>false</code>
	 */
	public static boolean isParseException(Exception e) {
	    return isParseException0(e);
	}

	private static boolean isParseException0(Throwable e) {
        if (null == e) {
            return false;
        }

        if (e instanceof com.fasterxml.jackson.core.JsonParseException) {
            return true;
        }

        Throwable cause = e.getCause();
        return null == cause ? false : isParseException0(cause);
    }

	// -----------------------------------------------------------------------------------------------------------------

	private Throwable cause;

    /**
     * Constructs a JSONException with an explanatory message.
     * @param message Detail about the reason for the exception.
     */
    public JSONException(String message) {
        super(message);
    }

    /**
     * Constructs a JSONException with an explanatory message.
     * @param message Detail about the reason for the exception.
     */
    public JSONException(String message, Throwable t) {
        super(message, t);
    }

    public JSONException(Throwable t) {
        super(t.getMessage());
        this.cause = t;
    }

    @Override
	public Throwable getCause() {
        return this.cause;
    }
}
