import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Scaffolds a fresh practice attempt. Run from the repo root:
 *   java scripts/NewAttempt.java                 (pick interactively)
 *   java scripts/NewAttempt.java parking-garage  (pick by id)
 *   java scripts/NewAttempt.java --list
 */
public class NewAttempt {

    private static final Path ROOT = Path.of("").toAbsolutePath();
    private static final Path THEMES_DIR = ROOT.resolve("themes");
    private static final Path ATTEMPTS_DIR = ROOT.resolve("attempts");

    private record Theme(Path dir, String id, String name, String description, String estimatedMinutes,
                         List<String> sections) {
    }

    public static void main(String[] args) throws IOException {
        if (!Files.isDirectory(THEMES_DIR)) {
            System.err.println("Run this from the repo root (couldn't find a themes/ directory in "
                    + ROOT + ").");
            System.exit(1);
        }

        List<Theme> themes = loadThemes();

        if (args.length > 0 && args[0].equals("--list")) {
            printList(themes);
            return;
        }

        if (args.length > 0) {
            Theme theme = themes.stream().filter(t -> t.id().equals(args[0])).findFirst().orElse(null);
            if (theme == null) {
                System.err.println("Unknown theme \"" + args[0] + "\". Run with --list to see options.");
                System.exit(1);
            }
            scaffold(theme);
            return;
        }

        printList(themes);
        System.out.print("Pick a theme number (or Ctrl+C to cancel): ");
        Scanner in = new Scanner(System.in);
        int index;
        try {
            index = Integer.parseInt(in.nextLine().trim()) - 1;
        } catch (RuntimeException e) {
            index = -1;
        }
        if (index < 0 || index >= themes.size()) {
            System.err.println("Invalid selection.");
            System.exit(1);
        }
        scaffold(themes.get(index));
    }

    private static List<Theme> loadThemes() throws IOException {
        List<Theme> themes = new ArrayList<>();
        try (Stream<Path> dirs = Files.list(THEMES_DIR)) {
            for (Path dir : dirs.filter(Files::isDirectory).sorted(Comparator.comparing(Path::getFileName)).toList()) {
                String json = Files.readString(dir.resolve("META.json"));
                themes.add(new Theme(
                        dir,
                        stringField(json, "id"),
                        stringField(json, "name"),
                        stringField(json, "description"),
                        numberField(json, "estimatedMinutes"),
                        stringArrayField(json, "sections")));
            }
        }
        return themes;
    }

    // META.json is flat and ours, so a few regexes beat pulling in a JSON library.
    private static String stringField(String json, String key) {
        Matcher m = Pattern.compile("\"" + key + "\"\\s*:\\s*\"((?:[^\"\\\\]|\\\\.)*)\"").matcher(json);
        if (!m.find()) throw new IllegalStateException("META.json is missing \"" + key + "\"");
        return m.group(1);
    }

    private static String numberField(String json, String key) {
        Matcher m = Pattern.compile("\"" + key + "\"\\s*:\\s*(\\d+)").matcher(json);
        if (!m.find()) throw new IllegalStateException("META.json is missing \"" + key + "\"");
        return m.group(1);
    }

    private static List<String> stringArrayField(String json, String key) {
        Matcher array = Pattern.compile("\"" + key + "\"\\s*:\\s*\\[(.*?)]", Pattern.DOTALL).matcher(json);
        if (!array.find()) throw new IllegalStateException("META.json is missing \"" + key + "\"");
        List<String> values = new ArrayList<>();
        Matcher item = Pattern.compile("\"((?:[^\"\\\\]|\\\\.)*)\"").matcher(array.group(1));
        while (item.find()) values.add(item.group(1));
        return values;
    }

    private static void printList(List<Theme> themes) {
        System.out.println("\nAvailable assessment themes:\n");
        for (int i = 0; i < themes.size(); i++) {
            Theme t = themes.get(i);
            System.out.println("  " + (i + 1) + ". " + t.name() + "  (" + t.id() + ")");
            System.out.println("     " + t.description());
            System.out.println("     ~" + t.estimatedMinutes() + " min | sections: "
                    + String.join(" -> ", t.sections()) + "\n");
        }
    }

    private static void scaffold(Theme theme) throws IOException {
        String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"));
        String dirName = theme.id() + "-" + stamp;
        Path dest = ATTEMPTS_DIR.resolve(dirName);
        if (Files.exists(dest)) {
            System.err.println("Attempt directory already exists: " + dest);
            System.exit(1);
        }

        copyRecursive(theme.dir().resolve("starter"), dest);

        String startedAt = Instant.now().toString();
        Files.writeString(dest.resolve(".attempt-meta.json"),
                "{\n  \"theme\": \"" + theme.id() + "\",\n  \"startedAt\": \"" + startedAt + "\"\n}\n");

        System.out.println("\nScaffolded \"" + theme.name() + "\" into attempts/" + dirName + "\n");
        System.out.println("Read the spec:   themes/" + theme.id() + "/README.md");
        System.out.println("Implement in:    attempts/" + dirName + "/src/main/java/");
        System.out.println("Run section 1:   cd attempts/" + dirName + " && mvn test -Dtest=Section1Test");
        System.out.println("Run everything:  cd attempts/" + dirName + " && mvn test");
        System.out.println("Started at:      " + startedAt);
        System.out.println("\nDon't open themes/" + theme.id()
                + "/reference-solution/ until you're done or truly stuck.\n");
    }

    private static void copyRecursive(Path src, Path dest) throws IOException {
        try (Stream<Path> paths = Files.walk(src)) {
            for (Path p : paths.toList()) {
                Path target = dest.resolve(src.relativize(p).toString());
                if (Files.isDirectory(p)) {
                    Files.createDirectories(target);
                } else {
                    Files.copy(p, target);
                }
            }
        }
    }
}
