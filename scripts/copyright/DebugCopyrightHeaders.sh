#!/bin/bash
set -exuo pipefail

if [ -a LicenseTemplate.txt ] ; then
    LICENSE_TEMPLATE=LicenseTemplate.txt
elif [ -a ../LicenseTemplate.txt ] ; then
    LICENSE_TEMPLATE=../LicenseTemplate.txt
else
    echo "Error: LicenseTemplate.txt not found"
    exit 1
fi

echo "--------------- DEBUG ---------------"

echo "Current Directory: $(pwd)\n"

echo "Current Environment: \n"

env

echo "Find Output\n"

find . -type f -and \( \( -name \*.js -or -name \*.java -or -name \*.cpp -or -name \*.h \) -and -not \( -wholename \*/db2/\* -or -wholename \*/target/\* -or -wholename \*/tmp/\* -or -name \*.pb.h -or -wholename \*/schema/__init__.js -or -wholename \*/etds\*/__init__.js \) \) -exec $HOOT_HOME/scripts/copyright/UpdateCopyrightHeader.py --copyright-header=$LICENSE_TEMPLATE --file={} --update-mode=0 \;
