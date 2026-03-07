export function buildFallbackSubtasks(taskTitle: string): string[] {
  const title = (taskTitle || "").trim();
  if (!title) {
    return ["Write your goal in one clear sentence."];
  }

  const items: string[] = [
    `Define success criteria for "${title}".`,
    "Create one 25-minute focus block.",
    "Prepare required resources and notes.",
    "Complete the first small action.",
    "Review progress and schedule the next action."
  ];

  if (/study|exam|vize|final/i.test(title)) {
    items.splice(1, 0, "Split topics into 3 sections and assign a slot for each.");
    items.push("Solve at least 10 past questions and mark weak areas.");
  }

  if (/project|proje|app|uygulama/i.test(title)) {
    items.splice(1, 0, "Define MVP scope and success metric.");
    items.push("Break work into 30-60 minute technical tasks.");
  }

  return Array.from(new Set(items)).slice(0, 7);
}

export function buildFallbackCoaching(taskTitle: string, snoozeCount: number) {
  const message =
    snoozeCount >= 3
      ? `"${taskTitle}" was snoozed ${snoozeCount} times. Want to shrink it into a 10-minute action?`
      : "Start with a tiny step and regain momentum.";

  return {
    message,
    actions: [
      "Create a 10-minute micro task",
      "Move to tomorrow 09:00",
      "Archive for this week"
    ]
  };
}

export function buildFallbackRankedTasks(tasks: string[]): string[] {
  const quick = tasks.filter((task) => task.length <= 40);
  const deep = tasks.filter((task) => task.length > 40);
  return [...quick, ...deep];
}
