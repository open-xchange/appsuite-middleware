package com.openexchange.rss;

public class RssResult {
	private String source, author, format, body, subject;

	public String getSource() {
		return source;
	}

	public String getAuthor() {
		return author;
	}

	public String getFormat() {
		return format;
	}

	public String getBody() {
		return body;
	}

	public String getSubject() {
		return subject;
	}

	public RssResult setSource(String source) {
		this.source = source;
		return this;
	}

	public RssResult setAuthor(String author) {
		this.author = author;
		return this;
	}

	public RssResult setFormat(String format) {
		this.format = format;
		return this;
	}

	public RssResult setBody(String body) {
		this.body = body;
		return this;
	}

	public RssResult setSubject(String subject) {
		this.subject = subject;
		return this;
	}
}
