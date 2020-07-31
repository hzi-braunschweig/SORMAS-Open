#!/bin/bash
#*******************************************************************************
# SORMAS® - Surveillance Outbreak Response Management & Analysis System
# Copyright © 2016-2018 Helmholtz-Zentrum f�r Infektionsforschung GmbH (HZI)
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

PAYARA_HOME="/opt/payara5"

DOMAIN_DIR="$(dirname "$0")"
DOMAINS_HOME="$(dirname "$DOMAIN_DIR")"
SORMAS_DOMAIN_NAME="$(basename "$DOMAIN_DIR")"
ASADMIN="${PAYARA_HOME}/bin/asadmin"

"${ASADMIN}" stop-domain --domaindir "${DOMAINS_HOME}" "${SORMAS_DOMAIN_NAME}"
