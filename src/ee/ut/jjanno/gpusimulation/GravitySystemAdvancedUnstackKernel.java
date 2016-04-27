package ee.ut.jjanno.gpusimulation;

import java.util.List;

import com.amd.aparapi.Kernel;

import ee.ut.jjanno.simulation.Body;
import ee.ut.jjanno.simulation.Gravity;

class GravitySystemAdvancedUnstackKernel extends Kernel {

	private static final float CUTOFF = 0.5f;
	private static final float CUTOFFSQ = CUTOFF * CUTOFF;

	private float[] points;
	private float[] tree;
	private int realSize;

	@Override
	public void run() {
		int pointIndex = getGlobalId() * 5;

		if (getGlobalId() < realSize) {

			float fx = 0;
			float fy = 0;

			int vertex = 1;
			int traversed = 1;

			while (vertex != 0) {
				int vertexPointer = (vertex - 1) * 9;
				boolean open = true;

				if (vertex != traversed) {
					open = false;
					int pointer2 = (int) tree[vertexPointer + 4];
					if (pointer2 > traversed && pointer2 != 0) {
						vertex = pointer2;
						traversed = vertex;
					} else {
						int pointer3 = (int) tree[vertexPointer + 5];
						if (pointer3 > traversed && pointer3 != 0) {
							vertex = pointer3;
							traversed = vertex;
						} else {
							int pointer4 = (int) tree[vertexPointer + 6];
							if (pointer4 > traversed && pointer4 != 0) {
								vertex = pointer4;
								traversed = vertex;
							} else {
								vertex = (int) tree[vertexPointer + 8];
							}
						}
					}
				}
				if (open && vertex == traversed) {
					float x1 = points[pointIndex];
					float y1 = points[pointIndex + 1];
					float x2 = tree[vertexPointer];
					float y2 = tree[vertexPointer + 1];

					float d = tree[vertexPointer + 7];

					float sqDistance = (x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2);

					if (sqDistance != 0 && (d * d) <= sqDistance * CUTOFFSQ) {
						float dist = sqrt(sqDistance);
						float m1 = points[pointIndex + 4];
						float m2 = tree[vertexPointer + 2];
						float fDivD = computeForceOverDist(m1, m2, dist + Gravity.wall);
						fx += computeForceComponent(x1, x2, fDivD);
						fy += computeForceComponent(y1, y2, fDivD);
						vertex = (int) tree[vertexPointer + 8];
					} else {
						int pointer1 = (int) tree[vertexPointer + 3];
						if (pointer1 > traversed) {
							vertex = pointer1;
							traversed = vertex;
						} else {
							int pointer2 = (int) tree[vertexPointer + 4];
							if (pointer2 > traversed) {
								vertex = pointer2;
								traversed = vertex;
							} else {
								int pointer3 = (int) tree[vertexPointer + 5];
								if (pointer3 > traversed) {
									vertex = pointer3;
									traversed = vertex;
								} else {
									int pointer4 = (int) tree[vertexPointer + 6];
									if (pointer4 > traversed) {
										vertex = pointer4;
										traversed = vertex;
									} else {
										vertex = (int) tree[vertexPointer + 8];
									}
								}
							}
						}
					}
				}

			}
			points[pointIndex + 2] += fx / points[pointIndex + 4];
			points[pointIndex + 3] += fy / points[pointIndex + 4];
			points[pointIndex + 0] += points[pointIndex + 2];
			points[pointIndex + 1] += points[pointIndex + 3];
		}

	}

	private static float computeForceOverDist(float mass1, float mass2, float dist) {
		return (Gravity.G * mass1 * mass2) / (dist * dist * dist);
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

	public void setBodies(List<Body> bodies) {
		float[] bodyArray = new float[bodies.size() * 5];
		for (int i = 0; i < bodies.size() * 5; i += 5) {
			Body b = bodies.get(i / 5);
			bodyArray[i] = b.x;
			bodyArray[i + 1] = b.y;
			bodyArray[i + 2] = b.xv;
			bodyArray[i + 3] = b.yv;
			bodyArray[i + 4] = b.mass;
		}
		points = bodyArray;
	}

	public void getBodies(List<Body> bodies) {
		for (int i = 0; i < bodies.size() * 5; i += 5) {
			Body b = bodies.get(i / 5);
			b.x = points[i];
			b.y = points[i + 1];
			b.xv = points[i + 2];
			b.yv = points[i + 3];
			b.mass = points[i + 4];
		}
	}

	public void setTree(float[] tree) {
		this.tree = tree;
	}

	public void setRealSize(int realSize) {
		this.realSize = realSize;
	}

}
