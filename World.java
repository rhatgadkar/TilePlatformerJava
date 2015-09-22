import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;

public class World {
	
	private Game m_game;
	
	private GameObject[][] m_map;
	
	private int m_numCols;
	private int m_levelWidth;
	
	private String m_mapFile;
	
	public World(String mapFile, Game game) {
		m_game = game;
		m_mapFile = mapFile;
		m_levelWidth = Game.MAX_LEVEL_WIDTH;
	    m_numCols = Game.MAX_NUM_COLS;
	    
	    // initialize map
	    m_map = new GameObject[Game.NUM_ROWS][];
	    for (int r = 0; r < Game.NUM_ROWS; r++) {
	    	m_map[r] = new GameObject[m_numCols];
	    	for (int c = 0; c < m_numCols; c++)
	    		m_map[r][c] = null;
	    }
	    
	    int a = parseMapFile();
	    if (a == -1)
	    	System.exit(0);
	    
	    m_levelWidth = a + Game.TILE_WIDTH;
	    m_numCols = m_levelWidth / Game.TILE_WIDTH;
	}
	
	public void gameOver() {
		m_game.reset();
		
		for (int r = 0; r < Game.NUM_ROWS; r++)
	    {
	        //m_map[r] = new GameObject[m_numCols];
	        for (int c = 0; c < m_numCols; c++)
	            m_map[r][c] = null;
	    }
		
		parseMapFile();
	}
	
	public boolean getKey(MyKeys key) {
		return m_game.getKey(key);
	}
	
	public boolean inTileCol(int x) {
		return (x % Game.TILE_WIDTH == 0);
	}
	
	public boolean inTileRow(int y) {
		return (y % Game.TILE_HEIGHT == 0);
	}
	
	public boolean validRow(int r) {
		return (r >= 0 && r < Game.NUM_ROWS);
	}
	
	public boolean validCol(int c) {
		return (c >= 0 && c < m_numCols);
	}
	
	public boolean validX(int x) {
		return (x >= 0 && x < m_levelWidth);
	}
	
	public boolean validY(int y) {
		return (y >= 0 && y < Game.SCREEN_HEIGHT);
	}
	
	public final int getLevelWidth() {
		return m_levelWidth;
	}
	
	public final int getNumCols() {
		return m_numCols;
	}
	
	public final GameObject getMap(int r, int c) {
		return m_map[r][c];
	}
	
	public void setMap(int r, int c, GameObject go) {
		m_map[r][c] = go;
	}
	
	private int parseMapFile() {		
		Player player = null;
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(m_mapFile));
			
			int highest_x = 0;
			
			int lineNum = 1;
			String line;
			final String REGEX = ",";
			
			while ( (line = br.readLine()) != null ) {
				char tile = '-';
				int row = -1, col = -1, width = -1, height = -1;
				
				Pattern p = Pattern.compile(REGEX);
				String[] vars = p.split(line);
				
				try {
					tile = vars[0].charAt(0);
					row = Integer.parseInt(vars[1]);
					col = Integer.parseInt(vars[2]);
					width = Integer.parseInt(vars[3]);
					height = Integer.parseInt(vars[4]);
				}
				catch (Exception ex) {
					System.out.println("Map file error:\nInvalid line at line: " + lineNum);
	                System.exit(0);
				}
				
				// create the gameobject
				switch (tile) {
				case 'w':
		            m_game.addNewStationaryObject(new Wall(row, col, width, height, this, lineNum));
		            break;
		        case 'p':
		        {
		            if (player != null)
		            {
		                System.out.println("Map file error:\nCan only have one player. Extra player at line: " + lineNum);
		                System.exit(0);
		            }

		            player = new Player(row, col, width, height, this, lineNum);
		            break;
		        }
		        case 's':
		            m_game.addNewStationaryObject(new StationaryEnemy(row, col, width, height, this, lineNum));
		            break;
		        case 'm':
		            break;
		        default:
		        	System.out.println("Map file error:\nNot a valid tile char at line: " + lineNum);
	                System.exit(0);
				}
				
				if (tile == 'w' || tile == 's') {
					StationaryObject last = m_game.getLastStatObj();
					int last_x = last.getX();
					if (last_x > highest_x)
						highest_x = last_x;
				}
				
				lineNum++;
			}
			
			if (player == null) {
				System.out.println("Map file error:\nA player must be added.");
				System.exit(0);
			}
			
			// remove chars in map for player
		    int player_row = player.getR();
		    int player_col = player.getC();
		    for (int r = player_row; r < (player_row + player.getHeight()); r++)
		    {
		        for (int c = player_col; c < (player_col + player.getWidth()); c++)
		            setMap(r, c, null);
		    }

		    // remove chars in map for movingobjects
		    // not yet implemented because there are no movingobjects yet.


		    br.close();
		    
		    m_game.setPlayer(player);

		    return highest_x;
		}
		catch (FileNotFoundException ex) {
			System.out.println("Could not open map file.");
			return -1;
		}
		catch (IOException ex) {
			System.out.println("IO exception");
			return -1;
		}
	}
}
