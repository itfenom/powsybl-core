/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package com.powsybl.security.execution;

import com.google.auto.service.AutoService;
import com.powsybl.computation.ComputationManager;
import com.powsybl.computation.Partition;
import com.powsybl.contingency.ContingenciesProvider;
import com.powsybl.contingency.Contingency;
import com.powsybl.iidm.network.Network;
import com.powsybl.security.*;
import com.powsybl.security.distributed.DistributedSecurityAnalysisExecution;
import com.powsybl.security.distributed.ExternalSecurityAnalysisConfig;
import com.powsybl.security.distributed.ForwardedSecurityAnalysisExecution;
import com.powsybl.security.interceptors.SecurityAnalysisInterceptor;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class SecurityAnalysisExecutionBuilderTest {

    private SecurityAnalysis analysis;
    private static AtomicReference<ContingenciesProvider> actualProvider;
    private SecurityAnalysisExecutionBuilder builder;

    @Before
    public void setUp() {

        Contingency contingency = new Contingency("cont");
        ContingenciesProvider provider = network -> Collections.nCopies(10, contingency);

        actualProvider = new AtomicReference<>();

        builder = new SecurityAnalysisExecutionBuilder(ExternalSecurityAnalysisConfig::new,
                "BuilderTestProvider",
            execInput -> new SecurityAnalysisInput(Mockito.mock(NetworkVariant.class))
                        .setContingencies(provider));
    }

    @Test
    public void checkLocal() {
        SecurityAnalysisExecution execution = builder.build();
        assertTrue(execution instanceof SecurityAnalysisExecutionImpl);

        execution.execute(Mockito.mock(ComputationManager.class), new SecurityAnalysisExecutionInput());

        assertNotNull(actualProvider.get());
        assertEquals(10, actualProvider.get().getContingencies(null).size());
    }

    @Test
    public void checkForwarded() {
        builder.forward(true);
        assertTrue(builder.build() instanceof ForwardedSecurityAnalysisExecution);
    }

    @Test
    public void checkDistributedForwarded() {
        builder.forward(true)
                .distributed(12);
        assertTrue(builder.build() instanceof ForwardedSecurityAnalysisExecution);
    }

    @Test
    public void checkDistributed() {
        builder.distributed(12);
        assertTrue(builder.build() instanceof DistributedSecurityAnalysisExecution);
    }

    @Test
    public void checkSubtaskHasOnly5Contingencies() {
        SecurityAnalysisExecution execution = builder.subTask(new Partition(1, 2)).build();
        assertTrue(execution instanceof SecurityAnalysisExecutionImpl);

        execution.execute(Mockito.mock(ComputationManager.class), new SecurityAnalysisExecutionInput());

        assertNotNull(actualProvider.get());
        assertEquals(5, actualProvider.get().getContingencies(null).size());
    }

    @AutoService(SecurityAnalysisProvider.class)
    public static class BuilderTestProvider implements SecurityAnalysisProvider {

        @Override
        public CompletableFuture<SecurityAnalysisResult> run(Network network, LimitViolationDetector detector, LimitViolationFilter filter,
                                                             ComputationManager computationManager, String workingVariantId, SecurityAnalysisParameters parameters, ContingenciesProvider contingenciesProvider, List<SecurityAnalysisInterceptor> interceptors) {
            actualProvider.set(contingenciesProvider);
            return null;
        }

        @Override
        public String getName() {
            return "BuilderTestProvider";
        }

        @Override
        public String getVersion() {
            return null;
        }
    }

}
