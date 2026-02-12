import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.stream.*;

/**
 * Validador de chaves de internacionalização (i18n).
 *
 * Compara todos os arquivos de idioma em resource/lang/ com o idioma base (en_US)
 * e aponta chaves faltantes ou sobrando em cada idioma.
 *
 * Uso:
 *   cd tools
 *   javac I18nValidator.java
 *   java I18nValidator
 *
 * Ou a partir da raiz do projeto:
 *   javac tools/I18nValidator.java -d tools
 *   java -cp tools I18nValidator
 */
public class I18nValidator {

    private static final String LANG_DIR = "resource/lang";
    private static final String BASE_LOCALE = "messages_en_US.properties";

    public static void main(String[] args) throws Exception {
        // Resolve o diretório base do projeto (pai de tools/)
        Path projectRoot = resolveProjectRoot();
        Path langDir = projectRoot.resolve(LANG_DIR);

        if (!Files.isDirectory(langDir)) {
            System.err.println("ERRO: Diretório de idiomas não encontrado: " + langDir.toAbsolutePath());
            System.exit(1);
        }

        // Carrega chaves do idioma base
        Path basePath = langDir.resolve(BASE_LOCALE);
        if (!Files.exists(basePath)) {
            System.err.println("ERRO: Arquivo base não encontrado: " + basePath.toAbsolutePath());
            System.exit(1);
        }

        Set<String> baseKeys = loadKeys(basePath);
        System.out.println("=============================================================");
        System.out.println(" i18n Key Validator");
        System.out.println("=============================================================");
        System.out.println("Idioma base: " + BASE_LOCALE + " (" + baseKeys.size() + " chaves)");
        System.out.println("-------------------------------------------------------------");

        // Descobre todos os arquivos de idioma
        List<Path> localeFiles;
        try (Stream<Path> stream = Files.list(langDir)) {
            localeFiles = stream
                    .filter(p -> p.getFileName().toString().endsWith(".properties"))
                    .filter(p -> !p.getFileName().toString().equals(BASE_LOCALE))
                    .sorted()
                    .collect(Collectors.toList());
        }

        int totalMissing = 0;
        int totalExtra = 0;
        int filesWithIssues = 0;
        List<String> summaryLines = new ArrayList<>();

        for (Path localeFile : localeFiles) {
            String fileName = localeFile.getFileName().toString();
            Set<String> localeKeys = loadKeys(localeFile);

            // Chaves faltantes (estão no base mas não no locale)
            Set<String> missing = new TreeSet<>(baseKeys);
            missing.removeAll(localeKeys);

            // Chaves sobrando (estão no locale mas não no base)
            Set<String> extra = new TreeSet<>(localeKeys);
            extra.removeAll(baseKeys);

            boolean hasIssues = !missing.isEmpty() || !extra.isEmpty();
            if (hasIssues) filesWithIssues++;

            totalMissing += missing.size();
            totalExtra += extra.size();

            // Status
            String status = hasIssues ? "FALHA" : "OK";
            String line = String.format("%-35s %3d chaves | %s", fileName, localeKeys.size(), status);
            summaryLines.add(line);

            System.out.println();
            System.out.println("[" + status + "] " + fileName + " (" + localeKeys.size() + " chaves)");

            if (!missing.isEmpty()) {
                System.out.println("  Chaves FALTANDO (" + missing.size() + "):");
                for (String key : missing) {
                    System.out.println("    - " + key);
                }
            }

            if (!extra.isEmpty()) {
                System.out.println("  Chaves SOBRANDO (" + extra.size() + "):");
                for (String key : extra) {
                    System.out.println("    + " + key);
                }
            }

            if (!hasIssues) {
                System.out.println("  Todas as chaves estão presentes.");
            }
        }

        // Resumo final
        System.out.println();
        System.out.println("=============================================================");
        System.out.println(" RESUMO");
        System.out.println("=============================================================");
        for (String s : summaryLines) {
            System.out.println("  " + s);
        }
        System.out.println("-------------------------------------------------------------");
        System.out.println("Total de idiomas verificados: " + localeFiles.size());
        System.out.println("Idiomas com problemas:        " + filesWithIssues);
        System.out.println("Chaves faltantes (total):     " + totalMissing);
        System.out.println("Chaves sobrando  (total):     " + totalExtra);
        System.out.println("=============================================================");

        if (totalMissing == 0 && totalExtra == 0) {
            System.out.println("RESULTADO: TODOS OS IDIOMAS ESTÃO COMPLETOS!");
        } else {
            System.out.println("RESULTADO: HÁ INCONSISTÊNCIAS - verifique acima.");
            System.exit(2);
        }
    }

    /**
     * Carrega todas as chaves (lado esquerdo do '=') de um arquivo .properties,
     * ignorando linhas em branco e comentários.
     */
    private static Set<String> loadKeys(Path path) throws IOException {
        Set<String> keys = new LinkedHashSet<>();
        try (BufferedReader reader = Files.newBufferedReader(path)) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // Ignora linhas vazias e comentários
                if (line.isEmpty() || line.startsWith("#") || line.startsWith("!")) {
                    continue;
                }
                // Extrai a chave (antes do primeiro '=' ou ':')
                int eqIdx = line.indexOf('=');
                int colonIdx = line.indexOf(':');
                int sepIdx;
                if (eqIdx < 0 && colonIdx < 0) continue;
                else if (eqIdx < 0) sepIdx = colonIdx;
                else if (colonIdx < 0) sepIdx = eqIdx;
                else sepIdx = Math.min(eqIdx, colonIdx);

                String key = line.substring(0, sepIdx).trim();
                if (!key.isEmpty()) {
                    keys.add(key);
                }
            }
        }
        return keys;
    }

    /**
     * Resolve o diretório raiz do projeto. Tenta encontrar o diretório resource/lang
     * a partir do diretório atual, ou navega para o pai (caso esteja em tools/).
     */
    private static Path resolveProjectRoot() {
        Path cwd = Paths.get("").toAbsolutePath();

        // Se executado de dentro de tools/, volte um nível
        if (cwd.getFileName().toString().equals("tools")) {
            cwd = cwd.getParent();
        }

        // Se resource/lang não existe no cwd, tente o pai
        if (!Files.isDirectory(cwd.resolve(LANG_DIR))) {
            Path parent = cwd.getParent();
            if (parent != null && Files.isDirectory(parent.resolve(LANG_DIR))) {
                return parent;
            }
        }

        return cwd;
    }
}
