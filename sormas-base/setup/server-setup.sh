#!/bin/bash
#*******************************************************************************
# SORMAS® - Surveillance Outbreak Response Management & Analysis System
# Copyright © 2016-2020 Helmholtz-Zentrum f�r Infektionsforschung GmbH (HZI)
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

# >>>>> FUNCTIONS >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>
SCRIPT_DIR="$(dirname "$0")"
source "$SCRIPT_DIR/utilitylib.sh"

# Save the variables with given names with their respective values into the
# choice file, such that they can be reloaded on consequtive script runs.
# Usage: remember_choice {<VAR>}
remember_choice() {
  local SED_FILTER=""
  local VARS_APPEND=""
  for VAR in "$@"; do
    local VARNAME="$VAR"
    local VARVALUE="${!VAR}"

    SED_FILTER="$SED_FILTER{/${VARNAME}=/d};"
    VARS_APPEND="${VARS_APPEND}if [[ -z \"\${${VARNAME}}\" ]]; then ${VARNAME}='${VARVALUE}'; fi"$'\n'
  done

  local CHOICES_TMP="${CHOICES_FILE}.tmp"
  cat "$CHOICES_FILE" | sed -e "${SED_FILTER}" > "$CHOICES_TMP"
  echo "$VARS_APPEND" >> "$CHOICES_TMP"
  mv "$CHOICES_TMP" "$CHOICES_FILE"
}

# Make paths stored in specified variables absolute.
abspath() {
  for PATHVAR in "$@"; do
    printf -v "$PATHVAR" '%s' "$(realpath -m "${!PATHVAR}")"
  done
}

# Render given text in bold.
bold() {
  echo -e '\e[1m'"$1"'\e[22m'
}

# Render given text as underlined.
underlined () {
  echo -e '\e[4m'"$1"'\e[24m'
}

# <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<

CHOICES_FILE="./server-setup.conf"

rm -f setup.log
exec > >(tee -ia setup.log)
exec 2> >(tee -ia setup.log)

echo -e $(bold "# SORMAS SERVER SETUP")
echo "# Welcome to the SORMAS server setup routine. This script will guide you through the setup of your server."
echo "# If anything goes wrong, please consult the server setup guide or get in touch with the developers."

# Ask the user whether he or she wants to re-use configuration values of previous executions
if [[ -f "$CHOICES_FILE" ]]; then
  echo -e $(bold "--- Continue using existing configuration choices in ${CHOICES_FILE}?")
  select LOAD_CHOICES in "Yes" "No"; do
		case $LOAD_CHOICES in
			Yes )
			  source "$CHOICES_FILE"
			  break;;
			No ) break;;
		esac
	done
else
  echo "This script file will save your configuration choices to ${CHOICES_FILE}. In case of an error, " \
       "you can reload the values for choices you already made at script startup."
  read -p "$(bold "--- Press [Enter] to continue or [Ctrl+C] to cancel.")"
  touch "$CHOICES_FILE"
fi


# >>>>> CONFIGURATION PHASE >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

# DEVELOPMENT ENVIRONMENT OR PRODUCTION/TEST SERVER?
if [[ -z ${DEV_SYSTEM} ]]; then
  echo $(bold "--- Are you setting up a local system or a server?")
  select LS in "Local" "Server"; do
      case $LS in
          Local ) DEV_SYSTEM=true; break;;
          Server ) DEV_SYSTEM=false; break;;
      esac
  done
fi
remember_choice DEV_SYSTEM

if [[ -z ${DEMO_SYSTEM} ]]; then
	echo $(bold "--- Is the server meant to be a demo/test or a production-kind server?")
	select LS in "Demo/Test" "Production"; do
		case $LS in
			Demo/Test ) DEMO_SYSTEM=true; break;;
			Production ) DEMO_SYSTEM=false; break;;
		esac
	done
else
	DEMO_SYSTEM=false
fi
remember_choice DEMO_SYSTEM


if [[ $(expr substr "$(uname -a)" 1 5) = "Linux" ]]; then
	LINUX=true
else
	LINUX=false
fi

