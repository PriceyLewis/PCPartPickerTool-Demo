import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class PCPartAITest {

    @Test
    void emptyPromptReturnsGuidanceWithoutUsingDatabase() {
        PCPartAI assistant = new PCPartAI(null);

        PCPartAI.AssistantReply reply = assistant.respond("   ", Map.of());

        assertEquals(
            "Tell me what kind of component you want, your budget, or the store you want reviewed.",
            reply.getMessage()
        );
        assertFalse(reply.hasRecommendation());
    }

    @Test
    void parsesCommonUkPoundBudget() {
        assertEquals(500.0, PCPartAI.extractBudget("recommend a GPU under £500"));
    }

    @Test
    void parsesGbpBudgetAndDecimals() {
        assertEquals(749.99, PCPartAI.extractBudget("budget GBP 749.99"));
    }

    @Test
    void returnsSentinelWhenNoBudgetIsPresent() {
        assertEquals(-1.0, PCPartAI.extractBudget("recommend a quiet graphics card"));
    }
}
