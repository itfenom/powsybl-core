package com.powsybl.cgmes.conversion.test.tools;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

import com.google.auto.service.AutoService;
import com.powsybl.computation.ComputationManager;
import com.powsybl.iidm.network.Network;
import com.powsybl.loadflow.LoadFlowParameters;
import com.powsybl.loadflow.LoadFlowProvider;
import com.powsybl.loadflow.LoadFlowResult;
import com.powsybl.loadflow.LoadFlowResultImpl;
import com.powsybl.loadflow.resultscompletion.LoadFlowResultsCompletion;
import com.powsybl.loadflow.resultscompletion.LoadFlowResultsCompletionParameters;
import com.powsybl.loadflow.validation.CandidateComputation;

/**
 * @author Geoffroy Jamgotchian <geoffroy.jamgotchian at rte-france.com>
 */
@AutoService(LoadFlowProvider.class)
public class LoadFlowResultCompletionMock implements LoadFlowProvider {

    @Override
    public CompletableFuture<LoadFlowResult> run(Network network, ComputationManager computationManager, String workingStateId, LoadFlowParameters lfParameters) {
        network.getBusView().getBuses().forEach(b -> b.setV(b.getV() + 10 * Math.random()));
        LoadFlowResultsCompletionParameters parameters = new LoadFlowResultsCompletionParameters();
        CandidateComputation computation = new LoadFlowResultsCompletion(parameters, lfParameters);
        computation.run(network, null);
        return CompletableFuture.completedFuture(new LoadFlowResultImpl(true, Collections.emptyMap(), ""));
    }

    @Override
    public String getName() {
        return "LoadFlowResultCompletionMock";
    }

    @Override
    public String getVersion() {
        return "1.0";
    }
}
