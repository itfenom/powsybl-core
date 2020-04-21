/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.tck;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public abstract class AbstractMergeNetworkTest {

    private static final String MERGE2 = "merge";

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    Network merge;
    Network n1;
    Network n2;

    @Before
    public void setup() {
        merge = Network.create(MERGE2, "asdf");
        n1 = Network.create("n1", "asdf");
        n2 = Network.create("n2", "qwer");
    }

    @Test
    public void failMergeIfMultiVariants() {
        n1.getVariantManager().cloneVariant(VariantManagerConstants.INITIAL_VARIANT_ID, "Totest");
        thrown.expect(PowsyblException.class);
        thrown.expectMessage("Merging of multi-variants network is not supported");
        merge.merge(n1);
    }

    @Test
    public void failMergeWithSameObj() {
        addSubstation(n1, "P1");
        addSubstation(n2, "P1");
        merge.merge(n1);
        thrown.expect(PowsyblException.class);
        thrown.expectMessage("The following object(s) of type SubstationImpl exist(s) in both networks: [P1]");
        merge.merge(n2);
    }

    @Test
    public void xnodeNonCompatible() {
        addSubstationAndVoltageLevel();
        addDanglingLine("dl", "code", "dl", "deco");
        merge.merge(n1);
        thrown.expect(PowsyblException.class);
        thrown.expectMessage("Dangling line couple dl have inconsistent Xnodes (code!=deco)");
        merge.merge(n2);
    }

    @Test
    public void testMerge() {
        addSubstationAndVoltageLevel();
        addDanglingLine("dl1", "code", "dl2", "code");

        DanglingLine dl1 = n1.getDanglingLine("dl1");
        dl1.setStringProperty("network", "noEquipNetwork");
        dl1.setStringProperty("vl", "");
        dl1.setStringProperty("OneSideProp", "one side prop");
        dl1.setIntegerProperty("diffTypeProp", 1);
        dl1.setStringProperty("str", "test");
        dl1.setStringProperty("str2", "");
        dl1.setStringProperty("str3", "test*");
        dl1.setStringProperty("strError", "error");
        dl1.setIntegerProperty("int", 5);
        dl1.setIntegerProperty("int3", 10);
        dl1.setIntegerProperty("intError", 54);
        dl1.setDoubleProperty("double", 5d);
        dl1.setDoubleProperty("double3", 8d);
        dl1.setDoubleProperty("doubleError", 10d);
        dl1.setBooleanProperty("bool", true);
        dl1.setBooleanProperty("bool3", true);
        dl1.setBooleanProperty("boolError", true);

        DanglingLine dl2 = n2.getDanglingLine("dl2");
        dl2.setStringProperty("network", "");
        dl2.setStringProperty("vl", "vl2");
        dl2.setDoubleProperty("diffTypeProp", 12d);
        dl2.setStringProperty("str", "test");
        dl2.setStringProperty("str2", "test*");
        dl2.setStringProperty("str3", "");
        dl2.setStringProperty("strError", "errorDiff");
        dl2.setIntegerProperty("int", 5);
        dl2.setIntegerProperty("int2", 10);
        dl2.setIntegerProperty("intError", 55);
        dl2.setDoubleProperty("double", 5d);
        dl2.setDoubleProperty("double2", 8d);
        dl2.setDoubleProperty("doubleError", 11d);
        dl2.setBooleanProperty("bool", true);
        dl2.setBooleanProperty("bool2", true);
        dl2.setBooleanProperty("boolError", false);

        merge.merge(n1, n2);
        assertNotNull(merge.getLine("dl1 + dl2"));
        assertEquals("dl1_name + dl2_name", merge.getLine("dl1 + dl2").getName());
        assertEquals(6, merge.getLine("dl1 + dl2").getStringPropertyNames().size());
        assertEquals(3, merge.getLine("dl1 + dl2").getIntegerPropertyNames().size());
        assertEquals(3, merge.getLine("dl1 + dl2").getDoublePropertyNames().size());
        assertEquals(3, merge.getLine("dl1 + dl2").getBooleanPropertyNames().size());
    }

    private void addSubstation(Network network, String substationId) {
        network.newSubstation()
                            .setId(substationId)
                            .setCountry(Country.FR)
                            .setTso("RTE")
                            .setGeographicalTags("A")
                        .add();
    }

    private void addSubstationAndVoltageLevel() {
        Substation s1 = n1.newSubstation()
                .setId("s1")
                .setCountry(Country.FR)
                .add();
        VoltageLevel vl1 = s1.newVoltageLevel()
                .setId("vl1")
                .setNominalV(380)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl1.getBusBreakerView().newBus()
                .setId("b1")
                .add();

        Substation s2 = n2.newSubstation()
                .setId("s2")
                .setCountry(Country.BE)
                .add();
        VoltageLevel vl2 = s2.newVoltageLevel()
                .setId("vl2")
                .setNominalV(380)
                .setTopologyKind(TopologyKind.BUS_BREAKER)
                .add();
        vl2.getBusBreakerView().newBus()
                .setId("b2")
                .add();
    }

    private void addDanglingLine(String dl1, String code1, String dl2, String code2) {
        n1.getVoltageLevel("vl1").newDanglingLine()
                .setId(dl1)
                .setName(dl1 + "_name")
                .setConnectableBus("b1")
                .setBus("b1")
                .setP0(0.0)
                .setQ0(0.0)
                .setR(1.0)
                .setX(2.0)
                .setG(4.0)
                .setB(5.0)
                .setUcteXnodeCode(code1)
                .add();
        n2.getVoltageLevel("vl2").newDanglingLine()
                .setId(dl2)
                .setName(dl2 + "_name")
                .setConnectableBus("b2")
                .setBus("b2")
                .setP0(0.0)
                .setQ0(0.0)
                .setR(10.0)
                .setX(20.0)
                .setG(30.0)
                .setB(40.0)
                .setUcteXnodeCode(code2)
                .add();
    }

    @Test
    public void test() {
        merge.merge(n1, n2);
        assertEquals(MERGE2, merge.getId());
        assertEquals("hybrid", merge.getSourceFormat());
    }

    @Test
    public void checkMergingSameFormat() {
        merge.merge(n1);
        assertEquals(MERGE2, merge.getId());
        assertEquals("asdf", merge.getSourceFormat());
    }

    @Test
    public void checkMergingDifferentFormat() {
        merge.merge(n2);
        assertEquals(MERGE2, merge.getId());
        assertEquals("hybrid", merge.getSourceFormat());
    }
}
