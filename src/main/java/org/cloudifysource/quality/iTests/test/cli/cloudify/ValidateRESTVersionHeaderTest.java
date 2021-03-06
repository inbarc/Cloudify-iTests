package org.cloudifysource.quality.iTests.test.cli.cloudify;

import java.io.IOException;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.cloudifysource.dsl.internal.CloudifyConstants;
import org.cloudifysource.quality.iTests.test.AbstractTestSupport;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import com.j_spaces.kernel.PlatformVersion;

/**
 * Tests that check REST's version validation, which is performed for each
 * request received from the client.
 *
 * @author yael
 *
 */
public class ValidateRESTVersionHeaderTest extends AbstractLocalCloudTest {

	private static String requestUri;
	private static final String WRONG_VERSION = "0.0.0";

	/**
	 * Updates the request's URI.
	 */
	@BeforeClass
	public void updateRequestUri() {
		requestUri = restUrl + "/service/testrest";
	}

	/************
	 * Verifies that localcloud runs on 127.0.0.1.
	 * @throws IOException .
	 */
	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
	public void nicAddressValidationTest() throws IOException {
		Assert.assertTrue(restUrl.contains("127.0.0.1"), "Local Cloud is not running on 127.0.0.1");

	}

	/**
	 * Sends a request without the cloudify-api-version header.
	 *
	 * @throws IOException
	 *
	 */
	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
	public void noVersionHeaderTest() throws IOException {
		final HttpResponse response = createAndSendRequest(null);
		AbstractTestSupport.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
	}

	/**
	 * Sends a request with the correct version.
	 *
	 * @throws IOException
	 *
	 */
	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
	public void correctVersionHeaderTest() throws IOException {
		final HttpResponse response = createAndSendRequest(PlatformVersion
				.getVersionNumber());
		AbstractTestSupport.assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
	}

	/**
	 * Sends a request with a wrong version.
	 *
	 * @throws IOException .

	 */
	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT, groups = "1", enabled = true)
	public void wrongVersionHeaderTest() throws IOException {
		final HttpResponse response = createAndSendRequest(WRONG_VERSION);
		AbstractTestSupport.assertEquals(HttpStatus.SC_INTERNAL_SERVER_ERROR, response
                .getStatusLine().getStatusCode());
	}

	/**
	 * Sends to the rest a request with cloudify-api-header equals to
	 * headerValue.
	 *
	 * @param headerValue
	 * @return the response
	 * @throws IOException
	 */
	private HttpResponse createAndSendRequest(final String headerValue)
			throws IOException {
		final DefaultHttpClient httpClient = new DefaultHttpClient();
		final HttpGet httpRequest = new HttpGet(requestUri);
		if (headerValue != null) {
			httpRequest.addHeader(CloudifyConstants.REST_API_VERSION_HEADER,
					headerValue);
		}
		return httpClient.execute(httpRequest);
	}
}
