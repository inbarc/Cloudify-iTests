package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.hp.storage.staticstorage;

import java.util.concurrent.TimeoutException;

import org.cloudifysource.esc.driver.provisioning.storage.StorageProvisioningException;
import org.cloudifysource.quality.iTests.framework.utils.ApplicationInstaller;
import org.cloudifysource.quality.iTests.framework.utils.RecipeInstaller;
import org.cloudifysource.quality.iTests.framework.utils.ServiceInstaller;
import org.cloudifysource.quality.iTests.test.AbstractTestSupport;
import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.AbstractStorageAllocationTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Author: nirb
 * Date: 21/02/13
 */
public class HpStorageDeleteOnExitTest extends AbstractStorageAllocationTest {

    @Override
    protected String getCloudName() {
        return "hp";
    }

    @BeforeClass(alwaysRun = true)
    protected void bootstrap() throws Exception {
        super.bootstrap();
    }

    @Override
    protected void customizeCloud() throws Exception {
        super.customizeCloud();
        getService().getAdditionalPropsToReplace().put("deleteOnExit true", "deleteOnExit false");
    }

    @Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 4, enabled = true)
    public void testLinux() throws Exception {
        storageAllocationTester.testDeleteOnExitFalseLinux();
    }


    @AfterMethod
    public void cleanup() {
        RecipeInstaller installer = storageAllocationTester.getInstaller();
        if (installer instanceof ServiceInstaller) {
            ((ServiceInstaller) installer).uninstallIfFound();
        } else {
            ((ApplicationInstaller) installer).uninstallIfFound();
        }
    }

    @AfterClass
    public void scanForLeakes() throws TimeoutException, StorageProvisioningException {
        super.scanForLeakedVolumesCreatedViaTemplate("SMALL_BLOCK");
    }

    @AfterClass(alwaysRun = true)
    protected void teardown() throws Exception {
        super.teardown();
    }


    @Override
    protected boolean isReusableCloud() {
        return false;
    }
}
