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

package com.openexchange.webdav.action;

import static com.openexchange.tools.io.IOTools.reallyBloodySkip;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.http.HttpServletResponse;
import com.openexchange.java.Streams;
import com.openexchange.webdav.protocol.WebdavPath;
import com.openexchange.webdav.protocol.WebdavProtocolException;
import com.openexchange.webdav.protocol.WebdavResource;

public class WebdavGetAction extends WebdavHeadAction {
	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(WebdavGetAction.class);
	private static final Pattern RANGE_PATTERN = Pattern.compile("bytes=(\\S+)");

	@Override
	public void perform(final WebdavRequest req, final WebdavResponse res) throws WebdavProtocolException {
		final WebdavResource resource = req.getResource();
		res.setContentType(resource.getContentType());
		if(!resource.exists()) {
			throw WebdavProtocolException.Code.GENERAL_ERROR.create(req.getUrl(), HttpServletResponse.SC_NOT_FOUND);
		}
		final List<ByteRange> ranges = getRanges(req, res);

		long size = 0;
		long offset = 0;
		for(final ByteRange range : ranges) {
			offset = (range.startOffset < offset) ? offset : range.startOffset;
			if(offset > range.endOffset) {
				continue;
			}
			size += range.endOffset - offset;
			size++;
		}
		head(res,resource,size);

		BufferedOutputStream out = null;
		InputStream in = null;
		try {
			out = new BufferedOutputStream(res.getOutputStream());
			in = resource.getBody();
			final byte[] chunk = new byte[200];
			offset = 0;
			for(final ByteRange range : ranges) {
				if(offset < range.startOffset) {
					reallyBloodySkip(in, range.startOffset-offset);
					offset = (int) range.startOffset;
				}
				if(offset > range.endOffset) {
					continue;
				}
				int read = 0;
				int need = (int) Math.min(chunk.length, range.endOffset - offset + 1);
				while(need > 0 && (read = in.read(chunk, 0, need)) > 0) {
					out.write(chunk,0,read);
					offset += read;
					need = (int) Math.min(chunk.length, range.endOffset - offset + 1);
				}
			}

		} catch (final IOException e) {
			throw WebdavProtocolException.Code.GENERAL_ERROR.create(req.getUrl(), HttpServletResponse.SC_SERVICE_UNAVAILABLE, e, new Object[0]);
		} finally {
		    Streams.flush(out);
			Streams.close(in);
		}
	}

	private List<ByteRange> getRanges(final WebdavRequest req, final WebdavResponse res) throws WebdavProtocolException {
	    WebdavResource resource = req.getResource();
	    if (null == resource || resource.isCollection()) {
	        return Collections.emptyList();
	    }
        Long resourceLength = resource.getLength();
        if (null == resourceLength || 0 >= resourceLength.longValue()) {
            return Collections.emptyList();
        }

		final String byteRanges = req.getHeader("Bytes");
		final long length = resourceLength.longValue();

		final List<ByteRange> retVal = new ArrayList<ByteRange>();
		if(byteRanges != null) {

			for(String range : byteRanges.split("\\s*,\\s*")) {
				range = range.trim();
				final ByteRange ro =parseRange(range, length, req.getUrl());
				if(ro!=null) {
					retVal.add(ro);
				}
			}
		}

		String range = req.getHeader("Range");

		if(range != null) {
			final Matcher m = RANGE_PATTERN.matcher(range);
			while(m.find()){
				final String br = m.group(1);
				for(final String r : br.split("\\s*,\\s*")) {
					range = range.trim();
					final ByteRange ro = parseRange(r, length,req.getUrl());
					if(ro!=null) {
						retVal.add(ro);
					}
				}
			}
		}

		if(retVal.size() == 0) {
			res.setStatus(HttpServletResponse.SC_OK);
			return Arrays.asList(new ByteRange(0, req.getResource().getLength()-1));
		}
		res.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
		Collections.sort(retVal);
		return retVal;
	}

	private ByteRange parseRange(final String range, final long length, final WebdavPath url) throws WebdavProtocolException {
		if(range.charAt(0) == '-') {
			final long reqLength = Long.parseLong(range.substring(1));
			if(reqLength > length) {
				return new ByteRange(0, length-1);
			}
			final ByteRange br = new ByteRange(length-reqLength,length-1);
			return br;
		} else if (range.charAt(range.length()-1) == '-') {
			final long startOffset = Long.parseLong(range.substring(0, range.length()-1));
			final ByteRange br = new ByteRange(startOffset, length-1);
			return br;
		} else {
			final String[] startAndEnd = range.split("\\s*-\\s*");
			final long startOffset = Long.parseLong(startAndEnd[0]);
			final long endOffset = Long.parseLong(startAndEnd[1]);
			/*if(startOffset>endOffset) {
				return new ByteRange(0,0);
			}*/
			if(startOffset>length) {
				throw WebdavProtocolException.Code.GENERAL_ERROR.create(url, HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
			}
			final ByteRange br = new ByteRange(startOffset, endOffset);
			return br;
		}
	}

    private static final class ByteRange implements Comparable<ByteRange> {

        public final long startOffset;

        public final long endOffset;

        public ByteRange(final long start, final long end) {
            startOffset = start;
            endOffset = end;
        }

        @Override
		public int compareTo(final ByteRange arg0) {
            final ByteRange other = arg0;
            return (Long.valueOf(startOffset)).compareTo(Long.valueOf(other.startOffset));
        }

        @Override
        public String toString() {
            return String.format("%d-%d", Long.valueOf(startOffset), Long.valueOf(endOffset));
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + (int) (startOffset ^ (startOffset >>> 32));
            return result;
        }

        @Override
        public boolean equals(final Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof ByteRange)) {
                return false;
            }
            final ByteRange other = (ByteRange) obj;
            if (startOffset != other.startOffset) {
                return false;
            }
            return true;
        }

    }

}
