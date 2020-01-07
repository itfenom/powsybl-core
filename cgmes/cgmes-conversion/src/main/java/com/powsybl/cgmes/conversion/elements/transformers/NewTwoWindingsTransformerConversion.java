/**
 * Copyright (c) 2019, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements.transformers;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.RegulatingControlMappingForTransformers.CgmesRegulatingControlPhase;
import com.powsybl.cgmes.conversion.RegulatingControlMappingForTransformers.CgmesRegulatingControlRatio;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.PhaseTapChangerAdder;
import com.powsybl.iidm.network.RatioTapChangerAdder;
import com.powsybl.iidm.network.TwoWindingsTransformer;
import com.powsybl.iidm.network.TwoWindingsTransformerAdder;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * TwoWindingsTransformer Cgmes Conversion
 * <p>
 * Cgmes conversion for transformers (two and three windings) is divided into four stages: load, interpret, convert and set.
 * <p>
 * Load <br>
 * Native CGMES data is loaded from the triple store query and is put in the CGMES model object (CgmesT2xModel).
 * <p>
 * Interpret <br>
 * CgmesT2xModel data is mapped to a more general two windings transformer model (InterpretedT2xModel)
 * according to a predefined configured alternative. It is an elemental process as the only objective is to put
 * Cgmes data in the placeholders of the general two windings transformer model.
 * All possible alternatives and the default one are defined in conversion class. See {@link Conversion} <br>
 * InterpretedT2xModel supports ratioTapChanger and phaseTapChanger at each end. Shunt admittances can be defined at both ends and
 * allows to specify the end of the structural ratio.
 * <p>
 * Convert <br>
 * Converts the interpreted model (InterpretedT2xModel) to the converted model object (ConvertedT2xModel). <br>
 * The ConvertedT2xModel only allows to define ratioTapChanger and phaseTapChanger at end1.
 * Shunt admittances and structural ratio must be also at end1. <br>
 * To do this process the following methods are used: <br>
 * moveTapChangerFrom2To1: To move a tapChanger from end2 to end1 <br>
 * combineTapChanger: To reduce two tapChangers to one <br>
 * moveRatioFrom2To1: To move structural ratio from end2 to end1 <br>
 * Finally shunt admittance of both ends is added to end1. This step is an approximation and only
 * will be possible to reproduce the exact case result if Cgmes shunts are defined at end1 or
 * are split and the LoadflowParameter splitShuntAdmittance option is selected. <br>
 * See {@link AbstractTransformerConversion}
 * <p>
 * Set <br>
 * A direct map from ConvertedT2xModel to IIDM model
 * <p>
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class NewTwoWindingsTransformerConversion extends AbstractTransformerConversion {

    public NewTwoWindingsTransformerConversion(PropertyBags ends, Context context) {
        super(CgmesNames.POWER_TRANSFORMER, ends, context);
    }

    @Override
    public boolean valid() {
        if (!super.valid()) {
            return false;
        }
        if (context.boundary().containsNode(nodeId(1))
            || context.boundary().containsNode(nodeId(2))) {
            invalid("2 windings transformer end point at boundary is not supported");
            return false;
        }
        return true;
    }

    @Override
    public void convert() {
        CgmesT2xModel cgmesT2xModel = load();
        InterpretedT2xModel interpretedT2xModel = interpret(cgmesT2xModel, context.config());
        ConvertedT2xModel convertedT2xModel = convertToIidm(interpretedT2xModel);

        setToIidm(convertedT2xModel);
    }

    private CgmesT2xModel load() {
        // ends = ps
        PropertyBag end1 = ps.get(0);
        PropertyBag end2 = ps.get(1);

        double x1 = end1.asDouble(CgmesNames.X);
        double x2 = end2.asDouble(CgmesNames.X);
        double r = end1.asDouble(CgmesNames.R) + end2.asDouble(CgmesNames.R);
        double x = x1 + x2;

        String terminal1 = end1.getId(CgmesNames.TERMINAL);
        String terminal2 = end2.getId(CgmesNames.TERMINAL);

        TapChanger ratioTapChanger1 = TapChanger.ratioTapChangerFromEnd(end1, context);
        TapChanger ratioTapChanger2 = TapChanger.ratioTapChangerFromEnd(end2, context);
        TapChanger phaseTapChanger1 = TapChanger.phaseTapChangerFromEnd(end1, x, context);
        TapChanger phaseTapChanger2 = TapChanger.phaseTapChangerFromEnd(end2, x, context);

        double ratedU1 = end1.asDouble(CgmesNames.RATEDU);
        double ratedU2 = end2.asDouble(CgmesNames.RATEDU);

        CgmesT2xModel cgmesT2xModel = new CgmesT2xModel();
        cgmesT2xModel.end1.g = end1.asDouble(CgmesNames.G, 0);
        cgmesT2xModel.end1.b = end1.asDouble(CgmesNames.B);
        cgmesT2xModel.end1.ratioTapChanger = ratioTapChanger1;
        cgmesT2xModel.end1.phaseTapChanger = phaseTapChanger1;
        cgmesT2xModel.end1.ratedU = ratedU1;
        cgmesT2xModel.end1.terminal = terminal1;

        cgmesT2xModel.end1.xIsZero = x1 == 0.0;

        cgmesT2xModel.end2.g = end2.asDouble(CgmesNames.G, 0);
        cgmesT2xModel.end2.b = end2.asDouble(CgmesNames.B);
        cgmesT2xModel.end2.ratioTapChanger = ratioTapChanger2;
        cgmesT2xModel.end2.phaseTapChanger = phaseTapChanger2;
        cgmesT2xModel.end2.ratedU = ratedU2;
        cgmesT2xModel.end2.terminal = terminal2;

        cgmesT2xModel.end2.xIsZero = x2 == 0.0;

        cgmesT2xModel.r = r;
        cgmesT2xModel.x = x;

        return cgmesT2xModel;
    }

    /**
    * Maps Cgmes ratioTapChangers, phaseTapChangers, shuntAdmittances and structural ratio
    * according to the alternative. The rest of the Cgmes data is directly mapped.
    */
    private InterpretedT2xModel interpret(CgmesT2xModel cgmesT2xModel, Conversion.Config alternative) {

        AllTapChanger interpretedTapChanger = ratioPhaseAlternative(cgmesT2xModel, alternative);
        AllShunt interpretedShunt = shuntAlternative(cgmesT2xModel, alternative);

        boolean structuralRatioAtEnd2 = structuralRatioAlternative(cgmesT2xModel, alternative);

        InterpretedT2xModel interpretedT2xModel = new InterpretedT2xModel();
        interpretedT2xModel.r = cgmesT2xModel.r;
        interpretedT2xModel.x = cgmesT2xModel.x;

        interpretedT2xModel.end1.g = interpretedShunt.g1;
        interpretedT2xModel.end1.b = interpretedShunt.b1;
        interpretedT2xModel.end1.ratioTapChanger = interpretedTapChanger.ratioTapChanger1;
        interpretedT2xModel.end1.phaseTapChanger = interpretedTapChanger.phaseTapChanger1;
        interpretedT2xModel.end1.ratedU = cgmesT2xModel.end1.ratedU;
        interpretedT2xModel.end1.terminal = cgmesT2xModel.end1.terminal;

        interpretedT2xModel.end2.g = interpretedShunt.g2;
        interpretedT2xModel.end2.b = interpretedShunt.b2;
        interpretedT2xModel.end2.ratioTapChanger = interpretedTapChanger.ratioTapChanger2;
        interpretedT2xModel.end2.phaseTapChanger = interpretedTapChanger.phaseTapChanger2;
        interpretedT2xModel.end2.ratedU = cgmesT2xModel.end2.ratedU;
        interpretedT2xModel.end2.terminal = cgmesT2xModel.end2.terminal;

        interpretedT2xModel.structuralRatioAtEnd2 = structuralRatioAtEnd2;

        return interpretedT2xModel;
    }

    /**
     * END1. All tapChangers of the Cgmes model are supposed to be at end1. The interpreted model only supports
     * one ratioTapChanger or phaseTapChanger at each end so they should be combined to only one as two are possible.
     * END2. All tapChangers of the Cgmes model are supposed to be at end2. They should be combined.
     * END1_END2. Tap changers are directly mapped at each end.
     * X. Tap changers are mapped at end1 or end2 depending on the xIsZero attribute.
     * Finally the angle sign is changed according to the alternative
     */
    private AllTapChanger ratioPhaseAlternative(CgmesT2xModel cgmesT2xModel, Conversion.Config alternative) {
        TapChanger ratioTapChanger1 = null;
        TapChanger phaseTapChanger1 = null;
        TapChanger ratioTapChanger2 = null;
        TapChanger phaseTapChanger2 = null;

        switch (alternative.getXfmr2RatioPhase()) {
            case END1:
                ratioTapChanger1 = combineTapChangers(cgmesT2xModel.end1.ratioTapChanger, cgmesT2xModel.end2.ratioTapChanger);
                phaseTapChanger1 = combineTapChangers(cgmesT2xModel.end1.phaseTapChanger, cgmesT2xModel.end2.phaseTapChanger);
                break;
            case END2:
                ratioTapChanger2 = combineTapChangers(cgmesT2xModel.end2.ratioTapChanger, cgmesT2xModel.end1.ratioTapChanger);
                phaseTapChanger2 = combineTapChangers(cgmesT2xModel.end2.phaseTapChanger, cgmesT2xModel.end1.phaseTapChanger);
                break;
            case END1_END2:
                ratioTapChanger1 = cgmesT2xModel.end1.ratioTapChanger;
                phaseTapChanger1 = cgmesT2xModel.end1.phaseTapChanger;
                ratioTapChanger2 = cgmesT2xModel.end2.ratioTapChanger;
                phaseTapChanger2 = cgmesT2xModel.end2.phaseTapChanger;
                break;
            case X:
                if (cgmesT2xModel.end1.xIsZero) {
                    ratioTapChanger1 = combineTapChangers(cgmesT2xModel.end1.ratioTapChanger, cgmesT2xModel.end2.ratioTapChanger);
                    phaseTapChanger1 = combineTapChangers(cgmesT2xModel.end1.phaseTapChanger, cgmesT2xModel.end2.phaseTapChanger);
                } else {
                    ratioTapChanger2 = combineTapChangers(cgmesT2xModel.end2.ratioTapChanger, cgmesT2xModel.end1.ratioTapChanger);
                    phaseTapChanger2 = combineTapChangers(cgmesT2xModel.end2.phaseTapChanger, cgmesT2xModel.end1.phaseTapChanger);
                }
                break;
        }

        if (alternative.isXfmr2PhaseNegate()) {
            negatePhaseTapChanger(phaseTapChanger1);
            negatePhaseTapChanger(phaseTapChanger2);
        }

        AllTapChanger allTapChanger = new AllTapChanger();
        allTapChanger.ratioTapChanger1 = ratioTapChanger1;
        allTapChanger.phaseTapChanger1 = phaseTapChanger1;
        allTapChanger.ratioTapChanger2 = ratioTapChanger2;
        allTapChanger.phaseTapChanger2 = phaseTapChanger2;

        return allTapChanger;
    }

    /**
     * Shunt admittances are mapped according to alternative options
     */
    private static AllShunt shuntAlternative(CgmesT2xModel cgmesT2xModel, Conversion.Config alternative) {
        double g1 = 0.0;
        double b1 = 0.0;
        double g2 = 0.0;
        double b2 = 0.0;
        switch (alternative.getXfmr2Shunt()) {
            case END1:
                g1 = cgmesT2xModel.end1.g + cgmesT2xModel.end2.g;
                b1 = cgmesT2xModel.end1.b + cgmesT2xModel.end2.b;
                break;
            case END2:
                g2 = cgmesT2xModel.end1.g + cgmesT2xModel.end2.g;
                b2 = cgmesT2xModel.end1.b + cgmesT2xModel.end2.b;
                break;
            case END1_END2:
                g1 = cgmesT2xModel.end1.g;
                b1 = cgmesT2xModel.end1.b;
                g2 = cgmesT2xModel.end2.g;
                b2 = cgmesT2xModel.end2.b;
                break;
            case SPLIT:
                g1 = (cgmesT2xModel.end1.g + cgmesT2xModel.end2.g) * 0.5;
                b1 = (cgmesT2xModel.end1.b + cgmesT2xModel.end2.b) * 0.5;
                g2 = (cgmesT2xModel.end1.g + cgmesT2xModel.end2.g) * 0.5;
                b2 = (cgmesT2xModel.end1.b + cgmesT2xModel.end2.b) * 0.5;
                break;
        }

        AllShunt allShunt = new AllShunt();
        allShunt.g1 = g1;
        allShunt.b1 = b1;
        allShunt.g2 = g2;
        allShunt.b2 = b2;

        return allShunt;
    }

    /**
     * return true if the structural ratio is at end2
     */
    private static boolean structuralRatioAlternative(CgmesT2xModel cgmesT2xModel, Conversion.Config alternative) {
        if (cgmesT2xModel.end1.ratedU == cgmesT2xModel.end2.ratedU) {
            return false;
        }
        switch (alternative.getXfmr2StructuralRatio()) {
            case END1:
                return false;
            case END2:
                return true;
            case X:
                return !cgmesT2xModel.end1.xIsZero;
        }
        return false;
    }

    /**
     * ratioTapChanger and phaseTapChanger of end2 are moved to end1 and then combined with the tapChangers
     * initially  defined at end1.
     * If the structural ratio is defined at end2 is moved to end1
     * The rest of attributes are directly mapped
     */

    private ConvertedT2xModel convertToIidm(InterpretedT2xModel interpretedT2xModel) {

        TapChanger nRatioTapChanger2 = moveTapChangerFrom2To1(interpretedT2xModel.end2.ratioTapChanger);
        TapChanger nPhaseTapChanger2 = moveTapChangerFrom2To1(interpretedT2xModel.end2.phaseTapChanger);

        TapChanger ratioTapChanger = combineTapChangers(interpretedT2xModel.end1.ratioTapChanger, nRatioTapChanger2);
        TapChanger phaseTapChanger = combineTapChangers(interpretedT2xModel.end1.phaseTapChanger, nPhaseTapChanger2);

        RatioConversion rc0;
        if (interpretedT2xModel.structuralRatioAtEnd2) {
            double a0 = interpretedT2xModel.end2.ratedU / interpretedT2xModel.end1.ratedU;
            rc0 = moveRatioFrom2To1(a0, 0.0, interpretedT2xModel.r, interpretedT2xModel.x,
                interpretedT2xModel.end1.g, interpretedT2xModel.end1.b,
                interpretedT2xModel.end2.g, interpretedT2xModel.end2.b);
        } else {
            rc0 = identityRatioConversion(interpretedT2xModel.r, interpretedT2xModel.x,
                interpretedT2xModel.end1.g, interpretedT2xModel.end1.b,
                interpretedT2xModel.end2.g, interpretedT2xModel.end2.b);
        }
        ConvertedT2xModel convertedModel = new ConvertedT2xModel();

        convertedModel.r = rc0.r;
        convertedModel.x = rc0.x;

        convertedModel.end1.g = rc0.g1 + rc0.g2;
        convertedModel.end1.b = rc0.b1 + rc0.b2;
        convertedModel.end1.ratioTapChanger = ratioTapChanger;
        convertedModel.end1.phaseTapChanger = phaseTapChanger;
        convertedModel.end1.ratedU = interpretedT2xModel.end1.ratedU;
        convertedModel.end1.terminal = interpretedT2xModel.end1.terminal;

        convertedModel.end2.ratedU = interpretedT2xModel.end2.ratedU;
        convertedModel.end2.terminal = interpretedT2xModel.end2.terminal;

        return convertedModel;
    }

    private void setToIidm(ConvertedT2xModel convertedT2xModel) {
        TwoWindingsTransformerAdder adder = substation().newTwoWindingsTransformer()
            .setR(convertedT2xModel.r)
            .setX(convertedT2xModel.x)
            .setG(convertedT2xModel.end1.g)
            .setB(convertedT2xModel.end1.b)
            .setRatedU1(convertedT2xModel.end1.ratedU)
            .setRatedU2(convertedT2xModel.end2.ratedU);
        identify(adder);
        connect(adder);
        TwoWindingsTransformer tx = adder.add();
        convertedTerminals(tx.getTerminal1(), tx.getTerminal2());

        setToIidmRatioTapChanger(convertedT2xModel, tx);
        setToIidmPhaseTapChanger(convertedT2xModel, tx);

        setRegulatingControlContext(convertedT2xModel, tx);
    }

    private static void setToIidmRatioTapChanger(ConvertedT2xModel convertedT2xModel, TwoWindingsTransformer tx) {
        TapChanger rtc = convertedT2xModel.end1.ratioTapChanger;
        if (rtc == null) {
            return;
        }

        RatioTapChangerAdder rtca = newRatioTapChanger(tx);
        setToIidmRatioTapChanger(rtc, rtca);
    }

    private static void setToIidmPhaseTapChanger(ConvertedT2xModel convertedT2xModel, TwoWindingsTransformer tx) {
        TapChanger ptc = convertedT2xModel.end1.phaseTapChanger;
        if (ptc == null) {
            return;
        }

        PhaseTapChangerAdder ptca = newPhaseTapChanger(tx);
        setToIidmPhaseTapChanger(ptc, ptca);
    }

    private static RatioTapChangerAdder newRatioTapChanger(TwoWindingsTransformer tx) {
        return tx.newRatioTapChanger();
    }

    private static PhaseTapChangerAdder newPhaseTapChanger(TwoWindingsTransformer tx) {
        return tx.newPhaseTapChanger();
    }

    private void setRegulatingControlContext(ConvertedT2xModel convertedT2xModel, TwoWindingsTransformer tx) {
        CgmesRegulatingControlRatio rcRtc = setContextRegulatingDataRatio(convertedT2xModel.end1.ratioTapChanger);
        CgmesRegulatingControlPhase rcPtc = setContextRegulatingDataPhase(convertedT2xModel.end1.phaseTapChanger);

        context.regulatingControlMapping().forTransformers().add(tx.getId(), rcRtc, rcPtc);
    }

    static class CgmesT2xModel {
        double r;
        double x;
        CgmesEnd end1 = new CgmesEnd();
        CgmesEnd end2 = new CgmesEnd();
    }

    static class CgmesEnd {
        double g;
        double b;
        TapChanger ratioTapChanger;
        TapChanger phaseTapChanger;
        double ratedU;
        String terminal;
        boolean xIsZero;
    }

    static class InterpretedT2xModel {
        double r;
        double x;
        InterpretedEnd end1 = new InterpretedEnd();
        InterpretedEnd end2 = new InterpretedEnd();
        boolean structuralRatioAtEnd2;
    }

    static class InterpretedEnd {
        double g;
        double b;
        TapChanger ratioTapChanger;
        TapChanger phaseTapChanger;
        double ratedU;
        String terminal;
    }

    static class ConvertedT2xModel {
        double r;
        double x;
        ConvertedEnd1 end1 = new ConvertedEnd1();
        ConvertedEnd2 end2 = new ConvertedEnd2();
    }

    static class ConvertedEnd2 {
        double ratedU;
        String terminal;
    }
}
