package org.cloudifysource.quality.iTests.framework.utils;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import iTests.framework.utils.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.cloudifysource.quality.iTests.test.cli.cloudify.CommandTestUtils;
import org.cloudifysource.quality.iTests.test.cli.cloudify.cloud.services.CloudService;

public class CloudBootstrapper extends Bootstrapper {

	private static final int DEFAULT_BOOTSTRAP_CLOUD_TIMEOUT = 30;

    private CloudService cloudService;

    private String provider;
	private boolean noWebServices = false;
	private String useExistingFilePath = "";
	private String cloudFolderPath = "";
	private boolean useExisting = false;
	private Map<String, Object> cloudOverrides;
	
	public CloudBootstrapper() {
		super(DEFAULT_BOOTSTRAP_CLOUD_TIMEOUT);
	}
	
	public CloudBootstrapper provider(String provider) {
		this.provider = provider;
		return this;
	}

    public void setCloudService(CloudService cloudService) {
        this.cloudService = cloudService;
    }

    public CloudService getCloudService() {

        return cloudService;
    }


    public String getProvider() {
		return provider;
	}
	
	public CloudBootstrapper noWebServices(boolean noWebServices) {
		this.noWebServices = noWebServices;
		return this;
	}
	
	public boolean isNoWebServices() {
		return noWebServices;
	}
	
	public CloudBootstrapper cloudOverrides(Map<String, Object> overrides) {
		this.cloudOverrides = overrides;
		return this;
	}

	@Override
	public String getCustomOptions() throws IOException{
		
		StringBuilder builder = new StringBuilder();
		if (noWebServices) {
			builder.append("-no-web-services ");
		}
		if (cloudOverrides != null && !cloudOverrides.isEmpty()) {
			File cloudOverridesFile = IOUtils.createTempOverridesFile(cloudOverrides);
			builder
				.append("-cloud-overrides").append(" ")
				.append(cloudOverridesFile.getAbsolutePath().replace("\\", "/"));
		}

        if(StringUtils.isNotBlank(useExistingFilePath)){
            builder.append("-use-existing-from-file " + useExistingFilePath);
        }

        if(useExisting){
            builder.append("-use-existing");
        }

		return builder.toString();
	}

	public String addTemplate(String templatePath, boolean isExpectedToFail) throws Exception {
		lastActionOutput = CommandTestUtils.runCommand(connectCommand() + ";add-templates " + templatePath, true, isExpectedToFail);
		return lastActionOutput;
	}

	public String getTemplate(String templateName, boolean isExpectedToFail) throws Exception {
		lastActionOutput = CommandTestUtils.runCommand(connectCommand() + ";get-template " + templateName, true, isExpectedToFail);
		return lastActionOutput;
	}

	public String listTemplates(boolean isExpectedToFail) throws Exception {
		lastActionOutput = CommandTestUtils.runCommand(connectCommand() + ";list-templates", true, isExpectedToFail);
		return lastActionOutput;
	}

	public String removeTemplate(String templateName, boolean isExpectedToFail) throws Exception {
		lastActionOutput = CommandTestUtils.runCommand(connectCommand() + ";remove-template " + templateName, true, isExpectedToFail);
		return lastActionOutput;
	}

    public String getUseExistingFilePath() {
        return useExistingFilePath;
    }

    public CloudBootstrapper useExistingFilePath(String useExistingFilePath) {
        this.useExistingFilePath = useExistingFilePath;
        return this;
    }

    public boolean isUseExisting() {
        return useExisting;
    }

    public CloudBootstrapper useExisting(boolean useExisting) {
        this.useExisting = useExisting;
        return this;
    }

    public String getCloudFolderPath() {
        return cloudFolderPath;
    }

    public void setCloudFolderPath(String cloudFolderPath) {
        this.cloudFolderPath = cloudFolderPath;
    }
}
