package com.xiangqi.client.ui;

import com.xiangqi.shared.model.*;
import com.xiangqi.client.multimedia.ResourceManager;
import com.xiangqi.client.multimedia.AudioManager;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Chess board panel that displays the Xiangqi game board and handles piece interactions.
 * Supports drag-and-drop and click-to-move functionality with move highlighting.
 */
public class ChessBoardPanel extends JPanel {
    private static final int BOARD_WIDTH = 521;  // Based on typical Xiangqi board proportions
    private static final int BOARD_HEIGHT = 577;
    private static final int CELL_WIDTH = BOARD_WIDTH / Position.BOARD_COLS;
    private static final int CELL_HEIGHT = BOARD_HEIGHT / Position.BOARD_ROWS;
    
    private GameState gameState;
    private Player localPlayer;  // The player viewing this board
    private boolean boardFlipped; // True if board should be flipped (for black player)
    private Position selectedPosition;
    private List<Position> validMoves;
    private Map<String, Image> pieceImages;
    private ResourceManager resourceManager;
    private AudioManager audioManager;
    
    // Drag and drop state
    private ChessPiece draggedPiece;
    private Point dragOffset;
    private boolean isDragging;
    
    // Event listeners
    private BoardEventListener eventListener;
    
    public interface BoardEventListener {
        void onMoveAttempted(Position from, Position to);
        void onPieceSelected(Position position);
    }
    
    public ChessBoardPanel() {
        setPreferredSize(new Dimension(BOARD_WIDTH, BOARD_HEIGHT));
        setBackground(Color.WHITE);
        
        // Initialize resource managers
        resourceManager = ResourceManager.getInstance();
        audioManager = resourceManager.getAudioManager();
        
        loadImages();
        setupMouseListeners();
    }
    
    /**
     * Load all chess piece images and board graphics using ResourceManager.
     */
    private void loadImages() {
        pieceImages = new HashMap<>();
        
        // Load piece images using ResourceManager - mapping piece types to image numbers
        // Red pieces (1-16): General(5), Advisor(4,6), Elephant(3,7), Horse(8,2), 
        // Chariot(1,9), Cannon(10,11), Soldier(12,13,14,15,16)
        ImageIcon redGeneral = resourceManager.getPieceImage(5);
        if (redGeneral != null) pieceImages.put("red_general", redGeneral.getImage());
        
        ImageIcon redAdvisor = resourceManager.getPieceImage(4);
        if (redAdvisor != null) pieceImages.put("red_advisor", redAdvisor.getImage());
        
        ImageIcon redElephant = resourceManager.getPieceImage(3);
        if (redElephant != null) pieceImages.put("red_elephant", redElephant.getImage());
        
        ImageIcon redHorse = resourceManager.getPieceImage(8);
        if (redHorse != null) pieceImages.put("red_horse", redHorse.getImage());
        
        ImageIcon redChariot = resourceManager.getPieceImage(1);
        if (redChariot != null) pieceImages.put("red_chariot", redChariot.getImage());
        
        ImageIcon redCannon = resourceManager.getPieceImage(10);
        if (redCannon != null) pieceImages.put("red_cannon", redCannon.getImage());
        
        ImageIcon redSoldier = resourceManager.getPieceImage(12);
        if (redSoldier != null) pieceImages.put("red_soldier", redSoldier.getImage());
        
        // Black pieces (17-32): General(21), Advisor(20,22), Elephant(19,23), Horse(18,24),
        // Chariot(17,25), Cannon(26,27), Soldier(28,29,30,31,32)
        ImageIcon blackGeneral = resourceManager.getPieceImage(21);
        if (blackGeneral != null) pieceImages.put("black_general", blackGeneral.getImage());
        
        ImageIcon blackAdvisor = resourceManager.getPieceImage(20);
        if (blackAdvisor != null) pieceImages.put("black_advisor", blackAdvisor.getImage());
        
        ImageIcon blackElephant = resourceManager.getPieceImage(23);
        if (blackElephant != null) pieceImages.put("black_elephant", blackElephant.getImage());
        
        ImageIcon blackHorse = resourceManager.getPieceImage(18);
        if (blackHorse != null) pieceImages.put("black_horse", blackHorse.getImage());
        
        ImageIcon blackChariot = resourceManager.getPieceImage(17);
        if (blackChariot != null) pieceImages.put("black_chariot", blackChariot.getImage());
        
        ImageIcon blackCannon = resourceManager.getPieceImage(26);
        if (blackCannon != null) pieceImages.put("black_cannon", blackCannon.getImage());
        
        ImageIcon blackSoldier = resourceManager.getPieceImage(28);
        if (blackSoldier != null) pieceImages.put("black_soldier", blackSoldier.getImage());
        
        // If any images failed to load, create placeholders
        if (pieceImages.isEmpty()) {
            createPlaceholderImages();
        }
    }
    