# DIRECTORIES
if [[ ${LINUX} = true ]]; then
	ROOT_PREFIX=
	# make sure to update payara-sormas script when changing the user name
  if [[ ${DEV_SYSTEM} = true ]]; then
    PAYARA_USER_DEFAULT="$(whoami)"
  else
    PAYARA_USER_DEFAULT="payara"
  fi
	prompted_defaulted PAYARA_USER="$PAYARA_USER_DEFAULT" \
	  "*nix user to be used by Payara server"

	if [[ -n "$(which sudo)" ]]; then
	  if [[ -z ${USE_SUDO} ]]; then
	    echo -e "$(bold "Some commands may need elevated rights. Use sudo where necessary?")"
	    select CHOICE in "Yes" "No"; do
	      case $CHOICE in
	        Yes ) USE_SUDO=true; break;;
	        No ) USE_SUDO=false; break;;
	      esac
      done
      remember_choice USE_SUDO
    fi
  else
    USE_SUDO=false
  fi

  if [[ ${USE_SUDO} = true ]]; then
    ELEVATED=(sudo bash -c)
  else
    ELEVATED=(su -c)
  fi

else 
	ROOT_PREFIX=/c
fi

prompted_defaulted INSTALL_DIR="${ROOT_PREFIX}/opt" \
  "Installation base directory for Payara and SORMAS files"
abspath INSTALL_DIR

prompted_defaulted TEMP_DIR="${INSTALL_DIR}/sormas/temp" \
  "Directory for SORMAS temporary files (required during operations)"
prompted_defaulted GENERATED_DIR="${INSTALL_DIR}/sormas/generated" \
  "Directory for SORMAS generated files (required during operations)"
prompted_defaulted CUSTOM_DIR="${INSTALL_DIR}/sormas/custom" \
  "Directory for customizable SORMAS files"
prompted_defaulted PAYARA_HOME="${INSTALL_DIR}/payara" \
  "Installation directory for Payara Application Server"
prompted_defaulted DOMAINS_HOME="${INSTALL_DIR}/domains" \
  "Home directory for Payara domains"

abspath TEMP_DIR
abspath GENERATED_DIR
abspath CUSTOM_DIR
abspath PAYARA_HOME
abspath DOMAINS_HOME

prompted_defaulted PAYARA_VERSION=5.2020.2 \
  "Payara version to use"
prompted_defaulted DOMAIN_NAME=sormas \
  "Name for the Payara domain created for SORMAS"
prompted_defaulted PAYARA_PORT_BASE=6000 \
  "Base port for the SORMAS Payara domain"
PORT_ADMIN=$(expr $PAYARA_PORT_BASE + 48)
defaulted DOMAIN_XMX=4096m
DOMAIN_DIR="${DOMAINS_HOME}/${DOMAIN_NAME}"


# DB
prompted_defaulted DB_HOST=localhost \
  "PostgreSQL: Database Host"
prompted_defaulted DB_PORT=5432 \
  "PostgreSQL: Database Port"
prompted_defaulted DB_NAME=sormas_db \
  "Name of SORMAS database"
prompted_defaulted DB_NAME_AUDIT=sormas_audit_db \
  "Name of SORMAS database for auditing"
prompted_defaulted DB_USER=sormas_user \
  "Database user to be used by SORMAS"
defaulted DB_JDBC_MAXPOOLSIZE=128

# ------ Config END ------

echo "$(bold "--- Please confirm that all values are set properly:")"
echo "$(underlined "System Specs:")"
if [[ ${DEV_SYSTEM} = true ]]; then
	echo " ├ System type: Local Development System"
else
	echo " ├ System type: Server"
fi
if [[ ${LINUX} = true ]]; then
	echo " ├ OS: Linux"
else
	echo " ├ OS: Windows"
fi
if [[ -n "${AS_JAVA_NATIVE}" ]]; then
  echo " └ Java JDK: ${AS_JAVA_NATIVE}"
else
  echo " └ Java JDK: To be configured"
fi
echo "$(underlined "Payara config:")"
echo " ├ Payara Version: ${PAYARA_VERSION}"
echo " ├ *nix user: ${PAYARA_USER}"
echo " ├ SORMAS domain: ${DOMAIN_NAME}"
echo " └ Base port: ${PAYARA_PORT_BASE}"
echo "$(underlined "Directory config:")"
echo " ├ Temp directory: ${TEMP_DIR}"
echo " ├ Directory for generated files: ${GENERATED_DIR}"
echo " ├ Directory for custom files: ${CUSTOM_DIR}"
echo " ├ Payara home: ${PAYARA_HOME}"
echo " └ Domain directory: ${DOMAIN_DIR}"
echo "$(underlined "Database config:")"
echo " ├ Server: ${DB_HOST}:${DB_PORT}"
echo " ├ Database name: ${DB_NAME}, ${DB_NAME_AUDIT} for auditing"
echo " └ Database user: ${DB_USER}"
echo "---"
read -p "$(bold "Press [Enter] to continue or [Ctrl+C] to cancel and adjust the values...")"

