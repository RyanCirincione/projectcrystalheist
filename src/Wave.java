import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Wave
{
	int time;
	public HashMap<Integer, ArrayList<Bullet>> bullets;
	
	public Wave()
	{
		time = 0;
		bullets = new HashMap<Integer, ArrayList<Bullet>>();
	}
	
	public Wave(HashMap<Integer, ArrayList<Bullet>> list)
	{
		time = 0;
		bullets = new HashMap<Integer, ArrayList<Bullet>>(list);
	}
	
	public void put(int t, Bullet... b)
	{
		bullets.put(t, new ArrayList<Bullet>(Arrays.asList(b)));
	}
	
	public ArrayList<Bullet> tick()
	{
		time++;
		return bullets.get(time);
	}
	
	public static Wave flowey_circle_wave()
	{
		Wave wave = new Wave();
		final int RADIUS = 15, RANGE = Game.S_WIDTH/2 - RADIUS - 1;
		for(int theta = 0; theta < 360; theta+=5)
		{
			Bullet b = new Bullet(true, new Ellipse2D.Double(
					Math.cos(Math.toRadians(theta)) * RANGE - RADIUS + Game.S_WIDTH/2,
					Math.sin(Math.toRadians(theta)) * RANGE - RADIUS + Game.S_HEIGHT/2,
					RADIUS*2, RADIUS*2), 500 + theta/10){
				int time = 0;
				public void tick(){
					time++;
					if(time*10 > (Integer)data.get("theta") + 360*5){
						((Ellipse2D.Double)shape).x-=3*
								Math.cos(Math.toRadians((Integer)data.get("theta")));
						((Ellipse2D.Double)shape).y-=3*
								Math.sin(Math.toRadians((Integer)data.get("theta")));
					}
				}
			};
			b.data.put("theta", theta);
			wave.put(theta/2+15, b);
		}
		wave.put(670);
		
		return wave;
	}
	
	public static Wave stripe_move_right_wave()
	{
		Wave wave = new Wave();
		final int WIDTH = 70;
		for(int i = -7; i < Game.S_WIDTH/WIDTH; i++)
		{
			Bullet b = new Bullet(i%2==0, new Rectangle(i*WIDTH, -Game.S_HEIGHT, WIDTH, Game.S_HEIGHT),
					500){
				public void tick(){
					if(((Rectangle)shape).y < 0)
						((Rectangle)shape).y+=5;
					else
						((Rectangle)shape).x++;
				}
			};
			wave.put(i+5, b);
		}
		wave.put(550);
		
		return wave;
	}
	
	public static Wave random_grow_circles_wave()
	{
		Wave wave = new Wave();
		for(int i = 0; i < 10; i++)
		{
			Bullet b = new Bullet(Math.random()<0.5, new Ellipse2D.Double(
					Math.random()*(Game.S_WIDTH-Game.BOX_SIDE_BUFFER*2) + Game.BOX_SIDE_BUFFER,
					Math.random()*(Game.S_HEIGHT-Game.BOX_SIDE_BUFFER*2) + Game.BOX_SIDE_BUFFER,
					1, 1), 100){
				public void tick(){
					if(life > 50)
					{
						((Ellipse2D.Double)shape).x--;
						((Ellipse2D.Double)shape).y--;
						((Ellipse2D.Double)shape).width+=2;
						((Ellipse2D.Double)shape).height+=2;
					}
					else
					{
						((Ellipse2D.Double)shape).x++;
						((Ellipse2D.Double)shape).y++;
						((Ellipse2D.Double)shape).width-=2;
						((Ellipse2D.Double)shape).height-=2;
					}
				}
			};
			wave.put(i*30, b);
		}
		
		wave.put(300);
		
		return wave;
	}
}
