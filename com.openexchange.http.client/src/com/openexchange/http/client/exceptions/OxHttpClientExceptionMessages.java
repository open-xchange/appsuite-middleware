package com.openexchange.http.client.exceptions;

import com.openexchange.i18n.LocalizableStrings;

public class OxHttpClientExceptionMessages implements LocalizableStrings {
	public static String APACHE_CLIENT_ERROR_MSG = "The embedded Apache client threw an error: %1$s";
	public static String JSON_ERROR_MSG = "Parsing this JSON did not work: %1$s";
	public static String SAX_ERROR_MSG = "Parsing this XML with SAX did not work: %1$s";
	public static String CATCH_ALL_MSG = "Some generic exception was thrown: %1$s";
	public static String IO_ERROR_MSG = "An IO error occurred: %1$s";
}