remember_choice \
  PAYARA_VERSION \
  PAYARA_USER \
  INSTALL_DIR \
  TEMP_DIR \
  GENERATED_DIR \
  CUSTOM_DIR \
  PAYARA_HOME \
  DOMAINS_HOME \
  DOMAIN_NAME \
  PAYARA_PORT_BASE \
  PORT_ADMIN \
  DB_HOST \
  DB_PORT \
  DB_NAME \
  DB_NAME_AUDIT \
  DB_USER

if [[ -d "${DOMAIN_DIR}" ]]; then
	echo "The directory/domain $DOMAIN_DIR already exists. Please remove it and restart the script."
	exit 1
fi


# >>>>> INSTALLATION PHASE >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

echo "Starting server setup..."


# PREPARATION: Create needed directories and set user rights
mkdir -p "${PAYARA_HOME}"
mkdir -p "${DOMAINS_HOME}"
mkdir -p "${TEMP_DIR}"
mkdir -p "${GENERATED_DIR}"
mkdir -p "${CUSTOM_DIR}"

if [[ ${LINUX} = true ]]; then
  if [[ -z "$(sed -e "/^${PAYARA_USER}:/!d" "/etc/passwd")" ]]; then
	  "${ELEVATED[@]}" "\"adduser ${PAYARA_USER} --no-create-home\""
  fi
	setfacl -m u:${PAYARA_USER}:rwx "${DOMAINS_HOME}"
	setfacl -m u:${PAYARA_USER}:rwx "${TEMP_DIR}"
	setfacl -m u:${PAYARA_USER}:rwx "${GENERATED_DIR}"
	setfacl -m u:${PAYARA_USER}:rwx "${CUSTOM_DIR}"

	setfacl -m u:postgres:rwx "${TEMP_DIR}"
	setfacl -m u:postgres:rwx "${GENERATED_DIR}"
	setfacl -m u:postgres:rwx "${CUSTOM_DIR}"
fi


# PAYARA: Download and unzip payara
if [[ -d "${PAYARA_HOME}/glassfish" ]]; then
	echo "Found Payara (${PAYARA_HOME})"
