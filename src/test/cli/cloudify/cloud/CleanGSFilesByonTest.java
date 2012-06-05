/*******************************************************************************
* Copyright (c) 2012 GigaSpaces Technologies Ltd. All rights reserved
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
******************************************************************************/
package test.cli.cloudify.cloud;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.FileSystemOptions;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder;
import org.cloudifysource.dsl.cloud.FileTransferModes;
import org.cloudifysource.dsl.internal.CloudifyConstants;
import org.openspaces.admin.AdminFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import test.cli.cloudify.CloudTestUtils;
import test.cli.cloudify.cloud.services.byon.ByonCloudService;
import framework.utils.LogUtils;

public class CleanGSFilesByonTest extends AbstractCloudTest {
	
	private static final String DEFAULT_USER = "tgrid";
	private static final String DEFAULT_PASSWORD = "tgrid";
	private static final String TEST_MACHINES_LIST = "ipList";
	private static final String ITEMS_NOT_DELETED_MSG = "The GS files and folders were not deleted on teardown.";
	// timeout for SFTP connection
	private static final Integer SFTP_DISCONNECT_DETECTION_TIMEOUT_MILLIS = Integer.valueOf(10 * 1000);
	
	private ByonCloudService service;
	private Set<String> hosts = null;

	@BeforeMethod
	public void bootstrap() throws IOException, InterruptedException {	
		service = new ByonCloudService();
		service.setMachinePrefix(this.getClass().getName() + CloudTestUtils.SGTEST_MACHINE_PREFIX);
		service.bootstrapCloud();
	}
	
	@AfterMethod(alwaysRun = true)
	public void teardown() throws IOException {
		try {
			service.teardownCloud();
			//TODO : verify the files were deleted
		}
		catch (Throwable e) {
			LogUtils.log("caught an exception while tearing down byon", e);
			sendTeardownCloudFailedMail("byon", e);
		}
		LogUtils.log("restoring original bootstrap-management file");
	}
	
	/**
	 * Checks if the folders and files were removed as we wanted.
	 * NOTE: In order to simplify the test we're using the default credentials to access our lab machines.
	 * @throws Exception
	 */
	@Test(timeOut = DEFAULT_TEST_TIMEOUT * 2, groups = "1", enabled = true)
	public void installTest() throws Exception {
		List<String> itemsToClean = new ArrayList<String>();
		itemsToClean.add("/tmp/gs-files/gigaspaces/work");
		itemsToClean.add("/tmp/gs-files/gigaspaces.zip");
		
		LogUtils.log("creating admin");
		AdminFactory factory = new AdminFactory();
		//TODO what should this be?
		//factory.addGroup(LOOKUPGROUP);
		String machinesList = System.getProperty(TEST_MACHINES_LIST);
		assertTrue("ipList system property is empty", StringUtils.isNotBlank(machinesList));
		StringTokenizer tokenizer = new StringTokenizer(machinesList, ",");
		while (tokenizer.hasMoreTokens()) {
			factory.addLocator(tokenizer.nextToken().trim() + ":" + CloudifyConstants.DEFAULT_LUS_PORT);
		}
		
		admin = factory.createAdmin();
		hosts = admin.getMachines().getHostsByAddress().keySet();
		service.teardownCloud();
		for (String address : hosts) {
			//using the default credentials to access our lab machines
			assertTrue(ITEMS_NOT_DELETED_MSG, !fileSystemObjectsExist(address, DEFAULT_USER, DEFAULT_PASSWORD, null /*key file*/,
					itemsToClean, FileTransferModes.SCP, false));
		}
	}
	
	/**
	 * Checks whether the files or folders exist on a remote host.
	 * The returned value depends on the last parameter - "allMustExist".
	 * If allMustExist is True the returned value is True only if all listed objects exist.
	 * If allMustExist is False, the returned value is True if at least one object exists.
	 * 
	 * @param host The host to connect to
	 * @param username The name of the user that deletes the file/folder
	 * @param password The password of the above user
	 * @param keyFile The key file, if used
	 * @param fileSystemObjects The files or folders to delete
	 * @param fileTransferMode SCP for secure copy in Linux, or CIFS for windows file sharing
	 * @param allMustExist If set to True the function will return True only if all listed objects exist.
	 * 			If set to False, the function will return True if at least one object exists.
	 * @return depends on allMustExist
	 * @throws IOException Indicates the deletion failed
	 */
	public static boolean fileSystemObjectsExist(final String host, final String username, final String password,
			final String keyFile, final List<String> fileSystemObjects, final FileTransferModes fileTransferMode,
			final boolean allMustExist)
			throws IOException {
		
		boolean objectsExist;
		if (allMustExist) {
			objectsExist = true;
		} else {
			objectsExist = false;
		}

		if (!fileTransferMode.equals(FileTransferModes.SCP)) {
			//TODO Support get with CIFS as well
			throw new IOException("File resolving is currently not supported for this file transfer protocol ("
					+ fileTransferMode + ")");
		}

		final FileSystemOptions opts = new FileSystemOptions();
		SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(opts, "no");
		SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, false);
		if (keyFile != null && !keyFile.isEmpty()) {
			final File temp = new File(keyFile);
			if (!temp.isFile()) {
				throw new FileNotFoundException("Could not find key file: " + temp);
			}
			SftpFileSystemConfigBuilder.getInstance().setIdentities(opts, new File[] { temp });
		}

		SftpFileSystemConfigBuilder.getInstance().setTimeout(opts, SFTP_DISCONNECT_DETECTION_TIMEOUT_MILLIS);
		final FileSystemManager mng = VFS.getManager();

		String scpTargetBase, scpTarget;
		if (password != null && !password.isEmpty()) {
			scpTargetBase = "sftp://" + username + ':' + password + '@' + host;
		} else {
			scpTargetBase = "sftp://" + username + '@' + host;
		}
		
		FileObject remoteDir = null;
		try {
			for (final String fileSystemObject : fileSystemObjects) {
				scpTarget = scpTargetBase + fileSystemObject;
				remoteDir = mng.resolveFile(scpTarget, opts);
				if (remoteDir.exists()) {
					if (!allMustExist) {
						objectsExist = true;
						break;
					}
				} else {
					if (allMustExist) {
						objectsExist = false;
						break;
					}
				}
			}
		} finally {
			if (remoteDir != null) {
				mng.closeFileSystem(remoteDir.getFileSystem());
			}
		}
		
		return objectsExist;
	}

}