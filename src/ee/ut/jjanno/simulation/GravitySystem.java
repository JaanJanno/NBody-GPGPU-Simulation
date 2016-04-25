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
		for(Body b: bodies)
			b.update();

	}
	
	public static void generateOrbitals(List<Body> bodies, int n, float refx, float refy, float orbitIn, float orbitOut) {
		generateOrbitals(bodies, n, refx, refy, orbitIn, orbitOut, Color.black, Color.black, 0, 0, 120f);
	}
	
	public static void generateOrbitals(List<Body> bodies, int n, float refx, float refy, float orbitIn, float orbitOut,
			Color c1, Color c2) {
		generateOrbitals(bodies, n, refx, refy, orbitIn, orbitOut, c1, c2, 0, 0, 120f);
	}

	public static void generateOrbitals(List<Body> bodies, int n, float refx, float refy, float orbitIn, float orbitOut,
			Color c1, Color c2, float xvB, float yvB, float cMass) {
		for (int i = 0; i < n; i++) {
			float dist = orbitIn + (float) Math.random() * orbitOut;
			float deg = 2f * (float) Math.PI * (float) Math.random();
			int x = (int) (refx + Math.cos(deg) * dist);
			int y = (int) (refy + Math.sin(deg) * dist);

			float dir = deg + (float) Math.PI / 2f;
			float speed = (float) Math.sqrt((Gravity.G * cMass) / dist);
			float xv = (float) Math.cos(dir) * speed + xvB;
			float yv = (float) Math.sin(dir) * speed + yvB;

			Body newBody = new Body(x, y, xv, yv, 0.00001f, 1.99f);

			newBody.color = new Color(c1.getRed() + (int)(Math.random()*c2.getRed()), c1.getGreen() + (int)(Math.random()*c2.getGreen()),
					c1.getBlue() + (int)(Math.random()*c2.getBlue()));

			bodies.add(newBody);
		}
	}

}
