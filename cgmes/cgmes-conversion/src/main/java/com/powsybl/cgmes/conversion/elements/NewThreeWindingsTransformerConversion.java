/**
 * Copyright (c) 2017-2018, RTE (http://www.rte-france.com)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.powsybl.cgmes.conversion.elements;

import java.util.Map;

import com.powsybl.cgmes.conversion.Context;
import com.powsybl.cgmes.conversion.Conversion;
import com.powsybl.cgmes.conversion.RegulatingControlMappingForTransformers.CgmesRegulatingControlPhase;
import com.powsybl.cgmes.conversion.RegulatingControlMappingForTransformers.CgmesRegulatingControlRatio;
import com.powsybl.cgmes.model.CgmesNames;
import com.powsybl.iidm.network.PhaseTapChangerAdder;
import com.powsybl.iidm.network.RatioTapChangerAdder;
import com.powsybl.iidm.network.ThreeWindingsTransformer;
import com.powsybl.iidm.network.ThreeWindingsTransformerAdder;
import com.powsybl.iidm.network.ThreeWindingsTransformerAdder.LegAdder;
import com.powsybl.iidm.network.extensions.ThreeWindingsTransformerPhaseAngleClock;
import com.powsybl.triplestore.api.PropertyBag;
import com.powsybl.triplestore.api.PropertyBags;

/**
 * @author Luma Zamarreño <zamarrenolm at aia.es>
 * @author José Antonio Marqués <marquesja at aia.es>
 */
public class NewThreeWindingsTransformerConversion extends AbstractTransformerConversion {

    public NewThreeWindingsTransformerConversion(PropertyBags ends,
        Map<String, PropertyBag> powerTransformerRatioTapChanger,
        Map<String, PropertyBag> powerTransformerPhaseTapChanger, Context context) {
        super(STRING_POWER_TRANSFORMER, ends, context);
        this.powerTransformerRatioTapChanger = powerTransformerRatioTapChanger;
        this.powerTransformerPhaseTapChanger = powerTransformerPhaseTapChanger;
    }

    @Override
    public void convert() {
        CgmesT3xModel cgmesT3xModel = load();
        InterpretedT3xModel interpretedT3xModel = interpret(cgmesT3xModel, context.config());
        ConvertedT3xModel convertedT3xModel = convertToIidm(interpretedT3xModel);

        setToIidm(convertedT3xModel);
    }

    private CgmesT3xModel load() {
        CgmesT3xModel cgmesT3xModel = new CgmesT3xModel();

        // ends = ps
        loadWinding(ps.get(0), cgmesT3xModel.winding1);
        loadWinding(ps.get(1), cgmesT3xModel.winding2);
        loadWinding(ps.get(2), cgmesT3xModel.winding3);

        return cgmesT3xModel;
    }

    private void loadWinding(PropertyBag winding, CgmesWinding cgmesWinding) {
        PropertyBag rtc = getTransformerTapChanger(winding, STRING_RATIO_TAP_CHANGER,
            powerTransformerRatioTapChanger);
        PropertyBag ptc = getTransformerTapChanger(winding, STRING_PHASE_TAP_CHANGER,
            powerTransformerPhaseTapChanger);

        String terminal = winding.getId(CgmesNames.TERMINAL);
        double ratedU = winding.asDouble(STRING_RATEDU);
        double x = winding.asDouble(STRING_X);

        TapChangerConversion ratioTapChanger = getRatioTapChanger(rtc);
        TapChangerConversion phaseTapChanger = getPhaseTapChanger(ptc, x);

        cgmesWinding.r = winding.asDouble(STRING_R);
        cgmesWinding.x = x;
        cgmesWinding.g = winding.asDouble(STRING_G, 0);
        cgmesWinding.b = winding.asDouble(STRING_B);
        cgmesWinding.ratioTapChanger = ratioTapChanger;
        cgmesWinding.phaseTapChanger = phaseTapChanger;
        cgmesWinding.ratedU = ratedU;
        cgmesWinding.phaseAngleClock = winding.asInt(STRING_PHASE_ANGLE_CLOCK, 0);
        cgmesWinding.terminal = terminal;
    }

    private InterpretedT3xModel interpret(CgmesT3xModel cgmesT3xModel, Conversion.Config alternative) {

        InterpretedT3xModel interpretedT3xModel = new InterpretedT3xModel();
        double ratedUf = ratedUfAlternative(cgmesT3xModel, alternative);
        interpretedT3xModel.ratedUf = ratedUf;

        interpretWinding(cgmesT3xModel.winding1, alternative, interpretedT3xModel.winding1);
        interpretWinding(cgmesT3xModel.winding2, alternative, interpretedT3xModel.winding2);
        interpretWinding(cgmesT3xModel.winding3, alternative, interpretedT3xModel.winding3);

        return interpretedT3xModel;
    }

