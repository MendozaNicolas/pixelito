package org.pixelito;

import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.pixelito.block.Block;
import org.pixelito.block.BlockType;
import org.pixelito.camera.Camera;
import org.pixelito.graphics.ShaderProgram;
import org.pixelito.render.Mesh;
import org.pixelito.render.VoxelMesher;
import org.pixelito.window.Window;

public class Game {

    private Window window;
    private Mesh mesh;
    private ShaderProgram shader;
    private Camera camera;

    public void run() {
        init();
        loop();
        cleanup();
    }

    private void init() {
        window = new Window(1280, 720, "Pixelito", true);
        window.create();

        camera = new Camera();

        // Cargar shaders
        try {
            shader = new ShaderProgram(
                    "src/main/resources/shaders/block.vert",
                    "src/main/resources/shaders/block.frag"
            );
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }

        // Crear bloques
        Block[][][] blocks = new Block[16][4][16];
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 4; y++) {
                for (int z = 0; z < 16; z++) {
                    blocks[x][y][z] = new Block(BlockType.DIRT);
                }
            }
        }

        // Generar mesh
        VoxelMesher.MeshData data = VoxelMesher.generateMesh(blocks);
        mesh = new Mesh(data.vertices, data.indices);
    }

    private void loop() {
        Matrix4f projection = new Matrix4f().perspective(
                (float) Math.toRadians(70.0f),
                (float) window.getWidth() / window.getHeight(),
                0.1f,
                1000.0f
        );

        while (!window.shouldClose()) {
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);

            shader.bind();
            // Aca va la logica de renderizado y demas cosas
            int projLoc = GL20.glGetUniformLocation(shader.getId(), "projection");
            int viewLoc = GL20.glGetUniformLocation(shader.getId(), "view");

            GL20.glUniformMatrix4fv(projLoc, false, projection.get(new float[16]));
            GL20.glUniformMatrix4fv(viewLoc, false, camera.getViewMatrix().get(new float[16]));

            mesh.render();
            shader.unbind();

            window.update();
        }
    }

    private void cleanup() {
        shader.destroy();
        mesh.destroy();
        window.destroy();
    }

}
