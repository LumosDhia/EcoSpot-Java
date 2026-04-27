# UI Unification Roadmap (JavaFX / FXML)

## Objective

Create a single, consistent UI system across all screens by unifying:

- Visual tokens (color, spacing, typography, radius, shadow)
- Core components (button, input, card, badge, header, status)
- Naming conventions and stylesheet structure
- Behavior states (hover, focus, disabled, selected, error)

Success means new and existing screens use shared classes and tokens instead of one-off inline styles.

## Dedicated UI Direction (Low-Change Strategy)

After reviewing the current implementation, the recommended dedicated UI is:

- **Base system:** `src/main/resources/style.css`
- **Reason:** it already defines the brand language used across major screens (Eco green palette, existing `.btn*`, nav, card patterns) and is loaded broadly.
- **Approach:** keep the current look and behavior, then migrate ticket/admin one-off styles to equivalent classes in `style.css` instead of redesigning.

### Why this is the safest choice

- Most global screens already align with `style.css` conventions.
- Ticket pages currently load both stylesheets and add many inline overrides; this is where drift comes from.
- Keeping `style.css` as the source of truth avoids large visual rework while still improving consistency.

### What to keep as-is (for minimal change)

- Primary eco palette (`#1b4332`, `#2d6a4f`, `#40916c`)
- Existing button families (`.btn`, `.btn-primary`, `.btn-outline`, `.btn-danger`)
- Existing nav/header structure and general card shadows

### What to normalize only (not redesign)

- Border radius values to a small set (4, 6, 10, 12)
- Body text scale to a stable set (12, 14, 16, 18, 24, 32, 48)
- Ticket neutral colors mapped into semantic classes (`success`, `warning`, `danger`, `muted`)
- Inline FXML styles moved to classes with near-identical visuals

## Current Problems to Fix

- Heavy use of inline styles in FXML creates drift and duplication.
- Two style systems (`style.css` and `ticket-style.css`) conflict in palette and shape language.
- Repeated component patterns are implemented differently (buttons, cards, forms).
- Typography and icon sizes are inconsistent between pages.
- Component naming is not semantic (`article-card` used outside article domain).

## Scope

In scope:

- `src/main/resources/**/*.fxml`
- `src/main/resources/style.css`
- `src/main/resources/ticket-style.css`
- Any other UI stylesheet loaded by FXML screens

Out of scope:

- Backend/business logic
- Database changes
- Feature redesign beyond consistency improvements

## Principles

1. **Token-first**: all values come from shared semantic tokens.
2. **Class-first**: FXML nodes use style classes, not long inline style strings.
3. **Component-first**: repeated UI patterns become reusable primitives.
4. **Incremental rollout**: migrate high-impact screens first.
5. **No visual regressions**: every phase includes screenshot/manual QA.

## Target End State

### 1) Unified Design Tokens

Create one source of truth for:

- Color tokens: `--color-bg`, `--color-surface`, `--color-primary`, `--color-success`, `--color-warning`, `--color-danger`, `--color-text`, `--color-text-muted`, `--color-border`
- Radius tokens: `--radius-sm`, `--radius-md`, `--radius-lg`, `--radius-pill`
- Spacing scale: `--space-1` to `--space-8`
- Typography: `--font-size-xs` to `--font-size-4xl`
- Elevation: `--shadow-sm`, `--shadow-md`, `--shadow-lg`

### 2) Unified Primitive Components

Define canonical classes used across all modules:

- Buttons: `.btn`, `.btn-primary`, `.btn-secondary`, `.btn-ghost`, `.btn-danger`
- Inputs: `.input`, `.input-textarea`, `.input-error`
- Cards: `.card`, `.card-elevated`, `.card-header`, `.card-body`, `.card-footer`
- Status: `.badge`, `.badge-success`, `.badge-warning`, `.badge-danger`, `.badge-neutral`
- Layout utility classes for spacing and alignment

### 3) Unified Naming Conventions

- Use domain-neutral base names (`.content-card`) with modifiers as needed.
- Keep class names semantic, not page-specific.
- Avoid component misuse (for example, event cards should not reuse `article-card` by name).

## Roadmap by Phase

## Phase 0 - Baseline Audit and Freeze (1-2 days)

### Goals

- Lock down current inconsistencies and prevent new ones.

### Tasks

- Inventory all inline `-fx-` style usages per FXML file.
- Inventory duplicated values (colors, radii, shadow, font sizes).
- List all style classes currently in use and where.
- Add temporary rule: no new inline styles unless justified.

### Deliverables

- UI inventory spreadsheet or markdown table
- Baseline screenshots for core pages
- Migration tracking checklist

## Phase 1 - Token Foundation (2-3 days)

### Goals

- Create a shared token layer with `style.css` as source of truth and map `ticket-style.css` into it.

### Tasks

- Define shared token section at top of primary stylesheet.
- Map old values from both stylesheets into semantic tokens.
- Keep module-specific classes only when behavior differs functionally.
- Normalize key values:
  - Radius: choose 3-4 global values
  - Typography scale: choose fixed steps
  - Spacing scale: choose fixed steps
  - Shadow depth levels: choose fixed levels

### Deliverables

- Refactored base stylesheet with token section
- Compatibility mapping notes from old classes to new classes
- Deprecation list for `ticket-style.css` classes (kept temporarily, replaced incrementally)

## Phase 2 - Core Component Library (3-5 days)

### Goals

- Build reusable UI primitives and stop ad-hoc component styling.

### Tasks

