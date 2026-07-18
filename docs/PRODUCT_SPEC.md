# Nestmate — Product Spec

*Living document. Last updated: 2026-06-15.*

## Vision

A trusted place where someone moving to a new city can find **either a room or a roommate** — and reach the other person directly — without trawling scattered WhatsApp groups, Facebook posts, and sketchy classifieds.

## Problem

People relocating for study or work struggle to find shared accommodation because listings are fragmented across many platforms, often fake or stale, roommates are hard to discover, seekers have no structured way to advertise what they want, and trust is low because nothing is verified. Both sides waste significant time.

## Target users

**Primary**
- International students and domestic students relocating to universities
- Early-career and relocating professionals (incl. IT employees moving cities)

**Secondary**
- Live-in landlords and existing tenants seeking a roommate
- Small property managers and co-living operators

Global product, but **launched one market at a time** — region-aware recommendations surface what's near the user. (See `ROADMAP.md` and the strategy note in the root analysis: build global-ready, go to market narrow.)

## Value proposition

Unlike whole-property rental portals, Nestmate is **room-level and roommate-level**, and it is **two-sided**: it serves both "I have a room" and "I'm looking for a room." Trust (verification) and compatibility (lifestyle filters) are first-class, not afterthoughts.

## v1 scope — trust-first two-sided core

**In scope**
1. **Accounts & profiles** — phone auth (OTP), profile with verification flags.
2. **Vacancy listings** — create, edit, delete, view ("I have a room").
3. **Requirement listings** — create, edit, delete, view ("I need a room").
4. **Discovery** — search + filters: location, budget range, room type.
5. **Messaging** — in-app real-time 1:1 chat.
6. **Bookmarks** — save listings and requirements.

**Explicitly out of v1** (deferred — see roadmap)
- Photo uploads (waiting on Firebase Storage / billing).
- Reviews & ratings (need usage volume to be meaningful).
- Roommate-matching algorithm / AI search & fraud detection (your edge — built once the core loop works).
- Payments, agreements, visit scheduling.
- iOS / web.

## Core user stories (v1)

- As a seeker, I can sign up with my phone number, verify it via OTP, and create a profile so others trust I'm real.
- As a room-holder, I can post a vacancy with rent, deposit, availability, and preferences.
- As a seeker, I can post what I'm looking for so room-holders can reach me.
- As either side, I can search and filter by location, budget, and room type.
- As either side, I can message a match in-app and save listings I like.

## Non-goals (for now)

- Not a whole-apartment rental portal (no competing with Zillow/MagicBricks on full units).
- Not a brokerage; Nestmate does not transact rent or deposits in v1.
- Not chasing many cities at once — depth over breadth.

## Success criteria (personal project)

This is built for craft and completeness, so "success" is defined by the build, not the market:
1. The full v1 loop works end-to-end on a real device against live Firebase.
2. The codebase is clean, documented, and architected like a real product (testable, layered).
3. Security rules lock data down properly before v1 is called "done."

## Beyond v1

Photos → reviews/ratings + reporting → AI compatibility matching, conversational search, and fake-listing detection → (later) payments, agreements, co-living tools. Full detail in `ROADMAP.md`.
