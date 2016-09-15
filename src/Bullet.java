import java.awt.Shape;
import java.util.HashMap;

public class Bullet
{
	public boolean isRed;
	public Shape shape;
	public HashMap<String, Object> data;
	public int life;
	
	public Bullet(boolean red, Shape s, int l)
	{
		isRed = red;
		shape = s;
		life = l;
		data = new HashMap<String, Object>();
	}
	
	public void update()
	{
		if(life > 0)
			life--;
		this.tick();
	}
	
	public void tick()
	{
		
	}
}
