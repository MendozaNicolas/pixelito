package org.pixelito.render;

import org.pixelito.block.Block;
import org.pixelito.block.BlockType;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Implements the "Greedy Meshing" algorithm for voxel terrain optimization.
 * This algorithm combines adjacent faces of the same type into larger rectangles,
 * significantly reducing the number of vertices and triangles needed.
 */
public class GreedyMesher {
    // Direction vectors for the 6 faces of a block
    private static final int[][] FACE_DIRS = {
            {0, 0, 1},  // FRONT (SOUTH)
            {0, 0, -1}, // BACK (NORTH)
            {-1, 0, 0}, // LEFT (WEST)
            {1, 0, 0},  // RIGHT (EAST)
            {0, 1, 0},  // TOP (UP)
            {0, -1, 0}  // BOTTOM (DOWN)
    };

    // Adjacent axes for each face direction (used for greedy meshing)
    // For example, if we're scanning a front face (Z+), we move along the X and Y axes
    private static final int[][][] FACE_ADJACENTS = {
            {{1, 0, 0}, {0, 1, 0}}, // FRONT: Move along X and Y
            {{1, 0, 0}, {0, 1, 0}}, // BACK: Move along X and Y
            {{0, 0, 1}, {0, 1, 0}}, // LEFT: Move along Z and Y
            {{0, 0, 1}, {0, 1, 0}}, // RIGHT: Move along Z and Y
            {{1, 0, 0}, {0, 0, 1}}, // TOP: Move along X and Z
            {{1, 0, 0}, {0, 0, 1}}  // BOTTOM: Move along X and Z
    };

    /**
     * Generates an optimized mesh for a given 3D block array using the greedy meshing algorithm.
     *
     * @param blocks 3D array of blocks
     * @return MeshData with optimized vertices and indices
     */
    public static VoxelMesher.MeshData generateMesh(Block[][][] blocks) {
        int width = blocks.length;
        int height = blocks[0].length;
        int depth = blocks[0][0].length;

        List<Float> vertices = new ArrayList<>();
        List<Float> texCoords = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();
        int vertexCount = 0;

        // For each face direction
        for (int face = 0; face < 6; face++) {
            // The mask marks which blocks have a visible face in the current direction
            boolean[][][] mask = new boolean[width][height][depth];
            // Initialize the mask for this face direction
            initMask(blocks, mask, face, width, height, depth);

            // The direction we're moving in to find adjacent faces of the same type
            int[] dir = FACE_DIRS[face];
            int[] du = FACE_ADJACENTS[face][0]; // First adjacent direction
            int[] dv = FACE_ADJACENTS[face][1]; // Second adjacent direction

            // For greedy meshing, we work on 2D slices
            int dimU = Math.abs(du[0]) == 1 ? width : (Math.abs(du[2]) == 1 ? depth : width);
            int dimV = Math.abs(dv[1]) == 1 ? height : (Math.abs(dv[2]) == 1 ? depth : height);
            int dimW = Math.abs(dir[0]) == 1 ? width : (Math.abs(dir[1]) == 1 ? height : depth);

            // For each slice along the normal direction
            for (int w = 0; w < dimW; w++) {
                // Sweep through the slice to find rectangles
                for (int v = 0; v < dimV; v++) {
                    for (int u = 0; u < dimU; u++) {
                        // If this block face is already part of a merged face or not visible, skip it
                        int[] pos = getBlockPos(u, v, w, face, du, dv, dir);
                        int x = pos[0], y = pos[1], z = pos[2];
                        if (x < 0 || y < 0 || z < 0 || x >= width || y >= height || z >= depth || !mask[x][y][z]) {
                            continue;
                        }

                        // Get the block type for this face
                        Block block = blocks[x][y][z];
                        if (block == null) continue;
                        BlockType blockType = block.getType();

                        // Expand along U direction as far as possible
                        int uEnd;
                        for (uEnd = u + 1; uEnd < dimU; uEnd++) {
                            pos = getBlockPos(uEnd, v, w, face, du, dv, dir);
                            int nx = pos[0], ny = pos[1], nz = pos[2];
                            if (nx < 0 || ny < 0 || nz < 0 || nx >= width || ny >= height || nz >= depth || 
                                !mask[nx][ny][nz] || blocks[nx][ny][nz] == null || 
                                blocks[nx][ny][nz].getType() != blockType) {
                                break;
                            }
                        }

                        // Expand along V direction as far as possible
                        int vEnd;
                        expandV: for (vEnd = v + 1; vEnd < dimV; vEnd++) {
                            for (int uu = u; uu < uEnd; uu++) {
                                pos = getBlockPos(uu, vEnd, w, face, du, dv, dir);
                                int nx = pos[0], ny = pos[1], nz = pos[2];
                                if (nx < 0 || ny < 0 || nz < 0 || nx >= width || ny >= height || nz >= depth || 
                                    !mask[nx][ny][nz] || blocks[nx][ny][nz] == null || 
                                    blocks[nx][ny][nz].getType() != blockType) {
                                    break expandV;
                                }
                            }
                        }

                        // Mark the rectangle in the mask as processed
                        for (int vv = v; vv < vEnd; vv++) {
                            for (int uu = u; uu < uEnd; uu++) {
                                pos = getBlockPos(uu, vv, w, face, du, dv, dir);
                                int nx = pos[0], ny = pos[1], nz = pos[2];
                                if (nx >= 0 && ny >= 0 && nz >= 0 && nx < width && ny < height && nz < depth) {
                                    mask[nx][ny][nz] = false;
                                }
                            }
                        }

                        // Add the quad (two triangles) to the mesh
                        vertexCount = addGreedyQuad(vertices, texCoords, indices, vertexCount, 
                                u, v, w, uEnd - u, vEnd - v, face, du, dv, dir, blockType);
                    }
                }
            }
        }

        // Create and return the mesh data
        return new VoxelMesher.MeshData(vertices, texCoords, indices);
    }

