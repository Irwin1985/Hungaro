import java.awt.Color;

public final class GUI {
    // ----------------------------------------------------------------------------- //
    // colorString is a string with three posible formats:
    // - "r,g,b" where r, g and b are integers between 0 and 255
    // - "#rrggbb" where rr, gg and bb are hexadecimal numbers between 00 and ff
    // - "name" where name is a color name (e.g. "red", "green", "blue", etc.)
    // ----------------------------------------------------------------------------- //
    public static Color decodeColor(String colorString) {
        // we use the default color
        Color color = Color.WHITE;        
        // check if the string is in the first format
        if (colorString.contains(",")) {
            String[] rgb = colorString.split("\\s*,\\s*");                            
            color = new Color(Integer.parseInt(rgb[0]), Integer.parseInt(rgb[1]), Integer.parseInt(rgb[2]));
        } else if (colorString.startsWith("#")) {
            color = Color.decode(colorString);                            
        } else {                            
            if (!BuiltinsForWindow.colors.containsKey(colorString)) {
                throw new RuntimeException("Unknown color: `" + colorString + "`");
            }
            color = BuiltinsForWindow.colors.get(colorString);
        }
        return color;
    }
}
