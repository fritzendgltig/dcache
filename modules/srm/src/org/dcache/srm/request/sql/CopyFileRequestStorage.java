// $Id$
// $Log: not supported by cvs2svn $
// Revision 1.8  2007/01/06 00:23:55  timur
// merging production branch changes to database layer to improve performance and reduce number of updates
//
/*
 * CopyFileRequestStorage.java
 *
 * Created on June 17, 2004, 4:49 PM
 */

package org.dcache.srm.request.sql;
import java.sql.*;
import org.dcache.srm.request.FileRequest;
import org.dcache.srm.request.CopyFileRequest;
import org.dcache.srm.util.Configuration;
import org.dcache.srm.scheduler.State;
import org.globus.util.GlobusURL;
import org.dcache.srm.scheduler.Job;

/**
 *
 * @author  timur
 */
public class CopyFileRequestStorage extends DatabaseFileRequestStorage {
    public static final String TABLE_NAME="copyfilerequests";
    private static final String UPDATE_PREFIX = "UPDATE " + TABLE_NAME + " SET "+
        "NEXTJOBID=?, " +
        "CREATIONTIME=?,  " +
        "LIFETIME=?, " +
        "STATE=?, " +
        "ERRORMESSAGE=?, " +//5
        "SCHEDULERID=?, " +
        "SCHEDULERTIMESTAMP=?," +
        "NUMOFRETR=?," +
        "MAXNUMOFRETR=?," +
        "LASTSTATETRANSITIONTIME=? ";//10

    public PreparedStatement getStatement(Connection connection, 
                                          String query, 
                                          Job job) throws SQLException { 
        CopyFileRequest request = (CopyFileRequest)job;
        PreparedStatement stmt = getPreparedStatement(connection,
                                  query,
                                  request.getNextJobId(),
                                  request.getCreationTime(),
                                  request.getLifetime(),
                                  request.getState().getStateId(),
                                  request.getErrorMessage(),
                                  request.getSchedulerId(),
                                  request.getSchedulerTimeStamp(),
                                  request.getNumberOfRetries(),
                                  request.getMaxNumberOfRetries(),
                                  request.getLastStateTransitionTime(),//10
                                  request.getRequestId(),
                                  request.getCredentialId(),
                                  request.getStatusCodeString(),
                                  request.getFromURL(),
                                  request.getToURL(),
                                  (request.getFrom_turl()!=null?request.getFrom_turl().getURL():null),
                                  (request.getTo_turl()!=null?request.getTo_turl().getURL():null),
                                  request.getLocal_from_path(),
                                  request.getLocal_to_path(),
                                  request.getSize(),//20
                                  request.getFromFileId(),
                                  request.getToFileId(),
                                  request.getRemoteRequestId(),
                                  request.getRemoteFileId(),
                                  request.getSpaceReservationId(),
                                  request.getTransferId(),
                                  request.getId());
        return stmt;
    }

    private static final String UPDATE_REQUEST_SQL = UPDATE_PREFIX + ", REQUESTID=?, "+
            "CREDENTIALID=?, "+
            "STATUSCODE=?, "+
            "FROMURL=? ,"+
            "TOURL =?,"+
            "FROMTURL=? ,"+
            "TOTURL=? ,"+
            "FROMLOCALPATH=? ,"+
            "TOLOCALPATH=? ,"+
            "SIZE=? ,"+  //20
            "FROMFILEID=? ,"+
            "TOFILEID=? ,"+
            "REMOTEREQUESTID=? ,"+
            "REMOTEFILEID=? , "+
            "SPACERESERVATIONID=? , "+
            "TRANSFERID=?   "+
            "WHERE ID=? ";//27
    
    public PreparedStatement getUpdateStatement(Connection connection, 
                                                Job job) 
        throws SQLException { 
        if(job == null || !(job instanceof CopyFileRequest)) {
            throw new IllegalArgumentException("job is not CopyFileRequest" );
        }
        CopyFileRequest request = (CopyFileRequest)job;
        return getStatement(connection,UPDATE_REQUEST_SQL, request);
    }
        private static final String INSERT_SQL = "INSERT INTO "+ TABLE_NAME+ "(    " +
            "ID ,"+
            "NEXTJOBID ,"+
            "CREATIONTIME ,"+
            "LIFETIME ,"+
            "STATE ,"+ //5
            "ERRORMESSAGE ,"+
            "SCHEDULERID ,"+
            "SCHEDULERTIMESTAMP ,"+
            "NUMOFRETR ,"+
            "MAXNUMOFRETR ,"+ //10
            "LASTSTATETRANSITIONTIME,"+
            //DATABASE FILE REQUEST STORAGE
            "REQUESTID , " +
            "CREDENTIALID , "+
            "STATUSCODE , "+
            "FROMURL ,"+ //15
            "TOURL ,"+
            "FROMTURL ,"+
            "TOTURL ,"+
            "FROMLOCALPATH ,"+
            "TOLOCALPATH ,"+ //20
            "SIZE ,"+
            "FROMFILEID ,"+
            "TOFILEID ,"+
            "REMOTEREQUESTID ,"+
            "REMOTEFILEID , "+ //25
            "SPACERESERVATIONID , "+
            "TRANSFERID )  "+
            "VALUES (?,?,?,?,?,?,?,?,?,?," +
                    "?,?,?,?,?,?,?,?,?,?," +
                    "?,?,?,?,?,?,?)";
    