else
	PAYARA_ZIP_FILE_NAME="payara-${PAYARA_VERSION}.zip"
	PAYARA_ZIP_FILE="${PAYARA_HOME}/${PAYARA_ZIP_FILE_NAME}"
	if [[ -f "${PAYARA_ZIP_FILE}" ]]; then
		echo "Payara already downloaded: ${PAYARA_ZIP_FILE}"
	else
		echo "Downloading Payara 5..."
		PAYARA_DOWNLOAD_URL="https://search.maven.org/remotecontent?filepath=fish/payara/distributions/payara/${PAYARA_VERSION}/${PAYARA_ZIP_FILE_NAME}"
		if [[ ${LINUX} = true ]]; then
			wget -O "${PAYARA_ZIP_FILE}" "${PAYARA_DOWNLOAD_URL}"
		else
			curl -L -o "${PAYARA_ZIP_FILE}" "${PAYARA_DOWNLOAD_URL}"
		fi
	fi

	echo "Unzipping Payara..."
	unzip -q -o "${PAYARA_ZIP_FILE}" -d "${PAYARA_HOME}"
	mv "${PAYARA_HOME}/payara5"/* "${PAYARA_HOME}"
	rm -R "${PAYARA_HOME}/payara5"
	rm -R "${PAYARA_HOME}/glassfish/domains"
fi

ASENV_PATH_LINUX="${PAYARA_HOME}/glassfish/config/asenv.conf"
ASENV_PATH_WINDOWS="${PAYARA_HOME}/glassfish/config/asenv.bat"

if [[ ${LINUX} = true ]]; then
	ASENV_PATH="${ASENV_PATH_LINUX}"
else
	ASENV_PATH="${ASENV_PATH_WINDOWS}"
fi


# JAVA CONFIG: Identify JDK
if [[ -z "${AS_JAVA_NATIVE}" && -z "${PAYARA_ZIP_FILE}" && -f "${ASENV_PATH}" ]]; then
	#payara already installed
	echo "Trying to deduce Java home directory from Payara installation..."
	while read line; do
		if [[ "$line" =~ '^AS_JAVA.*' ]]; then
			AS_JAVA_NATIVE=$(echo "${line#*=}"| tr -d '"')
		fi
	done < "${ASENV_PATH}"
fi

if [[ -z "${AS_JAVA_NATIVE}" && -n "$JAVA_HOME" ]]; then
  echo "Found Java installation specified by \$JAVA_HOME..."
  AS_JAVA_NATIVE="$JAVA_HOME"
fi

if [[ -z "${AS_JAVA_NATIVE}" && ${LINUX} = true ]]; then
  echo "Deducing Java home directory from 'javac' command..."
  AS_JAVA_NATIVE="$(realpath -P "$(dirname "$(which javac)")/..")"
fi

JDK_VERSION_REQUIRED=11
while [[ -z "$JDK_HOME" ]]; do
  prompted_defaulted JDK_HOME="$AS_JAVA_NATIVE" \
    "Home directory of the JDK to use"

  if [[ ${LINUX} = true ]]; then
    AS_JAVA="$JDK_HOME"
  else
    AS_JAVA=$(printf "/$JDK_HOME" | sed 's/:\?\\/\//g')
  fi

  if [[ ! -x "$AS_JAVA/bin/javac" && ! -x "$AS_JAVA/bin/javac.exe" ]]; then
    echo "$JDK_HOME is no valid Java JDK home (no Java compiler executable found)"
    unset JDK_HOME
    continue
  fi
  JAVAC="${AS_JAVA}/bin/javac"

  # Check Java JDK
  CHOICES=("Choose other" "Abort")
  JAVA_VERSION=$("${JAVAC}" -version 2>&1 | sed 's/^.\+ //;s/^1\.//;s/[^0-9].*//')
  if [[ "${JAVA_VERSION}" -eq $JDK_VERSION_REQUIRED ]]; then
    echo "Found Java ${JAVA_VERSION} JDK."
    CHOICES=()
  elif [[ "${JAVA_VERSION}" -gt $JDK_VERSION_REQUIRED ]]; then
    echo "Found Java ${JAVA_VERSION} JDK - This version may be too new, SORMAS functionality cannot be guaranteed. Consider downgrading to Java $JDK_VERSION_REQUIRED SDK."
    CHOICES=("Continue anyway" "${CHOICES[@]}")
  else
    echo "ERROR: Found Java ${JAVA_VERSION} JDK - This version is too old."
  fi

  if [[ -n "$CHOICES" ]]; then
    echo "What do you want to do?"
    select CHOICE in "${CHOICES[@]}"; do
      case "$CHOICE" in
        "Continue anyway" ) break ;;
        "Choose other" ) unset JDK_HOME; break ;;
        "Abort" ) exit 1 ;;
      esac
    done
  fi
done
remember_choice JDK_HOME

if [[ -n "${PAYARA_ZIP_FILE}" ]] && [[ -n "${AS_JAVA}" ]]; then

	#set Java JDK for payara
	printf "AS_JAVA=\"${AS_JAVA}\"" >> ${ASENV_PATH_LINUX}
	if [[ ${LINUX} != true ]]; then
		printf "set AS_JAVA=${AS_JAVA_NATIVE}" >> ${ASENV_PATH_WINDOWS}
	fi
fi


# POSTGRESQL: Set up the database
echo "Starting database setup..."

while [[ -z "${DB_PASSWORD}" ]]; do
  read -p "$(bold "--- Enter a password for the new database user '${DB_USER}': ")" DB_PASSWORD
done
remember_choice DB_PASSWORD

if [[ -z "$INIT_DB" ]]; then
  echo "$(bold "--- Do you want to initialize your PostgreSQL database?")"
  select CHOICE in "Yes" "No"; do
    case $CHOICE in
      Yes ) INIT_DB=true; break;;
      No ) INIT_DB=false; break;;
    esac
  done
fi

