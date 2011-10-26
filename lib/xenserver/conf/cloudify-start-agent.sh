#! /bin/bash

#############################################################################
# Parameters:
# $1 - The IP of this server (Useful if multiple NICs exist)
#	$2 - not used
#	$3 - LOOKUPLOCATORS (recommended)
#	$4 - LOOKUPGROUPS (mandatory if LOOKUPLOCATORS is not specified)
# $5 - GSA ZONE (mandatory)
#############################################################################

# UPDATE SETENV SCRIPT...
/opt/setup-env.sh $1 $3 $4

cd /opt/gigaspaces/tools/cli/
nohup ./cloudify.sh start-agent -auto-shutdown -zone $5 > /dev/null &

exit 0