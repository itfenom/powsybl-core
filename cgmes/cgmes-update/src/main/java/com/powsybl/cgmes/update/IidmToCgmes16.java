package com.powsybl.cgmes.update;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.powsybl.cgmes.model.CgmesModel;
import com.powsybl.cgmes.update.elements16.*;

public class IidmToCgmes16 extends AbstractIidmToCgmes {

    public IidmToCgmes16(IidmChange change, CgmesModel cgmes) {
        super(change, cgmes);
    }

    public IidmToCgmes16(IidmChange change) {
        super(change);
    }

    @Override
    protected Map<String, CgmesPredicateDetails> switcher() {
        LOG.info("IIDM instance is: " + getIidmInstanceName());
        switch (getIidmInstanceName()) {
            case SUBSTATION_IMPL:
                SubstationToSubstation sb = new SubstationToSubstation(change);
                mapIidmToCgmesPredicates = sb.mapIidmToCgmesPredicates();
                break;
            case BUSBREAKER_VOLTAGELEVEL:
                VoltageLevelToVoltageLevel vl = new VoltageLevelToVoltageLevel(change, cgmes);
                mapIidmToCgmesPredicates = vl.mapIidmToCgmesPredicates();
                break;
            case CONFIGUREDBUS_IMPL:
                BusToTopologicalNode btn = new BusToTopologicalNode(change, cgmes);
                mapIidmToCgmesPredicates = btn.mapIidmToCgmesPredicates();
                break;
            case TWOWINDINGS_TRANSFORMER_IMPL:
                TwoWindingsTransformerToPowerTransformer twpt = new TwoWindingsTransformerToPowerTransformer(change,
                    cgmes);
                mapIidmToCgmesPredicates = twpt.mapIidmToCgmesPredicates();
                break;
            case GENERATOR_IMPL:
                GeneratorToSynchronousMachine gsm = new GeneratorToSynchronousMachine(change, cgmes);
                mapIidmToCgmesPredicates = gsm.mapIidmToCgmesPredicates();
                break;
            case LOAD_IMPL:
                LoadToEnergyConsumer lec = new LoadToEnergyConsumer(change);
                mapIidmToCgmesPredicates = lec.mapIidmToCgmesPredicates();
                break;
            case LCCCONVERTER_STATION_IMPL:
                LccConverterStationToAcdcConverter lcc = new LccConverterStationToAcdcConverter(change);
                mapIidmToCgmesPredicates = lcc.mapIidmToCgmesPredicates();
                break;
            case LINE_IMPL:
                LineToACLineSegment lac = new LineToACLineSegment(change);
                mapIidmToCgmesPredicates = lac.mapIidmToCgmesPredicates();
                break;
            case SHUNTCOMPENSATOR_IMPL:
                ShuntCompensatorToShuntCompensator sc = new ShuntCompensatorToShuntCompensator(change);
                mapIidmToCgmesPredicates = sc.mapIidmToCgmesPredicates();
                break;
            default:
                LOG.info("This element is not convertable to CGMES");
        }
        return mapIidmToCgmesPredicates;
    }

    private static final Logger LOG = LoggerFactory.getLogger(IidmToCgmes16.class);
}
