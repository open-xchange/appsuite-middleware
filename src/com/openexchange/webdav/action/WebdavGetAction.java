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
 *    trademarks of the Open-Xchange, Inc. group of companies.
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
 *     Copyright (C) 2004-2006 Open-Xchange, Inc.
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.webdav.protocol.WebdavException;
import com.openexchange.webdav.protocol.WebdavResource;

import static com.openexchange.tools.io.IOTools.reallyBloodySkip;;

public class WebdavGetAction extends WebdavHeadAction {
	private static final Log LOG = LogFactory.getLog(WebdavGetAction.class);
	private static final Pattern RANGE_PATTERN = Pattern.compile("bytes=(\\S+)");

	public void perform(WebdavRequest req, WebdavResponse res) throws WebdavException {
		WebdavResource resource = req.getResource();
		if(!resource.exists()) 
			throw new WebdavException(req.getUrl(), HttpServletResponse.SC_NOT_FOUND);
		List<ByteRange> ranges = getRanges(req, res);
		
		long size = 0;
		long offset = 0;
		for(ByteRange range : ranges) {
			offset = (range.startOffset < offset) ? offset : range.startOffset;
			if(offset > range.endOffset)
				continue;
			size += range.endOffset - offset;
			size++;
		}
		head(res,resource,size);
		
		BufferedOutputStream out = null;
		InputStream in = null;
		try {
			out = new BufferedOutputStream(res.getOutputStream());
			in = resource.getBody();
			byte[] chunk = new byte[200];
			offset = 0;
			for(ByteRange range : ranges) {
				if(offset < range.startOffset) {
					reallyBloodySkip(in, range.startOffset-offset);
					offset = (int) range.startOffset;
				}
				if(offset > range.endOffset)
					continue;
				int read = 0;
				int need = (int) ((offset + chunk.length >= range.endOffset) ? range.endOffset - offset + 1 : chunk.length);
				while(need > 0 && (read = in.read(chunk, 0, need)) != -1) {
					out.write(chunk,0,read);
					offset += read;
					need = (int) ((offset + chunk.length >= range.endOffset) ? range.endOffset - offset + 1: chunk.length);
				}
			}
			
		} catch (IOException e) {
			throw new WebdavException(req.getUrl(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		} finally {
			if(out != null) {
				try {
					out.flush();
				} catch (IOException e) {
					LOG.debug("",e);
				}
				// NEVER CLOSE THE OUTPUT STREAM NO MATTER WHAT FINDBUGS TELLS YOU
				/*try {
					out.close();
				} catch (IOException e) {
					LOG.debug("",e);
				}*/
			}
			
			if(in != null) {
				try {
					in.close();
				} catch (IOException e) {
					LOG.debug("",e);
				}
			}
		}
	}
	
	private List<ByteRange> getRanges(WebdavRequest req, WebdavResponse res) throws WebdavException {
		String byteRanges = req.getHeader("Bytes");
		long length = req.getResource().getLength();
		
		List<ByteRange> retVal = new ArrayList<ByteRange>();
		if(byteRanges != null) {
			
			for(String range : byteRanges.split("\\s*,\\s*")) {
				range = range.trim();
				ByteRange ro =parseRange(range, length, req.getUrl());
				if(ro!=null)
					retVal.add(ro);
			}
		}
		
		String range = req.getHeader("Range");
		
		if(range != null) {
			Matcher m = RANGE_PATTERN.matcher(range);
			while(m.find()){
				String br = m.group(1);
				for(String r : br.split("\\s*,\\s*")) {
					range = range.trim();
					ByteRange ro = parseRange(r, length,req.getUrl());
					if(ro!=null)
						retVal.add(ro);
				}
			}
		}
		
		if(retVal.size() == 0) {
			res.setStatus(HttpServletResponse.SC_OK);
			return Arrays.asList(new ByteRange(0, req.getResource().getLength()-1));
		} else {
			res.setStatus(HttpServletResponse.SC_PARTIAL_CONTENT);
		}
		Collections.sort(retVal);
		return retVal;
	}

	private ByteRange parseRange(String range, long length, String url) throws WebdavException {
		if(range.charAt(0) == '-') {
			long reqLength = new Long(range.substring(1));
			if(reqLength > length) {
				return new ByteRange(0, length-1);
			}
			ByteRange br = new ByteRange(length-reqLength,length-1);
			return br;
		} else if (range.charAt(range.length()-1) == '-') {
			long startOffset = new Long(range.substring(0, range.length()-1));
			ByteRange br = new ByteRange(startOffset, length-1);
			return br;
		} else {
			String[] startAndEnd = range.split("\\s*-\\s*");
			long startOffset = new Long(startAndEnd[0]);
			long endOffset = new Long(startAndEnd[1]);
			/*if(startOffset>endOffset) {
				return new ByteRange(0,0);
			}*/
			if(startOffset>length) {
				throw new WebdavException(url, HttpServletResponse.SC_REQUESTED_RANGE_NOT_SATISFIABLE);
			}
			ByteRange br = new ByteRange(startOffset, endOffset);
			return br;
		}
	}

	private static final class ByteRange implements Comparable{
		public long startOffset;
		public long endOffset;
		
		public ByteRange(long start, long end){
			startOffset = start;
			endOffset = end;
		}

		public int compareTo(Object arg0) {
			ByteRange other = (ByteRange) arg0;
			return ((Long)startOffset).compareTo((Long)other.startOffset);
		}
		
		public String toString(){
			return String.format("%d-%d", startOffset, endOffset);
		}
		
	}

}
