/*
 * @copyright Copyright (c) OX Software GmbH, Germany <info@open-xchange.com>
 * @license AGPL-3.0
 *
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OX App Suite.  If not, see <https://www.gnu.org/licenses/agpl-3.0.txt>.
 *
 * Any use of the work other than as authorized under this license or copyright law is prohibited.
 *
 */
package com.openexchange.rss;

import java.util.Date;

public class RssResult {

	private String url, author, format, body, subject, feedUrl, feedTitle, imageUrl;
	private Date date;
    private boolean externalImagesDropped;

    /**
     * Initializes a new {@link RssResult}.
     */
    public RssResult() {
        super();
        externalImagesDropped = false;
    }

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
		return this.date;
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

	public RssResult setDate(Date... possibleDates) {
		for(Date d: possibleDates) {
			if (d != null) {
				this.date = d;
				return this;
			}
		}
		return this;
	}

    /**
     * Checks if this RSS result has dropped external images
     *
     * @return <code>true</code> if this RSS result has dropped external images; otherwise <code>false</code>
     */
    public boolean hasDroppedExternalImages() {
        return externalImagesDropped;
    }

    /**
     * Marks this RSS result to have external images dropped.
     */
    public void markExternalImagesDropped() {
        externalImagesDropped = true;
    }
}
