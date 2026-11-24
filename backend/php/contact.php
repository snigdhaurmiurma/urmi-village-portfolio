<?php
declare(strict_types=1);

/**
 * Portfolio Contact Endpoint (PHP 8+)
 * Handles POST JSON contact form submissions with validation, sanitization,
 * messages.json persistence, and optional mail() dispatch.
 *
 * Deployable on cPanel, Hostinger, InfinityFree, Railway, or any PHP 8+ host.
 * Point CONTACT_API_URL in index.html to "/backend/php/contact.php" (or full URL).
 */

header('Content-Type: application/json; charset=UTF-8');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: POST, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type, Accept');

if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(204);
    exit;
}

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    http_response_code(405);
    echo json_encode([
        'ok' => false,
        'error' => 'Method not allowed. Use POST with a JSON body.',
    ]);
    exit;
}

$rawBody = file_get_contents('php://input');
$data = json_decode($rawBody ?: '', true);
if (!is_array($data)) {
    // Graceful fallback for standard form-encoded POST bodies
    $data = $_POST;
}

$sanitizeLine = static function (mixed $value, int $maxLength): string {
    $str = is_string($value) ? trim($value) : '';
    $str = preg_replace('/[\r\n\t]+/', ' ', $str) ?? '';
    $str = strip_tags($str);
    return mb_substr($str, 0, $maxLength, 'UTF-8');
};

$sanitizeMessage = static function (mixed $value, int $maxLength): string {
    $str = is_string($value) ? trim($value) : '';
    $str = str_replace("\0", '', $str);
    $str = strip_tags($str);
    return mb_substr($str, 0, $maxLength, 'UTF-8');
};

$name = $sanitizeLine($data['name'] ?? '', 120);
$email = $sanitizeLine($data['email'] ?? '', 180);
$message = $sanitizeMessage($data['message'] ?? '', 4000);

$errors = [];
if ($name === '' || mb_strlen($name, 'UTF-8') < 2) {
    $errors['name'] = 'Please enter your name (at least 2 characters).';
}
if ($email === '' || filter_var($email, FILTER_VALIDATE_EMAIL) === false) {
    $errors['email'] = 'Please enter a valid email address.';
}
if ($message === '' || mb_strlen($message, 'UTF-8') < 5) {
    $errors['message'] = 'Please enter a message (at least 5 characters).';
}

if ($errors !== []) {
    http_response_code(400);
    echo json_encode([
        'ok' => false,
        'error' => 'Validation failed.',
        'fields' => $errors,
    ]);
    exit;
}

$entry = [
    'id' => bin2hex(random_bytes(8)),
    'timestamp' => gmdate('c'),
    'name' => $name,
    'email' => $email,
    'message' => $message,
    'ip' => $_SERVER['REMOTE_ADDR'] ?? 'unknown',
];

$logPath = __DIR__ . '/messages.json';
$logged = false;

$fp = @fopen($logPath, 'c+');
if ($fp !== false) {
    if (flock($fp, LOCK_EX)) {
        $existingRaw = stream_get_contents($fp);
        $existing = json_decode($existingRaw ?: '[]', true);
        if (!is_array($existing)) {
            $existing = [];
        }
        $existing[] = $entry;
        ftruncate($fp, 0);
        rewind($fp);
        fwrite($fp, json_encode($existing, JSON_PRETTY_PRINT | JSON_UNESCAPED_UNICODE) . PHP_EOL);
        fflush($fp);
        flock($fp, LOCK_UN);
        $logged = true;
    }
    fclose($fp);
}

$recipient = 'urmiurmasnigdha@gmail.com';
$subject = 'New Inquiry from urmi-village-portfolio';
$mailBody = "Name: {$name}\nEmail: {$email}\nTimestamp (UTC): {$entry['timestamp']}\n\nMessage:\n{$message}\n";
$safeReplyTo = str_replace(["\r", "\n"], '', $email);
$headers = [
    'From: Portfolio Contact <noreply@urmi.design>',
    "Reply-To: {$safeReplyTo}",
    'Content-Type: text/plain; charset=UTF-8',
    'X-Mailer: PHP/' . PHP_VERSION,
];

$mailSent = function_exists('mail') && @mail($recipient, $subject, $mailBody, implode("\r\n", $headers));

http_response_code(200);
echo json_encode([
    'ok' => true,
    'id' => $entry['id'],
    'message' => 'Thank you! Your inquiry has been received.',
    'logged' => $logged,
    'mailSent' => $mailSent,
]);
