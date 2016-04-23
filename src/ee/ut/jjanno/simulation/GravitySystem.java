package ee.ut.jjanno.simulation;

import java.awt.Color;
import java.util.List;

public class GravitySystem {

	public static void simulate(List<Body> bodies) {
		int len = bodies.size();
		for (int i = 0; i < len - 1; i++) {
			for (int j = i + 1; j < len; j++) {
				Body b1 = bodies.get(i);
				Body b2 = bodies.get(j);

				float[] gravVector = Gravity.computeVector(b1.mass, b2.mass, b1.x, b1.y, b2.x, b2.y);
				float xb = gravVector[0];
				float yb = gravVector[1];
				b1.accelerate(xb, yb);
				b2.accelerate(-xb, -yb);
			}
		}

	}

	public static void generateOrbitals(List<Body> bodies, int n, float refx, float refy, float orbitIn, float orbitOut,
			Color c1, Color c2) {
		for (int i = 0; i < n; i++) {
			float dist = orbitIn + (float) Math.random() * orbitOut;
			float deg = 2f * (float) Math.PI * (float) Math.random();
			int x = (int) (refx + Math.cos(deg) * dist);
			int y = (int) (refy + Math.sin(deg) * dist);

			float dir = deg + (float) Math.PI / 2f;
			float speed = (float) Math.sqrt((Gravity.G * 120.0001f) / dist);
			float xv = (float) Math.cos(dir) * speed;
			float yv = (float) Math.sin(dir) * speed;

			Body newBody = new Body(x, y, xv, yv, 0.00001f, 1.99f);

			newBody.color = new Color(c1.getRed() + c2.getRed(), c1.getGreen() + c2.getGreen(),
					c1.getBlue() + c2.getBlue());

			bodies.add(newBody);
		}
	}

}
