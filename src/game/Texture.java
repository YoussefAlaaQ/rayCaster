package game;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class Texture {
    public static Texture wall = new Texture("src/game/Textures/wall.png", 64);
    public static Texture brick = new Texture("src/game/Textures/RedBrickWall.bmp", 128);
	public static Texture floor = new Texture("src/game/Textures/floor.bmp", 128);
	public static Texture grid = new Texture("src/game/Textures/grid.bmp", 128);

    public int [] pixels;
    private String loc;
    public final int SIZE;

    public Texture(String path, int size){
        loc = path;
        SIZE = size;

        pixels = new int [size * size];

        load();
    }

    private void load(){
        try{
            File imageFile = new File(loc);
            BufferedImage image = ImageIO.read(imageFile);
            int width = image.getWidth();
            int height = image.getHeight();
            image.getRGB(0, 0, width, height, pixels, 0, width);
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }
}
