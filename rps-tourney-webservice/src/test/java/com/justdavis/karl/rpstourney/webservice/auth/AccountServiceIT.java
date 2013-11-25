package com.justdavis.karl.rpstourney.webservice.auth;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.cxf.jaxrs.client.WebClient;
import org.junit.After;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import com.justdavis.karl.rpstourney.webservice.EmbeddedServerResource;
import com.justdavis.karl.rpstourney.webservice.WebClientHelper;
import com.justdavis.karl.rpstourney.webservice.auth.guest.GuestAuthService;

/**
 * Integration tests for {@link AccountService}.
 */
public final class AccountServiceIT {
	@ClassRule
	public static EmbeddedServerResource server = new EmbeddedServerResource();

	/**
	 * FIXME Remove or rework once actual persistence is in place.
	 */
	@After
	public void removeAccounts() {
		AccountService.existingAccounts.clear();
		GuestAuthService.existingLogins.clear();
	}

	/**
	 * Ensures that {@link AccountService#validateAuth()} returns
	 * {@link Status#UNAUTHORIZED} as expected when called without
	 * authentication.
	 */
	@Test
	public void validateGuestLoginDenied() {
		WebClient client = WebClient.create(server.getServerBaseAddress());

		// Validate the login.
		Response validateResponse = client
				.replacePath(null)
				.accept(MediaType.TEXT_XML)
				.path(AccountService.SERVICE_PATH
						+ AccountService.SERVICE_PATH_VALIDATE).get();

		// Verify the results
		Assert.assertEquals(Status.UNAUTHORIZED.getStatusCode(),
				validateResponse.getStatus());
	}

	/**
	 * Ensures that {@link AccountService#validateAuth()} works as expected when
	 * used with an {@link Account} created via
	 * {@link GuestAuthService#loginAsGuest()}.
	 */
	@Test
	public void createAndValidateGuestLogin() {
		WebClient client = WebClient.create(server.getServerBaseAddress());
		WebClientHelper.enableSessionMaintenance(client, true);

		// Login as guest.
		Response loginResponse = client.accept(MediaType.TEXT_XML)
				.path(GuestAuthService.SERVICE_PATH).post(null);
		Assert.assertEquals(Status.OK.getStatusCode(),
				loginResponse.getStatus());

		// Validate the login.
		Response validateResponse = client
				.replacePath(null)
				.accept(MediaType.TEXT_XML)
				.path(AccountService.SERVICE_PATH
						+ AccountService.SERVICE_PATH_VALIDATE).get();
		Assert.assertEquals(Status.OK.getStatusCode(),
				validateResponse.getStatus());

		// Verify the results
		Account account = (Account) validateResponse.readEntity(Account.class);
		Assert.assertNotNull(account);
	}
}
