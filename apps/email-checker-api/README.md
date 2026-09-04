# Email Checker API (`email-checker-api`)

Simple Express & TypeScript microservice that checks if an email address exists and is reachable by performing a direct SMTP handshake (`EHLO` → `MAIL FROM` → `RCPT TO` → `QUIT`) without delivering any message.

---

## What It Does

1. **DNS MX Lookup**: Queries DNS for the target domain's MX records and picks the primary server.
2. **Direct SMTP Handshake**: Connects to port 25 and asks the server `RCPT TO:<target@domain>`.
3. **Verdict**: Returns whether the address exists (`250`), is rejected/unknown (`550`), is temporarily deferred (`451`), or connection is rejected.
4. **Clean Disconnect**: Immediately issues `QUIT` without sending message data.

---

## Endpoints

### `GET /health`

Healthcheck.

### `POST /verify` (or `GET /verify?email=...`)

**Body:**

```json
{
  "email": "user@example.com"
}
```

**Response:**

```json
{
  "email": "user@example.com",
  "user": "user",
  "domain": "example.com",
  "validSyntax": true,
  "mxRecords": ["mail.example.com"],
  "selectedMx": "mail.example.com",
  "responseCode": 250,
  "responseMessage": "250 2.1.5 Recipient OK",
  "verdict": "deliverable",
  "details": "The mail server 'mail.example.com' confirmed that mailbox 'user@example.com' exists and accepts messages (250).",
  "durationMs": 350,
  "logs": [...]
}
```
