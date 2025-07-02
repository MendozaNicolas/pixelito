package org.pixelito.block;

public class Block {
    private final BlockType type;

    public Block(BlockType type) {
        this.type = type;
    }

    public BlockType getType() {
        return type;
    }

    public boolean isSolid() {
        return type.isSolid();
    }
}
