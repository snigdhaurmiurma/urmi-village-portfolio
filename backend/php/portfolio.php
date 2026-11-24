<?php
declare(strict_types=1);

/**
 * Portfolio Data API (PHP 8+)
 * GET JSON endpoint returning Urmi Urma Snigdha's profile, Light/Dark themes,
 * and project list (with optional ?category=... filter).
 */

header('Content-Type: application/json; charset=UTF-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Accept');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(204);
    exit;
}

if ($_SERVER['REQUEST_METHOD'] !== 'GET') {
    http_response_code(405);
    echo json_encode([
        'ok' => false,
        'error' => 'Method not allowed. Use GET.',
    ]);
    exit;
}

$projects = [
    [
        'id' => 'fundbridge',
        'shopName' => 'FundBridge Vault',
        'category' => 'FinTech & Software',
        'badge' => 'FinTech Engineering & Cryptographic Security',
        'year' => '2026',
        'title' => 'FundBridge — Distributed Micro-Finance & Anti-Predatory Lending Engine',
        'tags' => ['React', 'Node.js', 'Express', 'SQLite (WAL)', 'AES-256-GCM', 'SHA-256 Chain'],
    ],
    [
        'id' => 'medvision-ai',
        'shopName' => 'MedVision Micro-Lab',
        'category' => 'Computer Vision',
        'badge' => 'Digital Image Processing & Computer Vision',
        'year' => '2026',
        'title' => 'MedVision-AI — Cellular & Microscopic Image Analysis Pipeline',
        'tags' => ['Python', 'OpenCV', 'Scikit-Image', 'Watershed Algorithm', 'Otsu Thresholding'],
    ],
    [
        'id' => 'marketpulse-mining',
        'shopName' => 'MarketPulse Exchange',
        'category' => 'Data Mining',
        'badge' => 'Data Mining & Predictive Analytics',
        'year' => '2026',
        'title' => 'MarketPulse — Customer Behavior Mining & Churn Intelligence',
        'tags' => ['Python', 'Pandas', 'Scikit-Learn', 'Apriori Algorithm', 'K-Means RFM'],
    ],
    [
        'id' => 'docuquery-rag',
        'shopName' => 'DocuQuery Archive',
        'category' => 'AI & NLP',
        'badge' => 'Applied Machine Learning & NLP',
        'year' => '2026',
        'title' => 'DocuQuery-AI — Enterprise Document Intelligence & Semantic Search (RAG)',
        'tags' => ['Python', 'PyTorch', 'FAISS', 'Vector Search', 'Cross-Encoder Reranker'],
    ],
    [
        'id' => 'green-computing',
        'shopName' => 'The Green Observatory',
        'category' => 'AI & NLP',
        'badge' => 'Academic Research · Pending Publication',
        'year' => '2026',
        'title' => 'Green Computing & AI Safety Guardrail Benchmarking',
        'tags' => ['Python', 'CodeCarbon', 'NVML', 'PyTorch', 'GPU Profiling'],
    ],
    [
        'id' => 'cross-lingual-nli',
        'shopName' => 'The Babel Library',
        'category' => 'AI & NLP',
        'badge' => 'Final Year Design Project (FYDP) · UIU',
        'year' => '2026',
        'title' => 'Cross-Lingual NLI & Multilingual LLM Benchmarking (CLSC)',
        'tags' => ['PyTorch', 'Transformers', 'LaBSE', 'XLM-R', 'Llama-3.2'],
    ],
    [
        'id' => 'moving-canvas',
        'shopName' => 'UIU Moving Canvas Atelier',
        'category' => 'Branding & Livery',
        'badge' => '1st Prize Winner · BDT 50,000',
        'year' => '2026',
        'title' => 'UIU Moving Canvas — Official Student Transit Bus Fleet Livery',
        'tags' => ['Adobe Illustrator', 'Adobe Photoshop', 'Brand Architecture', 'Vehicle Livery'],
    ],
    [
        'id' => 'edusmart',
        'shopName' => 'EduSmart Study Café',
        'category' => 'UI/UX & Product',
        'badge' => 'UI/UX & Product Prototype',
        'year' => '2025',
        'title' => 'EduSmart — Academic Companion & Interactive Study Ecosystem',
        'tags' => ['Figma', 'Design Systems', 'Auto-Layout', 'User Flows', 'Prototyping'],
    ],
    [
        'id' => 'bangla-news-mismatch',
        'shopName' => 'The Dhaka Press & Lens',
        'category' => 'Computer Vision',
        'badge' => 'NLP & Computer Vision Research',
        'year' => '2025',
        'title' => 'Bangla News Image-Text Mismatch Detection',
        'tags' => ['Python', 'NLP', 'OpenCV', 'Exploratory Data Analysis', 'Multimodal AI'],
    ],
    [
        'id' => 'synapse-iot',
        'shopName' => "Synapse Tinkerer's Workshop",
        'category' => 'Embedded & IoT',
        'badge' => 'UIU Champion · Microprocessor & IoT',
        'year' => '2024',
        'title' => 'Synapse — Sensor Telemetry & Microcontroller Hub',
        'tags' => ['Python', 'Microcontrollers (C/C++)', 'IoT Telemetry', 'Circuit Design'],
    ],
];

$categoryFilter = isset($_GET['category']) && is_string($_GET['category']) ? trim($_GET['category']) : '';
if ($categoryFilter !== '' && strcasecmp($categoryFilter, 'All') !== 0) {
    $projects = array_values(array_filter(
        $projects,
        static fn(array $p): bool => strcasecmp($p['category'], $categoryFilter) === 0
    ));
}

$payload = [
    'ok' => true,
    'profile' => [
        'name' => 'Urmi Urma Snigdha',
        'brand' => 'urmi.design',
        'headline' => 'CSE Graduate · UI/UX Designer & Software Engineer · Dhaka',
        'university' => 'United International University (UIU)',
        'location' => 'Dhaka, Bangladesh',
        'email' => 'urmiurmasnigdha@gmail.com',
        'linkedin' => 'https://www.linkedin.com/in/urmi-urma-snigdha-4836362a1/',
        'github' => 'https://github.com/snigdhaurmiurma',
    ],
    'themes' => [
        'light' => [
            'name' => 'Warm Paper & Daylight Village',
            'paper' => '#FAF6EE',
            'paperDeep' => '#F3ECE0',
            'ink' => '#241F1B',
            'accent' => '#C2654A',
        ],
        'dark' => [
            'name' => 'Candlelit Espresso & Starlit Night Village',
            'paper' => '#161311',
            'paperDeep' => '#1F1A17',
            'ink' => '#F4EDE2',
            'accent' => '#E08A5E',
            'starsCount' => 42,
        ],
    ],
    'projects' => $projects,
];

echo json_encode($payload, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE | JSON_UNESCAPED_SLASHES);
