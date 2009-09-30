/*
COPYRIGHT STATUS:
  Dec 1st 2001, Fermi National Accelerator Laboratory (FNAL) documents and
  software are sponsored by the U.S. Department of Energy under Contract No.
  DE-AC02-76CH03000. Therefore, the U.S. Government retains a  world-wide
  non-exclusive, royalty-free license to publish or reproduce these documents
  and software for U.S. Government purposes.  All documents and software
  available from this server are protected under the U.S. and Foreign
  Copyright Laws, and FNAL reserves all rights.
 
 
 Distribution of the software available from this server is free of
 charge subject to the user following the terms of the Fermitools
 Software Legal Information.
 
 Redistribution and/or modification of the software shall be accompanied
 by the Fermitools Software Legal Information  (including the copyright
 notice).
 
 The user is asked to feed back problems, benefits, and/or suggestions
 about the software to the Fermilab Software Providers.
 
 
 Neither the name of Fermilab, the  URA, nor the names of the contributors
 may be used to endorse or promote products derived from this software
 without specific prior written permission.
 
 
 
  DISCLAIMER OF LIABILITY (BSD):
 
  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  "AS IS" AND ANY EXPRESS OR IMPLIED  WARRANTIES, INCLUDING, BUT NOT
  LIMITED TO, THE IMPLIED  WARRANTIES OF MERCHANTABILITY AND FITNESS
  FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL FERMILAB,
  OR THE URA, OR THE U.S. DEPARTMENT of ENERGY, OR CONTRIBUTORS BE LIABLE
  FOR  ANY  DIRECT, INDIRECT,  INCIDENTAL, SPECIAL, EXEMPLARY, OR
  CONSEQUENTIAL DAMAGES  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
  OF SUBSTITUTE  GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
  BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY  OF
  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
  NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT  OF THE USE OF THIS
  SOFTWARE, EVEN IF ADVISED OF THE  POSSIBILITY OF SUCH DAMAGE.
 
 
  Liabilities of the Government:
 
  This software is provided by URA, independent from its Prime Contract
  with the U.S. Department of Energy. URA is acting independently from
  the Government and in its own private capacity and is not acting on
  behalf of the U.S. Government, nor as its contractor nor its agent.
  Correspondingly, it is understood and agreed that the U.S. Government
  has no connection to this software and in no manner whatsoever shall
  be liable for nor assume any responsibility or obligation for any claim,
  cost, or damages arising out of or resulting from the use of the software
  available from this server.
 
 
  Export Control:
 
  All documents and software available from this server are subject to U.S.
  export control laws.  Anyone downloading information from this server is
  obligated to secure any necessary Government licenses before exporting
  documents or software obtained from this server.
 */

/*
 * FileRequest.java
 *
 * Created on July 5, 2002, 12:04 PM
 */

package org.dcache.srm.request;

import java.net.MalformedURLException;

import diskCacheV111.srm.RequestFileStatus;
import java.sql.SQLException;
import org.dcache.srm.FileMetaData;
import org.dcache.srm.AbstractStorageElement;
import org.globus.util.GlobusURL;
import org.dcache.srm.SRMUser;
import org.dcache.srm.SRMException;
import org.dcache.srm.scheduler.Job;
import org.dcache.srm.scheduler.JobStorage;
import org.dcache.srm.util.Configuration;
import org.dcache.srm.util.Tools;
import org.dcache.srm.GetFileInfoCallbacks;
import org.dcache.srm.PinCallbacks;
import org.dcache.srm.UnpinCallbacks;
import org.dcache.srm.scheduler.State;
import org.dcache.srm.scheduler.Scheduler;
import org.dcache.srm.scheduler.IllegalStateTransition;
import org.dcache.srm.scheduler.NonFatalJobFailure;
import org.dcache.srm.scheduler.FatalJobFailure;

import org.dcache.srm.v2_2.TStatusCode;
import org.dcache.srm.v2_2.TBringOnlineRequestFileStatus;
import org.dcache.srm.v2_2.TReturnStatus;
import org.apache.axis.types.URI;
import org.apache.log4j.Logger;
import org.dcache.srm.v2_2.TSURLReturnStatus;
import org.dcache.srm.SRMInvalidRequestException;
/**
 *
 * @author  timur
 * @version
 */
public class BringOnlineFileRequest extends FileRequest {
    private final static Logger _log = Logger.getLogger(BringOnlineFileRequest.class);
    
    // the globus url class created from surl_string
    private GlobusURL surl;
    private String pinId;
    private String fileId;
    private FileMetaData fileMetaData;
    
