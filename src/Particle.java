import org.newdawn.slick.Color;

import com.osreboot.ridhvl.HvlColorUtil;
import com.osreboot.ridhvl.HvlCoord;
import com.osreboot.ridhvl.HvlMath;
import com.osreboot.ridhvl.painter.painter2d.HvlPainter2D;


public class Particle {
	private Color color;
	
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
		color = HvlColorUtil.lerpColor(Color.blue, Color.red, Math.min(3000, Math.max(0, (float)Main.parts.size()))/3000);
		
		try {
			Main.applyCollision(delta, pos.addNew(4, 0), vel, 1.0f);
			Main.applyCollision(delta, pos.addNew(-4, 0), vel, 1.0f);
			Main.applyCollision(delta, pos.addNew(0, -4), vel, 1.0f);
			Main.applyCollision(delta, pos.addNew(0, 4), vel, 1.0f);
		} catch (Exception e) {
			e.printStackTrace();
		}
		pos.add(vel.x * delta, vel.y * delta);
	}
	
	public void draw(float delta)
	{
		HvlPainter2D.hvlDrawQuad(pos.x - 4, pos.y - 4, 8, 8, color);
	}
}
