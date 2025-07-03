package org.pixelito.render;

import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL11;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.lwjgl.system.MemoryUtil;

/**
 * Represents a 3D mesh with vertex positions, texture coordinates, and indices.
 */
public class Mesh {

    private final int vaoId;
    private final int posVboId;
    private final int texCoordsVboId;
    private final int iboId;
    private final int vertexCount;

    /**
     * Creates a mesh with vertices, texture coordinates, and indices.
     * 
     * @param vertices Position data for the mesh (x,y,z triplets)
     * @param texCoords Texture coordinate data (u,v pairs)
     * @param indices Index data for the triangles
     */
    public Mesh(float[] vertices, float[] texCoords, int[] indices) {
        vertexCount = indices.length;

        // Create VAO
        vaoId = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoId);

        // Create position VBO (location 0)
        posVboId = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, posVboId);
        FloatBuffer vertexBuffer = MemoryUtil.memAllocFloat(vertices.length);
        vertexBuffer.put(vertices).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, vertexBuffer, GL15.GL_STATIC_DRAW);
        GL20.glEnableVertexAttribArray(0);
        GL20.glVertexAttribPointer(0, 3, GL15.GL_FLOAT, false, 0, 0);

        // Create texture coordinates VBO (location 1)
        texCoordsVboId = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, texCoordsVboId);
        FloatBuffer texCoordsBuffer = MemoryUtil.memAllocFloat(texCoords.length);
        texCoordsBuffer.put(texCoords).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, texCoordsBuffer, GL15.GL_STATIC_DRAW);
        GL20.glEnableVertexAttribArray(1);
        GL20.glVertexAttribPointer(1, 2, GL15.GL_FLOAT, false, 0, 0);

        // Create index buffer
        iboId = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ELEMENT_ARRAY_BUFFER, iboId);
        IntBuffer indexBuffer = MemoryUtil.memAllocInt(indices.length);
        indexBuffer.put(indices).flip();
        GL15.glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, indexBuffer, GL15.GL_STATIC_DRAW);

        // Unbind everything
        GL30.glBindVertexArray(0);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        // Free buffers
        MemoryUtil.memFree(vertexBuffer);
        MemoryUtil.memFree(texCoordsBuffer);
        MemoryUtil.memFree(indexBuffer);
    }

    /**
     * Legacy constructor for meshes without texture coordinates.
     * 
     * @param vertices Position data for the mesh
     * @param indices Index data for the triangles
     */
    public Mesh(float[] vertices, int[] indices) {
        this(vertices, createEmptyTexCoords(vertices.length / 3), indices);
    }

    /**
     * Helper method to create empty texture coordinates for legacy support.
     */
    private static float[] createEmptyTexCoords(int vertexCount) {
        float[] texCoords = new float[vertexCount * 2];
        for (int i = 0; i < vertexCount; i++) {
            texCoords[i * 2] = 0.0f;
            texCoords[i * 2 + 1] = 0.0f;
        }
        return texCoords;
    }

    /**
     * Renders the mesh.
     */
    public void render() {
        GL30.glBindVertexArray(vaoId);
        GL11.glDrawElements(GL11.GL_TRIANGLES, vertexCount, GL11.GL_UNSIGNED_INT, 0);
        GL30.glBindVertexArray(0);
    }

    /**
     * Destroys the mesh and frees all resources.
     */
    public void destroy() {
        GL20.glDisableVertexAttribArray(0);
        GL20.glDisableVertexAttribArray(1);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL15.glDeleteBuffers(posVboId);
        GL15.glDeleteBuffers(texCoordsVboId);
        GL15.glDeleteBuffers(iboId);

        GL30.glBindVertexArray(0);
        GL30.glDeleteVertexArrays(vaoId);
    }
}
