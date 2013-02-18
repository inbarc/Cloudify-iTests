/***************
 * Cloud configuration file for the Bring-Your-Own-Node (BYON) cloud.
 * See org.cloudifysource.dsl.cloud.Cloud for more details.
 *
 * @author noak
 *
 */

cloud {
	// Mandatory. The name of the cloud, as it will appear in the Cloudify UI.
	name = "byon"

	/********
	 * General configuration information about the cloud driver implementation.
	 */
	configuration {
		// The cloud-driver implementation class.
		className "org.cloudifysource.esc.driver.provisioning.byon.ByonProvisioningDriver"
		// Optional. 	The template name for the management machines. Defaults to the first template in the templates section below.
		managementMachineTemplate "SMALL_LINUX"
		// Optional. Indicates whether internal cluster communications should use the machine private IP. Defaults to true.
		connectToPrivateIp true
		//Indicates whether communications with the management servers should use the machine private IP.
		bootstrapManagementOnPublicIp false
		components {
			usm {
				//RMI port range
				portRange "7010-7110"
				minMemory "64m"
				maxMemory "64m"
			}
			agent {
				//RMI port
				port 7002
				minMemory "64m"
				maxMemory "64m"

			}
			deployer {
				//webster port
				websterPort 6666
				//RMI port
				port 7000
				minMemory "64m"
				maxMemory "64m"
			}
			discovery {
				//Discovery port
				discoveryPort 4175
				//RMI port
				port 7001
				minMemory "64m"
				maxMemory "64m"
			}

			orchestrator {
				//RMI port
				port 7003
				minMemory "64m"
				maxMemory "64m"
			}

			//The RMI port for rest and webui will be determined using the GSC closure def or by default
			rest {
				//Rest port
				port 8102
				minMemory "128m"
				maxMemory "64m"
			}
			webui {
				//Webui port
				port 8101
				minMemory "128m"
				maxMemory "64m"
			}

		}
	}

	/*************
	 * Provider specific information.
	 */
	provider {
		// Mandatory. The name of the provider.
		provider "byon"


		// Optional. The HTTP/S URL where cloudify can be downloaded from by newly started machines. Defaults to downloading the
		// cloudify version matching that of the client from the cloudify CDN.
		// Change this if your compute nodes do not have access to an internet connection, or if you prefer to use a
		// different HTTP server instead.
		// IMPORTANT: the default linux bootstrap script appends '.tar.gz' to the url whereas the default windows script appends '.zip'.
		// Therefore, if setting a custom URL, make sure to leave out the suffix.
		cloudifyUrl "http://pc-lab25:8087/publish/adaml/gigaspaces-cloudify-2.5.0-m6-b3986-72"


		// Mandatory. The prefix for new machines started for servies.
		machineNamePrefix "cloudify_agent_"
		// Optional. Defaults to true. Specifies whether cloudify should try to deploy services on the management machine.
		// Do not change this unless you know EXACTLY what you are doing.

		managementOnlyFiles ([])

		// Optional. Logging level for the intenal cloud provider logger. Defaults to INFO.
		sshLoggingLevel "INFO"
		// Mandatory. Name of the new machine/s started as cloudify management machines.
		managementGroup "cloudify_manager"
		// Mandatory. Number of management machines to start on bootstrap-cloud. In production, should be 2. Can be 1 for dev.
		numberOfManagementMachines 1

		reservedMemoryCapacityPerMachineInMB 1024
		reservedMemoryCapacityPerManagementMachineInMB 1280
	}

	/*************
	 * Cloud authentication information
	 */
	user {

	}

	cloudCompute {
		/***********
		 * Cloud machine templates available with this cloud. 
		 */
		templates ([
					// Mandatory. Template Name.
					SMALL_LINUX : computeTemplate{
						// Mandatory. Amount of RAM available to machine.
						machineMemoryMB 5850
						// Mandatory. Files from the local directory will be copied to this directory on the remote machine.
						remoteDirectory "/tmp/byon/gs-files"
						// Optional. template-generic credentials. Can be overridden by specific credentials on each node, in the nodesList section.
						username username
						password password

						// Mandatory. All files from this LOCAL directory will be copied to the remote machine directory.
						localDirectory "upload"

						// Mandatory for BYON.
						custom ([
									// Mandatory for BYON. The nodesList custom property lists the nodes that compose this cloud-like environment.
									// For each node required:
									// An alias for this node. can be static or use a template with an dynamic-index.
									// The server's private (local) IP. can be a single IP, a list, a range or CIDR.
									//Optional - user and password for the node. can be taken from general cloud configuration.
									"nodesList" : ([
										([
											"id" : "pc-lab121, pc-lab122",
											"host-list" : "192.168.9.141, 192.168.9.142"
										])
									])
								])

						// enable sudo.
						privileged false

					}
				])
	}

	/*****************
	 * Optional. Custom properties used to extend existing drivers or create new ones. 
	 */
	// Optional. Sets whether to delete the remoteDirectory created by the cloud driver, when shutting down.
	custom ([
				"cleanGsFilesOnShutdown": "true",
				"clearRemoteDirectoryOnStart":"true",
				"itemsToClean": ([
					"/tmp/gs-files/gigaspaces.tar.gz"
				])
			])

}