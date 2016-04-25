package ee.ut.jjanno.simulation;

public class Gravity {

	public static final float GModifier = 10f;
	public static final float G = 6.67f * GModifier;
	public static final float wall = 30.0f;

	public static float compute(float mass1, float mass2, float dist) {
		return (G * mass1 * mass2) / (dist * dist);
	}

	public static float[] computeVector(float mass1, float mass2, float x1, float y1, float x2, float y2) {
		float dist = (float) Math.sqrt((x1 - x2) * (x1 - x2) + (y1 - y2) * (y1 - y2));
		if (dist == 0) {
			return new float[] { 0, 0 };
		}
		float force = compute(mass1, mass2, dist + wall);
		float ratio = force / (dist + wall);
		return new float[] { computeComponent(x1, x2, ratio), computeComponent(y1, y2, ratio) };
	}

	public static float computeComponent(float c1, float c2, float ratio) {

		return (c2 - c1) * ratio;
	}

}
