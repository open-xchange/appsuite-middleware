
package com.openexchange.admin.tools;

import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Hashtable;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openexchange.admin.daemons.ClientAdminThread;
import com.openexchange.admin.exceptions.OXGenericException;
import com.openexchange.admin.rmi.exceptions.PoolException;


public class AdminDaemonTools {
    
    private static Log log = LogFactory.getLog( AdminDaemonTools.class );
    
    public static boolean checkValidStoreURI( String uriToCheck ) {
        boolean isOK = true;
        
        try {
            URI.create( uriToCheck );
            isOK = true;
        } catch ( IllegalArgumentException e ) {
            // given string violates RFC 2396
            isOK = false;
        } catch ( NullPointerException e ) {
            // given uri is null
            isOK = false;
        }
        
        return isOK;
    }
    
    public static String stringReplacer( String source, String find, String replacement ) {
        int i = 0;
        int j;
        int k = find.length();
        int m = replacement.length();
        
        while ( i < source.length() ) {
            j = source.indexOf( find, i );
            
            if ( j == -1 ) {
                break;
            }
            
            if ( j == 0 ) {
                source = replacement + source.substring( j + k );
            } else if ( j + k == source.length() ) {
                source = source.substring( 0, j ) + replacement;
            } else {
                source = source.substring( 0, j ) + replacement + source.substring( j + k );
            }
            i = j + m;
        }
        
        return source;
    }
    
    public static void checkNeeded( Hashtable sendDATA, String[] needed_field ) throws OXGenericException {
        Vector<String> missingFields = new Vector<String>();
        
        for ( int i = 0; i < needed_field.length; i++ ) {
            if ( !sendDATA.containsKey( needed_field[ i ] ) ) {
                missingFields.add( needed_field[ i ] );
            }
        }
        
        if ( missingFields.size() > 0 ) {
            throw new OXGenericException( OXGenericException.KEY_MISSING + ": " + missingFields );
        }
    }
    
    public static void checkEmpty( Hashtable sendDATA, String[] needed_field ) throws OXGenericException {
        Vector<String> emptyFields = new Vector<String>();
        
        if ( sendDATA == null || sendDATA.size() <= 0 ) {
            throw new OXGenericException( OXGenericException.NULL_EMPTY );
        }
        
        for ( int i = 0; i < needed_field.length; i++ ) {
            
            if ( sendDATA.containsKey( needed_field[ i ] ) ) {
                Object sd = sendDATA.get( needed_field[ i ] );
                
                if( sd == null || ( sd instanceof String  &&
                        ( (String)sendDATA.get( needed_field[ i ] ) ).trim().length() == 0 ))
                    emptyFields.add( needed_field[ i ] );
            }
            
        }
        
        if ( emptyFields.size() > 0 ) {
            throw new OXGenericException( OXGenericException.NULL_EMPTY + ": " + emptyFields );
        }
    }
    
    
    //FIXME: d7 remove this function when old rmi interface is removed
    public static boolean existsResource( int context_ID, String identifier,int resource_id ) throws SQLException, PoolException {
        boolean         retBool = false;
        AdminCache      cache   = ClientAdminThread.cache;
        Connection      con     = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            con = cache.getWRITEConnectionForContext( context_ID );
            prep_check = con.prepareStatement( "SELECT id FROM resource WHERE cid = ? AND identifier = ? OR id = ?" );
            prep_check.setInt( 1, context_ID );
            prep_check.setString( 2, identifier );
            prep_check.setInt(3,resource_id);
            
            rs = prep_check.executeQuery();
            if(rs.next()){
                retBool = true;
            }else{
                retBool = false;
            }
            
        } finally {
            try {
                if(prep_check!=null){
                    prep_check.close();
                }
            } catch ( Exception e ) {
                log.error("Error closing prepared statement!",e);
            }
            
            try {
                cache.pushOXDBWrite(context_ID,con);
                //con.close();
            } catch ( Exception e ) {
                log.error("Error pushing ox db write connection to pool!",e);
            }
            
            if(rs!=null){
                rs.close();
            }
        }
        
