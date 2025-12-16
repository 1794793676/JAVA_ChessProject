# Networked Xiangqi Game

A networked Chinese Chess (Xiangqi) game application built with Java Swing, featuring client-server architecture, real-time gameplay, and comprehensive chess logic.

## Project Structure

This is a multi-module Maven project with the following structure:

```
networked-xiangqi-game/
├── pom.xml                     # Root Maven configuration
├── xiangqi-shared/             # Shared components (models, interfaces, network messages)
│   ├── pom.xml
│   └── src/
│       ├── main/java/com/xiangqi/shared/
│       │   ├── model/          # Core data models (Player, GameState, ChessPiece, etc.)
│       │   ├── network/        # Network message classes and interfaces
│       │   └── engine/         # Game engine interfaces
│       └── test/java/          # Unit tests
├── xiangqi-client/             # Client application (Swing UI, client networking)
│   ├── pom.xml
│   └── src/
│       ├── main/java/
│       └── test/java/
├── xiangqi-server/             # Server application (game server, connection management)
│   ├── pom.xml
│   └── src/
│       ├── main/java/
│       └── test/java/
└── source/                     # Game assets (audio, graphics)
    ├── audio/                  # Sound effects (.wav files)
    ├── face/                   # Player avatars (.gif files)
    ├── img/                    # UI images (.gif files)
    └── qizi/                   # Chess piece graphics (.gif files)
```

## Core Components Created

### Shared Module (`xiangqi-shared`)

#### Data Models
- **Player**: Represents a game player with ID, username, status, rating, and statistics
- **Position**: Represents board coordinates with validation (10x9 Xiangqi board)
- **ChessPiece**: Abstract base class for all chess pieces
- **Move**: Represents a chess move with source, target, piece, and capture information
- **GameState**: Complete game state including board, current player, status, and move history
- **GameResult**: Represents the outcome of a completed game

#### Enumerations
- **PlayerStatus**: Player states (OFFLINE, ONLINE, IN_LOBBY, IN_GAME, AWAY)
- **PieceType**: Chess piece types (GENERAL, ADVISOR, ELEPHANT, HORSE, CHARIOT, CANNON, SOLDIER)
- **GameStatus**: Game states (WAITING_FOR_PLAYERS, IN_PROGRESS, CHECK, CHECKMATE, etc.)
- **MessageType**: Network message types for client-server communication

#### Network Components
- **NetworkMessage**: Abstract base class for all network messages
- **NetworkMessageHandler**: Interface for handling different message types
- **Message Classes**: LoginMessage, MoveMessage, ChatMessage, GameInvitationMessage, etc.

#### Engine Interfaces
- **GameEventListener**: Interface for listening to game events (moves, state changes, etc.)

### Testing Framework
- **JUnit 5**: For unit testing
- **QuickCheck for Java**: For property-based testing (as specified in design document)
- **Basic Tests**: Created initial tests for Player and Position classes

## Requirements Addressed

This project structure addresses the following requirements from the specification:

- **Requirement 1.1, 2.1, 3.1**: Core interfaces and data models for authentication, lobby, and gameplay
- **Multi-module Architecture**: Separation of client, server, and shared components
- **Network Communication**: Message-based protocol with serializable classes
- **Game Logic Foundation**: Abstract chess piece class and game state management
- **Testing Infrastructure**: JUnit and property-based testing setup

## Build Configuration

- **Java Version**: Java 8 (compatible with available runtime)
- **Build Tool**: Maven 3.x
- **Dependencies**: JUnit 5, QuickCheck for Java
- **Modules**: Multi-module project with proper dependency management

## Next Steps

The project structure and core interfaces are now established. The next tasks will involve:

1. Implementing specific chess piece classes with movement rules
2. Creating the chess engine with rule validation
3. Building the network communication layer
4. Developing the Swing-based user interfaces
5. Implementing the game server with multi-threading support

## Development Notes

- All classes implement Serializable for network transmission
- Null safety is enforced throughout with proper validation
- Position class validates Xiangqi board dimensions (10 rows × 9 columns)
- Game state supports deep copying for move validation
- Message system is extensible for future message types