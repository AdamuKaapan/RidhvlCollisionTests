import java.util.ArrayList;
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
import com.osreboot.ridhvl.tile.collection.HvlSimpleTile;

public class Main extends HvlTemplateInteg2D {

	private HvlCoord playerPos;

	private HvlCoord projPos;
	private HvlCoord projVel;

	public static final float playerMovementSpeed = 128.0f, projectileSpeed = 128.0f;

	public static final List<Integer> ul = new ArrayList<>(), ur = new ArrayList<>(), lr = new ArrayList<>(), ll = new ArrayList<>();

	static {
		ul.add(25);
		ul.add(28);
		ur.add(26);
		ur.add(29);
		ll.add(33);
		ll.add(36);
		lr.add(34);
		lr.add(37);
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

		playerPos = new HvlCoord(0, 0);
		
		projPos = new HvlCoord(0, 0);
		projVel = new HvlCoord(0, 0);
	}

	@Override
	public void update(float delta) {
		
		HvlCoord vel = new HvlCoord(HvlInputSeriesAction.HORIZONTAL.getCurrentOutput(), HvlInputSeriesAction.VERTICAL.getCurrentOutput()).normalize().fixNaN().mult(playerMovementSpeed);
		
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
		HvlPainter2D.hvlDrawQuad(projPos.x - 4, projPos.y - 4, 8, 8, Color.pink);
	}

	private HvlCoord raytrace(HvlCoord start, HvlCoord end, HvlCoord segStart, HvlCoord segEnd) {
		HvlCoord tr = new HvlCoord(0, 0);

		HvlCoord b = end.subtractNew(start);
		HvlCoord d = segEnd.subtractNew(segStart);
		float bDotDPerp = b.x * d.y - b.y * d.x;

		if (bDotDPerp == 0)
			return null;

		HvlCoord c = segStart.subtractNew(start);
		float t = (c.x * d.y - c.y * d.x) / bDotDPerp;
		if (t < 0 || t > 1)
			return null;

		float u = (c.x * b.y - c.y * b.x) / bDotDPerp;
		if (u < 0 || u > 1)
			return null;

		tr = start.addNew(b.multNew(t));

		if (HvlMath.distance(tr.x, tr.y, start.x, start.y) > HvlMath.distance(start.x, start.y, end.x, end.y))
			return null;

		return tr;
	}

	private void applyBounce(HvlCoord start, HvlCoord end, HvlCoord segStart, HvlCoord segEnd, HvlCoord pos, HvlCoord vel, float bounce) {

		HvlCoord coll = raytrace(start, end, segStart, segEnd);

		if (coll != null) {
			float angle = (float) Math.atan2(pos.y - coll.y, pos.x - coll.x);

			float normal = (float) ((Math.PI / 2) + Math.atan2(segEnd.y - segStart.y, segEnd.x - segStart.x) % Math.PI);

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

	private void applyCollision(float delta, HvlCoord pos, HvlCoord vel, float bounce) {
		int tileX = map.toTileX(pos.x);
		int tileY = map.toTileY(pos.y);
		
		for (int x = -2; x < 3; x++) {
			for (int y = -2; y < 3; y++) {

				if (tileX + x < 0 || tileX + x >= map.getLayer(1).getMapWidth() || tileY + y < 0 || tileY + y >= map.getLayer(1).getMapHeight())
					continue;

				if (!map.isTileInLocation(tileX + x, tileY + y, 1))
					continue;

				HvlSimpleTile tile = (HvlSimpleTile) map.getLayer(1).getTile(tileX + x, tileY + y);

				if (tile == null)
					continue;

				HvlCoord start = new HvlCoord(pos.x, pos.y);
				HvlCoord end = new HvlCoord(pos.x + (vel.x * delta), projPos.y + (projVel.y * delta));

				if (ul.contains(tile.getTile())) {

					applyBounce(start, end, new HvlCoord(map.toWorldX(tileX + x + 1), map.toWorldY(tileY + y)),
							new HvlCoord(map.toWorldX(tileX + x), map.toWorldY(tileY + y + 1)), pos, vel, bounce);
				} else if (ur.contains(tile.getTile())) {

					applyBounce(start, end, new HvlCoord(map.toWorldX(tileX + x), map.toWorldY(tileY + y)),
							new HvlCoord(map.toWorldX(tileX + x + 1), map.toWorldY(tileY + y + 1)), pos, vel, bounce);
				} else if (lr.contains(tile.getTile())) {

					applyBounce(start, end, new HvlCoord(map.toWorldX(tileX + x), map.toWorldY(tileY + y + 1)),
							new HvlCoord(map.toWorldX(tileX + x + 1), map.toWorldY(tileY + y)), pos, vel, bounce);
				} else if (ll.contains(tile.getTile())) {

					applyBounce(start, end, new HvlCoord(map.toWorldX(tileX + x), map.toWorldY(tileY + y)),
							new HvlCoord(map.toWorldX(tileX + x + 1), map.toWorldY(tileY + y + 1)), pos, vel, bounce);
				} else // Flat collision
				{
					if (x != 0 || y != 0) {
						HvlCoord wallStart = null, wallEnd = null;

						if (x < 0) {
							wallStart = new HvlCoord(map.toWorldX(tileX + x + 1), map.toWorldY(tileY + y));
							wallEnd = new HvlCoord(map.toWorldX(tileX + x + 1), map.toWorldY(tileY + y + 1));
						}
						if (x > 0) {
							wallStart = new HvlCoord(map.toWorldX(tileX + x), map.toWorldY(tileY + y));
							wallEnd = new HvlCoord(map.toWorldX(tileX + x), map.toWorldY(tileY + y + 1));
						}
						if (y < 0) {
							wallStart = new HvlCoord(map.toWorldX(tileX + x), map.toWorldY(tileY + y + 1));
							wallEnd = new HvlCoord(map.toWorldX(tileX + x + 1), map.toWorldY(tileY + y + 1));
						}
						if (y > 0) {
							wallStart = new HvlCoord(map.toWorldX(tileX + x), map.toWorldY(tileY + y));
							wallEnd = new HvlCoord(map.toWorldX(tileX + x + 1), map.toWorldY(tileY + y));
						}

						applyBounce(start, end, wallStart, wallEnd, pos, vel, bounce);
					}
				}
			}
		}
	}
}
