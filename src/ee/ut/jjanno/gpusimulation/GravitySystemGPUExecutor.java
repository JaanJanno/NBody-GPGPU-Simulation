package ee.ut.jjanno.gpusimulation;

import java.util.ArrayList;
import java.util.List;

import com.amd.aparapi.Kernel.EXECUTION_MODE;

import ee.ut.jjanno.simulation.Body;
import ee.ut.jjanno.view.View;

public class GravitySystemGPUExecutor {

	static final GravitySystemKernel kernel = new GravitySystemKernel();
	static final GravitySystemAdvancedKernel kernelAdvanced = new GravitySystemAdvancedKernel();
	static final GravitySystemAdvancedUnstackKernel kernelAdvancedPlus = new GravitySystemAdvancedUnstackKernel();
	static final GravitySystemAdvancedUnstackDuoKernel kernelAdvancedPlusDuo = new GravitySystemAdvancedUnstackDuoKernel();

	static {
		kernel.setExecutionMode(EXECUTION_MODE.GPU);
		kernelAdvanced.setExecutionMode(EXECUTION_MODE.GPU);
		kernelAdvancedPlus.setExecutionMode(EXECUTION_MODE.GPU);
		kernelAdvancedPlusDuo.setExecutionMode(EXECUTION_MODE.GPU);
	}

	public static void execute(List<Body> bodies) {
		kernel.setPoints(parametrizeBodies(bodies));
		kernel.setRefPoints(parametrizeBodies(bodies));
		kernel.setRealSize(bodies.size());
		kernel.execute(getClosestSize(bodies.size()));
		deParametrizeBodies(kernel.getPoints(), bodies);
	}

	public static void executeAdvanced(List<Body> bodies) {
		float[] extremes = View.getExtremes(bodies);
		IndexSequence seq = new IndexSequence(0);
		BodyTree tree = new BodyTree(bodies, seq, extremes[0], extremes[1], extremes[2], extremes[3], false);
		kernelAdvanced.setPoints(parametrizeBodies(bodies));
		kernelAdvanced.setTree(parametrizeTree(tree, seq.getLastIndex()));
		int stackSize = 6400 + seq.getLastIndex() * seq.getLastIndex();
		kernelAdvanced.setTreeStack(new int[stackSize]);
		kernelAdvanced.setParams(stackSize);
		kernelAdvanced.execute(bodies.size());
		deParametrizeBodies(kernelAdvanced.getPoints(), bodies);
	}

	public static void executeAdvancedPlus(List<Body> bodies) {
		float[] extremes = View.getExtremes(bodies);
		IndexSequence seq = new IndexSequence(1);
		BodyTree tree = new BodyTree(bodies, seq, extremes[0], extremes[1], extremes[2], extremes[3], false);
		kernelAdvancedPlus.setPoints(parametrizeBodies(bodies));
		kernelAdvancedPlus.setTree(parametrizeTreeAdvanced(tree, seq.getLastIndex()));
		kernelAdvancedPlus.setRealSize(bodies.size());
		kernelAdvancedPlus.execute(getClosestSize(bodies.size()));
		deParametrizeBodies(kernelAdvancedPlus.getPoints(), bodies);
	}

	public static void executeAdvancedPlusParallelTree(List<Body> bodies) {
		float[] extremes = View.getExtremes(bodies);
		IndexSequence seq = new IndexSequence(1);
		BodyTreeParallel tree = new BodyTreeParallel(bodies, extremes[0], extremes[1], extremes[2], extremes[3], seq);
		kernelAdvancedPlus.setBodies(bodies);
		kernelAdvancedPlus.setTree(parametrizeTreeAdvanced(tree, seq.getLastIndex()));
		kernelAdvancedPlus.setRealSize(bodies.size());
		kernelAdvancedPlus.execute(getClosestSize(bodies.size()));
		kernelAdvancedPlus.getBodies(bodies);
	}

