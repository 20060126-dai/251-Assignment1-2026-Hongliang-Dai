package nz.ac.massey.cs251.editor;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

/**
 * Loads the editor.yaml configuration file at startup.
 * If the file is missing or something is wrong, default values are used
 * so the editor still opens normally.
 */
public class Settings {

    private String font = "Consolas";
    private int fontSize = 14;
    private Color fontColor = Color.BLACK;
    private Color backgroundColor = Color.WHITE;
    private boolean wrap = true;

    /**
     * Reads editor.yaml from the current working directory.
     * Values that are not present in the file keep their defaults.
     */
    @SuppressWarnings("unchecked")
    public static Settings load() {
        Settings s = new Settings();
        File file = new File("editor.yaml");
        if (!file.exists()) {
            return s;
        }
        try (InputStream in = new FileInputStream(file)) {
            Yaml yaml = new Yaml();
            Map<String, Object> root = yaml.load(in);
            if (root == null) {
                return s;
            }
            Object editor = root.get("editor");
            if (editor instanceof Map) {
                Map<String, Object> map = (Map<String, Object>) editor;
                if (map.get("font") != null) {
                    s.font = String.valueOf(map.get("font"));
                }
                if (map.get("font_size") != null) {
                    s.fontSize = Integer.parseInt(String.valueOf(map.get("font_size")));
                }
                if (map.get("font_color") != null) {
                    s.fontColor = parseColor(String.valueOf(map.get("font_color")));
                }
                if (map.get("background_color") != null) {
                    s.backgroundColor = parseColor(String.valueOf(map.get("background_color")));
                }
                if (map.get("wrap") != null) {
                    s.wrap = Boolean.parseBoolean(String.valueOf(map.get("wrap")));
                }
            }
        } catch (IOException | RuntimeException e) {
            // keep defaults, the editor should still start
            System.err.println("Could not read editor.yaml, using defaults: " + e.getMessage());
        }
        return s;
    }

    /**
     * Converts a hex colour string like "#112233" into a Color.
     */
    private static Color parseColor(String value) {
        try {
            String hex = value.startsWith("#") ? value.substring(1) : value;
            return new Color(Integer.parseInt(hex, 16));
        } catch (NumberFormatException e) {
            return Color.BLACK;
        }
    }

    public String getFont() {
        return font;
    }

    public int getFontSize() {
        return fontSize;
    }

    public Color getFontColor() {
        return fontColor;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public boolean isWrap() {
        return wrap;
    }
}
