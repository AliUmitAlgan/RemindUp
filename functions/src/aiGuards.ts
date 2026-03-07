import {getFirestore} from "firebase-admin/firestore";
import {HttpsError} from "firebase-functions/v2/https";

const DEFAULT_RPM_LIMIT = 5;
const DEFAULT_RPD_LIMIT = 20;
const DEFAULT_SAFE_RPD_BUDGET = Math.floor(DEFAULT_RPD_LIMIT * 0.8);
const DEFAULT_PREMIUM_QUOTA_WHEN_ENABLED = 40;
const DEFAULT_FREE_QUOTA_WHEN_ENABLED = 6;
const CIRCUIT_BREAKER_OPEN_MS = 15 * 60 * 1000;
const CIRCUIT_BREAKER_FAILURE_THRESHOLD = 3;

export type RuntimeAiConfig = {
  paidTierEnabled: boolean;
  rpmLimit: number;
  rpdLimit: number;
  safeRpdBudget: number;
  freeAiDailyQuota: number;
  premiumAiDailyQuota: number;
};

export async function loadRuntimeConfig(): Promise<RuntimeAiConfig> {
  const db = getFirestore();
  const doc = await db.collection("app_config").doc("ai_limits").get();
  if (!doc.exists) {
    return {
      paidTierEnabled: false,
      rpmLimit: DEFAULT_RPM_LIMIT,
      rpdLimit: DEFAULT_RPD_LIMIT,
      safeRpdBudget: DEFAULT_SAFE_RPD_BUDGET,
      freeAiDailyQuota: 0,
      premiumAiDailyQuota: 0
    };
  }

  const data = doc.data() ?? {};
  const rpdLimit = numberValue(data.rpdLimit, DEFAULT_RPD_LIMIT);
  return {
    paidTierEnabled: booleanValue(data.paidTierEnabled, false),
    rpmLimit: numberValue(data.rpmLimit, DEFAULT_RPM_LIMIT),
    rpdLimit,
    safeRpdBudget: numberValue(data.safeRpdBudget, Math.floor(rpdLimit * 0.8)),
    freeAiDailyQuota: numberValue(data.freeAiDailyQuota, DEFAULT_FREE_QUOTA_WHEN_ENABLED),
    premiumAiDailyQuota: numberValue(data.premiumAiDailyQuota, DEFAULT_PREMIUM_QUOTA_WHEN_ENABLED)
  };
}

export async function ensureCircuitClosed(): Promise<void> {
  const db = getFirestore();
  const now = Date.now();
  const snap = await db.collection("ai_state").doc("circuit").get();
  const openUntil = snap.get("openUntil") ?? 0;
  if (typeof openUntil === "number" && openUntil > now) {
    throw new HttpsError("unavailable", "AI circuit breaker is open.");
  }
}

export async function markCircuitSuccess(): Promise<void> {
  const db = getFirestore();
  await db.collection("ai_state").doc("circuit").set(
    {
      failures: 0,
      openUntil: 0,
      updatedAt: Date.now()
    },
    {merge: true}
  );
}

export async function markCircuitFailure(): Promise<void> {
  const db = getFirestore();
  const circuitRef = db.collection("ai_state").doc("circuit");
  await db.runTransaction(async (tx) => {
    const snap = await tx.get(circuitRef);
    const failures = Number(snap.get("failures") ?? 0) + 1;
    const now = Date.now();
    const openUntil = failures >= CIRCUIT_BREAKER_FAILURE_THRESHOLD ? now + CIRCUIT_BREAKER_OPEN_MS : 0;
    tx.set(
      circuitRef,
      {
        failures,
        openUntil,
        updatedAt: now
      },
      {merge: true}
    );
  });
}