    public PreparedStatement getCreateStatement(Connection connection, 
                                                Job job) 
        throws SQLException {
        if(job == null || !(job instanceof CopyFileRequest)) {
            throw new IllegalArgumentException("fr is not CopyFileRequest" );
        }
        CopyFileRequest request = (CopyFileRequest)job;
        PreparedStatement stmt = getPreparedStatement(connection,
                                  INSERT_SQL,
                                  request.getId(),
                                  request.getNextJobId(),
                                  request.getCreationTime(),
                                  request.getLifetime(),
                                  request.getState().getStateId(),
                                  request.getErrorMessage(),
                                  request.getSchedulerId(),
                                  request.getSchedulerTimeStamp(),
                                  request.getNumberOfRetries(),
                                  request.getMaxNumberOfRetries(),
                                  request.getLastStateTransitionTime(),
                                  request.getRequestId(),
                                  request.getCredentialId(),
                                  request.getStatusCodeString(),
                                  request.getFromURL(),
                                  request.getToURL(),
                                  (request.getFrom_turl()!=null?request.getFrom_turl().getURL():null),
                                  (request.getTo_turl()!=null?request.getTo_turl().getURL():null),
                                  request.getLocal_from_path(),
                                  request.getLocal_to_path(),
                                  request.getSize(),
                                  request.getFromFileId(),
                                  request.getToFileId(),
                                  request.getRemoteRequestId(),
                                  request.getRemoteFileId(),
                                  request.getSpaceReservationId(),
                                  request.getTransferId());
        return stmt;
    }

    
    /** Creates a new instance of CopyFileRequestStorage */
    public CopyFileRequestStorage(Configuration configuration) throws SQLException {
        super(configuration        );
    }
    
      
    public void say(String s){
        if(logger != null) {
           logger.log(" CopyFileRequestStorage: "+s);
        }
    }
    
    public void esay(String s){
        if(logger != null) {
           logger.elog(" CopyFileRequestStorage: "+s);
        }
    }
    
    public void esay(Throwable t){
        if(logger != null) {
           logger.elog(t);
        }
    }
  
    protected FileRequest getFileRequest(
    Connection _con,
    Long ID,
    Long NEXTJOBID,
    long CREATIONTIME,
    long LIFETIME,
    int STATE,
    String ERRORMESSAGE,
    String SCHEDULERID,
    long SCHEDULER_TIMESTAMP,
    int NUMOFRETR,
    int MAXNUMOFRETR,
    long LASTSTATETRANSITIONTIME,
    Long REQUESTID,
    Long CREDENTIALID,
    String STATUSCODE,
    java.sql.ResultSet set,
    int next_index)throws java.sql.SQLException {
            
        String FROMURL = set.getString(next_index++);
        String TOURL = set.getString(next_index++);
        String FROMTURL = set.getString(next_index++);
        String TOTURL = set.getString(next_index++);
        String FROMLOCALPATH = set.getString(next_index++);
        String TOLOCALPATH = set.getString(next_index++);
        long size = set.getLong(next_index++);
        String fromFileId = set.getString(next_index++);
        String toFileId = set.getString(next_index++);
        String REMOTEREQUESTID = set.getString(next_index++);
        String REMOTEFILEID = set.getString(next_index++);
        String SPACERESERVATIONID = set.getString(next_index++);
        String TRANSFERID = set.getString(next_index);
        Job.JobHistory[] jobHistoryArray = 
        getJobHistory(ID,_con);
        
   
           return new CopyFileRequest(
            ID,
            NEXTJOBID ,
            this,
            CREATIONTIME,
            LIFETIME,
            STATE,
            ERRORMESSAGE,
            SCHEDULERID,
            SCHEDULER_TIMESTAMP,
            NUMOFRETR,
            MAXNUMOFRETR,
            LASTSTATETRANSITIONTIME,
            jobHistoryArray,
            REQUESTID,
            CREDENTIALID,
            STATUSCODE,
            FROMURL,
            TOURL,
             FROMTURL,
             TOTURL,
             FROMLOCALPATH,
             TOLOCALPATH,
             size,
             fromFileId,
             toFileId,
             REMOTEREQUESTID,
             REMOTEFILEID,
             SPACERESERVATIONID,
             TRANSFERID);
    }
    