- Implement unified button classes and state styles.
- Implement unified input/textarea/select styles and focus/error states.
- Implement unified card primitives for all list/detail modules.
- Implement badge/status classes used in tickets/admin areas.
- Add helper classes for common layouts (stack gaps, row alignments, wrappers).

### Deliverables

- Stable shared component class set
- Small visual reference page (optional but recommended)

## Phase 3 - High-Impact Screen Migration (4-7 days)

### Goals

- Migrate screens with most inconsistency and user visibility first.

### Priority Order

1. Ticket flows
   - `src/main/resources/ticket/CreateTicket.fxml`
   - `src/main/resources/ticket/MyTickets.fxml`
   - `src/main/resources/ticket/PendingTickets.fxml`
   - `src/main/resources/ticket/TicketDetail.fxml`
2. Blog/event authoring and detail flows
   - `src/main/resources/blog/NewArticle.fxml`
   - `src/main/resources/event/EventDetail.fxml` (if present)
3. Admin/user management surfaces
   - `src/main/resources/admin/UserManagement.fxml` (if present)
4. Shared card components
   - `src/main/resources/blog/BlogCard.fxml`
   - `src/main/resources/event/EventCard.fxml`
   - `src/main/resources/event/EventAdminCard.fxml`

### Migration Rules

- Replace inline styles with shared classes first.
- Use tokenized class values only.
- Avoid introducing new one-off classes during migration.
- Rename misleading classes (example: replace `article-card` with neutral `content-card`).
- Keep visual output close to current UI unless there is a clear usability issue.

### Minimal-Change Mapping Starter (Use First)

- `ticket-card` -> keep class temporarily, point its values to shared card tokens
- inline green primary buttons -> `.btn .btn-primary`
- inline transparent bordered action buttons -> `.btn .btn-outline`
- inline gray input fields -> shared `.input` class
- inline status pills -> shared `.badge-*` variants (`success`, `warning`, `danger`, `neutral`)

This mapping-first method reduces risk and avoids a full component rewrite.

### Deliverables

- Migrated FXML files with reduced inline style usage
- Updated stylesheets and class mapping notes

## Phase 4 - Low-Impact Screens and Polish (2-4 days)

### Goals

- Finish remaining screens and remove legacy drift.

### Tasks

- Migrate lower-traffic/secondary pages.
- Remove dead CSS selectors not referenced by any FXML.
- Tighten spacing/typography rhythm across all pages.
- Unify icon sizing classes (`.icon-sm`, `.icon-md`, `.icon-lg`, `.icon-xl`).

### Deliverables

- Fully migrated UI layer
- Dead CSS cleanup report

## Phase 5 - Governance and Regression Prevention (1-2 days)

### Goals

- Ensure consistency is maintained over time.

### Tasks

- Add a UI contribution guide to README or dedicated doc.
- Add review checklist for PRs:
  - No unnecessary inline styles
  - Uses shared primitives
  - Uses semantic tokens
  - States are covered (hover/focus/disabled/error)
- Add optional static check script for inline style detection.
- Assign owner(s) for design-system changes.

### Deliverables

- UI contribution standards
- PR checklist template
- Inline-style guard script (optional)

## Implementation Details

## Stylesheet Architecture Proposal

- Keep one base stylesheet as source of truth for tokens and primitives.
- Keep optional module extension stylesheets for module-only layout rules.
- Avoid redefining primitives in module files.

Suggested order in stylesheet:

1. Token definitions
2. Reset/base typography
3. Layout utilities
4. Primitives (button/input/card/badge)
5. Composite components
6. Module-specific overrides (minimal)

## Naming Standard

- Base component: `.card`, `.btn`, `.input`, `.badge`
- Variant modifier: `.btn-primary`, `.badge-warning`
- Context modifier only when required: `.card-ticket`, `.card-event`
- Utility class prefix (optional): `.u-` (example: `.u-gap-md`)

## Definition of Done (DoD)

A screen is considered unified when:

- No non-trivial inline style strings remain.
- All major controls use shared primitives.
- Color/spacing/typography values map to tokenized system.
- Hover/focus/disabled/error states are present and consistent.
- Visual review on target resolution passes.

## Risk Register and Mitigation

- **Risk:** Visual regressions during migration  
  **Mitigation:** Keep before/after screenshots and migrate screen-by-screen.

- **Risk:** Hidden dependencies on old classes  
  **Mitigation:** Maintain class mapping and deprecate old classes gradually.

- **Risk:** Team reintroduces inline styles  
  **Mitigation:** Add PR checklist and inline-style detection script.

- **Risk:** Scope creep into redesign  
  **Mitigation:** Restrict changes to consistency and token alignment.

## Suggested Work Breakdown (2-Week Example)

Week 1:

- Day 1-2: Audit + freeze
- Day 3-4: Tokens and stylesheet consolidation
- Day 5: Core button/input/card primitives

Week 2:

- Day 1-3: Ticket + blog/event high-impact screen migration
- Day 4: Admin/shared cards migration
- Day 5: QA pass, cleanup, governance docs

## Tracking Checklist

- [ ] Baseline audit completed
- [ ] Shared tokens finalized
- [ ] Primitive classes finalized
- [ ] Ticket screens migrated
- [ ] Blog/event screens migrated
- [ ] Admin screens migrated
- [ ] Shared cards unified
- [ ] Dead CSS removed
- [ ] UI standards documented
- [ ] Final regression QA completed

## Optional Next Step

Create a dedicated `UI_STYLE_GUIDE.md` that shows each primitive class with a screenshot and usage examples so future contributors can build consistent screens quickly.
