package com.openexchange.groupware.infostore.utils;

import java.util.regex.Pattern;

public class URLHelper {

	private static final Pattern SCHEMA_PATTERN = Pattern.compile("^\\S+?:(//)?");
	private static final Pattern MAYBE_MAIL = Pattern.compile("\\S+?@\\S+?\\.\\w+$");
	
	public String process(String url) {
		if(!hasSchema(url)) {
			return isMail(url) ? "mailto:"+url : "http://"+url;
		}
		return url;
	}

	private final boolean isMail(String url) {
		return MAYBE_MAIL.matcher(url).find();
	}

	private final boolean hasSchema(String url) {
		return SCHEMA_PATTERN.matcher(url).find();
	}

}
