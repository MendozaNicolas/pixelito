package org.pixelito;

/**
 * The main entry point for the Pixelito application.
 */
public class Main {
    /**
     * Main method that starts the game.
     * 
     * @param args Command-line arguments
     */
    public static void main(String[] args) {
        System.out.println("Iniciando pixelito optimizado...");
        System.out.println("Controls:");
        System.out.println("- WASD: Move the camera");
        System.out.println("- Mouse: Look around");
        System.out.println("- Space: Move up");
        System.out.println("- Shift: Move down");
        System.out.println("- ESC: Toggle mouse capture");
        System.out.println("- G: Toggle between meshing algorithms");
        System.out.println("- P: Print current camera position");
        
        Game game = new Game();
        
        // Configure options here
        game.setUseGreedyMesher(true); // Use optimized meshing by default
        
        // Start the game
        game.run();
    }
}