    public String getFileRequestCreateTableFields() {
        return                     
        ","+
        "FROMURL "+  stringType+
        ","+
        "TOURL "+  stringType+
        ","+
        "FROMTURL "+  stringType+
        ","+
        "TOTURL "+  stringType+
        ","+
        "FROMLOCALPATH "+  stringType+
        ","+
        "TOLOCALPATH "+  stringType+
        ","+
        "SIZE "+  longType+
        ","+
        "FROMFILEID "+  stringType+
        ","+
        "TOFILEID "+  stringType+
        ","+
        "REMOTEREQUESTID "+  stringType+
        ","+
        "REMOTEFILEID "+  stringType+
         ","+
        "SPACERESERVATIONID "+  stringType+
        ","+
        "TRANSFERID "+ stringType;
  }
    private static int ADDITIONAL_FIELDS = 13;
    public String getTableName() {
        return TABLE_NAME;
    }
    
   
     public String getRequestTableName() {
          return CopyRequestStorage.TABLE_NAME;
     }     
     
     protected void __verify(int nextIndex, int columnIndex, String tableName, String columnName, int columnType) throws SQLException {
        /*","+
        ","+
        "FROMURL "+  stringType+
        ","+
        "TOURL "+  stringType+
        ","+
        "FROMTURL "+  stringType+
        ","+
        "TOTURL "+  stringType+
        ","+
        "FROMLOCALPATH "+  stringType+
        ","+
        "TOLOCALPATH "+  stringType+
        ","+
        "SIZE "+  longType+
        ","+
        "FROMFILEID "+  stringType+
        ","+
        "TOFILEID "+  stringType+
        ","+
        "REMOTEREQUESTID "+  stringType+
        ","+
        "REMOTEFILEID "+  stringType+
         ","+
        "SPACERESERVATIONID "+  stringType+
         ","+
        "TRANSFERID "+  stringType;
        */
        if(columnIndex == nextIndex) {
            verifyStringType("FROMURL",columnIndex,tableName, columnName, columnType);
        }
        else if(columnIndex == nextIndex+1)
        {
            verifyStringType("TOURL",columnIndex,tableName, columnName, columnType);
            
        }
        else if(columnIndex == nextIndex+2)
        {
            verifyStringType("FROMTURL",columnIndex,tableName, columnName, columnType);
        }
        else if(columnIndex == nextIndex+3)
        {
            verifyStringType("TOTURL",columnIndex,tableName, columnName, columnType);
        }
        else if(columnIndex == nextIndex+4)
        {
            verifyStringType("FROMLOCALPATH",columnIndex,tableName, columnName, columnType);
        }
        else if(columnIndex == nextIndex+5)
        {
            verifyStringType("TOLOCALPATH",columnIndex,tableName, columnName, columnType);
        }
        else if(columnIndex == nextIndex+6)
        {
            verifyLongType("SIZE",columnIndex,tableName, columnName, columnType);
        }
        else if(columnIndex == nextIndex+7)
        {
            verifyStringType("FROMFILEID",columnIndex,tableName, columnName, columnType);
        }
        else if(columnIndex == nextIndex+8)
        {
            verifyStringType("TOFILEID",columnIndex,tableName, columnName, columnType);
        }
        else if(columnIndex == nextIndex+9)
        {
            verifyStringType("REMOTEREQUESTID",columnIndex,tableName, columnName, columnType);
        }
        else if(columnIndex == nextIndex+10)
        {
            verifyStringType("REMOTEFILEID",columnIndex,tableName, columnName, columnType);
        }
        else if(columnIndex == nextIndex+11)
        {
            verifyStringType("SPACERESERVATIONID",columnIndex,tableName, columnName, columnType);
        }
       else if(columnIndex == nextIndex+12)
        {
            verifyStringType("TRANSFERID",columnIndex,tableName, columnName, columnType);
        }
        else {
            throw new SQLException("database table schema changed:"+
                    "table named "+tableName+
                    " column #"+columnIndex+" has name \""+columnName+
                    "\"  has type \""+getTypeName(columnType)+
                    " this column should not be present!!!");
        }
     }
     
     protected int getMoreCollumnsNum() {
         return ADDITIONAL_FIELDS;
     }
     
}
