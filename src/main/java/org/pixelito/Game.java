package org.pixelito;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;
import org.pixelito.block.Block;
import org.pixelito.block.BlockType;
import org.pixelito.camera.Camera;
import org.pixelito.graphics.ShaderProgram;
import org.pixelito.input.InputManager;
import org.pixelito.input.KeyCode;
import org.pixelito.input.Keyboard;
import org.pixelito.input.Mouse;
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

        InputManager.setup(window.getWindowHandle());
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



            // LÓGICA DE LOS CONTROLES
            float speed = 0.1f;

            float yaw = (float) Math.toRadians(camera.getRotation().y);
            float dx = (float) Math.sin(yaw) * speed;
            float dz = (float) Math.cos(yaw) * speed;

            Vector3f pos = camera.getPosition();

            // Movimiento frontal
            if (Keyboard.isKeyDown(KeyCode.W)) {
                pos.x += dx;
                pos.z -= dz;
            }
            if (Keyboard.isKeyDown(KeyCode.S)) {
                pos.x -= dx;
                pos.z += dz;
            }

            // Movimiento lateral (perpendicular)
            if (Keyboard.isKeyDown(KeyCode.A)) {
                pos.x -= dz;
                pos.z -= dx;
            }
            if (Keyboard.isKeyDown(KeyCode.D)) {
                pos.x += dz;
                pos.z += dx;
            }

            if (Keyboard.isKeyDown(KeyCode.SPACE)) {
                pos.y += speed;
            }

            if (Keyboard.isKeyDown(KeyCode.LEFT_SHIFT)) {
                pos.y -= speed;
            }

            if (Keyboard.isKeyDown(KeyCode.ESCAPE)) {
                GLFW.glfwSetInputMode(window.getWindowHandle(), GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_NORMAL);
            }

            // Mouse look
            camera.getRotation().y += (float) Mouse.getDeltaX() * 0.1f;
            camera.getRotation().x += (float) Mouse.getDeltaY() * 0.1f;

            // Limitar ángulo vertical (para que no dé la vuelta)
            camera.getRotation().x = Math.max(-90, Math.min(90, camera.getRotation().x));

            // Reset mouse deltas
            Mouse.resetDeltas();
            // FIN LÓGICA DE LOS CONTROLES





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
