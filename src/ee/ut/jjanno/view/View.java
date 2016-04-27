package ee.ut.jjanno.view;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.JPanel;

import ee.ut.jjanno.gpusimulation.BodyTree;
import ee.ut.jjanno.gpusimulation.GravitySystemGPUExecutor;
import ee.ut.jjanno.gpusimulation.IndexSequence;
import ee.ut.jjanno.simulation.Body;
import ee.ut.jjanno.simulation.GravitySystem;

@SuppressWarnings("serial")
public class View extends JPanel implements KeyListener {

	private static final int INIT_BODIES = 5000;
	private static final int FPS_CLAMP = 10;
	
	Lock bLock = new ReentrantLock();
	List<Body> bodies;

	List<Body> drawBodies;
	public Body reference;
	float zoom = 1.0f;
	boolean qTreeVisualize = false;
	
	private float computeTime = 0f;
	
	private double fps = 0;
	private long lastFrame;
	private int cyc = 1;

	private static List<Float> rays = new ArrayList<>();

	public static void addRay(Float xWallL, Float xWallR, Float yWallT, Float yWallB, Float xCentre, Float yCentre) {
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
		this.drawBodies = new ArrayList<Body>();

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
				float newX = x / zoom + reference.x - getWidth() / 2 / zoom;
				float newY = y / zoom + reference.y - getHeight() / 2 / zoom;

				float velConstant = 0.03f;
				float newXv = (e.getX() / zoom - x / zoom) * velConstant + reference.xv;
				float newYv = (e.getY() / zoom - y / zoom) * velConstant + reference.yv;

				Body newBody = new Body(newX, newY, newXv, newYv, 20, 7);
				bLock.lock();
				bodies.add(newBody);
				bLock.unlock();
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
					float xr = e.getX() / zoom + reference.x - getWidth() / 2 / zoom;
					float yr = e.getY() / zoom + reference.y - getHeight() / 2 / zoom;
					for (Body b : getBodies()) {

						if (Math.abs(b.x - xr) < b.size + 15 / zoom)
							if (Math.abs(b.y - yr) < b.size + 15 / zoom)
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

		addMouseWheelListener(new MouseWheelListener() {

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				zoom *= Math.pow(0.5, e.getWheelRotation());

			}
		});

		addKeyListener(this);
		setFocusable(true);

		Color c1 = new Color(155, 155, 155);
		Color c2 = new Color(50, 50, 100);
		GravitySystem.generateOrbitals(bodies, INIT_BODIES / 3, 512, 768f / 2f, 1000, 1500, c1, c2);

		c1 = new Color(155, 55, 155);
		c2 = new Color(100, 0, 10);
		GravitySystem.generateOrbitals(bodies, INIT_BODIES / 3, 512, 768f / 2f, 2000, 1500, c1, c2);

		c1 = new Color(155, 155, 155);
		c2 = new Color(100, 100, 0);
		GravitySystem.generateOrbitals(bodies, INIT_BODIES / 3, 512, 768f / 2f, 3000, 1500, c1, c2);

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
		float radius = Math.max(vals[1] - vals[0], vals[3] - vals[2]) / 2 + 1;
		return new float[] { (vals[0] + vals[1]) / 2 - radius, (vals[0] + vals[1]) / 2 + radius,
				(vals[2] + vals[3]) / 2 - radius, (vals[2] + vals[3]) / 2 + radius };
	}

