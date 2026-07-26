# MarketCanvas — AI Context System

> **This directory is the project's persistent memory.**
> Chat is temporary. Documentation is permanent.

## What Is This?

This `/ai` directory is a structured knowledge base that allows **any AI assistant** (Claude, GPT, Gemini, Codex, or any future model) to immediately understand, adopt, and continue work on this project — without requiring previous chat history.

## How to Use (For AI Assistants)

When starting a new session, **read these files in this exact order:**

| Step | File | Purpose |
|------|------|---------|
| 1 | `README.md` | You are here. Understand the system. |
| 2 | `ROLE.md` | Adopt the engineering persona and mindset. |
| 3 | `PROJECT.md` | Understand what we are building and why. |
| 4 | `CURRENT_STATE.md` | Know where the project is right now. |
| 5 | `ARCHITECTURE.md` | Understand how the system is designed. |
| 6 | `DECISIONS.md` | Know what was decided and why — never reverse without justification. |
| 7 | `TASKS.md` | See what needs to be done next. |
| 8 | `SESSION_LOG.md` | Read what happened in recent sessions. |
| 9 | `PATTERNS.md` | Follow established coding conventions. |
| 10 | `STACK.md` | Know the technology choices. |
| 11 | `GLOSSARY.md` | Understand domain-specific terminology. |

**Optional reads (use when relevant):**

| File | When to Read |
|------|-------------|
| `PROMPTS.md` | When the user asks you to perform a common task. |
| `AGENTS.md` | When asked to assume a specialized role (e.g., "act as security engineer"). |
| `CONTEXT_RULES.md` | When uncertain about how to behave. |
| `CHECKLIST.md` | Before implementing, merging, or deploying. |
| `ROADMAP.md` | When discussing future features or prioritization. |

## The One-Liner to Start a New Chat

The user will say something like:

> Read the AI context inside `/ai`, understand the project, adopt the documented role, then continue from where we stopped.

That single sentence should be enough to fully onboard.

## Maintenance Rules

After completing meaningful work in any session, the AI **must** update:

- [ ] `CURRENT_STATE.md` — Reflect what changed.
- [ ] `SESSION_LOG.md` — Append a dated entry summarizing the session.
- [ ] `TASKS.md` — Move tasks between Todo / In Progress / Completed.
- [ ] `DECISIONS.md` — If any architectural decision was made.
- [ ] `PATTERNS.md` — If a new coding convention was established.
- [ ] `ROADMAP.md` — If priorities shifted.

**Documentation is part of development, not optional afterwork.**

## Conflict Resolution

If there is a conflict between:
- **Chat history vs `/ai` documentation** → Trust the documentation unless the user explicitly overrides.
- **Code vs `/ai` documentation** → Report the discrepancy to the user. Do not silently guess.
- **User instruction vs documented decision** → Follow the user, then update the documentation to reflect the new decision.

## Directory Structure

```
/ai
├── README.md              ← You are here
├── ROLE.md                ← Engineering persona
├── PROJECT.md             ← What we are building
├── ARCHITECTURE.md        ← How the system works
├── DECISIONS.md           ← Architecture Decision Records
├── CURRENT_STATE.md       ← Project snapshot (living document)
├── ROADMAP.md             ← Long-term vision
├── TASKS.md               ← Living task board
├── SESSION_LOG.md         ← Chronological work history
├── PATTERNS.md            ← Coding conventions
├── STACK.md               ← Technology documentation
├── GLOSSARY.md            ← Domain vocabulary
├── PROMPTS.md             ← Reusable AI prompts
├── AGENTS.md              ← Specialized AI roles
├── CONTEXT_RULES.md       ← Behavioral rules for AI
└── CHECKLIST.md           ← Development checklists
```
