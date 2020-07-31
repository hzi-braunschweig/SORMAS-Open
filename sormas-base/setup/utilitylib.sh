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

# Rudimentary parsing routine for `<VAR>=<VALUE>` expressions to retrieve VAR
# and VALUE independently of each other.
tokenize_vardef () {
	local SPLIT_IDX=`expr index "$1" =`
	local NAME="${1:0:`expr $SPLIT_IDX - 1`}"
	local VALUE="${1:$SPLIT_IDX}"

	echo "$NAME" "$VALUE"
}

# Prefix a variable definition with this function to only apply specified
# value to the specified variable if it has not been set already, e.g. by
# environment or as variable override like `<VAR>=<VALUE> <command> ...`.
defaulted () {
	local VARDEF_PARTS=(`tokenize_vardef "$1"`)
	local NAME="${VARDEF_PARTS[0]}"
	local DEFAULT_VALUE="${VARDEF_PARTS[1]}"

	if [[ -z "${!NAME}" ]]; then
		printf -v "$NAME" '%s' "$DEFAULT_VALUE"
	fi
}

# Like defaulted function, but asks the user via command line for the
# variable's value if it does not already exist. The value of the
# passed assignment expression will be presented to the user as the default
# value.
#
# Usage: prompted_defaulted <VAR>=<Default Value> [<prompt>]
prompted_defaulted () {
	local VARDEF_PARTS=(`tokenize_vardef "$1"`)

	if [[ -z "${!VARDEF_PARTS[0]}" ]]; then
		local PROMPT
		if [[ $# -ge 2 ]]; then
			PROMPT="$2"
		else
			PROMPT="Value for variable ${VARDEF_PARTS[0]}"
		fi
		PROMPT="${PROMPT}: [${VARDEF_PARTS[1]}] "
		local VALUE
		read -p "$PROMPT" VALUE
		if [[ -z "$VALUE" ]]; then
			VALUE="${VARDEF_PARTS[1]}"
		fi

		printf -v "${VARDEF_PARTS[0]}" '%s' "$VALUE"
	fi
}

# Override all variable assignments in the stream provided by Stdin
# with those specified as arguments to this function. Requirement for
# assignments to be replaced is for those to start directly at the
# beginning of the line and being an assignment to a variable with a
# name being part of one of the override expression arguments.
#
# Usage: redefine_vars {<VAR>=<VALUE>}
redefine_vars () {
	local SED_ARG=""
	for VARDEF in "$@"; do
		local VARDEF_PARTS=(`tokenize_vardef "$VARDEF"`)
		SED_ARG="$SED_ARG;"{/^${VARDEF_PARTS[0]}=/c'\'$'\n'"$VARDEF"$'\n'}
	done
	SED_ARG="${SED_ARG:1}"

	sed -e "$SED_ARG"
}
