import com.osreboot.ridhvl.display.collection.HvlDisplayModeDefault;
import com.osreboot.ridhvl.template.HvlTemplateInteg2D;


public class Main extends HvlTemplateInteg2D {

	public Main()
	{
		super(60, 1280, 720, "Ridhvl Collision Tests", new HvlDisplayModeDefault());
	}
	
	public static void main(String[] args) {
		new Main();
	}

	@Override
	public void initialize() {
		
	}

	@Override
	public void update(float delta) {
		
	}

}
