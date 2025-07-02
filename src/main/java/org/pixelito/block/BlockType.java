package org.pixelito.block;

public enum BlockType {
    AIR(false),
    DIRT(true),
    STONE(true),
    GRASS(true);

    private final boolean solid;

    BlockType(boolean solid) {
        this.solid = solid;
    }

    public boolean isSolid() {
        return solid;
    }
}