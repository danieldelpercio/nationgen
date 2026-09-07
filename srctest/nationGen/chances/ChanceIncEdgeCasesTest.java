package nationGen.chances;

import java.util.Arrays;
import java.util.Random;

import nationGen.NationGen;
import nationGen.entities.Entity;
import nationGen.misc.Command;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests edge cases and boundary conditions in the ChanceInc system.
 * Focuses on error handling, extreme values, and corner cases.
 */
public class ChanceIncEdgeCasesTest {

    private NationGen nationGen;
    private TestEntity entity1;
    
    private EntityChances<TestEntity> chanceDistribution;

    private class TestEntity extends Entity {
        public TestEntity(NationGen nationGen, String name, double basechance) {
            super(nationGen);
            this.name = name;
            this.basechance = basechance;
        }

        @Override
        public void handleOwnCommand(Command command) {
            // No-op for testing
        }
    }

    @Before
    public void setup() {
        nationGen = new NationGen();
        entity1 = new TestEntity(nationGen, "test", 10.0);
        chanceDistribution = EntityChances.baseChances(Arrays.asList(entity1));
    }

    @Test
    public void testChanceModifiedToExactlyZero() {
        ChanceInc inc = ChanceInc.parse("true *0");
        chanceDistribution.modifyChance(entity1, inc.modificationAmount);

        assertEquals(0.0, chanceDistribution.getChance(entity1), 0.0001);
        assertFalse(chanceDistribution.hasPossible());
    }

    @Test
    public void testChanceModifiedToNegative() {
        // TODO: do we really want to allow negative chance values? What's the point?
        ChanceInc inc = ChanceInc.parse("true -15");
        chanceDistribution.modifyChance(entity1, inc.modificationAmount);

        double result = chanceDistribution.getChance(entity1);
        assertEquals(-5.0, result, 0.0001);
    }

    @Test
    public void testNegativeBasechanceEntity() {
        TestEntity negative = new TestEntity(nationGen, "neg", -5.0);
        chanceDistribution.setChance(negative, -5.0);

        ChanceInc inc = ChanceInc.parse("true +10");
        chanceDistribution.modifyChance(negative, inc.modificationAmount);

        assertEquals(5.0, chanceDistribution.getChance(negative), 0.0001);
    }

    @Test
    public void testNegativeChanceNotSelectedByGetRandom() {
        final int seed = 42;
        final Random random = new Random(seed);
        ChanceInc inc = ChanceInc.parse("true -20");
        chanceDistribution.modifyChance(entity1, inc.modificationAmount);

        // Should not select entity with negative chance
        assertNull(chanceDistribution.getRandom(random));
    }

    @Test
    public void testVeryLargeMultiplier() {
        ChanceInc inc = ChanceInc.parse("true *1000000");
        chanceDistribution.modifyChance(entity1, inc.modificationAmount);

        assertEquals(10000000.0, chanceDistribution.getChance(entity1), 0.1);
    }

    @Test
    public void testVerySmallMultiplier() {
        ChanceInc inc = ChanceInc.parse("true *0.000001");
        chanceDistribution.modifyChance(entity1, inc.modificationAmount);

        assertEquals(0.00001, chanceDistribution.getChance(entity1), 0.00000001);
    }

    @Test
    public void testZeroDivisionAttempt() {
        ChanceInc inc = ChanceInc.parse("true /0");
        try {
            chanceDistribution.modifyChance(entity1, inc.modificationAmount);
            fail("Division by zero should throw ArithmeticException");
        } catch (ArithmeticException e) {
            assertTrue("Expected division by zero exception", true);
        }
    }

    @Test
    public void testEmptyChancesDistribution() {
        final int seed = 42;
        final Random random = new Random(seed);
        EntityChances<TestEntity> emptyChances = new EntityChances<>();

        assertTrue(emptyChances.isEmpty());
        assertFalse(emptyChances.hasPossible());
        assertEquals(0, emptyChances.countPossible());
        assertNull(emptyChances.getRandom(random));
    }

    @Test
    public void testModifyChanceOnEmptyDistribution() {
        EntityChances<TestEntity> emptyChances = new EntityChances<>();
        TestEntity newEntity = new TestEntity(nationGen, "new", 5.0);

        ChanceInc inc = ChanceInc.parse("true +10");
        emptyChances.modifyChance(newEntity, inc.modificationAmount);

        // Should use base chance (5) + modifier (10) = 15
        assertEquals(15.0, emptyChances.getChance(newEntity), 0.0001);
    }

    @Test
    public void testMultipleZeroChances() {
        final int seed = 42;
        final Random random = new Random(seed);
        EntityChances<TestEntity> chanceDistribution = new EntityChances<>();
        TestEntity e1 = new TestEntity(nationGen, "e1", 0.0);
        TestEntity e2 = new TestEntity(nationGen, "e2", 0.0);

        chanceDistribution.setChance(e1, 0.0);
        chanceDistribution.setChance(e2, 0.0);

        assertTrue(chanceDistribution.isEmpty());
        assertNull(chanceDistribution.getRandom(random));
    }

    @Test
    public void testChanceIncWithZeroModifier() {
        ChanceInc inc = ChanceInc.parse("true +0");
        chanceDistribution.modifyChance(entity1, inc.modificationAmount);

        // Adding zero should not change the chance
        assertEquals(10.0, chanceDistribution.getChance(entity1), 0.0001);
    }

