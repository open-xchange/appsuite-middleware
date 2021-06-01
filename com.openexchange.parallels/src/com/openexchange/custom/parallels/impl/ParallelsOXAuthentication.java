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



package com.openexchange.custom.parallels.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import org.slf4j.Logger;
import com.openexchange.authentication.Authenticated;
import com.openexchange.authentication.AuthenticationService;
import com.openexchange.authentication.LoginExceptionCodes;
import com.openexchange.authentication.LoginInfo;
import com.openexchange.config.ConfigurationService;
import com.openexchange.context.ContextService;
import com.openexchange.custom.parallels.osgi.Services;
import com.openexchange.database.Databases;
import com.openexchange.databaseold.Database;
import com.openexchange.exception.OXException;
import com.openexchange.groupware.contexts.Context;
import com.openexchange.java.Strings;
import com.openexchange.user.User;
import com.openexchange.user.UserService;


/**
 * This Plugin is used to authenticate the service users of Parallels Operations Automations s
 * The process is as following:
 * User enters a "loginstring" and password in the UIs
 * The plugins takes the "loginstring" and tries to resolve the ox-username and ox-context.
 *
 * Here is how the resolving of context and username works:
 *
 *  See code for detailed description!
 *
 *
 *
 * @author Manuel Kraft
 *
 */
public class ParallelsOXAuthentication implements AuthenticationService {

    private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(ParallelsOXAuthentication.class);

    /**
     * Default constructor.
     */
    public ParallelsOXAuthentication() {
        super();
    }

    public static String getFromConfig(final String key) {
        final ConfigurationService configservice = Services.getService(ConfigurationService.class);
        return configservice.getProperty(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Authenticated handleLoginInfo(final LoginInfo loginInfo) throws OXException {



        String gui_loginstring = null;
        String gui_password = null;

        final Connection configdb_read;

        try {
            configdb_read = Database.get(false);
        } catch (OXException e) {
            LOG.error("Error while setting up internal database connection", e);
            throw LoginExceptionCodes.DATABASE_DOWN.create(e);
        }

        PreparedStatement prep = null;
        ResultSet rs = null;
        try {

            gui_loginstring = loginInfo.getUsername();
            gui_password = loginInfo.getPassword();



            if ("".equals(gui_loginstring.trim()) || "".equals(gui_password.trim()) || Strings.containsSurrogatePairs(gui_loginstring)) {
                throw LoginExceptionCodes.INVALID_CREDENTIALS.create();
            }

            LOG.debug("Now trying to resolve ox-username and ox-context...");

            /**
             * Resolve context and username via sql query against configdb
             *
             * Contexts are provisioned by Parallels in following:
             *
             * A context has loginmappings containing loginstrings suffixed with "||<ox_username>":
             *
             *
             *
             * Example: test@gmail.com||test
             *
             * So we can make a SQL Query with the loginstring:
             *
             * Select cid,login_info from login2context where login_info like "<loginstringfromgui>||%"
             *
             * As result we get for example "1337" as cid and "test@gmail.com||test" as login_info
             * if we used "test@gmail.com" as loginstring in the OX ui.
             *
             * Now we have the cid for the groupware and we have to split up the "login_info" at the "||"
             * to get the actual username , which would be "test".
             *
             * Now we have all 2 infos to load the user from the system to authenticate the database
             *
             */


            // Get a configdb read connection from pool

            prep = configdb_read.prepareStatement("SELECT cid,login_info from login2context where login_info like ?");
            prep.setString(1, gui_loginstring+"||%");

            rs = prep.executeQuery();
            String cid = null;
            String loginmapping = null;
            if (rs.next()){
                cid = rs.getString("cid");
                loginmapping = rs.getString("login_info");
            }else{
                LOG.error("Did not get any login_info mapping from configdb database for loginstring {}", gui_loginstring);
                LOG.error("Hint: Account \"{}\" not yet provsioned in OX?", gui_loginstring);
                LOG.error("This authentication request for loginstring {} will fail", gui_loginstring);
                throw LoginExceptionCodes.INVALID_CREDENTIALS.create();
            }

            final String[] tmp_ = loginmapping.split("\\|\\|");

            // only if we get 2 strings out of the split , then proceed
            if (tmp_.length!=2){
                LOG.error("handleLoginInfo: Could not split up login_info mapping correctly for mappingstring \"{}\" ", loginmapping);
                throw LoginExceptionCodes.INVALID_CREDENTIALS.create();
            }


            final String oxuser_ =	tmp_[1]; // ox-user name

            /**
             *  now try to load the context object and the user object from database
             *  Then auth against the database with given password
             *  If successfull return the username and context to the OX groupware
             *  else throw an invalid credentials loginexception
             */
            final UserService userservice = Services.getService(UserService.class);

            final ContextService contextservice = Services.getService(ContextService.class);

            final Context ctx = contextservice.getContext(Integer.parseInt(cid));
            final String real_context_name = gui_loginstring+"||"+oxuser_; // must be given to the groupware , else groupware does not know context if we pass the id of the context
            final int userId;
            try {
                userId = userservice.getUserId(oxuser_, ctx);
            } catch (OXException e) {
                LOG.error("UserID for {} could not be resolved via OX API from database. Not provisioned yet?", oxuser_,e);
                throw LoginExceptionCodes.INVALID_CREDENTIALS_MISSING_USER_MAPPING.create(oxuser_);
            }
            final User user = userservice.getUser(userId, ctx);
            if (!userservice.authenticate(user, gui_password)) {
                OXException e = LoginExceptionCodes.INVALID_CREDENTIALS.create();
                LOG.error("Invalid credentials");
                // FIXME: exception below is never logged for some reason :-(
                LOG.error("",e);
                throw e;
            }



            return new Authenticated() {
                @Override
                public String getContextInfo() {
                    return real_context_name;
                }

                @Override
                public String getUserInfo() {
                    return oxuser_;
                }
            };

        } catch (SQLException e) {
            LOG.error("Error in configdb query", e);
            throw LoginExceptionCodes.COMMUNICATION.create(e);
        } catch (NumberFormatException e) {
            LOG.error("Error in parsing context id from configdb", e);
            throw LoginExceptionCodes.INVALID_CREDENTIALS.create(e);
        } catch (OXException e) {
            LOG.error("Error in loading user or context from loginstring {} from OX API", gui_loginstring, e);
            throw LoginExceptionCodes.INVALID_CREDENTIALS.create(e);
        } finally {

            // close resultset and statement before releasing connection back to pool
            Databases.closeSQLStuff(rs, prep);

            // return configdb connection back to pool
            Database.back(false, configdb_read);

        }
    }

    @Override
    public Authenticated handleAutoLoginInfo(final LoginInfo loginInfo) throws OXException {
        throw LoginExceptionCodes.NOT_SUPPORTED.create(ParallelsOXAuthentication.class.getName());
    }

}
