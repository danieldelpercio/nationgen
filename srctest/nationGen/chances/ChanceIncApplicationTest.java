package nationGen.chances;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import nationGen.NationGen;
import nationGen.entities.Entity;
import nationGen.misc.Command;

public class ChanceIncApplicationTest {
    
    private NationGen nationGen;
    private TestEntity entity1;
    private TestEntity entity2;
    private TestEntity entity3;

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

        entity1 = new TestEntity(nationGen, "fire_bonus", 10.0);
        entity2 = new TestEntity(nationGen, "cold_bonus", 5.0);
        entity3 = new TestEntity(nationGen, "shock_bonus", 8.0);

        chanceDistribution = EntityChances.baseChances(Arrays.asList(entity1, entity2, entity3));
    }

    @Test
    public void testInitialChancesFromBasechance() {
        double chanceEntity1 = chanceDistribution.getChance(entity1);
        double chanceEntity2 = chanceDistribution.getChance(entity2);
        double chanceEntity3 = chanceDistribution.getChance(entity3);
        double errorTolerance = 0.0001;

        assertEquals(10.0, chanceEntity1, errorTolerance);
        assertEquals(5.0, chanceEntity2, errorTolerance);
        assertEquals(8.0, chanceEntity3, errorTolerance);
    }

    @Test
    public void testSingleAdditionModifier() {
        ChanceInc plusFive = ChanceInc.parse("true +5.0");
        ChanceInc minusSixPointThree = ChanceInc.parse("true +6.3");
        double errorTolerance = 0.0001;

        chanceDistribution.modifyChance(entity1, plusFive.modificationAmount);
        chanceDistribution.modifyChance(entity2, minusSixPointThree.modificationAmount);

        assertEquals(15.0, chanceDistribution.getChance(entity1), errorTolerance);
        assertEquals(11.3, chanceDistribution.getChance(entity2), errorTolerance);
        assertEquals(8.00, chanceDistribution.getChance(entity3), errorTolerance);
    }

    @Test
    public void testSingleSubtractionModifier() {
        ChanceInc minusFive = ChanceInc.parse("true -5.0");
        ChanceInc minusSevenPointFour = ChanceInc.parse("true -7.4");
        double errorTolerance = 0.0001;

        chanceDistribution.modifyChance(entity1, minusFive.modificationAmount);
        chanceDistribution.modifyChance(entity2, minusSevenPointFour.modificationAmount);

        assertEquals(5.0, chanceDistribution.getChance(entity1), errorTolerance);
        assertEquals(-2.4, chanceDistribution.getChance(entity2), errorTolerance);
        assertEquals(8.00, chanceDistribution.getChance(entity3), errorTolerance);
    }

    @Test
    public void testSingleMultiplicationModifier() {
        ChanceInc timesFive = ChanceInc.parse("true *5");
        ChanceInc seventyFivePercent = ChanceInc.parse("true *0.75");
        double errorTolerance = 0.0001;

        chanceDistribution.modifyChance(entity1, timesFive.modificationAmount);
        chanceDistribution.modifyChance(entity2, seventyFivePercent.modificationAmount);

        assertEquals(50.0, chanceDistribution.getChance(entity1), errorTolerance);
        assertEquals(3.75, chanceDistribution.getChance(entity2), errorTolerance);
        assertEquals(8.00, chanceDistribution.getChance(entity3), errorTolerance);
    }

    @Test
    public void testSingleDivisionModifier() {
        ChanceInc byFive = ChanceInc.parse("true /5");
        ChanceInc byZeroPointFive = ChanceInc.parse("true /0.5");
        double errorTolerance = 0.0001;

        chanceDistribution.modifyChance(entity1, byFive.modificationAmount);
        chanceDistribution.modifyChance(entity2, byZeroPointFive.modificationAmount);

        assertEquals(2.0, chanceDistribution.getChance(entity1), errorTolerance);
        assertEquals(10.0, chanceDistribution.getChance(entity2), errorTolerance);
        assertEquals(8.00, chanceDistribution.getChance(entity3), errorTolerance);
    }

    @Test
    public void testSetBasechanceToZero() {
        double errorTolerance = 0.0;

        chanceDistribution.setChance(entity1, 0);

        assertEquals(0.0, chanceDistribution.getChance(entity1), errorTolerance);
        assertEquals(2, chanceDistribution.countPossible());
    }

    @Test
    public void testEliminateAllChances() {
        double errorTolerance = 0.0;

        chanceDistribution.eliminate(entity1);

        assertEquals(null, chanceDistribution.getChance(entity1));
        assertEquals(5.0, chanceDistribution.getChance(entity2), errorTolerance);

        chanceDistribution.eliminate(entity2);
        chanceDistribution.eliminate(entity3);

        assertEquals(true, chanceDistribution.isEmpty());
        assertEquals(0, chanceDistribution.countPossible());
        assertTrue(chanceDistribution.getPossible().isEmpty());
    }

    @Test
    public void testProbabilitySelectionRespectModifiedChances() {
        final int seed = 42;
        final Random random = new Random(seed);
        final int testIterations = 10000;
        final int errorTolerance = 1000;
        final double entity1Chance = 0.9;
        final double entity2Chance = 1 - entity1Chance;
        final double minEntity1AcceptedCount = Math.floor((testIterations * entity1Chance) - (errorTolerance * 0.5));
        final double maxEntity1AcceptedCount = Math.floor((testIterations * entity1Chance) + (errorTolerance * 0.5));
        int entity1Count = 0;

        chanceDistribution.setChance(entity1, entity1Chance);
        chanceDistribution.setChance(entity2, entity2Chance);
        chanceDistribution.setChance(entity3, 0);
        
        for (int i = 0; i < testIterations; i++) {
            if (chanceDistribution.getRandom(random) == entity1) {
                entity1Count++;
            }
        }

        assertTrue(entity1Count >= minEntity1AcceptedCount && entity1Count < maxEntity1AcceptedCount);
    }
}
