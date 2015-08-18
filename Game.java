import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.regex.Pattern;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

enum MyKeys {
	KEY_UP(0), 
	KEY_DOWN(1), 
	KEY_LEFT(2), 
	KEY_RIGHT(3);
	
	public final int Value;
	
	private MyKeys(int value) {
		Value = value;
	}
}

public class Game extends JPanel {

	private static final long serialVersionUID = 1L;
	
	public final static int SCREEN_WIDTH = 640;
	public final static int SCREEN_HEIGHT = 480;
	
	public final static int TILE_WIDTH = 32;
	public final static int TILE_HEIGHT = 24;
	
	public final static int PLAYER_WIDTH = 22;
	public final static int PLAYER_HEIGHT = 14;
	
	public final static int MAX_LEVEL_WIDTH = 3200;
	
	public final static int MAX_NUM_COLS = MAX_LEVEL_WIDTH / TILE_WIDTH;
	
	public final static int NUM_ROWS = SCREEN_HEIGHT / TILE_HEIGHT;
	
	public final static int FPS = 60;

	public static void main(String[] args) {
		if (args == null || args.length == 0) {
			System.out.println("No map file entered as argument.");
			return;
		}
		if (args == null || args.length > 1) {
			System.out.println("Only one map file is allowed.");
			return;
		}
		
		JFrame window = new JFrame("TilePlatformerJava");
		Game content = new Game(args[0]);
		window.setContentPane(content);
		window.setSize(SCREEN_WIDTH, SCREEN_HEIGHT + 25);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		window.setLocation((screenSize.width - SCREEN_WIDTH) / 2, 
				           (screenSize.height - SCREEN_HEIGHT) / 2);
		window.setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
        window.setResizable(false);
        window.setVisible(true);
	}

	private String m_mapFile;
	
	private Timer m_timer;
	private boolean[] m_key;
	
	private boolean m_doexit;
	private boolean m_redraw;
	private boolean m_gameStarted;
	private boolean m_gameOver;
	private boolean m_pause;
	
	private boolean m_reset;
	
	private int m_numCols;
	private int m_levelWidth;
	
	private int m_camX;
	
	private LinkedList<StationaryObject> m_stationaryobjects;
	private int m_stationaryCount;
	
	private LinkedList<MovingObject> m_movingobjects;
	private int m_movingCount;
	
	private Player m_player;
	
	public void addNewStationaryObject(StationaryObject so) {
		m_stationaryobjects.add(so); 
		so.setPos(m_stationaryCount); 
		m_stationaryCount++;
	}
	
	public void addNewMovingObject(MovingObject mo) {
		m_movingobjects.add(mo); 
		mo.setPos(m_movingCount); 
		m_movingCount++;
	}
	
	private GameObject[][] m_map;
	
	public Game(String mapFile) {
		m_mapFile = mapFile;
		m_doexit = false;
	    m_redraw = false;
	    m_gameStarted = false;
	    m_gameOver = false;
	    m_pause = false;
	    m_reset = false;
	    m_camX = 0;
	    m_numCols = MAX_NUM_COLS;
	    m_stationaryCount = 0;
	    m_movingCount = 0;
	    m_levelWidth = Game.MAX_LEVEL_WIDTH;
	    m_numCols = Game.MAX_NUM_COLS;
	    m_player = null;
	    
	    m_stationaryobjects = new LinkedList<StationaryObject>();
	    m_movingobjects = new LinkedList<MovingObject>();
	    
	    // initialize map
	    m_map = new GameObject[NUM_ROWS][];
	    for (int r = 0; r < NUM_ROWS; r++) {
	    	m_map[r] = new GameObject[m_numCols];
	    	for (int c = 0; c < m_numCols; c++)
	    		m_map[r][c] = null;
	    }
	    
	    int a = parseMapFile();
	    if (a == -1)
	    	System.exit(0);
	    
	    m_levelWidth = a + TILE_WIDTH;
	    m_numCols = m_levelWidth / TILE_WIDTH;
	    
	    m_key = new boolean[4];
	    for (int k = 0; k < 4; k++)
	    	m_key[k] = false;
		
		setBackground(Color.BLACK);
		
		ActionListener action = new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (m_doexit)
					System.exit(0);
			
				if (!m_gameStarted || m_gameOver || m_pause)
					m_redraw = false;
				else {
					m_redraw = true;
					getInput();
					
					if (m_reset) {
						m_gameOver = true;
						m_reset = false;
						m_redraw = false;
					}
				}
				
				if (m_gameStarted && !m_gameOver && !m_pause && m_redraw)
					m_redraw = false;
				
				repaint();
			}
		};
		m_timer = new Timer(1000 / FPS, action);
		
