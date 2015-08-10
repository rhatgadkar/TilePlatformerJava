public abstract class GameObject {
	private Game m_game;
	private char m_tile;
	private int m_pos;
	private int m_width;
	private int m_height;
	
	protected int m_x;
	protected int m_y;
	protected int m_row;
	protected int m_col;
	
	public GameObject(int r, int c, char tile, int w, int h, Game g, int line) {
		m_game = g;
		m_tile = tile;
		m_width = w;
		m_height = h;
		m_row = r;
		m_col = c;
		m_x = c * Game.TILE_WIDTH;
		m_y = r * Game.TILE_HEIGHT;
		m_row = r;
		m_col = c;
		
		if (tile != 'w' && tile != 'p' && tile != 's' && tile != 'm') {
			System.out.println("Invalid tile char: '" + tile + "' at line: " + line);
			System.exit(0);
		}
		if (!g.validRow(r)) {
			System.out.println("Invalid row: '" + r + "' at line: " + line);
			System.exit(0);
		}
		if (c < 0 || c >= g.getNumCols()) {
			System.out.println("Invalid col: '" + c + "' at line: " + line);
			System.exit(0);
		}
		if (w < 1 || w > g.getNumCols()) {
			System.out.println("Invalid width: '" + w + "' at line: " + line);
			System.exit(0);
		}
		if (h < 1 || h > Game.NUM_ROWS) {
			System.out.println("Invalid height: '" + h + "' at line: " + line);
			System.exit(0);
		}
		
		for (int rCount = r; rCount < r + h; rCount++) {
			for (int cCount = c; cCount < c + w; cCount++) {
				GameObject curr = g.getMap(rCount, cCount);
				if (curr != null) {
					System.out.println("Cannot add GameObject: '" + tile + 
									   "', This tile exists here: '" + 
							           curr.getTile() + "'. Line: " + line);
					System.exit(0);
				}
				
				g.setMap(rCount, cCount, this);
			}
		}
	}
	
	public abstract void doSomething();
	
	public final Game getGame() {
		return m_game;
	}
	
	public final char getTile() {
		return m_tile;
	}
	
	public final int getPos() {
		return m_pos;
	}
	
	public void setPos(int pos) {
		m_pos = pos;
	}
	
	public final int getWidth() {
		return m_width;
	}
	
	public final int getHeight() {
		return m_height;
	}
	
	public final int getX() {
		return m_x;
	}
	
	public final int getY() {
		return m_y;
	}
	
	public final int getR() {
		return m_row;
	}
	
	public final int getC() {
		return m_col;
	}
	
	public abstract void Dispose();
}
