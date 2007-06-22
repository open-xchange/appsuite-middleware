
package com.openexchange.admin.jobs;

import java.io.Serializable;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * @author choeger
 *
 */
public class AdminJob extends FutureTask<Vector> implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public enum Mode {
		MOVE_DATABASE,
		MOVE_FILESTORE
	}

	public static final int WAITING = 0;
	public static final int RUNNING = 1;
	public static final int DONE    = 2;
    public static final int FAILED  = 3;

    private Log log = LogFactory.getLog( this.getClass() );
	private int m_destination = -1;
	private int m_context = -1;
	private int m_reason = -1;
	private Integer state = WAITING;
	// Stores a the mode
	private Mode m_mode = null;
	private Callable<Vector> cbobj;
	
	public AdminJob(Callable<Vector> callable) {
		super(callable);
		this.cbobj = callable;
	}

	/**
	 * set's this.state to FAILED, if job did fail
	 */
	private void setState() {
		try {
			Vector retVec = this.get();
			if( retVec.get(0).equals("ERROR") ) {
				synchronized( this.state ) {
					this.state = FAILED;
				}
			}
		} catch (InterruptedException e) {
			log.error(e);
			synchronized( this.state ) {
				this.state = FAILED;
			}
		} catch (ExecutionException e) {
			log.error(e);
			synchronized( this.state ) {
				this.state = FAILED;
			}
		}
	}

	public boolean isDone() {
		setState();
		synchronized( this.state ) {
			return this.state == DONE;
		}
	}
	
	public boolean isFailed() {
		setState();
		synchronized( this.state ) {
			return this.state == FAILED;
		}
	}

	public boolean isRunning() {
		synchronized( this.state ) {
			return this.state == RUNNING;
		}
	}

	public int getPercentDone() {
		if( this.cbobj instanceof I_AdminProgressEnabledJob ) {
			return ((I_AdminProgressEnabledJob)this.cbobj).getPercentDone();
		}
		return 0;
	}
	
	@Override
	public void run() {
		synchronized( this.state ) {
			this.state = RUNNING;
		}
		super.run();
		synchronized( this.state ) {
			this.state = DONE;
		}
	}
	
	public int getDestination() {
		return m_destination;
	}

	public void setDestination(int destination) {
		this.m_destination = destination;
	}

	public int getContext() {
		return m_context;
	}

	public void setContext(int context) {
		m_context = context;
	}

	public Mode getMode() {
		return m_mode;
	}

	public void setMode(Mode mode) {
		m_mode = mode;
	}

	public int getReason() {
		return m_reason;
	}

	public void setReason(int reason) {
		m_reason = reason;
	}

}
