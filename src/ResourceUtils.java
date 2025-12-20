import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.net.URL;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class ResourceUtils {

    private ResourceUtils() {}

    public static BufferedImage loadImage(String path) throws Exception {
        InputStream stream = openStream(path);
        if (stream == null) return null;
        try (InputStream in = stream) {
            return ImageIO.read(in);
        }
    }

    public static URL getResourceUrl(String path) {
        ClassLoader cl = ResourceUtils.class.getClassLoader();
        String normalized = normalizePath(path);
        URL url = cl.getResource(normalized);
        if (url != null) return url;
        if (normalized.startsWith("res/")) {
            return cl.getResource(normalized.substring("res/".length()));
        }
        return null;
    }

    public static InputStream openStream(String path) throws FileNotFoundException {
        ClassLoader cl = ResourceUtils.class.getClassLoader();
        String normalized = normalizePath(path);
        InputStream stream = cl.getResourceAsStream(normalized);
        if (stream != null) return stream;
        if (normalized.startsWith("res/")) {
            stream = cl.getResourceAsStream(normalized.substring("res/".length()));
            if (stream != null) return stream;
        }
        File file = new File(path);
        if (file.exists()) {
            return new FileInputStream(file);
        }
        return null;
    }

    private static String normalizePath(String path) {
        if (path == null) return "";
        if (path.startsWith("/")) return path.substring(1);
        return path;
    }
}
