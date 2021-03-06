package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.byon;

import iTests.framework.utils.AssertUtils;
import iTests.framework.utils.LogUtils;
import iTests.framework.utils.SSHUtils;
import org.cloudifysource.quality.iTests.test.AbstractTestSupport;
import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.services.byon.ByonCloudService;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AddRemoveTemplatesMultipleManagementMachinesTest extends AbstractByonAddRemoveTemplatesTest{

	@BeforeClass(alwaysRun = true)
	protected void bootstrap() throws Exception {		
		super.bootstrap();
	}

	
	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = true)
	public void addRemoveTemplates() throws Exception {
				
		TemplatesBatchHandler handler = new TemplatesBatchHandler();
		TemplateDetails template = handler.addTemplate();
		String templateName = template.getTemplateName();
		addTemplates(handler);
		assertExpectedListTemplates();
				
		String templateRemotePath = getTemplateRemoteDirFullPath(templateName) + template.getTemplateFile().getName();	

		verifyTemplateExistence(mngMachinesIP[0], template, templateRemotePath, true);
		verifyTemplateExistence(mngMachinesIP[1], template, templateRemotePath, true);
							
		removeTemplate(handler, templateName, false, null);
		assertExpectedListTemplates();
		
		verifyTemplateExistence(mngMachinesIP[0], template, templateRemotePath, false);
		verifyTemplateExistence(mngMachinesIP[1], template, templateRemotePath, false);
			
	}
	
	@Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 2, enabled = true)
	public void failedAddRemoveTemplates() throws Exception {
		
		TemplatesBatchHandler handler = new TemplatesBatchHandler();
		TemplateDetails template = handler.addTemplate();
		String templateName = template.getTemplateName();
		addTemplates(handler);
		
		String templateRemotePath = getTemplateRemoteDirFullPath(templateName) + template.getTemplateFile().getName();
		
		LogUtils.log(SSHUtils.runCommand(mngMachinesIP[1], AbstractTestSupport.OPERATION_TIMEOUT, "rm -f " + templateRemotePath, USER, PASSWORD));
		
		String output = removeTemplate(handler, templateName, true, null);
		
		AssertUtils.assertTrue("successfully removed template from " + mngMachinesIP[1], output.contains("Failed to remove"));
		AssertUtils.assertTrue("successfully removed template from " + mngMachinesIP[1], output.contains(templateName));
		AssertUtils.assertTrue("successfully removed template from " + mngMachinesIP[1], output.contains(mngMachinesIP[1]));
		
		handler.addExpectedToFailTemplate(template);
		output = addTemplates(handler);
				
		int failedIndex = output.indexOf("Failed to add the following templates:");
		AssertUtils.assertTrue("successfully added " + templateName + " to " + mngMachinesIP[1], failedIndex != -1);

		int successIndex = output.indexOf("Successfully added the following templates:");
		AssertUtils.assertTrue("failed to add " + templateName + " to " + mngMachinesIP[0], successIndex != -1);
		
		String failedOutput = output.substring(failedIndex, successIndex);
		String successOutput = output.substring(successIndex);
		
		AssertUtils.assertTrue("successfully added " + templateName + " to " + mngMachinesIP[1], failedOutput.contains(mngMachinesIP[1]));
		AssertUtils.assertTrue("successfully added " + templateName + " to " + mngMachinesIP[1], failedOutput.contains(templateName));

		AssertUtils.assertTrue("failed to add " + templateName + " to " + mngMachinesIP[0], successOutput.contains(mngMachinesIP[0]));
		AssertUtils.assertTrue("failed to add " + templateName + " to " + mngMachinesIP[0], successOutput.contains(templateName));
		
	}
	
	@AfterClass(alwaysRun = true)
	protected void teardown() throws Exception {
		super.teardown();
	}


	protected void startManagement(String machine1) throws Exception {
		
		for (int i = 0 ; i < 3 ; i++) {
			try {
				LogUtils.log(SSHUtils.runCommand(machine1, AbstractTestSupport.DEFAULT_TEST_TIMEOUT,  ByonCloudService.BYON_HOME_FOLDER + "/gigaspaces/tools/cli/cloudify.sh start-management", getService().getUser(), getService().getApiKey()));
				return;
			} catch (Throwable t) {
				LogUtils.log("Failed to start management on machine " + machine1 + ". Attempt number " + (i + 1));
			}
		}
		
		AssertUtils.assertFail("Failed to start management on machine " + machine1 + ".");
	}

	@Override
	public int getNumOfMngMachines() {
		return 2;
	}



}