    private static final long serialVersionUID = -9155373723705753177L;
    
    /** Creates new FileRequest */
    public BringOnlineFileRequest(Long requestId,
    Long  requestCredentalId,
    String url,
    long lifetime,
    int maxNumberOfRetries
    
    ) throws Exception {
        super(requestId,
                requestCredentalId, 
                lifetime, 
                maxNumberOfRetries);
        say("BringOnlineFileRequest, requestId="+requestId+" fileRequestId = "+getId());
        try {
            surl = new GlobusURL(url);
            say("    surl = "+surl.getURL());
        }
        catch(MalformedURLException murle) {
            throw new IllegalArgumentException(murle.toString());
        }
        
    }
    /**
     * restore constructore, used for restoring the existing
     * file request from the database
     */
    
    public BringOnlineFileRequest(
    Long id,
    Long nextJobId,
    long creationTime,
    long lifetime,
    int stateId,
    String errorMessage,
    String scheduelerId,
    long schedulerTimeStamp,
    int numberOfRetries,
    int maxNumberOfRetries,
    long lastStateTransitionTime,
    JobHistory[] jobHistoryArray,
    Long requestId,
    Long  requestCredentalId,
    String statusCodeString,
    String SURL,
    String fileId,
    String pinId
    ) throws java.sql.SQLException {
        super(id,
        nextJobId,
        creationTime,
        lifetime,
        stateId,
        errorMessage,
        scheduelerId,
        schedulerTimeStamp,
        numberOfRetries,
        maxNumberOfRetries,
        lastStateTransitionTime, 
        jobHistoryArray,
        requestId,
        requestCredentalId,
        statusCodeString);
        
        try {
            this.surl = new GlobusURL(SURL);
        }
        catch(MalformedURLException murle) {
            throw new IllegalArgumentException(murle.toString());
        }
        
        if(fileId != null && (!fileId.equalsIgnoreCase("null"))) {
            this.fileId = fileId;
        }
        
        if(pinId != null && (!pinId.equalsIgnoreCase("null"))) {
            this.pinId = pinId;
        }
    }
    
    public void say(String s) {
        if(getStorage() != null) {
            getStorage().log("BringOnlineFileRequest reqId #"+requestId+" file#"+getId()+": "+s);
        }
        
    }
    
    public void esay(String s) {
        if(getStorage() != null) {
            getStorage().elog("BringOnlineFileRequest eqId #"+requestId+" file#"+getId()+": "+s);
        }
    }
    
    public void esay(Throwable t) {
        if(getStorage() != null) {
            getStorage().elog(t);
        }
    }
    
    public void setPinId(String pinId) {
        this.pinId = pinId;
    }
    
    public String getPinId() {
        return pinId;
    }
    
    public boolean isPinned() {
        return pinId != null;
    }
    
    
    
    public String getPath() {
        return getPath(surl);
    }
    
    
    public GlobusURL getSurl() {
        return surl;
    }
    
    
    public String getSurlString() {
        return surl.getURL();
    }
    
    public boolean canRead() throws SRMInvalidRequestException {
        if(fileId == null) {
            return false;
        }
        SRMUser user =(SRMUser) getUser();
        say("BringOnlineFileRequest calling storage.canRead()");
        return getStorage().canRead(user,fileId,fileMetaData);
    }
    
    
    public String getFileId() {
        return fileId;
    }
    
    
    public RequestFileStatus getRequestFileStatus(){
        RequestFileStatus rfs;
        if(fileMetaData != null) {
            rfs = new RequestFileStatus(fileMetaData);
        }
        else {
            rfs = new RequestFileStatus();
        }
        
        rfs.fileId = getId().intValue();
        rfs.SURL = getSurlString();
        
        
        if(this.isPinned()) {
            rfs.isPinned = true;
            rfs.isCached = true;
        }
        
        State state = getState();
        if(state == State.RQUEUED) {
            tryToReady();
            state = getState();
        }
        if(state == State.DONE) {
            rfs.state = "Done";
        }
        else if(state == State.READY) {
            rfs.state = "Ready";
        }
        else if(state == State.TRANSFERRING) {
            rfs.state = "Running";
        }
        else if(state == State.FAILED
        || state == State.CANCELED ) {
            rfs.state = "Failed";
        }
        else {
            rfs.state = "Pending";
        }
        
        //say(" returning requestFileStatus for "+rfs.toString());
        return rfs;
    }
    
