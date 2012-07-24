package test.cli.cloudify.cloud.byon;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.cloudifysource.dsl.cloud.Cloud;
import org.cloudifysource.dsl.internal.ServiceReader;
import org.openspaces.admin.Admin;
import org.openspaces.admin.AdminFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import test.cli.cloudify.cloud.NewAbstractCloudTest;
import test.cli.cloudify.cloud.services.byon.ByonCloudService;
import framework.tools.SGTestHelper;
import framework.utils.AssertUtils;
import framework.utils.AssertUtils.RepetitiveConditionProvider;
import framework.utils.IOUtils;
import framework.utils.LogUtils;
import framework.utils.ScriptUtils;

/**
 * This test installs petclinic with 3 different templates on a byon cloud. It checks that each service was
 * assigned to the correct template, according to byon-cloud.groovy. After the installation completes, the
 * test checks the uninstall and teardown operations.
 * 
 * Note: this test uses 5 fixed machines -
 * 192.168.9.115,192.168.9.116,192.168.9.120,192.168.9.125,192.168.9.126.
 */
public class MultipleMachineTemplatesTest extends NewAbstractCloudTest {

	protected static String TEMPLATE_1_IPs = "192.168.9.115,192.168.9.116";
	protected static String TEMPLATE_2_IPs = "192.168.9.120,192.168.9.125";
	protected static String TEMPLATE_3_IPs = "192.168.9.126";

	private static final int MONGOD_DEFAULT_INSTANCES_NUM = 2;
	private static final int MONGOD_INSTANCES_NUM = 1;
	private static final long MY_OPERATION_TIMEOUT = 1 * 60 * 1000;

	private static final String DEFAULT_MACHINE_TEMPLATE = "SMALL_LINUX";
	private static final String TEST_UNIQUE_NAME = "MultipleMachineTemplatesTest";

	private String cloudName = "byon";
	private File mongodbDir = new File(ScriptUtils.getBuildPath(), "recipes/services/mongodb");
	private File tomcatDir = new File(ScriptUtils.getBuildPath(), "recipes/services");
	private File newCloudGroovyFile = new File(ScriptUtils.getBuildPath() + "/tools/cli/plugins/esc/byon/",
			"byon-cloud.new");

	protected Admin admin = null;

	@Override
	protected void customizeCloud() throws Exception {
		// NOA: note, this next section is a hack. The test needs a special copy of byon-groovy and edits it
		// before we create the test's folder (byon_MultipleMachineTemplatesTest), so we must copy files and
		// replace values here and eventually copy "byon-cloud.new" over
		// "byon_MultipleMachineTemplatesTest\byon-cloud.groovy".

		ByonCloudService byonService = (ByonCloudService) cloud;
		// use SGTest's special byon groovy (with templates) instead of the original one,
		// but don't override the original file - use byon-new-cloud.groovy instead
		File multiTemplatesGroovy = new File(SGTestHelper.getSGTestRootDir()
				+ "/apps/cloudify/cloud/byon/byon-cloud.groovy");
		copyFile(multiTemplatesGroovy, newCloudGroovyFile);

		// read the original file to get the download URL from it
		File originialCloudGroovy = new File(ScriptUtils.getBuildPath() + "/tools/cli/plugins/esc/byon/",
				"byon-cloud.groovy");
		Cloud origCloudConf = ServiceReader.readCloud(originialCloudGroovy);
		String cloudifyUrl = origCloudConf.getProvider().getCloudifyUrl();

		// replace the Cloudify URL and nodes' IPs in the new groovy file
		Map<String, String> replaceMap = new HashMap<String, String>();
		replaceMap.put("cloudify.zip", cloudifyUrl);
		replaceMap.put("1.1.1.1", TEMPLATE_1_IPs);
		replaceMap.put("2.2.2.2", TEMPLATE_2_IPs);
		replaceMap.put("3.3.3.3", TEMPLATE_3_IPs);
		replaceMap.put("localDirectory \"tools/cli/plugins/esc/byon/upload\"",
				"localDirectory \"tools/cli/plugins/esc/" + byonService.getServiceFolder() + "/upload\"");
		IOUtils.replaceTextInFile(newCloudGroovyFile.getAbsolutePath(), replaceMap);

		// replace the default bootstrap-management and cloud groovy with the customized versions
		File standardBootstrapManagement = new File(byonService.getPathToCloudFolder() + "/upload",
				"bootstrap-management.sh");
		File bootstrapManagementCustomized = new File(SGTestHelper.getSGTestRootDir()
				+ "/apps/cloudify/cloud/byon/bootstrap-management-" + byonService.getServiceFolder() + ".sh");
		File fileToBeReplaced = new File(byonService.getPathToCloudFolder(), "byon-cloud.groovy");
		Map<File, File> filesToReplace = new HashMap<File, File>();
		filesToReplace.put(fileToBeReplaced, newCloudGroovyFile);
		filesToReplace.put(standardBootstrapManagement, bootstrapManagementCustomized);
		byonService.addFilesToReplace(filesToReplace);

		// TODO : this is dangerous, need to fix
		backupAndReplaceMachineTemplateInService("mongos", "TEMPLATE_1");
		backupAndReplaceMachineTemplateInService("mongod", "TEMPLATE_2");
		backupAndReplaceMachineTemplateInService("mongoConfig", "TEMPLATE_1");
		backupAndReplaceMachineTemplateInService("tomcat", "TEMPLATE_2");
		IOUtils.replaceTextInFile(mongodbDir.getAbsolutePath() + "/mongod/mongod-service.groovy", "numInstances "
				+ MONGOD_DEFAULT_INSTANCES_NUM, "numInstances " + MONGOD_INSTANCES_NUM);

	}

