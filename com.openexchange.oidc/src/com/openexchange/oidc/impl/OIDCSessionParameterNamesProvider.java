package com.openexchange.oidc.impl;

import java.util.Collections;
import java.util.List;
import com.openexchange.exception.OXException;
import com.openexchange.oidc.tools.OIDCTools;
import com.openexchange.sessionstorage.SessionStorageParameterNamesProvider;


public class OIDCSessionParameterNamesProvider implements SessionStorageParameterNamesProvider {

    @Override
    public List<String> getParameterNames(int userId, int contextId) throws OXException {
        return Collections.singletonList(OIDCTools.IDTOKEN);
    }

}
