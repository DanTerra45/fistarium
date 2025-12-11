import * as functions from "firebase-functions";
import * as admin from "firebase-admin";

admin.initializeApp();

export const sendNewsNotification = functions.firestore
  .document("news/{newsId}")
  .onCreate(async (snapshot, context) => {
    const newsData = snapshot.data();
    if (!newsData) return;

    const defaultTitle = newsData.title;
    const defaultSummary = newsData.summary;
    const translations = newsData.translations || {};

    // 1. Send to English Users (news_en)
    const enData = translations["en"] || {};
    const enTitle = enData.title || defaultTitle;
    const enBody = enData.summary || defaultSummary;

    const payloadEn = {
      notification: {
        title: enTitle,
        body: enBody,
      },
      data: {
        navigate_to: "news"
      },
      topic: "news_en"
    };

    // 2. Send to Spanish Users (news_es)
    const esData = translations["es"] || {};
    const esTitle = esData.title || defaultTitle;
    const esBody = esData.summary || defaultSummary;

    const payloadEs = {
      notification: {
        title: esTitle,
        body: esBody,
      },
      data: {
        navigate_to: "news"
      },
      topic: "news_es"
    };

    // Send messages
    // Note: We use try-catch to prevent one failure from stopping the other
    try {
        await admin.messaging().send(payloadEn as any);
        console.log("Sent English notification");
    } catch (e) {
        console.error("Error sending English notification", e);
    }

    try {
        await admin.messaging().send(payloadEs as any);
        console.log("Sent Spanish notification");
    } catch (e) {
        console.error("Error sending Spanish notification", e);
    }
    
    console.log("Notifications process completed for news:", context.params.newsId);
  });
