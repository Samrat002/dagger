package io.odpf.dagger.core.config.commandline;

import io.odpf.dagger.core.config.CommandlineConfigurationProvider;
import org.apache.flink.configuration.Configuration;
import org.junit.Test;

import java.util.Base64;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CommandlineConfigurationProviderTest {

    @Test
    public void shouldProvideFromEmptyInput() throws Exception {
        Configuration configurations = new CommandlineConfigurationProvider(new String[]{}).get();

        assertTrue(configurations.keySet().isEmpty());
    }

    @Test
    public void shouldProvideFromOneValidInput() throws Exception {
        Configuration configurations = new CommandlineConfigurationProvider(new String[]{"--key", "value"}).get();

        assertEquals(1, configurations.keySet().size());

        assertTrue(configurations.containsKey("key"));
        assertEquals("value", configurations.getString("key", ""));
    }

    @Test
    public void shouldProvideFromMultipleValidInputs() throws Exception {
        Configuration configurations = new CommandlineConfigurationProvider(new String[]{"--key", "value", "--k", "v"}).get();

        assertEquals(2, configurations.keySet().size());

        assertTrue(configurations.containsKey("key"));
        assertEquals("value", configurations.getString("key", ""));
        assertTrue(configurations.containsKey("k"));
        assertEquals("v", configurations.getString("k", ""));
    }

    @Test
    public void shouldUseEncodedArgsIfProvided() {
        String args = Base64.getEncoder().encodeToString("[\"--key\", \"value\"]".getBytes());
        Configuration configurations = new CommandlineConfigurationProvider(new String[]{"--encodedArgs", args}).get();

        assertTrue(configurations.containsKey("key"));
        assertEquals("value", configurations.getString("key", ""));
    }
}
