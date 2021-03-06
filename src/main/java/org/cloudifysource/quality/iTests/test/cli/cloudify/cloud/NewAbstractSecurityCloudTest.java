package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud;

import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.cloudifysource.quality.iTests.framework.utils.ApplicationInstaller;
import org.cloudifysource.quality.iTests.framework.utils.CloudBootstrapper;
import org.cloudifysource.quality.iTests.framework.utils.ServiceInstaller;
import org.cloudifysource.quality.iTests.test.cli.cloudify.CommandTestUtils;
import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.services.CloudService;
import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.services.CloudServiceManager;
import org.cloudifysource.quality.iTests.test.cli.cloudify.security.SecurityConstants;

public abstract class NewAbstractSecurityCloudTest extends NewAbstractCloudTest {
	
	protected static final int TIMEOUT_IN_MINUTES = 60;
	protected static final String APP_NAME = "simple";
	protected static final String APP_PATH = CommandTestUtils.getPath("/src/main/resources/apps/USM/usm/applications/" + APP_NAME);
	protected static final String TEARDOWN_ACCESS_DENIED_MESSAGE = "Permission not granted, access is denied.";
	protected static final String TEARDOWN_SUCCESSFULL_MESSAGE = "Cloud terminated successfully.";
	
	@Override
	protected void bootstrap() throws Exception {
		
		CloudService service = CloudServiceManager.getInstance().getCloudService(getCloudName());
		CloudBootstrapper securedBootstrapper = new CloudBootstrapper();
		securedBootstrapper.secured(true).securityFilePath(SecurityConstants.BUILD_SECURITY_FILE_PATH)
			.user(SecurityConstants.USER_PWD_ALL_ROLES).password(SecurityConstants.USER_PWD_ALL_ROLES);
		securedBootstrapper.keystoreFilePath(SecurityConstants.DEFAULT_KEYSTORE_FILE_PATH).keystorePassword(SecurityConstants.DEFAULT_KEYSTORE_PASSWORD);
		service.setBootstrapper(securedBootstrapper);
		
		super.bootstrap(service);
	}
	
	@Override
	protected void teardown() throws Exception {
		CloudBootstrapper securedBootstrapper = (CloudBootstrapper) getService().getBootstrapper().user(SecurityConstants.USER_PWD_ALL_ROLES).password(SecurityConstants.USER_PWD_ALL_ROLES);
		teardown(securedBootstrapper);
	}
	
	protected void teardown(CloudBootstrapper securedBootstrapper) throws Exception {
		getService().setBootstrapper(securedBootstrapper);
		super.teardown();
	}
	
	protected String installApplicationAndWait(String applicationPath, String applicationName, int timeout, final String cloudifyUsername,
			final String cloudifyPassword, boolean isExpectedToFail, final String authGroups) throws IOException, InterruptedException {

		ApplicationInstaller applicationInstaller = new ApplicationInstaller(getRestUrl(), applicationName);
		applicationInstaller.recipePath(applicationPath);
		applicationInstaller.waitForFinish(true);
		applicationInstaller.cloudifyUsername(cloudifyUsername);
		applicationInstaller.cloudifyPassword(cloudifyPassword);
		applicationInstaller.expectToFail(isExpectedToFail);
		if (StringUtils.isNotBlank(authGroups)) {
			applicationInstaller.authGroups(authGroups);
		}

		return applicationInstaller.install();
	}

	protected String uninstallApplicationAndWait(String applicationPath, String applicationName, int timeout, final String cloudifyUsername,
			final String cloudifyPassword, boolean isExpectedToFail, final String authGroups) throws IOException, InterruptedException {

		ApplicationInstaller applicationInstaller = new ApplicationInstaller(getRestUrl(), applicationName);
		applicationInstaller.recipePath(applicationPath);
		applicationInstaller.waitForFinish(true);
		applicationInstaller.cloudifyUsername(cloudifyUsername);
		applicationInstaller.cloudifyPassword(cloudifyPassword);
		applicationInstaller.expectToFail(isExpectedToFail);
		if (StringUtils.isNotBlank(authGroups)) {
			applicationInstaller.authGroups(authGroups);
		}

		return applicationInstaller.uninstall();
	}
	