    public TBringOnlineRequestFileStatus  getTGetRequestFileStatus() 
    throws java.sql.SQLException, SRMInvalidRequestException{
        TBringOnlineRequestFileStatus fileStatus = new TBringOnlineRequestFileStatus();
        if(fileMetaData != null) {
            fileStatus.setFileSize(new org.apache.axis.types.UnsignedLong(fileMetaData.size));
        }
         
        try {
             fileStatus.setSourceSURL(new URI(getSurlString()));
        } catch (Exception e) {
            esay(e);
            throw new java.sql.SQLException("wrong surl format");
        }
        
        if(this.isPinned()) {
            
            fileStatus.setRemainingPinTime(new Integer((int)(getRemainingLifetime()/1000)));
        }
        fileStatus.setEstimatedWaitTime(new Integer((int)(getRequest().getRetryDeltaTime())));
        TReturnStatus returnStatus = getReturnStatus();
        fileStatus.setStatus(returnStatus);
        
        return fileStatus;
    }

    public TSURLReturnStatus  getTSURLReturnStatus() throws java.sql.SQLException{
        TReturnStatus returnStatus = getReturnStatus();
        TSURLReturnStatus surlReturnStatus = new TSURLReturnStatus();
        try {
            surlReturnStatus.setSurl(new URI(getSurlString()));
        } catch (Exception e) {
            esay(e);
            throw new java.sql.SQLException("wrong surl format");
        }
        surlReturnStatus.setStatus(returnStatus);
        return surlReturnStatus;
    }
    
 
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(" BringOnlineFileRequest ");
        sb.append(" id =").append(getId());
        sb.append(" SURL=").append(surl.getURL());
        
