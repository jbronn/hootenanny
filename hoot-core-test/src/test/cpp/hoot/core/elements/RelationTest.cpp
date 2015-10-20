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
 * @copyright Copyright (C) 2015 DigitalGlobe (http://www.digitalglobe.com/)
 */

// Hoot
#include <hoot/core/OsmMap.h>
#include <hoot/core/elements/Relation.h>
#include <hoot/core/visitors/CountVisitor.h>

#include "../TestUtils.h"

namespace hoot
{

class RelationTest : public CppUnit::TestFixture
{
  CPPUNIT_TEST_SUITE(RelationTest);
  CPPUNIT_TEST(runCircularVisitRo1Test);
  CPPUNIT_TEST(runCircularVisitRo2Test);
  CPPUNIT_TEST(runCircularVisitRw1Test);
  CPPUNIT_TEST(runCircularVisitRw2Test);
  CPPUNIT_TEST_SUITE_END();

public:

  void runCircularVisitRo1Test()
  {
    DisableLog dl;

    shared_ptr<OsmMap> map(new OsmMap());
    shared_ptr<Relation> r1(new Relation(Status::Unknown1, 1, 15));
    shared_ptr<Relation> r2(new Relation(Status::Unknown1, 2, 15));
    map->addElement(r1);
    map->addElement(r2);

    r1->addElement("", r2->getElementId());
    r2->addElement("", r1->getElementId());

    CountVisitor v;
    r1->visitRo(*map, v);

    LOG_VAR(v.getCount());
  }

  void runCircularVisitRo2Test()
  {
    DisableLog dl;

    shared_ptr<OsmMap> map(new OsmMap());
    shared_ptr<Relation> r1(new Relation(Status::Unknown1, 1, 15));
    shared_ptr<Relation> r2(new Relation(Status::Unknown1, 2, 15));
    map->addElement(r1);
    map->addElement(r2);

    r1->addElement("", r1->getElementId());

    CountVisitor v;
    r1->visitRo(*map, v);
    r2->visitRo(*map, v);

    LOG_VAR(v.getCount());
  }

  void runCircularVisitRw1Test()
  {
    DisableLog dl;

    shared_ptr<OsmMap> map(new OsmMap());
    shared_ptr<Relation> r1(new Relation(Status::Unknown1, 1, 15));
    shared_ptr<Relation> r2(new Relation(Status::Unknown1, 2, 15));
    map->addElement(r1);
    map->addElement(r2);

    r1->addElement("", r2->getElementId());
    r2->addElement("", r1->getElementId());

    CountVisitor v;
    r1->visitRw(*map, v);

    LOG_VAR(v.getCount());
  }

  void runCircularVisitRw2Test()
  {
    DisableLog dl;

    shared_ptr<OsmMap> map(new OsmMap());
    shared_ptr<Relation> r1(new Relation(Status::Unknown1, 1, 15));
    shared_ptr<Relation> r2(new Relation(Status::Unknown1, 2, 15));
    map->addElement(r1);
    map->addElement(r2);

    r1->addElement("", r1->getElementId());

    CountVisitor v;
    r1->visitRw(*map, v);
    r2->visitRw(*map, v);

    LOG_VAR(v.getCount());
  }
};

CPPUNIT_TEST_SUITE_NAMED_REGISTRATION(RelationTest, "quick");

}


