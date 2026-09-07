package nationGen.chances;

import java.util.HashSet;

import nationGen.magic.MagicPath;
import nationGen.magic.MagicPathInts;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests that ChanceInc conditions correctly evaluate against ChanceIncData contexts.
 * Verifies condition types and composite boolean logic with realistic data.
 * 
 * Note: This test uses simplified "true"/"false" conditions (built-in to ChanceIncConditionType).
 * For real NationGen conditions like "#haspath", "#theme", etc., see integration tests.
 */
public class ChanceIncConditionEvaluationTest {
    private ChanceIncData data;

    @Before
    public void setup() {
        data = new ChanceIncData();
    }

    @Test
    public void testTrueCondition() {
        ChanceInc inc = ChanceInc.parse("true +1");
        assertTrue(inc.condition.test(data));
    }

    @Test
    public void testFalseCondition() {
        ChanceInc inc = ChanceInc.parse("false +1");
        assertFalse(inc.condition.test(data));
    }

    @Test
    public void testAndCondition_BothTrue() {
        ChanceInc inc = ChanceInc.parse("true and true +1");
        assertTrue(inc.condition.test(data));
    }

    @Test
    public void testAndCondition_FirstFalse() {
        ChanceInc inc = ChanceInc.parse("false and true +1");
        assertFalse(inc.condition.test(data));
    }

    @Test
    public void testAndCondition_SecondFalse() {
        ChanceInc inc = ChanceInc.parse("true and false +1");
        assertFalse(inc.condition.test(data));
    }

    @Test
    public void testAndCondition_BothFalse() {
        ChanceInc inc = ChanceInc.parse("false and false +1");
        assertFalse(inc.condition.test(data));
    }

    @Test
    public void testOrCondition_BothTrue() {
        ChanceInc inc = ChanceInc.parse("true or true +1");
        assertTrue(inc.condition.test(data));
    }

    @Test
    public void testOrCondition_FirstTrue() {
        ChanceInc inc = ChanceInc.parse("true or false +1");
        assertTrue(inc.condition.test(data));
    }

    @Test
    public void testOrCondition_SecondTrue() {
        ChanceInc inc = ChanceInc.parse("false or true +1");
        assertTrue(inc.condition.test(data));
    }

    @Test
    public void testOrCondition_BothFalse() {
        ChanceInc inc = ChanceInc.parse("false or false +1");
        assertFalse(inc.condition.test(data));
    }

    @Test
    public void testParenthesesPrecedence() {
        ChanceInc incNoParen = ChanceInc.parse("false and false or true +1");
        assertTrue(incNoParen.condition.test(data));

        ChanceInc incWithParen = ChanceInc.parse("false and (false or true) +1");
        assertFalse(incWithParen.condition.test(data));
    }

    @Test
    public void testNestedParentheses() {
        ChanceInc inc = ChanceInc.parse("(false or true) and true +1");
        assertTrue(inc.condition.test(data));

        ChanceInc inc2 = ChanceInc.parse("(false or true) and false +1");
        assertFalse(inc2.condition.test(data));
    }

    @Test
    public void testTripleAndCondition() {
        ChanceInc inc = ChanceInc.parse("true and true and true +1");
        assertTrue(inc.condition.test(data));

        ChanceInc incWithFalse = ChanceInc.parse("true and false and true +1");
        assertFalse(incWithFalse.condition.test(data));
    }

    @Test
    public void testTripleOrCondition() {
        ChanceInc inc = ChanceInc.parse("false or false or true +1");
        assertTrue(inc.condition.test(data));

        ChanceInc incAllFalse = ChanceInc.parse("false or false or false +1");
        assertFalse(incAllFalse.condition.test(data));
    }

    @Test
    public void testComplexExpression() {
        ChanceInc inc = ChanceInc.parse("(true or false) and (true or false) +1");
        assertTrue(inc.condition.test(data));

        ChanceInc inc2 = ChanceInc.parse("(true and false) or (false and true) +1");
        assertFalse(inc2.condition.test(data));
    }

    @Test
    public void testMagicPathConditionData() {
        // Set up magic path data
        data.atHighest = new HashSet<>();
        data.atHighest.add(MagicPath.FIRE);
        data.atHighest.add(MagicPath.NATURE);

        assertNotNull(data.atHighest);
        assertTrue(data.atHighest.contains(MagicPath.FIRE));
    }

    @Test
    public void testDiversityData() {
        data.diversity = 2;
        data.at = new int[10];
        data.at[0] = 3; // Fire
        data.at[4] = 2; // Nature

        assertEquals(2, data.diversity);
        assertEquals(3, data.at[0]);
        assertEquals(2, data.at[4]);
    }

    @Test
    public void testAverageResourcesData() {
        data.avgres = 15.5;
        data.avggold = 25.0;

        assertEquals(15.5, data.avgres, 0.0001);
        assertEquals(25.0, data.avggold, 0.0001);
    }

    @Test
    public void testNonrandomPathsData() {
        data.nonrandom_paths = new MagicPathInts();
        data.nonrandom_paths.set(MagicPath.BLOOD, 2);
        data.nonrandom_paths.set(MagicPath.ASTRAL, 1);

        assertEquals(2, data.nonrandom_paths.get(MagicPath.BLOOD));
        assertEquals(1, data.nonrandom_paths.get(MagicPath.ASTRAL));
    }

    @Test
    public void testConditionSourceTracking() {
        ChanceInc inc = ChanceInc.parse("true +5");
        assertNotNull(inc.source);
        assertTrue(inc.source.contains("true"));
    }

    @Test
    public void testModificationAmountExtraction() {
        ChanceInc inc = ChanceInc.parse("true +10");
        assertNotNull(inc.modificationAmount);
        
        double result = inc.modificationAmount.applyOperatorTo(5.0);
        assertEquals(15.0, result, 0.0001);
    }

    @Test
    public void testComplexConditionWithModifier() {
        ChanceInc inc = ChanceInc.parse("(true and true) or false *2");
        assertTrue(inc.condition.test(data));
        
        double result = inc.modificationAmount.applyOperatorTo(10.0);
        assertEquals(20.0, result, 0.0001);
    }

    @Test
    public void testDeepNesting() {
        ChanceInc inc = ChanceInc.parse("(((true))) +1");
        assertTrue(inc.condition.test(data));
    }

    @Test
    public void testMixedOperatorPrecedence() {
        // These are evaluated left to right
        ChanceInc inc = ChanceInc.parse("true or false and true +1");
        assertTrue(inc.condition.test(data));

        // These are evaluated left to right
        ChanceInc inc2 = ChanceInc.parse("false and true or false +1");
        assertFalse(inc2.condition.test(data));
    }
}
