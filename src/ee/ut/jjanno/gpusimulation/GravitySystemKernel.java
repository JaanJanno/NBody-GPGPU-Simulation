package ee.ut.jjanno.gpusimulation;

import com.amd.aparapi.Kernel;

class GravitySystemKernel extends Kernel {

	private float[] points;

	@Override
	public void run() {
		int pointIndex = getGlobalId() * 7;
		points[pointIndex+4] = 0;
		points[pointIndex+5] = 0;
		for(int i = 0; i < getGlobalSize() * 7; i += 7) {
			computeAccelerationVector(points[pointIndex+6], points[i+6], points[pointIndex], points[pointIndex+1], points[i], points[i+1], pointIndex);
		}
		points[pointIndex+2] += points[pointIndex+4];
		points[pointIndex+3] += points[pointIndex+5];
		points[pointIndex+0] += points[pointIndex+2];
		points[pointIndex+1] += points[pointIndex+3];
	}

	private static float computeForce(float mass1, float mass2, float dist) {
		return (6.67f * mass1 * mass2) / (dist * dist);
	}

	private void computeAccelerationVector(float mass1, float mass2, float x1, float y1, float x2, float y2, int point) {
		float dist = sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
		if (dist == 0) {
			return;
		}
		float force = computeForce(mass1, mass2, dist);
		float ratio = force / dist;
		
		points[point+4] += computeForceComponent(x1, x2, ratio) / mass1;
		points[point+5] += computeForceComponent(y1, y2, ratio) / mass1;
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

}
