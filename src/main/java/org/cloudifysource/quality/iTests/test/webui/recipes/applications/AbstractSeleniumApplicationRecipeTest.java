package org.cloudifysource.quality.iTests.test.webui.recipes.applications;

import java.io.IOException;

import org.testng.annotations.BeforeMethod;

import org.cloudifysource.quality.iTests.test.cli.cloudify.CommandTestUtils;
import org.cloudifysource.quality.iTests.test.webui.AbstractWebUILocalCloudTest;
import iTests.framework.utils.LogUtils;
import iTests.framework.utils.ScriptUtils;

public class AbstractSeleniumApplicationRecipeTest extends AbstractWebUILocalCloudTest {
	
	protected String pathToApplication;
	protected boolean wait = true;
	protected String applicationName;
	
	public void setCurrentApplication(String application) {
		this.applicationName = application;
		String gigaDir = ScriptUtils.getBuildPath();	
		this.pathToApplication = gigaDir + "/recipes/apps/" + application;
	}
	
	public void setWait(boolean wait) {
		this.wait = wait;
	}
	
	@BeforeMethod(alwaysRun = true)
	public void install() throws IOException, InterruptedException {	
		LogUtils.log("Installing application " + applicationName);
		installApplication(pathToApplication, wait);
		LogUtils.log("retrieving webui url");
	}
	
	public void installApplication(String pathToApplication, boolean wait) throws InterruptedException, IOException {
		String command = "connect localhost:8100;install-application --verbose -timeout 25 " + pathToApplication;
		if (wait) {
			LogUtils.log("Waiting for install-application to finish...");
			CommandTestUtils.runCommandAndWait(command);
		}
		else {
			LogUtils.log("Not waiting for application to finish, assuming it will succeed");
			CommandTestUtils.runCommand(command);
		}
	}
	
	public void uninstallApplication(String applicationName, boolean wait) throws IOException, InterruptedException {	
		String command = "connect localhost:8100;uninstall-application --verbose -timeout 25 " + applicationName;
		if (wait) {
			LogUtils.log("Waiting for uninstall-application to finish...");
			CommandTestUtils.runCommandAndWait(command);
		}
		else {
			CommandTestUtils.runCommand(command);
		}
	}
}
