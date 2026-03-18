# Twinkle Image Encoding Framework

A library for rendering images in terminal emulators using various image encoding formats.

**This library has _no_ dependencies!** It can therefore be easily used outside of the Twinkle framework.

## Supported Encoders

This module provides implementations for four major terminal image encoding formats:

### 1. **Sixel**

- Legacy encoding format developed by Digital Equipment Corporation (DEC)
- Widely supported by various terminal emulators (xterm, mlterm, etc.)
- Encodes images as six-pixel-high strips
- Good compatibility but less efficient than modern formats

### 2. **Kitty**

- Modern encoding format developed for the Kitty terminal emulator
- Supports direct PNG transmission via base64 encoding
- Highly efficient with support for advanced features
- Supported by: Kitty, WezTerm, and others

### 3. **iTerm2**

- Inline image encoding format for iTerm2 terminal
- Uses OSC escape sequences with base64-encoded images
- Supports various image formats
- Supported by: iTerm2, WezTerm, and others

### 4. **Block** (Unicode-based)

- Uses Unicode block drawing characters for image rendering
- **Most compatible** - works in any terminal with Unicode support
- Five rendering modes with different resolution/compatibility tradeoffs:
  - **Full-block** (1x1 sub-pixels): Solid blocks, lowest resolution
  - **Half-block** (1x2 sub-pixels): Maximum compatibility, basic resolution
  - **Quadrant** (2x2 sub-pixels): Good balance of resolution and compatibility
  - **Sextant** (2x3 sub-pixels): Higher resolution, requires Unicode 13.0 support
  - **Octant** (2x4 sub-pixels): Highest resolution, experimental
- Uses 2-color clustering per cell (foreground + background) for sub-pixel modes
- No special terminal capabilities required beyond ANSI RGB colors

## Usage

### Basic Usage

```java
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;
import org.codejive.twinkle.image.ImageEncoder;
import org.codejive.twinkle.image.ImageEncoders;

// Load an image
BufferedImage image = ImageIO.read(new File("image.png"));

// Define target size in terminal columns and rows
int targetWidth = 40;   // 40 columns
int targetHeight = 20;  // 20 rows
boolean fitImage = false;  // Preserve aspect ratio

// Auto-detect the best encoder for the current terminal and create it
ImageEncoder encoder = ImageEncoders.detectAndCreate(image, targetWidth, targetHeight, fitImage);

// Render the image
encoder.render(System.out);
```

### Using a Specific Encoder

```java
// Use an encoder explicitly, in this case Kitty
ImageEncoder kitty = ImageEncoders.kitty(image, targetWidth, targetHeight, fitImage);
kitty.render(output);
```

## API Reference

### ImageEncoder Interface

The core interface for all image encoder implementations:

```java
public interface ImageEncoder {
    String name();
    int targetWidth();
    int targetHeight();
    ImageEncoder targetSize(int targetWidth, int targetHeight);
    boolean fitImage();
    ImageEncoder fitImage(boolean fitImage);
    void render(Appendable output) throws IOException;
}
```

**Methods:**

- `name()`: Returns the encoder name (e.g., "sixel", "kitty", "iterm", "block-quadrant")
- `targetWidth()` / `targetHeight()`: Get the current target size in terminal columns/rows
- `targetSize(width, height)`: Set the target size in terminal columns and rows (returns this for chaining)
- `fitImage()`: Get whether the image should be fitted exactly or preserve aspect ratio
- `fitImage(boolean)`: Set fit mode (returns this for chaining)
- `render(output)`: Render the image to the given Appendable using the encoder's format

**Note:** Encoders are stateful objects that encapsulate an image and rendering parameters. The target size and fit mode can be changed via setters. Expensive transformations like image scaling are performed lazily on the first call to `render()` and cached for subsequent calls.

### ImageEncoders Factory

Factory methods for creating encoder instances:

