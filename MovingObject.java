public abstract class MovingObject extends GameObject {

	public MovingObject(int r, int c, char tile, int w, int h, World world, int line) {
		super(r, c, tile, w, h, world, line);
		
		
	}
	
	public void moveLeft() { 
		m_x--; 
	}
    public void moveRight() { 
    	m_x++; 
    }
    public void moveUp() { 
    	m_y--; 
    }
    public void moveDown() { 
    	m_y++; 
    }
}
