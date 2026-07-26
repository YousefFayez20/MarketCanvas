# Context Rules — MarketCanvas

> These rules are mandatory for every AI working on this project.  
> Violations will lead to architectural drift, technical debt, and lost context.

---

## Rule 1: Always Read README First

Before doing any work, read `/ai/README.md` and follow the prescribed reading order. Do not skip files.

## Rule 2: Never Assume Undocumented Decisions

If a technical decision is not documented in `/ai/DECISIONS.md`, do not assume it was intentional. Ask the user before proceeding.

## Rule 3: Update SESSION_LOG After Every Session

Before ending a session, append a dated entry to `/ai/SESSION_LOG.md` summarizing:
- What was done
- Files changed
- Decisions made
- Bugs found
- Next step

## Rule 4: Update DECISIONS After Architecture Changes

If any architectural choice is made during a session (new pattern, new technology, design trade-off), add an ADR entry to `/ai/DECISIONS.md`.

## Rule 5: Update CURRENT_STATE After Completing Features

After implementing a feature or fixing a bug, update `/ai/CURRENT_STATE.md` to reflect:
- New features added
- Bugs fixed or discovered
- Tech debt changes

## Rule 6: Never Contradict Documented Architecture

If the user asks for something that contradicts `/ai/ARCHITECTURE.md` or `/ai/DECISIONS.md`:
1. Acknowledge the request.
2. Explain the conflict.
3. Ask for explicit confirmation before proceeding.
4. If confirmed, update the documentation to reflect the new decision.

## Rule 7: Documentation Is Source of Truth

If code conflicts with `/ai` documentation:
- Report the discrepancy to the user.
- Do not silently "fix" the documentation to match the code OR the code to match the documentation.
- Let the user decide which is correct.

## Rule 8: Follow Coding Patterns

All new code must follow the conventions documented in `/ai/PATTERNS.md`. This includes:
- Package structure
- Naming conventions
- Entity patterns
- Event patterns
- Error handling
- Testing

## Rule 9: Teach, Don't Just Implement

Before implementing anything significant:
1. Explain the concept
2. Present options with trade-offs
3. Recommend an approach with justification
4. Only then write code

This project exists for learning as much as for production.

## Rule 10: No Technology Without Justification

Do not introduce a new library, framework, or infrastructure component without:
1. Explaining what problem it solves
2. Documenting the decision in `/ai/DECISIONS.md`
3. Getting user approval

## Rule 11: Respect Module Boundaries

Never create direct imports between bounded contexts. All cross-context communication happens through:
- Events (domain events, Kafka messages)
- IDs (not shared entity references)

This is enforced by ArchUnit. Violation = test failure.

## Rule 12: Update TASKS After Work

After completing or discovering tasks:
- Move completed tasks to the `✅ Completed` section
- Add newly discovered work to `📋 Todo`
- Update priorities and dependencies

## Rule 13: Preserve Domain Language

Use the terminology defined in `/ai/GLOSSARY.md`. Do not introduce synonyms for established terms. A "Watchlist" is always a "Watchlist", never a "portfolio tracker" or "asset list."

## Rule 14: Test Before Declaring Done

Before marking a task complete:
- Run `./mvnw clean compile` (must succeed)
- Run `./mvnw test` if tests exist (must pass)
- Verify ArchUnit boundaries are not violated

## Rule 15: Never Invent Project Details

If context is missing:
- Ask targeted questions.
- If assumptions are required, state them explicitly with `[ASSUMPTION]` prefix.
- Never fill gaps with fabricated information.

## Rule 16: Maintain the AI Context System

The `/ai` directory is infrastructure. Treat updates to these files with the same care as production code changes. They are not optional notes — they are the project's persistent memory.

## Rule 17: Always Shippable (MVP-First)

After completing every feature milestone, the application must be testable end-to-end through a real UI — not just through `curl` commands. If a backend feature doesn't have a corresponding frontend interaction point, a frontend sprint must be inserted before moving to the next infrastructure milestone. The app must always be demonstrable.
