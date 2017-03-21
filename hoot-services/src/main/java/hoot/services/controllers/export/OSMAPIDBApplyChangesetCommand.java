/*
 * This file is part of Hootenanny.
 *
 * Hootenanny is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * --------------------------------------------------------------------
 *
 * The following copyright notices are generated automatically. If you
 * have a new notice to add, please use the format:
 * " * @copyright Copyright ..."
 * This will properly maintain the copyright information. DigitalGlobe
 * copyrights will be updated automatically.
 *
 * @copyright Copyright (C) 2015, 2016, 2017 DigitalGlobe (http://www.digitalglobe.com/)
 */
package hoot.services.controllers.export;

import static hoot.services.HootProperties.OSMAPI_DB_URL;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/*
== apply-changeset

=== Description

The +apply-changeset+ command writes an OSM changeset file that represents the difference
between two OSM files to an OSM API database.  Optional conflict parameters may be
passed, so that if the target database contains any changesets created after the time
represented by a timestamp that intersect with a specified AOI, the command will return
an error.

* +sqlChangeset+ - input SQL changeset (e.g. .osc.sql file).
* +targetDatabaseUrl+ - database the changeset will be written to.
* +conflictAoi+ - bounding box string of the form: minx,miny,maxx,maxy used to prevent writing conflicted data (see description)
* +conflictTimestamp+ - timestamp of the form: "yyyy-MM-dd hh:mm:ss.zzz" used to prevent writing conflicted data (see description)

=== Usage

--------------------------------------
apply-changeset (sqlChangeset) (targetDatabaseUrl) [conflictAoi] [conflictTimestamp]
--------------------------------------

==== Examples

--------------------------------------
hoot apply-changeset changeset.osc.sql osmapidb://username:password@localhost:5432/databaseName

hoot apply-changeset changeset.osc.sql osmapidb://username:password@localhost:5432/databaseName -93.89258,40.96917,-93.60583,41.0425 "2016-05-04 10:15:37.000"-93.89258,40.96917,-93.60583,41.0425
--------------------------------------
 */

class OSMAPIDBApplyChangesetCommand extends ExportCommand {

    OSMAPIDBApplyChangesetCommand(String jobId, Map<String, String> paramMap, String debugLevel, Class<?> caller) {
        super(jobId, paramMap, debugLevel, caller);

        List<String> options = super.getCommonExportHootOptions();
        String hootOptions = options.stream().collect(Collectors.joining(" "));

        String mapName = paramMap.get("input");
        hoot.services.models.osm.Map conflatedMap = getConflatedMap(mapName);

        // AOI = Area of Interest
        String conflictAOI = getAoi(paramMap, conflatedMap);

        //timestamp of the form: "yyyy-MM-dd hh:mm:ss.zzz" used to prevent writing conflicted dat
        String conflictTimestamp = getMapForExportTag(conflatedMap);

        // Services currently always write changeset with sql
        String sqlChangeset = super.getSQLChangesetPath();

        String targetDatabaseUrl = OSMAPI_DB_URL;

        //hoot apply-changeset $(HOOT_OPTS) $(changesetoutput) "$(OSM_API_DB_URL)" "$(aoi)" "$(changesetsourcedatatimestamp)"
        String command = "hoot apply-changeset --" + debugLevel + " " + hootOptions + " " + sqlChangeset + " " + targetDatabaseUrl + " " + conflictAOI + " " + conflictTimestamp;

        super.configureCommand(command, caller);
    }

    private static String getMapForExportTag(hoot.services.models.osm.Map conflatedMap) {
        Map<String, String> tags = (Map<String, String>) conflatedMap.getTags();

        //+osm_api_db_export_time+ is a timestamp that's written at the time the data in the OSM API database is first exported.
        // It's checked against when writing the resulting changeset after the conflation job to see if any other changesets
        // were added to the OSM API db between the export time and the time the changeset is written.
        if (! tags.containsKey("osm_api_db_export_time")) {
            throw new IllegalStateException("Error exporting data.  Map with ID: " + conflatedMap.getId() + " has no osm_api_db_export_time tag.");
        }

        return tags.get("osm_api_db_export_time");
    }
}
