package statalign.base.thread;

/**
 * Exception to be thrown by StoppableThread's stoppable() method when termination of the
 * thread has been previously requested by an external thread (by calling stopSoft()).
 * This exception should be caught for final do up before termination.
 * 
 * @author novak
 *
 */
public class StoppedException extends Exception {

	private static final long serialVersionUID = 1L;
	
	private String errorMessage;
	
	public StoppedException(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	public String getError() {
		return errorMessage;
	}
	
	public String getMessage() {
		return "Cannot stop during the burn-in procedure!";
	}
	
}
