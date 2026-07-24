# Notifications

## What ships now (client-only, no billing)
- **Runtime permission**: `POST_NOTIFICATIONS` (Android 13+) requested once via a
  rationale card on first Home load (`HomeScreen` + `NotificationHelper`).
- **Unread badge** on the Inbox tab: computed from `conversations` where the
  last message is from the other participant and newer than a **device-local**
  "last read" timestamp (`SettingsRepository` read-tracking in SharedPreferences).
  Opening the Inbox marks conversations read and clears the badge.
- **Foreground local notifications**: while the app process is alive, a new
  inbound message posts a local notification (`NotificationHelper`, channel
  `messages`). De-duplicated per conversation by `lastMessage.sentAt`.

## What is NOT covered (needs FCM + server + billing)
Local notifications only fire while the app is running. To notify a user when the
app is **backgrounded or killed**, you need push:

1. Add Firebase Cloud Messaging: `com.google.firebase:firebase-messaging` +
   a `FirebaseMessagingService` that calls `NotificationHelper.showMessage(...)`.
2. Store each device's FCM token on the user doc (`users/{uid}.fcmTokens`).
3. Send from a **trusted** server on new message — a Cloud Function triggered by
   `onCreate` of `conversations/{cid}/messages/{mid}` that looks up the recipient's
   tokens and calls the FCM Admin API. (Cloud Functions requires the Blaze/billing
   plan — currently deferred, same blocker as Storage.)

Read-state is currently device-local; once push lands, move `lastReadAt` to the
conversation doc (per-participant map) so unread stays consistent across devices.
