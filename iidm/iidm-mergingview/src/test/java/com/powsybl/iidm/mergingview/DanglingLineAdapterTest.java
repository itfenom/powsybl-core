/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.commons.PowsyblException;
import com.powsybl.iidm.network.*;
import com.powsybl.iidm.network.test.NoEquipmentNetworkFactory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
public class DanglingLineAdapterTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    private MergingView mergingView;

    private Network noEquipNetwork;

    @Before
    public void initNetwork() {
        mergingView = MergingView.create("DanglingLineAdapterTest", "iidm");
        noEquipNetwork = NoEquipmentNetworkFactory.create();
    }

    @Test
    public void baseTests() {
        mergingView.merge(noEquipNetwork);

        double r = 10.0;
        double x = 20.0;
        double g = 30.0;
        double b = 40.0;
        double p0 = 50.0;
        double q0 = 60.0;
        String id = "danglingId";
        String name = "danglingName";
        String ucteXnodeCode = "standalone";
        String busId = "busA";
        String voltageLevelId = "vl1";

        // adder
        DanglingLine danglingLine = createDanglingLine(mergingView, voltageLevelId, id, name, r, x, g, b, p0, q0, ucteXnodeCode, busId);
        assertNotNull(mergingView.getDanglingLine(id));
        assertTrue(danglingLine instanceof DanglingLineAdapter);
        assertSame(mergingView, danglingLine.getNetwork());

        assertEquals(ConnectableType.DANGLING_LINE, danglingLine.getType());
        assertEquals(r, danglingLine.getR(), 0.0);
        assertEquals(x, danglingLine.getX(), 0.0);
        assertEquals(g, danglingLine.getG(), 0.0);
        assertEquals(b, danglingLine.getB(), 0.0);
        assertEquals(p0, danglingLine.getP0(), 0.0);
        assertEquals(q0, danglingLine.getQ0(), 0.0);
        assertEquals(id, danglingLine.getId());
        assertEquals(name, danglingLine.getOptionalName().orElse(null));
        assertEquals(name, danglingLine.getNameOrId());
        assertEquals(ucteXnodeCode, danglingLine.getUcteXnodeCode());

        // setter getter
        double r2 = 11.0;
        double x2 = 21.0;
        double g2 = 31.0;
        double b2 = 41.0;
        double p02 = 51.0;
        double q02 = 61.0;
        danglingLine.setR(r2);
        assertEquals(r2, danglingLine.getR(), 0.0);
        danglingLine.setX(x2);
        assertEquals(x2, danglingLine.getX(), 0.0);
        danglingLine.setG(g2);
        assertEquals(g2, danglingLine.getG(), 0.0);
        danglingLine.setB(b2);
        assertEquals(b2, danglingLine.getB(), 0.0);
        danglingLine.setP0(p02);
        assertEquals(p02, danglingLine.getP0(), 0.0);
        danglingLine.setQ0(q02);
        assertEquals(q02, danglingLine.getQ0(), 0.0);

        danglingLine.newCurrentLimits()
                .setPermanentLimit(100.0)
                .add();
        assertEquals(100.0, danglingLine.getCurrentLimits().getPermanentLimit(), 0.0);

        assertTrue(danglingLine.getTerminal() instanceof TerminalAdapter);
        danglingLine.getTerminals().forEach(t -> {
            assertTrue(t instanceof TerminalAdapter);
            assertNotNull(t);
        });
        assertEquals(1, danglingLine.getTerminals().size());
    }

    @Test
    public void mergedDanglingLine() {
        mergingView.merge(noEquipNetwork);
        double p0 = 1.0;
        double q0 = 1.0;
        final DanglingLine dl1 = createDanglingLine(mergingView, "vl1", "dl1", "dl1", 1.0, 1.0, 1.0, 1.0, p0, q0, "code", "busA");
        assertNotNull(mergingView.getDanglingLine("dl1"));
        assertEquals(1, mergingView.getDanglingLineCount());
        assertEquals(0, mergingView.getLineCount());
        final DanglingLine dl2 = createDanglingLine(mergingView, "vl2", "dl2", "dl2", 1.0, 1.0, 1.0, 1.0, p0, q0, "code", "busB");
        // Check no access to Dl1 & Dl2
        assertEquals(0, mergingView.getDanglingLineCount());
        assertNull(mergingView.getDanglingLine("dl1"));
        assertNull(mergingView.getDanglingLine("dl2"));
        // Check access to MergedLine
        assertEquals(1, mergingView.getLineCount());
        assertEquals(1, mergingView.getBranchCount());
        final Line line = mergingView.getLine("dl1 + dl2");
        assertSame(line, mergingView.getIdentifiable("dl1 + dl2"));
        assertSame(line, mergingView.getBranch("dl1 + dl2"));
        assertSame(line, mergingView.getIdentifiable("dl1"));
        assertSame(line, mergingView.getIdentifiable("dl2"));
        assertTrue(line instanceof MergedLine);
        assertTrue(mergingView.getIdentifiables().contains(line));

        // MergedLine
        final MergedLine mergedLine = (MergedLine) line;
        assertEquals(ConnectableType.LINE, mergedLine.getType());
        assertFalse(mergedLine.isTieLine());
        assertSame(mergingView, mergedLine.getNetwork());
        assertSame(dl1.getTerminal(), mergedLine.getTerminal(Branch.Side.ONE));
        assertSame(dl1.getTerminal(), mergedLine.getTerminal1());
        assertSame(dl2.getTerminal(), mergedLine.getTerminal(Branch.Side.TWO));
        assertSame(dl2.getTerminal(), mergedLine.getTerminal2());
        assertSame(dl1.getCurrentLimits(), mergedLine.getCurrentLimits(Branch.Side.ONE));
        assertSame(dl1.getCurrentLimits(), mergedLine.getCurrentLimits1());
        assertSame(dl2.getCurrentLimits(), mergedLine.getCurrentLimits(Branch.Side.TWO));
        assertSame(dl2.getCurrentLimits(), mergedLine.getCurrentLimits2());
        final CurrentLimits currentLimits1 = mergedLine.newCurrentLimits1()
                .setPermanentLimit(100)
                .beginTemporaryLimit()
                .setName("5'")
                .setAcceptableDuration(5 * 60)
                .setValue(1400)
                .endTemporaryLimit()
                .add();
        final CurrentLimits currentLimits2 = mergedLine.newCurrentLimits2()
                .setPermanentLimit(50)
                .beginTemporaryLimit()
                .setName("20'")
                .setAcceptableDuration(20 * 60)
                .setValue(1200)
                .endTemporaryLimit()
                .add();
        assertSame(currentLimits1, mergedLine.getCurrentLimits1());
        assertSame(currentLimits2, mergedLine.getCurrentLimits2());
        assertSame(currentLimits1, mergedLine.getCurrentLimits(Branch.Side.ONE));
        assertSame(currentLimits2, mergedLine.getCurrentLimits(Branch.Side.TWO));
        assertEquals("dl1 + dl2", mergedLine.getId());
        assertEquals("dl1 + dl2", mergedLine.getOptionalName().orElse(null));
        assertEquals("dl1 + dl2", mergedLine.getNameOrId());
        assertSame(mergedLine, mergedLine.setR(1.0d));
        assertEquals(dl1.getR() + dl2.getR(), mergedLine.getR(), 0.0d);
        assertSame(mergedLine, mergedLine.setX(2.0d));
        assertEquals(dl1.getX() + dl2.getX(), mergedLine.getX(), 0.0d);
        assertSame(mergedLine, mergedLine.setG1(3.0d));
        assertEquals(dl1.getG(), mergedLine.getG1(), 0.0d);
        assertSame(mergedLine, mergedLine.setG2(4.0d));
        assertEquals(dl2.getG(), mergedLine.getG2(), 0.0d);
        assertSame(mergedLine, mergedLine.setB1(5.0d));
        assertEquals(dl1.getB(), mergedLine.getB1(), 0.0d);
        assertSame(mergedLine, mergedLine.setB2(6.0d));
        assertEquals(dl2.getB(), mergedLine.getB2(), 0.0d);
        assertEquals(p0, dl1.getP0(), 0.0d);
        assertEquals(q0, dl1.getQ0(), 0.0d);
        assertEquals(p0, dl2.getP0(), 0.0d);
        assertEquals(q0, dl2.getQ0(), 0.0d);

        double p1 = -605.0;
        double q1 = -302.5;
        double p2 = 600.0;
        double q2 = 300.0;
        double lossesP = p1 + p2;
        double lossesQ = q1 + q2;
        final Terminal t1 = mergedLine.getTerminal("vl1");
        assertNotNull(t1);
        assertEquals(Branch.Side.ONE, mergedLine.getSide(t1));
        final Terminal t2 = mergedLine.getTerminal("vl2");
        assertNotNull(t2);
        assertEquals(Branch.Side.TWO, mergedLine.getSide(t2));
        // Update P & Q
        t1.setP(p1);
        t1.setQ(q1);
        t2.setP(p2);
        t2.setQ(q2);
        // Check P & Q are computed by Listener
        assertEquals(p1 + (lossesP / 2.0), dl1.getP0(), 0.0d);
        assertEquals(q1 + (lossesQ / 2.0), dl1.getQ0(), 0.0d);
        assertEquals((p2 + (lossesP / 2.0)) * -1, dl2.getP0(), 0.0d);
        assertEquals((q2 + (lossesQ / 2.0)) * -1, dl2.getQ0(), 0.0d);

        assertFalse(mergedLine.isOverloaded());
        assertEquals(Integer.MAX_VALUE, mergedLine.getOverloadDuration());
        assertFalse(mergedLine.checkPermanentLimit(Branch.Side.ONE));
        assertFalse(mergedLine.checkPermanentLimit(Branch.Side.TWO));
        assertFalse(mergedLine.checkPermanentLimit1());
        assertFalse(mergedLine.checkPermanentLimit2());
        assertNull(mergedLine.checkTemporaryLimits(Branch.Side.ONE));
        assertNull(mergedLine.checkTemporaryLimits(Branch.Side.TWO));
        assertNull(mergedLine.checkTemporaryLimits1(1.0f));
        assertNull(mergedLine.checkTemporaryLimits1());
        assertNull(mergedLine.checkTemporaryLimits2(1.0f));
        assertNull(mergedLine.checkTemporaryLimits2());
        mergedLine.getTerminals().forEach(t -> {
            assertTrue(t instanceof TerminalAdapter);
            assertNotNull(t);
        });

        mergedLine.setFictitious(true);
        assertTrue(mergedLine.isFictitious());

        // Not implemented yet !
        TestUtil.notImplemented(mergedLine::remove);
        TestUtil.notImplemented(() -> mergedLine.addExtension(null, null));
        TestUtil.notImplemented(() -> mergedLine.getExtension(null));
        TestUtil.notImplemented(() -> mergedLine.getExtensionByName(""));
        TestUtil.notImplemented(() -> mergedLine.removeExtension(null));
        TestUtil.notImplemented(mergedLine::getExtensions);

        // Exception(s)
        thrown.expect(PowsyblException.class);
        thrown.expectMessage("No terminal connected to voltage level invalid");
        mergedLine.getTerminal("invalid");
    }

    @Test
    public void testProperties() {
        final DanglingLine dl1 = createDanglingLine(noEquipNetwork, "vl1", "dl1", "dl", 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, "code", "busA");
        dl1.setStringProperty("ucteCode", dl1.getUcteXnodeCode()); // test equals property
        dl1.setStringProperty("id", dl1.getId()); // test not equals property
        dl1.setStringProperty("network", "noEquipNetwork"); // test empty property
        dl1.setStringProperty("vl", ""); // test empty property

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

        final DanglingLine dl2 = createDanglingLine(noEquipNetwork, "vl2", "dl2", "dl", 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, "code", "busB");
        dl2.setStringProperty("ucteCode", dl2.getUcteXnodeCode()); // test equals property
        dl2.setStringProperty("id", dl2.getId()); // test not equals property
        dl2.setStringProperty("network", ""); // test empty property
        dl2.setStringProperty("vl", "vl2"); // test empty property

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

        mergingView.merge(noEquipNetwork);
        final Line line = mergingView.getLine("dl1 + dl2");
        final MergedLine mergedLine = (MergedLine) line;
        assertEquals("dl", mergedLine.getOptionalName().orElse(null));
        assertEquals("dl", mergedLine.getNameOrId());

        assertTrue(mergedLine.hasProperty());
        assertTrue(mergedLine.hasProperty("ucteCode"));
        assertEquals(6, mergedLine.getPropertyNames().size());

        String keyBool = "bool-test";
        String keyInt = "int-test";
        String keyDouble = "double-test";
        String keyString = "string-test";

        int intValue = 5;
        double doubleValue = 5d;
        String stringValue = "test";
        int intValue2 = 52;
        double doubleValue2 = 51d;
        String stringValue2 = "test2";

        mergedLine.setBooleanProperty(keyBool, true);
        mergedLine.setIntegerProperty(keyInt, intValue);
        mergedLine.setDoubleProperty(keyDouble, doubleValue);
        mergedLine.setStringProperty(keyString, stringValue);

        assertTrue(mergedLine.hasBooleanProperty());
        assertTrue(mergedLine.getOptionalBooleanProperty(keyBool).isPresent());
        assertTrue(mergedLine.getBooleanProperty(keyBool));
        assertEquals(Identifiable.PropertyType.BOOLEAN, mergedLine.getPropertyType(keyBool));

        assertTrue(mergedLine.hasIntegerProperty());
        assertTrue(mergedLine.getOptionalIntegerProperty(keyInt).isPresent());
        assertEquals(intValue, mergedLine.getIntegerProperty(keyInt).intValue());
        assertEquals(Identifiable.PropertyType.INTEGER, mergedLine.getPropertyType(keyInt));

        assertTrue(mergedLine.hasDoubleProperty());
        assertTrue(mergedLine.getOptionalDoubleProperty(keyDouble).isPresent());
        assertEquals(doubleValue, mergedLine.getDoubleProperty(keyDouble), 0.001d);
        assertEquals(Identifiable.PropertyType.DOUBLE, mergedLine.getPropertyType(keyDouble));

        assertTrue(mergedLine.hasStringProperty());
        assertTrue(mergedLine.getOptionalStringProperty(keyString).isPresent());
        assertEquals(stringValue, mergedLine.getStringProperty(keyString));
        assertEquals(Identifiable.PropertyType.STRING, mergedLine.getPropertyType(keyString));

        assertEquals(7, mergedLine.getStringPropertyNames().size());
        assertEquals(4, mergedLine.getIntegerPropertyNames().size());
        assertEquals(4, mergedLine.getDoublePropertyNames().size());
        assertEquals(4, mergedLine.getBooleanPropertyNames().size());

        mergedLine.setBooleanProperty(keyBool, false);
        mergedLine.setIntegerProperty(keyInt, intValue2);
        mergedLine.setDoubleProperty(keyDouble, doubleValue2);
        mergedLine.setStringProperty(keyString, stringValue2);
        assertFalse(mergedLine.getBooleanProperty(keyBool));
        assertEquals(intValue2, mergedLine.getIntegerProperty(keyInt).intValue());
        assertEquals(doubleValue2, mergedLine.getDoubleProperty(keyDouble), 0.001d);
        assertEquals(stringValue2, mergedLine.getStringProperty(keyString));
        assertEquals(7, mergedLine.getStringPropertyNames().size());
        assertEquals(4, mergedLine.getIntegerPropertyNames().size());
        assertEquals(4, mergedLine.getDoublePropertyNames().size());
        assertEquals(4, mergedLine.getBooleanPropertyNames().size());

        assertTrue(mergedLine.getBooleanProperty("notFound", true));
        assertEquals(intValue2, mergedLine.getIntegerProperty("notFound", intValue2).intValue());
        assertEquals(doubleValue2, mergedLine.getDoubleProperty("notFound", doubleValue2), 0.001d);
        assertEquals(stringValue2, mergedLine.getStringProperty("notFound", stringValue2));
    }

    @Test
    public void testListener() {
        mergingView.merge(noEquipNetwork);
        createDanglingLine(mergingView, "vl1", "testListener1", "testListener2", 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, "testListenerCode", "busA");
        assertNotNull(mergingView.getDanglingLine("testListener1"));
        assertEquals(1, mergingView.getDanglingLineCount());
        assertEquals(0, mergingView.getLineCount());
        createDanglingLine(noEquipNetwork, "vl2", "testListener2", "testListener1", 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, "testListenerCode", "busB");
        assertNull(mergingView.getDanglingLine("testListener1"));
        assertNull(mergingView.getDanglingLine("testListener2"));
        assertEquals(0, mergingView.getDanglingLineCount());
        assertEquals(1, mergingView.getLineCount());
        final Line line = mergingView.getLine("testListener1 + testListener2");
        final MergedLine mergedLine = (MergedLine) line;
        assertEquals("testListener1 + testListener2", mergedLine.getOptionalName().orElse(null));
        assertEquals("testListener1 + testListener2", mergedLine.getNameOrId());

    }

    private static DanglingLine createDanglingLine(Network n, String vlId, String id, String name, double r, double x, double g, double b,
                                            double p0, double q0, String ucteCode, String busId) {

        DanglingLine dl = n.getVoltageLevel(vlId).newDanglingLine()
                                                     .setId(id)
                                                     .setName(name)
                                                     .setR(r)
                                                     .setX(x)
                                                     .setG(g)
                                                     .setB(b)
                                                     .setP0(p0)
                                                     .setQ0(q0)
                                                     .setUcteXnodeCode(ucteCode)
                                                     .setBus(busId)
                                                     .setConnectableBus(busId)
                                                     .setEnsureIdUnicity(false)
                                                 .add();
        return dl;
    }
}
