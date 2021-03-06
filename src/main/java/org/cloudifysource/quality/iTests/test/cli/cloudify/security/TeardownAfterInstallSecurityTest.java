package org.cloudifysource.quality.iTests.test.cli.cloudify.security;

import java.io.IOException;

import iTests.framework.utils.AssertUtils;
import org.cloudifysource.quality.iTests.framework.utils.LocalCloudBootstrapper;
import org.cloudifysource.quality.iTests.test.AbstractTestSupport;
import org.cloudifysource.quality.iTests.test.cli.cloudify.AbstractSecuredLocalCloudTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class TeardownAfterInstallSecurityTest extends AbstractSecuredLocalCloudTest {
	
	private LocalCloudBootstrapper bootstrapper;
	
	@BeforeClass
	public void bootstrap() throws Exception {
		bootstrapper = new LocalCloudBootstrapper();
		bootstrapper.secured(true).securityFilePath(SecurityConstants.BUILD_SECURITY_FILE_PATH);
		bootstrapper.keystoreFilePath(SecurityConstants.DEFAULT_KEYSTORE_FILE_PATH).keystorePassword(SecurityConstants.DEFAULT_KEYSTORE_PASSWORD);
		super.bootstrap(bootstrapper);	
	}
	
	@AfterClass(alwaysRun = true)
	public void teardown() throws IOException, InterruptedException {
		super.teardown();
	}
	
	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT, enabled = true)
	public void teardownWithInstallTest() throws Exception{
		
		installApplicationAndWait(SIMPLE_APP_PATH, SIMPLE_APP_NAME, TIMEOUT_IN_MINUTES, SecurityConstants.USER_PWD_APP_MANAGER, SecurityConstants.USER_PWD_APP_MANAGER, false, null);
		
		bootstrapper.user(SecurityConstants.USER_PWD_APP_MANAGER).password(SecurityConstants.USER_PWD_APP_MANAGER).teardownExpectedToFail(true);
		String output = super.teardown(bootstrapper);
		
		AssertUtils.assertTrue(SecurityConstants.APP_MANAGER_DESCRIPTIN + " uninstalled the installed application during teardown", !output.contains("uninstalled"));
		
	}

}