	@Override
	protected void afterBootstrap() throws Exception {
		// create an admin object with a unique group
		LogUtils.log("creating admin");
		AdminFactory factory = new AdminFactory();
		factory.addGroup(TEST_UNIQUE_NAME);
		admin = factory.createAdmin();
	}

	/**
	 * check that each service was assigned to the correct template, according to byon-cloud.groovy.
	 * 
	 * @throws Exception
	 */
	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = true, priority = 1)
	public void testPetclinic() throws Exception {

		LogUtils.log("installing application petclinic on " + cloudName);
		installApplicationAndWait(ScriptUtils.getBuildPath() + "/recipes/apps/petclinic", "petclinic");

		String template1IPsArray[] = TEMPLATE_1_IPs.split(",");
		String template2IPsArray[] = TEMPLATE_2_IPs.split(",");
		String template3IPsArray[] = TEMPLATE_3_IPs.split(",");

		// TODO : edit this, so if it fails it won't be on NPE!
		String hostAddressToCompare = admin.getProcessingUnits().getProcessingUnit("petclinic.mongod").getInstances()[0]
				.getMachine().getHostAddress();
		Assert.assertTrue(Arrays.asList(template2IPsArray).contains(hostAddressToCompare));

		hostAddressToCompare = admin.getProcessingUnits().getProcessingUnit("petclinic.mongos").getInstances()[0]
				.getMachine().getHostAddress();
		Assert.assertTrue(Arrays.asList(template1IPsArray).contains(hostAddressToCompare));

		hostAddressToCompare = admin.getProcessingUnits().getProcessingUnit("petclinic.mongoConfig").getInstances()[0]
				.getMachine().getHostAddress();
		Assert.assertTrue(Arrays.asList(template1IPsArray).contains(hostAddressToCompare));

		hostAddressToCompare = admin.getProcessingUnits().getProcessingUnit("petclinic.tomcat").getInstances()[0]
				.getMachine().getHostAddress();
		Assert.assertTrue(Arrays.asList(template2IPsArray).contains(hostAddressToCompare));

		hostAddressToCompare = admin.getProcessingUnits().getProcessingUnit("webui").getInstances()[0].getMachine()
				.getHostAddress();
		Assert.assertTrue(Arrays.asList(template3IPsArray).contains(hostAddressToCompare));

		hostAddressToCompare = admin.getProcessingUnits().getProcessingUnit("rest").getInstances()[0].getMachine()
				.getHostAddress();
		Assert.assertTrue(Arrays.asList(template3IPsArray).contains(hostAddressToCompare));

	}

	/**
	 * tests the uninstall operation - uninstalls and checks that each application service is down.
	 */
	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, enabled = false, priority = 2)
	public void testPetclinicUninstall() {

		try {
			uninstallApplicationAndWait("petclinic");
		} catch (Throwable e) {
			LogUtils.log("caught an exception while uninstalling petclinic", e);
		}

		AssertUtils.repetitiveAssertTrue("petclinic.mongod is not down", new RepetitiveConditionProvider() {
			public boolean getCondition() {
				try {
					return (admin.getProcessingUnits().getProcessingUnit("petclinic.mongod") == null);
				} catch (Exception e) {
					return false;
				}
			}
		}, MY_OPERATION_TIMEOUT);
		AssertUtils.repetitiveAssertTrue("petclinic.mongos is not down", new RepetitiveConditionProvider() {
			public boolean getCondition() {
				try {
					return (admin.getProcessingUnits().getProcessingUnit("petclinic.mongos") == null);
				} catch (Exception e) {
					return false;
				}
			}
		}, MY_OPERATION_TIMEOUT);
		AssertUtils.repetitiveAssertTrue("petclinic.mongoConfig is not down", new RepetitiveConditionProvider() {
			public boolean getCondition() {
				try {
					return (admin.getProcessingUnits().getProcessingUnit("petclinic.mongoConfig") == null);
				} catch (Exception e) {
					return false;
				}
			}
		}, MY_OPERATION_TIMEOUT);
		AssertUtils.repetitiveAssertTrue("petclinic.tomcat is not down", new RepetitiveConditionProvider() {
			public boolean getCondition() {
				try {
					return (admin.getProcessingUnits().getProcessingUnit("petclinic.tomcat") == null);
				} catch (Exception e) {
					return false;
				}
			}
		}, MY_OPERATION_TIMEOUT);
		AssertUtils.repetitiveAssertTrue("webui is down", new RepetitiveConditionProvider() {
			public boolean getCondition() {
				try {
					return (admin.getProcessingUnits().getProcessingUnit("webui") != null);
				} catch (Exception e) {
					return false;
				}
			}
		}, MY_OPERATION_TIMEOUT);
		AssertUtils.repetitiveAssertTrue("rest is down", new RepetitiveConditionProvider() {
			public boolean getCondition() {
				try {
					return (admin.getProcessingUnits().getProcessingUnit("rest") != null);
				} catch (Exception e) {
					return false;
				}
			}
		}, MY_OPERATION_TIMEOUT);
		AssertUtils.repetitiveAssertTrue("cloudifyManagementSpace is down", new RepetitiveConditionProvider() {
			public boolean getCondition() {
				try {
					return (admin.getProcessingUnits().getProcessingUnit("cloudifyManagementSpace") != null);
				} catch (Exception e) {
					return false;
				}
			}
		}, MY_OPERATION_TIMEOUT);

	}

	@Override
	protected void beforeTeardown() throws Exception {
		if (admin != null) {
			admin.close();
			admin = null;
		}
	}
	
	@Override
	protected void afterTeardown() throws Exception {
		
		try {
			AssertUtils.repetitiveAssertTrue("rest is not down", new RepetitiveConditionProvider() {
				public boolean getCondition() {
					try {
						return (admin.getProcessingUnits().getProcessingUnit("rest") == null);
					} catch (Exception e) {
						return false;
					}
				}
			}, MY_OPERATION_TIMEOUT);
			AssertUtils.repetitiveAssertTrue("webui is not down", new RepetitiveConditionProvider() {
				public boolean getCondition() {
					try {
						return (admin.getProcessingUnits().getProcessingUnit("webui") == null);
					} catch (Exception e) {
						return false;
					}
				}
			}, MY_OPERATION_TIMEOUT);
			AssertUtils.repetitiveAssertTrue("cloudifyManagementSpace is down", new RepetitiveConditionProvider() {
				public boolean getCondition() {
					try {
						return (admin.getProcessingUnits().getProcessingUnit("cloudifyManagementSpace") == null);
					} catch (Exception e) {
						return false;
					}
				}
			}, MY_OPERATION_TIMEOUT);
		} finally {
			// clean
			restoreOriginalServiceFile("mongos");
			restoreOriginalServiceFile("mongod");
			restoreOriginalServiceFile("tomcat");
			restoreOriginalServiceFile("mongoConfig");

			if (newCloudGroovyFile.exists()) {
				newCloudGroovyFile.delete();
			}
		}
	}

	@Override
	protected String getCloudName() {
		return "byon";
	}

	@Override
	protected boolean isReusableCloud() {
		return false;
	}

	private void backupAndReplaceMachineTemplateInService(String serviceName, String newTemplate) throws IOException {

		File parentDir = mongodbDir;
		if (serviceName.equalsIgnoreCase("tomcat"))
			parentDir = tomcatDir;

		File originalService = new File(parentDir, serviceName + "/" + serviceName + "-service.groovy");
		FileUtils.copyFile(originalService, new File(originalService.getAbsolutePath() + ".backup"));
		IOUtils.replaceTextInFile(originalService.getAbsolutePath(), DEFAULT_MACHINE_TEMPLATE, newTemplate);
	}

	private void copyFile(File originalFile, File targetFile) throws IOException {
		if (targetFile.exists()) {
			targetFile.delete();
		}

		// copy replacement file with the original file's name
		FileUtils.copyFile(originalFile, targetFile);
	}

	private void restoreOriginalServiceFile(String serviceName) throws IOException {

		File parentDir = mongodbDir;
		if (serviceName.equalsIgnoreCase("tomcat"))
			parentDir = tomcatDir;

		File originalService = new File(parentDir, serviceName + "/" + serviceName + "-service.groovy");
		originalService.delete();
		FileUtils.moveFile(new File(originalService + ".backup"), originalService);
	}

}