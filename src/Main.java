import org.lwjgl.input.Keyboard;
import org.newdawn.slick.Color;

import com.osreboot.ridhvl.HvlCoord;
import com.osreboot.ridhvl.HvlMath;
import com.osreboot.ridhvl.display.collection.HvlDisplayModeDefault;
import com.osreboot.ridhvl.painter.HvlCamera;
import com.osreboot.ridhvl.painter.HvlCamera.HvlCameraAlignment;
import com.osreboot.ridhvl.painter.painter2d.HvlPainter2D;
import com.osreboot.ridhvl.template.HvlTemplateInteg2D;

public class Main extends HvlTemplateInteg2D {

	private float x1, y1, x2, y2;

	private final float wallX = 0.0f, wallY = 0.0f, wallSize = 512.0f;

	private float projX, projY;
	private float projVelX, projVelY;

	private float projTime;

	public static final float projectileLife = 5.0f;

	public static final float movementSpeed = 128.0f, projectileSpeed = 128.0f;

	public Main() {
		super(60, 1280, 720, "Ridhvl Collision Tests", new HvlDisplayModeDefault());
	}

	public static void main(String[] args) {
		new Main();
	}

	@Override
	public void initialize() {
		getTextureLoader().loadResource("Curve");

		HvlCamera.setPosition(wallSize / 2, wallSize / 2);
		HvlCamera.setAlignment(HvlCameraAlignment.CENTER);
	}

	@Override
	public void update(float delta) {
		projTime += delta;

		if (projTime >= projectileLife) {
			projTime = 0.0f;
			projX = x1;
			projY = y1;
			HvlCoord dir = new HvlCoord(x2 - x1, y2 - y1).normalize();
			projVelX = dir.x;
			projVelY = dir.y;
		}
		
		if (isCollided(projX + (projVelX * delta * projectileSpeed), projY + (projVelY * delta * projectileSpeed)))
		{
			HvlCoord contactPoint = new HvlCoord(projX + (projVelX * delta * projectileSpeed), projY + (projVelY * delta * projectileSpeed));
			contactPoint.subtract(wallX, wallY);
			
//			System.out.println(contactPoint.x + ", " + contactPoint.y);
			
			float angleCenterContact = (float) Math.atan2(wallSize - contactPoint.y, wallSize - contactPoint.x);
			float angleVelContact = (float) Math.atan2(projY - wallY - contactPoint.y, projX - wallX - contactPoint.x);
//			System.out.println(Math.toDegrees(angleCenterContact - angleVelContact));
			
			float angleOfReflection = (float) ((angleVelContact - angleCenterContact));
			System.out.println((Math.toDegrees((angleOfReflection)) + 360) % 360);
			projVelX = (projVelX - wallX) * -(float)Math.cos(angleOfReflection) + (wallX);
			projVelY = (projVelY - wallY) * -(float)Math.sin(angleOfReflection) + (wallY);
		}

		projX += projVelX * delta * projectileSpeed;
		projY += projVelY * delta * projectileSpeed;

		float h1 = 0.0f, v1 = 0.0f;
		if (Keyboard.isKeyDown(Keyboard.KEY_D))
			h1 += 1.0f;
		if (Keyboard.isKeyDown(Keyboard.KEY_A))
			h1 -= 1.0f;
		if (Keyboard.isKeyDown(Keyboard.KEY_S))
			v1 += 1.0f;
		if (Keyboard.isKeyDown(Keyboard.KEY_W))
			v1 -= 1.0f;

		x1 += h1 * delta * movementSpeed;
		y1 += v1 * delta * movementSpeed;

		float h2 = 0.0f, v2 = 0.0f;
		if (Keyboard.isKeyDown(Keyboard.KEY_RIGHT))
			h2 += 1.0f;
		if (Keyboard.isKeyDown(Keyboard.KEY_LEFT))
			h2 -= 1.0f;
		if (Keyboard.isKeyDown(Keyboard.KEY_DOWN))
			v2 += 1.0f;
		if (Keyboard.isKeyDown(Keyboard.KEY_UP))
			v2 -= 1.0f;

		x2 += h2 * delta * movementSpeed;
		y2 += v2 * delta * movementSpeed;

		// System.out.println(512 * 512 < Math.pow(projX - 1, 2) +
		// (Math.pow(projY - 1, 2)));

		draw(delta);
	}

	public void draw(float delta) {
		HvlPainter2D.hvlDrawQuad(wallX, wallY, wallSize, wallSize, getTexture(0));
		HvlPainter2D.hvlDrawQuad(x1 - 8, y1 - 8, 16, 16, Color.cyan);
		HvlPainter2D.hvlDrawQuad(x2 - 8, y2 - 8, 16, 16, Color.magenta);
		HvlPainter2D.hvlDrawQuad(projX - 4, projY - 4, 8, 8, isCollided(projX, projY) ? Color.red : Color.green);
	}

	private boolean isCollided(float x, float y) {
		return HvlMath.distance(wallX + wallSize, wallY + wallSize, x, y) > wallSize && x > wallX && x < wallX + wallSize && y > wallY
				&& projY < y + wallSize;
	}
}
