package com.openexchange.rss;

import java.util.Date;

public class RssResult {
	private String url, author, format, body, subject, feedUrl, feedTitle, imageUrl;
	private Date publishedDate;

	public String getUrl() {
		return url;
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

	public String getFeedUrl() {
		return feedUrl;
	}

	public String getFeedTitle() {
		return feedTitle;
	}

	public String getImageUrl() {
		return this.imageUrl;
	}
	
	public Date getDate() {
		return this.publishedDate;
	}

	public RssResult setUrl(String url) {
		this.url = url;
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
	
	public RssResult setFeedUrl(String feedUrl) {
		this.feedUrl = feedUrl;
		return this;
	}
	
	public RssResult setFeedTitle(String feedTitle) {
		this.feedTitle = feedTitle;
		return this;
	}
	
	public RssResult setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
		return this;
	}

	public RssResult setDate(Date publishedDate) {
		this.publishedDate = publishedDate;
		return this;
	}
}
