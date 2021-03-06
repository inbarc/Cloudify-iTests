package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.ec2.bigdata;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import iTests.framework.tools.SGTestHelper;
import org.cloudifysource.quality.iTests.test.AbstractTestSupport;
import org.cloudifysource.quality.iTests.test.cli.cloudify.CommandTestUtils;
import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.NewAbstractCloudTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class StockDemoApplicationTest extends NewAbstractCloudTest {

	private static final int APPLICATION_INSTALL_TIMEOUT_IN_MINUTES = 120;
	
	@BeforeClass(alwaysRun = true)
	protected void bootstrap() throws Exception {
		super.bootstrap();
	}
	
	@BeforeMethod
	public void prepareApplication() throws IOException {
		File stockDemoAppSG = new File(CommandTestUtils.getPath("src/main/resources/apps/USM/usm/applications/stockdemo"));
		File appsFolder = new File(SGTestHelper.getBuildDir() + "/recipes/apps/stockdemo");
		FileUtils.copyDirectory(stockDemoAppSG, appsFolder);
	}
	
	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 8, groups = "1", enabled = true)
	public void testStockDemo() throws IOException, InterruptedException {
		doSanityTest("stockdemo", "stockdemo", APPLICATION_INSTALL_TIMEOUT_IN_MINUTES);
	}
	
	@Override
	protected void beforeBootstrap() throws Exception {
		String suiteName = System.getProperty("iTests.suiteName");
		if(suiteName != null && "CLOUDIFY_XAP".equalsIgnoreCase(suiteName)){
			/* copy premium license to cloudify-overrides in order to run xap pu's */
			String overridesFolder = getService().getPathToCloudFolder() + "/upload/cloudify-overrides";
			File cloudifyPremiumLicenseFile = new File(SGTestHelper.getSGTestRootDir() + "/config/gslicense.xml");
			FileUtils.copyFileToDirectory(cloudifyPremiumLicenseFile, new File(overridesFolder));
		}
	}
	
	@AfterClass(alwaysRun = true)
	protected void teardown() throws Exception {
		super.teardown();
	}
	
	@Override
	public void beforeTeardown() throws IOException, InterruptedException {
		super.uninstallApplicationIfFound("stockdemo");
	}
	

	@Override
	protected String getCloudName() {
		return "ec2";
	}

	@Override
	protected boolean isReusableCloud() {
		return false;
	}
}
