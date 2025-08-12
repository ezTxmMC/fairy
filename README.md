# Fairy - Minimalistic Minecraft Cloud System (Beginner Guide)

Welcome to Fairy! This guide is written for newbies. It explains what Fairy does and how to use it in the simplest way possible.

Table of Contents
- What is Fairy?
- Quick Start
- What you can do (very short)
- How to run Fairy safely
- Basic Console commands
- API at a glance (token, endpoints)
- Where to find help and support

1) What is Fairy?
Fairy is a small, friendly tool to manage Minecraft servers (like Vanilla, Spigot, Paper, Velocity, and more) and normal Java programs. It gives you:
- A tiny command-line shell (console)
- A REST API to control things from other apps
- Auto-start for the API so you can use the web API right away
- Simple, readable layouts and color in the console

2) Quick Start (noob-friendly)
- Prerequisites: Java 22+ installed
- Run Fairy: open a terminal and type:
  - java -jar fairy.jar
- Right after starting, Fairy will show an API URL and a token. You will use this token to talk to the REST API.
- The API starts on port 8080 by default (you can change with a simple flag, see below).

3) What you can do (very short)
- Use the console to start and manage Minecraft servers
- Use the REST API to do the same things from other apps
- See who is connected and basic stats
- Store a secret API token securely in a .secret file

4) Starting Fairy safely
- Just run: java -jar fairy.jar
- If you want the API on another port, start Fairy like:
  - java -Dfairy.api.port=9090 -jar fairy.jar
- If you want to turn off auto-starting the API, use:
  - java -Dfairy.api.autostart=false -jar fairy.jar

5) Basic Console commands (summary)
- help — show the list of available commands
- api start <port> — start API server on the given port
- api status — show API status and port
- minecraft start <id> <type> <dir> [script] — start a Minecraft server
- minecraft stop <id> — stop a server
- process list — see running Java processes
- process attach <id> — attach to a process for input
- exit — quit Fairy

6) API at a glance
- Base URL: http://localhost:8080/api (or your chosen port)
- Token: use the token shown when Fairy starts (Authentication header)
- Common endpoints (examples, all require a token):
  - GET /health
  - GET /servers
  - POST /servers/{id}/start
  - POST /servers/{id}/command
  - GET /players/{name}/stats
  - POST /auth/regenerate (get a new token)

7) Secrets and safety
- Fairy saves a token in a file called .secret
- The token is used to talk to the API
- Keep the .secret file safe and don’t share it

8) Help and support
- If something doesn’t work, check the console messages first
- Look for “API on port X” or “Server started” messages
- If you’re stuck, ask in the project’s community channels or open an issue

9) Quick deployment tips
- For a simple home setup, run Fairy on one computer with the API on 8080
- For a small network, run Fairy as the central controller and have many Minecraft servers on different folders
- Consider using Docker or a system service for reliability (docs included in the project)

10) Next steps (for future readers)
- Learn how to add more plugins or server types
- Build dashboards to visualize status
- Automate backups and monitoring

That’s it — a super simple beginner guide to get you started with Fairy. If you want more details, we can add a separate beginner-friendly tutorial for each feature (API, servers, and processes) in future updates.