	protected void uninstallApplicationIfFound(String applicationName, final String cloudifyUsername, final String cloudifyPassword) throws IOException, InterruptedException {
		ApplicationInstaller applicationInstaller = new ApplicationInstaller(getRestUrl(), applicationName);
		applicationInstaller.waitForFinish(true);
		applicationInstaller.cloudifyUsername(cloudifyUsername);
		applicationInstaller.cloudifyPassword(cloudifyPassword);
		applicationInstaller.uninstallIfFound();
	}
	
	protected void uninstallServiceIfFound(String serviceName, final String cloudifyUsername, final String cloudifyPassword) throws IOException, InterruptedException {
		ServiceInstaller serviceInstaller = new ServiceInstaller(getRestUrl(), serviceName);
		serviceInstaller.waitForFinish(true);
		serviceInstaller.cloudifyUsername(cloudifyUsername);
		serviceInstaller.cloudifyPassword(cloudifyPassword);
		serviceInstaller.uninstallIfFound();		
	}
	
	protected String installServiceAndWait(String servicePath, String serviceName, int timeout, final String cloudifyUsername,
			final String cloudifyPassword, boolean isExpectedToFail, final String authGroups) throws IOException, InterruptedException {

		ServiceInstaller serviceInstaller = new ServiceInstaller(getRestUrl(), serviceName);
		serviceInstaller.recipePath(servicePath);
		serviceInstaller.waitForFinish(true);
		serviceInstaller.cloudifyUsername(cloudifyUsername);
		serviceInstaller.cloudifyPassword(cloudifyPassword);
		serviceInstaller.expectToFail(isExpectedToFail);
		if (StringUtils.isNotBlank(authGroups)) {
			serviceInstaller.authGroups(authGroups);
		}

		return serviceInstaller.install();
	}
	
	protected String uninstallServiceAndWait(String servicePath, String serviceName, int timeout, final String cloudifyUsername,
			final String cloudifyPassword, boolean isExpectedToFail, final String authGroups) throws IOException, InterruptedException {

		ServiceInstaller serviceInstaller = new ServiceInstaller(getRestUrl(), serviceName);
		serviceInstaller.recipePath(servicePath);
		serviceInstaller.waitForFinish(true);
		serviceInstaller.cloudifyUsername(cloudifyUsername);
		serviceInstaller.cloudifyPassword(cloudifyPassword);
		serviceInstaller.expectToFail(isExpectedToFail);
		if (StringUtils.isNotBlank(authGroups)) {
			serviceInstaller.authGroups(authGroups);
		}

		return serviceInstaller.uninstall();
	}

	protected String login(String user, String password, boolean failCommand) throws IOException, InterruptedException{
		getService().getBootstrapper().setRestUrl(getRestUrl());
		return getService().getBootstrapper().user(user).password(password).login(failCommand);
	}
	
	protected String connect(String user, String password, boolean isExpectedToFail) throws IOException, InterruptedException{
		getService().getBootstrapper().setRestUrl(getRestUrl());
		return getService().getBootstrapper().user(user).password(password).connect(isExpectedToFail);
	}

	protected String listApplications(String user, String password, boolean expectedFail) throws IOException, InterruptedException{
		getService().getBootstrapper().setRestUrl(getRestUrl());
		return getService().getBootstrapper().user(user).password(password).listApplications(expectedFail);
	}
	
	protected String listServices(String user, String password, String applicationName, boolean expectedFail) throws IOException, InterruptedException{
		getService().getBootstrapper().setRestUrl(getRestUrl());
		return getService().getBootstrapper().user(user).password(password).listServices(applicationName, expectedFail);
	}
	
	protected String listInstances(String user, String password, String applicationName, String serviceName, boolean expectedFail) throws IOException, InterruptedException{
		getService().getBootstrapper().setRestUrl(getRestUrl());
		return getService().getBootstrapper().user(user).password(password).listInstances(applicationName, serviceName, expectedFail);
	}
	
	public String addTemplate(String user, String password, String templatePath) throws Exception{
		cloudService.getBootstrapper().setRestUrl(getRestUrl());
		cloudService.getBootstrapper().user(user).password(password);
		return cloudService.getBootstrapper().addTemplate(templatePath, false);
	}
}

