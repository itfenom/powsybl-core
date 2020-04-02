/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.mergingview;

import com.powsybl.commons.extensions.Extension;
import com.powsybl.commons.extensions.ExtensionAdder;
import com.powsybl.iidm.network.Identifiable;
import com.powsybl.iidm.network.util.Properties;

import java.util.*;

/**
 * @author Thomas Adam <tadam at silicom.fr>
 */
abstract class AbstractIdentifiableAdapter<I extends Identifiable<I>> extends AbstractAdapter<I> implements Identifiable<I> {

    protected AbstractIdentifiableAdapter(final I delegate, final MergingViewIndex index) {
        super(delegate, index);
    }

    @Override
    public MergingView getNetwork() {
        return getIndex().getView();
    }

    // -------------------------------
    // Simple delegated methods ------
    // -------------------------------
    @Override
    public String getId() {
        return getDelegate().getId();
    }

    @Override
    public String getName() {
        return getDelegate().getName();
    }

    @Override
    public boolean hasProperty() {
        return getDelegate().hasProperty();
    }

    @Override
    public boolean hasProperty(String key) {
        return getDelegate().hasProperty(key);
    }

    @Override
    public Properties.Type getPropertyType(String key) {
        return getDelegate().getPropertyType(key);
    }

    @Override
    public Optional<String> getProperty(String key) {
        return getDelegate().getProperty(key);
    }

    @Override
    public Optional<String> getProperty(String key, String defaultValue) {
        return getDelegate().getProperty(key, defaultValue);
    }

    @Override
    public OptionalInt getIntegerProperty(String key) {
        return getDelegate().getIntegerProperty(key);
    }

    @Override
    public OptionalInt getIntegerProperty(String key, Integer defaultValue) {
        return getDelegate().getIntegerProperty(key, defaultValue);
    }

    @Override
    public OptionalDouble getDoubleProperty(String key) {
        return getDelegate().getDoubleProperty(key);
    }

    @Override
    public OptionalDouble getDoubleProperty(String key, Double defaultValue) {
        return getDelegate().getDoubleProperty(key, defaultValue);
    }

    @Override
    public Optional<Boolean> getBooleanProperty(String key) {
        return getDelegate().getBooleanProperty(key);
    }

    @Override
    public Optional<Boolean> getBooleanProperty(String key, Boolean defaultValue) {
        return getDelegate().getBooleanProperty(key, defaultValue);
    }

    @Override
    public String setProperty(String key, String value) {
        return getDelegate().setProperty(key, value);
    }

    @Override
    public Integer setIntegerProperty(String key, Integer value) {
        return getDelegate().setIntegerProperty(key, value);
    }

    @Override
    public Double setDoubleProperty(String key, Double value) {
        return getDelegate().setDoubleProperty(key, value);
    }

    @Override
    public Boolean setBooleanProperty(String key, Boolean value) {
        return getDelegate().setBooleanProperty(key, value);
    }

    @Override
    public Set<String> getPropertyNames() {
        return getDelegate().getPropertyNames();
    }

    @Override
    public Boolean removeProperty(String key) {
        return getDelegate().removeProperty(key);
    }

    @Override
    public boolean isFictitious() {
        return getDelegate().isFictitious();
    }

    @Override
    public void setFictitious(boolean fictitious) {
        getDelegate().setFictitious(fictitious);
    }

    @Override
    public <E extends Extension<I>> void addExtension(final Class<? super E> type, final E extension) {
        getDelegate().addExtension(type, extension);
    }

    @Override
    public <E extends Extension<I>> E getExtension(final Class<? super E> type) {
        return getDelegate().getExtension(type);
    }

    @Override
    public <E extends Extension<I>> E getExtensionByName(final String name) {
        return getDelegate().getExtensionByName(name);
    }

    @Override
    public <E extends Extension<I>> boolean removeExtension(final Class<E> type) {
        return getDelegate().removeExtension(type);
    }

    @Override
    public <E extends Extension<I>> Collection<E> getExtensions() {
        return getDelegate().getExtensions();
    }

    @Override
    public <E extends Extension<I>, B extends ExtensionAdder<I, E>> B newExtension(Class<B> type) {
        return getDelegate().newExtension(type);
    }
}
