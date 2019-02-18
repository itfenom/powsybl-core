/**
 * Copyright (c) 2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.iidm.network.util;

import com.google.common.collect.Iterables;
import com.powsybl.commons.extensions.Extension;
import com.powsybl.iidm.network.*;

import java.util.*;
import java.util.stream.Stream;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
public final class ImmutableSubstation extends AbstractImmutableIdentifiable<Substation> implements Substation {

    private static final Map<Substation, ImmutableSubstation> CACHE = new HashMap<>();

    private ImmutableSubstation(Substation identifiable) {
        super(identifiable);
    }

    static ImmutableSubstation ofNullable(Substation identifiable) {
        return identifiable == null ? null : CACHE.computeIfAbsent(identifiable, k -> new ImmutableSubstation(identifiable));
    }

    @Override
    public Network getNetwork() {
        return ImmutableNetwork.of(identifiable.getNetwork());
    }

    @Override
    public Country getCountry() {
        return identifiable.getCountry();
    }

    @Override
    public Substation setCountry(Country country) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public String getTso() {
        return identifiable.getTso();
    }

    @Override
    public Substation setTso(String tso) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public VoltageLevelAdder newVoltageLevel() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public Iterable<VoltageLevel> getVoltageLevels() {
        return Iterables.transform(identifiable.getVoltageLevels(), ImmutableVoltageLevel::ofNullable);
    }

    @Override
    public Stream<VoltageLevel> getVoltageLevelStream() {
        return identifiable.getVoltageLevelStream().map(ImmutableVoltageLevel::ofNullable);
    }

    @Override
    public TwoWindingsTransformerAdder newTwoWindingsTransformer() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public Iterable<TwoWindingsTransformer> getTwoWindingsTransformers() {
        return Iterables.transform(identifiable.getTwoWindingsTransformers(), ImmutableTwoWindingsTransformer::ofNullable);
    }

    @Override
    public Stream<TwoWindingsTransformer> getTwoWindingsTransformerStream() {
        return identifiable.getTwoWindingsTransformerStream().map(ImmutableTwoWindingsTransformer::ofNullable);
    }

    @Override
    public int getTwoWindingsTransformerCount() {
        return identifiable.getTwoWindingsTransformerCount();
    }

    @Override
    public ThreeWindingsTransformerAdder newThreeWindingsTransformer() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public Iterable<ThreeWindingsTransformer> getThreeWindingsTransformers() {
        return Iterables.transform(identifiable.getThreeWindingsTransformers(), ImmutableThreeWindingsTransformer::ofNullable);
    }

    @Override
    public Stream<ThreeWindingsTransformer> getThreeWindingsTransformerStream() {
        return identifiable.getThreeWindingsTransformerStream().map(ImmutableThreeWindingsTransformer::ofNullable);
    }

    @Override
    public int getThreeWindingsTransformerCount() {
        return identifiable.getThreeWindingsTransformerCount();
    }

    @Override
    public Set<String> getGeographicalTags() {
        return identifiable.getGeographicalTags();
    }

    @Override
    public Substation addGeographicalTag(String tag) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public ContainerType getContainerType() {
        return identifiable.getContainerType();
    }

    @Override
    public String getId() {
        return identifiable.getId();
    }

    @Override
    public String getName() {
        return identifiable.getName();
    }

    @Override
    public void remove() {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public boolean hasProperty() {
        return identifiable.hasProperty();
    }

    @Override
    public Properties getProperties() {
        return identifiable.getProperties();
    }

    @Override
    public <E extends Extension<Substation>> void addExtension(Class<? super E> type, E extension) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public <E extends Extension<Substation>> E getExtensionByName(String name) {
        return identifiable.getExtensionByName(name);
    }

    @Override
    public <E extends Extension<Substation>> boolean removeExtension(Class<E> type) {
        throw ImmutableNetwork.createUnmodifiableNetworkException();
    }

    @Override
    public <E extends Extension<Substation>> Collection<E> getExtensions() {
        return identifiable.getExtensions();
    }
}