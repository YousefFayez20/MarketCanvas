# Reusable Prompts — MarketCanvas

> Copy-paste these when you need the AI to perform common tasks.  
> Adjust the specifics inside `[brackets]`.

---

## Onboarding (Start of Every New Session)

```
Read the AI context inside /ai, understand the project, adopt the documented role, then continue from where we stopped.
```

---

## Engineering Handbook (Deep Learning Guide for a Task)

> Use this prompt when starting a new task. It generates a complete, handbook-style teaching document — the same format used in `/ai/handbook_july14/` and `/ai/handbook_july15/`.

```
Read the AI context inside /ai. Adopt the documented role from ROLE.md.

Generate a complete Engineering Handbook for: [TASK DESCRIPTION]

The handbook must follow this exact structure:

1. **Where We Are Now** — Current state analysis. What exists today, what's missing, what's broken. Reference specific files and line numbers.

2. **What You Must Study Before Coding** — Required reading with links. Concepts the developer must understand before touching any code. Organized as: Required → Recommended → Deep Dive.

3. **The Problem** — Why we need this. Real scenarios showing what goes wrong without it. Use Mermaid diagrams to visualize flows.

4. **Deep Dive** — The core concepts and classes involved. How the framework/library handles this internally. Sequence diagrams showing data flow.

5. **Architecture Decision** — ADR format. List all available options with pros/cons/trade-offs. State the recommendation and why.

6. **Implementation Guide** — For each file to create or modify:
   - Explain WHAT the file does and WHY it exists
   - Explain WHERE it goes in the package structure and WHY
   - List the key classes, annotations, and methods needed
   - Provide the METHOD SIGNATURES and KEY ANNOTATIONS only
   - Explain what each annotation/method does
   - Do NOT provide the full implementation code
   - Provide HINTS and DIRECTIONS for the developer to write it themselves
   - Example: "This method needs to: (1) create a recoverer using X, (2) configure it with Y, (3) return Z. The key class is `DefaultErrorHandler` — look at its constructor that accepts a `ConsumerRecordRecoverer` and `BackOff`."

7. **Before vs. After** — Show what changes in existing files. Show the BEFORE code and describe WHAT should change and WHY, but let the developer write the AFTER code.

8. **Testing Plan** — What to test, expected results, verification commands.

9. **Verification Steps** — Exact curl commands, Docker commands, log patterns to look for.

10. **Future Evolution** — What breaks at scale, how this evolves.

11. **Reflection Questions** — 5 questions that test deep understanding. The developer must answer these BEFORE coding.

12. **Study Materials** — Organized as Required / Recommended / Deep Dive with links and reasons.

13. **Implementation Checklist** — Ordered steps the developer follows to implement.

CRITICAL RULES:
- Do NOT give complete implementation code. Give hints, directions, syntax, class names, method signatures, and annotations — but the developer writes the code.
- Every concept must be explained with WHY, not just WHAT.
- Use Mermaid diagrams for all flows and architectures.
- Use tables for comparisons and trade-offs.
- Reference specific files in the project with absolute paths.
- Follow the teaching philosophy from ROLE.md: explain the problem → explain the approaches → explain the recommendation → explain the trade-offs → then guide implementation.

Save the handbook as an artifact file named: session[N]_handbook_[topic].md
```

### Shortened Version (Quick Reference)

```
Read /ai context. Adopt ROLE.md role.
Generate an Engineering Handbook for [TASK].
Follow the 13-chapter structure from PROMPTS.md.
Hints and directions only — no complete code.
Save as artifact.
```

## Generate REST Endpoint

```
Following the patterns in /ai/PATTERNS.md, create a REST endpoint for [FEATURE].

Requirements:
- Controller in [context]/infrastructure/
- Application Service in [context]/application/
- DTO for request and response
- Input validation
- Proper HTTP status codes
- Error handling

Follow the package structure and naming conventions documented in PATTERNS.md.
Update CURRENT_STATE.md and SESSION_LOG.md after completion.
```

---

## Review Architecture

```
Review the current architecture for [COMPONENT/FEATURE].

Check:
- Does it follow the patterns in /ai/PATTERNS.md?
- Does it respect module boundaries documented in /ai/ARCHITECTURE.md?
- Are there any violations of decisions in /ai/DECISIONS.md?
- Are there any new risks?
- Is there unnecessary complexity?

Provide a structured review with severity ratings.
```

---

## Generate Database Migration

```
Create a Flyway migration for [CHANGE].

Requirements:
- Follow V{version}__{description}.sql naming
- Include rollback script
- Consider data preservation
- Document in DECISIONS.md if this is a schema design choice
```

---

## Refactor Service

```
Refactor [SERVICE/CLASS] to follow the patterns documented in /ai/PATTERNS.md.

Specifically check:
- Separation of concerns
- Single responsibility
- Proper layer placement (domain vs application vs infrastructure)
- No cross-context imports
- Error handling pattern
- Logging pattern

Do not change behavior. Only improve structure.
```

---

## Review Security

```
Perform a security review of [COMPONENT].

Check:
- Input validation
- Authentication requirements
- Authorization (resource-level access control)
- Data exposure in API responses
- SQL injection vectors
- Secret management
- Audit logging

Reference the security requirements in /ai/PROJECT.md.
```

---

## Generate Tests

```
Generate tests for [CLASS/FEATURE].

Requirements:
- Unit tests for domain logic (plain JUnit 5, no Spring context)
- Integration tests if database interaction is involved
- Test the public API of aggregates, not internal state
- Include edge cases and error scenarios
- Follow naming: should_[expected]_when_[condition]
```

---

## Explain Implementation

```
Explain how [FEATURE/COMPONENT] works in this project.

Include:
- What problem it solves
- How data flows through it
- Which files are involved
- What patterns are used
- What the trade-offs are
- How it would evolve at scale
```

---

## Add New Bounded Context

```
Create a new bounded context for [CONTEXT_NAME].

Requirements:
- Follow the package structure in /ai/PATTERNS.md
- Create domain/, application/, infrastructure/ sub-packages
- Add the context to the ArchUnit test in ArchitectureEnforcementTest.java
- Update /ai/ARCHITECTURE.md with the new context
- Define initial domain events
- Document the decision in /ai/DECISIONS.md
```

---

## Debug Issue

```
There is a bug: [DESCRIPTION].

Before fixing:
1. Check /ai/CURRENT_STATE.md for known bugs
2. Check /ai/DECISIONS.md for relevant architectural constraints
3. Explain the root cause
4. Propose a fix with trade-offs
5. Implement after I approve

After fixing:
- Update CURRENT_STATE.md (remove from known bugs)
- Update SESSION_LOG.md
```

---

## End of Session

```
We're done for today. Please:
1. Update /ai/CURRENT_STATE.md with what changed
2. Append to /ai/SESSION_LOG.md with today's entry
3. Update /ai/TASKS.md (move completed items, add new ones)
4. Update /ai/DECISIONS.md if any architectural decisions were made
5. Update /ai/PATTERNS.md if any new conventions emerged
6. Summarize what was accomplished and what should be done next
```
