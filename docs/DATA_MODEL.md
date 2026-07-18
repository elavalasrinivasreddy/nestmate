# Nestmate — Data Model (Cloud Firestore)

*Living document. Last updated: 2026-06-15.*

Firestore is document-based. Below are the collections, their fields, relationships, the indexes the queries will need, and the security-rules plan. Photos depend on Firebase Storage and are **deferred** (fields are reserved but unused in v1).

## Collections overview

```
users/{uid}
listings/{listingId}                 # "I have a room" (vacancy)
requirements/{requirementId}         # "I need a room"
conversations/{conversationId}
conversations/{conversationId}/messages/{messageId}
users/{uid}/bookmarks/{itemId}
```

## `users/{uid}`

| Field | Type | Notes |
|---|---|---|
| `uid` | string | == Firebase Auth UID |
| `displayName` | string | |
| `email` | string? | **Optional (legacy)** kept in case users link it later |
| `phoneNumber` | string? | E.164, set after phone verification |
| `photoUrl` | string? | **deferred** (Storage) |
| `userType` | string enum | `seeker` \| `room_holder` \| `both` |
| `occupationType` | string enum | `student` \| `professional` \| `other` |
| `bio` | string? | |
| `preferredLocations` | array<string> | city/area names |
| `lifestyle` | map | `{ smoking, food, sleepSchedule, cleanliness }` — for compatibility |
| `verification` | map | `{ phoneVerified: bool }` |
| `createdAt` / `updatedAt` | timestamp | |

## `listings/{listingId}` — vacancy

| Field | Type | Notes |
|---|---|---|
| `id` | string | == doc id |
| `ownerUid` | string | author; drives write rules |
| `title` / `description` | string | |
| `roomType` | string enum | `private` \| `shared` \| `entire` |
| `rentAmount` | number | |
| `currency` | string | ISO 4217 (region-aware) |
| `depositAmount` | number? | |
| `location` | map | `{ city, area, lat?, lng?, geohash? }` |
| `availableFrom` | timestamp | |
| `preferences` | map? | `{ gender?, occupationType?, smoking?, food? }` |
| `photoUrls` | array<string> | **deferred** |
| `status` | string enum | `active` \| `paused` \| `filled` |
| `createdAt` / `updatedAt` | timestamp | |

## `requirements/{requirementId}` — seeker ask

| Field | Type | Notes |
|---|---|---|
| `id` | string | |
| `seekerUid` | string | author; drives write rules |
| `title` / `description` | string | |
| `budgetMin` / `budgetMax` | number | |
| `currency` | string | |
| `preferredLocations` | array<string> | |
| `moveInDate` | timestamp | |
| `roomType` | string enum | |
| `lifestyle` | map? | preferences mirror of user lifestyle |
| `status` | string enum | `active` \| `paused` \| `fulfilled` |
| `createdAt` / `updatedAt` | timestamp | |

## `conversations/{conversationId}` (+ `messages` subcollection)

Conversation id = sorted participant-uid pair (+ optional context id) so a pair doesn't create duplicates.

| Field | Type | Notes |
|---|---|---|
| `id` | string | |
| `participantUids` | array<string> | exactly 2; drives access rules |
| `participantsMeta` | map | per-uid `{ displayName, photoUrl? }` (denormalized) |
| `context` | map? | `{ type: listing\|requirement, id }` — what it's about |
| `lastMessage` | map | `{ text, senderUid, sentAt }` for the list preview |
| `updatedAt` | timestamp | sort key for conversation list |

`messages/{messageId}`

| Field | Type | Notes |
|---|---|---|
| `id` | string | |
| `senderUid` | string | |
| `text` | string | |
| `sentAt` | timestamp | |
| `readBy` | array<string>? | |

## `users/{uid}/bookmarks/{itemId}`

| Field | Type | Notes |
|---|---|---|
| `itemId` | string | listingId or requirementId |
| `itemType` | string enum | `listing` \| `requirement` |
| `snapshot` | map | denormalized `{ title, price, location }` for fast render |
| `createdAt` | timestamp | |

## Queries & indexes

v1 keeps location filtering as **city/area string equality** (index-friendly) rather than radius search. Likely composite indexes:

- `listings`: `status == active` + `location.city == X` ordered by `createdAt desc`
- `listings`: `status == active` + `roomType == X` + `rentAmount` range
- `requirements`: `status == active` + `preferredLocations array-contains X` ordered by `createdAt desc`
- `conversations`: `participantUids array-contains uid` ordered by `updatedAt desc`

Firestore will emit the exact composite-index link on first run; add them to `firestore.indexes.json` as they appear. Radius/geohash search is a later (geo) enhancement.

## Security rules plan

Starts in test mode; **must be locked before v1 is "done"** (Phase 9). Target rules:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{db}/documents {
    function signedIn() { return request.auth != null; }
    function isOwner(uid) { return signedIn() && request.auth.uid == uid; }

    match /users/{uid} {
      allow read: if signedIn();
      allow write: if isOwner(uid);
      match /bookmarks/{itemId} {
        allow read, write: if isOwner(uid);
      }
    }
    match /listings/{id} {
      allow read: if signedIn();
      allow create: if signedIn() && request.resource.data.ownerUid == request.auth.uid;
      allow update, delete: if signedIn() && resource.data.ownerUid == request.auth.uid;
    }
    match /requirements/{id} {
      allow read: if signedIn();
      allow create: if signedIn() && request.resource.data.seekerUid == request.auth.uid;
      allow update, delete: if signedIn() && resource.data.seekerUid == request.auth.uid;
    }
    match /conversations/{cid} {
      allow read, update: if signedIn() && request.auth.uid in resource.data.participantUids;
      allow create: if signedIn() && request.auth.uid in request.resource.data.participantUids;
      match /messages/{mid} {
        allow read: if signedIn() &&
          request.auth.uid in get(/databases/$(db)/documents/conversations/$(cid)).data.participantUids;
        allow create: if signedIn() && request.resource.data.senderUid == request.auth.uid;
      }
    }
  }
}
```

## Storage plan (deferred)

When Storage is enabled: `listings/{listingId}/{imageId}.jpg`, `profiles/{uid}.jpg`; write allowed only to the owning user, read for signed-in users. Tracked as a known dependency in `STATUS_TRACKER.md`.
