public abstract class StationaryObject extends GameObject {

	public StationaryObject(int r, int c, char tile, int w, int h, World world, int line) {
		super(r, c, tile, w, h, world, line);
		
		if (tile != 'w' && tile != 's')
	    {
	        System.out.println("Invalid StationaryObject tile char: '" + tile + "' at line: " + line);
	        System.exit(0);
	    }
	}
	
	public void Dispose() {
		World world = getWorld();
		int h = getHeight();
		int w = getWidth();
		
		for (int rCount = m_row; rCount < m_row + h; rCount++)
	    {
	        for (int cCount = m_col; cCount < m_col + w; cCount++)
	            world.setMap(rCount, cCount, null);
	    }
	}
}
