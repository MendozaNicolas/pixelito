package org.pixelito.render;

import org.pixelito.block.Block;
import org.pixelito.block.BlockType;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class VoxelMesher {

    // Posiciones de los vértices de un cubo (caras unitarias)
    private static final float[][] FACE_VERTICES = {
            // FRONT
            {0, 0, 1}, {1, 0, 1}, {1, 1, 1}, {0, 1, 1},
            // BACK
            {1, 0, 0}, {0, 0, 0}, {0, 1, 0}, {1, 1, 0},
            // LEFT
            {0, 0, 0}, {0, 0, 1}, {0, 1, 1}, {0, 1, 0},
            // RIGHT
            {1, 0, 1}, {1, 0, 0}, {1, 1, 0}, {1, 1, 1},
            // TOP
            {0, 1, 1}, {1, 1, 1}, {1, 1, 0}, {0, 1, 0},
            // BOTTOM
            {0, 0, 0}, {1, 0, 0}, {1, 0, 1}, {0, 0, 1},
    };

    // Texture coordinates for each vertex of a cube face
    private static final float[][] FACE_TEXCOORDS = {
            // For each face, we define UV coordinates for each vertex
            // FRONT
            {0, 1}, {1, 1}, {1, 0}, {0, 0},
            // BACK
            {0, 1}, {1, 1}, {1, 0}, {0, 0},
            // LEFT
            {0, 1}, {1, 1}, {1, 0}, {0, 0},
            // RIGHT
            {0, 1}, {1, 1}, {1, 0}, {0, 0},
            // TOP
            {0, 1}, {1, 1}, {1, 0}, {0, 0},
            // BOTTOM
            {0, 1}, {1, 1}, {1, 0}, {0, 0},
    };

    // Índices para formar triángulos por cara
    private static final int[] FACE_INDICES = {
            0, 1, 2, 2, 3, 0
    };

    // Orden de caras: FRONT, BACK, LEFT, RIGHT, TOP, BOTTOM
    private static final int[][] FACE_OFFSETS = {
            {0, 0, 1},   // FRONT
            {0, 0, -1},  // BACK
            {-1, 0, 0},  // LEFT
            {1, 0, 0},   // RIGHT
            {0, 1, 0},   // TOP
            {0, -1, 0},  // BOTTOM
    };

    /**
     * Generate a simple mesh for the given blocks, one face per visible block face.
     * This is less efficient than the greedy meshing algorithm.
     * 
     * @param blocks 3D array of blocks
     * @return MeshData with vertices, texture coordinates, and indices
     */
    public static MeshData generateMesh(Block[][][] blocks) {
        int width = blocks.length;
        int height = blocks[0].length;
        int depth = blocks[0][0].length;

        List<Float> vertices = new ArrayList<>();
        List<Float> texCoords = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        int indexOffset = 0;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    Block block = blocks[x][y][z];
                    if (block == null || !block.isSolid()) continue;

                    for (int face = 0; face < 6; face++) {
                        int nx = x + FACE_OFFSETS[face][0];
                        int ny = y + FACE_OFFSETS[face][1];
                        int nz = z + FACE_OFFSETS[face][2];

                        boolean neighborSolid =
                                nx >= 0 && nx < width &&
                                        ny >= 0 && ny < height &&
                                        nz >= 0 && nz < depth &&
                                        blocks[nx][ny][nz] != null &&
                                        blocks[nx][ny][nz].isSolid();

                        if (!neighborSolid) {
                            int baseIndex = face * 4;
                            
                            // Add vertices
                            for (int i = 0; i < 4; i++) {
                                float[] vertex = FACE_VERTICES[baseIndex + i];
                                vertices.add(x + vertex[0]);
                                vertices.add(y + vertex[1]);
                                vertices.add(z + vertex[2]);
                            }

                            // Add texture coordinates
                            BlockType blockType = block.getType();
                            float uMin = blockType.getTextureU();
                            float vMin = blockType.getTextureV();
                            
                            for (int i = 0; i < 4; i++) {
                                float[] texCoord = FACE_TEXCOORDS[baseIndex + i];
                                texCoords.add(uMin + texCoord[0] * 0.25f);
                                texCoords.add(vMin + texCoord[1] * 0.25f);
                            }

                            // Add indices
                            for (int i = 0; i < FACE_INDICES.length; i++) {
                                indices.add(indexOffset + FACE_INDICES[i]);
                            }

                            indexOffset += 4;
                        }
                    }
                }
            }
        }

        return new MeshData(vertices, texCoords, indices);
    }

    /**
     * MeshData class to hold the mesh information
     */
    public static class MeshData {
        public final float[] vertices;
        public final float[] texCoords;
        public final int[] indices;

        /**
         * Constructor for MeshData that takes Lists of Floats and Integers
         */
        public MeshData(List<Float> verts, List<Float> texs, List<Integer> inds) {
            this.vertices = new float[verts.size()];
            this.texCoords = new float[texs.size()];
            this.indices = new int[inds.size()];
            
            for (int i = 0; i < verts.size(); i++) this.vertices[i] = verts.get(i);
            for (int i = 0; i < texs.size(); i++) this.texCoords[i] = texs.get(i);
            for (int i = 0; i < inds.size(); i++) this.indices[i] = inds.get(i);
        }

        /**
         * Legacy constructor for backward compatibility
         */
        public MeshData(List<Float> verts, List<Integer> inds) {
            this.vertices = new float[verts.size()];
            this.indices = new int[inds.size()];
            
            // Create empty texture coordinates for compatibility
            this.texCoords = new float[verts.size() / 3 * 2];
            
            for (int i = 0; i < verts.size(); i++) this.vertices[i] = verts.get(i);
            for (int i = 0; i < inds.size(); i++) this.indices[i] = inds.get(i);
        }
    }
}
