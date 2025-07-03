package org.pixelito.block;

/**
 * Defines the different types of blocks in the game.
 */
public enum BlockType {
    AIR(false, 0, 0),
    DIRT(true, 0, 0),
    STONE(true, 1, 0),
    GRASS(true, 2, 0),
    SAND(true, 3, 0),
    WOOD(true, 0, 1),
    LEAVES(true, 1, 1),
    WATER(false, 2, 1),
    BRICK(true, 3, 1);

    private final boolean solid;
    private final int textureX; // X position in texture atlas (0-3)
    private final int textureY; // Y position in texture atlas (0-3)

    /**
     * Creates a new block type with texture coordinates in the texture atlas.
     * 
     * @param solid Whether the block is solid
     * @param textureX X position in the texture atlas (0-3)
     * @param textureY Y position in the texture atlas (0-3)
     */
    BlockType(boolean solid, int textureX, int textureY) {
        this.solid = solid;
        this.textureX = textureX;
        this.textureY = textureY;
    }

    /**
     * Checks if this block type is solid (collidable).
     * 
     * @return true if the block is solid
     */
    public boolean isSolid() {
        return solid;
    }

    /**
     * Gets the X position of this block's texture in the texture atlas.
     * 
     * @return X position (0-3)
     */
    public int getTextureX() {
        return textureX;
    }

    /**
     * Gets the Y position of this block's texture in the texture atlas.
     * 
     * @return Y position (0-3)
     */
    public int getTextureY() {
        return textureY;
    }
    
    /**
     * Gets the U coordinate for this block's texture in the texture atlas.
     * 
     * @return U coordinate (0.0-1.0)
     */
    public float getTextureU() {
        return textureX * 0.25f;
    }
    
    /**
     * Gets the V coordinate for this block's texture in the texture atlas.
     * 
     * @return V coordinate (0.0-1.0)
     */
    public float getTextureV() {
        return textureY * 0.25f;
    }
}