	public static void executeAdvancedPlusParallelTreeRefed(List<Body> bodies) {
		float[] extremes = View.getExtremes(bodies);
		IndexSequence seq = new IndexSequence(1);
		BodyTreeParallel tree = new BodyTreeParallel(bodies, extremes[0], extremes[1], extremes[2], extremes[3]);
		tree.index(seq);
		kernelAdvancedPlusDuo.setPoints(parametrizeBodies(bodies));
		kernelAdvancedPlusDuo.setTree(parametrizeTreeAdvancedUnrefed(tree, seq.getLastIndex()));
		kernelAdvancedPlusDuo.setRefs(parametrizeTreeIndex(tree, seq.getLastIndex()));
		kernelAdvancedPlusDuo.setRealSize(bodies.size());
		kernelAdvancedPlusDuo.execute(getClosestSize(bodies.size()));
		deParametrizeBodies(kernelAdvancedPlusDuo.getPoints(), bodies);
	}

	private static int getClosestSize(int size) {
		return (int) (Math.pow(2, (int) (Math.log(size) / Math.log(2)) + 1));
	}

	private static float[] parametrizeBodies(List<Body> bodies) {
		float[] bodyArray = new float[bodies.size() * 7];
		for (int i = 0; i < bodies.size() * 7; i += 7) {
			Body b = bodies.get(i / 7);
			bodyArray[i] = b.x;
			bodyArray[i + 1] = b.y;
			bodyArray[i + 2] = b.xv;
			bodyArray[i + 3] = b.yv;
			bodyArray[i + 6] = b.mass;
		}
		return bodyArray;
	}

	private static List<Body> deParametrizeBodies(float[] bodyArray, List<Body> bodies) {
		List<Body> bodyList = new ArrayList<Body>();
		for (int i = 0; i < bodies.size() * 7; i += 7) {
			Body b = bodies.get(i / 7);
			b.x = bodyArray[i];
			b.y = bodyArray[i + 1];
			b.xv = bodyArray[i + 2];
			b.yv = bodyArray[i + 3];
			b.mass = bodyArray[i + 6];
		}
		return bodyList;
	}

	private static int[] parametrizeTreeIndex(BodyTreeParallel tree, int size) {
		int[] treeArray = new int[size * 5];
		fillTreeIndex(tree, treeArray);
		return treeArray;
	}

	private static void fillTreeIndex(BodyTreeParallel tree, int[] treeArray) {
		int pointer = (tree.index - 1) * 5;
		treeArray[pointer] = tree.parent;

		if (tree.lt != null) {
			treeArray[pointer + 1] = tree.lt.index;
			fillTreeIndex(tree.lt, treeArray);
		} else {
			treeArray[pointer + 1] = 0;
		}

		if (tree.lb != null) {
			treeArray[pointer + 2] = tree.lb.index;
			fillTreeIndex(tree.lb, treeArray);
		} else {
			treeArray[pointer + 2] = 0;
		}

		if (tree.rt != null) {
			treeArray[pointer + 3] = tree.rt.index;
			fillTreeIndex(tree.rt, treeArray);
		} else {
			treeArray[pointer + 3] = 0;
		}

		if (tree.rb != null) {
			treeArray[pointer + 4] = tree.rb.index;
			fillTreeIndex(tree.rb, treeArray);
		} else {
			treeArray[pointer + 4] = 0;
		}
	}

	private static float[] parametrizeTree(BodyTree tree, int size) {
		float[] treeArray = new float[size * 8];
		fillTree(tree, treeArray);
		return treeArray;
	}

	private static float[] parametrizeTreeAdvanced(BodyTree tree, int size) {
		float[] treeArray = new float[size * 9];
		fillTreeAdvanced(tree, treeArray);
		return treeArray;
	}

	private static float[] parametrizeTreeAdvanced(BodyTreeParallel tree, int size) {
		float[] treeArray = new float[size * 9];
		fillTreeAdvanced(tree, treeArray);
		return treeArray;
	}

	private static float[] parametrizeTreeAdvancedUnrefed(BodyTreeParallel tree, int size) {
		float[] treeArray = new float[size * 4];
		fillTreeAdvancedUnrefed(tree, treeArray);
		return treeArray;
	}

