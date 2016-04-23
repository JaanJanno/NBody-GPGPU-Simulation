package ee.ut.jjanno.gpusimulation;

import com.amd.aparapi.Kernel;

class GravitySystemAdvancedKernel extends Kernel {

	private static final float CUTOFF = 0f;

	private float[] points;
	private float[] tree;
	private int[] treeStack;

	@Override
	public void run() {
		int pointIndex = getGlobalId() * 7;
		points[pointIndex + 4] = 0;
		points[pointIndex + 5] = 0;

		int stackPointer = 0;
		treeStack[0] = 1;
		while (stackPointer >= 0) {
			int stackedPointer = treeStack[stackPointer];
			if (stackedPointer == 0) {
				stackPointer--;
				
			} else {
				int pointer = (stackedPointer - 1) * 8;

				float x1 = points[pointIndex];
				float y1 = points[pointIndex + 1];
				float m1 = points[pointIndex + 6];

				float x2 = tree[pointer];
				float y2 = tree[pointer + 1];
				float m2 = tree[pointer + 2];

				float d = tree[pointer + 7];

				float distance = sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));

				if (distance != 0 && d / distance < CUTOFF) {
					computeAccelerationVector(m1, m2, x1, y1, x2, y2, pointIndex);
					stackPointer--;
				} else {
					stackPointer--;

					int pointer1 = (int) tree[pointer + 3];
					int pointer2 = (int) tree[pointer + 4];
					int pointer3 = (int) tree[pointer + 5];
					int pointer4 = (int) tree[pointer + 6];

					if (pointer1 == 0 && pointer2 == 0 && pointer3 == 0 && pointer4 == 0) {
						computeAccelerationVector(m1, m2, x1, y1, x2, y2, pointIndex);
					} else {

						treeStack[++stackPointer] = pointer1;
						treeStack[++stackPointer] = pointer2;
						treeStack[++stackPointer] = pointer3;
						treeStack[++stackPointer] = pointer4;
					}
				}
			}
		}
		points[pointIndex + 2] += points[pointIndex + 4];
		points[pointIndex + 3] += points[pointIndex + 5];
		points[pointIndex + 0] += points[pointIndex + 2];
		points[pointIndex + 1] += points[pointIndex + 3];
	}

	private static float computeForce(float mass1, float mass2, float dist) {
		return (6.67f * mass1 * mass2) / (dist * dist);
	}

	private void computeAccelerationVector(float mass1, float mass2, float x1, float y1, float x2, float y2,
			int point) {
		float dist = sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
		if (dist == 0) {
			return;
		}
		float force = computeForce(mass1, mass2, dist);
		float ratio = force / dist;

		points[point + 4] += computeForceComponent(x1, x2, ratio) / mass1;
		points[point + 5] += computeForceComponent(y1, y2, ratio) / mass1;
	}

	private static float computeForceComponent(float c1, float c2, float ratio) {

		return (c2 - c1) * ratio;
	}

	public float[] getPoints() {
		return points;
	}

	public void setPoints(float[] points) {
		this.points = points;
	}

	public void setTree(float[] tree) {
		this.tree = tree;
	}

	public void setTreeStack(int[] treeStack) {
		this.treeStack = treeStack;
	}

}
