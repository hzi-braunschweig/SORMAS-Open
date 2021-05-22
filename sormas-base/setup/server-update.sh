#!/bin/bash

#*******************************************************************************
# SORMAS® - Surveillance Outbreak Response Management & Analysis System
# Copyright © 2016-2019 Helmholtz-Zentrum f�r Infektionsforschung GmbH (HZI)
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <https://www.gnu.org/licenses/>.
#*******************************************************************************

# If you want your server to show a maintenance page while the server is updated, use the following commands:
# 	a2dissite your.sormas.server.url.conf
# 	apache2ctl graceful
# ... where your.sormas.server.url is the URL of your SORMAS server, e.g. sormas.org.
# Use the following commands to re-enable the normal operating mode after the update has been completed:
#	a2ensite your.sormas.server.url.conf
#	apache2ctl graceful

# CAUTION: Configure these variables in server-update.conf when your server setup differs from the official server setup instructions!
# Paths need to be specified WITHOUT a trailing slash.

DEPLOY_PATH=/root/deploy/sormas/$(date +%F)
GLASSFISH_PATH=/opt/payara5/glassfish
DOMAIN_PATH=/opt/domains
DOMAIN_NAME="sormas"
DOWNLOADS_PATH=/var/www/sormas/downloads
DB_BACKUP_PATH=/root/deploy/sormas/backup
DATABASE_NAME="sormas_db"
DATABASE_AUDIT_NAME="sormas_audit_db"
LOG_FILE_PATH=$DOMAIN_PATH/$DOMAIN_NAME/logs
UPDATE_LOG_PATH=$DOMAIN_PATH/$DOMAIN_NAME/update-logs
UPDATE_LOG_FILE_NAME=server_update_$(date +"%Y-%m-%d_%H-%M-%S").txt
CUSTOM_DIR=/opt/sormas/custom
ABOUT_FILES_DIR=$CUSTOM_DIR/aboutfiles
DOCUMENTS_DIR=/opt/sormas/documents
USER_NAME=payara
CONTINUOUS_DELIVERY=no

# Override default configuration by system dependent .conf file if present (read "dirname" to be able to call the script remote via SSH)
CONF_FILE=$(dirname "$0")/server-update.conf
if test -f "$CONF_FILE"; then
	echo "Read-in system dependent configuration ..."
	# shellcheck disable=SC1090
	source "$CONF_FILE" 
fi

echo "# SORMAS SERVER UPDATE"
echo "# Welcome to the SORMAS server update routine. This script will automatically update your SORMAS server."
echo "# If anything goes wrong, please consult the server update guide or get in touch with the developers."

# Check whether all paths exist on the server

echo "Checking whether all directories are set up properly..."

if [ ! -d "$DEPLOY_PATH" ]; then
	echo "Deploy files directory not found. Make sure it's located in the following path: $DEPLOY_PATH"
	exit 1
fi

if [ ! -d "$DEPLOY_PATH/serverlibs" ]; then
	echo "Missing directory serverlibs in domain directory $DEPLOY_PATH. Re-download the deploy files and make sure the directory exists."
	exit 1
fi

if [ ! -d "$DEPLOY_PATH/apps" ]; then
	echo "Missing directory apps in domain directory $DEPLOY_PATH. Re-download the deploy files and make sure the directory exists."
	exit 1
fi

if [ ! -d "$DEPLOY_PATH/android" ]; then
	echo "Missing directory android in domain directory $DEPLOY_PATH. Re-download the deploy files and make sure the directory exists."
	exit 1
fi

if [ ! -d $GLASSFISH_PATH ]; then
	echo "Glassfish directory not found. Make sure it's located in the following path: $GLASSFISH_PATH"
	exit 1
fi

if [ ! -d $DOMAIN_PATH ]; then
	echo "SORMAS domain directory not found. Make sure it's located in the following path: $DOMAIN_PATH/$DOMAIN_NAME"
	exit 1
fi

if [ ! -d $DOWNLOADS_PATH ]; then
	echo "Download directory not found. Make sure it's located in the following path: $DOWNLOADS_PATH"
	exit 1
fi

if [ ! -d $CUSTOM_DIR ]; then
	mkdir -p ${CUSTOM_DIR}
	setfacl -m u:${USER_NAME}:rwx ${CUSTOM_DIR}
	setfacl -m u:postgres:rwx ${CUSTOM_DIR}
fi

if [ ! -d $ABOUT_FILES_DIR ]; then
	mkdir -p ${ABOUT_FILES_DIR}
	setfacl -m u:${USER_NAME}:rwx ${ABOUT_FILES_DIR}
	setfacl -m u:postgres:rwx ${ABOUT_FILES_DIR}
fi

if [ ! -d $DOCUMENTS_DIR ]; then
  mkdir -p ${DOCUMENTS_DIR}
  setfacl -m u:${USER_NAME}:rwx ${DOCUMENTS_DIR}
  setfacl -m u:postgres:rwx ${DOCUMENTS_DIR}
fi