//		addMouseListener(new MouseAdapter() {
//	        public void mousePressed(MouseEvent evt) {
//	            requestFocus();
//	        }
//		});
//		
//	    addFocusListener(new FocusListener() {
//	        public void focusGained(FocusEvent evt) {
//	            m_timer.start();
//	            //m_pause = false;
//	            repaint();
//	        }
//	        public void focusLost(FocusEvent evt) {
//	            m_timer.stop();
//	            //m_pause = true;
//	        }
//	    });
	    
	    addKeyListener(new KeyAdapter() {
	    	public void keyPressed(KeyEvent evt) {
	    		int code = evt.getKeyCode();
	    		
	    		if (code == KeyEvent.VK_LEFT)
	    			m_key[MyKeys.KEY_LEFT.Value] = true;
	    		
	    		if (code == KeyEvent.VK_RIGHT)
	    			m_key[MyKeys.KEY_RIGHT.Value] = true;
	    		
	    		if (code == KeyEvent.VK_UP)
	    			m_key[MyKeys.KEY_UP.Value] = true;
	    		
	    		if (code == KeyEvent.VK_DOWN)
	    			m_key[MyKeys.KEY_DOWN.Value] = true;
	    	}
	    	
	    	public void keyReleased(KeyEvent evt) {
	    		int code = evt.getKeyCode();
	    
	    		if (code == KeyEvent.VK_LEFT)
	    			m_key[MyKeys.KEY_LEFT.Value] = false;
	    		
	    		if (code == KeyEvent.VK_RIGHT)
	    			m_key[MyKeys.KEY_RIGHT.Value] = false;
	    		
	    		if (code == KeyEvent.VK_UP)
	    			m_key[MyKeys.KEY_UP.Value] = false;
	    		
	    		if (code == KeyEvent.VK_DOWN)
	    			m_key[MyKeys.KEY_DOWN.Value] = false;
	    		
	    		if (code == KeyEvent.VK_ESCAPE)
	    			m_doexit = true;
	    		
	    		if (code == KeyEvent.VK_P) {
	    			if (m_gameStarted && !m_gameOver && !m_pause)
	    				m_pause = true;
	    			else if (m_gameStarted && !m_gameOver && m_pause)
	    				m_pause = false;
	    		}
	    		
	    		if (code == KeyEvent.VK_ENTER) {
	    			if (!m_gameStarted && !m_gameOver)
	                    m_gameStarted = true;
	                else if (m_gameStarted && m_gameOver)
	                    m_gameOver = false;
	    		}
	    	}
	    });
	    
	    setFocusable(true);
	    
		m_timer.start();
	}
	
	public void reset() {
		m_reset = true;
		
		m_player = null;
		
		m_camX = 0;
		
		for (StationaryObject so : m_stationaryobjects) {
			so.Dispose();
		}
		m_stationaryobjects.clear();
		
		m_movingobjects.clear();
		
		m_stationaryCount = 0;
		m_movingCount = 0;
		
		for (int r = 0; r < NUM_ROWS; r++)
	    {
	        m_map[r] = new GameObject[m_numCols];
	        for (int c = 0; c < m_numCols; c++)
	            m_map[r][c] = null;
	    }

	    parseMapFile();
	}
	
	public boolean getKey(MyKeys key) {
		return m_key[key.Value];
	}
	
	public void setReset(boolean val) {
		m_reset = val;
	}
	
	public boolean inTileCol(int x) {
		return (x % TILE_WIDTH == 0);
	}
	
	public boolean inTileRow(int y) {
		return (y % TILE_HEIGHT == 0);
	}
	
	public boolean validRow(int r) {
		return (r >= 0 && r < NUM_ROWS);
	}
	
	public boolean validCol(int c) {
		return (c >= 0 && c < m_numCols);
	}
	
	public boolean validX(int x) {
		return (x >= 0 && x < m_levelWidth);
	}
	
	public boolean validY(int y) {
		return (y >= 0 && y < SCREEN_HEIGHT);
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
	
	private void getInput() {
		m_player.doSomething();
		
		for (MovingObject mo : m_movingobjects) {
			mo.doSomething();
			
			if (m_reset)
				return;
		}
	}
	
	public void paintComponent(Graphics g) {
		if (m_gameStarted && !m_gameOver && !m_pause && !m_redraw) {
			Image img = draw();
			g.drawImage(img, 0, 0, this);
		}
		else if (!m_gameStarted) {
			Image img = drawStartScreen();
			g.drawImage(img, 0, 0, this);
		}
		else if (m_gameOver) {
			Image img = drawGameOverScreen();
			g.drawImage(img, 0, 0, this);
		}
	}
	
	private Image draw() {
		BufferedImage buffImg = new BufferedImage(m_levelWidth, SCREEN_HEIGHT, BufferedImage.TYPE_INT_RGB);
		Graphics g = buffImg.getGraphics();
		
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, m_levelWidth, SCREEN_HEIGHT);
		
		// draw player
	    int x1 = m_player.getX();
	    //int x2 = x1 + (m_player.getWidth() * PLAYER_WIDTH);
	    int y1 = m_player.getY();
	    //int y2 = y1 + (m_player.getHeight() * PLAYER_HEIGHT);
	    
	    int playerX = x1;
	    
	    if (playerX <= SCREEN_WIDTH / 2)
	    	;
	    else if (playerX >= m_levelWidth - SCREEN_WIDTH / 2)
	    	m_camX = m_levelWidth - SCREEN_WIDTH;
	    else
	    	m_camX = playerX - SCREEN_WIDTH / 2;
	    g.translate(-m_camX, 0);
	    
	    g.setColor(Color.GREEN);
	    g.fillRect(playerX, y1, PLAYER_WIDTH, PLAYER_HEIGHT);
	    
	    for (StationaryObject so : m_stationaryobjects) {
	    	int width = so.getWidth() * TILE_WIDTH;
	        int height = so.getHeight() * TILE_HEIGHT;
	        x1 = so.getX();
	        int x2 = x1 + width;
	        y1 = so.getY();

	        if (x2 >= m_camX || x1 >= m_camX) {
	            char tile = so.getTile();
	            switch (tile) {
	            case 'w':
	            	g.setColor(Color.RED);
	        		g.fillRect(x1, y1, width, height);
	                break;
	            case 's':
	            	g.setColor(Color.YELLOW);
	        		g.fillRect(x1, y1, width, height);
	                break;
	            }
	        }
	    }
	    
	    return buffImg;
	}
	
	private final Image drawStartScreen() {
		BufferedImage buffImg = new BufferedImage(SCREEN_WIDTH, SCREEN_HEIGHT, BufferedImage.TYPE_INT_RGB);
		Graphics g = buffImg.getGraphics();
		
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
		
		g.setColor(Color.WHITE);
		g.drawString("Press Enter to start", SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2);
		
		return buffImg;
	}
	
	private final Image drawGameOverScreen() {
		BufferedImage buffImg = new BufferedImage(SCREEN_WIDTH, SCREEN_HEIGHT, BufferedImage.TYPE_INT_RGB);
		Graphics g = buffImg.getGraphics();
		
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, SCREEN_WIDTH, SCREEN_HEIGHT);
		
		g.setColor(Color.WHITE);
		g.drawString("Game Over", (int) (SCREEN_WIDTH / 3.5), SCREEN_HEIGHT / 10);
		g.drawString("Press Enter to retry",  SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2);
		
		return buffImg;
	}
	
	private int parseMapFile() {
		try {
			BufferedReader br = new BufferedReader(new FileReader(m_mapFile));
			
			int highest_x = 0;
			
			int lineNum = 1;
			String line;
			final String REGEX = ",";
			
			while ( (line = br.readLine()) != null ) {
				char tile = '-';
				int row = -1, col = -1, width = -1, height = -1;
				int num = -1;
				
				Pattern p = Pattern.compile(REGEX);
				String[] vars = p.split(line);
				for (String s : vars) {
					if (tile == '-')
						tile = s.charAt(0);
					else if (row == -1)
						row = Integer.parseInt(s);
					else if (col == -1)
						col = Integer.parseInt(s);
					else if (width == -1)
						width = Integer.parseInt(s);
					else if (height == -1)
						height = Integer.parseInt(s);
					else if (num == -1)
						num = Integer.parseInt(s);
					else
						break;
				}
				
				// create the gameobject
				switch (tile) {
				case 'w':
		            addNewStationaryObject(new Wall(row, col, width, height, this, lineNum));
		            break;
		        case 'p':
		        {
		            if (m_player != null)
		            {
		                System.out.println("Map file error:\nCan only have one player. Extra player at line: " + lineNum);
		                System.exit(0);
		            }

		            m_player = new Player(row, col, width, height, this, lineNum);
		            break;
		        }
		        case 's':
		            addNewStationaryObject(new StationaryEnemy(row, col, width, height, this, lineNum));
		            break;
		        case 'm':
		            break;
		        default:
		        	System.out.println("Map file error:\nNot a valid tile char at line: " + lineNum);
	                System.exit(0);
				}
				
				if (tile == 'w' || tile == 's') {
					StationaryObject last = m_stationaryobjects.getLast();
					int last_x = last.getX();
					if (last_x > highest_x)
						highest_x = last_x;
				}
				
				lineNum++;
			}
			
			if (m_player == null) {
				System.out.println("Map file error:\nA player must be added.");
				System.exit(0);
			}
			
			// remove chars in map for player
		    int player_row = m_player.getR();
		    int player_col = m_player.getC();
		    for (int r = player_row; r < (player_row + m_player.getHeight()); r++)
		    {
		        for (int c = player_col; c < (player_col + m_player.getWidth()); c++)
		            setMap(r, c, null);
		    }

		    // remove chars in map for movingobjects
		    // not yet implemented because there are not movingobjects yet.


		    br.close();

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
