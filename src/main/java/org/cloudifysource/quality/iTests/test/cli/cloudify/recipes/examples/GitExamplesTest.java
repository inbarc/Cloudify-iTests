package org.cloudifysource.quality.iTests.test.cli.cloudify.recipes.examples;

import iTests.framework.tools.SGTestHelper;

import java.io.File;

import org.apache.commons.io.FileUtils;
import org.cloudifysource.quality.iTests.framework.utils.JGitUtils;
import iTests.framework.utils.LogUtils;
import iTests.framework.utils.ScriptUtils;
import org.cloudifysource.quality.iTests.test.cli.cloudify.AbstractLocalCloudTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class GitExamplesTest extends AbstractLocalCloudTest {

	private String localGitAppsPath;
	private String localGitRepoPath;
    private static String BRANCH_NAME = SGTestHelper.getBranchName();

	@BeforeClass(alwaysRun = true)
	protected void bootstrap() throws Exception {
	    localGitRepoPath = ScriptUtils.getBuildPath() + "/git-temp-repo";
        String remotePath = "https://github.com/CloudifySource/cloudify-recipes.git";
        JGitUtils.clone(localGitRepoPath, remotePath, BRANCH_NAME);
	}
	
	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 4, enabled = false)
	public void testComputers() throws Exception {
		super.doTest(localGitAppsPath, "computers", "computers");
	}
	
	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 4, enabled = false)
	public void testBabies() throws Exception {
		super.doTest(localGitAppsPath, "drupal-babies", "babies");
	}
	
	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 4, enabled = false)
	public void testBiginsights() throws Exception {
		super.doTest(localGitAppsPath, "hadoop-biginsights", "biginsights");
	}
	
	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 4, enabled = false)
	public void testLamp() throws Exception {
		super.doTest(localGitAppsPath, "lamp", "lamp");
	}
	
	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 4, enabled = false)
	public void testMasterSlave() throws Exception {
		super.doTest(localGitAppsPath, "masterslave", "masterslave");
	}

	@AfterClass(alwaysRun = true)
	protected void teardown() throws Exception {
		LogUtils.log("removing exported git folder");
		FileUtils.deleteDirectory(new File(localGitRepoPath));
	}
}
