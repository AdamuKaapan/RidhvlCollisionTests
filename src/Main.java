import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.Color;

import com.osreboot.ridhvl.HvlCoord;
import com.osreboot.ridhvl.HvlMath;
import com.osreboot.ridhvl.display.collection.HvlDisplayModeDefault;
import com.osreboot.ridhvl.input.HvlInputSeriesAction;
import com.osreboot.ridhvl.painter.HvlCamera;
import com.osreboot.ridhvl.painter.HvlCamera.HvlCameraAlignment;
import com.osreboot.ridhvl.painter.HvlCursor;
import com.osreboot.ridhvl.painter.painter2d.HvlPainter2D;
import com.osreboot.ridhvl.template.HvlTemplateInteg2D;
import com.osreboot.ridhvl.tile.HvlLayeredTileMap;
import com.osreboot.ridhvl.tile.HvlTilemapCollisionUtil;
import com.osreboot.ridhvl.tile.HvlTilemapCollisionUtil.LineSegment;

public class Main extends HvlTemplateInteg2D {

	private HvlCoord playerPos;

	private HvlCoord projPos;
	private HvlCoord projVel;

	public static final float playerMovementSpeed = 128.0f, projectileSpeed = 1024.0f;

	static {
		HvlTilemapCollisionUtil.registerCornerSet(25, 26, 33, 34);
		HvlTilemapCollisionUtil.registerCornerSet(28, 29, 36, 37);
	}

	private HvlLayeredTileMap map;

	public Main() {
		super(60, 1280, 720, "Ridhvl Collision Tests", new HvlDisplayModeDefault());
	}

	public static void main(String[] args) {
		new Main();
	}

	@Override
	public void initialize() {
		getTextureLoader().loadResource("Curve");
		getTextureLoader().loadResource("Slope");
		getTextureLoader().loadResource("Tilemap");

		map = HvlLayeredTileMap.load("TestMap", true, 0, 0, 64, 64, getTexture(2));

		playerPos = new HvlCoord((map.getTileWidth() / 2) + 5 * map.getTileWidth(), (map.getTileHeight() / 2) + 5 * map.getTileHeight());

		projPos = new HvlCoord(0, 0);
		projVel = new HvlCoord(0, 0);
	}

	@Override
	public void update(float delta) {

		HvlCoord vel = new HvlCoord(HvlInputSeriesAction.HORIZONTAL.getCurrentOutput(), HvlInputSeriesAction.VERTICAL.getCurrentOutput()).normalize().fixNaN()
				.mult(playerMovementSpeed);

		List<LineSegment> lines = HvlTilemapCollisionUtil.getAllNearbySides(map, playerPos.x, playerPos.y, 1, 1);
		
		for (LineSegment seg : lines)
		{
			HvlCoord coll = HvlMath.raytrace(playerPos, playerPos.addNew(vel.x * delta, vel.y * delta), seg.start, seg.end);
			
			if (coll != null)
			{
				vel = seg.end.subtractNew(seg.start).mult(coll.subtractNew(playerPos).dot(seg.end.subtractNew(seg.start))).normalize().fixNaN().mult(playerMovementSpeed);
			}
		}

		playerPos.add(vel.multNew(delta));

		if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
			projPos = new HvlCoord(playerPos.x, playerPos.y);
			projVel = new HvlCoord(HvlCursor.getCursorX() + HvlCamera.getX() - (Display.getWidth() / 2) - playerPos.x, HvlCursor.getCursorY()
					+ HvlCamera.getY() - (Display.getHeight() / 2) - playerPos.y).normalize().mult(projectileSpeed);
		}

		applyCollision(delta, projPos, projVel, 1.0f);

		projPos.add(projVel.x * delta, projVel.y * delta);

		HvlCamera.setPosition(playerPos.x, playerPos.y);
		HvlCamera.setAlignment(HvlCameraAlignment.CENTER);

		draw(delta);
	}

	public void draw(float delta) {
		map.draw(delta);
		HvlPainter2D.hvlDrawQuad(playerPos.x - 8, playerPos.y - 8, 16, 16, Color.cyan);
		// HvlPainter2D.hvlDrawQuad(x2 - 8, y2 - 8, 16, 16, Color.magenta);
		HvlPainter2D.hvlDrawQuad(projPos.x - 4, projPos.y - 4, 8, 8, Color.magenta);
		
		List<LineSegment> segs = HvlTilemapCollisionUtil.getAllNearbySides(map, playerPos.x, playerPos.y, 1, 1);
		
		for (LineSegment seg : segs)
		{
			HvlPainter2D.hvlDrawLine(seg.start.x, seg.start.y, seg.end.x, seg.end.y, Color.red);
		}
	}

	private void applyCollision(float delta, HvlCoord pos, HvlCoord vel, float bounce) {
		List<LineSegment> segs = HvlTilemapCollisionUtil.getAllNearbySides(map, pos.x, pos.y, 1, 1);

		for (LineSegment seg : segs) {
			HvlCoord coll = HvlMath.raytrace(pos, new HvlCoord(pos.x + (vel.x * delta), pos.y + (vel.y * delta)), seg.start, seg.end);

			if (coll != null) {
				float angle = (float) Math.atan2(pos.y - coll.y, pos.x - coll.x);

				float normal = (float) ((Math.PI / 2) + Math.atan2(seg.end.y - seg.start.y, seg.end.x - seg.start.x) % Math.PI);

				float angleOfReflection = normal - angle;

				float oldVel = new HvlCoord(vel.x, vel.y).length();

				float newAngle = angle + 2 * angleOfReflection;

				HvlCoord newDir = new HvlCoord((float) Math.cos(newAngle), (float) Math.sin(newAngle)).normalize().mult(oldVel);
				pos.x = coll.x;
				pos.y = coll.y;
				vel.x = newDir.x * bounce;
				vel.y = newDir.y * bounce;
			}
		}
	}

	private LineSegment getCollisionIfAny(float delta, HvlCoord pos, HvlCoord vel, HvlCoord out) {
		List<LineSegment> segs = HvlTilemapCollisionUtil.getAllNearbySides(map, pos.x, pos.y, 1, 1);

		for (LineSegment seg : segs) {
			HvlCoord coll = HvlMath.raytrace(pos, new HvlCoord(pos.x + (vel.x * delta), pos.y + (vel.y * delta)), seg.start, seg.end);

			if (coll != null) {
				out.x = coll.x;
				out.y = coll.y;
				return seg;
			}
		}

		return null;
	}
}
