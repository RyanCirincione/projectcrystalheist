import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class Game extends JPanel
{
	private static final long serialVersionUID = 4648172894076113183L;
	
	public static void main(String[] args)
	{
		JFrame frame = new JFrame("Crystal Heist");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setCursor(frame.getToolkit().createCustomCursor(new BufferedImage(3, 3, BufferedImage.TYPE_INT_ARGB),
				new Point(0, 0), "null"));
		
		Game panel = new Game();
		frame.getContentPane().add(panel);
		
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run()
			{
				panel.tick();
				panel.repaint();
			}
		}, 0, 1000/60);
	}
	
	public static final int S_WIDTH = 700, S_HEIGHT = 700, MAX_JUMPS = 1, BOX_SIDE_BUFFER = 50,
			MAX_HEALTH = 400, MAX_PLATFORMS = 5, PLATFORM_WIDTH = 100;
	final double GRAVITY = 0.5, JUMP_POWER = 12, SHORT_HOP_POWER = 1.4, PLAYER_RUN_SPEED = 4;
	public static final int PLAYER_WIDTH = 16, PLAYER_HEIGHT = 32, MOUSE_RADIUS = 16;
	public static Vector playerPos, playerVelocity, mousePos;
	int jumps;
	double redDamage, blueDamage; //RED IS FOR PLAYER - BLUE IS FOR MOUSE
	Object controlLock = new Object();
	ArrayList<Bullet> redAttacks, blueAttacks;
	ArrayList<Point> platforms;
	ArrayList<Wave> currentWaves;
	Controls controls, prevControls;
	BufferedImage playerImg, mouseImg, platformImg;
	
	public Game()
	{
		playerPos = new Vector(S_WIDTH/2, S_HEIGHT-BOX_SIDE_BUFFER);
		playerVelocity = new Vector();
		mousePos = new Vector();
		redAttacks = new ArrayList<Bullet>();
		blueAttacks = new ArrayList<Bullet>();
		currentWaves = new ArrayList<Wave>();
		platforms = new ArrayList<Point>();
		prevControls = new Controls();
		controls = new Controls();
		jumps = MAX_JUMPS;
		redDamage = blueDamage = 0;

		try {
			playerImg = ImageIO.read(new File("res/player.png"));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			mouseImg = ImageIO.read(new File("res/mouse.png"));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			platformImg = ImageIO.read(new File("res/platform.png"));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		this.addKeyListener(new KeyAdapter(){
			public void keyPressed(KeyEvent e)
			{
				synchronized(controlLock)
				{
					if(e.getKeyCode() == KeyEvent.VK_D)
						controls.right = true;
					if(e.getKeyCode() == KeyEvent.VK_W)
						controls.up = true;
					if(e.getKeyCode() == KeyEvent.VK_A)
						controls.left = true;
					if(e.getKeyCode() == KeyEvent.VK_S)
						controls.down = true;
				}
			}
			
			public void keyReleased(KeyEvent e)
			{
				synchronized(controlLock)
				{
					if(e.getKeyCode() == KeyEvent.VK_D)
						controls.right = false;
					if(e.getKeyCode() == KeyEvent.VK_W || e.getKeyCode() == KeyEvent.VK_SPACE)
						controls.up = false;
					if(e.getKeyCode() == KeyEvent.VK_A)
						controls.left = false;
					if(e.getKeyCode() == KeyEvent.VK_S)
						controls.down = false;
				}
			}
		});
		this.addMouseMotionListener(new MouseMotionAdapter(){
			public void mouseMoved(MouseEvent e)
			{
				mousePos.set(e.getX(), e.getY());
			}

			public void mouseDragged(MouseEvent e)
			{
				mousePos.set(e.getX(), e.getY());
			}
		});
		this.addMouseListener(new MouseAdapter(){
			public void mouseReleased(MouseEvent e)
			{
				platforms.add(new Point(e.getX(), e.getY()));
				if(platforms.size() > MAX_PLATFORMS)
					platforms.remove(0);
			}
		});

		this.setFocusable(true);
		this.requestFocus();
		this.setPreferredSize(new Dimension(S_WIDTH, S_HEIGHT));
	}
	
	public void tick()
	{
		//Velocity control and input
		playerVelocity.y += GRAVITY;
		
		playerVelocity.x = 0;
		if(controls.right)
			playerVelocity.x += PLAYER_RUN_SPEED;
		if(controls.left)
			playerVelocity.x += -PLAYER_RUN_SPEED;
		if(controls.up)
		{
			if(jumps > 0 && prevControls.up == false)
			{
				playerVelocity.y = -JUMP_POWER;
				jumps--;
			}
		}
		else
			if(playerVelocity.y < 0)
				playerVelocity.y += SHORT_HOP_POWER;
		
		//Check platforms before moving
		Polygon playerPoly = new Polygon(new int[]{(int) playerPos.x, (int) playerPos.x+PLAYER_WIDTH,
				(int) (playerPos.x+playerVelocity.x), (int) (playerPos.x+PLAYER_WIDTH+playerVelocity.x)},
				new int[]{(int) (playerPos.y+PLAYER_HEIGHT), (int) (playerPos.y+PLAYER_HEIGHT),
						(int) (playerPos.y+PLAYER_HEIGHT+playerVelocity.y),
						(int) (playerPos.y+PLAYER_HEIGHT+playerVelocity.y)}, 4);
		synchronized(controlLock)
		{
			if(playerVelocity.y > 0 && !controls.down)
				for(Point p : platforms)
					if(playerPoly.intersects(new Rectangle(p.x - PLATFORM_WIDTH/2,
							p.y - PLATFORM_HEIGHT/2, PLATFORM_WIDTH, 1)))
					{
						playerPos.y = p.y - PLATFORM_HEIGHT/2 - PLAYER_HEIGHT;
						playerVelocity.y = 0;
						jumps = MAX_JUMPS;
						break;
					}
		}
		playerPos.add(playerVelocity);
		
		//Player bounding
		if(playerPos.y + PLAYER_HEIGHT > S_HEIGHT - BOX_SIDE_BUFFER)
		{
			playerVelocity.y = 0;
			playerPos.y = S_HEIGHT - PLAYER_HEIGHT - BOX_SIDE_BUFFER;
			jumps = MAX_JUMPS;
		}
		else if(playerPos.y < BOX_SIDE_BUFFER)
		{
			playerVelocity.y = 0;
			playerPos.y = BOX_SIDE_BUFFER;
		}
		if(playerPos.x < BOX_SIDE_BUFFER)
		{
			playerPos.x = BOX_SIDE_BUFFER;
			playerVelocity.x = 0;
		}
		else if(playerPos.x + PLAYER_WIDTH > S_WIDTH - BOX_SIDE_BUFFER)
		{
			playerPos.x = S_WIDTH - BOX_SIDE_BUFFER - PLAYER_WIDTH;
			playerVelocity.x = 0;
		}

		//Update waves
		Iterator<Wave> iter = currentWaves.iterator();
		while(iter.hasNext())
		{
			ArrayList<Bullet> list = iter.next().tick();
			if(list == null)
				continue;
			
			if(list.size() == 0)
				iter.remove();
			else
				while(list.size() > 0)
					if(list.get(0).isRed)
						redAttacks.add(list.remove(0));
					else
						blueAttacks.add(list.remove(0));
		}
		//TODO Remove temp system of spawning new waves
		if(currentWaves.size() == 0)
			switch((int)(Math.random()*3)){
			case 0:
				currentWaves.add(Wave.flowey_circle_wave());
				break;
			case 1:
				currentWaves.add(Wave.stripe_move_right_wave());
				break;
			case 2:
				currentWaves.add(Wave.random_grow_circles_wave());
				break;
			}
		
		//Update bullets
		Iterator<Bullet> itera = redAttacks.iterator();
		while(itera.hasNext())
		{
			Bullet b = itera.next();
			
			b.update();
			if(b.life <= 0)
				itera.remove();
		}
		
		itera = blueAttacks.iterator();
		while(itera.hasNext())
		{
			Bullet b = itera.next();
			
			b.update();
			if(b.life <= 0)
				itera.remove();
		}
		
		//Player damage
		for(Bullet b : redAttacks)
			if(b.shape.intersects(new Rectangle((int)playerPos.x, (int)playerPos.y, PLAYER_WIDTH, PLAYER_HEIGHT)))
				redDamage++;
		for(Bullet b : blueAttacks)
			if(b.shape.intersects(new Rectangle((int)mousePos.x - MOUSE_RADIUS, (int)mousePos.y - MOUSE_RADIUS,
					MOUSE_RADIUS*2, MOUSE_RADIUS*2)))
				blueDamage++;

		synchronized(controlLock)
		{
			prevControls = controls.clone();
		}
	}
	
	final int HEALTH_PER_PIXEL = 8, HEALTH_BAR_HEIGHT = 12, BOX_THICKNESS = 4, PLATFORM_HEIGHT = 16;
	public void paintComponent(Graphics gr)
	{
		Graphics2D g = (Graphics2D) gr;
		
		//Background
		g.setColor(Color.white);
		g.fillRect(0, 0, S_WIDTH, S_HEIGHT);
		
		//Attacks
		BufferedImage attackImg = new BufferedImage(S_WIDTH, S_HEIGHT, BufferedImage.TYPE_INT_ARGB);
		Graphics2D ag = (Graphics2D) attackImg.getGraphics();
		ag.setComposite(AlphaComposite.Clear);
		ag.fillRect(0, 0, S_WIDTH, S_HEIGHT);
		ag.setComposite(AlphaComposite.SrcOver);
		
		ag.setColor(new Color(127, 0, 0, 255));
		for(Bullet b : redAttacks)
			ag.fill(b.shape);
		
		ag.setColor(new Color(127, 0, 127, 255));
		ag.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_IN));
		g.setColor(new Color(0, 0, 127, 255));
		for(Bullet b : blueAttacks)
		{
			ag.fill(b.shape);
			g.fill(b.shape);
		}
		g.drawImage(attackImg, 0, 0, null);
		
		//Platforms
		for(Point p : platforms)
			g.drawImage(platformImg, p.x - PLATFORM_WIDTH/2, p.y - PLATFORM_HEIGHT/2,
					PLATFORM_WIDTH, PLATFORM_HEIGHT, null);
		
		//Outside/Inside White
		//Box Borders
		g.setColor(Color.black);
		for(int x = BOX_THICKNESS-1; x >= 0; x--)
			g.drawRect(BOX_SIDE_BUFFER - x, BOX_SIDE_BUFFER - x, S_WIDTH - BOX_SIDE_BUFFER*2 + x*2, S_HEIGHT - BOX_SIDE_BUFFER*2 + x*2);
		
		//Health
		g.setColor(Color.green);
		g.fillRect(S_WIDTH/2 - (MAX_HEALTH / HEALTH_PER_PIXEL)/2,
				S_HEIGHT - (BOX_SIDE_BUFFER - BOX_THICKNESS)/2 - HEALTH_BAR_HEIGHT/2,
				(MAX_HEALTH / HEALTH_PER_PIXEL), HEALTH_BAR_HEIGHT);
		g.setColor(Color.red);
		g.fillRect(S_WIDTH/2 - (MAX_HEALTH / HEALTH_PER_PIXEL)/2,
				S_HEIGHT - (BOX_SIDE_BUFFER - BOX_THICKNESS)/2 - HEALTH_BAR_HEIGHT/2,
				(MAX_HEALTH / HEALTH_PER_PIXEL) * (int)redDamage / MAX_HEALTH, HEALTH_BAR_HEIGHT);
		g.fillRect(S_WIDTH/2 + (MAX_HEALTH / HEALTH_PER_PIXEL)/2 - (int)blueDamage / HEALTH_PER_PIXEL,
				S_HEIGHT - (BOX_SIDE_BUFFER - BOX_THICKNESS)/2 - HEALTH_BAR_HEIGHT/2,
				(MAX_HEALTH / HEALTH_PER_PIXEL) * (int)blueDamage / MAX_HEALTH, HEALTH_BAR_HEIGHT);
		
		//Player & Mouse & Enemy
		g.drawImage(playerImg, (int)playerPos.x, (int)playerPos.y, null);
		g.drawImage(mouseImg, (int)mousePos.x - MOUSE_RADIUS, (int)mousePos.y - MOUSE_RADIUS, null);
	}
}
