package game;

import java.util.ArrayList;

public class Screen {
    public int [][] map;
    public int mapWidth, mapHeight, width, height;
    public ArrayList <Texture> textures;

    public Screen(int[][] m, int mapW, int mapH, ArrayList<Texture> tex, int w, int h) {
		map = m;
		mapWidth = mapW;
		mapHeight = mapH;
		textures = tex;
		width = w;
		height = h;
	}

    public int[] update (Camera camera, int [] pixels){
		//floor and ceiling
        for(int y = 0; y < height; y++){
            double rayDirX0 = camera.xDir - camera.xPlane;
            double rayDirY0 = camera.yDir - camera.yPlane;
            double rayDirX1 = camera.xDir + camera.xPlane;
            double rayDirY1 = camera.yDir + camera.yPlane;

            // Current y position compared to the center of the screen (the horizon)
            int p = y - height / 2;

            // Vertical position of the camera.
            double posZ = 0.5 * (double)height;

            // Horizontal distance from the camera to the floor for the current row.
            // 0.5 is the z position exactly in the middle between floor and ceiling.
            double rowDistance = posZ / p;

            // calculate the real world step vector we have to add for each x (parallel to camera plane)
            double floorStepX = rowDistance * (rayDirX1 - rayDirX0) / height;
            double floorStepY = rowDistance * (rayDirY1 - rayDirY0) / height;

            // real world coordinates of the leftmost column. This will be updated as we step to the right.
			//Position of each column starting from left to right
            double floorX = camera.xPos + rowDistance * rayDirX0;
            double floorY = camera.yPos + rowDistance * rayDirY0;

            for(int x = 0; x < width; ++x){
                // the cell coord is simply got from the integer parts of floorX and floorY
                int cellX = (int)(floorX);
                int cellY = (int)(floorY);

                // get the texture coordinate from the fractional part
                int tx = (int)(128 * (floorX - cellX)) & (128 - 1);
                int ty = (int)(128 * (floorY - cellY)) & (128 - 1);

                floorX += floorStepX;
                floorY += floorStepY;

                // choose texture and draw the pixel
                int floorTexture = 3;
                int ceilingTexture = 2;
                int color;

				//Draw floor texture
                if (tx < 128 && ty < 128) {
                    int index = ty * 128 + tx; // Calculate correct 1D index
                    if (index >= 0 && index < textures.get(floorTexture).pixels.length) {
                        color = textures.get(floorTexture).pixels[index];
                        color = (color >> 1) & 8355711; // Make it a bit darker
                        pixels[x + y * width] = color;
                    }
                }   

				//Draw ceiling texture
                if (tx < 128 && ty < 128) {
                    int index = ty * 128 + tx; 
                    if (index >= 0 && index < textures.get(ceilingTexture).pixels.length) {
                        color = textures.get(ceilingTexture).pixels[index];
                        color = (color >> 1) & 8355711; // Make it a bit darker
                        pixels[x + (height - y - 1) * width] = color;
                    }
                }
            }
        }

		//draw walls
	    for(int x=0; x<width; x=x+1) {
			double cameraX = (2 * x / (double)(width)) -1;
		    double rayDirX = camera.xDir + camera.xPlane * cameraX;
		    double rayDirY = camera.yDir + camera.yPlane * cameraX;
		    
			//postion of player
		    int mapX = (int)camera.xPos;
		    int mapY = (int)camera.yPos;

		    //length of ray from current position to next x or y-side
		    double sideDistX;
		    double sideDistY;

		    //Length of ray from one side to next in map 
		    double deltaDistX = Math.sqrt(1 + (rayDirY*rayDirY) / (rayDirX*rayDirX));
		    double deltaDistY = Math.sqrt(1 + (rayDirX*rayDirX) / (rayDirY*rayDirY));

			//double deltaDistX = (rayDirX == 0) ? 1e30 : Math.abs(1 / rayDirX);
      		//double deltaDistY = (rayDirY == 0) ? 1e30 : Math.abs(1 / rayDirY);

		    double perpWallDist; // length of the ray

		    //Direction to go in x and y
		    int stepX, stepY;

		    boolean hit = false; //was a wall hit
		    int side = 0; //was the wall vertical or horizontal

		    //Figure out the step direction and initial distance to a side
		    if (rayDirX < 0){
		    	stepX = -1;
		    	sideDistX = (camera.xPos - mapX) * deltaDistX;
		    }
		    else{
		    	stepX = 1;
		    	sideDistX = (mapX + 1.0 - camera.xPos) * deltaDistX;
		    }

		    if (rayDirY < 0){
		    	stepY = -1;
		        sideDistY = (camera.yPos - mapY) * deltaDistY;
		    }
		    else{
		    	stepY = 1;
		        sideDistY = (mapY + 1.0 - camera.yPos) * deltaDistY;
		    }

		    //Loop until ray hit a wall
		    while(!hit) {
		    	if (sideDistX < sideDistY)
		        {
		    		sideDistX += deltaDistX;
		    		mapX += stepX;
		    		side = 0; //vertical wall
		        }
		        else
		        {
		        	sideDistY += deltaDistY;
		        	mapY += stepY;
		        	side = 1; //horizontal wall
		        }
		    	//Check if ray has hit a wall
		    	if(map[mapX][mapY] > 0)
					hit = true;
		    }

            //calculate the distance
            if(side==0)
		    	perpWallDist = Math.abs((mapX - camera.xPos + (1 - stepX) / 2) / rayDirX);
		    else
		    	perpWallDist = Math.abs((mapY - camera.yPos + (1 - stepY) / 2) / rayDirY);

		    //Now calculate the height of the wall based on the distance from the camera
		    int lineHeight;
		    if(perpWallDist > 0) 
				lineHeight = Math.abs((int)(height / perpWallDist));
		    else 
				lineHeight = height;
		    
			//calculate lowest and highest pixel to fill in current stripe
		    int drawStart = -lineHeight / 2 + height / 2;

		    if(drawStart < 0)
		    	drawStart = 0;

		    int drawEnd = lineHeight /  2 + height / 2;

		    if(drawEnd >= height) 
		    	drawEnd = height - 1;

		    //add a texture
		    int texNum = map[mapX][mapY] - 1;

		    double wallX;//position of where wall was hit

		    if(side==1) {//If its a y-axis wall
		    	wallX = (camera.xPos + ((mapY - camera.yPos + (1 - stepY) / 2) / rayDirY) * rayDirX);
		    } 
			else{//X-axis wall
		    	wallX = (camera.yPos + ((mapX - camera.xPos + (1 - stepX) / 2) / rayDirX) * rayDirY);
		    }
		    wallX -= Math.floor(wallX);
            
		    //x coordinate on the texture
		    int texX = (int)(wallX * (textures.get(texNum).SIZE));
		    if(side == 0 && rayDirX > 0) 
                texX = textures.get(texNum).SIZE - texX - 1;
		    if(side == 1 && rayDirY < 0)
                texX = textures.get(texNum).SIZE - texX - 1;
		    //calculate y coordinate on texture
		    for(int y=drawStart; y<drawEnd; y++) {
		    	int texY = (((y*2 - height + lineHeight) << 6) / lineHeight) / 2;
		    	int color = 0;

		    	if(side==0) 
                    color = textures.get(texNum).pixels[texX + (texY * textures.get(texNum).SIZE)];
		    	else 
                if (texX < textures.get(texNum).SIZE && texY < textures.get(texNum).SIZE){
                    int index = texX + (texY * textures.get(texNum).SIZE);
                    
					if (index >= 0 && index < textures.get(texNum).pixels.length) 
                    	color = (textures.get(texNum).pixels[index] >>1) & 8355711;//Make y sides darker
                }
                pixels[x + y*(width)] = color;
		    }
        }

        return pixels;
    }
}
