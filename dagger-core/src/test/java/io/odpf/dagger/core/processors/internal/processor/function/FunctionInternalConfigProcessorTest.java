package io.odpf.dagger.core.processors.internal.processor.function;

import io.odpf.dagger.core.exception.InvalidConfigurationException;
import io.odpf.dagger.core.processors.ColumnNameManager;
import io.odpf.dagger.core.processors.common.RowManager;
import io.odpf.dagger.core.processors.internal.InternalSourceConfig;
import org.apache.flink.types.Row;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.Assert.*;

public class FunctionInternalConfigProcessorTest {

    @Test
    public void shouldBeAbleToProcessFunctionCustomType() {
        ColumnNameManager columnManager = new ColumnNameManager(new String[0], new ArrayList<>());
        FunctionInternalConfigProcessor functionInternalConfigProcessor = new FunctionInternalConfigProcessor(columnManager, getCustomConfig("function"));

        assertTrue(functionInternalConfigProcessor.canProcess("function"));
    }

    @Test
    public void shouldNotBeAbleToProcessConstantCustomType() {
        ColumnNameManager columnManager = new ColumnNameManager(new String[0], new ArrayList<>());
        FunctionInternalConfigProcessor functionInternalConfigProcessor = new FunctionInternalConfigProcessor(columnManager, getCustomConfig("constant"));

        assertFalse(functionInternalConfigProcessor.canProcess("constant"));
    }

    @Test
    public void shouldNotBeAbleToProcessSqlCustomType() {
        ColumnNameManager columnManager = new ColumnNameManager(new String[0], new ArrayList<>());
        FunctionInternalConfigProcessor functionInternalConfigProcessor = new FunctionInternalConfigProcessor(columnManager, getCustomConfig("sql"));

        assertFalse(functionInternalConfigProcessor.canProcess("sql"));
    }

    @Test
    public void shouldThrowInvalidConfigurationException() {
        ColumnNameManager columnNameManager = new ColumnNameManager(new String[]{"input1", "input2"}, Arrays.asList("output1", "output2", "output3"));
        InternalSourceConfig internalSourceConfig = new InternalSourceConfig("output3", "test", "function");
        FunctionInternalConfigProcessor functionInternalConfigProcessor = new FunctionInternalConfigProcessor(columnNameManager, internalSourceConfig);

        Row inputRow = new Row(2);
        Row outputRow = new Row(3);
        Row parentRow = new Row(2);
        parentRow.setField(0, inputRow);
        parentRow.setField(1, outputRow);
        RowManager rowManager = new RowManager(parentRow);

        InvalidConfigurationException invalidConfigException = assertThrows(InvalidConfigurationException.class,
                () -> functionInternalConfigProcessor.process(rowManager));
        assertEquals("The function test is not supported in custom configuration",
                invalidConfigException.getMessage());
    }

    @Test
    public void shouldProcessToPopulateDataAtRightIndexForRightConfiguration() {
        Timestamp currentTimestamp = new Timestamp(System.currentTimeMillis());
        ColumnNameManager columnNameManager = new ColumnNameManager(new String[]{"input1", "input2"}, Arrays.asList("output1", "output2", "output3"));
        InternalSourceConfig internalSourceConfig = new InternalSourceConfig("output2", "CURRENT_TIMESTAMP", "function");
        FunctionInternalConfigProcessor functionInternalConfigProcessor = new FunctionInternalConfigProcessorMock(columnNameManager, internalSourceConfig, currentTimestamp);

        Row inputRow = new Row(2);
        Row outputRow = new Row(3);
        Row parentRow = new Row(2);
        parentRow.setField(0, inputRow);
        parentRow.setField(1, outputRow);
        RowManager rowManager = new RowManager(parentRow);

        functionInternalConfigProcessor.process(rowManager);

        assertNotNull(rowManager.getOutputData().getField(1));
        assertEquals(currentTimestamp, rowManager.getOutputData().getField(1));
    }

    private InternalSourceConfig getCustomConfig(String type) {
        return new InternalSourceConfig("field", "value", type);
    }

    final class FunctionInternalConfigProcessorMock extends FunctionInternalConfigProcessor {

        private Timestamp currentTimestamp;

        private FunctionInternalConfigProcessorMock(ColumnNameManager columnNameManager, InternalSourceConfig internalSourceConfig, Timestamp currentTimestamp) {
            super(columnNameManager, internalSourceConfig);
            this.currentTimestamp = currentTimestamp;
        }

        @Override
        protected Timestamp getCurrentTime() {
            return currentTimestamp;
        }
    }
}