if [[ $INIT_DB = true ]]; then
  cat > setup.sql <<-EOF
  CREATE USER $DB_USER WITH PASSWORD '$DB_PASSWORD' CREATEDB;
  CREATE DATABASE $DB_NAME WITH OWNER = '$DB_USER' ENCODING = 'UTF8';
  CREATE DATABASE $DB_NAME_AUDIT WITH OWNER = '$DB_USER' ENCODING = 'UTF8';
  \c $DB_NAME
  CREATE OR REPLACE PROCEDURAL LANGUAGE plpgsql;
  ALTER PROCEDURAL LANGUAGE plpgsql OWNER TO $DB_USER;
  CREATE EXTENSION temporal_tables;
  CREATE EXTENSION pg_trgm;
  GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO $DB_USER;
  \c $DB_NAME_AUDIT
  CREATE EXTENSION IF NOT EXISTS plpgsql WITH SCHEMA pg_catalog;
  COMMENT ON EXTENSION plpgsql IS 'PL/pgSQL procedural language';
  GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO $DB_USER;
  ALTER TABLE IF EXISTS schema_version OWNER TO $DB_USER;
EOF

  if [[ ${LINUX} = true ]]; then
    # no host is specified as by default the postgres user has only local access
    if [[ -z "${DB_TCP_CONNECT}" ]]; then
      if [[ "${DB_HOST}" = "127.0.0.1" || "${DB_HOST}" = "localhost" ]]; then
        echo "$(bold "--- Connect to database server using TCP instead of Unix Domain Socket?")"
        select CHOICE in "Yes" "No"; do
          case "$CHOICE" in
            Yes ) DB_TCP_CONNECT=true; break;;
            No ) DB_TCP_CONNECT=false; break;;
          esac
        done
      else
        DB_TCP_CONNECT=true
      fi
    fi

    if [[ $DB_TCP_CONNECT = true ]]; then
      while [[ -z "${DB_PG_PW}" ]]; do
        read -r -p "$(bold "--- Enter the password for the 'postgres' user of your database: ")" DB_PG_PW
      done
      env PGPASSWORD="$DB_PG_PW" psql -h "${DB_HOST}" -p "${DB_PORT}" -U postgres < setup.sql
    else
      "${ELEVATED[@]}" '"su postgres -c \"psql -p '"${DB_PORT}"' < setup.sql\""'
    fi
  else
    PSQL_DEFAULT="${PROGRAMFILES//\\/\/}/PostgreSQL/10/"
    echo "--- Enter the name install path of Postgres on your system (default: \"${PSQL_DEFAULT}\":"
    read -r PSQL_DIR
    if [[ -z "${PSQL_DIR}" ]]; then
      PSQL_DIR="${PSQL_DEFAULT}"
    fi
    PSQL="${PSQL_DIR}/bin/psql.exe"
    while [[ -z "${DB_PG_PW}" ]]; do
      read -r -p "$(bold "--- Enter the password for the 'postgres' user of your database: ")" DB_PG_PW
    done
    "${PSQL}" --no-password --file=setup.sql "postgresql://postgres:${DB_PG_PW}@${DB_HOST}:${DB_PORT}/postgres"
  fi

  rm setup.sql

  echo "---"
  bold "Database setup completed. Please check the output for any substantial errors ('already existing' errors are typically OK)."
  read -p "$(bold "Press [Enter] to continue or [Ctrl+C] to cancel.")"
fi # End of ${INIT_DB} = true


# PAYARA DOMAIN: Setting ASADMIN_CALL and creating domain
echo "Creating domain for Payara..."
"${PAYARA_HOME}/bin/asadmin" create-domain --domaindir "${DOMAINS_HOME}" --portbase "${PAYARA_PORT_BASE}" --nopassword --template ${PAYARA_HOME}/glassfish/common/templates/gf/production-domain.jar "${DOMAIN_NAME}"
ASADMIN=("${PAYARA_HOME}/bin/asadmin" --port ${PORT_ADMIN})

if [[ ${LINUX} = true ]]; then
	chown -R "${PAYARA_USER}:${PAYARA_USER}" "${PAYARA_HOME}"
fi

"${PAYARA_HOME}/bin/asadmin" start-domain --domaindir "${DOMAINS_HOME}" ${DOMAIN_NAME}

if [[ 0 != $? ]]; then
	echo "ERROR: Payara domain failed to start."
	exit 2
fi

echo "$(bold "--- Enter the email sender address that is used for all mails generated by the system:")"
while [[ -z "${MAIL_FROM}" ]]; do
	read MAIL_FROM
