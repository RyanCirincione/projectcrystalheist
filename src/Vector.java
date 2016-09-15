import java.text.DecimalFormat;

public class Vector
{
	public double x, y;
	
	public Vector()
	{
		this(0, 0);
	}
	
	public Vector(double x, double y)
	{
		this.x = x;
		this.y = y;
	}
	
	public Vector(Vector v)
	{
		this.x = v.x;
		this.y = v.y;
	}
	
	public Vector set(double x, double y)
	{
		this.x = x;
		this.y = y;
		return this;
	}
	
	public Vector set(Vector v)
	{
		this.x = v.x;
		this.y = v.y;
		return this;
	}
	
	public Vector add(double x, double y)
	{
		this.x += x;
		this.y += y;
		return this;
	}
	
	public Vector add(Vector v)
	{
		this.x += v.x;
		this.y += v.y;
		return this;
	}
	
	public Vector sub(double x, double y)
	{
		this.x -= x;
		this.y -= y;
		return this;
	}
	
	public Vector sub(Vector v)
	{
		this.x -= v.x;
		this.y -= v.y;
		return this;
	}
	
	public double dst(Vector v)
	{
		return Math.sqrt(Math.pow(x - v.x, 2) + Math.pow(y - v.y, 2));
	}
	
	public double dst(double x, double y)
	{
		return Math.sqrt(Math.pow(this.x - x, 2) + Math.pow(this.y - y, 2));
	}
	
	public double dst2(Vector v)
	{
		return Math.pow(x - v.x, 2) + Math.pow(y - v.y, 2);
	}
	
	public double dst2(double x, double y)
	{
		return Math.pow(this.x - x, 2) + Math.pow(this.y - y, 2);
	}
	
	public double length()
	{
		return Math.sqrt(x*x + y*y);
	}
	
	public double length2()
	{
		return x*x + y*y;
	}
	
	public Vector scale(double scl)
	{
		x *= scl;
		y *= scl;
		return this;
	}
	
	public Vector setLength(double len)
	{
		double l = this.length();
		x *= len / l;
		y *= len / l;
		return this;
	}
	
	public Vector setLength2(double len)
	{
		double l = this.length2();
		x *= len / l;
		y *= len / l;
		return this;
	}
	
	public Vector clone()
	{
		return new Vector(this);
	}
	
	public boolean equals(Vector v)
	{
		return x == v.x && y == v.y;
	}
	
	public boolean approxEquals(Vector v, double dst)
	{
		return this.dst2(v) < dst*dst;
	}
	
	public String toString()
	{
		return "<" + new DecimalFormat("0.####").format(x) + ", " + new DecimalFormat("0.####").format(y) + ">";
	}
}
