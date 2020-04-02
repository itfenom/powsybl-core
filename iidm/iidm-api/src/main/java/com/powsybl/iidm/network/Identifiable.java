/**
 * Copyright (c) 2016, All partners of the iTesla project (http://www.itesla-project.eu/consortium)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network;

import java.util.*;

import com.powsybl.commons.extensions.Extendable;

/**
 * An object that is part of the network model and that is identified uniquely
 * by a <code>String</code> id.
 *
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public interface Identifiable<I extends Identifiable<I>> extends Extendable<I> {

    /**
     * Get the network associated to the object.
     */
    Network getNetwork();

    /**
     * Get the unique identifier of the object.
     */
    String getId();

    /**
     * Get an the (optional) name  of the object.
     */
    String getName();

    /**
     * Check that this object has some properties.
     */
    boolean hasProperty();

    /**
     * Get properties associated to the object.
     *
     * @deprecated Use {@link #getProperty(String)} & {@link #setProperty(String, String)} instead.
     */
    @Deprecated
    default Properties getProperties() {
        throw new UnsupportedOperationException("Deprecated");
    }

    /**
     * Check that this object has property with specified name.
     */
    boolean hasProperty(String key);

    /**
     * Get property associated to specified key.
     */
    Optional<String> getProperty(String key);

    /**
     * Get property associated to specified key, with default value.
     */
    Optional<String> getProperty(String key, String defaultValue);

    /**
     * Set property value associated to specified key.
     */
    String setProperty(String key, String value);

    /**
     * Get properties key values.
     */
    Set<String> getPropertyNames();

    /**
     * Get the type of the property associated to specified key
     */
    com.powsybl.iidm.network.util.Properties.Type getPropertyType(String key);

    /**
     * Get property associated to specified key as an Integer.
     */
    OptionalInt getIntegerProperty(String key);

    OptionalInt getIntegerProperty(String key, Integer defaultValue);

    /**
     * Get property associated to specified key as a Double.
     */
    OptionalDouble getDoubleProperty(String key);

    OptionalDouble getDoubleProperty(String key, Double defaultValue);

    /**
     * Get property associated to specified key as a Boolean.
     */
    Optional<Boolean> getBooleanProperty(String key);

    Optional<Boolean> getBooleanProperty(String key, Boolean defaultValue);

    /**
     * Set Integer property value associated to specified key.
     */
    Integer setIntegerProperty(String key, Integer value);

    /**
     * Set Double property value associated to specified key.
     */
    Double setDoubleProperty(String key, Double value);

    /**
     * Set Boolean property value associated to specified key.
     */
    Boolean setBooleanProperty(String key, Boolean value);

    /**
     * Remove property associated to specified key.
     */
    Boolean removeProperty(String key);

    /**
     * Get the fictitious status
     */
    default boolean isFictitious() {
        return false;
    }

    /**
     * Set the fictitious status
     */
    default void setFictitious(boolean fictitious) {
        throw new UnsupportedOperationException();
    }
}
