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



package com.openexchange.tools.text.spelling;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * OXAspellCheck
 * 
 * @author Stefan Preuss <stefan.preuss@open-xchange.com>
 * @version 0.8.1, 11/11/05
 * @since 0.8.1-5
 */
public class OXAspellCheck implements OXSpellCheck {

	BufferedReader _spReader;
	BufferedWriter _spWriter;
	Process _spProcess;
	String _spVersion;
	static final String _spPrefix = "^";
	String _spCmd;
   String _spEncoding = "UTF-8";
    
	public OXAspellCheck(final String spCmd) throws IOException {
		_spCmd = spCmd;
		init();
	}
	    
	/**
	 * Initiate the process to the spellchecker
	 * @throws IOException
	 */
	private void init() throws IOException {
        final Runtime runtime = Runtime.getRuntime();
        _spProcess = runtime.exec(_spCmd);
        _spReader = new BufferedReader(new InputStreamReader(_spProcess.getInputStream(), _spEncoding));
        _spWriter = new BufferedWriter(new OutputStreamWriter(_spProcess.getOutputStream(), _spEncoding));
        _spVersion = _spReader.readLine();
    }
	
	public List<OXSpellCheckResult> parseLine(final String line) throws IOException {
        final List<OXSpellCheckResult> results = new ArrayList<OXSpellCheckResult>();
        _spWriter.write(_spPrefix + line);
        _spWriter.newLine();
        _spWriter.flush();
        String response = _spReader.readLine();
        while (response != null && !response.equals("")) {
            final OXSpellCheckResult result = new OXSpellCheckResult(response);
            if (result.getType() == OXSpellCheckResult.SUGGESTION) {
				results.add(result);
			}
            response = _spReader.readLine();
        }
        return results;
    }
	
	public String getVersion() {
		return _spVersion;
	}
	
    public void setEncoding(final String encoding) {
        _spEncoding = encoding;
    }
    
	public void destroy() {
		_spProcess.destroy();
	}
}
