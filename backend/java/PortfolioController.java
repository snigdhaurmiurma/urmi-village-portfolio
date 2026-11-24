package design.urmi.portfolio.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Pattern;

/**
 * Spring Boot 3 REST Controller for Urmi Urma Snigdha's Dual-Mode Portfolio.
 * Ready to drop into a Spring Boot 3 (Java 17/21) project for Render, Railway, or Fly.io.
 *
 * Set CONTACT_API_URL in index.html to "https://your-java-api.onrender.com/api/contact".
 */
@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class PortfolioController {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final List<ContactRecord> inbox = new CopyOnWriteArrayList<>();

    public record ProjectSummary(
            String id,
            String shopName,
            String category,
            String badge,
            String year,
            String title,
            List<String> tags
    ) {}

    public record ContactRequest(String name, String email, String message) {}

    public record ContactRecord(String id, String timestamp, String name, String email, String message) {}

    private static final List<ProjectSummary> PROJECTS = List.of(
            new ProjectSummary(
                    "green-computing",
                    "The Green Observatory",
                    "AI Research & NLP",
                    "Academic Research · Pending Publication",
                    "2026",
                    "Green Computing & AI Safety Guardrail Benchmarking",
                    List.of("Python", "CodeCarbon", "NVML", "PyTorch", "GPU Profiling")
            ),
            new ProjectSummary(
                    "cross-lingual-nli",
                    "The Babel Library",
                    "AI Research & NLP",
                    "Final Year Design Project · UIU",
                    "2026",
                    "Cross-Lingual NLI & Multilingual LLM Benchmarking",
                    List.of("PyTorch", "Transformers", "LaBSE", "XLM-R", "Llama-3.2")
            ),
            new ProjectSummary(
                    "edusmart",
                    "EduSmart Study Café",
                    "UI/UX & AI Product",
                    "UI/UX & AI Prototype",
                    "2025",
                    "EduSmart — Academic AI Companion & Study Ecosystem",
                    List.of("Figma", "Design Systems", "Auto-Layout", "User Flows", "Prototyping")
            ),
            new ProjectSummary(
                    "moving-canvas",
                    "UIU Moving Canvas Atelier",
                    "Branding & Identity",
                    "1st Prize Winner · BDT 50,000",
                    "2026",
                    "UIU Moving Canvas — Official Student Transit Bus Fleet Livery",
                    List.of("Adobe Illustrator", "Adobe Photoshop", "Brand Architecture", "Vector Art")
            ),
            new ProjectSummary(
                    "bangla-news-mismatch",
                    "The Dhaka Press & Lens",
                    "AI Research & NLP",
                    "NLP & Computer Vision Research",
                    "2025",
                    "Bangla News Image-Text Mismatch Detection",
                    List.of("Python", "NLP", "OpenCV", "Exploratory Data Analysis", "Matplotlib")
            ),
            new ProjectSummary(
                    "synapse-iot",
                    "Synapse Tinkerer's Workshop",
                    "Data & IoT",
                    "UIU Lab Champion Project",
                    "2024",
                    "Synapse — Sensor Telemetry & Microcontroller Hub",
                    List.of("Python", "Microcontrollers (C/C++)", "IoT Telemetry", "Circuit Design")
            )
    );

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
                "status", "ok",
                "service", "urmi-village-portfolio",
                "runtime", "Java " + System.getProperty("java.version"),
                "timestamp", Instant.now().toString()
        );
    }

    @GetMapping("/portfolio")
    public Map<String, Object> portfolio(@RequestParam(required = false) String category) {
        List<ProjectSummary> filtered = (category == null || category.isBlank() || "All".equalsIgnoreCase(category))
                ? PROJECTS
                : PROJECTS.stream()
                        .filter(p -> p.category().equalsIgnoreCase(category.trim()))
                        .toList();

        return Map.of(
                "ok", true,
                "profile", Map.of(
                        "name", "Urmi Urma Snigdha",
                        "brand", "urmi.design",
                        "headline", "CSE Graduate · UI/UX Designer & Software Engineer · Dhaka",
                        "university", "United International University (UIU)",
                        "email", "urmiurmasnigdha@gmail.com",
                        "linkedin", "https://www.linkedin.com/in/urmi-urma-snigdha-4836362a1/",
                        "github", "https://github.com/snigdhaurmiurma"
                ),
                "themes", Map.of(
                        "light", Map.of("name", "Warm Paper & Daylight Village", "paper", "#FAF6EE", "accent", "#C2654A"),
                        "dark", Map.of("name", "Candlelit Espresso & Starlit Night Village", "paper", "#161311", "accent", "#E08A5E", "starsCount", 42)
                ),
                "projects", filtered
        );
    }

    @PostMapping("/contact")
    public ResponseEntity<Map<String, Object>> submitContact(@RequestBody ContactRequest request) {
        String name = request != null && request.name() != null ? request.name().trim() : "";
        String email = request != null && request.email() != null ? request.email().trim() : "";
        String message = request != null && request.message() != null ? request.message().trim() : "";

        if (name.length() < 2 || !EMAIL_PATTERN.matcher(email).matches() || message.length() < 5) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "ok", false,
                    "error", "Please provide a valid name, email, and message."
            ));
        }

        ContactRecord record = new ContactRecord(
                UUID.randomUUID().toString(),
                Instant.now().toString(),
                name,
                email,
                message
        );
        inbox.add(record);

        return ResponseEntity.ok(Map.of(
                "ok", true,
                "id", record.id(),
                "message", "Inquiry received by Urmi's Spring Boot portfolio API."
        ));
    }
}