        return retBool;
    }
    
    //FIXME: d7 remove this function when old rmi interface is removed
    public static boolean existsResource( int context_ID, int resource_id ) throws SQLException, PoolException {
        boolean         retBool = false;
        AdminCache      cache   = ClientAdminThread.cache;
        Connection      con     = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            con = cache.getWRITEConnectionForContext( context_ID );
            prep_check = con.prepareStatement( "SELECT id FROM resource WHERE cid = ? AND id = ?" );
            prep_check.setInt( 1, context_ID );
            prep_check.setInt(2,resource_id);
            rs = prep_check.executeQuery();
            if(rs.next()){
                retBool = true;
            }else{
                retBool = false;
            }
            
        } finally {
            if(rs!=null){
                rs.close();
            }
            try {
                if(prep_check!=null){
                    prep_check.close();
                }
            } catch ( Exception e ) {
                log.error("Error closing prepared statement!",e);
            }
            
            try {
                cache.pushOXDBWrite(context_ID,con);
                //con.close();
            } catch ( Exception e ) {
                log.error("Error pushing ox db write connection to pool!",e);
            }
            
        }
        
        return retBool;
    }
    
    //FIXME: d7 remove this function when old rmi interface is removed
    public static boolean existsGroup( int context_ID, int gid ) throws SQLException, PoolException {
        boolean         retBool = false;
        AdminCache      cache   = ClientAdminThread.cache;
        Connection      con     = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            con = cache.getWRITEConnectionForContext( context_ID );
            
            prep_check = con.prepareStatement( "SELECT id FROM groups WHERE cid = ? AND id = ?;" );
            prep_check.setInt( 1, context_ID );
            prep_check.setInt( 2, gid );
            
            rs = prep_check.executeQuery();
            if(rs.next()){
                retBool = true;
            }else{
                retBool = false;
            }
        } finally {
            if(rs!=null){
                rs.close();
            }
            try {
                if(prep_check!=null){
                    prep_check.close();
                }
            } catch ( Exception e ) {
                log.error("Error closing prepared statement!",e);
            }
            
            try {
                cache.pushOXDBWrite(context_ID,con);
                //con.close();
            } catch ( Exception e ) {
                log.error("Error pushing ox db write connection to pool!",e);
            }
            
        }
        
        return retBool;
    }
    
    //FIXME: d7 remove this function when old rmi interface is removed
    public static boolean existsGroup( int context_ID, String identifier ) throws SQLException, PoolException {
        boolean         retBool = false;
        AdminCache      cache   = ClientAdminThread.cache;
        Connection      con     = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            con = cache.getWRITEConnectionForContext( context_ID );
            
            prep_check = con.prepareStatement( "SELECT id FROM groups WHERE cid = ? AND identifier = ?" );
            prep_check.setInt( 1, context_ID );
            prep_check.setString( 2, identifier );
            rs = prep_check.executeQuery();
            if(rs.next()){
                retBool = true;
            }else{
                retBool = false;
            }
        } finally {
            if(rs!=null){
                rs.close();
            }
            try {
                if(prep_check!=null){
                    prep_check.close();
                }
            } catch ( Exception e ) {
                log.error("Error closing prepared statement!",e);
            }
            
            try {
                cache.pushOXDBWrite(context_ID,con);
            } catch ( Exception e ) {
                log.error("Error pushing ox db write connection to pool!",e);
            }
            
        }
        
        return retBool;
    }
    
    //FIXME: d7 remove this function when old rmi interface is removed
    public static boolean existsGroupMember(int context_id,int group_ID,int[] user_ids) throws PoolException, SQLException {
        boolean ret = false;
        Connection      con     = null;
        AdminCache      cache   = ClientAdminThread.cache;
        ResultSet rs = null;
        PreparedStatement prep = null;
        try{
            StringBuffer sb = new StringBuffer();
            for(int a = 0;a< user_ids.length;a++){
                sb.append(user_ids[a]+",");
            }
            sb.delete(sb.length()-1,sb.length());
            con = cache.getWRITEConnectionForContext(context_id);
            prep = con.prepareStatement("SELECT member FROM groups_member WHERE cid = ? AND id = ? AND member IN ("+sb.toString()+")");
            prep.setInt(1,context_id);
            prep.setInt(2,group_ID);
            rs = prep.executeQuery();
            if(rs.next()){
                // one of the members is already in this group
                ret = true;
            }
            prep.close();
        }finally{
            if(rs!=null){
                try{
                    rs.close();
                } catch (Exception e) {
                    log.error("Error closing resultset",e);
                }
            }
            try {
                if(prep!=null){
                    prep.close();
                }
            } catch (Exception e) {
                log.error("Error closing statement",e);
            }
            if(con!=null){
                try{
                    cache.pushOXDBWrite(context_id,con);
                    
                }catch(Exception ecp){
                    log.error("Error pushing ox db write connection to pool!",ecp);
                }
            }
            
        }
        return ret;
    }
    
    //FIXME: d7 remove this function when old rmi interface is removed
    public static boolean existsGroupMember( int context_ID, int group_ID, int member_ID ) throws SQLException, PoolException {
        boolean         retBool = false;
        AdminCache      cache   = ClientAdminThread.cache;
        Connection      con     = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            con = cache.getWRITEConnectionForContext( context_ID );
            prep_check = con.prepareStatement( "SELECT id FROM groups_member WHERE cid = ? AND id = ? AND member = ?" );
            prep_check.setInt( 1, context_ID );
            prep_check.setInt( 2, group_ID );
            prep_check.setInt( 3, member_ID );
            
            rs = prep_check.executeQuery();
            if(rs.next()){
                retBool = true;
            }else{
                retBool = false;
            }
        } finally {
            if(rs!=null){
                try{
                    rs.close();
                } catch (Exception e) {
                    log.error("Error closing resultset",e);
                }
            }
            try {
                if(prep_check!=null){
                    prep_check.close();
                }
            } catch ( Exception e ) {
                log.error("Error closing prepared statement!",e);
            }
            
            try {
                cache.pushOXDBWrite(context_ID,con);
            } catch ( Exception e ) {
                log.error("Error pushing ox db write connection to pool!",e);
            }
            
        }
        
        return retBool;
    }
    
    //FIXME: d7 remove this function when old rmi interface is removed
    public static boolean existsUser( int context_ID, String username ) throws SQLException, PoolException {
        boolean         retBool = false;
        AdminCache      cache   = ClientAdminThread.cache;
        Connection      con     = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            con = cache.getWRITEConnectionForContext( context_ID );
            prep_check = con.prepareStatement( "SELECT id FROM login2user WHERE cid = ? AND uid = ?" );
            prep_check.setInt( 1, context_ID );
            prep_check.setString( 2, username );
            rs = prep_check.executeQuery();
            if(rs.next()){
                retBool = true;
            }else{
                retBool = false;
            }
        } finally {
            if(rs!=null){
                try{
                    rs.close();
                } catch (Exception e) {
                    log.error("Error closing resultset",e);
                }
            }
            try {
                if(prep_check!=null){
                    prep_check.close();
                }
            } catch ( Exception e ) {
                log.error("Error closing prepared statement!",e);
            }
            
            try {
                cache.pushOXDBWrite(context_ID,con);
                //con.close();
            } catch ( Exception e ) {
                log.error("Error pushing ox db write connection to pool!",e);
            }
            
        }
        
        return retBool;
    }
    
    //FIXME: d7 remove this function when old rmi interface is removed
    public static boolean existsUser( int context_ID, int uid ) throws SQLException, PoolException {
        boolean         retBool = false;
        AdminCache      cache   = ClientAdminThread.cache;
        Connection      con     = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            con = cache.getWRITEConnectionForContext( context_ID );
            prep_check = con.prepareStatement( "SELECT id FROM user WHERE cid = ? AND id = ?;" );
            prep_check.setInt( 1, context_ID );
            prep_check.setInt( 2, uid );
            
            rs = prep_check.executeQuery();
            if(rs.next()){
                retBool = true;
            }else{
                retBool = false;
            }
        } finally {
            if(rs!=null){
                try{
                    rs.close();
                } catch (Exception e) {
                    log.error("Error closing resultset",e);
                }
            }
            try {
                if(prep_check!=null){
                    prep_check.close();
                }
            } catch ( Exception e ) {
                log.error("Error closing prepared statement!",e);
            }
            
            try {
                cache.pushOXDBWrite(context_ID,con);
            } catch ( Exception e ) {
                log.error("Error pushing ox db write connection to pool!",e);
            }
            
            
        }
        
        return retBool;
    }
    
    //FIXME: d7 remove this function when old rmi interface is removed
    public static boolean isMasterDatabase(int database_id ) throws SQLException, PoolException {
        boolean         retBool = false;
        AdminCache      cache   = ClientAdminThread.cache;
        Connection      con     = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            con = cache.getWRITEConnectionForCONFIGDB();
            prep_check = con.prepareStatement( "SELECT cluster_id FROM db_cluster WHERE write_db_pool_id = ?" );
            prep_check.setInt( 1, database_id );
            
            rs = prep_check.executeQuery();
            if(rs.next()){
                retBool = true;
            }else{
                retBool = false;
            }
            
        } finally {
            if(rs!=null){
                try{
                    rs.close();
                } catch (Exception e) {
                    log.error("Error closing resultset",e);
                }
            }
            try {
                if(prep_check!=null){
                    prep_check.close();
                }
            } catch ( Exception e ) {
                log.error("Error closing prepared statement!",e);
            }
            
            try {
                cache.pushConfigDBWrite(con);
            } catch ( Exception e ) {
                log.error("Error pushing configdb write connection to pool!",e);
            }
            
        }
        
        return retBool;
    }
    
    //FIXME: d7 remove this function when old rmi interface is removed
    public static boolean existsReason(long rid ) throws SQLException, PoolException {
        boolean         retBool = false;
        AdminCache      cache   = ClientAdminThread.cache;
        Connection      con     = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            con = cache.getWRITEConnectionForCONFIGDB();
            prep_check = con.prepareStatement( "SELECT id FROM reason_text WHERE id = ?;" );
            prep_check.setLong( 1, rid );
            
            rs = prep_check.executeQuery();
            if(rs.next()){
                retBool = true;
            }else{
                retBool = false;
            }
            
        } finally {
            if(rs!=null){
                try{
                    rs.close();
                } catch (Exception e) {
                    log.error("Error closing resultset",e);
                }
            }
            try {
                if(prep_check!=null){
                    prep_check.close();
                }
            } catch ( Exception e ) {
                log.error("Error closing prepared statement!",e);
            }
            
            try {
                cache.pushConfigDBWrite(con);
            } catch ( Exception e ) {
                log.error("Error pushing configdb write connection to pool!",e);
            }
            
        }
        
        return retBool;
    }
    
    //FIXME: d7 remove this function when old rmi interface is removed
    public static boolean existsReason(String reason) throws SQLException, PoolException {
        boolean         retBool = false;
        AdminCache      cache   = ClientAdminThread.cache;
        Connection      con     = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            con = cache.getWRITEConnectionForCONFIGDB();
            prep_check = con.prepareStatement("SELECT id FROM reason_text WHERE text LIKE \"" + reason + "\";" );
            
            rs = prep_check.executeQuery();
            if(rs.next()){
                retBool = true;
            }else{
                retBool = false;
            }
            
        } finally {
            if(rs!=null){
                try{
                    rs.close();
                } catch (Exception e) {
                    log.error("Error closing resultset",e);
                }
            }
            try {
                if(prep_check!=null){
                    prep_check.close();
                }
            } catch ( Exception e ) {
                log.error("Error closing prepared statement!",e);
            }
            
            try {
                cache.pushConfigDBWrite(con);
            } catch ( Exception e ) {
                log.error("Error pushing configdb write connection to pool!",e);
            }
            
            
            
        }
        
        return retBool;
    }
    
    //FIXME: d7 remove this function when old rmi interface is removed
    public static boolean existsUser(int context_id,int[] user_ids) throws PoolException, SQLException{
        boolean ret = false;
        Connection      con     = null;
        AdminCache      cache   = ClientAdminThread.cache;
        ResultSet rs = null;
        PreparedStatement prep = null;
        try{
            StringBuffer sb = new StringBuffer();
            for(int a = 0;a< user_ids.length;a++){
                sb.append(user_ids[a]+",");
            }
            sb.delete(sb.length()-1,sb.length());
            con = cache.getWRITEConnectionForContext(context_id);
            prep = con.prepareStatement("SELECT id FROM user WHERE cid = ? AND id IN ("+sb.toString()+")");
            prep.setInt(1,context_id);
            rs = prep.executeQuery();
            int count = 0;
            while(rs.next()){
                count++;
            }
            rs.close();
            
            if(count==user_ids.length){
                // ok, die user gibts alle
                ret = true;
            }
        }finally{
            
            try{
                if(prep!=null){
                    prep.close();
                }
            }catch(Exception e){
                log.error("Error closing statement",e);
            }
            
            if(con!=null){
                try{
                    cache.pushOXDBWrite(context_id,con);
                }catch(Exception ecp){
                    log.error("Error pushing ox db write connection to pool!",ecp);
                }
            }
            
        }
        return ret;
    }
    
    //FIXME: d7 remove this function when old rmi interface is removed
    public static boolean existsContext( long context_ID ) throws SQLException, PoolException {
        boolean         retBool = false;
        AdminCache      cache   = ClientAdminThread.cache;
        Connection      con     = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            con = cache.getWRITEConnectionForCONFIGDB();
            prep_check = con.prepareStatement( "SELECT cid FROM context WHERE cid = ?;" );
            prep_check.setLong( 1, context_ID );
            
            rs = prep_check.executeQuery();
            if(rs.next()){
                retBool = true;
            }else{
                retBool = false;
            }
            
        } finally {
            if(rs!=null){
                try{
                    rs.close();
                } catch (Exception e) {
                    log.error("Error closing resultset",e);
                }
            }
            try {
                if(prep_check!=null){
                    prep_check.close();
                }
            } catch ( Exception e ) {
                log.error("Error closing prepared statement!",e);
            }
            
            try {
                cache.pushConfigDBWrite(con);
            } catch ( Exception e ) {
                log.error("Error pushing configdb write connection to pool!",e);
            }
            
        }
        
        return retBool;
    }
    
    //FIXME: d7 remove this function when old rmi interface is removed
    public static boolean isContextAdmin(int context_id,int user_id) throws PoolException, SQLException {
        Connection con = null;
        AdminCache      cache   = ClientAdminThread.cache;
        boolean isadmin = false;
        try {
            con = cache.getREADConnectionForContext(context_id);
            int a = getAdminForContext(context_id,con);
            if(a==user_id){
                isadmin = true;
            }
        }finally{
            try {
                if(con!=null){
                    cache.pushOXDBRead(context_id,con);
                }
            } catch ( Exception e ) {
                log.error("Error pushing oxdb read connection to pool!",e);
            }
        }
        return isadmin;
    }
    
    //FIXME: d7 remove this function when old rmi interface is removed
    public static int getAdminForContext(int context_id,Connection con) throws SQLException{
        int admin_id = 1;
        
        PreparedStatement stmt = null;
        ResultSet rs = null;
        try {
            //con = cache.getREADConnectionForContext(context_id);
            stmt = con.prepareStatement("SELECT user FROM user_setting_admin WHERE cid = ?");
            stmt.setInt( 1, context_id );
            rs = stmt.executeQuery();
            if(rs.next()){
                admin_id = rs.getInt("user");
            } else {
                throw new SQLException("UNABLE TO GET MAILADMIN ID FOR CONTEXT " + context_id);
            }
        } finally {
            if(rs!=null){
                try{
                    rs.close();
                } catch (Exception e) {
                    log.error("Error closing resultset",e);
                }
            }
            try {
                if(stmt!=null){
                    stmt.close();
                }
            } catch ( Exception e ) {
                log.error("Error closing prepared statement!",e);
            }
            
        }
        
        return admin_id;
        
    }
    
    //FIXME: d7 remove this function when old rmi interface is removed
    public static long getDefaultGroupForContext(int context_id, Connection con) throws SQLException {
        int group_id = 0;
        
        PreparedStatement stmt = null;
        ResultSet rs  = null;
        try {
            stmt = con.prepareStatement("SELECT MIN(id) FROM groups WHERE cid=?");
            stmt.setInt( 1, context_id );
            rs = stmt.executeQuery();
            if(rs.next()){
                group_id = rs.getInt("MIN(id)");
            } else {
                throw new SQLException("UNABLE TO GET DEFAULT GROUP FOR CONTEXT " + context_id);
            }
        } finally {
            try {
                rs.close();
            } catch ( Exception e ) {
                log.error("Error closing resultset!",e);
            }
            try {
                if(stmt!=null){
                    stmt.close();
                }
            } catch ( Exception e ) {
                log.error("Error closing prepared statement!",e);
            }
            
            
        }
        
        return group_id;
    }
    
    //FIXME: d7 remove this function when old rmi interface is removed
    public static boolean existsServerID( int check_ID, String table, String field ) throws SQLException, PoolException {
        boolean retBool = false;
        
        AdminCache      cache   = ClientAdminThread.cache;
        Connection      con     = null;
        PreparedStatement prep_check = null;
        
        try {
            con = cache.getWRITEConnectionForCONFIGDB();
            prep_check = con.prepareStatement( "SELECT server_id FROM " + table + " WHERE " + field + " = ?;" );
            prep_check.setInt( 1, check_ID );
            
            
            ResultSet rs = prep_check.executeQuery();
            if(rs.next()){
                retBool = true;
            }else{
                retBool = false;
            }
            rs.close();
        } finally {
            try {
                if(prep_check!=null){
                    prep_check.close();
                }
            } catch ( Exception e ) {
                log.error("Error closing prepared statement!",e);
            }
            
            try {
                cache.pushConfigDBWrite(con);
            } catch ( Exception e ) {
                log.error("Error pushing configdb write connection to pool!",e);
            }
        }
        
        return retBool;
    }
    
    //FIXME: d7 remove this function when old rmi interface is removed
    public static boolean existsServer( long server_id ) throws SQLException, PoolException {
        boolean         retBool = false;
        AdminCache      cache   = ClientAdminThread.cache;
        Connection      con     = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            con = cache.getWRITEConnectionForCONFIGDB();
            prep_check = con.prepareStatement( "SELECT server_id FROM server WHERE server_id = ?" );
            prep_check.setLong( 1, server_id );
            
            rs = prep_check.executeQuery();
            if(rs.next()){
                retBool = true;
            }else{
                retBool = false;
            }
        } finally {
            if(rs!=null){
                try{
                    rs.close();
                } catch (Exception e) {
                    log.error("Error closing resultset",e);
                }
            }
            try {
                if(prep_check!=null){
                    prep_check.close();
                }
            } catch ( Exception e ) {
                log.error("Error closing prepared statement!",e);
            }
            
            try {
                cache.pushConfigDBWrite(con);
            } catch ( Exception e ) {
                log.error("Error pushing configdb write connection to pool!",e);
            }
        }
        
        return retBool;
    }
    
    //FIXME: d7 remove this function when old rmi interface is removed
    public static boolean existsServer( String server_name ) throws SQLException, PoolException {
        boolean         retBool = false;
        AdminCache      cache   = ClientAdminThread.cache;
        Connection      con     = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            con = cache.getWRITEConnectionForCONFIGDB();
            prep_check = con.prepareStatement( "SELECT server_id FROM server WHERE name = ?;" );
            prep_check.setString( 1, server_name );
            
            rs = prep_check.executeQuery();
            if(rs.next()){
                retBool = true;
            }else{
                retBool = false;
            }
        } finally {
            if(rs!=null){
                try{
                    rs.close();
                } catch (Exception e) {
                    log.error("Error closing resultset",e);
                }
            }
            try {
                if(prep_check!=null){
                    prep_check.close();
                }
            } catch ( Exception e ) {
                log.error("Error closing prepared statement!",e);
            }
            
            try {
                cache.pushConfigDBWrite(con);
            } catch ( Exception e ) {
                log.error("Error pushing configdb write connection to pool!",e);
            }
            
            
        }
        
        return retBool;
    }
    
    //FIXME: d7 remove this function when old rmi interface is removed
    public static boolean existsDatabase( long db_id ) throws SQLException, PoolException {
        boolean retval = false;
        
        AdminCache      cache   = ClientAdminThread.cache;
        Connection      con     = null;
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            con = cache.getWRITEConnectionForCONFIGDB();
            stmt = con.prepareStatement("SELECT name FROM db_pool WHERE db_pool_id = ?");
            stmt.setLong(1,db_id);
            rs = stmt.executeQuery();
            if(rs.next()){
                retval = true;
            }
        }finally{
            if(rs!=null){
                try{
                    rs.close();
                } catch (Exception e) {
                    log.error("Error closing resultset",e);
                }
            }
            try {
                if(stmt!=null){
                    stmt.close();
                }
            } catch (Exception e) {
                log.error("Error closing statement",e);
            }
            if(con!=null){
                try{
                    cache.pushConfigDBWrite(con);
                }catch(Exception ecp){
                    log.error("Error pushing configdb write connection to pool!",ecp);
                }
            }
            
        }
        
        
        return retval;
    }
    
    // TODO: d7: superfluous
    public static boolean existsCluster( int cl_id ) throws SQLException, PoolException {
        boolean retval = false;
        
        AdminCache      cache   = ClientAdminThread.cache;
        Connection      con     = null;
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            con = cache.getWRITEConnectionForCONFIGDB();
            stmt = con.prepareStatement("SELECT cluster_id FROM db_cluster WHERE cluster_id = ?");
            stmt.setInt(1,cl_id);
            rs = stmt.executeQuery();
            if(rs.next()){
                retval = true;
            }
        }finally{
            if(rs!=null){
                try{
                    rs.close();
                } catch (Exception e) {
                    log.error("Error closing resultset",e);
                }
            }
            try {
                if(stmt!=null){
                    stmt.close();
                }
            } catch (Exception e) {
                log.error("Error closing statment",e);
            }
            
            if(con!=null){
                try{
                    cache.pushConfigDBWrite(con);
                }catch(Exception ecp){
                    log.error("Error pushing configdb write connection to pool!",ecp);
                }
            }
            
        }
        
        
        return retval;
    }
    
    //FIXME: d7 remove this function when old rmi interface is removed
    public static boolean existsStore( long store_id ) throws SQLException, PoolException {
        boolean retval = false;
        
        AdminCache      cache   = ClientAdminThread.cache;
        Connection      con     = null;
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            con = cache.getWRITEConnectionForCONFIGDB();
            stmt = con.prepareStatement("SELECT uri FROM filestore WHERE id = ?");
            stmt.setLong(1,store_id);
            rs = stmt.executeQuery();
            if(rs.next()){
                retval = true;
            }
        }finally{
            if(rs!=null){
                try{
                    rs.close();
                } catch (Exception e) {
                    log.error("Error closing resultset",e);
                }
            }
            try {
                if(stmt!=null){
                    stmt.close();
                }
            } catch (Exception e) {
                log.error("Error closing statment",e);
            }
            
            if(con!=null){
                try{
                    cache.pushConfigDBWrite(con);
                }catch(Exception ecp){
                    log.error("Error pushing configdb write connection to pool!",ecp);
                }
            }
            
        }
        
        return retval;
    }
    
    //FIXME: d7 remove this function when old rmi interface is removed
    public static boolean existsStore( String url ) throws SQLException, PoolException {
        boolean retval = false;
        
        AdminCache      cache   = ClientAdminThread.cache;
        Connection      con     = null;
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try {
            con = cache.getWRITEConnectionForCONFIGDB();
            stmt = con.prepareStatement("SELECT uri FROM filestore WHERE uri = ?");
            stmt.setString(1,url);
            rs = stmt.executeQuery();
            if(rs.next()){
                retval = true;
            }
        }finally{
            if(rs!=null){
                try{
                    rs.close();
                } catch (Exception e) {
                    log.error("Error closing resultset",e);
                }
            }
            try {
                if(stmt!=null){
                    stmt.close();
                }
            } catch (Exception e) {
                log.error("Error closing statment",e);
            }
            
            if(con!=null){
                try{
                    cache.pushConfigDBWrite(con);
                }catch(Exception ecp){
                    log.error("Error pushing configdb write connection to pool!",ecp);
                }
            }
            
        }
        
        return retval;
    }

    //FIXME: d7 remove this function when old rmi interface is removed
    public static boolean existsDatabase( String db_name ) throws SQLException, PoolException {
        boolean         retBool = false;
        AdminCache      cache   = ClientAdminThread.cache;
        Connection      con     = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            con = cache.getWRITEConnectionForCONFIGDB();
            prep_check = con.prepareStatement( "SELECT db_pool_id FROM db_pool WHERE name = ?;" );
            prep_check.setString( 1, db_name );
            
            rs = prep_check.executeQuery();
            if(rs.next()){
                retBool = true;
            }else{
                retBool = false;
            }
            
        } finally {
            if(rs!=null){
                try{
                    rs.close();
                } catch (Exception e) {
                    log.error("Error closing resultset",e);
                }
            }
            try {
                if(prep_check!=null){
                    prep_check.close();
                }
            } catch ( Exception e ) {
                log.error("Error closing prepared statement!",e);
            }
            
            try {
                cache.pushConfigDBWrite(con);
            } catch ( Exception e ) {
                log.error("Error pushing configdb write connection to pool!",e);
            }
            
        }
        
        return retBool;
    }
    
    //FIXME: d7 remove this function when old rmi interface is removed
    public static boolean isContextEnabled(int context_ID) throws PoolException, SQLException  {
        boolean retBool = false;
        AdminCache cache = ClientAdminThread.cache;
        Connection con = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            con = cache.getWRITEConnectionForCONFIGDB();
            prep_check = con.prepareStatement("SELECT enabled FROM context WHERE cid = ?;");
            prep_check.setInt(1, context_ID);
            rs = prep_check.executeQuery();
            if( rs.next() ) {
                retBool = rs.getBoolean("enabled");
            } else {
                throw new SQLException("UNABLE TO QUERY CONTEXT STATUS");
            }
            
        } finally {
            if(rs!=null){
                try{
                    rs.close();
                } catch (Exception e) {
                    log.error("Error closing resultset",e);
                }
            }
            try {
                if(prep_check!=null){
                    prep_check.close();
                }
            } catch (Exception e) {
                log.error("Error closing prepared statement!",e);
            }
            
            try {
                cache.pushConfigDBWrite(con);
                //con.close();
            } catch (Exception e) {
                log.error("Error pushing configdb write connection to pool!",e);
            }
        }
        
        return retBool;
    }
    
    //FIXME: d7 remove this function when old rmi interface is removed
    public static boolean storeInUse( long store_id ) throws SQLException, PoolException {
        boolean         retBool = false;
        AdminCache      cache   = ClientAdminThread.cache;
        Connection      con     = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            con = cache.getWRITEConnectionForCONFIGDB();
            prep_check = con.prepareStatement( "SELECT cid FROM context WHERE filestore_id = ?" );
            prep_check.setLong( 1, store_id );
            
            rs = prep_check.executeQuery();
            if(rs.next()){
                retBool = true;
            }else{
                retBool = false;
            }
        } finally {
            if(rs!=null){
                try{
                    rs.close();
                } catch (Exception e) {
                    log.error("Error closing resultset",e);
                }
            }
            try {
                if(prep_check!=null){
                    prep_check.close();
                }
            } catch ( Exception e ) {
                log.error("Error closing prepared statement!",e);
            }
            
            try {
                cache.pushConfigDBWrite(con);
            } catch ( Exception e ) {
                log.error("Error pushing configdb write connection to pool!",e);
            }
        }
        
        return retBool;
    }

    //FIXME: d7 remove this function when old rmi interface is removed
    public static boolean poolInUse( long pool_id ) throws SQLException, PoolException {
        boolean         retBool = false;
        AdminCache      cache   = ClientAdminThread.cache;
        Connection      con     = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            con = cache.getWRITEConnectionForCONFIGDB();
            prep_check = con.prepareStatement( "SELECT cid FROM context_server2db_pool WHERE write_db_pool_id = ? OR read_db_pool_id = ?" );
            prep_check.setLong( 1, pool_id );
            prep_check.setLong( 2, pool_id );
            
            rs = prep_check.executeQuery();
            if(rs.next()){
                retBool = true;
            }else{
                retBool = false;
            }
        } finally {
            if(rs!=null){
                try{
                    rs.close();
                } catch (Exception e) {
                    log.error("Error closing resultset",e);
                }
            }
            try {
                if(prep_check!=null){
                    prep_check.close();
                }
            } catch ( Exception e ) {
                log.error("Error closing prepared statement!",e);
            }
            
            try {
                cache.pushConfigDBWrite(con);
            } catch ( Exception e ) {
                log.error("Error pushing configdb write connection to pool!",e);
            }
        }
        
        return retBool;
    }
    
    //FIXME: d7 remove this function when old rmi interface is removed
    public static boolean serverInUse( long server_id ) throws SQLException, PoolException {
        boolean         retBool = false;
        AdminCache      cache   = ClientAdminThread.cache;
        Connection      con     = null;
        PreparedStatement prep_check = null;
        ResultSet rs = null;
        try {
            con = cache.getWRITEConnectionForCONFIGDB();
            prep_check = con.prepareStatement( "SELECT cid FROM context_server2db_pool WHERE server_id = ?" );
            prep_check.setLong( 1, server_id );
            
            rs = prep_check.executeQuery();
            if(rs.next()){
                retBool = true;
            }else{
                retBool = false;
            }
        } finally {
            if(rs!=null){
                try{
                    rs.close();
                } catch (Exception e) {
                    log.error("Error closing resultset",e);
                }
            }
            try {
                if(prep_check!=null){
                    prep_check.close();
                }
            } catch ( Exception e ) {
                log.error("Error closing prepared statement!",e);
            }
            
            try {
                cache.pushConfigDBWrite(con);
            } catch ( Exception e ) {
                log.error("Error pushing configdb write connection to pool!",e);
            }
        }
        
        return retBool;
    }
}
