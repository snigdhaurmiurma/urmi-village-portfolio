import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Zero-dependency Java 17/21 HTTP + REST server for Urmi's Dual-Mode Portfolio.
 *
 * Build & run from the repository root:
 *   javac backend/java/PortfolioServer.java && java -cp backend/java PortfolioServer
 *
 * Endpoints:
 *   GET  /              - Serves index.html
 *   GET  /api/health    - Health check JSON
 *   GET  /api/portfolio - Profile, Light/Dark themes, and project list JSON
 *   POST /api/contact   - Validates JSON inquiry and appends to messages.jsonl
 */
public class PortfolioServer {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Object LOG_LOCK = new Object();

    public static void main(String[] args) throws IOException {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "8080"));
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.setExecutor(Executors.newFixedThreadPool(8));

        server.createContext("/", PortfolioServer::handleRoot);
        server.createContext("/api/health", PortfolioServer::handleHealth);
        server.createContext("/api/portfolio", PortfolioServer::handlePortfolio);
        server.createContext("/api/contact", PortfolioServer::handleContact);

        server.start();
        System.out.println("Urmi Portfolio Java Server running on http://localhost:" + port);
    }

    private static void handleRoot(HttpExchange exchange) throws IOException {
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, "{\"ok\":false,\"error\":\"Method not allowed\"}");
            return;
        }
        String path = exchange.getRequestURI().getPath();
        if (!"/".equals(path) && !"/index.html".equals(path)) {
            sendJson(exchange, 404, "{\"ok\":false,\"error\":\"Not found\"}");
            return;
        }

        Path indexPath = resolveIndexHtml();
        if (indexPath != null && Files.exists(indexPath)) {
            byte[] html = Files.readAllBytes(indexPath);
            exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
            exchange.sendResponseHeaders(200, html.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(html);
            }
            return;
        }

        String fallback = "<!doctype html><html><body><h1>Urmi Portfolio API</h1>"
                + "<p>Endpoints: /api/health, /api/portfolio, POST /api/contact</p></body></html>";
        byte[] bytes = fallback.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8");
        exchange.sendResponseHeaders(200, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void handleHealth(HttpExchange exchange) throws IOException {
        if (handleCorsPreflight(exchange)) {
            return;
        }
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, "{\"ok\":false,\"error\":\"Method not allowed\"}");
            return;
        }
        String json = "{"
                + "\"status\":\"ok\","
                + "\"service\":\"urmi-village-portfolio\","
                + "\"runtime\":\"Java " + escapeJson(System.getProperty("java.version")) + "\","
                + "\"timestamp\":\"" + Instant.now() + "\""
                + "}";
        sendJson(exchange, 200, json);
    }

    private static void handlePortfolio(HttpExchange exchange) throws IOException {
        if (handleCorsPreflight(exchange)) {
            return;
        }
        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, "{\"ok\":false,\"error\":\"Method not allowed\"}");
            return;
        }
        String json = """
                {
                  "ok": true,
                  "profile": {
                    "name": "Urmi Urma Snigdha",
                    "brand": "urmi.design",
                    "headline": "CSE Graduate · UI/UX Designer & Software Engineer · Dhaka",
                    "university": "United International University (UIU)",
                    "email": "urmiurmasnigdha@gmail.com",
                    "linkedin": "https://www.linkedin.com/in/urmi-urma-snigdha-4836362a1/",
                    "github": "https://github.com/snigdhaurmiurma"
                  },
                  "themes": {
                    "light": {
                      "name": "Warm Paper & Daylight Village",
                      "paper": "#FAF6EE",
                      "paperDeep": "#F3ECE0",
                      "ink": "#241F1B",
                      "accent": "#C2654A"
                    },
                    "dark": {
                      "name": "Candlelit Espresso & Starlit Night Village",
                      "paper": "#161311",
                      "paperDeep": "#1F1A17",
                      "ink": "#F4EDE2",
                      "accent": "#E08A5E",
                      "starsCount": 42
                    }
                  },
                  "projects": [
                    {
                      "id": "fundbridge",
                      "shopName": "FundBridge Vault",
                      "category": "FinTech & Software",
                      "year": "2026",
                      "title": "FundBridge — Distributed Micro-Finance & Anti-Predatory Lending Engine"
                    },
                    {
                      "id": "medvision-ai",
                      "shopName": "MedVision Micro-Lab",
                      "category": "Computer Vision",
                      "year": "2026",
                      "title": "MedVision-AI — Cellular & Microscopic Image Analysis Pipeline"
                    },
                    {
                      "id": "marketpulse-mining",
                      "shopName": "MarketPulse Exchange",
                      "category": "Data Mining",
                      "year": "2026",
                      "title": "MarketPulse — Customer Behavior Mining & Churn Intelligence"
                    },
                    {
                      "id": "docuquery-rag",
                      "shopName": "DocuQuery Archive",
                      "category": "AI & NLP",
                      "year": "2026",
                      "title": "DocuQuery-AI — Enterprise Document Intelligence & Semantic Search (RAG)"
                    },
                    {
                      "id": "green-computing",
                      "shopName": "The Green Observatory",
                      "category": "AI & NLP",
                      "year": "2026",
                      "title": "Green Computing & AI Safety Guardrail Benchmarking"
                    },
                    {
                      "id": "cross-lingual-nli",
                      "shopName": "The Babel Library",
                      "category": "AI & NLP",
                      "year": "2026",
                      "title": "Cross-Lingual NLI & Multilingual LLM Benchmarking (CLSC)"
                    },
                    {
                      "id": "moving-canvas",
                      "shopName": "UIU Moving Canvas Atelier",
                      "category": "Branding & Livery",
                      "year": "2026",
                      "title": "UIU Moving Canvas — Official Student Transit Bus Fleet Livery"
                    },
                    {
                      "id": "edusmart",
                      "shopName": "EduSmart Study Café",
                      "category": "UI/UX & Product",
                      "year": "2025",
                      "title": "EduSmart — Academic Companion & Interactive Study Ecosystem"
                    },
                    {
                      "id": "bangla-news-mismatch",
                      "shopName": "The Dhaka Press & Lens",
                      "category": "Computer Vision",
                      "year": "2025",
                      "title": "Bangla News Image-Text Mismatch Detection"
                    },
                    {
                      "id": "synapse-iot",
                      "shopName": "Synapse Tinkerer's Workshop",
                      "category": "Embedded & IoT",
                      "year": "2024",
                      "title": "Synapse — Sensor Telemetry & Microcontroller Hub"
                    }
                  ]
                }
                """;
        sendJson(exchange, 200, json);
    }

    private static void handleContact(HttpExchange exchange) throws IOException {
        if (handleCorsPreflight(exchange)) {
            return;
        }
        if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
            sendJson(exchange, 405, "{\"ok\":false,\"error\":\"Method not allowed. Use POST.\"}");
            return;
        }

        String body;
        try (InputStream is = exchange.getRequestBody()) {
            byte[] raw = is.readNBytes(65536);
            body = new String(raw, StandardCharsets.UTF_8);
        }

        String name = extractJsonField(body, "name").trim();
        String email = extractJsonField(body, "email").trim();
        String message = extractJsonField(body, "message").trim();

        if (name.length() < 2 || !EMAIL_PATTERN.matcher(email).matches() || message.length() < 5) {
            sendJson(exchange, 400,
                    "{\"ok\":false,\"error\":\"Please provide a valid name, email, and message.\"}");
            return;
        }

        String id = UUID.randomUUID().toString();
        String record = "{"
                + "\"id\":\"" + escapeJson(id) + "\","
                + "\"timestamp\":\"" + Instant.now() + "\","
                + "\"name\":\"" + escapeJson(name) + "\","
                + "\"email\":\"" + escapeJson(email) + "\","
                + "\"message\":\"" + escapeJson(message) + "\""
                + "}\n";

        Path logFile = resolveMessagesLog();
        synchronized (LOG_LOCK) {
            Files.writeString(
                    logFile,
                    record,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.APPEND
            );
        }

        sendJson(exchange, 200,
                "{\"ok\":true,\"id\":\"" + escapeJson(id) + "\",\"message\":\"Inquiry received and logged.\"}");
    }

    private static boolean handleCorsPreflight(HttpExchange exchange) throws IOException {
        Headers headers = exchange.getResponseHeaders();
        headers.set("Access-Control-Allow-Origin", "*");
        headers.set("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        headers.set("Access-Control-Allow-Headers", "Content-Type, Accept");
        if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
            return true;
        }
        return false;
    }

    private static void sendJson(HttpExchange exchange, int status, String json) throws IOException {
        Headers headers = exchange.getResponseHeaders();
        headers.set("Content-Type", "application/json; charset=UTF-8");
        headers.set("Access-Control-Allow-Origin", "*");
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static Path resolveIndexHtml() {
        Path direct = Path.of("index.html");
        if (Files.exists(direct)) {
            return direct;
        }
        Path parent = Path.of("..", "..", "index.html");
        if (Files.exists(parent)) {
            return parent;
        }
        return null;
    }

    private static Path resolveMessagesLog() {
        Path backendDir = Path.of("backend", "java");
        if (Files.isDirectory(backendDir)) {
            return backendDir.resolve("messages.jsonl");
        }
        return Path.of("messages.jsonl");
    }

    private static String extractJsonField(String json, String fieldName) {
        Pattern p = Pattern.compile("\"" + Pattern.quote(fieldName) + "\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\"");
        Matcher m = p.matcher(json);
        if (!m.find()) {
            return "";
        }
        return unescapeJson(m.group(1));
    }

    private static String unescapeJson(String s) {
        StringBuilder out = new StringBuilder(s.length());
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\\' && i + 1 < s.length()) {
                char next = s.charAt(++i);
                switch (next) {
                    case '"' -> out.append('"');
                    case '\\' -> out.append('\\');
                    case 'n' -> out.append('\n');
                    case 'r' -> out.append('\r');
                    case 't' -> out.append('\t');
                    case '/' -> out.append('/');
                    default -> out.append(next);
                }
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    private static String escapeJson(String s) {
        if (s == null) {
            return "";
        }
        StringBuilder out = new StringBuilder(s.length() + 8);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> out.append("\\\"");
                case '\\' -> out.append("\\\\");
                case '\n' -> out.append("\\n");
                case '\r' -> out.append("\\r");
                case '\t' -> out.append("\\t");
                default -> {
                    if (c < 0x20) {
                        out.append(String.format("\\u%04x", (int) c));
                    } else {
                        out.append(c);
                    }
                }
            }
        }
        return out.toString();
    }
}