    private void interpretWinding(CgmesWinding cgmesWinding, Conversion.Config alternative, InterpretedWinding interpretedWinding) {

        AllTapChanger windingInterpretedTapChanger = ratioPhaseAlternative(cgmesWinding, alternative);
        AllShunt windingInterpretedShunt = shuntAlternative(cgmesWinding, alternative);
        int windingInterpretedClock = phaseAngleClockAlternative(cgmesWinding, alternative);
        boolean windingRatio0AtEnd2 = ratio0Alternative(cgmesWinding, alternative);

        interpretedWinding.r = cgmesWinding.r;
        interpretedWinding.x = cgmesWinding.x;
        interpretedWinding.end1.g = windingInterpretedShunt.g1;
        interpretedWinding.end1.b = windingInterpretedShunt.b1;
        interpretedWinding.end1.ratioTapChanger = windingInterpretedTapChanger.ratioTapChanger1;
        interpretedWinding.end1.phaseTapChanger = windingInterpretedTapChanger.phaseTapChanger1;
        interpretedWinding.end1.phaseAngleClock = windingInterpretedClock;
        interpretedWinding.end1.ratedU = cgmesWinding.ratedU;
        interpretedWinding.end1.terminal = cgmesWinding.terminal;

        interpretedWinding.end2.g = windingInterpretedShunt.g2;
        interpretedWinding.end2.b = windingInterpretedShunt.b2;
        interpretedWinding.end2.ratioTapChanger = windingInterpretedTapChanger.ratioTapChanger2;
        interpretedWinding.end2.phaseTapChanger = windingInterpretedTapChanger.phaseTapChanger2;

        interpretedWinding.ratio0AtEnd2 = windingRatio0AtEnd2;
    }

    private AllTapChanger ratioPhaseAlternative(CgmesWinding cgmesWinding, Conversion.Config alternative) {
        TapChangerConversion ratioTapChanger1 = null;
        TapChangerConversion phaseTapChanger1 = null;
        TapChangerConversion ratioTapChanger2 = null;
        TapChangerConversion phaseTapChanger2 = null;

        if (alternative.isXfmr3RatioPhaseNetworkSide()) {
            ratioTapChanger1 = cgmesWinding.ratioTapChanger;
            phaseTapChanger1 = cgmesWinding.phaseTapChanger;
        } else {
            ratioTapChanger2 = cgmesWinding.ratioTapChanger;
            phaseTapChanger2 = cgmesWinding.phaseTapChanger;
        }

        AllTapChanger allTapChanger = new AllTapChanger();
        allTapChanger.ratioTapChanger1 = ratioTapChanger1;
        allTapChanger.phaseTapChanger1 = phaseTapChanger1;
        allTapChanger.ratioTapChanger2 = ratioTapChanger2;
        allTapChanger.phaseTapChanger2 = phaseTapChanger2;

        return allTapChanger;
    }

    private AllShunt shuntAlternative(CgmesWinding cgmesWinding, Conversion.Config alternative) {
        double g1 = 0.0;
        double b1 = 0.0;
        double g2 = 0.0;
        double b2 = 0.0;
        if (alternative.isXfmr3ShuntNetworkSide()) {
            g1 = cgmesWinding.g;
            b1 = cgmesWinding.b;
        } else if (alternative.isXfmr3ShuntStarBusSide()) {
            g2 = cgmesWinding.g;
            b2 = cgmesWinding.b;
        } else {
            g1 = cgmesWinding.g * 0.5;
            b1 = cgmesWinding.b * 0.5;
            g2 = cgmesWinding.g * 0.5;
            b2 = cgmesWinding.b * 0.5;
        }

        AllShunt allShunt = new AllShunt();
        allShunt.g1 = g1;
        allShunt.b1 = b1;
        allShunt.g2 = g2;
        allShunt.b2 = b2;

        return allShunt;
    }

    private int phaseAngleClockAlternative(CgmesWinding cgmesWinding, Conversion.Config alternative) {
        int phaseAngleClock = 0;

        if (alternative.isXfmr3PhaseAngleClockOn()) {
            phaseAngleClock = cgmesWinding.phaseAngleClock;
        }

        return phaseAngleClock;
    }

    private boolean ratio0Alternative(CgmesWinding cgmesWinding, Conversion.Config alternative) {
        boolean ratio0AtEnd2;
        if (alternative.isXfmr3Ratio0StarBusSide()) {
            ratio0AtEnd2 = true;
        } else {
            ratio0AtEnd2 = false;
        }
        return ratio0AtEnd2;
    }

