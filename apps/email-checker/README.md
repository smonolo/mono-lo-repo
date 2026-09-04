# Email Checker Web App (`email-checker`)

Simple Next.js web application for verifying email reachability via direct SMTP handshake without sending any messages.

---

## Features

- **Direct Verification**: Checks target MX servers directly on port 25 (`EHLO` → `MAIL FROM` → `RCPT TO` → `QUIT`).
- **Clear Status**: Displays whether the mailbox exists, is rejected, or is deferred.
- **Handshake Inspector**: Expandable trace showing each command and server response.

---

## Development

```bash
# In mono-lo-repo root:
npm run dev:email-checker:full
```

Open [http://localhost:3000](http://localhost:3000).
