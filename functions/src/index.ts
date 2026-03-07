import {initializeApp} from "firebase-admin/app";
import {getFirestore} from "firebase-admin/firestore";
import {GoogleGenerativeAI} from "@google/generative-ai";
import {HttpsError, onCall} from "firebase-functions/v2/https";
import {
  enqueueDeferredRequest,
  enforceGlobalBudget,
  enforceUserQuota,
  ensureCircuitClosed,
  loadRuntimeConfig,
  markCircuitFailure,
  markCircuitSuccess,
  recordUsage
} from "./aiGuards";
import {
  buildFallbackCoaching,
  buildFallbackRankedTasks,
  buildFallbackSubtasks
} from "./fallback";

initializeApp();

type TaskBreakdownRequest = {
  taskTitle?: string;
  taskDescription?: string;
  locale?: string;
};

type SnoozeCoachingRequest = {
  taskTitle?: string;
  snoozeCount?: number;
  locale?: string;
};

type RankTasksRequest = {
  tasks?: string[];
  locale?: string;
};

function assertAuth(uid: string | undefined): string {
  if (!uid) throw new HttpsError("unauthenticated", "Authentication required.");
  return uid;
}

export const generateTaskBreakdown = onCall(async (request) => {
  const uid = assertAuth(request.auth?.uid);
  const data = request.data as TaskBreakdownRequest;
  const taskTitle = (data.taskTitle ?? "").trim();
  const taskDescription = (data.taskDescription ?? "").trim();
  const locale = (data.locale ?? "tr").toString();

  if (!taskTitle) {
    throw new HttpsError("invalid-argument", "taskTitle is required.");
  }

  const fallback = () => ({
    source: "fallback",
    subtasks: buildFallbackSubtasks(taskTitle),
    message: "Fallback generated due AI limits or provider issue."
  });

  try {
    const config = await loadRuntimeConfig();
    await ensureCircuitClosed();
    await enforceGlobalBudget(config);
    await enforceUserQuota(uid, config);

    const apiKey = process.env.GEMINI_API_KEY;
    if (!apiKey) {
      return fallback();
    }

    const prompt = [
      "Break the task into concise and actionable subtasks.",
      `Locale: ${locale}`,
      `Task title: ${taskTitle}`,
      `Task description: ${taskDescription || "-"}`,
      "Return 4-7 bullet items, plain text."
    ].join("\n");

    const genAI = new GoogleGenerativeAI(apiKey);
    const model = genAI.getGenerativeModel({model: "gemini-2.5-flash"});
    const result = await model.generateContent(prompt);
    const text = result.response.text();
    const subtasks = parseBullets(text).slice(0, 7);
    if (subtasks.length === 0) {
      return fallback();
    }

    await recordUsage(uid);
    await markCircuitSuccess();

    return {
      source: "model",
      subtasks,
      message: null
    };
  } catch (error) {
    await markCircuitFailure();
    if (error instanceof HttpsError && error.code === "resource-exhausted") {
      const queueId = await enqueueDeferredRequest(uid, "generateTaskBreakdown", {
        taskTitle,
        taskDescription,
        locale
      });
      return {
        ...fallback(),
        message: `Queued request id: ${queueId}. Fallback returned immediately.`
      };
    }
    return fallback();
  }
});

export const getSnoozeCoaching = onCall(async (request) => {
  const uid = assertAuth(request.auth?.uid);
  const data = request.data as SnoozeCoachingRequest;
  const taskTitle = (data.taskTitle ?? "").trim();
  const snoozeCount = Number(data.snoozeCount ?? 0);
  const locale = (data.locale ?? "tr").toString();

  if (!taskTitle) {
    throw new HttpsError("invalid-argument", "taskTitle is required.");
  }

  const fallbackPayload = buildFallbackCoaching(taskTitle, snoozeCount);

  try {
    const config = await loadRuntimeConfig();
    await ensureCircuitClosed();
    await enforceGlobalBudget(config);
    await enforceUserQuota(uid, config);

    const apiKey = process.env.GEMINI_API_KEY;
    if (!apiKey) {
      return {...fallbackPayload, source: "fallback"};
    }

    const prompt = [
      "You are a gentle productivity coach.",
      `Locale: ${locale}`,
      `Task: ${taskTitle}`,
      `Snooze count: ${snoozeCount}`,
      "Return one short supportive message and exactly 3 action options."
    ].join("\n");

    const genAI = new GoogleGenerativeAI(apiKey);
    const model = genAI.getGenerativeModel({model: "gemini-2.5-flash"});
    const result = await model.generateContent(prompt);
    const text = result.response.text();
    const lines = parseBullets(text);

    const message = lines[0] ?? fallbackPayload.message;
    const actions = lines.slice(1, 4);

    await recordUsage(uid);
    await markCircuitSuccess();

    return {
      message,
      actions: actions.length ? actions : fallbackPayload.actions,
      source: "model"
    };
  } catch (error) {
    await markCircuitFailure();
    if (error instanceof HttpsError && error.code === "resource-exhausted") {
      await enqueueDeferredRequest(uid, "getSnoozeCoaching", {
        taskTitle,
        snoozeCount,
        locale
      });
    }
    return {
      ...fallbackPayload,
      source: "fallback"
    };
  }
});

export const rankTasksByEnergyWindow = onCall(async (request) => {
  assertAuth(request.auth?.uid);
  const data = request.data as RankTasksRequest;
  const tasks = Array.isArray(data.tasks) ? data.tasks.filter((item) => typeof item === "string") : [];
  return {
    orderedTaskIds: buildFallbackRankedTasks(tasks),
    source: "fallback"
  };
});

export const verifySubscriptionPurchase = onCall(async (request) => {
  const uid = assertAuth(request.auth?.uid);
  const purchaseToken = (request.data?.purchaseToken ?? "").toString();
  const productId = (request.data?.productId ?? "").toString();

  if (!purchaseToken || !productId) {
    throw new HttpsError("invalid-argument", "purchaseToken and productId are required.");
  }

  // Production validation with Google Play Developer API should be added here.
  // This default flow marks premium as active when a non-empty token arrives.
  const db = getFirestore();
  await db
    .collection("users")
    .doc(uid)
    .collection("entitlements")
    .doc("current")
    .set(
      {
        planType: "PREMIUM",
        status: "ACTIVE",
        productId,
        purchaseToken,
        updatedAt: Date.now()
      },
      {merge: true}
    );

  return {success: true};
});

function parseBullets(text: string): string[] {
  return text
    .split("\n")
    .map((line) => line.replace(/^[-*0-9.)\s]+/, "").trim())
    .filter(Boolean);
}
