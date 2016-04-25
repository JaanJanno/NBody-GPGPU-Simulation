package ee.ut.jjanno.gpusimulation;

import com.amd.aparapi.Kernel;

import ee.ut.jjanno.simulation.Gravity;

class GravitySystemAdvancedUnstackKernel extends Kernel {

	private static final float CUTOFF = 0.5f;

	private float[] points;
	private float[] tree;
	private int realSize;

	@Override
	public void run() {
		int pointIndex = getGlobalId() * 7;

		if (getGlobalId() < realSize) {

			points[pointIndex + 4] = 0;
			points[pointIndex + 5] = 0;

			int vertex = 1;
			int traversed = 1;

			while (vertex != 0) {
				int vertexPointer = (vertex - 1) * 9;
				int pointer1 = (int) tree[vertexPointer + 3];
				int pointer2 = (int) tree[vertexPointer + 4];
				int pointer3 = (int) tree[vertexPointer + 5];
				int pointer4 = (int) tree[vertexPointer + 6];
				boolean lock = false;

				if (vertex == traversed) {
					float x1 = points[pointIndex];
					float y1 = points[pointIndex + 1];
					float m1 = points[pointIndex + 6];

					float x2 = tree[vertexPointer];
					float y2 = tree[vertexPointer + 1];
					float m2 = tree[vertexPointer + 2];

					float d = tree[vertexPointer + 7];

					float distance = sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));

					if (distance != 0 && d / distance <= CUTOFF) {
						computeAccelerationVector(m1, m2, x1, y1, x2, y2, pointIndex, distance);
						vertex = (int) tree[vertexPointer + 8];
						lock = true;
					} else {
						if (pointer1 > traversed && pointer1 != 0) {
							vertex = (int) tree[vertexPointer + 3];
							traversed = vertex;
						} else if (pointer2 > traversed && pointer2 != 0) {
							vertex = (int) tree[vertexPointer + 4];
							traversed = vertex;
						} else if (pointer3 > traversed && pointer3 != 0) {
							vertex = (int) tree[vertexPointer + 5];
							traversed = vertex;
						} else if (pointer4 > traversed && pointer4 != 0) {
							vertex = (int) tree[vertexPointer + 6];
							traversed = vertex;
						} else {
							vertex = (int) tree[vertexPointer + 8];
							lock = true;
						}
					}
				}
				if (vertex != traversed && !lock) {
					if (pointer2 > traversed && pointer2 != 0) {
						vertex = (int) tree[vertexPointer + 4];
						traversed = vertex;
					} else if (pointer3 > traversed && pointer3 != 0) {
						vertex = (int) tree[vertexPointer + 5];
						traversed = vertex;
					} else if (pointer4 > traversed && pointer4 != 0) {
						vertex = (int) tree[vertexPointer + 6];
						traversed = vertex;
					} else {
						vertex = (int) tree[vertexPointer + 8];
					}
				}
			}
			points[pointIndex + 2] += points[pointIndex + 4];
			points[pointIndex + 3] += points[pointIndex + 5];
			points[pointIndex + 0] += points[pointIndex + 2];
			points[pointIndex + 1] += points[pointIndex + 3];
		}

	}

	private static float computeForce(float mass1, float mass2, float dist) {
		return (Gravity.G * mass1 * mass2) / (dist * dist);
	}

	private void computeAccelerationVector(float mass1, float mass2, float x1, float y1, float x2, float y2, int point,
			float dist) {
		if (dist == 0) {
			return;
		}
		float force = computeForce(mass1, mass2, dist + Gravity.wall);
		float ratio = force / (dist + Gravity.wall);

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

	public void setRealSize(int realSize) {
		this.realSize = realSize;
	}

}
