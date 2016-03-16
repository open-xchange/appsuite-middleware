/*
 *
 *    OPEN-XCHANGE legal information
 *
 *    All intellectual property rights in the Software are protected by
 *    international copyright laws.
 *
 *
 *    In some countries OX, OX Open-Xchange, open xchange and OXtender
 *    as well as the corresponding Logos OX Open-Xchange and OX are registered
 *    trademarks of the OX Software GmbH group of companies.
 *    The use of the Logos is not covered by the GNU General Public License.
 *    Instead, you are allowed to use these Logos according to the terms and
 *    conditions of the Creative Commons License, Version 2.5, Attribution,
 *    Non-commercial, ShareAlike, and the interpretation of the term
 *    Non-commercial applicable to the aforementioned license is published
 *    on the web site http://www.open-xchange.com/EN/legal/index.html.
 *
 *    Please make sure that third-party modules and libraries are used
 *    according to their respective licenses.
 *
 *    Any modifications to this package must retain all copyright notices
 *    of the original copyright holder(s) for the original code used.
 *
 *    After any such modifications, the original and derivative code shall remain
 *    under the copyright of the copyright holder(s) and/or original author(s)per
 *    the Attribution and Assignment Agreement that can be located at
 *    http://www.open-xchange.com/EN/developer/. The contributing author shall be
 *    given Attribution for the derivative code and a license granting use.
 *
 *     Copyright (C) 2016-2020 OX Software GmbH
 *     Mail: info@open-xchange.com
 *
 *
 *     This program is free software; you can redistribute it and/or modify it
 *     under the terms of the GNU General Public License, Version 2 as published
 *     by the Free Software Foundation.
 *
 *     This program is distributed in the hope that it will be useful, but
 *     WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 *     or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 *     for more details.
 *
 *     You should have received a copy of the GNU General Public License along
 *     with this program; if not, write to the Free Software Foundation, Inc., 59
 *     Temple Place, Suite 330, Boston, MA 02111-1307 USA
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
			if(d != null) {
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
