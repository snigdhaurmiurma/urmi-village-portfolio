/**
 * Vercel Serverless Contact Relay (/api/contact)
 * Delivers portfolio contact form messages directly to urmiurmasnigdha@gmail.com
 * with zero browser CORS or ad-blocker interference.
 *
 * Priority chain:
 *  1. Web3Forms (set WEB3FORMS_ACCESS_KEY in Vercel Environment Variables)
 *  2. Resend    (set RESEND_API_KEY in Vercel Environment Variables)
 *  3. FormSubmit server-to-server relay (with proper Origin/Referer headers)
 */
const RECIPIENT_EMAIL = "urmiurmasnigdha@gmail.com";
const DEFAULT_WEB3FORMS_KEY = "64bffa9d-5e3e-49a3-940e-15899149cae0";

export default async function handler(req, res) {
  res.setHeader("Access-Control-Allow-Origin", "*");
  res.setHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
  res.setHeader("Access-Control-Allow-Headers", "Content-Type, Accept");

  if (req.method === "OPTIONS") {
    return res.status(204).end();
  }
  if (req.method !== "POST") {
    return res.status(405).json({ ok: false, message: "Method not allowed" });
  }

  try {
    const body = typeof req.body === "string" ? JSON.parse(req.body || "{}") : (req.body || {});
    const name = String(body.name || "").trim();
    const email = String(body.email || "").trim();
    const organization = String(body.organization || "").trim();
    const subject = String(body.subject || "General Inquiry").trim();
    const message = String(body.message || "").trim();
    const honey = String(body._honey || body.botcheck || "").trim();

    if (honey) {
      return res.status(200).json({ ok: true, success: true });
    }
    if (name.length < 2 || !/^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(email) || message.length < 4) {
      return res.status(400).json({ ok: false, message: "Please provide a valid name, email, and message." });
    }

    const composedMessage = (organization ? `Organization: ${organization}\n\n` : "") + message;
    const mailSubject = `Portfolio Inquiry (${subject}) — ${name}`;

    // 1. Web3Forms (100% reliable, free, zero captcha)
    const web3Key = process.env.WEB3FORMS_ACCESS_KEY || DEFAULT_WEB3FORMS_KEY;
    if (web3Key) {
      const w3Res = await fetch("https://api.web3forms.com/submit", {
        method: "POST",
        headers: { "Content-Type": "application/json", Accept: "application/json" },
        body: JSON.stringify({
          access_key: web3Key,
          name,
          email,
          subject: mailSubject,
          from_name: "Urmi Portfolio Contact Form",
          replyto: email,
          organization: organization || "Not specified",
          category: subject,
          message: composedMessage
        })
      });
      const w3Json = await w3Res.json().catch(() => ({}));
      if (w3Res.ok && w3Json.success) {
        return res.status(200).json({ ok: true, success: true, provider: "web3forms" });
      }
      return res.status(502).json({ ok: false, message: w3Json.message || "Web3Forms delivery failed" });
    }

    // 2. Resend API (if configured in Vercel)
    const resendKey = process.env.RESEND_API_KEY;
    if (resendKey) {
      const rRes = await fetch("https://api.resend.com/emails", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${resendKey}`
        },
        body: JSON.stringify({
          from: "Urmi Portfolio <onboarding@resend.dev>",
          to: [RECIPIENT_EMAIL],
          reply_to: email,
          subject: mailSubject,
          text: `New Portfolio Inquiry\n\nName: ${name}\nEmail: ${email}\nOrganization: ${organization || "Not specified"}\nCategory: ${subject}\n\nMessage:\n${message}`
        })
      });
      const rJson = await rRes.json().catch(() => ({}));
      if (rRes.ok) {
        return res.status(200).json({ ok: true, success: true, provider: "resend" });
      }
      return res.status(502).json({ ok: false, message: rJson.message || "Resend delivery failed" });
    }

    // 3. Server-to-server FormSubmit relay (bypasses browser CORS & ad-blockers)
    const originHeader = req.headers.origin || `https://${req.headers.host || "urmi-village-repo.vercel.app"}`;
    const fsRes = await fetch(`https://formsubmit.co/ajax/${RECIPIENT_EMAIL}`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Accept: "application/json",
        Origin: originHeader,
        Referer: `${originHeader}/`
      },
      body: JSON.stringify({
        name,
        email,
        organization: organization || "Not specified",
        subject,
        message: composedMessage,
        _subject: mailSubject,
        _template: "table"
      })
    });
    const fsJson = await fsRes.json().catch(() => ({}));
    const ok = fsJson && (fsJson.success === true || fsJson.success === "true");
    if (fsRes.ok && ok) {
      return res.status(200).json({ ok: true, success: true, provider: "formsubmit" });
    }
    return res.status(502).json({
      ok: false,
      needsActivation: /activat|confirm|verify/i.test(String(fsJson.message || "")),
      message: fsJson.message || `FormSubmit returned HTTP ${fsRes.status}`
    });
  } catch (err) {
    return res.status(500).json({ ok: false, message: err && err.message ? err.message : "Relay error" });
  }
}
