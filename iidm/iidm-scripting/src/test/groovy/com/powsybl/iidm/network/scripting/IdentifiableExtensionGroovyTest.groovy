/**
 * Copyright (c) 2017, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.scripting

import com.powsybl.commons.extensions.AbstractExtension
import com.powsybl.iidm.network.Country
import com.powsybl.iidm.network.Network
import com.powsybl.iidm.network.Substation
import org.junit.Before
import org.junit.Test

import static org.junit.Assert.*

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
class IdentifiableExtensionGroovyTest {

    static class Foo extends AbstractExtension<Substation> {

        private float value

        @Override
        String getName() {
            return "foo"
        }
    }

    private Substation s

    @Before
    void setUp() throws Exception {
        Network network = Network.create("test", "test")
        s = network.newSubstation()
                .setId("S")
                .setCountry(Country.FR)
                .add()
    }

    @Test
    void testStringProperty() {
        assertFalse(s.hasProperty())
        assertNull(s.greeting)
        s.setProperty("greeting", "hello")
        assertEquals("hello", s.greeting)
        assertEquals("hello", s.getProperty("greeting"))
    }

    @Test
    void testBooleanProperty() {
        assertFalse(s.hasProperty())
        assertNull(s.ok)
        s.setBooleanProperty("ok", true)
        assertEquals(true, s.ok)
        assertEquals(true, s.getBooleanProperty("ok"))
        s.ok = null
        assertNull(s.ok)
        s.ko = false
        assertEquals(false, s.ko)
        assertEquals(false, s.getBooleanProperty("ko"))
    }

    @Test
    void testExtension() {
        assertNull(s.foo)
        s.addExtension(Foo.class, new Foo())
        assertNotNull(s.foo)
        s.foo.value = 3f
        assertEquals(3f, s.foo.value, 0f)
    }

}
