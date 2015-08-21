import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.newdawn.slick.Color;

import com.osreboot.ridhvl.HvlCoord;
import com.osreboot.ridhvl.HvlMath;
import com.osreboot.ridhvl.HvlTimer;
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

	public static List<Particle> parts;
	// private Particle part;
	// private HvlCoord projPos;
	// private HvlCoord projVel;

	public static final float playerMovementSpeed = 128.0f, projectileSpeed = 1024.0f;

	static {
		HvlTilemapCollisionUtil.registerCornerSet(25, 26, 33, 34);
		HvlTilemapCollisionUtil.registerCornerSet(28, 29, 36, 37);
	}

	private static HvlLayeredTileMap map;

	public Main() {
		super(60, 1280, 720, "Ridhvl Collision Tests", new HvlDisplayModeDefault());
	}

	public static void main(String[] args) {
		new Main();
	}

	@Override
	public void initialize() {
		getTimer().setMaxDelta(HvlTimer.MD_TWENTIETH);

		getTextureLoader().loadResource("Curve");
		getTextureLoader().loadResource("Slope");
		getTextureLoader().loadResource("Tilemap");

		parts = new ArrayList<>();

		map = HvlLayeredTileMap.load("TestMap", true, 0, 0, 64, 64, getTexture(2));

		playerPos = new HvlCoord((map.getTileWidth() / 2) + 5 * map.getTileWidth(), (map.getTileHeight() / 2) + 5 * map.getTileHeight());
	}

	@Override
	public void update(float delta) {
		
		HvlCoord vel = new HvlCoord(HvlInputSeriesAction.HORIZONTAL.getCurrentOutput(), HvlInputSeriesAction.VERTICAL.getCurrentOutput()).normalize().fixNaN()
				.mult(playerMovementSpeed);

		List<LineSegment> lines = HvlTilemapCollisionUtil.getAllNearbySides(map, playerPos.x, playerPos.y, 1, 1);

		for (LineSegment seg : lines) {
			HvlCoord coll = HvlMath.raytrace(playerPos, playerPos.addNew(vel.x * delta, vel.y * delta), seg.start, seg.end);

			if (coll != null) {
				vel = seg.end.subtractNew(seg.start).mult(coll.subtractNew(playerPos).dot(seg.end.subtractNew(seg.start))).normalize().fixNaN()
						.mult(playerMovementSpeed);
			}
		}

		playerPos.add(vel.multNew(delta));

		if (Mouse.isButtonDown(0)) {
			for (int i = 0; i < 8; i++) {
				float mouseAngle = (float) Math.atan2(HvlCursor.getCursorY() + HvlCamera.getY() - (Display.getHeight() / 2) - playerPos.y, HvlCursor.getCursorX()
						+ HvlCamera.getX() - (Display.getWidth() / 2) - playerPos.x);

				float x = (float) Math.cos(i * (Math.PI / 4) + mouseAngle);
				float y = (float) Math.sin(i * (Math.PI / 4) + mouseAngle);

				parts.add(new Particle(new HvlCoord(playerPos.x, playerPos.y), new HvlCoord(x, y).normalize().mult(projectileSpeed)));
				// parts.add(new Particle(new HvlCoord(playerPos.x,
				// playerPos.y), new HvlCoord(HvlCursor.getCursorX() +
				// HvlCamera.getX() - (Display.getWidth() / 2)
				// - playerPos.x, HvlCursor.getCursorY() + HvlCamera.getY() -
				// (Display.getHeight() / 2) -
				// playerPos.y).normalize().mult(projectileSpeed)));
			}
		}

		for (Particle p : parts) {
			p.update(delta);
		}

		HvlCamera.setPosition(playerPos.x, playerPos.y);
		HvlCamera.setAlignment(HvlCameraAlignment.CENTER);

		draw(delta);
	}

	public void draw(float delta) {
		map.draw(delta);
		HvlPainter2D.hvlDrawQuad(playerPos.x - 8, playerPos.y - 8, 16, 16, Color.cyan);
		// HvlPainter2D.hvlDrawQuad(x2 - 8, y2 - 8, 16, 16, Color.magenta);
		for (Particle p : parts) {
			p.draw(delta);
		}

		List<LineSegment> segs = HvlTilemapCollisionUtil.getAllNearbySides(map, playerPos.x, playerPos.y, 1, 1);

		for (LineSegment seg : segs) {
			HvlPainter2D.hvlDrawLine(seg.start.x, seg.start.y, seg.end.x, seg.end.y, Color.red);
		}
	}

	public static void applyCollision(float delta, HvlCoord pos, HvlCoord vel, float bounce) throws Exception {
		for (int i = 0; i < 100; i++) {
			List<LineSegment> segs = HvlTilemapCollisionUtil.getAllNearbySides(map, pos.x, pos.y, 1, 1);

			Map<HvlCoord, LineSegment> colls = new HashMap<>();

			for (LineSegment seg : segs) {
				HvlCoord coll = HvlMath.raytrace(pos, new HvlCoord(pos.x + (vel.x * delta), pos.y + (vel.y * delta)), seg.start, seg.end);

				if (coll != null) {
					colls.put(coll, seg);
				}
			}
			if (colls.isEmpty())
				return;

			final HvlCoord tempPos = pos.clone();

			List<HvlCoord> keys = new ArrayList<>();
			for (HvlCoord key : colls.keySet()) {
				if (key == null)
					continue;

				keys.add(key);
			}

			Collections.sort(keys, new Comparator<HvlCoord>() {
				@Override
				public int compare(HvlCoord arg0, HvlCoord arg1) {
					return (int) Math.signum(HvlMath.distance(arg0.x, arg0.y, tempPos.x, tempPos.y) - HvlMath.distance(arg1.x, arg1.y, tempPos.x, tempPos.y));
				}
			});

			HvlCoord coll = keys.get(0);
			LineSegment seg = colls.get(coll);

			float angle = (float) Math.atan2(pos.y - coll.y, pos.x - coll.x);

			float normal = (float) ((Math.PI / 2) + Math.atan2(seg.end.y - seg.start.y, seg.end.x - seg.start.x) % Math.PI);

			float angleOfReflection = normal - angle;

			float oldVel = new HvlCoord(vel.x, vel.y).length();

			float newAngle = angle + 2 * angleOfReflection;

			HvlCoord newDir = new HvlCoord((float) Math.cos(newAngle), (float) Math.sin(newAngle)).normalize().mult(oldVel);
			vel.x = newDir.x * bounce;
			vel.y = newDir.y * bounce;
			HvlCoord mod = vel.normalizeNew();

			pos.x = coll.x + (mod.x * 0.001f);
			pos.y = coll.y + (mod.y * 0.001f);
		}
		throw new Exception("Looped too many times.");
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
