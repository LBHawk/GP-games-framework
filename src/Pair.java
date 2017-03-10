public class Pair{
	private int x;
	private int y;

	public Pair(int x, int y){
		this.x = x;
		this.y = y;
	}

	public int getX(){
		return this.x;
	}

	public int getY(){
		return this.y;
	}

	public void addX(){
		this.x++;
		//System.out.println("Adding to x: " + x);
	}

	public void addY(){
		this.y++;
	}

	public void subX(){
		this.x--;
	}

	public void subY(){
		this.y--;
	}

	public void setPair(Pair p){
		this.x = p.getX();
		this.y = p.getY();
	}
}
