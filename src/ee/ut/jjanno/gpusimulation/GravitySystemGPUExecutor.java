package ee.ut.jjanno.gpusimulation;

import java.util.ArrayList;
import java.util.List;

import com.amd.aparapi.Kernel.EXECUTION_MODE;

import ee.ut.jjanno.simulation.Body;
import ee.ut.jjanno.view.View;

public class GravitySystemGPUExecutor {

	static final GravitySystemKernel kernel = new GravitySystemKernel();
	static final GravitySystemAdvancedKernel kernelAdvanced = new GravitySystemAdvancedKernel();

	static {
		kernel.setExecutionMode(EXECUTION_MODE.GPU);
		kernelAdvanced.setExecutionMode(EXECUTION_MODE.GPU);
	}

	public static void execute(List<Body> bodies) {
		kernel.setPoints(parametrizeBodies(bodies));
		kernel.execute(bodies.size());
		deParametrizeBodies(kernel.getPoints(), bodies);
	}

	public static void executeAdvanced(List<Body> bodies) {
		float[] extremes = View.getExtremes(bodies);
		IndexSequence seq = new IndexSequence();
		BodyTree tree = new BodyTree(bodies, seq, extremes[0], extremes[1], extremes[2], extremes[3]);
		
		kernelAdvanced.setPoints(parametrizeBodies(bodies));
		kernelAdvanced.setTree(parametrizeTree(tree, seq.getLastIndex()));
		kernelAdvanced.setTreeStack(new int[6400 + seq.getLastIndex()]);
		kernelAdvanced.execute(bodies.size());
		deParametrizeBodies(kernelAdvanced.getPoints(), bodies);
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
		for (int i = 0; i < bodyArray.length; i += 7) {
			Body b = bodies.get(i / 7);
			b.x = bodyArray[i];
			b.y = bodyArray[i + 1];
			b.xv = bodyArray[i + 2];
			b.yv = bodyArray[i + 3];
			b.mass = bodyArray[i + 6];
		}
		return bodyList;
	}

	private static float[] parametrizeTree(BodyTree tree, int size) {
		float[] treeArray = new float[size * 8];
		//System.out.println("TAG");
		fillTree(tree, treeArray);
		return treeArray;
	}

	private static void fillTree(BodyTree tree, float[] treeArray) {
		int pointer = tree.index * 8;
		treeArray[pointer] = tree.x;
		treeArray[pointer + 1] = tree.y;
		treeArray[pointer + 2] = tree.mass;
		
		/*
		System.out.println(treeArray[pointer]);
		System.out.println(treeArray[pointer+1]);
		System.out.println(treeArray[pointer+2]);
		System.out.println(treeArray[pointer+7]);
		treeArray[pointer + 7] = tree.width;
		
		if (tree.lt != null)
			System.out.println(tree.lt.index);
		else
			System.out.println(0);
		if (tree.lb != null)
			System.out.println(tree.lb.index);
		else
			System.out.println(0);
		if (tree.rt != null)
			System.out.println(tree.rt.index);
		else
			System.out.println(0);
		if (tree.rb != null)
			System.out.println(tree.rb.index);
		else
			System.out.println(0);
		System.out.println();
		*/

		if (tree.lt != null) {
			treeArray[pointer + 3] = tree.lt.index+1;
			fillTree(tree.lt, treeArray);
		} else {
			treeArray[pointer + 3] = 0;
		}
		
		if (tree.lb != null) {
			treeArray[pointer + 4] = tree.lb.index+1;
			fillTree(tree.lb, treeArray);
		} else {
			treeArray[pointer + 4] = 0;
		}
		
		if (tree.rt != null) {
			treeArray[pointer + 5] = tree.rt.index+1;
			fillTree(tree.rt, treeArray);
		} else {
			treeArray[pointer + 5] = 0;
		}
		
		if (tree.rb != null) {
			treeArray[pointer + 6] = tree.rb.index+1;
			fillTree(tree.rb, treeArray);
		} else {
			treeArray[pointer + 6] = 0;
		}

	}

}