    /**
     * Create simple placeholder images if the actual images cannot be loaded.
     */
    private void createPlaceholderImages() {
        pieceImages = new HashMap<>();
        
        // Create simple colored rectangles as placeholders
        String[] colors = {"red", "black"};
        String[] types = {"general", "advisor", "elephant", "horse", "chariot", "cannon", "soldier"};
        
        for (String color : colors) {
            for (String type : types) {
                BufferedImage img = new BufferedImage(40, 40, BufferedImage.TYPE_INT_RGB);
                Graphics2D g2d = img.createGraphics();
                g2d.setColor(color.equals("red") ? Color.RED : Color.BLACK);
                g2d.fillOval(2, 2, 36, 36);
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Arial", Font.BOLD, 8));
                g2d.drawString(type.substring(0, 1).toUpperCase(), 15, 25);
                g2d.dispose();
                
                pieceImages.put(color + "_" + type, img);
            }
        }
    }
    
    /**
     * Setup mouse listeners for piece selection, dragging, and clicking.
     */
    private void setupMouseListeners() {
        MouseAdapter mouseHandler = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                handleMousePressed(e);
            }
            
            @Override
            public void mouseDragged(MouseEvent e) {
                handleMouseDragged(e);
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                handleMouseReleased(e);
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClicked(e);
            }
        };
        
        addMouseListener(mouseHandler);
        addMouseMotionListener(mouseHandler);
    }
    
    /**
     * Handle mouse press events for starting piece selection.
     * Drag functionality is disabled to avoid conflicts with click-to-move.
     */
    private void handleMousePressed(MouseEvent e) {
        // Dragging disabled - using click-to-move only
        // The piece selection is handled in handleMouseClicked
    }
    
    /**
     * Handle mouse drag events - disabled to avoid conflicts with click-to-move.
     */
    private void handleMouseDragged(MouseEvent e) {
        // Dragging disabled - using click-to-move only
    }
    
    /**
     * Handle mouse release events - disabled to avoid conflicts with click-to-move.
     */
    private void handleMouseReleased(MouseEvent e) {
        // Dragging disabled - using click-to-move only
    }
    
    /**
     * Handle mouse click events for click-to-move functionality.
     * First click selects a piece, second click moves it to the target position.
     */
    private void handleMouseClicked(MouseEvent e) {
        if (gameState == null) return;
        
        Position clickedPosition = getPositionFromPoint(e.getPoint());
        if (clickedPosition == null) return;
        
        if (selectedPosition == null) {
            // No piece selected, try to select one
            ChessPiece piece = gameState.getPiece(clickedPosition);
            if (piece != null && piece.getOwner().equals(gameState.getCurrentPlayer())) {
                selectPiece(clickedPosition);
            }
        } else {
            // Piece already selected, handle the second click
            ChessPiece clickedPiece = gameState.getPiece(clickedPosition);
            
            if (clickedPosition.equals(selectedPosition)) {
                // Clicked on same piece, deselect
                clearSelection();
            } else if (clickedPiece != null && clickedPiece.getOwner().equals(gameState.getCurrentPlayer())) {
                // Clicked on another piece of same player, switch selection
                selectPiece(clickedPosition);
            } else {
                // Try to move to clicked position (empty square or opponent piece)
                if (eventListener != null) {
                    eventListener.onMoveAttempted(selectedPosition, clickedPosition);
                }
                // Clear selection after attempting move
                clearSelection();
            }
        }
    }
    
    /**
     * Select a piece and highlight its valid moves.
     */
    private void selectPiece(Position position) {
        selectedPosition = position;
        
        // Play selection sound
        if (audioManager != null) {
            audioManager.playSelectSound();
        }
        
        // Get valid moves for the selected piece
        ChessPiece piece = gameState.getPiece(position);
        if (piece != null) {
            List<Move> moves = piece.getValidMoves(gameState);
            validMoves = new ArrayList<>();
            for (Move move : moves) {
                validMoves.add(move.getTo());
            }
        } else {
            validMoves = null;
        }
        
        if (eventListener != null) {
            eventListener.onPieceSelected(position);
        }
        
        repaint();
    }
    
    /**
     * Clear the current selection and valid move highlights.
     */
    public void clearSelection() {
        selectedPosition = null;
        validMoves = null;
        repaint();
    }
    
    /**
     * Convert screen coordinates to board position.
     * Takes into account board flipping for black player.
     */
    private Position getPositionFromPoint(Point point) {
        int col = point.x / CELL_WIDTH;
        int row = point.y / CELL_HEIGHT;
        
        // Flip coordinates if viewing as black player
        if (boardFlipped) {
            row = Position.BOARD_ROWS - 1 - row;
            col = Position.BOARD_COLS - 1 - col;
        }
        
        if (row >= 0 && row < Position.BOARD_ROWS && col >= 0 && col < Position.BOARD_COLS) {
            return new Position(row, col);
        }
        return null;
    }
    
    /**
     * Convert board position to screen coordinates (center of cell).
     * Takes into account board flipping for black player.
     */
    private Point getPointFromPosition(Position position) {
        int row = position.getRow();
        int col = position.getCol();
        
        // Flip coordinates if viewing as black player
        if (boardFlipped) {
            row = Position.BOARD_ROWS - 1 - row;
            col = Position.BOARD_COLS - 1 - col;
        }
        
        int x = col * CELL_WIDTH + CELL_WIDTH / 2;
        int y = row * CELL_HEIGHT + CELL_HEIGHT / 2;
        return new Point(x, y);
    }
    
    /**
     * Get the image key for a chess piece.
     */
    private String getPieceImageKey(ChessPiece piece) {
        String color = piece.isRed() ? "red" : "black";
        String type = piece.getType().name().toLowerCase();
        return color + "_" + type;
    }
    
    /**
     * Set the local player and determine if board should be flipped.
     */
    public void setLocalPlayer(Player player, GameState state) {
        this.localPlayer = player;
        // Flip board if local player is black (so they see their pieces at bottom)
        if (state != null) {
            this.boardFlipped = state.isBlackPlayer(player);
        }
    }
    
    /**
     * Update the game state and refresh the display.
     */
    public void updateGameState(GameState newState) {
        if (newState == null) {
            return;
        }
        
        this.gameState = newState;
        
        // Update board flip status if local player is set
        if (localPlayer != null) {
            this.boardFlipped = newState.isBlackPlayer(localPlayer);
        }
        
        // Clear selection when state changes
        clearSelection();
        
        // Force repaint to show updated board
        repaint();
        revalidate();
    }
    
    /**
     * Handle a successful move with appropriate sound effects.
     */
    public void onMoveExecuted(Move move) {
        if (audioManager != null) {
            if (move.getCapturedPiece() != null) {
                // Play capture sound if a piece was captured
                audioManager.playCaptureSound();
            } else {
                // Play regular move sound
                audioManager.playMoveSound();
            }
        }
    }
    
    /**
     * Handle check condition with sound effect.
     */
    public void onCheckDetected() {
        if (audioManager != null) {
            audioManager.playCheckSound();
        }
    }
    
    /**
     * Set the event listener for board interactions.
     */
    public void setBoardEventListener(BoardEventListener listener) {
        this.eventListener = listener;
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw board background (always upright)
        ImageIcon boardIcon = resourceManager.getBoardImage();
        if (boardIcon != null) {
            g2d.drawImage(boardIcon.getImage(), 0, 0, BOARD_WIDTH, BOARD_HEIGHT, this);
        }
        
        // Draw selection highlight
        if (selectedPosition != null) {
            ImageIcon selectIcon = resourceManager.getSelectImage();
            if (selectIcon != null) {
                Point pos = getPointFromPosition(selectedPosition);
                g2d.drawImage(selectIcon.getImage(), 
                    pos.x - CELL_WIDTH / 2, 
                    pos.y - CELL_HEIGHT / 2, 
                    CELL_WIDTH, CELL_HEIGHT, this);
            }
        }
        
        // Draw valid move highlights
        if (validMoves != null) {
            g2d.setColor(new Color(0, 255, 0, 100)); // Semi-transparent green
            for (Position move : validMoves) {
                Point pos = getPointFromPosition(move);
                g2d.fillOval(pos.x - 10, pos.y - 10, 20, 20);
            }
        }
        
        // Draw pieces
        if (gameState != null) {
            for (int row = 0; row < Position.BOARD_ROWS; row++) {
                for (int col = 0; col < Position.BOARD_COLS; col++) {
                    Position position = new Position(row, col);
                    ChessPiece piece = gameState.getPiece(position);
                    
                    if (piece != null && piece != draggedPiece) {
                        drawPiece(g2d, piece, position);
                    }
                }
            }
        }
        
        // Draw dragged piece at mouse position
        if (isDragging && draggedPiece != null) {
            Point mousePos = getMousePosition();
            if (mousePos != null && dragOffset != null) {
                Image pieceImage = pieceImages.get(getPieceImageKey(draggedPiece));
                if (pieceImage != null) {
                    g2d.drawImage(pieceImage, 
                        mousePos.x - dragOffset.x - CELL_WIDTH / 2,
                        mousePos.y - dragOffset.y - CELL_HEIGHT / 2,
                        CELL_WIDTH, CELL_HEIGHT, this);
                }
            }
        }
        
        g2d.dispose();
    }
    
    /**
     * Draw a chess piece at the specified position.
     */
    private void drawPiece(Graphics2D g2d, ChessPiece piece, Position position) {
        String imageKey = getPieceImageKey(piece);
        Image pieceImage = pieceImages.get(imageKey);
        
        if (pieceImage != null) {
            Point pos = getPointFromPosition(position);
            g2d.drawImage(pieceImage, 
                pos.x - CELL_WIDTH / 2, 
                pos.y - CELL_HEIGHT / 2,
                CELL_WIDTH, CELL_HEIGHT, this);
        } else {
            // Fallback: draw text representation
            Point pos = getPointFromPosition(position);
            g2d.setColor(piece.isRed() ? Color.RED : Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            String text = piece.getType().name().substring(0, 1);
            FontMetrics fm = g2d.getFontMetrics();
            int textWidth = fm.stringWidth(text);
            int textHeight = fm.getHeight();
            g2d.drawString(text, 
                pos.x - textWidth / 2, 
                pos.y + textHeight / 4);
        }
    }
}