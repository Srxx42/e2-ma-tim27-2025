import { onDocumentCreated, onDocumentUpdated } from "firebase-functions/v2/firestore";
import * as logger from "firebase-functions/logger";
import * as admin from "firebase-admin";

admin.initializeApp();
const db = admin.firestore();
const messaging = admin.messaging();

// --- Funkcija 1: Šalje notifikaciju pozvanom članu ---
export const sendAllianceInviteNotification = onDocumentCreated("alliance_invites/{inviteId}", async (event) => {
  const snapshot = event.data;
  if (!snapshot) {
    logger.log("No data associated with event");
    return;
  }
  const inviteData = snapshot.data();
  const recipientId = inviteData.receiverId;

  if (!recipientId) { return; }

  try {
    const userDoc = await db.collection("users").doc(recipientId).get();
    if (!userDoc.exists) { return; }

    const fcmToken = userDoc.data()?.fcmToken;

    if (fcmToken) {
      // Pravimo poruku za 'send' metodu
      const message = {
        token: fcmToken,
        data: {
          type: "INVITATION",
          inviteId: inviteData.inviteId,
          allianceId: inviteData.allianceId,
          allianceName: inviteData.allianceName,
          inviterUsername: inviteData.inviterUsername,
        },
      };

      logger.log(`Sending notification to token: ${fcmToken}`);
      // <<< IZMENA OVDE: Koristimo 'send' umesto 'sendToDevice'
      await messaging.send(message);
      logger.log("Notification sent successfully!");
    }
  } catch (error) {
    logger.error("Error sending invite notification:", error);
  }
});


// --- Funkcija 2: Šalje notifikaciju lideru ---
export const onAllianceInviteAccepted = onDocumentUpdated("alliance_invites/{inviteId}", async (event) => {
  if (!event.data) { return; }

  const newData = event.data.after.data();
  const oldData = event.data.before.data();

  if (newData.status === "ACCEPTED" && oldData.status === "PENDING") {
    const leaderId = newData.senderId;
    if (!leaderId) { return; }

    try {
      const memberDoc = await db.collection("users").doc(newData.receiverId).get();
      const memberName = memberDoc.data()?.username || "Someone";

      const leaderDoc = await db.collection("users").doc(leaderId).get();
      if (!leaderDoc.exists) { return; }

      const fcmToken = leaderDoc.data()?.fcmToken;

      if (fcmToken) {
        // Pravimo poruku za 'send' metodu
        const message = {
          token: fcmToken,
          data: {
            type: "INVITATION_ACCEPTED",
            memberName: memberName,
            allianceName: newData.allianceName,
          },
        };
        logger.log(`Sending 'accepted' notification to leader ${leaderId}`);
        // <<< IZMENA OVDE: Koristimo 'send' umesto 'sendToDevice'
        await messaging.send(message);
        logger.log("'Accepted' notification sent successfully!");
      }
    } catch (error) {
      logger.error("Error sending 'accepted' notification:", error);
    }
  }
});