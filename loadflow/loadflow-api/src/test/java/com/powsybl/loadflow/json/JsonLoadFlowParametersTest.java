package com.powsybl.loadflow.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.auto.service.AutoService;
import com.powsybl.commons.AbstractConverterTest;
import com.powsybl.commons.PowsyblException;
import com.powsybl.commons.extensions.AbstractExtension;
import com.powsybl.loadflow.LoadFlowParameters;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;

import static com.powsybl.loadflow.LoadFlowParameters.VoltageInitMode.PREVIOUS_VALUES;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Sylvain Leclerc <sylvain.leclerc at rte-france.com>
 */
public class JsonLoadFlowParametersTest extends AbstractConverterTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void roundTrip() throws IOException {
        LoadFlowParameters parameters = new LoadFlowParameters()
                .setVoltageInitMode(PREVIOUS_VALUES)
                .setNoGeneratorReactiveLimits(true)
                .setTransformerVoltageControlOn(true);
        roundTripTest(parameters, JsonLoadFlowParameters::write, JsonLoadFlowParameters::read, "/LoadFlowParameters.json");
    }

    @Test
    public void writeExtension() throws IOException {
        LoadFlowParameters parameters = new LoadFlowParameters();
        parameters.addExtension(DummyExtension.class, new DummyExtension());
        writeTest(parameters, JsonLoadFlowParameters::write, AbstractConverterTest::compareTxt, "/LoadFlowParametersWithExtension.json");
    }

    @Test
    public void readExtension() throws IOException {
        LoadFlowParameters parameters = JsonLoadFlowParameters.read(getClass().getResourceAsStream("/LoadFlowParametersWithExtension.json"));
        assertEquals(1, parameters.getExtensions().size());
        assertNotNull(parameters.getExtension(DummyExtension.class));
        assertNotNull(parameters.getExtensionByName("dummy-extension"));
    }

    @Test
    public void readError() throws IOException {
        try {
            JsonLoadFlowParameters.read(getClass().getResourceAsStream("/LoadFlowParametersError.json"));
            Assert.fail();
        } catch (AssertionError ignored) {
        }
    }

    @Test
    public void readJsonVersion10() throws IOException {
        LoadFlowParameters parameters = JsonLoadFlowParameters
            .read(getClass().getResourceAsStream("/LoadFlowParametersVersion10.json"));
        assertEquals(true, parameters.isT2wtSplitShuntAdmittance());
    }

    @Test
    public void readJsonVersion11() throws IOException {
        LoadFlowParameters parameters = JsonLoadFlowParameters
            .read(getClass().getResourceAsStream("/LoadFlowParametersVersion11.json"));
        assertEquals(true, parameters.isT2wtSplitShuntAdmittance());
    }

    @Test
    public void readJsonVersion10Exception() throws IOException {
        exception.expect(PowsyblException.class);
        exception.expectMessage("LoadFlowParameters. Tag: t2wtSplitShuntAdmittance is not valid for version 1.0. Version should be > 1.0");
        JsonLoadFlowParameters.read(getClass().getResourceAsStream("/LoadFlowParametersVersion10Exception.json"));
    }

    @Test
    public void readJsonVersion11Exception() throws IOException {
        exception.expect(PowsyblException.class);
        exception.expectMessage("LoadFlowParameters. Tag: specificCompatibility is not valid for version 1.1. Version should be <= 1.0");
        JsonLoadFlowParameters.read(getClass().getResourceAsStream("/LoadFlowParametersVersion11Exception.json"));
    }

    static class DummyExtension extends AbstractExtension<LoadFlowParameters> {

        DummyExtension() {
            super();
        }

        /**
         * Return the name of this extension.
         */
        @Override
        public String getName() {
            return "dummy-extension";
        }
    }

    @AutoService(JsonLoadFlowParameters.ExtensionSerializer.class)
    public static class DummySerializer implements JsonLoadFlowParameters.ExtensionSerializer<DummyExtension> {

        @Override
        public void serialize(DummyExtension extension, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            jsonGenerator.writeStartObject();
            jsonGenerator.writeEndObject();
        }

        @Override
        public DummyExtension deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
            return new DummyExtension();
        }

        @Override
        public String getExtensionName() {
            return "dummy-extension";
        }

        @Override
        public String getCategoryName() {
            return "loadflow-parameters";
        }

        @Override
        public Class<? super DummyExtension> getExtensionClass() {
            return DummyExtension.class;
        }
    }

}