- `ImageEncoders.detectEncoderType()` - Detect the best encoder type for the current terminal (returns `EncoderType` enum)
- `ImageEncoders.detectAndCreate(image, width, height, fit)` - Auto-detect and create the best encoder for the current terminal
- `ImageEncoders.sixel(image, width, height, fit)` - Create a Sixel encoder instance
- `ImageEncoders.kitty(image, width, height, fit)` - Create a Kitty encoder instance
- `ImageEncoders.iterm(image, width, height, fit)` - Create an iTerm2 encoder instance
- `ImageEncoders.block(mode, image, width, height, fit)` - Create a block encoder with specific mode
- `ImageEncoders.blockFull(image, width, height, fit)` - Create a full-block encoder (1x1, lowest resolution)
- `ImageEncoders.blockHalf(image, width, height, fit)` - Create a half-block encoder (1x2, maximum compatibility)
- `ImageEncoders.blockQuadrant(image, width, height, fit)` - Create a quadrant-block encoder (2x2, recommended fallback)
- `ImageEncoders.blockSextant(image, width, height, fit)` - Create a sextant-block encoder (2x3, higher resolution)
- `ImageEncoders.blockOctant(image, width, height, fit)` - Create an octant-block encoder (2x4, highest resolution)

## Encoder Detection

The `ImageEncoders.detectEncoderType()` method checks environment variables to determine the best encoder:

- **iTerm2**: Detected via `TERM_PROGRAM=iTerm.app` environment variable
- **Kitty**: Detected via `KITTY_WINDOW_ID` environment variable
- **WezTerm**: Detected via `TERM_PROGRAM=WezTerm` (uses Kitty encoding format)
- **Sixel**: Detected for terminals with xterm, mlterm, or vt340 in the `TERM` variable
- **Fallback**: Block encoder with quadrant mode (works in virtually all terminals)

Use `ImageEncoders.detectAndCreate()` to automatically detect and create the appropriate encoder instance, or use `detectEncoderType()` to get the detected type without creating an encoder.

**EncoderType Enum:**
```java
public enum EncoderType {
    SIXEL,
    KITTY,
    ITERM,
    BLOCK_QUADRANT
}
```

## Block Encoder Details

The block encoder uses Unicode block drawing characters to achieve sub-pixel resolution within each terminal cell. Since each cell can only have one foreground and one background color, the encoder:

1. **Samples pixels**: Collects all pixel colors for each terminal cell
2. **Clusters colors**: Uses k-means clustering to find the 2 best representative colors
3. **Assigns pixels**: Maps each pixel to either foreground or background based on proximity
4. **Selects character**: Chooses the appropriate Unicode character matching the pixel pattern
5. **Outputs with colors**: Writes the character with ANSI RGB color codes

### Block Modes

| Mode     | Grid | Total Pixels | Compatibility | Use Case                         |
|----------|------|--------------|---------------|----------------------------------|
| Full     | 1×1  | 1            | ★★★★★         | Solid blocks (lowest resolution) |
| Half     | 1×2  | 2            | ★★★★★         | Maximum compatibility            |
| Quadrant | 2×2  | 4            | ★★★★☆         | Recommended default              |
| Sextant  | 2×3  | 6            | ★★★☆☆         | Modern terminals (Unicode 13.0+) |
| Octant   | 2×4  | 8            | ★★☆☆☆         | Experimental, highest resolution |

## Image Scaling

All encoders automatically scale images to fit within the specified target size. The scaling behavior depends on the `fitImage` parameter:

- **`fitImage = false`** (default): Preserves aspect ratio, scaling the image to fit within the target dimensions without distortion
- **`fitImage = true`**: Scales to exact target dimensions, potentially stretching or squashing the image

The encoder handles all scaling internally, using the font size information to calculate the appropriate pixel dimensions.

## Notes

### Performance Considerations

- **Kitty**: Most efficient for large images (direct PNG transmission)
- **iTerm2**: Efficient for moderate-sized images
- **Block**: Works everywhere, moderate quality, best for fallback
- **Sixel**: Good compatibility but less efficient for complex images

## Command-Line Demo

Run the demo with different options:

```bash
# Auto-detect encoder and render
java -jar examples/target/examples-1.0-SNAPSHOT.jar

# Test all encoders
java -jar examples/target/examples-1.0-SNAPSHOT.jar --all
```

## Examples

See [examples/src/main/java/org/codejive/twinkle/demos/ImageEncoderDemo.java](../examples/src/main/java/org/codejive/twinkle/demos/ImageEncoderDemo.java) for a complete working example.

Build and run the demo:

```bash
mvn clean install
java -jar examples/target/examples-1.0-SNAPSHOT.jar
```

## Future Enhancements

Potential areas for improvement:

- Terminal capability detection (query terminal for supported features)
- Automatic font size detection via terminal queries
- Support for animation (animated GIFs)
- Improved caching and performance optimization
- Additional encoding formats (e.g., Jexer bitmap format)
- Dithering options for better quality on limited color terminals
- Color quantization improvements for Sixel encoding
