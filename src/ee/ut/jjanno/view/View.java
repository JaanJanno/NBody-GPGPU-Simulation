package ee.ut.jjanno.view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JPanel;

import ee.ut.jjanno.gpusimulation.BodyTree;
import ee.ut.jjanno.gpusimulation.GravitySystemGPUExecutor;
import ee.ut.jjanno.simulation.Body;
import ee.ut.jjanno.simulation.CollisionSystem;
import ee.ut.jjanno.simulation.GravitySystem;

@SuppressWarnings("serial")
public class View extends JPanel {

	List<Body> bodies;
	public Body reference;

	private static List<Float> rays = new ArrayList<>();
	
	public static synchronized void addRay(Float xWallL, Float xWallR, Float yWallT, Float yWallB, Float xCentre, Float yCentre) {
		rays.add(xWallL);
		rays.add(xWallR);
		rays.add(yWallT);
		rays.add(yWallB);
		rays.add(xCentre);
		rays.add(yCentre);
	}
	
	boolean runs = true;

	public View() {
		super();
		this.bodies = new ArrayList<Body>();

		MouseListener l = new MouseListener() {

			float x = 0, y = 0;

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getButton() == 2) {
					runs = true;
					return;
				}
				if (e.getButton() != 1)
					return;
				float newX = x + reference.x - getWidth() / 2;
				float newY = y + reference.y - getHeight() / 2;

				float velConstant = 0.03f;
				float newXv = (e.getX() - x) * velConstant + reference.xv;
				float newYv = (e.getY() - y) * velConstant + reference.yv;

				Body newBody = new Body(newX, newY, newXv, newYv, 20, 7);
				bodies.add(newBody);
			}

			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == 2) {
					runs = false;
					return;
				}
				if (e.getButton() == 1) {
					x = e.getX();
					y = e.getY();
				} else {
					float xr = e.getX() + reference.x - getWidth() / 2;
					float yr = e.getY() + reference.y - getHeight() / 2;
					for (Body b : getBodies()) {

						if (Math.abs(b.x - xr) < b.size + 15)
							if (Math.abs(b.y - yr) < b.size + 15)
								reference = b;
					}
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {

			}

			@Override
			public void mouseEntered(MouseEvent e) {

			}

			@Override
			public void mouseClicked(MouseEvent e) {

			}
		};
		addMouseListener(l);

		Color c1 = new Color(155, 155, 155);
		Color c2 = new Color(50, 50, 100);
		GravitySystem.generateOrbitals(bodies, 1000, 512, 768f / 2f, 100, 100, c1, c2);
		CollisionSystem.simulate(bodies, this);

		c1 = new Color(155, 55, 155);
		c2 = new Color(100, 0, 10);
		GravitySystem.generateOrbitals(bodies, 1000, 512, 768f / 2f, 200, 100, c1, c2);
		CollisionSystem.simulate(bodies, this);

		c1 = new Color(155, 155, 155);
		c2 = new Color(100, 100, 0);
		GravitySystem.generateOrbitals(bodies, 1000, 512, 768f / 2f, 300, 100, c1, c2);
		CollisionSystem.simulate(bodies, this);

		reference = new Body(512, 768 / 2, 0, 0, 120, 10);
		reference.filled = true;
		reference.color = new Color(255, 255, 255);
		reference.color = new Color(255, 255, 155);
		bodies.add(reference);	
	}

	public List<Body> getBodies() {
		return bodies;
	}

	public static float[] getExtremes(List<Body> bodies) {
		Body fst = bodies.get(0);
		float[] vals = { fst.x, fst.x, fst.y, fst.y };
		for (Body b : bodies) {
			if (b.x < vals[0]) {
				vals[0] = b.x;
			}
			if (b.x > vals[1]) {
				vals[1] = b.x;
			}
			if (b.y < vals[2]) {
				vals[2] = b.y;
			}
			if (b.y > vals[3]) {
				vals[3] = b.y;
			}
		}
		float radius = Math.max(vals[1]-vals[0], vals[3]-vals[2]);
		return new float[] {(vals[0]+vals[1])/2-radius, (vals[0]+vals[1])/2+radius, (vals[2]+vals[3])/2-radius, (vals[2]+vals[3])/2+radius};
	}

	public void mainLoop() {

		while (true) {
		
			long start = System.currentTimeMillis();
			
			CollisionSystem.simulate(bodies, this);
			if(runs) {
				GravitySystemGPUExecutor.execute(bodies);
			}
			
			/*
			GravitySystem.simulate(bodies);
			for (Body b : bodies) {
				b.update();
			}
			*/

			repaint();
			try {
				Thread.sleep(Math.max(0, 20 - (System.currentTimeMillis() - start)));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
		}
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(new Color(0, 0, 0));
		g.fillRect(0, 0, getWidth(), getHeight());

		g.setColor(new Color(0, 255, 0));
		float[] extremes = View.getExtremes(bodies);
		new BodyTree(bodies, extremes[0], extremes[1], extremes[2], extremes[3]);

		for (int j = 0; j < rays.size() / 6; j++) {
			int i = 6 * j;
			float xWallL = rays.get(i) - reference.x + (getWidth() / 2);
			float xWallR = rays.get(i + 1) - reference.x + (getWidth() / 2);
			float yWallT = rays.get(i + 2) - reference.y + (getHeight() / 2);
			float yWallB = rays.get(i + 3) - reference.y + (getHeight() / 2);
			float xCentre = rays.get(i + 4) - reference.x + (getWidth() / 2);
			float yCentre = rays.get(i + 5) - reference.y + (getHeight() / 2);

			g.drawLine((int) xWallL, (int) yCentre, (int) xWallR, (int) yCentre);
			g.drawLine((int) xCentre, (int) yWallT, (int) xCentre, (int) yWallB);
		}
		rays.clear();

		for (Body b : getBodies()) {
			b.draw(g, (int) reference.x - (getWidth() / 2), (int) reference.y - (getHeight() / 2));
		}

	}

}
