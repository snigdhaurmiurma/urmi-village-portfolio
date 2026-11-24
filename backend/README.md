# Portfolio Backends (PHP 8+ & Java 17/21)

Static hosts like **Vercel** and **GitHub Pages** serve `index.html` directly in the browser without running a PHP or JVM server. By default, `index.html` keeps `CONTACT_API_URL = ""` so the contact form submits via **FormSubmit** with zero downtime.

Whenever you host either backend below, update `CONTACT_API_URL` near the top of the `<script>` tag in `index.html` (e.g., `"/backend/php/contact.php"` or `"https://your-java-api.onrender.com/api/contact"`).

## PHP 8+ (`backend/php/`)
- `contact.php` — `POST` JSON endpoint with validation, sanitization, `messages.json` logging, and `mail()` dispatch (deployable on cPanel, Hostinger, InfinityFree, or Railway).
- `portfolio.php` — `GET` JSON API returning profile, Light/Dark themes, and projects (supports `?category=`).

Verify syntax locally:
```bash
php -l backend/php/contact.php
php -l backend/php/portfolio.php
```

## Java 17/21 (`backend/java/`)
- `PortfolioServer.java` — Zero-dependency Java HTTP + REST server (`GET /`, `GET /api/health`, `GET /api/portfolio`, `POST /api/contact`).
- `PortfolioController.java` — Spring Boot 3 `@RestController` ready for Render, Railway, or Fly.io.

Compile and run the zero-dependency server anywhere with JDK 17+:
```bash
javac backend/java/PortfolioServer.java && java -cp backend/java PortfolioServer
```
