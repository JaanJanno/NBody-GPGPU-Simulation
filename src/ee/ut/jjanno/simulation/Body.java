package ee.ut.jjanno.simulation;

import java.awt.Color;
import java.awt.Graphics;

public class Body implements Drawable{
	
	public float x, y;
	public float xv = 0, yv = 0;
	public float xa = 0, ya = 0;
	public float mass;
	public float size = 3;
	
	public boolean filled = false;
	public Color color = new Color(255, 185, (int) (Math.random() * 155));
	public Color fillColor = new Color(255, 185, (int)(Math.random()*155));
	
	public Body(float x, float y, float mass) {
		super();
		this.x = x;
		this.y = y;
		this.mass = mass;
	}	
	
	public Body(float x, float y, float xv, float yv, float mass) {
		super();
		this.x = x;
		this.y = y;
		this.xv = xv;
		this.yv = yv;
		this.mass = mass;
	}
	
	public Body(float x, float y, float xv, float yv, float mass, float size) {
		super();
		this.x = x;
		this.y = y;
		this.xv = xv;
		this.yv = yv;
		this.mass = mass;
		this.size = size;
	}

	public void accelerate(float xb, float yb) {
		xa += xb / mass;
		ya += yb / mass;
	}
	
	public void update() {
		xv += xa;
		yv += ya;
		x += xv;
		y += yv;
		
		xa = 0;
		ya = 0;
	}

	@Override
	public void draw(Graphics g, int xref, int yref) {
		int size = (int)this.size;	
		g.setColor(color);			
		g.drawRect((int)x - xref - size/2, (int)y - yref - size/2, size, size);
		
		if(filled) {
			g.setColor(fillColor);	
			g.fillRect((int)x - xref - size/2 + 1, (int)y - yref - size/2 + 1, size - 1, size - 1);
		}
	}

}