    private double ratedUfAlternative(CgmesT3xModel cgmesT3xModel, Conversion.Config alternative) {

        double ratedUf = 1.0;
        if (alternative.isXfmr3Ratio0StarBusSide()) {
            ratedUf = selectRatedUf(cgmesT3xModel);
        } else if (alternative.isXfmr3Ratio0NetworkSide()) {
            ratedUf = 1.0;
        } else if (alternative.isXfmr3Ratio0End1()) {
            ratedUf = cgmesT3xModel.winding1.ratedU;
        } else if (alternative.isXfmr3Ratio0End2()) {
            ratedUf = cgmesT3xModel.winding2.ratedU;
        } else if (alternative.isXfmr3Ratio0End3()) {
            ratedUf = cgmesT3xModel.winding3.ratedU;
        }
        return ratedUf;
    }

    // Select the ratedUf voltage
    private double selectRatedUf(CgmesT3xModel cgmesT3xModel) {
        return cgmesT3xModel.winding1.ratedU;
    }

    private ConvertedT3xModel convertToIidm(InterpretedT3xModel interpretedT3xModel) {

        double ratedUf = interpretedT3xModel.ratedUf;
        ConvertedT3xModel convertedModel = new ConvertedT3xModel();

        convertToIidmWinding(interpretedT3xModel.winding1, convertedModel.winding1, ratedUf);
        convertToIidmWinding(interpretedT3xModel.winding2, convertedModel.winding2, ratedUf);
        convertToIidmWinding(interpretedT3xModel.winding3, convertedModel.winding3, ratedUf);

        convertedModel.ratedUf = ratedUf;

        return convertedModel;
    }

    private void convertToIidmWinding(InterpretedWinding interpretedWinding, ConvertedWinding convertedWinding, double ratedUf) {
        TapChangerWinding windingTapChanger = moveCombineTapChangerWinding(interpretedWinding);

        RatioConversion windingRc0 = rc0Winding(interpretedWinding, ratedUf);

        convertedWinding.r = windingRc0.r;
        convertedWinding.x = windingRc0.x;
        convertedWinding.end1.g = windingRc0.g1;
        convertedWinding.end1.b = windingRc0.b1;
        convertedWinding.end1.ratioTapChanger = windingTapChanger.ratioTapChanger;
        convertedWinding.end1.phaseTapChanger = windingTapChanger.phaseTapChanger;
        convertedWinding.end1.phaseAngleClock = interpretedWinding.end1.phaseAngleClock;
        convertedWinding.end1.ratedU = interpretedWinding.end1.ratedU;
        convertedWinding.end1.terminal = interpretedWinding.end1.terminal;
        convertedWinding.end2.g = windingRc0.g2;
        convertedWinding.end2.b = windingRc0.b2;
    }

    private RatioConversion rc0Winding(InterpretedWinding interpretedWinding, double ratedUf) {
        RatioConversion rc0;
        // IIDM: Structural ratio always at network side
        if (interpretedWinding.ratio0AtEnd2) {
            double a0 = ratedUf / interpretedWinding.end1.ratedU;
            rc0 = moveRatioFrom2To1(a0, 0.0, interpretedWinding.r, interpretedWinding.x,
                interpretedWinding.end1.g, interpretedWinding.end1.b,
                interpretedWinding.end2.g, interpretedWinding.end2.b);
        } else {
            rc0 = identityRatioConversion(interpretedWinding.r, interpretedWinding.x,
                interpretedWinding.end1.g, interpretedWinding.end1.b,
                interpretedWinding.end2.g, interpretedWinding.end2.b);
        }

        return rc0;
    }

    private TapChangerWinding moveCombineTapChangerWinding(InterpretedWinding interpretedWinding) {

        TapChangerConversion nRatioTapChanger = moveTapChangerFrom2To1(interpretedWinding.end2.ratioTapChanger);
        TapChangerConversion nPhaseTapChanger = moveTapChangerFrom2To1(interpretedWinding.end2.phaseTapChanger);

        TapChangerConversion cRatioTapChanger = combineTapChangers(interpretedWinding.end1.ratioTapChanger, nRatioTapChanger);
        TapChangerConversion cPhaseTapChanger = combineTapChangers(interpretedWinding.end1.phaseTapChanger, nPhaseTapChanger);

        TapChangerWinding tapChangerWinding = new TapChangerWinding();
        tapChangerWinding.ratioTapChanger = cRatioTapChanger;
        tapChangerWinding.phaseTapChanger = cPhaseTapChanger;
        return tapChangerWinding;
    }

