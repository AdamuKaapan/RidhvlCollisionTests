import org.newdawn.slick.Color;

import com.osreboot.ridhvl.HvlCoord;
import com.osreboot.ridhvl.painter.painter2d.HvlPainter2D;


public class Particle {
	private HvlCoord pos;
	private HvlCoord vel;
	
	public Particle(float x, float y, float xVel, float yVel)
	{
		pos = new HvlCoord(x, y);
		vel = new HvlCoord(xVel, yVel);
	}
	
	public Particle(HvlCoord pos, HvlCoord vel)
	{
		this.pos = pos.clone();
		this.vel = vel.clone();
	}
	
	public void update(float delta)
	{
		Main.applyCollision(delta, pos, vel, 1.0f);
		pos.add(vel.x * delta, vel.y * delta);
	}
	
	public void draw(float delta)
	{
		HvlPainter2D.hvlDrawQuad(pos.x - 4, pos.y - 4, 8, 8, Color.magenta);
	}
}