        return sb.toString();
    }
    
    public synchronized void run() throws NonFatalJobFailure, FatalJobFailure {
        say("run()");
        try {
            if(fileId == null) {
                try {
                    if(!Tools.sameHost(getConfiguration().getSrmhost(),
                    getSurl().getHost())) {
                        String error ="surl is not local : "+getSurl().getURL();
                        esay(error);
                        throw new FatalJobFailure(error);
                    }
                }
                catch(java.net.UnknownHostException uhe) {
                    esay(uhe);
                    throw new FatalJobFailure(uhe.toString());
                }
                say("fileId is null, asking to get a fileId");
                askFileId();
                if(fileId == null) {
                   synchronized(this) {

                        State state = getState();
                        if(!State.isFinalState(state)) {
                            setState(State.ASYNCWAIT, "getting file Id");
                        }
                   }
                    say("BringOnlineFileRequest: waiting async notification about fileId...");
                    return;
                }
            }
            say("fileId = "+fileId);
            
            if(pinId == null) {
                if(!canRead()) {
                    
                     synchronized(this) {
                                State state = getState();
                            if(!State.isFinalState(state)) {
                                esay( "user "+getUser()+"has no permission to read "+fileId);
                                try {
                                    setState(State.FAILED,"user "+
                                            getUser()+"has no permission to read "+fileId);
                                }
                                catch(IllegalStateTransition ist) {
                                    esay("can not fail state:"+ist);
                                }
                            }
                       }
                     return;
                }

                say("pinId is null, asking to pin ");
                pinFile();
                if(pinId == null) {
                       synchronized(this) {

                            State state = getState();
                            if(!State.isFinalState(state)) {
                                setState(State.ASYNCWAIT,"pinning file");
                            }
                       }
                    say("BringOnlineFileRequest: waiting async notification about pinId...");
                    return;
                }
            }
        }
        catch (SRMInvalidRequestException ire) {
            esay(ire);
            throw new FatalJobFailure(ire.toString());
        }
        catch(IllegalStateTransition ist) {
            throw new NonFatalJobFailure(ist.toString());
        }
        say("PinId is "+pinId+" returning, scheduler should change" +
            " state to \"Ready\"");
        
    }
    
    public void askFileId() throws NonFatalJobFailure, FatalJobFailure {
        try {
            
            say(" proccessing the file request id "+getId());
            String  path =   getPath();
            say(" path is "+path);
            // if we can not read this path for some reason
            //(not in ftp root for example) this will throw exception
            // we do not care about the return value yet
            say("calling Job.getJob("+requestId+")");
            BringOnlineRequest request = (BringOnlineRequest) 
                Job.getJob(requestId);
            say("this file request's request is  "+request);
            //this will fail if the protocols are not supported
            if(request.protocols != null && request.protocols.length > 0) {
                String[] supported_prots = getStorage().supportedGetProtocols();
                boolean found_supp_prot=false;
                mark1:
                for(String supported_protocol: supported_prots) {
                    for(String request_protocol: request.protocols) {
                        if(supported_protocol.equals(request_protocol)) {
                            found_supp_prot = true;
                            break mark1;
                        }
                    }
                }
                if(!found_supp_prot) {
                    StringBuilder request_protocols = new StringBuilder("transfer protocols not supported: [");
                    for(String request_protocol: request.protocols ) {
                        request_protocols.append(request_protocol);
                        request_protocols.append(',');
                    }
                    int len = request_protocols.length();
                    request_protocols.replace(len-1, len,"]");
                    throw new FatalJobFailure(request_protocols.toString());
                }
            }
            //storage.getGetTurl(getUser(),path,request.protocols);
            say("storage.prepareToGet("+path+",...)");
            GetFileInfoCallbacks callbacks = new GetCallbacks(getId());
            getStorage().getFileInfo(getUser(),path,callbacks);
        }
        catch(Exception e) {
            esay(e);
            throw new NonFatalJobFailure(e.toString());
        }
    }
    
    public void pinFile() throws NonFatalJobFailure, FatalJobFailure {
        try {
            
            PinCallbacks callbacks = new ThePinCallbacks(getId());
            say("storage.pinFile("+fileId+",...)");
            long desiredPinLifetime =
                ((BringOnlineRequest)getRequest()).getDesiredOnlineLifetimeInSeconds();
            if(desiredPinLifetime != -1) {
                //convert to millis
                desiredPinLifetime *= 1000;
            }

            getStorage().pinFile(getUser(),
                fileId, 
                getRequest().getClient_host(),
                fileMetaData, 
                desiredPinLifetime, 
                getRequestId().longValue() ,
                callbacks);
        }
        catch(Exception e) {
            esay(e);
            throw new NonFatalJobFailure(e.toString());
        }
    }
    
    protected void stateChanged(org.dcache.srm.scheduler.State oldState) {
        State state = getState();
        say("State changed from "+oldState+" to "+getState());
        if(state == State.READY) {
            try {
                getRequest().resetRetryDeltaTime();
            }
            catch (SRMInvalidRequestException ire) {
                esay(ire);
            }
        }
        if(state == State.CANCELED || state == State.FAILED ) {
            if(fileId != null && pinId != null) {
                UnpinCallbacks callbacks = new TheUnpinCallbacks(this.getId());
                say("state changed to final state, unpinning fileId= "+ fileId+" pinId = "+pinId);
                try {
                    getStorage().unPinFile(getUser(),fileId, callbacks, pinId);
                }
                catch (SRMInvalidRequestException ire) {
                    esay(ire);
                    return;
                }
            }
        }
    }
    
    public TSURLReturnStatus releaseFile() throws SRMInvalidRequestException {
        TSURLReturnStatus surlReturnStatus = new TSURLReturnStatus();
        TReturnStatus returnStatus = new TReturnStatus();
        try {
            surlReturnStatus.setSurl(new URI(getSurlString()));
        } catch (Exception e) {
            esay(e);
           returnStatus.setExplanation("wrong surl format");
           returnStatus.setStatusCode(TStatusCode.SRM_INVALID_REQUEST);
           surlReturnStatus.setStatus(returnStatus);
           return surlReturnStatus;
        }
        synchronized(this) {
            State state = getState();
            if(!State.isFinalState(state)) {
                esay("Canceled by the srmReleaseFile");
                try {
                    this.setState(State.CANCELED, "Canceled by the srmReleaseFile");
                } catch (Exception e) {
                }
               returnStatus.setExplanation("srmBringOnline for this file has not completed yet,"+
                        " pending srmBringOnline canceled");
               returnStatus.setStatusCode(TStatusCode.SRM_FAILURE);
               surlReturnStatus.setStatus(returnStatus);
               return surlReturnStatus;

            }
        }
        
        if(fileId != null && pinId != null) {
            TheUnpinCallbacks callbacks = new TheUnpinCallbacks(this.getId());
            say("srmReleaseFile, unpinning fileId= "+ 
                    fileId+" pinId = "+pinId);
            getStorage().unPinFile(getUser(),fileId, callbacks, pinId);
            try {   
                callbacks.waitCompleteion(60000); //one minute
                if(callbacks.success) {
                    pinId = null;
                    this.saveJob();
                    returnStatus.setStatusCode(TStatusCode.SRM_SUCCESS);
                    surlReturnStatus.setStatus(returnStatus);
                    return surlReturnStatus;
                }
            } catch( InterruptedException ie) {
                ie.printStackTrace();
            }
            
           
           returnStatus.setExplanation(" srmReleaseFile failed: "+callbacks.getError());
           returnStatus.setStatusCode(TStatusCode.SRM_FAILURE);
           surlReturnStatus.setStatus(returnStatus);
           return surlReturnStatus;
        } else {
           returnStatus.setExplanation(" srmReleaseFile failed: file is not pinned");
           returnStatus.setStatusCode(TStatusCode.SRM_FAILURE);
           surlReturnStatus.setStatus(returnStatus);
           return surlReturnStatus;

        }
        
        
    }

    public TReturnStatus getReturnStatus() {
        TReturnStatus returnStatus = new TReturnStatus();
        
        State state = getState();
 	returnStatus.setExplanation(state.toString());

        if(getStatusCode() != null) {
            returnStatus.setStatusCode(getStatusCode());
        } else if(state == State.DONE) {
            if(pinId != null) {
                returnStatus.setStatusCode(TStatusCode.SRM_SUCCESS);
            }  else {
                returnStatus.setStatusCode(TStatusCode.SRM_RELEASED);
            }
        }
        else if(state == State.READY) {
            returnStatus.setStatusCode(TStatusCode.SRM_FILE_PINNED);
        }
        else if(state == State.TRANSFERRING) {
            returnStatus.setStatusCode(TStatusCode.SRM_FILE_PINNED);
        }
        else if(state == State.FAILED) {
	    returnStatus.setExplanation("FAILED: "+getErrorMessage());
            returnStatus.setStatusCode(TStatusCode.SRM_FAILURE);
        }
        else if(state == State.CANCELED ) {
            returnStatus.setStatusCode(TStatusCode.SRM_ABORTED);
        }
        else if(state == State.TQUEUED ) {
            returnStatus.setStatusCode(TStatusCode.SRM_REQUEST_QUEUED);
        }
        else if(state == State.RUNNING || 
                state == State.RQUEUED || 
                state == State.ASYNCWAIT ) {
            returnStatus.setStatusCode(TStatusCode.SRM_REQUEST_INPROGRESS);
        }
        else {
            returnStatus.setStatusCode(TStatusCode.SRM_REQUEST_QUEUED);
        }
        return returnStatus;
    }

    /**
     * 
     * @param newLifetime  new lifetime in millis
     *  -1 stands for infinite lifetime
     * @return int lifetime left in millis
     *    -1 stands for infinite lifetime
     */
    public long extendLifetime(long newLifetime) throws SRMException {
        long remainingLifetime = getRemainingLifetime();
        if(remainingLifetime >= newLifetime) {
            return remainingLifetime;
        }
        long requestLifetime = getRequest().extendLifetimeMillis(newLifetime);
        if(requestLifetime <newLifetime) {
            newLifetime = requestLifetime;
        }
        if(remainingLifetime >= newLifetime) {
            return remainingLifetime;
        }
        
        newLifetime = extendLifetimeMillis(newLifetime);   
        if(remainingLifetime >= newLifetime) {
            return remainingLifetime;
        }
        if(pinId == null) {
            return newLifetime;
        }
        SRMUser user =(SRMUser) getUser();
        getStorage().extendPinLifetime(user,fileId,pinId,newLifetime);
        return newLifetime;
    }
    
    
    
    private  static class GetCallbacks implements GetFileInfoCallbacks
    
    {
        
        Long fileRequestJobId;
        
        public GetCallbacks(Long fileRequestJobId) {
            this.fileRequestJobId = fileRequestJobId;
        }
        
        private BringOnlineFileRequest getBringOnlineFileRequest()   
                throws java.sql.SQLException, SRMInvalidRequestException {
            Job job = Job.getJob(fileRequestJobId);
            if(job != null) {
                return (BringOnlineFileRequest) job;
            }
            return null;
        }
        
        public void FileNotFound(String reason) {
            try {
                BringOnlineFileRequest fr = getBringOnlineFileRequest();
                try {
                   synchronized(fr) {

                        State state = fr.getState();
                        fr.setStatusCode(TStatusCode.SRM_INVALID_PATH);
                        if(!State.isFinalState(state)) {
                            fr.setState(State.FAILED,reason);
                        }
                   }
                }
                catch(IllegalStateTransition ist) {
                    fr.esay("can not fail state:"+ist);
                }
                fr.esay("GetCallbacks error: "+ reason);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        
        public void Error( String error) {
            try {
                BringOnlineFileRequest fr = getBringOnlineFileRequest();
                try {
                   synchronized(fr) {

                        State state = fr.getState();
                        if(!State.isFinalState(state)) {
                            fr.setState(State.FAILED,error);
                        }
                   }
                }
                catch(IllegalStateTransition ist) {
                    fr.esay("can not fail state:"+ist);
                }
                fr.esay("GetCallbacks error: "+ error);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        
        public void Exception( Exception e) {
            try {
                BringOnlineFileRequest fr = getBringOnlineFileRequest();
                try {
                   synchronized(fr) {

                        State state = fr.getState();
                        if(!State.isFinalState(state)) {
                            fr.setState(State.FAILED,e.toString());
                        }
                   }
                }
                catch(IllegalStateTransition ist) {
                    fr.esay("can not fail state:"+ist);
                }
                fr.esay("GetCallbacks exception");
                fr.esay(e);
            }
            catch(Exception e1) {
                e1.printStackTrace();
            }
        }
        
        public void GetStorageInfoFailed(String reason) {
            try {
                BringOnlineFileRequest fr = getBringOnlineFileRequest();
                try {
                   synchronized(fr) {

                        State state = fr.getState();
                        if(!State.isFinalState(state)) {
                            fr.setState(State.FAILED,reason);
                        }
                   }
                }
                catch(IllegalStateTransition ist) {
                    fr.esay("can not fail state:"+ist);
                }
                
                fr.esay("GetCallbacks error: "+ reason);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            
        }
        
        
        
        public void StorageInfoArrived(String fileId,FileMetaData fileMetaData) {            
            try {
                if (fileMetaData.isDirectory) {
                    FileNotFound("Path is a directory");
                    return;
                }
                BringOnlineFileRequest fr = getBringOnlineFileRequest();
                fr.say("StorageInfoArrived: FileId:"+fileId);
                State state ;
                synchronized(fr) {
                    state = fr.getState();
                }
                
                if(state == State.ASYNCWAIT || state == State.RUNNING) {
                    fr.fileId = fileId;
                    fr.fileMetaData = fileMetaData;
                    if(state == State.ASYNCWAIT) {
                        Scheduler scheduler = Scheduler.getScheduler(fr.getSchedulerId());
                        try {
                            scheduler.schedule(fr);
                        }
                        catch(Exception ie) {
                            fr.esay(ie);
                        }
                    }
                }
                
            }
            catch(Exception e) {
                e.printStackTrace();
            }
            
        }
        
        
        public void Timeout() {
            try {
                BringOnlineFileRequest fr = getBringOnlineFileRequest();
                try {
                   synchronized(fr) {

                        State state = fr.getState();
                        if(!State.isFinalState(state)) {
                            fr.setState(State.FAILED,"GetCallbacks Timeout");
                        }
                   }
                }
                catch(IllegalStateTransition ist) {
                    fr.esay("can not fail state:"+ist);
                }
                
                fr.esay("GetCallbacks Timeout");
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        
    }
    
    
    private  static class ThePinCallbacks implements PinCallbacks {
        
        Long fileRequestJobId;
        
        public ThePinCallbacks(Long fileRequestJobId) {
            this.fileRequestJobId = fileRequestJobId;
        }
        
        public BringOnlineFileRequest getBringOnlineFileRequest() 
                throws java.sql.SQLException, SRMInvalidRequestException {
            Job job = Job.getJob(fileRequestJobId);
            if(job != null) {
                return (BringOnlineFileRequest) job;
            }
            return null;
        }
        
        public void Error( String error) {
            try {
                BringOnlineFileRequest fr = getBringOnlineFileRequest();
                try {
                   synchronized(fr) {

                        State state = fr.getState();
                        if(!State.isFinalState(state)) {
                            fr.setState(State.FAILED,error);
                        }
                   }
                }
                catch(IllegalStateTransition ist) {
                    fr.esay("can not fail state:"+ist);
                }
                fr.esay("ThePinCallbacks error: "+ error);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        
        public void Exception( Exception e) {
            try {
                BringOnlineFileRequest fr = getBringOnlineFileRequest();
                try {
                   synchronized(fr) {

                        State state = fr.getState();
                        if(!State.isFinalState(state)) {
                            fr.setState(State.FAILED,e.toString());
                        }
                   }
                }
                catch(IllegalStateTransition ist) {
                    fr.esay("can not fail state:"+ist);
                }
                fr.esay("ThePinCallbacks exception");
                fr.esay(e);
            }
            catch(Exception e1) {
                e1.printStackTrace();
            }
        }
        
        
        
        
        public void Timeout() {
            try {
                BringOnlineFileRequest fr = getBringOnlineFileRequest();
                try {
                   synchronized(fr) {

                        State state = fr.getState();
                        if(!State.isFinalState(state)) {
                            fr.setState(State.FAILED,"ThePinCallbacks Timeout");
                        }
                   }
                }
                catch(IllegalStateTransition ist) {
                    fr.esay("can not fail state:"+ist);
                }
                
                fr.esay("GetCallbacks Timeout");
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        
        public void Pinned(String pinId) {
            try {
                BringOnlineFileRequest fr = getBringOnlineFileRequest();
                State state;
               fr.say("ThePinCallbacks: Pinned() pinId:"+pinId);
                synchronized(fr ) {
                    fr.pinId = pinId;
                    fr.setState(State.DONE," file is pinned, pinId="+pinId);
                }
            }
            catch (SRMInvalidRequestException ire) {
                _log.error("BringOnlineFileRequest failed: " + ire.getMessage());
            }
            catch(SQLException e) {
                _log.error("BringOnlineFileRequest failed: " + e.getMessage());
            }
            catch(IllegalStateTransition e) {
                _log.error("BringOnlineFileRequest failed: " + e.getMessage());
            }
        }
        
        public void PinningFailed(String reason) {
            try {
                BringOnlineFileRequest fr = getBringOnlineFileRequest();
                try {
                    fr.setState(State.FAILED,reason);
                }
                catch(IllegalStateTransition ist) {
                    fr.esay("can not fail state:"+ist);
                }
                
                fr.esay("ThePinCallbacks error: "+ reason);
            }
            catch(Exception e) {
                e.printStackTrace();
            }
        }
        
    }
    
    private static class TheUnpinCallbacks implements UnpinCallbacks {
        
        Long fileRequestJobId;
        
        public TheUnpinCallbacks(Long fileRequestJobId) {
            this.fileRequestJobId = fileRequestJobId;
        }
        
        public BringOnlineFileRequest getBringOnlineFileRequest() 
                throws java.sql.SQLException, SRMInvalidRequestException {
            if(fileRequestJobId != null) {
                Job job = Job.getJob(fileRequestJobId);
                if(job != null) {
                    return (BringOnlineFileRequest) job;
                }
            }
            return null;
        }
        
        public void Error( String error) {
            try {
                BringOnlineFileRequest fr = getBringOnlineFileRequest();
                /*
                 * unpin is called when the file request  is already 
                 * in a final state
                 */
                /*
                 try {
                    //fr.setState(State.FAILED);
                }
                catch(IllegalStateTransition ist) {
                    //fr.esay("can not fail state:"+ist);
                }
                 */
                this.error = "TheUnpinCallbacks error: "+ error;
                if(fr != null) {
                    fr.esay(this.error);
                }
                success = false;
                done();
            }
            catch(Exception e) {
                e.printStackTrace();
                done();
            }
        }
        
        public void Exception( Exception e) {
            try {
                BringOnlineFileRequest fr = getBringOnlineFileRequest();
                /*
                 * unpin is called when the file request  is already 
                 * in a final state
                 */
                /*
                try {
                    //fr.setState(State.FAILED);
                }
                catch(IllegalStateTransition ist) {
                    //fr.esay("can not fail state:"+ist);
                }
                 */
                if(fr != null) {
                    fr.esay("TheUnpinCallbacks exception");
                    fr.esay(e);
                }
                this.error = "TheUninCallbacks exception: "+e.toString();
                success = false;
                done();
            }
            catch(Exception e1) {
                e1.printStackTrace();
                done();
            }
        }
        
        
        
        
        public void Timeout() {
            try {
                BringOnlineFileRequest fr = getBringOnlineFileRequest();
                /*
                 * unpin is called when the file request  is already 
                 * in a final state
                 */
                /*
                try {
                    //fr.setState(State.FAILED);
                }
                catch(IllegalStateTransition ist) {
                    //fr.esay("can not fail state:"+ist);
                }
                 */
                
                this.error = "TheUninCallbacks Timeout";
                if(fr  != null) {
                    fr.esay(this.error);
               }
                success = false;
                done();
                
            }
            catch(Exception e) {
                e.printStackTrace();
                done();
            }
        }
        
        public void Unpinned(String pinId) {
            try {
                BringOnlineFileRequest fr = getBringOnlineFileRequest();
                if(fr != null) {
                    fr.say("TheUnpinCallbacks: Unpinned() pinId:"+pinId);
                    State state;
                    synchronized(fr ) {
                        state = fr.getState();
                    }
                   if(state == State.ASYNCWAIT) {
                        fr.pinId = pinId;
                        Scheduler scheduler = Scheduler.getScheduler(fr.getSchedulerId());
                        try {
                            scheduler.schedule(fr);
                        }
                        catch(Exception ie) {
                            fr.esay(ie);
                        }
                    }
                }
                success = true;
                done();
            }
            catch(Exception e) {
                e.printStackTrace();
                done();
            }
        }
        
        public void UnpinningFailed(String reason) {
            try {
                BringOnlineFileRequest fr = getBringOnlineFileRequest();
                /*
                 * unpin is called when the file request  is already 
                 * in a final state
                 */
                /*
                try {
                    //fr.setState(State.FAILED);
                }
                catch(IllegalStateTransition ist) {
                    //fr.esay("can not fail state:"+ist);
                }
                 */
                
                this.error = "TheUnpinCallbacks error: "+ reason;
                if(fr  != null) {
                    fr.esay(this.error);
                }
                success = false;
                done();

            }
            catch(Exception e) {
                e.printStackTrace();
                done();
            }
        }
        
        private boolean done = false;
        private boolean success  = true;
        private String error;
        
        public boolean isSuccess() {
            return done && success;
        }
        public  boolean waitCompleteion(long timeout) throws InterruptedException {
           long starttime = System.currentTimeMillis();
            while(true) {
                synchronized(this) {
                    wait(1000);
                    if(done) {
                        return success;
                    }
                    else
                    {
                        if((System.currentTimeMillis() - starttime)>timeout) {
                            error = " TheUnpinCallbacks Timeout";
                            return false;
                        }
                    }
                }
            }
        }
        
        public  synchronized void done() {
            done = true;
            notifyAll();
        }

        public java.lang.String getError() {
            return error;
        }      
        
        public boolean isDone() {
            return done;
        }

        
    }
    public static void unpinBySURLandRequestId(
        AbstractStorageElement storage,
        final SRMUser user, 
        final long id,
        final String surl_string) throws SRMException, MalformedURLException {
        GlobusURL surl = new GlobusURL(surl_string);
        String path = FileRequest.getPath(surl);
        
        FileMetaData fmd =
            storage.getFileMetaData(user,path);
        String fileId = fmd.fileId;
        if(fileId != null) {
            BringOnlineFileRequest.TheUnpinCallbacks unpinCallbacks = 
                new BringOnlineFileRequest.TheUnpinCallbacks(null);
            storage.unPinFileBySrmRequestId(user,
                fileId,unpinCallbacks,id);
          try {   
                unpinCallbacks.waitCompleteion(60000); //one minute
                if(unpinCallbacks.isDone()) {
                    
                    if(unpinCallbacks.isSuccess()) {
                        return;
                    } else 
                    throw new SRMException("unpinning of "+surl_string+" by SrmRequestId "+id+
                        " failed :"+unpinCallbacks.getError());
                } else {
                    throw new SRMException("unpinning of "+surl_string+" by SrmRequestId "+id+
                        " took too long");
                    
                }
            } catch( InterruptedException ie) {
                ie.printStackTrace();
                throw new SRMException("unpinning of "+surl_string+" by SrmRequestId "+id+
                        " got interrupted");
            }
         }
    }

    public static void unpinBySURL(
        AbstractStorageElement storage,
        final SRMUser user, 
        final String surl_string) 
        throws SRMException, 
            MalformedURLException {
        GlobusURL surl = new GlobusURL(surl_string);
        String path = FileRequest.getPath(surl);        
        FileMetaData fmd =
            storage.getFileMetaData(user,path);
        String fileId = fmd.fileId;
        if(fileId != null) {
            BringOnlineFileRequest.TheUnpinCallbacks unpinCallbacks = 
                new BringOnlineFileRequest.TheUnpinCallbacks(null);
            storage.unPinFile(user,
                fileId,unpinCallbacks);
          try {   
                unpinCallbacks.waitCompleteion(60000); //one minute
                if(unpinCallbacks.isDone()) {
                    if(unpinCallbacks.isSuccess()) {
                        return;
                    } else {
                        throw new SRMException("unpinning of "+surl_string+
                            " failed :"+unpinCallbacks.getError());
                    }
                } else {
                        throw new SRMException("unpinning of "+surl_string+
                            " took too long");
                }
            } catch( InterruptedException ie) {
                ie.printStackTrace();
                throw new SRMException("unpinning of "+surl_string+
                        " got interrupted");
            }
         }
    }
}