    private void setToIidm(ConvertedT3xModel convertedT3xModel) {
        ThreeWindingsTransformerAdder txadder = substation().newThreeWindingsTransformer()
            .setRatedU0(convertedT3xModel.ratedUf);
        identify(txadder);

        LegAdder l1adder = txadder.newLeg1();
        setToIidmWindingAdder(convertedT3xModel.winding1, l1adder);
        connect(l1adder, 1);
        l1adder.add();

        LegAdder l2adder = txadder.newLeg2();
        setToIidmWindingAdder(convertedT3xModel.winding2, l2adder);
        connect(l2adder, 2);
        l2adder.add();

        LegAdder l3adder = txadder.newLeg3();
        setToIidmWindingAdder(convertedT3xModel.winding3, l3adder);
        connect(l3adder, 3);
        l3adder.add();

        ThreeWindingsTransformer tx = txadder.add();

        convertedTerminals(
            tx.getLeg1().getTerminal(),
            tx.getLeg2().getTerminal(),
            tx.getLeg3().getTerminal());

        setToIidmWindingTapChanger(convertedT3xModel, convertedT3xModel.winding1, tx);
        setToIidmWindingTapChanger(convertedT3xModel, convertedT3xModel.winding2, tx);
        setToIidmWindingTapChanger(convertedT3xModel, convertedT3xModel.winding3, tx);

        setToIidmPhaseAngleClock(convertedT3xModel, tx);
        setRegulatingControlContext(convertedT3xModel, tx);
    }

    private void setToIidmWindingAdder(ConvertedWinding convertedModelWinding, LegAdder ladder) {
        ladder.setR(convertedModelWinding.r)
            .setX(convertedModelWinding.x)
            .setG(convertedModelWinding.end1.g + convertedModelWinding.end2.g)
            .setB(convertedModelWinding.end1.b + convertedModelWinding.end2.b)
            .setRatedU(convertedModelWinding.end1.ratedU);
    }

    private void setToIidmWindingTapChanger(ConvertedT3xModel convertedT3xModel, ConvertedWinding convertedModelWinding,
        ThreeWindingsTransformer tx) {
        setToIidmRatioTapChanger(convertedT3xModel, convertedModelWinding, tx);
        setToIidmPhaseTapChanger(convertedT3xModel, convertedModelWinding, tx);
    }

    private void setToIidmRatioTapChanger(ConvertedT3xModel convertedT3xModel, ConvertedWinding convertedWinding, ThreeWindingsTransformer tx) {
        TapChangerConversion rtc = convertedWinding.end1.ratioTapChanger;
        if (rtc == null) {
            return;
        }

        RatioTapChangerAdder rtca = newRatioTapChanger(convertedT3xModel, tx, convertedWinding.end1.terminal);
        setToIidmRatioTapChanger(rtc, rtca);
    }

    private void setToIidmPhaseTapChanger(ConvertedT3xModel convertedT3xModel, ConvertedWinding convertedWinding, ThreeWindingsTransformer tx) {
        TapChangerConversion ptc = convertedWinding.end1.phaseTapChanger;
        if (ptc == null) {
            return;
        }

        PhaseTapChangerAdder ptca = newPhaseTapChanger(convertedT3xModel, tx, convertedWinding.end1.terminal);
        setToIidmPhaseTapChanger(ptc, ptca);
    }

    protected RatioTapChangerAdder newRatioTapChanger(ConvertedT3xModel convertedT3xModel, ThreeWindingsTransformer tx,
        String terminal) {
        if (convertedT3xModel.winding1.end1.terminal.equals(terminal)) {
            return tx.getLeg1().newRatioTapChanger();
        } else if (convertedT3xModel.winding2.end1.terminal.equals(terminal)) {
            return tx.getLeg2().newRatioTapChanger();
        } else if (convertedT3xModel.winding3.end1.terminal.equals(terminal)) {
            return tx.getLeg3().newRatioTapChanger();
        }
        return null;
    }

    protected PhaseTapChangerAdder newPhaseTapChanger(ConvertedT3xModel convertedT3xModel, ThreeWindingsTransformer tx,
        String terminal) {
        if (convertedT3xModel.winding1.end1.terminal.equals(terminal)) {
            return tx.getLeg1().newPhaseTapChanger();
        } else if (convertedT3xModel.winding2.end1.terminal.equals(terminal)) {
            return tx.getLeg2().newPhaseTapChanger();
        } else if (convertedT3xModel.winding3.end1.terminal.equals(terminal)) {
            return tx.getLeg3().newPhaseTapChanger();
        }
        return null;
    }

