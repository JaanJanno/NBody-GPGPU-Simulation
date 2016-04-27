package ee.ut.jjanno.gpusimulation;

import java.util.ArrayList;
import java.util.List;

import ee.ut.jjanno.simulation.Body;

public class BodyTreeParallel {

	private static final int MT_CUTOFF = 5000;
	
	BodyTreeParallel lt;
	BodyTreeParallel rt;
	BodyTreeParallel lb;
	BodyTreeParallel rb;

	float mass;
	float x;
	float y;

	float width;
	float xCentre;
	float yCentre;
	
	int index;
	int parent = 0;
	
	public BodyTreeParallel(List<Body> bodies, float xWallL, float xWallR, float yWallT, float yWallB, IndexSequence seq) {
		this(bodies, xWallL, xWallR, yWallT, yWallB);
		index(seq);
	}

	public BodyTreeParallel(List<Body> bodies, float xWallL, float xWallR, float yWallT, float yWallB) {
		width = xWallR - xWallL;
		xCentre = (xWallL / 2 + xWallR / 2);
		yCentre = (yWallT / 2 + yWallB / 2);

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

		if (bodies.size() < MT_CUTOFF) {

			if (ltBodies.size() == 0) {
				lt = null;
			} else {
				lt = new BodyTreeParallel(ltBodies, xWallL, xCentre, yWallT, yCentre);
			}
			if (lbBodies.size() == 0) {
				lb = null;
			} else {
				lb = new BodyTreeParallel(lbBodies, xWallL, xCentre, yCentre, yWallB);
			}
			if (rtBodies.size() == 0) {
				rt = null;
			} else {
				rt = new BodyTreeParallel(rtBodies, xCentre, xWallR, yWallT, yCentre);
			}
			if (rbBodies.size() == 0) {
				rb = null;
			} else {
				rb = new BodyTreeParallel(rbBodies, xCentre, xWallR, yCentre, yWallB);
			}
		} else {
			Thread ltT = new Thread() {
				public void run() {
					if (ltBodies.size() == 0) {
						lt = null;
					} else {
						lt = new BodyTreeParallel(ltBodies, xWallL, xCentre, yWallT, yCentre);
					}				
				};
			};
			ltT.start();
			
			Thread lbT = new Thread() {
				public void run() {
					if (lbBodies.size() == 0) {
						lb = null;
					} else {
						lb = new BodyTreeParallel(lbBodies, xWallL, xCentre, yCentre, yWallB);
					}
				};
			};
			lbT.start();
			
			Thread rtT = new Thread() {
				public void run() {
					if (rtBodies.size() == 0) {
						rt = null;
					} else {
						rt = new BodyTreeParallel(rtBodies, xCentre, xWallR, yWallT, yCentre);
					}		
				};
			};
			rtT.start();
			
			Thread rbT = new Thread() {
				public void run() {
					if (rbBodies.size() == 0) {
						rb = null;
					} else {
						rb = new BodyTreeParallel(rbBodies, xCentre, xWallR, yCentre, yWallB);
					}
				};
			};
			rbT.start();
			
			try {
				ltT.join();
				lbT.join();
				rtT.join();
				rbT.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}
	
	public void index(IndexSequence seq) {
		index = seq.getIndex();
		parent = seq.getParentIndex();
		
		if(lt != null)
			lt.index(seq.setParentIndex(index));
		if(lb != null)
			lb.index(seq.setParentIndex(index));
		if(rt != null)
			rt.index(seq.setParentIndex(index));
		if(rb != null)
			rb.index(seq.setParentIndex(index));
	}

}
