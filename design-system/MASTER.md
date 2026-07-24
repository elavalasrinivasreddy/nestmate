# Nestmate — Design System (MASTER)

> Source of truth for UI decisions. Platform: **Android · Jetpack Compose · Material 3**.
> Generated with ui-ux-pro-max, then **reconciled against the existing codebase** — the
> skill's raw output (vibrant purple, block-based, web landing pattern) was rejected where
> it conflicted with the shipped Material 3 theme and the product's trust-first positioning.

## Product lens
- **Type:** Marketplace / Directory (two-sided: room-holders ↔ seekers).
- **Audience:** Students + relocating professionals. Priority = *trust and clarity*, not youthful flash.
- **Implication:** Search/feed is the primary surface; reduce friction to browse. Trust signals
  (verification badges, ratings, real photos) are first-class, not decoration.

## Style
- **System:** Material 3 (M3). ElevatedCard for content/forms, Hero headers, geometric placeholders.
- **Mood:** Calm, credible, approachable. Generous whitespace, soft elevation, rounded corners.
- **Rejected:** "Vibrant & Block-based" / high-saturation purple / gaming-youth energy — off-brand
  for a housing-trust product. Do **not** repaint to `#7C3AED`.

## Color — KEEP existing Nest palette (`ui/theme/Color.kt`)
Calm teal-green "nest" primary + warm terracotta tertiary. Full light + dark tonal sets already
defined and wired through `NestmateTheme`. This already satisfies the "trust = green" signal.

| Role | Light | Dark |
|---|---|---|
| Primary | `#1F6F5C` | `#8AD6BF` |
| Primary container | `#A6F2DC` | `#005141` |
| Secondary | `#4B635B` | `#B2CCC1` |
| Tertiary (accent) | `#B05A2A` | `#FAB785` |
| Background/Surface | `#FBFDF9` | `#191C1A` |
| Error | `#BA1A1A` | `#FFB4AB` |

**Rules**
- Always consume `MaterialTheme.colorScheme.*` — never hardcode hex in screens/components.
- Functional color (error/success) must pair with icon or text, never color-alone.
- Verify FG/BG pairs ≥ 4.5:1 (body) / 3:1 (large) in **both** themes.

## Typography — the real gap to fix
`Type.kt` currently overrides only `bodyLarge`; every other role is a Compose default with no
weight hierarchy. Target a full M3 type scale with deliberate weights:

| Role | Size / Line | Weight |
|---|---|---|
| headlineMedium (Hero) | 28 / 36 | 700 |
| titleLarge (section) | 22 / 28 | 600 |
| titleMedium (card title) | 16 / 24 | 600 |
| bodyLarge | 16 / 24 | 400 |
| bodyMedium | 14 / 20 | 400 |
| labelLarge (buttons/labels) | 14 / 20 | 500 |
| labelSmall (meta) | 12 / 16 | 500 |

- Bold 600–700 headings, Regular 400 body, Medium 500 labels.
- Optional (deferred): swap `FontFamily.Default` for a downloadable **Lexend** (headings) — the
  skill's "corporate/trustworthy/accessible" pairing. Not required; weight hierarchy matters more.
- Use tabular figures for rent/price and rating numbers to avoid layout shift.

## Spacing, elevation, shape
- 4/8dp spacing rhythm. Section rhythm tiers 16 / 24 / 32.
- Consistent M3 elevation scale for card < sheet < dialog; no random shadow values.
- Respect safe areas / system bars for the top bar and any bottom CTA.

## Motion
- Micro-interactions 150–300ms; complex ≤400ms. Animate transform/alpha, not layout bounds.
- Press feedback on all tappables (M3 state layers / ripple). Respect reduced-motion.

## Non-negotiables (checklist)
- [ ] No emoji as structural icons — vector icons only (Material Icons already in use).
- [ ] Touch targets ≥ 48dp; use `Modifier.minimumInteractiveComponentSize()` / padding for small icons.
- [ ] Every icon-only button has `contentDescription`.
- [ ] One primary CTA per screen; secondary actions visually subordinate.
- [ ] Loading state for any async > ~300ms (skeleton/spinner); disable buttons mid-request.
- [ ] Empty states: message + action, never a blank list.
- [ ] Dark mode verified independently, not inferred from light.

## Page overrides
Page-specific deviations live in `design-system/pages/<screen>.md`. If absent, this MASTER applies.