	private static void fillTreeAdvancedUnrefed(BodyTreeParallel tree, float[] treeArray) {
		int pointer = (tree.index - 1) * 4;
		treeArray[pointer] = tree.x;
		treeArray[pointer + 1] = tree.y;
		treeArray[pointer + 2] = tree.mass;
		treeArray[pointer + 3] = tree.width;

		if (tree.lt != null)
			fillTreeAdvancedUnrefed(tree.lt, treeArray);

		if (tree.lb != null)
			fillTreeAdvancedUnrefed(tree.lb, treeArray);

		if (tree.rt != null)
			fillTreeAdvancedUnrefed(tree.rt, treeArray);

		if (tree.rb != null)
			fillTreeAdvancedUnrefed(tree.rb, treeArray);

	}

	private static void fillTree(BodyTree tree, float[] treeArray) {
		int pointer = tree.index * 8;
		treeArray[pointer] = tree.x;
		treeArray[pointer + 1] = tree.y;
		treeArray[pointer + 2] = tree.mass;
		treeArray[pointer + 7] = tree.width;

		if (tree.lt != null) {
			treeArray[pointer + 3] = tree.lt.index + 1;
			fillTree(tree.lt, treeArray);
		} else {
			treeArray[pointer + 3] = 0;
		}

		if (tree.lb != null) {
			treeArray[pointer + 4] = tree.lb.index + 1;
			fillTree(tree.lb, treeArray);
		} else {
			treeArray[pointer + 4] = 0;
		}

		if (tree.rt != null) {
			treeArray[pointer + 5] = tree.rt.index + 1;
			fillTree(tree.rt, treeArray);
		} else {
			treeArray[pointer + 5] = 0;
		}

		if (tree.rb != null) {
			treeArray[pointer + 6] = tree.rb.index + 1;
			fillTree(tree.rb, treeArray);
		} else {
			treeArray[pointer + 6] = 0;
		}

	}

	private static void fillTreeAdvanced(BodyTree tree, float[] treeArray) {
		int pointer = (tree.index - 1) * 9;
		treeArray[pointer] = tree.x;
		treeArray[pointer + 1] = tree.y;
		treeArray[pointer + 2] = tree.mass;
		treeArray[pointer + 7] = tree.width;
		treeArray[pointer + 8] = tree.parent;

		if (tree.lt != null) {
			treeArray[pointer + 3] = tree.lt.index;
			fillTreeAdvanced(tree.lt, treeArray);
		} else {
			treeArray[pointer + 3] = 0;
		}

		if (tree.lb != null) {
			treeArray[pointer + 4] = tree.lb.index;
			fillTreeAdvanced(tree.lb, treeArray);
		} else {
			treeArray[pointer + 4] = 0;
		}

		if (tree.rt != null) {
			treeArray[pointer + 5] = tree.rt.index;
			fillTreeAdvanced(tree.rt, treeArray);
		} else {
			treeArray[pointer + 5] = 0;
		}

		if (tree.rb != null) {
			treeArray[pointer + 6] = tree.rb.index;
			fillTreeAdvanced(tree.rb, treeArray);
		} else {
			treeArray[pointer + 6] = 0;
		}

	}

	private static void fillTreeAdvanced(BodyTreeParallel tree, float[] treeArray) {
		int pointer = (tree.index - 1) * 9;
		treeArray[pointer] = tree.x;
		treeArray[pointer + 1] = tree.y;
		treeArray[pointer + 2] = tree.mass;
		treeArray[pointer + 7] = tree.width;
		treeArray[pointer + 8] = tree.parent;

		if (tree.lt != null) {
			treeArray[pointer + 3] = tree.lt.index;
			fillTreeAdvanced(tree.lt, treeArray);
		} else {
			treeArray[pointer + 3] = 0;
		}

		if (tree.lb != null) {
			treeArray[pointer + 4] = tree.lb.index;
			fillTreeAdvanced(tree.lb, treeArray);
		} else {
			treeArray[pointer + 4] = 0;
		}

		if (tree.rt != null) {
			treeArray[pointer + 5] = tree.rt.index;
			fillTreeAdvanced(tree.rt, treeArray);
		} else {
			treeArray[pointer + 5] = 0;
		}

		if (tree.rb != null) {
			treeArray[pointer + 6] = tree.rb.index;
			fillTreeAdvanced(tree.rb, treeArray);
		} else {
			treeArray[pointer + 6] = 0;
		}

	}

}
