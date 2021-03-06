package org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.ec2.storage.staticstorage;

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

import java.util.concurrent.TimeoutException;

/**
 *
 * @see https://cloudifysource.atlassian.net/browse/CLOUDIFY-1670
 * Check that there are no leaking volumes after uninstall of a failed installation.
 *
 * We see that this test sometimes fails because the timeout from the cli end happens during the volume creation.
 * immediatly after that an uninstall command is executed, if this uninstall happens before the volume was created and written into the space,
 * the uninstall will not be aware of it and therfore will not delete it. causing a leak.
 *
 * Created with IntelliJ IDEA.
 * User: elip
 * Date: 4/9/13
 * Time: 7:45 PM
 * To change this template use File | Settings | File Templates.
 */
public class FailedToInstallTest extends AbstractStorageAllocationTest {

    @BeforeClass(alwaysRun = true)
    protected void bootstrap() throws Exception {
        super.bootstrap();
    }

    @Test(timeOut = AbstractTestSupport.DEFAULT_TEST_TIMEOUT * 4, enabled = true, groups = AbstractTestSupport.SUSPECTED)
    public void testLinux() throws Exception {
        storageAllocationTester.testFaultyInstallLinux();
    }

    @AfterMethod
    public void cleanup() {
        RecipeInstaller installer = storageAllocationTester.getInstaller();
        installer.expectToFail(false);
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
    protected String getCloudName() {
        return "ec2";
    }

    @Override
    protected boolean isReusableCloud() {
        return false;
    }
}
