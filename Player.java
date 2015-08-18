public class Player extends MovingObject {
	
	private final int MAX_JUMP_COUNT = 50;

	public Player(int r, int c, int w, int h, Game g, int line) {
		super(r, c, 'p', w, h, g, line);
		
		m_jumpCount = MAX_JUMP_COUNT;
		m_fallDown = false;
		m_canJump = false;
	}

	@Override
	public void doSomething() {
		Game game = getGame();
	    int x = getX();
	    int y = getY();

	    boolean[] insideLeft = {false}; 
	    boolean[] insideRight = {false}; 
	    boolean[] insideTop = {false}; 
	    boolean[] insideDown = {false}; 
	    boolean[] insideEnemy = {false};
	    checkBounds(game, x, y, insideLeft, insideRight, insideTop, insideDown, insideEnemy);

	    if (insideEnemy[0])
	    {
	        game.reset();
	        return;
	    }

	    movePlayer(game, insideLeft[0], insideRight[0], insideTop[0], insideDown[0]);
	}

	@Override
	public void Dispose() {}

	private int m_jumpCount;
	private boolean m_fallDown;
	private boolean m_canJump;
	
	private void movePlayer(Game game, boolean insideLeftTile, 
			boolean insideRightTile, boolean insideUpTile, boolean insideDownTile) {
		if (game.getKey(MyKeys.KEY_LEFT))
	    {
	        if (!insideLeftTile)
	            moveLeft();
	    }

	    if (game.getKey(MyKeys.KEY_RIGHT))
	    {
	        if (!insideRightTile)
	            moveRight();
	    }

	    if (!insideDownTile && m_fallDown)
	    {
	        moveDown();
	        return;
	    }

	    if (insideDownTile)
	    {
	        m_jumpCount = MAX_JUMP_COUNT;
	        m_fallDown = false;
	    }

	    if (game.getKey(MyKeys.KEY_UP))
	    {
	        if (insideUpTile && !insideDownTile)
	        {
	            moveDown();
	            m_fallDown = true;
	        }
	        else if (!insideUpTile && !insideDownTile && m_jumpCount > 0 && m_jumpCount < MAX_JUMP_COUNT)
	        {
	            moveUp();
	            m_jumpCount++;
	        }
	        else if (!insideUpTile && !insideDownTile && m_jumpCount >= MAX_JUMP_COUNT)
	        {
	            moveDown();
	        }
	        else if (!insideUpTile && insideDownTile && m_jumpCount == MAX_JUMP_COUNT && m_canJump)
	        {
	            m_jumpCount = 0;
	            moveUp();
	            m_jumpCount++;
	            m_canJump = false;
	        }
	    }
	    else
	    {
	        m_jumpCount = MAX_JUMP_COUNT;

	        if (insideDownTile && insideUpTile)
	            m_canJump = false;
	        else if (insideDownTile)
	        {
	            m_canJump = true;
	        }
	        else
	        {
	            moveDown();
	            m_canJump = false;
	        }
	    }
	}
	
	private void checkBounds(Game game, int pX, int pY, boolean[] insideLeft, boolean[] insideRight, 
			boolean[] insideTop, boolean[] insideDown, boolean[] insideStationaryEnemy) {
		int px_start = pX;
	    int px_end = pX + Game.PLAYER_WIDTH;
	    int py_start = pY;
	    int py_end = pY + Game.PLAYER_HEIGHT;

	    // collision for stationary objects. more efficient than using loops. There is a wall jumping issue with this.
	    int pRow = py_start / Game.TILE_HEIGHT;
	    int pCol = px_start / Game.TILE_WIDTH;

	    int tr_row = pRow - 1, tr_col = pCol + 1;
	    int t_row = pRow - 1, t_col = pCol;
	    int tl_row = pRow - 1, tl_col = pCol - 1;
	    int l_row = pRow, l_col = pCol - 1;
	    int bl_row = pRow + 1, bl_col = pCol - 1;
	    int b_row = pRow + 1, b_col = pCol;
	    int br_row = pRow + 1, br_col = pCol + 1;
	    int r_row = pRow, r_col = pCol + 1;

	    int tileRows[] = { tr_row, t_row, tl_row, l_row, bl_row, b_row, br_row, r_row, pRow };
	    int tileCols[] = { tr_col, t_col, tl_col, l_col, bl_col, b_col, br_col, r_col, pCol };

	    int lastSelectedX = -1;
	    int lastSelectedY = -1;

	    for (int k = 0; k < 9; k++)
	    {
	        if (game.validRow(tileRows[k]) && game.validCol(tileCols[k]))
	        {
	            GameObject go = game.getMap(tileRows[k], tileCols[k]);
	            if (go == null)
	                continue;

	            char id = go.getTile();
	            if (id != 's' && id != 'w')
	                continue;

	            int gx_start = go.getX();
	            int g_width = go.getWidth();
	            int g_height = go.getHeight();
	            int gx_end = gx_start + Game.TILE_WIDTH * g_width;
	            int gy_start = go.getY();
	            int gy_end = gy_start + Game.TILE_HEIGHT * g_height;

	            if (gx_start == lastSelectedX && gy_start == lastSelectedY)
	                continue;
	            lastSelectedX = gx_start;
	            lastSelectedY = gy_start;

	            if ( (px_end >= gx_start) && (px_start <= gx_end ) && (py_end >= gy_start) && (py_start <= gy_end) ) // collision occured
	            {
	                if (id == 's')
	                {
	                    insideStationaryEnemy[0] = true;
	                    return;
	                }
	                
	                int leftCol = tileCols[k] - 1;
	                int rightCol = tileCols[k] + 1;
	                GameObject go_leftCol = game.getMap(tileRows[k], leftCol);
	                GameObject go_rightCol = game.getMap(tileRows[k], rightCol);

	                if (px_end == gx_start)
	                {
	                    if (game.validCol(leftCol) && go_leftCol != null && go_leftCol.getTile() == 'w')
	                        ;
	                    else
	                        insideRight[0] = true;
	                }
	                else if (px_start == gx_end)
	                {
	                    if (game.validCol(rightCol) && go_rightCol != null && go_rightCol.getTile() == 'w')
	                        ;
	                    else
	                        insideLeft[0] = true;
	                }
	                else if (py_start == gy_end && px_end != gx_start && px_start != gx_end)
	                    insideTop[0] = true;
	                else if (py_end == gy_start && px_start != gx_end && px_end != gx_start)
	                    insideDown[0] = true;
	            }
	        }
	    }
	}
}
