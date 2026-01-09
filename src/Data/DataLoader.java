package Data;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/** Simple data loader that reads the JSON file as a string. */
public class DataLoader {
    private final Path dbPath;

    public DataLoader() {
        // database.json lives in src/ (project root: csse374-hw2/src/database.json)
        this.dbPath = Paths.get("src", "database.json");
    }

    public String loadprevGame() {
        try {
            if (!Files.exists(dbPath)) {
                // fallback to project-root path
                return "";
            }
            return new String(Files.readAllBytes(dbPath));
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public void saveGame(String json) {
        try {
            Files.write(dbPath, json.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
