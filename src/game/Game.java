package game;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import javax.swing.JFrame;

public class Game extends JFrame implements Runnable{
    private static final long serialVersionUID = 1L;
	public int mapWidth = 15;
	public int mapHeight = 15;
	private Thread thread;
	private boolean running;
	private BufferedImage image;
    public int[] pixels;
    public ArrayList <Texture> textures;
    public Camera camera;
	public Screen screen;

	public static int[][] map = 
	{
		{1,1,1,1,1,1,1,1,2,2,2,2,2,2,2},
		{1,0,0,0,0,0,0,0,2,0,0,0,0,0,2},
		{1,0,1,1,1,1,1,0,0,0,0,0,0,0,2},
		{1,0,1,0,0,0,1,0,2,0,0,0,0,0,2},
		{1,0,1,0,0,0,1,0,2,2,2,0,2,2,2},
		{1,0,1,0,0,0,1,0,2,0,0,0,0,0,2},
		{1,0,1,1,0,1,1,0,2,0,0,0,0,0,2},
		{1,0,0,0,0,0,0,0,2,0,0,0,0,0,2},
		{1,1,1,1,1,1,1,1,2,2,2,0,2,2,2},
		{1,0,0,0,0,0,1,1,0,0,0,0,0,0,1},
		{1,0,0,0,0,0,1,1,0,0,0,0,0,0,1},
		{1,0,0,2,0,0,1,1,0,2,2,2,2,0,1},
		{1,0,0,0,0,0,1,1,0,2,2,2,2,0,1},
		{1,0,0,0,0,0,0,0,0,0,0,0,0,0,1},
		{1,1,1,1,1,1,1,1,1,1,1,1,1,1,1}
	};

    public Game(){
        thread = new Thread(this);
		image = new BufferedImage(640, 480, BufferedImage.TYPE_INT_RGB);
		pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
		
        textures = new ArrayList<>();
        textures.add(Texture.wall);
        textures.add(Texture.brick);
        textures.add(Texture.grid);
        textures.add(Texture.floor);


		screen = new Screen(map, mapWidth, mapHeight, textures, 640, 480);

        camera = new Camera(4.5, 4.5, 1, 0, 0, -.66);
        addKeyListener(camera);

        setSize(640, 480);
		setResizable(false);
		setTitle("RayCasting Engine");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBackground(Color.black);
		setLocationRelativeTo(null);
		setVisible(true);
		
        start();
    }

    public void nothing(){
        /* this method does nothing and just used to stop vscode (compiler) from complainning about unused variable */
    }

    private synchronized void start(){
        if(running) return;

        thread.start();
        running = true;
    }

    public synchronized void stop(){
        if(!running) return;

        running = false;
        try{
            thread.join();
        }
        catch(InterruptedException e){
            e.printStackTrace();
        }
    }

    public void render(){
        BufferStrategy bs = getBufferStrategy();
	    if(bs == null) {
		    createBufferStrategy(3);
		    return;
	    }
	    Graphics g = bs.getDrawGraphics();
	    g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
	    
        bs.show();
    }

    public void run(){
        long lastTime = System.nanoTime();
	    final double ns = 1000000000.0 / 60.0; //time per frame(in 60 fps)
	    double delta = 0;

	    requestFocus();

	    while(running) {
		    long now = System.nanoTime();
		    delta = delta + ((now-lastTime) / ns); //how many frames has target frame passed 
		    lastTime = now;

		    while (delta >= 1){ 
                camera.update(map);
			    delta--;
		    }

			screen.update(camera, pixels);

		    render(); 
	    }
    }
}