export async function enforceGlobalBudget(config: RuntimeAiConfig): Promise<void> {
  const db = getFirestore();
  const now = new Date();
  const dayKey = formatDay(now);
  const minuteKey = formatMinute(now);
  const dayRef = db.collection("ai_usage").doc(`day_${dayKey}`);
  const minuteRef = db.collection("ai_usage").doc(`minute_${minuteKey}`);

  const [daySnap, minuteSnap] = await Promise.all([dayRef.get(), minuteRef.get()]);
  const dayCount = Number(daySnap.get("count") ?? 0);
  const minuteCount = Number(minuteSnap.get("count") ?? 0);

  if (minuteCount >= config.rpmLimit) {
    throw new HttpsError("resource-exhausted", "Global AI RPM limit reached.");
  }
  if (dayCount >= config.safeRpdBudget) {
    throw new HttpsError("resource-exhausted", "Global AI daily budget reached.");
  }
}

export async function enforceUserQuota(uid: string, config: RuntimeAiConfig): Promise<void> {
  const db = getFirestore();
  const dayKey = formatDay(new Date());

  const [entitlementSnap, usageSnap] = await Promise.all([
    db.collection("users").doc(uid).collection("entitlements").doc("current").get(),
    db.collection("users").doc(uid).collection("usage").doc(dayKey).get()
  ]);

  if (!config.paidTierEnabled) {
    throw new HttpsError("resource-exhausted", "AI is disabled until paid tier is enabled.");
  }

  const planType = (entitlementSnap.get("planType") ?? "FREE").toString().toUpperCase();
  const customQuota = entitlementSnap.get("aiDailyQuota");
  const quota =
    typeof customQuota === "number"
      ? customQuota
      : planType === "PREMIUM"
        ? config.premiumAiDailyQuota
        : config.freeAiDailyQuota;
  const used = Number(usageSnap.get("aiCount") ?? 0);

  if (used >= quota) {
    throw new HttpsError("resource-exhausted", "User AI daily quota reached.");
  }
}

export async function recordUsage(uid: string): Promise<void> {
  const db = getFirestore();
  const now = new Date();
  const dayKey = formatDay(now);
  const minuteKey = formatMinute(now);
  const dayRef = db.collection("ai_usage").doc(`day_${dayKey}`);
  const minuteRef = db.collection("ai_usage").doc(`minute_${minuteKey}`);
  const usageRef = db.collection("users").doc(uid).collection("usage").doc(dayKey);

  await db.runTransaction(async (tx) => {
    const daySnap = await tx.get(dayRef);
    const minuteSnap = await tx.get(minuteRef);
    const usageSnap = await tx.get(usageRef);

    tx.set(dayRef, {count: Number(daySnap.get("count") ?? 0) + 1, updatedAt: Date.now()}, {merge: true});
    tx.set(minuteRef, {count: Number(minuteSnap.get("count") ?? 0) + 1, updatedAt: Date.now()}, {merge: true});
    tx.set(usageRef, {aiCount: Number(usageSnap.get("aiCount") ?? 0) + 1, updatedAt: Date.now()}, {merge: true});
  });
}

export async function enqueueDeferredRequest(
  uid: string,
  feature: string,
  payload: unknown
): Promise<string> {
  const db = getFirestore();
  const ref = await db.collection("ai_queue").add({
    uid,
    feature,
    payload,
    status: "queued",
    createdAt: Date.now()
  });
  return ref.id;
}

function formatDay(date: Date): string {
  return date.toISOString().slice(0, 10).replace(/-/g, "");
}

function formatMinute(date: Date): string {
  const iso = date.toISOString();
  return `${iso.slice(0, 10).replace(/-/g, "")}${iso.slice(11, 16).replace(":", "")}`;
}

function numberValue(value: unknown, defaultValue: number): number {
  if (typeof value === "number" && Number.isFinite(value)) return value;
  return defaultValue;
}

function booleanValue(value: unknown, defaultValue: boolean): boolean {
  if (typeof value === "boolean") return value;
  return defaultValue;
}
