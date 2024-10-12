import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

public class Pixellate extends JFrame {
    private int gridSize; // Size of the grid
    private int cellSize; // Size of each cell (pixels)
    private Color defaultColor; // Default color of each cell
    private Color[][] grid; // Grid representing the pixel art
    private Color currentColor; // Default selected color
    private boolean eraserMode; // Eraser mode toggle
    private boolean showGrid; // Toggle for showing grid lines

    public Pixellate() {
        gridSize = 30; // Default grid size
        cellSize = 20; // Default cell size
        defaultColor = Color.WHITE; // Default color of each cell
        currentColor = Color.BLACK; // Default selected color
        eraserMode = false; // Eraser mode toggle
        showGrid = true; // Toggle for showing grid lines

        // Initialize the grid
        grid = new Color[gridSize][gridSize];
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                grid[i][j] = defaultColor;
            }
        }

        // Set up the JFrame
        setTitle("Pixellate");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(gridSize * cellSize + 200, gridSize * cellSize + 100); // Adjust size as needed
        setLocationRelativeTo(null);

        // Create a JPanel to hold the grid
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Draw the grid
                for (int i = 0; i < gridSize; i++) {
                    for (int j = 0; j < gridSize; j++) {
                        g.setColor(grid[i][j]);
                        g.fillRect(i * cellSize, j * cellSize, cellSize, cellSize);
                        if (showGrid) {
                            g.setColor(Color.BLACK);
                            g.drawRect(i * cellSize, j * cellSize, cellSize, cellSize);
                        }
                    }
                }
            }
        };

        // Add mouse listener for real-time drawing
        MouseAdapter mouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleMouseInteraction(e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                handleMouseInteraction(e);
            }

            private void handleMouseInteraction(MouseEvent e) {
                int x = e.getX() / cellSize;
                int y = e.getY() / cellSize;
                // Ensure the clicked cell is within grid bounds
                if (x >= 0 && x < gridSize && y >= 0 && y < gridSize) {
                    if (eraserMode) {
                        grid[x][y] = defaultColor; // Erase if eraser mode is on
                    } else {
                        grid[x][y] = currentColor; // Use the currently selected color
                    }
                    panel.repaint();
                }
            }
        };

        panel.addMouseListener(mouseAdapter);
        panel.addMouseMotionListener(mouseAdapter);

        // Create the menu bar
        JMenuBar menuBar = new JMenuBar();

        // Create the "File" menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem newCanvasMenuItem = new JMenuItem("New Canvas");
        newCanvasMenuItem.addActionListener(e -> resetCanvas()); // Reset canvas action
        fileMenu.add(newCanvasMenuItem);
        
        JMenuItem openMenuItem = new JMenuItem("Open");
        openMenuItem.addActionListener(e -> openArt(panel)); // Open artwork action
        fileMenu.add(openMenuItem);

        JMenuItem saveMenuItem = new JMenuItem("Save");
        saveMenuItem.addActionListener(e -> saveArt(panel));
        fileMenu.add(saveMenuItem);

        // Create the "Color" menu
        JMenu colorMenu = new JMenu("Color");
        JMenuItem chooseColorMenuItem = new JMenuItem("Choose Color");
        chooseColorMenuItem.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(this, "Choose a Color", currentColor);
            if (newColor != null) {
                currentColor = newColor; // Update the current color when a new color is chosen
            }
        });
        colorMenu.add(chooseColorMenuItem);

        // Create the "Tools" menu
        JMenu toolsMenu = new JMenu("Tools");
        JMenuItem drawMenuItem = new JMenuItem("Draw");
        drawMenuItem.addActionListener(e -> {
            eraserMode = false; // Set to draw mode
        });
        toolsMenu.add(drawMenuItem);

        JMenuItem eraserMenuItem = new JMenuItem("Eraser");
        eraserMenuItem.addActionListener(e -> {
            eraserMode = true; // Set to eraser mode
        });
        toolsMenu.add(eraserMenuItem);

        // Create the "Canvas" menu
        JMenu canvasMenu = new JMenu("Canvas");
        JMenuItem clearMenuItem = new JMenuItem("Clear Canvas");
        clearMenuItem.addActionListener(e -> {
            for (int i = 0; i < gridSize; i++) {
                for (int j = 0; j < gridSize; j++) {
                    grid[i][j] = defaultColor;
                }
            }
            panel.repaint();
        });
        canvasMenu.add(clearMenuItem);

        JMenuItem changeCanvasColorMenuItem = new JMenuItem("Change Canvas Color");
        changeCanvasColorMenuItem.addActionListener(e -> {
            Color newCanvasColor = JColorChooser.showDialog(this, "Choose Canvas Color", defaultColor);
            if (newCanvasColor != null) {
                Color previousDefaultColor = defaultColor;
                defaultColor = newCanvasColor;
                // Update all cells that currently have the previous default color
                for (int i = 0; i < gridSize; i++) {
                    for (int j = 0; j < gridSize; j++) {
                        if (grid[i][j].equals(previousDefaultColor)) {
                            grid[i][j] = defaultColor;
                        }
                    }
                }
                panel.repaint();
            }
        });
        canvasMenu.add(changeCanvasColorMenuItem);

        // Create the "View" menu
        JMenu viewMenu = new JMenu("View");
        JMenuItem toggleGridMenuItem = new JMenuItem("Toggle Grid");
        toggleGridMenuItem.addActionListener(e -> {
            showGrid = !showGrid; // Toggle the grid visibility
            panel.repaint(); // Repaint the panel to reflect the change
        });
        viewMenu.add(toggleGridMenuItem);

        // Add grid size options to the view menu
        JMenuItem smallGridMenuItem = new JMenuItem("Small Grid (20x20)");
        smallGridMenuItem.addActionListener(e -> setGridSize(20));
        viewMenu.add(smallGridMenuItem);

        JMenuItem mediumGridMenuItem = new JMenuItem("Medium Grid (30x30)");
        mediumGridMenuItem.addActionListener(e -> setGridSize(30));
        viewMenu.add(mediumGridMenuItem);

        JMenuItem largeGridMenuItem = new JMenuItem("Large Grid (40x40)");
        largeGridMenuItem.addActionListener(e -> setGridSize(40));
        viewMenu.add(largeGridMenuItem);
        
        JMenuItem fullScreenMenuItem = new JMenuItem("Full-Screen Grid");
        fullScreenMenuItem.addActionListener(e -> setFullScreenGrid());
        viewMenu.add(fullScreenMenuItem);

        // Add menus to the menu bar
        menuBar.add(fileMenu);
        menuBar.add(colorMenu);
        menuBar.add(toolsMenu);
        menuBar.add(canvasMenu);
        menuBar.add(viewMenu);

        // Set the menu bar for the frame
        setJMenuBar(menuBar);

        // Add the grid panel to the frame
        add(panel, BorderLayout.CENTER);
        setVisible(true);
    }

    // Reset the canvas to its default state
    private void resetCanvas() {
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                grid[i][j] = defaultColor; // Set all cells to default color
            }
        }
        repaint(); // Repaint to reflect the changes
    }

    // Open an old artwork
    private void openArt(JPanel panel) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("PNG Images", "png"));
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            loadArt(file, panel); // Load the selected artwork
        }
    }

    // Load artwork from a file
    private void loadArt(File file, JPanel panel) {
        try {
            BufferedImage image = ImageIO.read(file);
            // Ensure the image dimensions match the grid size
            if (image.getWidth() == gridSize * cellSize && image.getHeight() == gridSize * cellSize) {
                // Load the image into the grid
                for (int i = 0; i < gridSize; i++) {
                    for (int j = 0; j < gridSize; j++) {
                        grid[i][j] = new Color(image.getRGB(i * cellSize, j * cellSize));
                    }
                }
                panel.repaint(); // Repaint the panel to display the loaded image
            } else {
                System.err.println("Image dimensions do not match the grid size.");
            }
        } catch (IOException ex) {
            System.err.println("Error opening the image: " + ex.getMessage());
        }
    }

    // Save the artwork to a file
    private void saveArt(JPanel panel) {
        // Temporarily hide the grid
        boolean previousShowGrid = showGrid;
        showGrid = false; // Hide grid

        // Create a new BufferedImage for the artwork
        BufferedImage image = new BufferedImage(gridSize * cellSize, gridSize * cellSize, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // Draw the pixel art onto the BufferedImage
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                g2d.setColor(grid[i][j]);
                g2d.fillRect(i * cellSize, j * cellSize, cellSize, cellSize);
            }
        }
        g2d.dispose(); // Dispose of the graphics context

        // Show file chooser to save the image
        JFileChooser fileChooser = new JFileChooser();
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            // Ensure the file has a .png extension
            if (!file.getAbsolutePath().toLowerCase().endsWith(".png")) {
                file = new File(file.getAbsolutePath() + ".png");
            }

            try {
                ImageIO.write(image, "png", file);
                System.out.println("Image saved successfully: " + file.getAbsolutePath());
            } catch (IOException ex) {
                System.err.println("Error saving the image: " + ex.getMessage());
            }
        }

        // Restore the grid visibility
        showGrid = previousShowGrid;
        panel.repaint(); // Repaint the panel to reflect the grid
    }

    // Set the grid size and resize the canvas
    private void setGridSize(int newSize) {
        this.gridSize = newSize; // Set new grid size
        this.cellSize = 20; // Reset cell size to default
        this.grid = new Color[gridSize][gridSize]; // Create a new grid array

        // Initialize the new grid with the default color
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                grid[i][j] = defaultColor;
            }
        }

        // Resize the JFrame based on the new grid size
        setSize(gridSize * cellSize + 200, gridSize * cellSize + 100);
        repaint(); // Repaint the panel to reflect the changes
    }
    
    // Set full-screen grid
    private void setFullScreenGrid() {
        // Get the screen size
        Dimension screenSize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        this.gridSize = (int) (Math.min(screenSize.getWidth(), screenSize.getHeight()) / cellSize);
        this.grid = new Color[gridSize][gridSize]; // Create a new grid array

        // Initialize the new grid with the default color
        for (int i = 0; i < gridSize; i++) {
            for (int j = 0; j < gridSize; j++) {
                grid[i][j] = defaultColor;
            }
        }

        // Resize the JFrame to full screen
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        repaint(); // Repaint the panel to reflect the changes
    }

    public static void main(String[] args) {
        new Pixellate();
    }
}
