package nationGen.entities;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import nationGen.NationGen;
import nationGen.misc.ResourceStorage;

/**
 * PoseIntegrityTest
 * Set of tests to verify the data integrity of Pose data definitions.
 */
public class PoseIntegrityTest {
    private NationGen nationGen;
    private ResourceStorage<Pose> poses;

    @Before
    public void setup() {
        nationGen = new NationGen();
        poses = nationGen.getAssets().poses;
    }

    @Test
    public void testAllPosesHaveIds() {
        List<String> poseRolesWithoutId = new ArrayList<>();

        poses.forEach((poseCategoryName, poses) -> {
            poses.forEach(pose -> {
                if (pose.getName() == null) {
                    poseRolesWithoutId.add(pose.roles.toString());
                }
            });
        });

        assertEquals("The following poses are missing an #id tag: " + poseRolesWithoutId.toString(), poseRolesWithoutId.size(), 0);
    }
}
