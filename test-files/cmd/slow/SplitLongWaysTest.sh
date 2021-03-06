#!/bin/bash
set -e

export HOOT_OPTS=--warn
export ADDITIONAL_VISITORS="hoot::SplitLongLinearWaysVisitor"
export TRANSLATION="TDSv40.js"
export OUTPUT_DIR=$HOOT_HOME/test-output/cmd/slow/SplitLongWaysTest
export DATA_DIR=$HOOT_HOME/test-files
export INPUTS=$DATA_DIR/SplitLongWaysTest.shp
export KNOWN_GOOD_OUTPUT=$DATA_DIR/SplitLongWaysTestResults.osm.pbf
export TEST_OUTPUT=$OUTPUT_DIR/SplitLongWaysTest.osm.pbf

# Wipe out output dir so we don't get stale data and false pass
rm -rf $OUTPUT_DIR
mkdir -p $OUTPUT_DIR

hoot convert-ogr2osm $HOOT_OPTS -D ogr2osm.ops+=$ADDITIONAL_VISITORS \
    $HOOT_HOME/translations/$TRANSLATION \
    $TEST_OUTPUT $INPUTS

hoot map-diff $HOOT_OPTS $KNOWN_GOOD_OUTPUT $TEST_OUTPUT && echo "Test Complete"