	public void mainLoop() {

		while (true) {

			long start = System.currentTimeMillis();

			if (runs) {
				bLock.lock();
				long startC = System.currentTimeMillis();
				GravitySystemGPUExecutor.executeAdvancedPlusParallelTree(bodies);
				computeTime = System.currentTimeMillis() - startC;

				List<Body> newDrawables = new ArrayList<Body>();
				for (Body b : bodies) {
					newDrawables.add(b.clone());
				}
				drawBodies = newDrawables;
				bLock.unlock();
			}

			repaint();
			try {
				Thread.sleep(Math.max(0, 16 - (System.currentTimeMillis() - start)));
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

		if (qTreeVisualize) {
			float[] extremes = View.getExtremes(bodies);

			IndexSequence seq = new IndexSequence(1);
			new BodyTree(bodies, seq, extremes[0], extremes[1], extremes[2], extremes[3], true);

			g.drawLine((int) (extremes[0] * zoom - reference.x * zoom + (getWidth() / 2)),
					(int) (extremes[2] * zoom - reference.y * zoom + (getHeight() / 2)),
					(int) (extremes[0] * zoom - reference.x * zoom + (getWidth() / 2)),
					(int) (extremes[3] * zoom - reference.y * zoom + (getHeight() / 2)));
			
			g.drawLine((int) (extremes[1] * zoom - reference.x * zoom + (getWidth() / 2)),
					(int) (extremes[2] * zoom - reference.y * zoom + (getHeight() / 2)),
					(int) (extremes[1] * zoom - reference.x * zoom + (getWidth() / 2)),
					(int) (extremes[3] * zoom - reference.y * zoom + (getHeight() / 2)));
			
			g.drawLine((int) (extremes[0] * zoom - reference.x * zoom + (getWidth() / 2)),
					(int) (extremes[2] * zoom - reference.y * zoom + (getHeight() / 2)),
					(int) (extremes[1] * zoom - reference.x * zoom + (getWidth() / 2)),
					(int) (extremes[2] * zoom - reference.y * zoom + (getHeight() / 2)));
			
			g.drawLine((int) (extremes[0] * zoom - reference.x * zoom + (getWidth() / 2)),
					(int) (extremes[3] * zoom - reference.y * zoom + (getHeight() / 2)),
					(int) (extremes[1] * zoom - reference.x * zoom + (getWidth() / 2)),
					(int) (extremes[3] * zoom - reference.y * zoom + (getHeight() / 2)));

			for (int j = 0; j < rays.size() / 6; j++) {
				int i = 6 * j;
				float xWallL = rays.get(i) * zoom - reference.x * zoom + (getWidth() / 2);
				float xWallR = rays.get(i + 1) * zoom - reference.x * zoom + (getWidth() / 2);
				if (xWallR - xWallL < 4)
					continue;

				float yWallT = rays.get(i + 2) * zoom - reference.y * zoom + (getHeight() / 2);
				float yWallB = rays.get(i + 3) * zoom - reference.y * zoom + (getHeight() / 2);
				float xCentre = rays.get(i + 4) * zoom - reference.x * zoom + (getWidth() / 2);
				float yCentre = rays.get(i + 5) * zoom - reference.y * zoom + (getHeight() / 2);

				g.drawLine((int) xWallL, (int) yCentre, (int) xWallR, (int) yCentre);
				g.drawLine((int) xCentre, (int) yWallT, (int) xCentre, (int) yWallB);
				((Graphics2D) g).drawString("QTree nodes: " + Integer.toString(seq.getLastIndex()), 10, 80);
			}
			rays.clear();
		}

		for (Body b : drawBodies) {
			b.draw(g, (int) (reference.x * zoom - (getWidth() / 2)), (int) (reference.y * zoom - (getHeight() / 2)),
					zoom);
		}

		((Graphics2D) g).drawString("Zoom: " + Float.toString(zoom), 10, 20);
		((Graphics2D) g).drawString("Compute time: " + Float.toString(computeTime), 10, 40);
		
		if(cyc % FPS_CLAMP == 0) {
			long thisFrame = System.nanoTime();
			fps = 1.0 / (thisFrame - lastFrame)  * 1000000000 * FPS_CLAMP;
			lastFrame = thisFrame;
			
		}
		
		((Graphics2D) g).drawString("Fps: " + Double.toString(fps), 10, 60);
		cyc++;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		if (e.getKeyCode() == KeyEvent.VK_SPACE) {
			qTreeVisualize = !qTreeVisualize;
		}

		if (e.getKeyChar() == 'k') {

			float xoffset = 1500;
			float yoffset = 7000;

			float xvoffset = 0f;
			float yvoffset = -2f;

			float cMass = 120f;

			bLock.lock();
			Color c1 = new Color(75, 125, 55);
			Color c2 = new Color(10, 60, 10);
			GravitySystem.generateOrbitals(bodies, 1000, 512 + xoffset, 768f / 2f + yoffset, 1000, 1500, c1, c2,
					xvoffset, yvoffset, cMass);

			c1 = new Color(155, 155, 155);
			c2 = new Color(100, 0, 10);
			GravitySystem.generateOrbitals(bodies, 1000, 512 + xoffset, 768f / 2f + yoffset, 2000, 1500, c1, c2,
					xvoffset, yvoffset, cMass);

			c1 = new Color(155, 155, 55);
			c2 = new Color(100, 100, 0);
			GravitySystem.generateOrbitals(bodies, 1000, 512 + xoffset, 768f / 2f + yoffset, 3000, 1500, c1, c2,
					xvoffset, yvoffset, cMass);

			Body sun = new Body(512 + xoffset, 768 / 2 + yoffset, 0, 0, cMass, 10);
			sun.xv += xvoffset;
			sun.yv += yvoffset;
			sun.filled = true;
			bodies.add(sun);
			bLock.unlock();
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub

	}

}
