/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.loadflow.validation;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 *
 * @author Massimo Ferraro <massimo.ferraro@techrain.eu>
 */
public class ValidationUtilsTest extends AbstractValidationTest {

    @Test
    public void areNaN() {
        assertFalse(ValidationUtils.areNaN(looseConfig, 1.02f));
        assertFalse(ValidationUtils.areNaN(looseConfig, 1f, 3.5f));
        assertFalse(ValidationUtils.areNaN(looseConfig, 3.7f, 2f, .004f));
        assertTrue(ValidationUtils.areNaN(looseConfig, Float.NaN));
        assertTrue(ValidationUtils.areNaN(looseConfig, Float.NaN, 2f, .004f));
        assertTrue(ValidationUtils.areNaN(looseConfig, Float.NaN, 2f, Float.NaN));

        assertFalse(ValidationUtils.areNaN(looseConfig, 1.02d));
        assertFalse(ValidationUtils.areNaN(looseConfig, 1d, 3.5d));
        assertFalse(ValidationUtils.areNaN(looseConfig, 3.7d, 2d, .004d));
        assertTrue(ValidationUtils.areNaN(looseConfig, Double.NaN));
        assertTrue(ValidationUtils.areNaN(looseConfig, Double.NaN, 2d, .004d));
        assertTrue(ValidationUtils.areNaN(looseConfig, Double.NaN, 2d, Double.NaN));

        looseConfig.setOkMissingValues(true);
        assertFalse(ValidationUtils.areNaN(looseConfig, 3.7f, 2f, .004f));
        assertFalse(ValidationUtils.areNaN(looseConfig, Float.NaN));
        assertFalse(ValidationUtils.areNaN(looseConfig, Float.NaN, 2f, .004f));
        assertFalse(ValidationUtils.areNaN(looseConfig, Float.NaN, 2f, Float.NaN));
        assertFalse(ValidationUtils.areNaN(looseConfig, 3.7d, 2d, .004d));
        assertFalse(ValidationUtils.areNaN(looseConfig, Double.NaN));
        assertFalse(ValidationUtils.areNaN(looseConfig, Double.NaN, 2d, .004d));
        assertFalse(ValidationUtils.areNaN(looseConfig, Double.NaN, 2d, Double.NaN));
    }

    @Test
    public void boundedWithin() {
        assertTrue(ValidationUtils.boundedWithin(0f, 10f, 5f, 0f));
        assertFalse(ValidationUtils.boundedWithin(0f, 10f, -5f, 0f));
        assertFalse(ValidationUtils.boundedWithin(0f, 10f, 15f, 0f));

        assertFalse(ValidationUtils.boundedWithin(0f, 10f, Float.NaN, 0f));
        assertFalse(ValidationUtils.boundedWithin(Float.NaN, Float.NaN, 5f, 0f));

        assertTrue(ValidationUtils.boundedWithin(Float.NaN, 10f, 5f, 0f));
        assertFalse(ValidationUtils.boundedWithin(Float.NaN, 10f, 15f, 0f));

        assertTrue(ValidationUtils.boundedWithin(0f, Float.NaN, 5f, 0f));
        assertFalse(ValidationUtils.boundedWithin(0f, Float.NaN, -5f, 0f));
    }

}