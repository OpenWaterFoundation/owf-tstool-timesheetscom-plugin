package org.openwaterfoundation.tstool.plugin.timesheetscom.dto;

/**
 * Exception to encapsulate a non-200 HTTP code.
 */
@SuppressWarnings("serial")
public class HttpCodeException extends RuntimeException {
	
	/**
	 * The HTTP code.
	 */
	private int code = -1;
	
	/**
	 * Constructor.
	 * @param message exception message
	 * @param code the HTTP error code
	 */
	public HttpCodeException ( String message, int code ) {
		super(message);
		this.code = code;
	}

	/**
	 * Get the code.
	 * @return the HTTP error code
	 */
	public int getCode () {
		return this.code;
	}
}