    /**
     * Initializes the mask for visible faces in the given direction
     */
    private static void initMask(Block[][][] blocks, boolean[][][] mask, int face, int width, int height, int depth) {
        int[] dir = FACE_DIRS[face];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < depth; z++) {
                    Block block = blocks[x][y][z];
                    if (block == null || !block.isSolid()) {
                        mask[x][y][z] = false;
                        continue;
                    }

                    int nx = x + dir[0];
                    int ny = y + dir[1];
                    int nz = z + dir[2];

                    // Check if the neighbor is solid
                    boolean neighborSolid = nx >= 0 && nx < width && 
                                          ny >= 0 && ny < height && 
                                          nz >= 0 && nz < depth && 
                                          blocks[nx][ny][nz] != null && 
                                          blocks[nx][ny][nz].isSolid();

                    // Face is visible if the neighbor is not solid
                    mask[x][y][z] = !neighborSolid;
                }
            }
        }
    }

    /**
     * Converts a position in the greedy meshing algorithm's coordinate system
     * to actual block coordinates
     */
    private static int[] getBlockPos(int u, int v, int w, int face, int[] du, int[] dv, int[] dir) {
        int x = w * dir[0] + u * du[0] + v * dv[0];
        int y = w * dir[1] + u * du[1] + v * dv[1];
        int z = w * dir[2] + u * du[2] + v * dv[2];
        return new int[]{x, y, z};
    }

    /**
     * Adds a greedy mesh quad (merged face) to the mesh data
     */
    private static int addGreedyQuad(List<Float> vertices, List<Float> texCoords, List<Integer> indices, 
                                   int vertexCount, int u, int v, int w, int sizeU, int sizeV, 
                                   int face, int[] du, int[] dv, int[] dir, BlockType blockType) {
        // Calculate the positions of the quad corners
        float[] positions = new float[12]; // 4 vertices * 3 components (x,y,z)
        int[] quadPos = getBlockPos(u, v, w, face, du, dv, dir);
        int x = quadPos[0], y = quadPos[1], z = quadPos[2];

        // Determine the vertices based on the face and the size of the quad
        float x1 = x;
        float y1 = y;
        float z1 = z;
        float x2 = x + du[0] * sizeU + dv[0] * sizeV;
        float y2 = y + du[1] * sizeU + dv[1] * sizeV;
        float z2 = z + du[2] * sizeU + dv[2] * sizeV;

        // Adjust corners for different face orientations
        if (face == 0) { // FRONT
            positions[0] = x1; positions[1] = y1; positions[2] = z1 + 1; // Bottom-left
            positions[3] = x2; positions[4] = y1; positions[5] = z1 + 1; // Bottom-right
            positions[6] = x2; positions[7] = y2; positions[8] = z1 + 1; // Top-right
            positions[9] = x1; positions[10] = y2; positions[11] = z1 + 1; // Top-left
        } else if (face == 1) { // BACK
            positions[0] = x2; positions[1] = y1; positions[2] = z1; // Bottom-left
            positions[3] = x1; positions[4] = y1; positions[5] = z1; // Bottom-right
            positions[6] = x1; positions[7] = y2; positions[8] = z1; // Top-right
            positions[9] = x2; positions[10] = y2; positions[11] = z1; // Top-left
        } else if (face == 2) { // LEFT
            positions[0] = x1; positions[1] = y1; positions[2] = z1; // Bottom-left
            positions[3] = x1; positions[4] = y1; positions[5] = z2; // Bottom-right
            positions[6] = x1; positions[7] = y2; positions[8] = z2; // Top-right
            positions[9] = x1; positions[10] = y2; positions[11] = z1; // Top-left
        } else if (face == 3) { // RIGHT
            positions[0] = x1 + 1; positions[1] = y1; positions[2] = z2; // Bottom-left
            positions[3] = x1 + 1; positions[4] = y1; positions[5] = z1; // Bottom-right
            positions[6] = x1 + 1; positions[7] = y2; positions[8] = z1; // Top-right
            positions[9] = x1 + 1; positions[10] = y2; positions[11] = z2; // Top-left
        } else if (face == 4) { // TOP
            positions[0] = x1; positions[1] = y1 + 1; positions[2] = z2; // Bottom-left
            positions[3] = x2; positions[4] = y1 + 1; positions[5] = z2; // Bottom-right
            positions[6] = x2; positions[7] = y1 + 1; positions[8] = z1; // Top-right
            positions[9] = x1; positions[10] = y1 + 1; positions[11] = z1; // Top-left
        } else if (face == 5) { // BOTTOM
            positions[0] = x1; positions[1] = y1; positions[2] = z1; // Bottom-left
            positions[3] = x2; positions[4] = y1; positions[5] = z1; // Bottom-right
            positions[6] = x2; positions[7] = y1; positions[8] = z2; // Top-right
            positions[9] = x1; positions[10] = y1; positions[11] = z2; // Top-left
        }

        // Add the vertices to the list
        for (float pos : positions) {
            vertices.add(pos);
        }

        // Add texture coordinates based on the size of the quad and block type
        // For now, we'll use simple coordinates based on the block type
        float uMin = blockType.getTextureU();
        float vMin = blockType.getTextureV();
        float uMax = uMin + 0.25f * sizeU;
        float vMax = vMin + 0.25f * sizeV;

        texCoords.add(uMin); texCoords.add(vMax); // Bottom-left
        texCoords.add(uMax); texCoords.add(vMax); // Bottom-right
        texCoords.add(uMax); texCoords.add(vMin); // Top-right
        texCoords.add(uMin); texCoords.add(vMin); // Top-left

        // Add the indices for two triangles
        indices.add(vertexCount);
        indices.add(vertexCount + 1);
        indices.add(vertexCount + 2);
        indices.add(vertexCount + 2);
        indices.add(vertexCount + 3);
        indices.add(vertexCount);

        // Return the new vertex count
        return vertexCount + 4;
    }
}
