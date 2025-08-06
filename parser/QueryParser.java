package parser;

public class QueryParser {

    public static String[] parse(String query) {
        // Add spaces around '=' to ensure proper tokenizing
        String spacedQuery = query.replaceAll("=", " = ");
        return spacedQuery.trim().split("\\s+");
    }
}
