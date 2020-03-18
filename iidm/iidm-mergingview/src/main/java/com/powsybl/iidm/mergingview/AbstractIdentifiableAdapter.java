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
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.Set;

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
    public boolean hasProperty(final String key) {
        return getDelegate().hasProperty(key);
    }

    @Override
    public String getProperty(final String key) {
        return getDelegate().getProperty(key);
    }

    @Override
    public String getProperty(final String key, final String defaultValue) {
        return getDelegate().getProperty(key, defaultValue);
    }

    @Override
    public String setProperty(final String key, final String value) {
        return getDelegate().setProperty(key, value);
    }

    @Override
    public Set<String> getPropertyNames() {
        return getDelegate().getPropertyNames();
    }

    @Override
    public boolean hasTypedProperty(String key) {
        return getDelegate().hasTypedProperty(key);
    }

    @Override
    public Type getPropertyType(String key) {
        return getDelegate().getPropertyType(key);
    }

    @Override
    public String getStringProperty(String key) {
        return getDelegate().getStringProperty(key);
    }

    @Override
    public Integer getIntegerProperty(String key) {
        return getDelegate().getIntegerProperty(key);
    }

    @Override
    public Double getDoubleProperty(String key) {
        return getDelegate().getDoubleProperty(key);
    }

    @Override
    public Boolean getBooleanProperty(String key) {
        return getDelegate().getBooleanProperty(key);
    }

    @Override
    public Pair<Type, Object> getTypedProperty(String key) {
        return getDelegate().getTypedProperty(key);
    }

    @Override
    public String setStringProperty(String key, String value) {
        return getDelegate().setStringProperty(key, value);
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
    public Pair<Type, Object> setTypedProperty(String key, Pair<Type, Object> value) {
        return getDelegate().setTypedProperty(key, value);
    }

    @Override
    public Set<String> getTypedPropertyNames() {
        return getDelegate().getTypedPropertyNames();
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
