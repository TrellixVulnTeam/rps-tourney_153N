package com.justdavis.karl.rpstourney.service.client;

import javax.ws.rs.core.Response.StatusType;

/**
 * Indicates that an unexpected HTTP {@link StatusType} response was received.
 */
public final class HttpClientException extends RuntimeException {
	private static final long serialVersionUID = -7808416965381512945L;

	private final StatusType status;

	/**
	 * Constructs a new {@link HttpClientException}.
	 * 
	 * @param status
	 *            the unexpected HTTP {@link StatusType} response that was
	 *            received
	 */
	public HttpClientException(StatusType status) {
		super(String.format("Unexpected HTTP %d status on response: %s",
				status.getStatusCode(), status.getReasonPhrase()));
		this.status = status;
	}

	/**
	 * @return the unexpected HTTP {@link StatusType} response that was
	 *         received, and caused this {@link HttpClientException}
	 */
	public StatusType getStatus() {
		return status;
	}
}