    private void setToIidmPhaseAngleClock(ConvertedT3xModel convertedT3xModel, ThreeWindingsTransformer tx) {
        // add phaseAngleClock as an extension, cgmes does not allow pac at end1
        if (convertedT3xModel.winding1.end1.phaseAngleClock != 0) {
            String reason = "Unsupported modelling: threeWindingsTransformer with phaseAngleClock at end1";
            ignored("phaseAngleClock end1", reason);
        }

        if (convertedT3xModel.winding2.end1.phaseAngleClock != 0 || convertedT3xModel.winding3.end1.phaseAngleClock != 0) {
            ThreeWindingsTransformerPhaseAngleClock phaseAngleClock = new ThreeWindingsTransformerPhaseAngleClock(tx,
                convertedT3xModel.winding2.end1.phaseAngleClock, convertedT3xModel.winding3.end1.phaseAngleClock);
            tx.addExtension(ThreeWindingsTransformerPhaseAngleClock.class, phaseAngleClock);
        }
    }

    private void setRegulatingControlContext(ConvertedT3xModel convertedT3xModel, ThreeWindingsTransformer tx) {
        CgmesRegulatingControlRatio rcRtc1 = setContextRegulatingDataRatio(convertedT3xModel.winding1.end1.ratioTapChanger);
        CgmesRegulatingControlPhase rcPtc1 = setContextRegulatingDataPhase(convertedT3xModel.winding1.end1.phaseTapChanger);
        CgmesRegulatingControlRatio rcRtc2 = setContextRegulatingDataRatio(convertedT3xModel.winding2.end1.ratioTapChanger);
        CgmesRegulatingControlPhase rcPtc2 = setContextRegulatingDataPhase(convertedT3xModel.winding2.end1.phaseTapChanger);
        CgmesRegulatingControlRatio rcRtc3 = setContextRegulatingDataRatio(convertedT3xModel.winding3.end1.ratioTapChanger);
        CgmesRegulatingControlPhase rcPtc3 = setContextRegulatingDataPhase(convertedT3xModel.winding3.end1.phaseTapChanger);

        context.regulatingControlMapping().forTransformers().add(tx.getId(), rcRtc1, rcPtc1, rcRtc2, rcPtc2, rcRtc3, rcPtc3);
    }

    static class CgmesT3xModel {
        CgmesWinding winding1 = new CgmesWinding();
        CgmesWinding winding2 = new CgmesWinding();
        CgmesWinding winding3 = new CgmesWinding();
    }

    static class CgmesWinding {
        double r;
        double x;
        double g;
        double b;
        TapChangerConversion ratioTapChanger;
        TapChangerConversion phaseTapChanger;
        double ratedU;
        int phaseAngleClock;
        String terminal;
    }

    static class InterpretedT3xModel {
        InterpretedWinding winding1 = new InterpretedWinding();
        InterpretedWinding winding2 = new InterpretedWinding();
        InterpretedWinding winding3 = new InterpretedWinding();
        double ratedUf;
    }

    // 1 network side, 2 start bus side
    static class InterpretedWinding {
        double r;
        double x;
        InterpretedEnd1 end1 = new InterpretedEnd1();
        InterpretedEnd2 end2 = new InterpretedEnd2();
        boolean ratio0AtEnd2;
    }

    static class InterpretedEnd1 {
        double g;
        double b;
        TapChangerConversion ratioTapChanger;
        TapChangerConversion phaseTapChanger;
        double ratedU;
        String terminal;
        int phaseAngleClock;
    }

    static class InterpretedEnd2 {
        double g;
        double b;
        TapChangerConversion ratioTapChanger;
        TapChangerConversion phaseTapChanger;
    }

    static class ConvertedT3xModel {
        ConvertedWinding winding1 = new ConvertedWinding();
        ConvertedWinding winding2 = new ConvertedWinding();
        ConvertedWinding winding3 = new ConvertedWinding();
        double ratedUf;
    }

    // 1 network side, 2 start bus side
    static class ConvertedWinding {
        double r;
        double x;
        ConvertedEnd1 end1 = new ConvertedEnd1();
        ConvertedEnd2 end2 = new ConvertedEnd2();
    }

    static class ConvertedEnd2 {
        double g;
        double b;
    }

    static class TapChangerWinding {
        TapChangerConversion ratioTapChanger;
        TapChangerConversion phaseTapChanger;
    }

    private final Map<String, PropertyBag> powerTransformerRatioTapChanger;
    private final Map<String, PropertyBag> powerTransformerPhaseTapChanger;
}