    @Test
    public void testChanceIncWithOneMultiplier() {
        ChanceInc inc = ChanceInc.parse("true *1");
        chanceDistribution.modifyChance(entity1, inc.modificationAmount);

        // Multiplying by 1 should not change the chance
        assertEquals(10.0, chanceDistribution.getChance(entity1), 0.0001);
    }

    @Test
    public void testChanceIncWithOneDivisor() {
        ChanceInc inc = ChanceInc.parse("true /1");
        chanceDistribution.modifyChance(entity1, inc.modificationAmount);

        // Dividing by 1 should not change the chance
        assertEquals(10.0, chanceDistribution.getChance(entity1), 0.0001);
    }

    @Test
    public void testFloatingPointPrecision() {
        EntityChances<TestEntity> chanceDistribution = new EntityChances<>();
        TestEntity e = new TestEntity(nationGen, "e", 0.1 + 0.2); // Classic floating point issue

        ChanceInc inc = ChanceInc.parse("true *10");
        chanceDistribution.modifyChance(e, inc.modificationAmount);

        // Result should be close to 3.0 (allowing for floating point errors)
        assertEquals(3.0, chanceDistribution.getChance(e), 0.0001);
    }

    @Test
    public void testChainedEliminationModifiers() {
        ChanceInc inc1 = ChanceInc.parse("true *0.5");
        ChanceInc inc2 = ChanceInc.parse("true *0.5");
        ChanceInc inc3 = ChanceInc.parse("true *0.5");

        chanceDistribution.modifyChance(entity1, inc1.modificationAmount); // 10 * 0.5 = 5
        chanceDistribution.modifyChance(entity1, inc2.modificationAmount); // 5 * 0.5 = 2.5
        chanceDistribution.modifyChance(entity1, inc3.modificationAmount); // 2.5 * 0.5 = 1.25

        assertEquals(1.25, chanceDistribution.getChance(entity1), 0.0001);
    }

    @Test
    public void testExtremelyLargeFinalChance() {
        ChanceInc inc = ChanceInc.parse("true *999999");
        chanceDistribution.modifyChance(entity1, inc.modificationAmount);

        assertEquals(9999990.0, chanceDistribution.getChance(entity1), 0.1);
    }

    @Test
    public void testExtremelySmallFinalChance() {
        ChanceInc inc = ChanceInc.parse("true *0.000001");
        chanceDistribution.modifyChance(entity1, inc.modificationAmount);

        double result = chanceDistribution.getChance(entity1);
        assertTrue("Chance should be very small", result < 0.0001 && result > 0);
    }

    @Test
    public void testConditionAlwaysTrueBypassesEvaluation() {
        ChanceInc inc = ChanceInc.parse("true +5");
        ChanceIncData emptyData = new ChanceIncData();
        
        // Even with empty/null data, true condition should not throw
        assertTrue(inc.condition.test(emptyData));
    }

    @Test
    public void testConditionAlwaysFalseBypassesEvaluation() {
        ChanceInc inc = ChanceInc.parse("false +5");
        ChanceIncData emptyData = new ChanceIncData();
        
        // Even with empty/null data, false condition should not throw
        assertFalse(inc.condition.test(emptyData));
    }

    @Test
    public void testChanceIncParsingWithExtraWhitespace() {
        ChanceInc inc = ChanceInc.parse("  true   +   5  ");
        assertTrue(inc.condition.test(new ChanceIncData()));
        assertEquals(15.0, inc.modificationAmount.applyOperatorTo(10.0), 0.0001);
    }

    @Test
    public void testChanceRangeAfterMultipleModifications() {
        // Start at 10, apply various mods to test stability
        ChanceInc mod1 = ChanceInc.parse("true +50");
        ChanceInc mod2 = ChanceInc.parse("true /2");
        ChanceInc mod3 = ChanceInc.parse("true -5");

        chanceDistribution.modifyChance(entity1, mod1.modificationAmount); // 10 + 50 = 60
        chanceDistribution.modifyChance(entity1, mod2.modificationAmount); // 60 / 2 = 30
        chanceDistribution.modifyChance(entity1, mod3.modificationAmount); // 30 - 5 = 25

        assertEquals(25.0, chanceDistribution.getChance(entity1), 0.0001);
        assertTrue(chanceDistribution.hasPossible());
    }

    @Test
    public void testSetChanceDirectly() {
        chanceDistribution.setChance(entity1, 42.5);
        assertEquals(42.5, chanceDistribution.getChance(entity1), 0.0001);
    }

    @Test
    public void testGetTotalChances() {
        EntityChances<TestEntity> chanceDistribution = new EntityChances<>();
        TestEntity e1 = new TestEntity(nationGen, "e1", 10.0);
        TestEntity e2 = new TestEntity(nationGen, "e2", 5.0);
        TestEntity e3 = new TestEntity(nationGen, "e3", 8.0);

        chanceDistribution.setChance(e1, 10.0);
        chanceDistribution.setChance(e2, 5.0);
        chanceDistribution.setChance(e3, 8.0);

        assertEquals(23.0, chanceDistribution.getTotalChances(), 0.0001);
    }
}
