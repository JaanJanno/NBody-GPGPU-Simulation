package ee.ut.jjanno.gpusimulation;

import java.util.ArrayList;
import java.util.List;

import ee.ut.jjanno.simulation.Body;
import ee.ut.jjanno.view.View;

public class BodyTree {

	BodyTree lt;
	BodyTree rt;
	BodyTree lb;
	BodyTree rb;

	int index;
	float mass;
	float x;
	float y;
	float width;
	int parent = 0;

	boolean visualize = false;

	public BodyTree(List<Body> bodies, float xWallL, float xWallR, float yWallT, float yWallB) {
		this(bodies, new IndexSequence(0), xWallL, xWallR, yWallT, yWallB, true);
	}

	public BodyTree(List<Body> bodies, IndexSequence seq, float xWallL, float xWallR, float yWallT, float yWallB,
			boolean visualize) {
		this.visualize = visualize;
		index = seq.getIndex();
		width = xWallR - xWallL;
		float xCentre = (xWallL / 2 + xWallR / 2);
		float yCentre = (yWallT / 2 + yWallB / 2);

		if (bodies.size() == 1) {
			Body b = bodies.get(0);
			mass = b.mass;
			x = b.x;
			y = b.y;
			width = 0;
			return;
		}

		List<Body> ltBodies = new ArrayList<>();
		List<Body> lbBodies = new ArrayList<>();
		List<Body> rtBodies = new ArrayList<>();
		List<Body> rbBodies = new ArrayList<>();

		for (Body b : bodies) {
			mass += b.mass;
			x += b.mass * b.x;
			y += b.mass * b.y;
			if (b.x <= xCentre && b.y <= yCentre) {
				ltBodies.add(b);
			} else if (b.x <= xCentre && b.y >= yCentre) {
				lbBodies.add(b);
			} else if (b.x >= xCentre && b.y <= yCentre) {
				rtBodies.add(b);
			} else if (b.x >= xCentre && b.y >= yCentre) {
				rbBodies.add(b);
			}
		}
		x /= mass;
		y /= mass;

		if (xWallR - xWallL < 0.01 || yWallB - yWallT < 0.01) {
			return;
		}

		if (ltBodies.size() == 0) {
			lt = null;
		} else {
			lt = new BodyTree(ltBodies, seq, xWallL, xCentre, yWallT, yCentre, visualize);
			lt.parent = this.index;
		}
		if (lbBodies.size() == 0) {
			lb = null;
		} else {
			lb = new BodyTree(lbBodies, seq, xWallL, xCentre, yCentre, yWallB, visualize);
			lb.parent = this.index;
		}
		if (rtBodies.size() == 0) {
			rt = null;
		} else {
			rt = new BodyTree(rtBodies, seq, xCentre, xWallR, yWallT, yCentre, visualize);
			rt.parent = this.index;
		}
		if (rbBodies.size() == 0) {
			rb = null;
		} else {
			rb = new BodyTree(rbBodies, seq, xCentre, xWallR, yCentre, yWallB, visualize);
			rb.parent = this.index;
		}

		if (visualize)
			View.addRay(xWallL, xWallR, yWallT, yWallB, xCentre, yCentre);
	}

}