done
remember_choice MAIL_FROM

echo "Configuring domain..."

# General domain settings
"${ASADMIN[@]}" delete-jvm-options -Xms2g
"${ASADMIN[@]}" delete-jvm-options -Xmx2g
"${ASADMIN[@]}" create-jvm-options -Xmx${DOMAIN_XMX}
"${ASADMIN[@]}" set configs.config.server-config.admin-service.das-config.autodeploy-enabled=true
"${ASADMIN[@]}" set configs.config.server-config.admin-service.das-config.dynamic-reload-enabled=true

# JDBC pool
"${ASADMIN[@]}" create-jdbc-connection-pool --restype javax.sql.ConnectionPoolDataSource --datasourceclassname org.postgresql.ds.PGConnectionPoolDataSource --isconnectvalidatereq true --validationmethod custom-validation --validationclassname org.glassfish.api.jdbc.validation.PostgresConnectionValidation --maxpoolsize ${DB_JDBC_MAXPOOLSIZE} --property "portNumber=${DB_PORT}:databaseName=${DB_NAME}:serverName=${DB_HOST}:user=${DB_USER}:password=${DB_PASSWORD}" ${DOMAIN_NAME}DataPool
"${ASADMIN[@]}" create-jdbc-resource --connectionpoolid ${DOMAIN_NAME}DataPool jdbc/sormasDataPool

# Pool for audit log
"${ASADMIN[@]}" create-jdbc-connection-pool --restype javax.sql.XADataSource --datasourceclassname org.postgresql.xa.PGXADataSource --isconnectvalidatereq true --validationmethod custom-validation --validationclassname org.glassfish.api.jdbc.validation.PostgresConnectionValidation --maxpoolsize ${DB_JDBC_MAXPOOLSIZE} --property "portNumber=${DB_PORT}:databaseName=${DB_NAME_AUDIT}:serverName=${DB_HOST}:user=${DB_USER}:password=${DB_PASSWORD}" ${DOMAIN_NAME}AuditlogPool
"${ASADMIN[@]}" create-jdbc-resource --connectionpoolid ${DOMAIN_NAME}AuditlogPool jdbc/AuditlogPool

"${ASADMIN[@]}" create-javamail-resource --mailhost localhost --mailuser user --fromaddress "${MAIL_FROM}" mail/MailSession
"${ASADMIN[@]}" create-custom-resource --restype java.util.Properties --factoryclass org.glassfish.resources.custom.factory.PropertiesFactory --property "org.glassfish.resources.custom.factory.PropertiesFactory.fileName=\${com.sun.aas.instanceRoot}/sormas.properties" sormas/Properties


# SORMAS: Automatically configure script files
redefine_vars \
  temp.path="$TEMP_DIR" \
  generated.path="$GENERATED_DIR" \
  custom.path="$CUSTOM_DIR" \
  email.sender.address="$MAIL_FROM" \
  devmode="$(if [[ "${DEV_SYSTEM}" = true ]]; then echo true; else echo false; fi)" \
  < sormas.properties \
  > "${DOMAIN_DIR}/sormas.properties"

redefine_vars \
  PAYARA_HOME="'$PAYARA_HOME'" \
  < start-payara-sormas.sh \
  > "${DOMAIN_DIR}/start-payara-sormas.sh"

redefine_vars \
  PAYARA_HOME="'$PAYARA_HOME'" \
  < stop-payara-sormas.sh \
  > "${DOMAIN_DIR}/stop-payara-sormas.sh"

chmod a+x "${DOMAIN_DIR}"/{start,stop}-payara-sormas.sh

cp logback.xml ${DOMAIN_DIR}/config/
if [[ ${DEV_SYSTEM} = true ]] && [[ ${LINUX} != true ]]; then
	# Fixes outdated certificate - don't do this on linux systems!
	cp cacerts.jks.bin "${DOMAIN_DIR}/config/cacerts.jks"
fi
cp loginsidebar.html "${CUSTOM_DIR}"
cp loginsidebar-header.html "${CUSTOM_DIR}"
cp logindetails.html "${CUSTOM_DIR}"
if [[ ${DEMO_SYSTEM} = true ]]; then
	cp demologinmain.html "${CUSTOM_DIR}/loginmain.html"
