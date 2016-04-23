package ee.ut.jjanno.simulation;

import java.awt.Color;
import java.util.List;

import ee.ut.jjanno.view.View;

public class CollisionSystem {

	public static void simulate(List<Body> bodies, View view) {

		boolean collisions = true;
		boolean broken = false;

		while (collisions) {
			int len = bodies.size();
			for (int i = 0; i < len - 1; i++) {
				for (int j = i + 1; j < len; j++) {
					Body b1 = bodies.get(i);
					Body b2 = bodies.get(j);

					float dist = (float) Math.sqrt((b1.x - b2.x) * (b1.x - b2.x) + (b1.y - b2.y) * (b1.y - b2.y));
					float bound = b1.size + b2.size;

					if (dist < bound) {
						Body sum;
						if (b1.mass > b2.mass)
							sum = collide(b1, b2);
						else
							sum = collide(b2, b1);
						if (sum != null) {
							Body ref = view.reference;
							if (b1 == ref || b2 == ref) {
								view.reference = sum;
							}
							view.getBodies().remove(b1);
							view.getBodies().remove(b2);
							view.getBodies().add(sum);
						}
						broken = true;
						break;
					}
				}
				if (broken) {
					broken = false;
					break;
				}

			}
			collisions = false;
		}

	}

	private static Body collide(Body b1, Body b2) {
		float ratio = b1.mass / b2.mass;

		float size = (float) Math.sqrt(b1.size * b1.size + b2.size * b2.size);
		float yv = b1.yv * ratio + b1.yv * (1f - ratio);
		float xv = b1.xv * ratio + b1.xv * (1f - ratio);
		float x = b1.x;
		float y = b1.y;
		float mass = b1.mass;

		Body newBody = new Body(x, y, xv, yv, mass, size);

		Color c1 = b1.color;
		Color c2 = b2.color;
		newBody.color = new Color((c1.getRed() + c2.getRed()) / 2, (c1.getGreen() + c2.getGreen()) / 2,
				(c1.getBlue() + c2.getBlue()) / 2);
		if (b1.filled || b2.filled) {
			newBody.filled = true;
			newBody.color = new Color(255, 255, 255);
			newBody.color = new Color(255, 255, 155);
		}

		return newBody;
	}

}
