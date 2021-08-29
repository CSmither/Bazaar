package de.ancash.bazaar.exception;

public class AsyncBazaarException extends RuntimeException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8790435020735545883L;

	public AsyncBazaarException(String str, Exception ex) {
		super(str, ex);
	}
}