# Create a file to log errors and messages not produced by this script during the update process
if [ ! -d $UPDATE_LOG_PATH ]; then
	if mkdir $UPDATE_LOG_PATH 2>/dev/null; then
		echo "Could not create directory $UPDATE_LOG_PATH. Please create it manually."
		exit 1
	fi
fi


if ! touch "$UPDATE_LOG_PATH/$UPDATE_LOG_FILE_NAME" 2>/dev/null; then
	echo "Could not create server update log file. Maybe you need to execute the update script with superuser rights?"
	exit 1
fi

exec 6>&2 2>"$UPDATE_LOG_PATH/$UPDATE_LOG_FILE_NAME"

# Create a database backup directory if it does not exist
if [ ! -d $DB_BACKUP_PATH ]; then
	if ! mkdir $DB_BACKUP_PATH 2>/dev/null; then
		echo "Could not create database backup directory at $DB_BACKUP_PATH. Please create it manually."
		exit 1
	fi
fi

echo "All directories found."

# Start server update process

echo "Starting SORMAS update..."
echo "Removing old deploy files..."

rm $DOMAIN_PATH/$DOMAIN_NAME/autodeploy/*.war
rm $DOMAIN_PATH/$DOMAIN_NAME/autodeploy/*.ear

echo "Stopping server..."

if ! service payara-sormas stop > "$UPDATE_LOG_PATH/$UPDATE_LOG_FILE_NAME"; then
	# Try to manually stop the domain when the service is not set up
	if ! ($GLASSFISH_PATH/bin/asadmin stop-domain --domaindir $DOMAIN_PATH $DOMAIN_NAME > "$UPDATE_LOG_PATH/$UPDATE_LOG_FILE_NAME"); then
		echo "Service payara-sormas was not found and trying to manually stop the server failed. Please check the domain name or set up the payara-sormas service."
		exit 1
	fi
fi

# Wait for undeployment and shutdown of the domain
sleep 10s

rm $DOMAIN_PATH/$DOMAIN_NAME/lib/*.jar

echo "Copying server libs..."

cp "$DEPLOY_PATH/serverlibs/*" "$DOMAIN_PATH/$DOMAIN_NAME/lib/"

if [ ! -f $CUSTOM_DIR/loginsidebar.html ]; then
	cp loginsidebar.html ${CUSTOM_DIR}
fi

if [ ! -f $CUSTOM_DIR/loginsidebar-header.html ]; then
  cp loginsidebar-header.html ${CUSTOM_DIR}
fi

if [ ! -f $CUSTOM_DIR/logindetails.html ]; then
	cp logindetails.html ${CUSTOM_DIR}
fi

# You can use the following command to use a backup to restore the data in case the automatic database update process during deployment fails:
# pg_restore --clean -U postgres -Fc -d sormas_db sormas_db_....dump

if uname -a | grep "^Linux*"; then
	echo "Creating database backups..."

	if sudo -u postgres pg_dump -Fc -b $DATABASE_NAME | tee "$DB_BACKUP_PATH/${DATABASE_NAME}_$(date +"%Y-%m-%d_%H-%M-%S").dump"; then
		echo "Main database backup successfully completed..."
	else
		echo "Main database backup failed and will be skipped..."
	fi

	if sudo -u postgres pg_dump -Fc -b $DATABASE_AUDIT_NAME | tee "$DB_BACKUP_PATH/${DATABASE_AUDIT_NAME}_$(date +"%Y-%m-%d_%H-%M-%S").dump"; then
		echo "Audit database backup successfully completed..."
	else
		echo "Audit database backup failed and will be skipped..."
	fi
else
	echo "Database backups will be skipped because it seems the server is not running on a Linux system."
fi

echo "Starting server..."


if ! service payara-sormas start > "$UPDATE_LOG_PATH/$UPDATE_LOG_FILE_NAME"; then
	# Try to manually start the domain when the service is not set up
	if ! "$GLASSFISH_PATH/bin/asadmin" start-domain --domaindir $DOMAIN_PATH $DOMAIN_NAME > "$UPDATE_LOG_PATH/$UPDATE_LOG_FILE_NAME"; then
		echo "Service payara-sormas was not found and trying to manually start the server failed. Please check the domain name or set up the payara-sormas service."
		exit 1
	fi
fi

echo "Server successfully started..."
echo "Copying apk files..."

cp "$DEPLOY_PATH/android/release/*.apk" $DOWNLOADS_PATH

exec 2>&6

if [ "$CONTINUOUS_DELIVERY" == "yes" ]; then
	# Wait some seconds for the fully started domain
	sleep 10s
	echo "Deploying sormas artifacts"
else
	read -pr "SORMAS update successfully completed. The server will now be deployed and logs will be displayed to notify you if anything goes wrong. Press [Enter] to continue."
fi

cp "$DEPLOY_PATH/apps/*.ear" "$DOMAIN_PATH/$DOMAIN_NAME/autodeploy/"
cp "$DEPLOY_PATH/apps/*.war" "$DOMAIN_PATH/$DOMAIN_NAME/autodeploy/"

if [ "$CONTINUOUS_DELIVERY" != "yes" ]; then
	tail -f "$LOG_FILE_PATH/server.log"
fi
