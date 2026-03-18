
# Twinkle

Twinkle is a Java framework for creating text-based user interfaces (TUIs) in terminal emulators. It provides a layered architecture with components for text manipulation, screen rendering, shapes/borders, image encoding, and terminal abstraction.

## Architecture

The project follows a layered architecture:

- **Foundation**: Text utilities and ANSI escape code support
- **Rendering**: Screen buffers and double-buffering for flicker-free rendering
- **UI Components**: Drawing utilities for borders and shapes
- **Images**: [twinkle-image](/twinkle-image/) is a completely stand-alone library for rendering images in terminals
- **Terminal Access**: Abstraction layer with pluggable implementations

## Modules

### Core Modules

- **`twinkle-text`** - Core text handling and utilities for terminal applications
  - Position, Size, and dimension management
  - Unicode character utilities
  - Text sequence processing
  - ANSI escape codes (colors, styles, hyperlinks, mouse events)
  - Fluent API for building styled text with markup parsing

- **`twinkle-screen`** - Screen and buffer management for rendering content
  - `Buffer` - 2D renderable buffer with styled text and colors
  - `SwappableBuffer` - Double-buffering for flicker-free rendering
  - `BufferStack` - Stack-based buffer management for layering
  - Buffer I/O utilities

- **`twinkle-shapes`** - ASCII-based drawing utilities and UI components
  - Border drawing with various styles (ASCII, single-line, rounded)
  - Line and corner styles
  - Drawing utilities

- **`twinkle-image`** - Terminal image encoding framework (no dependencies)
  - **Sixel** - Legacy DEC format (xterm, mlterm)
  - **Kitty** - Modern format (Kitty, WezTerm)
  - **iTerm2** - Inline format (iTerm2, WezTerm)
  - **Block** - Unicode block-based fallback rendering
  - Pluggable encoder implementations

### Terminal Implementations

- **`twinkle-terminal`** - Terminal access and management abstraction API
  - `Terminal` interface for terminal operations
  - Service provider interface for different implementations
  - Support for terminal sizing and resize callbacks

- **`twinkle-terminal-aesh`** - Terminal implementation using the Aesh library
- **`twinkle-terminal-jline`** - Terminal implementation using the JLine 3 library

### Examples

- **`examples`** - Example programs demonstrating Twinkle capabilities
  - `BouncingTwinkleDemo` - Animated demo with bouncing text and ASCII borders
  - `ImageEncoderDemo` - Image rendering demonstration with automatic encoder detection

## Building

To build the project, run the following command:

```bash
./mvnw clean package
```

## Running Examples

After building, you can run the example programs to see Twinkle in action. This is most easily done using [JBang](jbang.dev):

```bash
# Bouncing animation demo
jbang bounce

# Image encoder demo
jbang image
```

Or if you want to use regular Java commands:

```bash
# Bouncing animation demo
java -jar examples/target/examples-1.0-SNAPSHOT.jar org.codejive.twinkle.examples.BouncingTwinkleDemo

# Image encoder demo
java -jar examples/target/examples-1.0-SNAPSHOT.jar org.codejive.twinkle.examples.ImageEncoderDemo
```

## Requirements

- Java 8 or higher (tests require Java 21)
- A terminal emulator with ANSI support
- For image rendering: Terminal with Sixel, Kitty, iTerm2, or Unicode block support