else
	cp loginmain.html "${CUSTOM_DIR}"
fi


# SORMAS SERVICE: Register SORMAS as a system service?
if [[ ${LINUX} = true ]]; then
  if [[ -z "${INSTALL_SERVICE}" ]]; then
    echo "$(bold "Do you want to install the init.d service file?")"
    select CHOICE in "Yes" "No"; do
      case "$CHOICE" in
        Yes ) INSTALL_SERVICE=true; break;;
        No ) INSTALL_SERVICE=false; break;;
      esac
    done
    remember_choice INSTALL_SERVICE
  fi

  if [[ ${INSTALL_SERVICE} = true ]]; then
    cp payara-sormas.sh /etc/init.d/payara-sormas
    chmod 755 /etc/init.d/payara-sormas
    update-rc.d payara-sormas defaults
  fi
	
	chown -R ${PAYARA_USER}:${PAYARA_USER} "${DOMAIN_DIR}"
fi

read -p "$(bold "--- Press [Enter] to continue...")"

# Logging
echo "Configuring logging..."
"${ASADMIN[@]}" create-jvm-options "-Dlogback.configurationFile=\${com.sun.aas.instanceRoot}/config/logback.xml"
"${ASADMIN[@]}" set-log-attributes com.sun.enterprise.server.logging.GFFileHandler.maxHistoryFiles=14
"${ASADMIN[@]}" set-log-attributes com.sun.enterprise.server.logging.GFFileHandler.rotationLimitInBytes=0
"${ASADMIN[@]}" set-log-attributes com.sun.enterprise.server.logging.GFFileHandler.rotationOnDateChange=true
#"${ASADMIN[@]}" set-log-levels org.wamblee.glassfish.auth.HexEncoder=SEVERE
#"${ASADMIN[@]}" set-log-levels javax.enterprise.system.util=SEVERE

if [[ ${DEV_SYSTEM} != true ]]; then
	# Make the payara listen to localhost only
	echo "Configuring security settings..."
	"${ASADMIN[@]}" set configs.config.server-config.http-service.virtual-server.server.network-listeners=http-listener-1
	"${ASADMIN[@]}" delete-network-listener --target=server-config http-listener-2
	"${ASADMIN[@]}" set configs.config.server-config.network-config.network-listeners.network-listener.admin-listener.address=127.0.0.1
	"${ASADMIN[@]}" set configs.config.server-config.network-config.network-listeners.network-listener.http-listener-1.address=127.0.0.1
	"${ASADMIN[@]}" set configs.config.server-config.iiop-service.iiop-listener.orb-listener-1.address=127.0.0.1
	"${ASADMIN[@]}" set configs.config.server-config.iiop-service.iiop-listener.SSL.address=127.0.0.1
	"${ASADMIN[@]}" set configs.config.server-config.iiop-service.iiop-listener.SSL_MUTUALAUTH.address=127.0.0.1
	"${ASADMIN[@]}" set configs.config.server-config.jms-service.jms-host.default_JMS_host.host=127.0.0.1
	"${ASADMIN[@]}" set configs.config.server-config.admin-service.jmx-connector.system.address=127.0.0.1
	"${ASADMIN[@]}" set-hazelcast-configuration --enabled=false
fi


# >>>>> EPILOGUE >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>

# don't stop the domain, because we need it running for the update script
#read -p "--- Press [Enter] to continue..."
#"${PAYARA_HOME}/bin/asadmin" stop-domain --domaindir "${DOMAINS_HOME}" "${DOMAIN_NAME}"

echo "Server setup completed."
echo "Commands to start and stop the domain: "
if [[ ${LINUX} = true && ${INSTALL_SERVICE} = true ]]; then
	echo "service payara-sormas start"
	echo "service payara-sormas stop"
else 
	echo "${DOMAIN_DIR}/start-payara-sormas.sh"
	echo "${DOMAIN_DIR}/stop-payara-sormas.sh"
fi
echo "---"
echo "Please make sure to perform the following steps:"
echo "  - Adjust the $DOMAIN_DIR/sormas.properties file to your system"
if [[ ${DEV_SYSTEM} != true ]]; then
	echo "  - Execute the sormas-update.sh file to populate the database and deploy the server"
	echo "  - Configure the apache web server according to the server setup guide"
fi
	echo "  - Execute the r-setup.sh file to enable disease network diagrams"
